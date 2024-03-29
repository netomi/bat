/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.netomi.bat.classfile.verifier

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.attribute.ExceptionEntry
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.classfile.constant.DoubleConstant
import com.github.netomi.bat.classfile.constant.FloatConstant
import com.github.netomi.bat.classfile.constant.LongConstant
import com.github.netomi.bat.classfile.constant.Utf8Constant
import com.github.netomi.bat.classfile.instruction.*
import com.github.netomi.bat.classfile.instruction.JvmOpCode.*
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.util.JAVA_LANG_STRING_TYPE
import com.github.netomi.bat.util.asJvmType
import com.github.netomi.bat.util.parseDescriptorToJvmTypes
import kotlin.collections.ArrayDeque

class CodeAnalyzer private constructor(private val processors: List<FrameProcessor> = emptyList()): MethodAttributeVisitor {

    private var evaluated = Array(0) { false }
    private var status    = Array(0) { 0 }

    private var framesBefore: Array<Frame?> = Array(0) { null }
    private var framesAfter:  Array<Frame?> = Array(0) { null }

    private val blockAnalyser = BlockAnalyser()
    private val frameUpdater  = FrameUpdater()

    private var processingQueue: ArrayDeque<Pair<Int, () -> Unit>> = ArrayDeque()

    override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {}

    override fun visitCode(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
//        println("evaluating method ${method.getFullExternalMethodSignature(classFile)}: ")

        evaluated = Array(attribute.codeLength) { false }
        status    = Array(attribute.codeLength) { 0 }

        framesBefore = Array(attribute.codeLength) { null }
        framesAfter  = Array(attribute.codeLength) { null }

        processingQueue = ArrayDeque()

        evaluateCode(classFile, method, attribute)
    }

    private fun evaluateCode(classFile: ClassFile, method: Method, code: CodeAttribute) {
        val initialFrame = setupInitialFrame(classFile, method)

        for (exceptionEntry in code.exceptionTable.asReversed()) {
            enqueueExceptionHandler(classFile, exceptionEntry)
        }

        enqueueBasicBlock(0, initialFrame)

        while (processingQueue.isNotEmpty()) {
            val (nextBlockOffset, setupFrame) = processingQueue.removeLast()
            setupFrame()
            evaluateBasicBlock(classFile, method, code, nextBlockOffset)
        }
    }

    private fun enqueueExceptionHandler(classFile: ClassFile, exceptionEntry: ExceptionEntry) {
        val handlerPC = exceptionEntry.handlerPC

        val setupFrame: () -> Unit = {
//            println("setup frame for exception handler $exceptionEntry")
            val frame = framesBefore[exceptionEntry.startPC]!!.copy()
            frame.clearStack()

            val exceptionType = if (exceptionEntry.catchType == 0) {
                "Ljava/lang/Throwable;".asJvmType()
            } else {
                exceptionEntry.getCaughtExceptionClassName(classFile)!!.toJvmType()
            }

            frame.push(VerificationType.of(exceptionType))
            framesBefore[handlerPC] = frame
        }

        setStatusFlag(handlerPC, BLOCK_ENTRY)
        setStatusFlag(handlerPC, EXCEPTION_HANDLER)

        processingQueue.addLast(Pair(handlerPC, setupFrame))
    }

    private fun enqueueBasicBlock(offset: Int, frame: Frame) {
        setStatusFlag(offset, BLOCK_ENTRY)
        processingQueue.addLast(Pair(offset) { framesBefore[offset] = frame })
    }

    private fun setupInitialFrame(classFile: ClassFile, method: Method): Frame {
        val descriptor = method.getDescriptor(classFile)
        val (parameterTypes, _) = parseDescriptorToJvmTypes(descriptor)

        val frame = Frame.empty()

        var variableIndex = 0

        if (!method.isStatic) {
            val classType = classFile.className.toJvmType()
            if (method.getName(classFile) == "<init>") {
                frame.store(variableIndex++, UninitializedThisType.of(classType))
            } else {
                frame.store(variableIndex++, UninitializedThisType.of(classType))
            }
        }

        for (parameterType in parameterTypes) {
            val verificationType = VerificationType.of(parameterType)
            frame.store(variableIndex++, verificationType)
            if (verificationType.isCategory2) {
                variableIndex++
            }
        }

        return frame
    }

    private fun evaluateBasicBlock(classFile: ClassFile, method: Method, attribute: CodeAttribute, offset: Int) {
        var currentOffset = offset

//        println("starting block at offset $offset")
        while (!evaluated[currentOffset]) {
            evaluated[currentOffset] = true

            val instruction = JvmInstruction.create(attribute.code, currentOffset)

            instruction.accept(classFile, method, attribute, currentOffset, frameUpdater)

            for (processor in processors) {
                processor.handleInstruction(currentOffset, instruction, framesBefore[currentOffset]!!, framesAfter[currentOffset]!!)
            }
//            println("$currentOffset: $instruction")
//            println("    before: ${framesBefore[currentOffset]}")
//            println("    after: ${framesAfter[currentOffset]}")

            instruction.accept(classFile, method, attribute, currentOffset, blockAnalyser)

            if (!isFlagSet(currentOffset, BLOCK_EXIT)) {
                val length     = instruction.getLength(offset)
                val oldOffset  = currentOffset
                currentOffset += length

                framesBefore[currentOffset] = framesAfter[oldOffset]
            }
        }
//        println("finished block")
    }

    inner class FrameUpdater: InstructionVisitor {
        override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {
            TODO("implement ${instruction.opCode.mnemonic}")
        }

        override fun visitStackInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: StackInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                DUP  -> frameAfter.push(frameAfter.peek())

                DUP2 -> {
                    val v1 = frameAfter.pop()
                    if (v1.isCategory2) {
                        frameAfter.push(v1)
                        frameAfter.push(v1)
                    } else {
                        val v2 = frameAfter.pop()

                        frameAfter.push(v2)
                        frameAfter.push(v1)
                        frameAfter.push(v2)
                        frameAfter.push(v1)
                    }
                }

                DUP_X1 -> {
                    val v1 = frameAfter.pop()
                    val v2 = frameAfter.pop()

                    frameAfter.push(v1)
                    frameAfter.push(v2)
                    frameAfter.push(v1)
                }

                DUP_X2 -> {
                    val v1 = frameAfter.pop()
                    if (v1.isCategory2) {
                        val v2 = frameAfter.pop()

                        frameAfter.push(v1)
                        frameAfter.push(v2)
                        frameAfter.push(v1)
                    } else {
                        val v2 = frameAfter.pop()
                        val v3 = frameAfter.pop()

                        frameAfter.push(v1)
                        frameAfter.push(v3)
                        frameAfter.push(v2)
                        frameAfter.push(v1)
                    }
                }

                DUP2_X1 -> {
                    val v1 = frameAfter.pop()
                    if (v1.isCategory2) {
                        val v2 = frameAfter.pop()

                        frameAfter.push(v1)
                        frameAfter.push(v2)
                        frameAfter.push(v1)
                    } else {
                        val v2 = frameAfter.pop()
                        val v3 = frameAfter.pop()

                        frameAfter.push(v2)
                        frameAfter.push(v1)
                        frameAfter.push(v3)
                        frameAfter.push(v2)
                        frameAfter.push(v1)
                    }
                }

                SWAP -> {
                    val v1 = frameAfter.pop()
                    val v2 = frameAfter.pop()

                    frameAfter.push(v1)
                    frameAfter.push(v2)
                }

                POP  -> frameAfter.pop()
                POP2 -> {
                    val topValue = frameAfter.pop()
                    if (!topValue.isCategory2) {
                        frameAfter.pop()
                    }
                }

                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitLiteralVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralVariableInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                IINC -> {}
                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: VariableInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val mnemonic = instruction.mnemonic
            if (mnemonic.contains("load")) {
                frameAfter.push(frameAfter.load(instruction.variable))
            } else if (mnemonic.contains("store")) {
                frameAfter.store(instruction.variable, frameAfter.pop())
            } else {
                TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitLiteralConstantInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralConstantInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.getConstant(classFile)) {
                is DoubleConstant -> frameAfter.push(DoubleType)
                is LongConstant   -> frameAfter.push(LongType)
                is FloatConstant  -> frameAfter.push(FloatType)
                is Utf8Constant   -> frameAfter.push(JavaReferenceType.of(JAVA_LANG_STRING_TYPE))
                else              -> frameAfter.push(IntegerType)
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitLiteralInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                DCONST_0,
                DCONST_1 -> frameAfter.push(DoubleType)

                FCONST_0,
                FCONST_1,
                FCONST_2 -> frameAfter.push(FloatType)

                LCONST_0,
                LCONST_1 -> frameAfter.push(LongType)

                else     -> frameAfter.push(IntegerType)
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitArrayInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                ARRAYLENGTH -> {
                    frameAfter.pop()
                    frameAfter.push(IntegerType)
                }

                AASTORE,
                BASTORE,
                SASTORE,
                CASTORE,
                LASTORE,
                DASTORE,
                FASTORE,
                IASTORE -> frameAfter.pop(3)

                AALOAD -> {
                    frameAfter.pop()
                    val arrayType = frameAfter.pop()
                    check(arrayType is JavaReferenceType && arrayType.classType.isArrayType)
                    val componentType = arrayType.classType.componentType
                    frameAfter.push(JavaReferenceType.of(componentType))
                }

                BALOAD,
                SALOAD,
                CALOAD,
                IALOAD  -> {
                    frameAfter.pop(2)
                    frameAfter.push(IntegerType)
                }

                FALOAD -> {
                    frameAfter.pop(2)
                    frameAfter.push(FloatType)
                }

                DALOAD -> {
                    frameAfter.pop(2)
                    frameAfter.push(DoubleType)
                }

                LALOAD -> {
                    frameAfter.pop(2)
                    frameAfter.push(LongType)
                }

                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitArrayPrimitiveTypeInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayPrimitiveTypeInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                NEWARRAY -> {
                    frameAfter.pop()
                    frameAfter.push(JavaReferenceType.of(instruction.arrayType))
                }

                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitArrayClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayClassInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val classType = instruction.getClassName(classFile).toJvmType()
            val arrayType = "[${classType.type}".asJvmType()

            when (instruction.opCode) {
                ANEWARRAY -> {
                    frameAfter.pop()
                    frameAfter.push(JavaReferenceType.of(arrayType))
                }

                MULTIANEWARRAY -> {
                    val dimensions = instruction.dimension
                    frameAfter.pop(dimensions)
                    frameAfter.push(JavaReferenceType.of(instruction.getClassName(classFile).toJvmType()))
                }

                else -> error("unexpected opcode '${instruction.opCode}'")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ClassInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val classType = instruction.getClassName(classFile).toJvmType()

            when (instruction.opCode) {
                NEW        -> frameAfter.push(UninitializedType.of(classType, offset))
                CHECKCAST  -> {
                    frameAfter.pop()
                    frameAfter.push(JavaReferenceType.of(classType))
                }
                INSTANCEOF -> {
                    frameAfter.pop()
                    frameAfter.push(IntegerType)
                }
                else       -> error("unexpected opcode '${instruction.opCode}'")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitFieldInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: FieldInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val fieldType = instruction.getDescriptor(classFile).asJvmType()

            when (instruction.opCode) {
                GETFIELD -> {
                    frameAfter.pop()
                    frameAfter.push(VerificationType.of(fieldType))
                }

                GETSTATIC -> {
                    frameAfter.push(VerificationType.of(fieldType))
                }

                PUTFIELD  -> frameAfter.pop(2)
                PUTSTATIC -> frameAfter.pop()

                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: MethodInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val (parameterTypes, returnType) = parseDescriptorToJvmTypes(instruction.getDescriptor(classFile))
            frameAfter.pop(parameterTypes.size)

            when (instruction.opCode) {
                INVOKESTATIC  -> {}
                INVOKESPECIAL -> {
                    val isInitializer = instruction.getMethodName(classFile) == "<init>"
                    if (isInitializer) {
                        val objectReference = frameAfter.pop()
                        frameAfter.referenceInitialized(objectReference)
                    } else {
                        frameAfter.pop()
                    }
                }
                else -> frameAfter.pop()
            }


            if (!returnType.isVoidType) {
                frameAfter.push(VerificationType.of(returnType))
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitInterfaceMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InterfaceMethodInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val (parameterTypes, returnType) = parseDescriptorToJvmTypes(instruction.getDescriptor(classFile))
            frameAfter.pop(parameterTypes.size)
            frameAfter.pop()

            if (!returnType.isVoidType) {
                frameAfter.push(VerificationType.of(returnType))
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitInvokeDynamicInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InvokeDynamicInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val (parameterTypes, returnType) = parseDescriptorToJvmTypes(instruction.getDescriptor(classFile))
            frameAfter.pop(parameterTypes.size)

            if (!returnType.isVoidType) {
                frameAfter.push(VerificationType.of(returnType))
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                IF_ACMPEQ,
                IF_ACMPNE -> frameAfter.pop(2)

                IF_ICMPEQ,
                IF_ICMPGE,
                IF_ICMPNE,
                IF_ICMPLE,
                IF_ICMPGT,
                IF_ICMPLT -> frameAfter.pop(2)

                IFEQ,
                IFLT,
                IFGE,
                IFLE,
                IFNE,
                IFGT,
                IFNULL,
                IFNONNULL -> frameAfter.pop()

                GOTO,
                GOTO_W -> {}

                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitConversionInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConversionInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                F2I,
                L2I,
                D2I,
                I2B,
                I2S,
                I2C -> {
                    frameAfter.pop()
                    frameAfter.push(IntegerType)
                }

                D2L,
                I2L -> {
                    frameAfter.pop()
                    frameAfter.push(LongType)
                }

                L2D,
                I2D -> {
                    frameAfter.pop()
                    frameAfter.push(DoubleType)
                }

                I2F,
                D2F,
                L2F -> {
                    frameAfter.pop()
                    frameAfter.push(FloatType)
                }

                else -> error("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitCompareInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: CompareInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                FCMPG,
                FCMPL,
                DCMPG,
                DCMPL,
                LCMP -> {
                    frameAfter.pop(2)
                    frameAfter.push(IntegerType)
                }

                else -> error("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitArithmeticInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArithmeticInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                IAND,
                IOR,
                IXOR,
                IADD,
                ISUB,
                ISHL,
                ISHR,
                IUSHR,
                IREM,
                IMUL,
                IDIV, -> {
                    frameAfter.pop(2)
                    frameAfter.push(IntegerType)
                }

                INEG -> {
                    frameAfter.pop()
                    frameAfter.push(IntegerType)
                }

                LNEG -> {
                    frameAfter.pop()
                    frameAfter.push(LongType)
                }

                DNEG -> {
                    frameAfter.pop()
                    frameAfter.push(DoubleType)
                }

                LOR,
                LXOR,
                LAND,
                LADD,
                LSUB,
                LDIV,
                LMUL,
                LSHL,
                LUSHR,
                LREM,
                LSHR -> {
                    frameAfter.pop(2)
                    frameAfter.push(LongType)
                }

                FADD,
                FSUB,
                FMUL,
                FDIV -> {
                    frameAfter.pop(2)
                    frameAfter.push(FloatType)
                }

                DSUB,
                DADD,
                DMUL,
                DDIV -> {
                    frameAfter.pop(2)
                    frameAfter.push(DoubleType)
                }

                else -> error("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitAnySwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: SwitchInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                LOOKUPSWITCH,
                TABLESWITCH -> frameAfter.pop()
                else        -> error("unexpected opcode ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitReturnInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ReturnInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                RETURN -> {}
                else   -> frameAfter.pop()
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitMonitorInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: MonitorInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                MONITORENTER,
                MONITOREXIT -> frameAfter.pop()

                else -> error("unexpected opcode ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitNullReferenceInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: NullReferenceInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()
            frameAfter.push(NullReference)
            framesAfter[offset] = frameAfter
        }

        override fun visitExceptionInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ExceptionInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            frameAfter.clearStack()
            frameAfter.push(frameBefore.peek())

            framesAfter[offset] = frameAfter
        }
    }

    inner class BlockAnalyser: InstructionVisitor {
        override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {}

        override fun visitAnySwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: SwitchInstruction) {
            setStatusFlag(offset, BLOCK_EXIT)

            val frameAfter = framesAfter[offset]!!

            val defaultOffset = offset + instruction.defaultOffset
            enqueueBasicBlock(defaultOffset, frameAfter)

            for (matchOffsetPair in instruction) {
                val targetOffset = offset + matchOffsetPair.offset
                enqueueBasicBlock(targetOffset, frameAfter)
            }
        }

        override fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
            setStatusFlag(offset, BLOCK_EXIT)

            val targetOffset = offset + instruction.branchOffset
            val nextOffset   = offset + instruction.getLength(offset)

            val frameAfter = framesAfter[offset]!!

            when (instruction.opCode) {
                GOTO,
                GOTO_W -> {
                    enqueueBasicBlock(targetOffset, frameAfter)
                }

                else -> {
                    enqueueBasicBlock(targetOffset, frameAfter)
                    enqueueBasicBlock(nextOffset, frameAfter)
                }
            }
        }

        override fun visitExceptionInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ExceptionInstruction) {
            setStatusFlag(offset, BLOCK_EXIT)
        }

        override fun visitReturnInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ReturnInstruction) {
            setStatusFlag(offset, BLOCK_EXIT)
        }
    }

    private fun setStatusFlag(offset: Int, flag: Int) {
        status[offset] = status[offset] or flag
    }

    private fun isFlagSet(offset: Int, flag: Int): Boolean {
        return (status[offset] and flag) != 0
    }

    companion object {
        private const val BLOCK_ENTRY       = 1 shl 1
        private const val BLOCK_EXIT        = 1 shl 2
        private const val EXCEPTION_HANDLER = 1 shl 3

        fun of(vararg processors: FrameProcessor): CodeAnalyzer {
            return CodeAnalyzer(processors.toList())
        }
    }
}

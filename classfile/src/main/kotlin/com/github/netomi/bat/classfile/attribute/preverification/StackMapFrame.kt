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

package com.github.netomi.bat.classfile.attribute.preverification

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.preverification.visitor.StackMapFrameVisitor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.*
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.util.mutableListOfCapacity

abstract class StackMapFrame protected constructor(val frameType: Int): ClassFileContent() {
    internal abstract val type: StackMapFrameType

    abstract val offsetDelta: Int

    abstract val verificationTypes: List<VerificationType>

    internal open fun readData(input: ClassDataInput) {}
    internal open fun writeData(output: ClassDataOutput) {}

    override fun write(output: ClassDataOutput) {
        output.writeByte(frameType)
        writeData(output)
    }

    abstract fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor)

    open fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {}

    companion object {
        internal fun read(input: ClassDataInput): StackMapFrame {
            val frameType = input.readUnsignedByte()

            val stackMapFrame = StackMapFrameType.of(frameType)
            stackMapFrame.readData(input)
            return stackMapFrame
        }
    }
}

class SameFrame private constructor(frameType: Int): StackMapFrame(frameType) {
    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_FRAME

    override val contentSize: Int
        get() = 1

    override val offsetDelta: Int
        get() = frameType

    override val verificationTypes: List<VerificationType>
        get() = emptyList()

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameFrame(classFile, this)
    }

    companion object {
        internal fun of(frameType: Int): SameFrame {
            require(frameType in 0 .. 63)
            return SameFrame(frameType)
        }
    }
}

class ChopFrame private constructor(            frameType:    Int,
                                    private var _offsetDelta: Int = 0): StackMapFrame(frameType) {
    override val type: StackMapFrameType
        get() = StackMapFrameType.CHOP_FRAME

    override val contentSize: Int
        get() = 3

    override val offsetDelta: Int
        get() = _offsetDelta

    val choppedVariables: Int
        get() = 251 - frameType

    override val verificationTypes: List<VerificationType>
        get() = emptyList()

    override fun readData(input: ClassDataInput) {
        _offsetDelta = input.readUnsignedShort()
    }

    override fun writeData(output: ClassDataOutput) {
        output.writeShort(offsetDelta)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitChopFrame(classFile, this)
    }

    companion object {
        internal fun of(frameType: Int): ChopFrame {
            require(frameType in 248 .. 250)
            return ChopFrame(frameType)
        }
    }
}

class SameExtendedFrame private constructor(            frameType:    Int,
                                            private var _offsetDelta: Int = 0): StackMapFrame(frameType) {
    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_EXTENDED_FRAME

    override val contentSize: Int
        get() = 3

    override val offsetDelta: Int
        get() = _offsetDelta

    override val verificationTypes: List<VerificationType>
        get() = emptyList()

    override fun readData(input: ClassDataInput) {
        _offsetDelta = input.readUnsignedShort()
    }

    override fun writeData(output: ClassDataOutput) {
        output.writeShort(offsetDelta)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameExtendedFrame(classFile, this)
    }

    companion object {
        internal fun of(frameType: Int): SameExtendedFrame {
            require(frameType == 251)
            return SameExtendedFrame(frameType)
        }
    }
}

class AppendFrame private constructor(            frameType:    Int,
                                      private var _offsetDelta: Int = 0,
                                      private var locals:       MutableList<VerificationType> = mutableListOfCapacity(0))
    : StackMapFrame(frameType), Sequence<VerificationType> {

    override val type: StackMapFrameType
        get() = StackMapFrameType.APPEND_FRAME

    override val contentSize: Int
        get() = 3 + locals.fold(0) { acc, element -> acc + element.contentSize }

    override val offsetDelta: Int
        get() = _offsetDelta

    val appendedVariables: Int
        get() = frameType - 251

    val size: Int
        get() = locals.size

    operator fun get(index: Int): VerificationType {
        return locals[index]
    }

    override fun iterator(): Iterator<VerificationType> {
        return locals.iterator()
    }

    override val verificationTypes: List<VerificationType>
        get() = locals

    override fun readData(input: ClassDataInput) {
        _offsetDelta = input.readUnsignedShort()
        locals = mutableListOfCapacity(appendedVariables)
        for (i in 0 until appendedVariables) {
            locals.add(VerificationType.read(input))
        }
    }

    override fun writeData(output: ClassDataOutput) {
        output.writeShort(offsetDelta)
        for (element in locals) {
            element.write(output)
        }
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitAppendFrame(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        for (local in locals) {
            local.referencedConstantsAccept(classFile, visitor)
        }
    }

    companion object {
        internal fun of(frameType: Int): AppendFrame {
            require(frameType in 252 .. 254)
            return AppendFrame(frameType)
        }
    }
}

class SameLocalsOneStackItemFrame private constructor(            frameType: Int,
                                                      private var _stack:    VerificationType = TopVariable.empty())
    : StackMapFrame(frameType) {

    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_LOCALS_1_STACK_ITEM_FRAME

    override val contentSize: Int
        get() = 1 + _stack.contentSize

    override val offsetDelta: Int
        get() = frameType - 64

    val stackItem: VerificationType
        get() = _stack

    override val verificationTypes: List<VerificationType>
        get() = listOf(stackItem)

    override fun readData(input: ClassDataInput) {
        _stack = VerificationType.read(input)
    }

    override fun writeData(output: ClassDataOutput) {
        _stack.write(output)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameLocalsOneStackItemFrame(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        _stack.referencedConstantsAccept(classFile, visitor)
    }

    companion object {
        internal fun of(frameType: Int): SameLocalsOneStackItemFrame {
            require(frameType in 64 .. 127)
            return SameLocalsOneStackItemFrame(frameType)
        }
    }
}

class SameLocalsOneStackItemExtendedFrame private constructor(            frameType:    Int,
                                                              private var _offsetDelta: Int = 0,
                                                              private var _stack:       VerificationType = TopVariable.empty())
    : StackMapFrame(frameType) {

    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_LOCALS_1_STACK_ITEM_EXTENDED_FRAME

    override val contentSize: Int
        get() = 3 + _stack.contentSize

    override val offsetDelta: Int
        get() = _offsetDelta

    val stackItem: VerificationType
        get() = _stack

    override val verificationTypes: List<VerificationType>
        get() = listOf(stackItem)

    override fun readData(input: ClassDataInput) {
        _offsetDelta = input.readUnsignedShort()
        _stack = VerificationType.read(input)
    }

    override fun writeData(output: ClassDataOutput) {
        output.writeShort(_offsetDelta)
        _stack.write(output)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameLocalsOneStackItemExtendedFrame(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        _stack.referencedConstantsAccept(classFile, visitor)
    }

    companion object {
        internal fun of(frameType: Int): SameLocalsOneStackItemExtendedFrame {
            require(frameType == 247)
            return SameLocalsOneStackItemExtendedFrame(frameType)
        }
    }
}

class FullFrame private constructor(            frameType:    Int,
                                    private var _offsetDelta: Int = 0,
                                    private var _locals:      MutableList<VerificationType> = mutableListOfCapacity(0),
                                    private var _stack:       MutableList<VerificationType> = mutableListOfCapacity(0))
    : StackMapFrame(frameType) {

    override val type: StackMapFrameType
        get() = StackMapFrameType.FULL_FRAME

    override val contentSize: Int
        get() = 3 + locals.contentSize() + stack.contentSize()

    override val offsetDelta: Int
        get() = _offsetDelta

    val locals: List<VerificationType>
        get() = _locals

    val stack: List<VerificationType>
        get() = _stack

    override val verificationTypes: List<VerificationType>
        get() = locals + stack

    override fun readData(input: ClassDataInput) {
        _offsetDelta = input.readUnsignedShort()
        _locals = input.readContentList(VerificationType.Companion::read)
        _stack  = input.readContentList(VerificationType.Companion::read)
    }

    override fun writeData(output: ClassDataOutput) {
        output.writeShort(_offsetDelta)
        output.writeContentList(_locals)
        output.writeContentList(_stack)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitFullFrame(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        for (local in _locals) {
            local.referencedConstantsAccept(classFile, visitor)
        }

        for (stackEntry in _stack) {
            stackEntry.referencedConstantsAccept(classFile, visitor)
        }
    }

    companion object {
        internal fun of(frameType: Int): FullFrame {
            require(frameType == 255)
            return FullFrame(frameType)
        }
    }
}

internal enum class StackMapFrameType constructor(private val supplier: (Int) -> StackMapFrame) {
    SAME_FRAME                             ({ SameFrame.of(it) }),
    SAME_LOCALS_1_STACK_ITEM_FRAME         ({ SameLocalsOneStackItemFrame.of(it) }),
    SAME_LOCALS_1_STACK_ITEM_EXTENDED_FRAME({ SameLocalsOneStackItemExtendedFrame.of(it) }),
    CHOP_FRAME                             ({ ChopFrame.of(it) }),
    SAME_EXTENDED_FRAME                    ({ SameExtendedFrame.of(it) }),
    APPEND_FRAME                           ({ AppendFrame.of(it) }),
    FULL_FRAME                             ({ FullFrame.of(it) });

    companion object {
        fun of(frameType: Int) : StackMapFrame {
            val type = when (frameType) {
                in 0 .. 63    -> SAME_FRAME
                in 64 .. 127  -> SAME_LOCALS_1_STACK_ITEM_FRAME
                247           -> SAME_LOCALS_1_STACK_ITEM_EXTENDED_FRAME
                in 248 .. 250 -> CHOP_FRAME
                251           -> SAME_EXTENDED_FRAME
                in 252 .. 254 -> APPEND_FRAME
                255           -> FULL_FRAME
                else -> error("unexpected frameType '$frameType'")
            }

            return type.supplier(frameType)
        }
    }
}

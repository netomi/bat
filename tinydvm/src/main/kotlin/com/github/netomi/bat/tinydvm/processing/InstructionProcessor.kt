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

package com.github.netomi.bat.tinydvm.processing

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.instruction.DexOpCode.*
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.util.*
import com.github.netomi.bat.tinydvm.Dvm
import com.github.netomi.bat.tinydvm.data.*
import com.github.netomi.bat.tinydvm.data.jvm.DvmNativeObject

class InstructionProcessor constructor(private val dvm:   Dvm,
                                       private val state: InterpreterState) : InstructionVisitor {

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {}

    override fun visitLiteralInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: LiteralInstruction) {
        val rA = instruction.registers[0]
        state.registers[rA] = DvmPrimitiveValue.ofUnknownType(instruction.value)
        if (instruction.opCode.targetsWideRegister) {
            state.registers[rA + 1] = state.registers[rA]
        }
    }

    override fun visitTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: TypeInstruction) {
        when (instruction.opCode) {
            NEW_INSTANCE -> {
                val dvmClazz = dvm.getClass(instruction.getType(dexFile))
                val r = instruction.registers[0]
                state.registers[r] = DvmReferenceValue.of(DvmObject.newInstanceOf(dvmClazz))
            }

            else -> {}
        }
    }

    override fun visitFieldInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: FieldInstruction) {

        val fieldID = instruction.getField(dexFile)
        val field   = dvm.getField(dexFile, fieldID) ?:
            throw VerifyException("[0x%x] field insn references non-existing field '%s.%s:%s'".format(offset,
                                                                                                      fieldID.getClassType(dexFile),
                                                                                                      fieldID.getName(dexFile),
                                                                                                      fieldID.getType(dexFile)))

        val javaFieldType = field.type

        val getStaticField = { supportedTypes: Array<String> ->
            val supportedJvmTypes = supportedTypes.map { it.asJvmType() }

            if (!supportedJvmTypes.contains(field.type)) {
                throw VerifyException("[0x%x] get insn has type '%s' but expected type '%d'"
                        .format(offset, supportedTypes.joinToString(separator = "|"), field.type))
            }

            val r = instruction.registers[0]
            val result = field.get(dvm, null)

            state.registers[r] = result
            if (instruction.opCode.targetsWideRegister) {
                state.registers[r + 1] = result
            }
        }

        val setPrimitiveStaticField = { supportedTypes: Array<String> ->
            val supportedJvmTypes = supportedTypes.map { it.asJvmType() }

            if (!supportedJvmTypes.contains(field.type)) {
                throw VerifyException("[0x%x] get insn has type '%s' but expected type '%d'"
                        .format(offset, supportedTypes.joinToString(separator = "|"), field.type))
            }

            val r = instruction.registers[0]
            var dvmValue = state.registers[r] ?:
                    throw VerifyException("[0x%x] unexpected value in v%d of type Undefined but expected '%s' for put"
                            .format(offset, r, field.type))

            if (dvmValue !is DvmPrimitiveValue) {
                throw VerifyException("[0x%x] unexpected value in v%d of type '%s' but expected '%s' for put"
                        .format(offset, r, dvmValue.type, field.type))
            }

            if (instruction.opCode.targetsWideRegister) {
                val dvmValue2 = state.registers[r + 1]
                if (dvmValue != dvmValue2) {
                    throw VerifyException("[0x%x] unexpected value in register pair v%d/v%d of type %s / %s but expected '%s' for put"
                            .format(offset, r, r + 1, dvmValue.type, dvmValue2?.type ?: "Undefined", field.type))
                }
            }

            dvmValue = dvmValue.withType(field.type)
            if (!supportedJvmTypes.contains(dvmValue.type)) {
                throw VerifyException("[0x%x] unexpected value in v%d of type '%s' but expected '%s' for put"
                        .format(offset, r, dvmValue.type, supportedTypes.joinToString(separator = "|")))
            }

            field.set(dvm, null, dvmValue)
        }

        when (instruction.opCode) {
            SGET         -> getStaticField(arrayOf(INT_TYPE, FLOAT_TYPE))
            SGET_WIDE    -> getStaticField(arrayOf(LONG_TYPE, DOUBLE_TYPE))
            SGET_BOOLEAN -> getStaticField(arrayOf(BOOLEAN_TYPE))
            SGET_BYTE    -> getStaticField(arrayOf(BYTE_TYPE))
            SGET_CHAR    -> getStaticField(arrayOf(CHAR_TYPE))
            SGET_SHORT   -> getStaticField(arrayOf(SHORT_TYPE))

            SGET_OBJECT -> {
                if (!javaFieldType.isReferenceType) {
                    throw VerifyException("[0x%x] get insn has reference type but expected type '%d'".format(offset, field.type))
                }
                val r = instruction.registers[0]
                state.registers[r] = field.get(dvm, null)
            }

            SPUT         -> setPrimitiveStaticField(arrayOf(INT_TYPE, FLOAT_TYPE))
            SPUT_WIDE    -> setPrimitiveStaticField(arrayOf(LONG_TYPE, DOUBLE_TYPE))
            SPUT_BOOLEAN -> setPrimitiveStaticField(arrayOf(BOOLEAN_TYPE))
            SPUT_BYTE    -> setPrimitiveStaticField(arrayOf(BYTE_TYPE))
            SPUT_CHAR    -> setPrimitiveStaticField(arrayOf(CHAR_TYPE))
            SPUT_SHORT   -> setPrimitiveStaticField(arrayOf(SHORT_TYPE))

            SPUT_OBJECT -> {
                if (!javaFieldType.isReferenceType) {
                    throw VerifyException("[0x%x] put insn has reference type but expected type '%d'".format(offset, field.type))
                }

                val r = instruction.registers[0]
                val dvmValue = state.registers[r] ?:
                        throw VerifyException("[0x%x] unexpected value in v%d of type Undefined but expected '%s' for put"
                                .format(offset, r, field.type))

                if (dvmValue.type != field.type) {
                    throw VerifyException("[0x%x] unexpected value in v%d of type '%s' but expected '%s' for put"
                            .format(offset, r, dvmValue.type, field.type))
                }

                field.set(dvm, null, dvmValue)
            }

            else -> {
                throw IllegalStateException("unexpected opCode ${instruction.opCode}")
            }
        }
    }

    override fun visitStringInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: StringInstruction) {
        when (instruction.opCode) {
            CONST_STRING,
            CONST_STRING_JUMBO -> {
                val rA = instruction.registers[0]

                state.registers[rA] = DvmReferenceValue.of(DvmNativeObject.of(instruction.getString(dexFile), dvm.getClass(JAVA_LANG_STRING_TYPE)))
            }

            else -> {
                throw IllegalStateException("unexpected opCode ${instruction.opCode}")
            }
        }
    }

    override fun visitMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        val methodID   = instruction.getMethodID(dexFile)
        val classType  = methodID.getClassType(dexFile)
        val methodName = methodID.getName(dexFile)
        val proto      = methodID.getProtoID(dexFile)
        val parameterTypes = proto.getParameterTypes(dexFile)

        when (instruction.opCode) {
            INVOKE_DIRECT  -> {
                val dvmClazz  = dvm.getClass(classType)
                val dvmMethod = dvmClazz.getDirectMethod(dexFile, methodName, proto)

                val parameters = Array(instruction.registers.size) { index -> state.registers[instruction.registers[index]]!! }
                dvmMethod?.invoke(dvm, *parameters)
            }

            INVOKE_VIRTUAL -> {
                val parameterTypeClasses = parameterTypes.map { it.toJvmClass() }
                val externalClassName = classType.toExternalClassName()
                val clazz = Class.forName(externalClassName)

                val m = clazz.getMethod(methodName, *parameterTypeClasses.toTypedArray())

                val r1 = instruction.registers[0]

                val paramList = Array(instruction.registers.size - 1) { index ->
                    val r = instruction.registers[index + 1]
                    val dvmValue = state.registers[r]

                    if (dvmValue == null) {
                        null
                    } else {
                        when (dvmValue) {
                            is DvmPrimitiveValue -> dvmValue.valueOfType(parameterTypes[index])
                            is DvmReferenceValue -> dvmValue.value
                            else -> {}
                        }
                    }
                }

                m.invoke(state.registers[r1]?.value, *paramList)
            }

            else -> {}
        }
    }
}
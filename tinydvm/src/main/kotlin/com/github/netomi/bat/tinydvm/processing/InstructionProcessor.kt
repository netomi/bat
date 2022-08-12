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
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.dexfile.util.DexClasses.isReferenceType
import com.github.netomi.bat.util.*
import com.github.netomi.bat.tinydvm.Dvm
import com.github.netomi.bat.tinydvm.data.*

class InstructionProcessor constructor(private val dvm: Dvm,
                                       private val registers: Array<DvmValue?>) : InstructionVisitor {

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {}

    override fun visitLiteralInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: LiteralInstruction) {
        val rA = instruction.registers[0]
        registers[rA] = DvmPrimitiveValue.ofUnknownType(instruction.value)
        if (instruction.opCode.targetsWideRegister) {
            registers[rA + 1] = registers[rA]
        }
    }

    override fun visitTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: TypeInstruction) {
        when (instruction.opCode) {
            NEW_INSTANCE -> {
                val dvmClazz = dvm.getClass(instruction.getType(dexFile))
                val r = instruction.registers[0]
                registers[r] = DvmReferenceValue.of(DvmObject.newInstanceOf(dvmClazz))
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

        val getStaticField = { supportedTypes: Array<String> ->
            if (!supportedTypes.contains(field.type)) {
                throw VerifyException("[0x%x] get insn has type '%s' but expected type '%d'"
                        .format(offset, supportedTypes.joinToString(separator = "|"), field.type))
            }

            val r = instruction.registers[0]
            val result = field.get(null)

            registers[r] = result
            if (instruction.opCode.targetsWideRegister) {
                registers[r + 1] = result
            }
        }

        val setPrimitiveStaticField = { supportedTypes: Array<String> ->
            if (!supportedTypes.contains(field.type)) {
                throw VerifyException("[0x%x] get insn has type '%s' but expected type '%d'"
                        .format(offset, supportedTypes.joinToString(separator = "|"), field.type))
            }

            val r = instruction.registers[0]
            var dvmValue = registers[r] ?:
                    throw VerifyException("[0x%x] unexpected value in v%d of type Undefined but expected '%s' for put"
                            .format(offset, r, field.type))

            if (dvmValue !is DvmPrimitiveValue) {
                throw VerifyException("[0x%x] unexpected value in v%d of type '%s' but expected '%s' for put"
                        .format(offset, r, dvmValue.type, field.type))
            }

            if (instruction.opCode.targetsWideRegister) {
                val dvmValue2 = registers[r + 1]
                if (dvmValue != dvmValue2) {
                    throw VerifyException("[0x%x] unexpected value in register pair v%d/v%d of type %s / %s but expected '%s' for put"
                            .format(offset, r, r + 1, dvmValue.type, dvmValue2?.type ?: "Undefined", field.type))
                }
            }

            dvmValue = dvmValue.withType(field.type)
            if (!supportedTypes.contains(dvmValue.type)) {
                throw VerifyException("[0x%x] unexpected value in v%d of type '%s' but expected '%s' for put"
                        .format(offset, r, dvmValue.type, supportedTypes.joinToString(separator = "|")))
            }

            field.set(null, dvmValue)
        }

        when (instruction.opCode) {
            SGET         -> getStaticField(arrayOf(INT_TYPE, FLOAT_TYPE))
            SGET_WIDE    -> getStaticField(arrayOf(LONG_TYPE, DOUBLE_TYPE))
            SGET_BOOLEAN -> getStaticField(arrayOf(BOOLEAN_TYPE))
            SGET_BYTE    -> getStaticField(arrayOf(BYTE_TYPE))
            SGET_CHAR    -> getStaticField(arrayOf(CHAR_TYPE))
            SGET_SHORT   -> getStaticField(arrayOf(SHORT_TYPE))

            SGET_OBJECT -> {
                if (!isReferenceType(field.type)) {
                    throw VerifyException("[0x%x] get insn has reference type but expected type '%d'".format(offset, field.type))
                }
                val r = instruction.registers[0]
                registers[r] = field.get(null)
            }

            SPUT         -> setPrimitiveStaticField(arrayOf(INT_TYPE, FLOAT_TYPE))
            SPUT_WIDE    -> setPrimitiveStaticField(arrayOf(LONG_TYPE, DOUBLE_TYPE))
            SPUT_BOOLEAN -> setPrimitiveStaticField(arrayOf(BOOLEAN_TYPE))
            SPUT_BYTE    -> setPrimitiveStaticField(arrayOf(BYTE_TYPE))
            SPUT_CHAR    -> setPrimitiveStaticField(arrayOf(CHAR_TYPE))
            SPUT_SHORT   -> setPrimitiveStaticField(arrayOf(SHORT_TYPE))

            SPUT_OBJECT -> {
                if (!isReferenceType(field.type)) {
                    throw VerifyException("[0x%x] put insn has reference type but expected type '%d'".format(offset, field.type))
                }

                val r = instruction.registers[0]
                val dvmValue = registers[r] ?:
                        throw VerifyException("[0x%x] unexpected value in v%d of type Undefined but expected '%s' for put"
                                .format(offset, r, field.type))

                if (dvmValue.type != field.type) {
                    throw VerifyException("[0x%x] unexpected value in v%d of type '%s' but expected '%s' for put"
                            .format(offset, r, dvmValue.type, field.type))
                }

                field.set(null, dvmValue)
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
                registers[rA] = DvmReferenceValue.of(DvmNativeObject.of(instruction.getString(dexFile), JAVA_LANG_STRING_TYPE))
            }

            else -> {
                throw IllegalStateException("unexpected opCode ${instruction.opCode}")
            }
        }
    }

    override fun visitMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        when (instruction.opCode) {
            INVOKE_VIRTUAL -> {
                val methodID = instruction.getMethodID(dexFile)
                val classType = methodID.getClassType(dexFile)
                val methodName = methodID.getName(dexFile)
                val proto = methodID.getProtoID(dexFile)
                val parameterTypes = proto.getParameterTypes(dexFile)

                val parameterTypeClasses = parameterTypes.map { DexClasses.externalClassNameFromInternalType(it) }
                    .map {
                        when (it) {
                            "I" -> Integer.TYPE
                            "J" -> java.lang.Long.TYPE
                            "B" -> java.lang.Byte.TYPE
                            "S" -> java.lang.Short.TYPE
                            "F" -> java.lang.Float.TYPE
                            else -> Class.forName(it)
                        }
                    }.toList()
                val className = DexClasses.externalClassNameFromInternalType(classType)
                val clazz = Class.forName(className)

                val m = clazz.getMethod(methodName, *parameterTypeClasses.toTypedArray())

                val r1 = instruction.registers[0]

                val paramList = Array(instruction.registers.size - 1) { index ->
                    val r = instruction.registers[index + 1]
                    val dvmValue = registers[r]

                    if (dvmValue == null) {
                        null
                    } else {
                        when (dvmValue) {
                            is DvmPrimitiveValue -> dvmValue.valueOfType(parameterTypes[index])
                            is DvmReferenceValue -> dvmValue.value.obj
                            else -> {}
                        }
                    }
                }

                m.invoke((registers[r1]?.value as DvmNativeObject).obj, *paramList)
            }

            else -> {}
        }
    }
}
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
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.dexfile.util.DexClasses
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

    override fun visitFieldInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: FieldInstruction) {
        when (instruction.opCode) {
            DexOpCode.SGET,
            DexOpCode.SGET_OBJECT -> {
                val fieldID = instruction.getField(dexFile)
                val field   = dvm.getField(dexFile, fieldID)
                val r       = instruction.registers[0]
                registers[r] = field.get(null)
            }

            DexOpCode.SPUT,
            DexOpCode.SPUT_OBJECT -> {
                val fieldID = instruction.getField(dexFile)
                val field   = dvm.getField(dexFile, fieldID)
                val r       = instruction.registers[0]
                field.set(null, registers[r]!!)
            }

            else -> {}
        }
    }

    override fun visitStringInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: StringInstruction) {
        when (instruction.opCode) {
            DexOpCode.CONST_STRING,
            DexOpCode.CONST_STRING_JUMBO -> {
                val rA = instruction.registers[0]
                registers[rA] = DvmReferenceValue.of(instruction.getString(dexFile), JAVA_LANG_STRING_TYPE)
            }

            else -> {
                throw IllegalStateException("unexpected opCode ${instruction.opCode}")
            }
        }
    }

    override fun visitMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        when (instruction.opCode) {
            DexOpCode.INVOKE_VIRTUAL -> {
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
                            else -> Class.forName(it)
                        }
                    }.toList()
                val className = DexClasses.externalClassNameFromInternalType(classType)
                val clazz = Class.forName(className)

                val m = clazz.getMethod(methodName, *parameterTypeClasses.toTypedArray())

                val r1 = instruction.registers[0]

                val paramList = Array(instruction.registers.size - 1) { index ->
                    val r = instruction.registers[index + 1]
                    var dvmValue = registers[r]
                    dvmValue?.valueOfType(parameterTypes[index])
                }

                m.invoke(registers[r1]?.value, *paramList)
            }

            else -> {}
        }
    }
}
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

package com.github.netomi.bat.tinydvm

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.instruction.DexInstruction
import com.github.netomi.bat.dexfile.util.asDexType
import com.github.netomi.bat.tinydvm.data.DvmValue
import com.github.netomi.bat.tinydvm.processing.InstructionProcessor
import com.github.netomi.bat.tinydvm.processing.InterpreterState

class Interpreter private constructor(private val dvm:      Dvm,
                                      private val dexFile:  DexFile,
                                      private val classDef: ClassDef,
                                      private val method:   EncodedMethod) {

    private val code = method.code

    private val state: InterpreterState = InterpreterState.of(code.registersSize)
    private val processor               = InstructionProcessor(dvm, state)

    fun invoke(vararg parameters: DvmValue): DvmValue {
        setParameterRegisters(*parameters)

        var offset = 0
        while (offset < code.insnsSize) {
            val instruction = DexInstruction.create(code.insns, offset)
            instruction.accept(dexFile, classDef, method, code, offset, processor)
            offset += instruction.length
        }

        return DvmValue.ofUnitValue()
    }

    private fun setParameterRegisters(vararg parameters: DvmValue) {
        val localRegisters = code.registersSize - code.insSize

        var paramRegister = localRegisters
        for (parameter in parameters) {
            state.registers[paramRegister] = parameter
            paramRegister += parameter.type.getArgumentSize()
        }
    }

    companion object {
        fun of(dvm: Dvm, dexFile: DexFile, classDef: ClassDef, method: EncodedMethod): Interpreter {
            return Interpreter(dvm, dexFile, classDef, method)
        }
    }
}

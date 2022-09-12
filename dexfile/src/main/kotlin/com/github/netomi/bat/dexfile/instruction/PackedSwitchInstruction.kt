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

package com.github.netomi.bat.dexfile.instruction

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor

class PackedSwitchInstruction: SwitchInstruction<PackedSwitchPayload> {

    private var _payload: PackedSwitchPayload = PackedSwitchPayload.empty()

    override val payload: PackedSwitchPayload
        get() = _payload

    private constructor(): super(DexOpCode.PACKED_SWITCH)

    private constructor(payload: PackedSwitchPayload, register: Int): super(DexOpCode.PACKED_SWITCH, register) {
        this._payload = payload
    }

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)

        _payload = PackedSwitchPayload.create(instructions, offset + payloadOffset)
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitPackedSwitchInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        fun of(payload: PackedSwitchPayload, register: Int): PackedSwitchInstruction {
            return PackedSwitchInstruction(payload, register)
        }

        internal fun create(opCode: DexOpCode): PackedSwitchInstruction {
            return PackedSwitchInstruction()
        }
    }
}
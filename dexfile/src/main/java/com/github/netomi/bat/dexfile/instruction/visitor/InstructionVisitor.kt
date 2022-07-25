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
package com.github.netomi.bat.dexfile.instruction.visitor

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.instruction.*

fun interface InstructionVisitor {
    fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction)
    
    fun visitAnyArithmeticInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArithmeticInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitArithmeticInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArithmeticInstruction) {
        visitAnyArithmeticInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitArithmeticLiteralInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArithmeticLiteralInstruction) {
        visitAnyArithmeticInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitAnyArrayInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArrayInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitArrayInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArrayInstruction) {
        visitAnyArrayInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitArrayTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArrayTypeInstruction) {
        visitAnyArrayInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitBasicInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: BasicInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitBranchInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: BranchInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitCallSiteInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: CallSiteInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitConversionInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ConversionInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitFieldInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: FieldInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitLiteralInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: LiteralInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitAnyMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        visitAnyMethodInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitMethodProtoInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodProtoInstruction) {
        visitAnyMethodInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitMethodHandleRefInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodHandleRefInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitMethodTypeRefInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodTypeRefInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitAnyPayloadInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: PayloadInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitAnySwitchInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: SwitchInstruction) {
        visitAnyPayloadInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitPackedSwitchInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: PackedSwitchInstruction) {
        visitAnySwitchInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitSparseSwitchInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: SparseSwitchInstruction) {
        visitAnySwitchInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitFillArrayDataInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: FillArrayDataInstruction) {
        visitAnyPayloadInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitStringInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: StringInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: TypeInstruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    fun visitAnyPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: Payload) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, payload)
    }

    fun visitFillArrayPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: FillArrayPayload) {
        visitAnyPayload(dexFile, classDef, method, code, offset, payload)
    }

    fun visitPackedSwitchPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: PackedSwitchPayload) {
        visitAnyPayload(dexFile, classDef, method, code, offset, payload)
    }

    fun visitSparseSwitchPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: SparseSwitchPayload) {
        visitAnyPayload(dexFile, classDef, method, code, offset, payload)
    }
}
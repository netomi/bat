/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.dexfile.visitor;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.instruction.*;

/**
 * @author Thomas Neidhart
 */
public interface InstructionVisitor
{
    default void visitAnyInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction) {
        throw new RuntimeException("Need to implement in class '" + this.getClass().getName() + "'.");
    }

    default void visitArithmeticInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArithmeticInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitArrayInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArrayInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitBasicInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BasicInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitBranchInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BranchInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitConversionInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ConversionInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitFieldInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FieldInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitLiteralInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, LiteralInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitMethodInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitFillArrayPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FillArrayPayload payload) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, payload);
    }

    default void visitPackedSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PackedSwitchPayload payload) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, payload);
    }

    default void visitSparseSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, SparseSwitchPayload payload) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, payload);
    }
}

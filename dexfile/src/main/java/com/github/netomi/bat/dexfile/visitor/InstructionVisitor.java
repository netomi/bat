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
    void visitAnyInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction);

    default void visitAnyArithmeticInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArithmeticInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitArithmeticInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArithmeticInstruction instruction) {
        visitAnyArithmeticInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitArithmeticLiteralInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArithmeticLiteralInstruction instruction) {
        visitAnyArithmeticInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitAnyArrayInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArrayInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitArrayInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArrayInstruction instruction) {
        visitAnyArrayInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitArrayTypeInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArrayTypeInstruction instruction) {
        visitAnyArrayInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitBasicInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BasicInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitBranchInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BranchInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitCallSiteInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, CallSiteInstruction instruction) {
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

    default void visitAnyMethodInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitMethodInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodInstruction instruction) {
        visitAnyMethodInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitMethodProtoInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodProtoInstruction instruction) {
        visitAnyMethodInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitMethodHandleRefInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodHandleRefInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitMethodTypeRefInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodTypeRefInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitPayloadInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PayloadInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitStringInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, StringInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitTypeInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, TypeInstruction instruction) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction);
    }

    default void visitAnyPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, Payload payload) {
        visitAnyInstruction(dexFile, classDef, method, code, offset, payload);
    }

    default void visitFillArrayPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FillArrayPayload payload) {
        visitAnyPayload(dexFile, classDef, method, code, offset, payload);
    }

    default void visitPackedSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PackedSwitchPayload payload) {
        visitAnyPayload(dexFile, classDef, method, code, offset, payload);
    }

    default void visitSparseSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, SparseSwitchPayload payload) {
        visitAnyPayload(dexFile, classDef, method, code, offset, payload);
    }
}

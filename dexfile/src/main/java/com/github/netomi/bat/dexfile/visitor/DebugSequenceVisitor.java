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
import com.github.netomi.bat.dexfile.debug.*;

public interface DebugSequenceVisitor
{
    default void visitAnyDebugInstruction(DexFile dexFile, DebugInfo debugInfo, DebugInstruction instruction) {
        throw new RuntimeException("Need to implement in class '" + this.getClass().getName() + "'.");
    }

    default void visitAdvanceLine(DexFile dexFile, DebugInfo debugInfo, DebugAdvanceLine instruction) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction);
    }

    default void visitAdvanceLineAndPC(DexFile dexFile, DebugInfo debugInfo, DebugAdvanceLineAndPC instruction) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction);
    }

    default void visitAdvancePC(DexFile dexFile, DebugInfo debugInfo, DebugAdvancePC instruction) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction);
    }

    default void visitEndLocal(DexFile dexFile, DebugInfo debugInfo, DebugEndLocal instruction) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction);
    }

    default void visitEndSequence(DexFile dexFile, DebugInfo debugInfo, DebugEndSequence instruction) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction);
    }

    default void visitRestartLocal(DexFile dexFile, DebugInfo debugInfo, DebugRestartLocal instruction) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction);
    }

    default void visitSetEpilogueBegin(DexFile dexFile, DebugInfo debugInfo, DebugSetEpilogueBegin instruction) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction);
    }

    default void visitSetFile(DexFile dexFile, DebugInfo debugInfo, DebugSetFile instruction) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction);
    }

    default void visitSetPrologueEnd(DexFile dexFile, DebugInfo debugInfo, DebugSetPrologueEnd instruction) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction);
    }

    default void visitStartLocal(DexFile dexFile, DebugInfo debugInfo, DebugStartLocal instruction) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction);
    }

    default void visitStartLocalExtended(DexFile dexFile, DebugInfo debugInfo, DebugStartLocalExtended instruction) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction);
    }
}

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
import com.github.netomi.bat.dexfile.debug.DebugInfo;

public interface DebugSequenceVisitor
{
    void visitAdvanceLine(DexFile dexFile, DebugInfo debugInfo, int lineDiff);

    void visitAdvanceLineAndPC(DexFile dexFile, DebugInfo debugInfo, int lineDiff, int addrDiff);

    void visitAdvancePC(DexFile dexFile, DebugInfo debugInfo, int addrDiff);

    void visitEndLocal(DexFile dexFile, DebugInfo debugInfo, int registerNum);

    void visitEndSequence(DexFile dexFile, DebugInfo debugInfo);

    void visitRestartLocal(DexFile dexFile, DebugInfo debugInfo, int registerNum);

    void visitSetEpilogueBegin(DexFile dexFile, DebugInfo debugInfo);

    void visitSetFile(DexFile dexFile, DebugInfo debugInfo, int nameIndex);

    void visitSetPrologueEnd(DexFile dexFile, DebugInfo debugInfo);

    void visitStartLocal(DexFile dexFile, DebugInfo debugInfo, int registerNum, int nameIndex, int typeIndex);

    void visitStartLocalExtended(DexFile dexFile, DebugInfo debugInfo, int registerNum, int nameIndex, int typeIndex, int sigIndex);
}

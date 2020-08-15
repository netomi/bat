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
package com.github.netomi.bat.smali;

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.debug.*;
import com.github.netomi.bat.dexfile.visitor.DebugSequenceVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class      SourceLineCollector
implements DebugSequenceVisitor
{
    private final Map<Integer, List<String>> debugState;

    private int   lineNumber;
    private short codeOffset;

    SourceLineCollector(Map<Integer, List<String>> debugState, int lineStart) {
        this.debugState      = debugState;
        this.lineNumber      = lineStart;
    }

    @Override
    public void visitAnyDebugInstruction(DexFile dexFile, DebugInfo debugInfo, DebugInstruction instruction) {}

    @Override
    public void visitSetPrologueEnd(DexFile dexFile, DebugInfo debugInfo, DebugSetPrologueEnd instruction) {
        addDebugInfo(codeOffset, ".prologue");
    }

    @Override
    public void visitAdvanceLine(DexFile dexFile, DebugInfo debugInfo, DebugAdvanceLine instruction) {
        lineNumber += instruction.getLineDiff();
    }

    @Override
    public void visitAdvanceLineAndPC(DexFile dexFile, DebugInfo debugInfo, DebugAdvanceLineAndPC instruction) {
        lineNumber += instruction.getLineDiff();
        codeOffset += instruction.getAddrDiff();
        addDebugInfo(codeOffset, ".line " + lineNumber);
    }

    @Override
    public void visitAdvancePC(DexFile dexFile, DebugInfo debugInfo, DebugAdvancePC instruction) {
        codeOffset += instruction.getAddrDiff();
    }

    private void addDebugInfo(int offset, String info) {
        List<String> infos = debugState.computeIfAbsent(offset, (key) -> new ArrayList<>());
        infos.add(info);
    }
}

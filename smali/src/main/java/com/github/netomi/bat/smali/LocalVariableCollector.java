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

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

class      LocalVariableCollector
implements DebugSequenceVisitor
{
    private final Map<Integer, List<String>> debugState;
    private final RegisterPrinter            registerPrinter;
    private final LocalVariableInfo[]        localVariableInfos;

    private short codeOffset;

    LocalVariableCollector(Map<Integer, List<String>> debugState, LocalVariableInfo[] localVariableInfos, RegisterPrinter registerPrinter) {
        this.debugState         = debugState;
        this.registerPrinter    = registerPrinter;
        this.localVariableInfos = localVariableInfos;
    }

    @Override
    public void visitAnyDebugInstruction(DexFile dexFile, DebugInfo debugInfo, DebugInstruction instruction) {}

    @Override
    public void visitAdvanceLineAndPC(DexFile dexFile, DebugInfo debugInfo, DebugAdvanceLineAndPC instruction) {
        codeOffset += instruction.getAddrDiff();
    }

    @Override
    public void visitAdvancePC(DexFile dexFile, DebugInfo debugInfo, DebugAdvancePC instruction) {
        codeOffset += instruction.getAddrDiff();
    }

    @Override
    public void visitEndLocal(DexFile dexFile, DebugInfo debugInfo, DebugEndLocal instruction) {
        StringBuilder sb = new StringBuilder();

        int registerNum = instruction.getRegisterNum();
        sb.append(".end local ");
        sb.append(registerPrinter.formatRegister(registerNum));

        handleGenericLocal(registerNum, sb);

        addDebugInfo(codeOffset, sb.toString());
    }

    @Override
    public void visitRestartLocal(DexFile dexFile, DebugInfo debugInfo, DebugRestartLocal instruction) {
        StringBuilder sb = new StringBuilder();

        int registerNum = instruction.getRegisterNum();
        sb.append(".restart local ");
        sb.append(registerPrinter.formatRegister(registerNum));

        handleGenericLocal(registerNum, sb);

        addDebugInfo(codeOffset, sb.toString());
    }

    @Override
    public void visitStartLocal(DexFile dexFile, DebugInfo debugInfo, DebugStartLocal instruction) {
        handleStartLocal(dexFile,
                         instruction.getRegisterNum(),
                         instruction.getNameIndex(),
                         instruction.getTypeIndex(),
                         NO_INDEX);
    }

    @Override
    public void visitStartLocalExtended(DexFile dexFile, DebugInfo debugInfo, DebugStartLocalExtended instruction) {
        handleStartLocal(dexFile,
                         instruction.getRegisterNum(),
                         instruction.getNameIndex(),
                         instruction.getTypeIndex(),
                         instruction.getSigIndex());
    }

    private void handleStartLocal(DexFile dexFile, int registerNum, int nameIndex, int typeIndex, int sigIndex) {
        localVariableInfos[registerNum] =
                new LocalVariableInfo(dexFile.getString(nameIndex),
                                      dexFile.getType(typeIndex),
                                      dexFile.getString(sigIndex));

        StringBuilder sb = new StringBuilder();

        sb.append(".local ");
        sb.append(registerPrinter.formatRegister(registerNum));
        sb.append(", ");

        String name = dexFile.getString(nameIndex);
        if (name != null) {
            sb.append("\"");
            sb.append(name);
            sb.append("\"");
        } else {
            sb.append("null");
        }
        sb.append(":");
        sb.append(dexFile.getType(typeIndex));

        if (sigIndex != NO_INDEX) {
            sb.append(", \"");
            sb.append(dexFile.getString(sigIndex));
            sb.append("\"");
        }

        addDebugInfo(codeOffset, sb.toString());
    }

    private void handleGenericLocal(int registerNum, StringBuilder sb) {
        LocalVariableInfo localVariableInfo = localVariableInfos[registerNum];
        if (localVariableInfo != null) {
            sb.append("    # ");

            if (localVariableInfo.name != null) {
                sb.append("\"");
                sb.append(localVariableInfo.name);
                sb.append("\"");
            } else {
                sb.append("null");
            }

            sb.append(":");
            sb.append(localVariableInfo.type);

            if (localVariableInfo.signature != null) {
                sb.append(", \"");
                sb.append(localVariableInfo.signature);
                sb.append("\"");
            }
        }
    }

    private void addDebugInfo(int offset, String info) {
        List<String> infos = debugState.computeIfAbsent(offset, (key) -> new ArrayList<>());
        infos.add(info);
    }

    public static class LocalVariableInfo
    {
        final String name;
        final String type;
        final String signature;

        LocalVariableInfo(String name, String type, String signature) {
            this.name      = name;
            this.type      = type;
            this.signature = signature;
        }
    }
}

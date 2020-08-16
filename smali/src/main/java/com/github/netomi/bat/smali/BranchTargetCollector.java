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

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.Code;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.EncodedMethod;
import com.github.netomi.bat.dexfile.instruction.*;
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;

import java.util.*;

class      BranchTargetCollector
implements InstructionVisitor
{
    private final Map<Integer, Set<String>> branchInfos;

    public BranchTargetCollector(Map<Integer, Set<String>> branchInfos) {
        this.branchInfos = branchInfos;
    }

    @Override
    public void visitAnyInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction) {}

    @Override
    public void visitBranchInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BranchInstruction instruction) {
        int target = offset + instruction.getBranchOffset();

        String mnemonic = instruction.getOpcode().getMnemonic();
        String prefix   = mnemonic.startsWith("goto") ? "goto" : "cond";

        addBranchInfo(target, ":" + prefix + "_" + Integer.toHexString(target));
    }

    @Override
    public void visitPayloadInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PayloadInstruction instruction) {

    }

    @Override
    public void visitPackedSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PackedSwitchPayload payload) {

    }

    @Override
    public void visitSparseSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, SparseSwitchPayload payload) {

    }

    private void addBranchInfo(int offset, String info) {
        Set<String> infos = branchInfos.computeIfAbsent(offset, (key) -> new LinkedHashSet<>());
        infos.add(info);
    }
}

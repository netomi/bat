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
import com.github.netomi.bat.io.IndentingPrinter;

import java.util.*;

class      BranchTargetPrinter
implements InstructionVisitor
{
    private final Map<Integer, Set<String>> branchInfos;
    private final Map<Integer, Integer>     reversePayloadLookup;

    public BranchTargetPrinter() {
        this.branchInfos          = new HashMap<>();
        this.reversePayloadLookup = new HashMap<>();
    }

    public void printLabels(int offset, IndentingPrinter printer) {
        Set<String> labels = branchInfos.get(offset);
        if (labels != null) {
            for (String label : labels) {
                printer.println(label);
            }
        }
    }

    public String formatBranchInstructionTarget(int offset, BranchInstruction instruction) {
        int target = offset + instruction.getBranchOffset();

        String mnemonic = instruction.getOpcode().getMnemonic();
        String prefix   = mnemonic.startsWith("goto") ? "goto" : "cond";

        return ":" + prefix + "_" + Integer.toHexString(target);
    }

    public String formatPayloadInstructionTarget(int offset, PayloadInstruction instruction) {
        String prefix = null;
        switch (instruction.getOpcode()) {
            case FILL_ARRAY_DATA:
                prefix = "array";
                break;

            case PACKED_SWITCH:
                prefix = "pswitch_data";
                break;

            case SPARSE_SWITCH:
                prefix = "sswitch_data";
                break;
        }

        int target = offset + instruction.getPayloadOffset();
        return ":" + prefix + "_" + Integer.toHexString(target);
    }

    public String formatPackedSwitchTarget(int payloadOffset, int branchTarget) {
        int switchOffset = reversePayloadLookup.get(payloadOffset);
        int target = switchOffset + branchTarget;
        return ":pswitch_" + Integer.toHexString(target);
    }

    @Override
    public void visitAnyInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction) {}

    @Override
    public void visitBranchInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BranchInstruction instruction) {
        int target = offset + instruction.getBranchOffset();
        addBranchInfo(target, formatBranchInstructionTarget(offset, instruction));
    }

    @Override
    public void visitPayloadInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PayloadInstruction instruction) {
        int target = offset + instruction.getPayloadOffset();
        reversePayloadLookup.put(target, offset);
        addBranchInfo(target, formatPayloadInstructionTarget(offset, instruction));
    }

    @Override
    public void visitPackedSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PackedSwitchPayload payload) {
        for (int branchTarget : payload.branchTargets) {
            int switchOffset = reversePayloadLookup.get(offset);
            int target = switchOffset + branchTarget;
            addBranchInfo(target, formatPackedSwitchTarget(offset, branchTarget));
        }
    }

    @Override
    public void visitSparseSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, SparseSwitchPayload payload) {

    }

    private void addBranchInfo(int offset, String info) {
        Set<String> infos = branchInfos.computeIfAbsent(offset, (key) -> new LinkedHashSet<>());
        infos.add(info);
    }
}

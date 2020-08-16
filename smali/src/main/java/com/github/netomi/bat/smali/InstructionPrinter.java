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

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.instruction.*;
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;
import com.github.netomi.bat.io.IndentingPrinter;
import com.github.netomi.bat.util.Primitives;

import java.util.List;
import java.util.Map;
import java.util.Set;

class      InstructionPrinter
implements InstructionVisitor
{
    private final IndentingPrinter           printer;
    private final RegisterPrinter            registerPrinter;
    private final Map<Integer, Set<String>>  labelInfos;
    private final Map<Integer, List<String>> debugState;

    public InstructionPrinter(IndentingPrinter           printer,
                              RegisterPrinter            registerPrinter,
                              Map<Integer, Set<String>>  labelInfos,
                              Map<Integer, List<String>> debugState) {
        this.printer         = printer;
        this.registerPrinter = registerPrinter;
        this.labelInfos      = labelInfos;
        this.debugState      = debugState;
    }

    @Override
    public void visitAnyInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printGeneric(code, instruction, true);
    }

    @Override
    public void visitArithmeticInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArithmeticInstruction instruction) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printGeneric(code, instruction, false);

        StringBuilder sb = new StringBuilder();

        int literal = instruction.getLiteral();

        if (instruction.containsLiteral())  {
            sb.append(", ");
            sb.append(toHexString(literal));
        }

        printer.println(sb.toString());
    }

    @Override
    public void visitBranchInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BranchInstruction instruction) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printGeneric(code, instruction, false);

        StringBuilder sb = new StringBuilder();

        if (instruction.registers.length > 0) {
            sb.append(", ");
        } else {
            sb.append(' ');
        }

        int target      = offset + instruction.getBranchOffset();
        String mnemonic = instruction.getOpcode().getMnemonic();
        String prefix   = mnemonic.startsWith("goto") ? "goto" : "cond";

        sb.append(":" + prefix + "_" + Integer.toHexString(target));

        printer.println(sb.toString());
    }

    @Override
    public void visitFieldInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FieldInstruction instruction) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printGeneric(code, instruction, false);

        printer.print(", ");

        FieldID fieldID = instruction.getField(dexFile);

        printer.print(fieldID.getClassName(dexFile));
        printer.print("->");
        printer.print(fieldID.getName(dexFile));
        printer.print(":");
        printer.println(fieldID.getType(dexFile));
    }

    @Override
    public void visitLiteralInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, LiteralInstruction instruction) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printGeneric(code, instruction, false);

        StringBuilder sb = new StringBuilder();
        sb.append(", ");

        long value = instruction.getValue();
        sb.append(toHexString(value));

        printer.println(sb.toString());
    }

    @Override
    public void visitMethodInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodInstruction instruction) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printer.print(instruction.getMnemonic());

        if (instruction.registers.length > 0) {
            printer.print(" {");
            printRegisters(code, instruction);
            printer.print("}");
        } else {
            printer.print(" {}");
        }

        printer.print(", ");

        MethodID methodID = instruction.getMethod(dexFile);

        printer.print(methodID.getClassName(dexFile));
        printer.print("->");
        printer.print(methodID.getName(dexFile));
        printer.println(methodID.getProtoID(dexFile).getDescriptor(dexFile));
    }

    @Override
    public void visitPayloadInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PayloadInstruction instruction) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printGeneric(code, instruction, false);

        StringBuilder sb = new StringBuilder();

        if (instruction.registers.length > 0) {
            sb.append(", ");
        } else {
            sb.append(' ');
        }

        sb.append(Primitives.asHexValue(offset + instruction.getPayloadOffset(), 8));
        sb.append(" // ");

        if (instruction.getPayloadOffset() < 0) {
            sb.append('-');
            sb.append(Primitives.asHexValue(-instruction.getPayloadOffset(), 8));
        } else {
            sb.append('+');
            sb.append(Primitives.asHexValue(instruction.getPayloadOffset(), 8));
        }

        printer.println(sb.toString());
    }

    @Override
    public void visitStringInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, StringInstruction instruction) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printGeneric(code, instruction, false);
        printer.println(", \"" + instruction.getString(dexFile) + "\"");
    }

    @Override
    public void visitTypeInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, TypeInstruction instruction) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printGeneric(code, instruction, false);

        printer.print(", ");

        TypeID typeID = instruction.getTypeID(dexFile);
        printer.println(typeID.getType(dexFile));
    }

    @Override
    public void visitFillArrayPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FillArrayPayload payload) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printer.println(payload.toString());
    }

    @Override
    public void visitPackedSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PackedSwitchPayload payload) {        printer.println();
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printer.println(payload.toString());
    }

    @Override
    public void visitSparseSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, SparseSwitchPayload payload) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printer.println(payload.toString());
    }

    private String toHexString(long value) {
        return value < 0 ?
            String.format("-0x%x", -value) :
            String.format("0x%x", value);
    }

    private void printGeneric(Code code, DexInstruction instruction, boolean appendNewLine) {
        printer.print(instruction.getMnemonic());

        if (instruction.registers.length > 0) {
            printer.print(" ");
            printRegisters(code, instruction);
        }

        if (appendNewLine) {
            printer.println();
        }
    }

    private void printRegisters(Code code, DexInstruction instruction) {
        for (int idx = 0; idx < instruction.registers.length; idx++) {
            if (idx > 0) {
                printer.print(", ");
            }

            int registerNum = instruction.registers[idx];
            registerPrinter.printRegister(printer, registerNum);
        }
    }

    private void printDebugInfo(int offset) {
        if (debugState == null) {
            return;
        }

        List<String> debugInfos = debugState.get(offset);
        if (debugInfos != null) {
            for (String info : debugInfos) {
                printer.println(info);
            }
        }
    }

    private void printLabels(int offset) {
        Set<String> labels = labelInfos.get(offset);
        if (labels != null) {
            for (String label : labels) {
                printer.println(label);
            }
        }
    }
}

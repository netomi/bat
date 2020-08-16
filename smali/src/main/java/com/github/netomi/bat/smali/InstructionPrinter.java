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

import java.util.List;
import java.util.Map;

class      InstructionPrinter
implements InstructionVisitor
{
    private final IndentingPrinter           printer;
    private final RegisterPrinter            registerPrinter;
    private final BranchTargetPrinter        branchTargetPrinter;
    private final Map<Integer, List<String>> debugState;

    public InstructionPrinter(IndentingPrinter           printer,
                              RegisterPrinter            registerPrinter,
                              BranchTargetPrinter        branchTargetPrinter,
                              Map<Integer, List<String>> debugState) {
        this.printer             = printer;
        this.registerPrinter     = registerPrinter;
        this.branchTargetPrinter = branchTargetPrinter;
        this.debugState          = debugState;
    }

    @Override
    public void visitAnyInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction) {
        printCommon(offset, code, instruction, true);
    }

    @Override
    public void visitArithmeticInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArithmeticInstruction instruction) {
        printCommon(offset, code, instruction, false);

        if (instruction.containsLiteral())  {
            printer.print(", ");
            printer.print(toHexString(instruction.getLiteral()));
        }

        printer.println();
    }

    @Override
    public void visitBranchInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BranchInstruction instruction) {
        printCommon(offset, code, instruction, false);

        if (instruction.registers.length > 0) {
            printer.print(", ");
        } else {
            printer.print(" ");
        }

        printer.println(branchTargetPrinter.formatBranchInstructionTarget(offset, instruction));
    }

    @Override
    public void visitFieldInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FieldInstruction instruction) {
        printCommon(offset, code, instruction, false);

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
        printCommon(offset, code, instruction, false);
        printer.print(", ");
        printer.println(toHexString(instruction.getValue()));
    }

    @Override
    public void visitMethodInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodInstruction instruction) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printer.print(instruction.getMnemonic());

        if (instruction.registers.length > 0) {
            printer.print(" {");
            printRegisters(instruction);
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
        printCommon(offset, code, instruction, false);

        if (instruction.registers.length > 0) {
            printer.print(", ");
        } else {
            printer.print(" ");
        }

        printer.println(branchTargetPrinter.formatPayloadInstructionTarget(offset, instruction));
    }

    @Override
    public void visitStringInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, StringInstruction instruction) {
        printCommon(offset, code, instruction, false);
        printer.println(", \"" + instruction.getString(dexFile) + "\"");
    }

    @Override
    public void visitTypeInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, TypeInstruction instruction) {
        printCommon(offset, code, instruction, false);
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
        printer.println(".packed-switch " + toHexString(payload.firstKey));
        printer.levelUp();
        for (int branchTarget : payload.branchTargets) {
            printer.println(branchTargetPrinter.formatPackedSwitchTarget(offset, branchTarget));
        }
        printer.levelDown();
        printer.println(".end packed-switch");
    }

    @Override
    public void visitSparseSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, SparseSwitchPayload payload) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printer.println(".sparse-switch");
        printer.levelUp();
        for (int i = 0; i < payload.keys.length; i++) {
            int key    = payload.keys[i];
            int target = payload.branchTargets[i];

            printer.print(toHexString(key));
            printer.print(" -> ");
            printer.println(branchTargetPrinter.formatSparseSwitchTarget(offset, target));
        }
        printer.levelDown();
        printer.println(".end sparse-switch");
    }

    private String toHexString(long value) {
        return value < 0 ?
            String.format("-0x%x", -value) :
            String.format("0x%x", value);
    }

    private void printCommon(int offset, Code code, DexInstruction instruction, boolean appendNewLine) {
        printer.println();
        printDebugInfo(offset);
        printLabels(offset);
        printGeneric(code, instruction, appendNewLine);
    }

    private void printGeneric(Code code, DexInstruction instruction, boolean appendNewLine) {
        printer.print(instruction.getMnemonic());

        if (instruction.registers.length > 0) {
            printer.print(" ");
            printRegisters(instruction);
        }

        if (appendNewLine) {
            printer.println();
        }
    }

    private void printRegisters(DexInstruction instruction) {
        boolean isRangeInstruction = instruction.getMnemonic().contains("range");
        if (isRangeInstruction) {
            int firstRegister = instruction.registers[0];
            int lastRegister  = instruction.registers[instruction.registers.length - 1];
            registerPrinter.printRegister(printer, firstRegister);
            printer.print(" .. ");
            registerPrinter.printRegister(printer, lastRegister);
        } else {
            for (int idx = 0; idx < instruction.registers.length; idx++) {
                if (idx > 0) {
                    printer.print(", ");
                }

                int registerNum = instruction.registers[idx];
                registerPrinter.printRegister(printer, registerNum);
            }
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
        branchTargetPrinter.printLabels(offset, printer);
    }
}

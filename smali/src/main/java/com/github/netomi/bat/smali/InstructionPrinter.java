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
import com.github.netomi.bat.dexfile.util.Numbers;
import com.github.netomi.bat.dexfile.visitor.AllCodeVisitor;
import com.github.netomi.bat.dexfile.visitor.AllInstructionsVisitor;
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
        printCommon(code, offset, instruction, true);
        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitArithmeticInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArithmeticInstruction instruction) {
        printCommon(code, offset, instruction, false);

        if (instruction.containsLiteral())  {
            printer.print(", ");
            printer.print(toHexString(instruction.getLiteral()));
        }

        printer.println();
        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitBranchInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BranchInstruction instruction) {
        printCommon(code, offset, instruction, false);

        if (instruction.registers.length > 0) {
            printer.print(", ");
        } else {
            printer.print(" ");
        }

        printer.println(branchTargetPrinter.formatBranchInstructionTarget(offset, instruction));
        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitFieldInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FieldInstruction instruction) {
        printCommon(code, offset, instruction, false);

        printer.print(", ");

        FieldID fieldID = instruction.getField(dexFile);

        printer.print(fieldID.getClassType(dexFile));
        printer.print("->");
        printer.print(fieldID.getName(dexFile));
        printer.print(":");
        printer.println(fieldID.getType(dexFile));
        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitLiteralInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, LiteralInstruction instruction) {
        printCommon(code, offset, instruction, false);
        printer.print(", ");
        printer.print(toHexString(instruction.getValue()));

        DexOpCode            opCode            = instruction.getOpcode();
        DexInstructionFormat instructionFormat = opCode.getFormat();

        // FIXME: this is a hack and should be made clean.
        if (instructionFormat == DexInstructionFormat.FORMAT_21h && opCode.targetsWideRegister() ||
            instructionFormat == DexInstructionFormat.FORMAT_51l) {
            printer.print("L");
        }

        if (instruction.getOpcode().targetsWideRegister()) {
            printCommentIfLikelyDouble(printer, instruction.getValue());
        } else {
            printCommentIfLikelyFloat(printer, (int) instruction.getValue());
        }

        printer.println();
        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitMethodInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodInstruction instruction) {
        printer.println();
        printDebugInfo(offset);
        printLabels(code, offset);

        MethodID methodID = instruction.getMethod(dexFile);

        if (methodID.getName(dexFile).startsWith("access$")) {
            AccessMethodFollower methodFollower = new AccessMethodFollower();

            methodID.accept(dexFile, new AllCodeVisitor(
                new AllInstructionsVisitor(
                methodFollower)));

            String explanation = methodFollower.getExplanation();
            if (explanation != null) {
                printer.println("# " + explanation);
            }
        }

        printer.print(instruction.getMnemonic());

        if (instruction.registers.length > 0) {
            printer.print(" {");
            printRegisters(instruction);
            printer.print("}");
        } else {
            printer.print(" {}");
        }

        printer.print(", ");

        printer.print(methodID.getClassType(dexFile));
        printer.print("->");
        printer.print(methodID.getName(dexFile));
        printer.println(methodID.getProtoID(dexFile).getDescriptor(dexFile));
        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitPayloadInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PayloadInstruction instruction) {
        printCommon(code, offset, instruction, false);

        if (instruction.registers.length > 0) {
            printer.print(", ");
        } else {
            printer.print(" ");
        }

        printer.println(branchTargetPrinter.formatPayloadInstructionTarget(offset, instruction));
        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitStringInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, StringInstruction instruction) {
        printCommon(code, offset, instruction, false);
        String str = instruction.getString(dexFile);

        // escape some chars
        // TODO: extract to util class
        str = str.replaceAll("'",  "\\\\'");
        str = str.replaceAll("\"", "\\\\\"");
        str = str.replaceAll("\n", "\\\\n");

        printer.println(", \"" + str + "\"");
        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitTypeInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, TypeInstruction instruction) {
        printCommon(code, offset, instruction, false);
        printer.print(", ");
        TypeID typeID = instruction.getTypeID(dexFile);
        printer.println(typeID.getType(dexFile));
        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitFillArrayPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FillArrayPayload payload) {
        printer.println();
        printDebugInfo(offset);
        printLabels(code, offset);
        printer.println(".array-data " + payload.elementWidth);
        printer.levelUp();
        for (int i = 0; i < payload.getElements(); i++) {
            long value = payload.getElement(i);
            printer.print(toHexString(value));

            if (payload.elementWidth <= 4) {
                printCommentIfLikelyFloat(printer, (int) value);
            } else if (payload.elementWidth == 8) {
                printCommentIfLikelyDouble(printer, value);
            }

            printer.println();
        }
        printer.levelDown();
        printer.println(".end array-data");
    }

    @Override
    public void visitPackedSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PackedSwitchPayload payload) {
        printer.println();
        printDebugInfo(offset);
        printLabels(code, offset);
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
        printLabels(code, offset);
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

    // private utility methods.

    private String toHexString(long value) {
        return value < 0 ?
            String.format("-0x%x", -value) :
            String.format("0x%x", value);
    }

    private void printCommon(Code code, int offset, DexInstruction instruction, boolean appendNewLine) {
        printer.println();
        printDebugInfo(offset);
        printLabels(code, offset);
        printGeneric(instruction, appendNewLine);
    }

    private void printGeneric(DexInstruction instruction, boolean appendNewLine) {
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

    private void printLabels(Code code, int offset) {
        branchTargetPrinter.printLabels(offset, printer);
        TryPrinter.printTryCatchLabels(code, offset, printer);
    }

    private void printEndLabels(DexFile dexFile, Code code, int offset, int instructionLength) {
        TryPrinter.printTryEndLabel(dexFile, code, offset, instructionLength, printer);
    }

    private void printCommentIfLikelyFloat(IndentingPrinter printer, int val) {
        if (Numbers.isLikelyFloat(val)) {
            printer.print("    # ");
            float fVal = Float.intBitsToFloat(val);
            if (fVal == Float.POSITIVE_INFINITY)
                printer.print("Float.POSITIVE_INFINITY");
            else if (fVal == Float.NEGATIVE_INFINITY)
                printer.print("Float.NEGATIVE_INFINITY");
            else if (Float.isNaN(fVal))
                printer.print("Float.NaN");
            else if (fVal == Float.MAX_VALUE)
                printer.print("Float.MAX_VALUE");
            else if (fVal == (float)Math.PI)
                printer.print("(float)Math.PI");
            else if (fVal == (float)Math.E)
                printer.print("(float)Math.E");
            else {
                printer.print(Float.toString(fVal));
                printer.print("f");
            }
        }
    }

    private void printCommentIfLikelyDouble(IndentingPrinter printer, long val) {
        if (Numbers.isLikelyDouble(val)) {
            printer.print("    # ");
            double dVal = Double.longBitsToDouble(val);
            if (dVal == Double.POSITIVE_INFINITY)
                printer.print("Double.POSITIVE_INFINITY");
            else if (dVal == Double.NEGATIVE_INFINITY)
                printer.print("Double.NEGATIVE_INFINITY");
            else if (Double.isNaN(dVal))
                printer.print("Double.NaN");
            else if (dVal == Double.MAX_VALUE)
                printer.print("Double.MAX_VALUE");
            else if (dVal == Math.PI)
                printer.print("Math.PI");
            else if (dVal == Math.E)
                printer.print("Math.E");
            else
                printer.print(Double.toString(dVal));
        }
    }

    // helper classes.

    private static class AccessMethodFollower
    implements           InstructionVisitor
    {
        private String explanation;

        public String getExplanation() {
            return explanation;
        }

        @Override
        public void visitAnyInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction) {}

        @Override
        public void visitFieldInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FieldInstruction instruction) {
            String mnemonic = instruction.getMnemonic();
            String action   = mnemonic.contains("get") ? "getter" : "setter";

            FieldID fieldID = instruction.getField(dexFile);
            explanation = action + " for: " + fieldID.getClassType(dexFile) + "->" + fieldID.getName(dexFile) + ":" + fieldID.getType(dexFile);
        }

        @Override
        public void visitMethodInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodInstruction instruction) {
            MethodID methodID = instruction.getMethod(dexFile);
            explanation = "invokes: " + methodID.getClassType(dexFile) + "->" + methodID.getName(dexFile) + methodID.getProtoID(dexFile).getDescriptor(dexFile);
        }
    }
}

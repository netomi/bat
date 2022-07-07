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
package com.github.netomi.bat.smali.disassemble;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.instruction.*;
import com.github.netomi.bat.dexfile.util.Numbers;
import com.github.netomi.bat.dexfile.value.EncodedArrayValue;
import com.github.netomi.bat.dexfile.visitor.AllCodeVisitor;
import com.github.netomi.bat.dexfile.visitor.AllInstructionsVisitor;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;
import com.github.netomi.bat.io.IndentingPrinter;
import com.github.netomi.bat.util.Strings;

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
        printCommon(code, offset, instruction, false, true);
        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitArithmeticLiteralInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArithmeticLiteralInstruction instruction) {
        printCommon(code, offset, instruction, false, false);

        printer.print(", ");
        printer.println(toHexString(instruction.getLiteral()));

        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitBranchInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BranchInstruction instruction) {
        printCommon(code, offset, instruction, false, false);

        if (instruction.registers.length > 0) {
            printer.print(", ");
        } else {
            printer.print(" ");
        }

        printer.println(branchTargetPrinter.formatBranchInstructionTarget(offset, instruction));

        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitCallSiteInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, CallSiteInstruction instruction) {
        printCommon(code, offset, instruction, true, false);

        printer.print(", call_site_" + instruction.getCallSiteIndex());

        CallSite callSite = instruction.getCallSiteID(dexFile).getCallSite();
        EncodedArrayValue arrayValue = callSite.getArray();

        printer.print("(");

        EncodedValueVisitor valueVisitor = new CallSiteArgumentPrinter(printer);
        for (int i = 1; i < arrayValue.getEncodedValueCount(); i++) {
            if (i > 1) {
                printer.print(", ");
            }
            arrayValue.getEncodedValue(i).accept(dexFile, valueVisitor);
        }

        printer.print(")@");

        MethodHandle methodHandle = callSite.getMethodHandle(dexFile);
        printer.print(methodHandle.getTargetClassType(dexFile));
        printer.print("->");
        printer.print(methodHandle.getTargetMemberName(dexFile));
        printer.println(methodHandle.getTargetDescriptor(dexFile));
    }

    @Override
    public void visitFieldInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FieldInstruction instruction) {
        printCommon(code, offset, instruction, false, false);

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
        printCommon(code, offset, instruction, false, false);

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
    public void visitAnyMethodInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodInstruction instruction) {
        String methodFollowerExplanation = null;
        MethodID methodID = instruction.getMethodID(dexFile);
        if (methodID.getName(dexFile).startsWith("access$")) {
            AccessMethodFollower methodFollower = new AccessMethodFollower();

            methodID.accept(dexFile, new AllCodeVisitor(
                new AllInstructionsVisitor(
                methodFollower)));

            if (methodFollower.getExplanation() != null) {
                methodFollowerExplanation = "# " + methodFollower.getExplanation();
            }
        }

        printCommon(code, offset, instruction, methodFollowerExplanation, true, false);

        printer.print(", ");
        printer.print(methodID.getClassType(dexFile));
        printer.print("->");
        printer.print(methodID.getName(dexFile));
        printer.print(methodID.getProtoID(dexFile).getDescriptor(dexFile));

        if (instruction instanceof MethodProtoInstruction) {
            printer.println(", " + ((MethodProtoInstruction) instruction).getProtoID(dexFile).getDescriptor(dexFile));
        } else {
            printer.println();
        }

        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitMethodHandleRefInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodHandleRefInstruction instruction) {
        printCommon(code, offset, instruction, false, false);

        printer.print(", invoke-instance@");

        MethodHandle methodHandle = instruction.getMethodHandle(dexFile);
        printer.print(methodHandle.getTargetClassType(dexFile));
        printer.print("->");
        printer.print(methodHandle.getTargetMemberName(dexFile));
        printer.println(methodHandle.getTargetMemberDescriptor(dexFile));

        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitMethodTypeRefInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodTypeRefInstruction instruction) {
        printCommon(code, offset, instruction, false, false);

        printer.print(", ");

        ProtoID protoID = instruction.getProtoID(dexFile);
        printer.println(protoID.getDescriptor(dexFile));

        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitPayloadInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PayloadInstruction instruction) {
        printCommon(code, offset, instruction, false, false);

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
        printCommon(code, offset, instruction, false, false);
        String str = instruction.getString(dexFile);

        // escape some chars
        str = Strings.escapeString(str);

        printer.println(", \"" + str + "\"");
        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitTypeInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, TypeInstruction instruction) {
        printCommon(code, offset, instruction, false, false);

        printer.print(", ");

        TypeID typeID = instruction.getTypeID(dexFile);
        printer.println(typeID.getType(dexFile));

        printEndLabels(dexFile, code, offset, instruction.getLength());
    }

    @Override
    public void visitArrayTypeInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArrayTypeInstruction instruction) {
        printCommon(code, offset, instruction, true, false);

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
            switch (payload.elementWidth) {
                case 1:
                    byte byteValue = payload.getElementAsByte(i);
                    printer.print(toHexString(byteValue) + "t");
                    printCommentIfLikelyFloat(printer, byteValue);
                    break;

                case 2:
                    short shortValue = payload.getElementAsShort(i);
                    printer.print(toHexString(shortValue) + "s");
                    printCommentIfLikelyFloat(printer, shortValue);
                    break;

                case 4:
                    int intValue = payload.getElementAsInt(i);
                    printer.print(toHexString(intValue));
                    printCommentIfLikelyFloat(printer, intValue);
                    break;

                case 8:
                    long longValue = payload.getElementAsLong(i);
                    String hexString = toHexString(longValue);
                    printer.print(hexString);

                    if (longValue < Integer.MIN_VALUE ||
                        longValue > Integer.MAX_VALUE) {
                        printer.print("L");
                    }

                    printCommentIfLikelyDouble(printer, longValue);
                    break;
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

    private String toHexString(int value) {
        return value < 0 ?
            String.format("-0x%x", -value) :
            String.format("0x%x", value);
    }

    private String toHexString(short value) {
        return value < 0 ?
            String.format("-0x%x", -value) :
            String.format("0x%x", value);
    }

    private String toHexString(byte value) {
        return value < 0 ?
            String.format("-0x%x", -value) :
            String.format("0x%x", value);
    }

    private void printCommon(Code code, int offset, DexInstruction instruction, boolean useBrackets, boolean appendNewLine) {
        printCommon(code, offset, instruction, null, useBrackets, appendNewLine);
    }

    private void printCommon(Code code, int offset, DexInstruction instruction, String preInstruction, boolean useBrackets, boolean appendNewLine) {
        printer.println();
        printDebugInfo(offset);
        printLabels(code, offset);
        if (preInstruction != null) {
            printer.println(preInstruction);
        }
        printer.print(instruction.getMnemonic());
        printRegisters(instruction, useBrackets);

        if (appendNewLine) {
            printer.println();
        }
    }

    private void printRegisters(DexInstruction instruction, boolean useBrackets) {
        if (useBrackets) {
            if (instruction.registers.length > 0) {
                printer.print(" {");
                printRegistersInternal(instruction);
                printer.print("}");
            }
            else {
                printer.print(" {}");
            }
        } else {
            if (instruction.registers.length > 0) {
                printer.print(" ");
                printRegistersInternal(instruction);
            }
        }
    }

    private void printRegistersInternal(DexInstruction instruction) {
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
        TryCatchPrinter.printTryCatchLabels(code, offset, printer);
    }

    private void printEndLabels(DexFile dexFile, Code code, int offset, int instructionLength) {
        TryCatchPrinter.printTryEndLabel(dexFile, code, offset, instructionLength, printer);
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
            MethodID methodID = instruction.getMethodID(dexFile);
            explanation = "invokes: " + methodID.getClassType(dexFile) + "->" + methodID.getName(dexFile) + methodID.getProtoID(dexFile).getDescriptor(dexFile);
        }
    }
}

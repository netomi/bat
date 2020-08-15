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

class      InstructionPrinter
implements InstructionVisitor
{
    private final IndentingPrinter printer;

    public InstructionPrinter(IndentingPrinter printer) {
        this.printer = printer;
    }

    @Override
    public void visitAnyInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction) {
        printer.println();
        printGeneric(code, instruction, true);
    }

    @Override
    public void visitArithmeticInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArithmeticInstruction instruction) {
        printer.println();
        printGeneric(code, instruction, false);

        StringBuilder sb = new StringBuilder();

        int literal = instruction.getLiteral();

        if (instruction.containsLiteral())  {
            sb.append(", ");
            sb.append("#int ");
            sb.append(literal);
            sb.append(" // #");

            switch (instruction.getOpcode().getFormat()) {
                case FORMAT_22s:
                    sb.append(Primitives.asHexValue((short) literal));
                    break;

                case FORMAT_22b:
                    sb.append(Primitives.asHexValue((byte) literal));
                    break;
            }
        }

        printer.println(sb.toString());
    }

    @Override
    public void visitBranchInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BranchInstruction instruction) {
        printer.println();
        printGeneric(code, instruction, false);

        StringBuilder sb = new StringBuilder();

        if (instruction.registers.length > 0) {
            sb.append(", ");
        } else {
            sb.append(' ');
        }

        sb.append(Primitives.asHexValue(offset + instruction.getBranchOffset(), 4));
        sb.append(" // ");

        if (instruction.getBranchOffset() < 0) {
            sb.append('-');
            sb.append(Primitives.asHexValue(-instruction.getBranchOffset(), 4));
        } else {
            sb.append('+');
            sb.append(Primitives.asHexValue(instruction.getBranchOffset(), 4));
        }

        printer.println(sb.toString());
    }

    @Override
    public void visitFieldInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FieldInstruction instruction) {
        printer.println();
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
        printGeneric(code, instruction, false);

        StringBuilder sb = new StringBuilder();
        sb.append(", ");

        long value = instruction.getValue();

        switch (instruction.getOpcode().getFormat()) {
            case FORMAT_11n:
            case FORMAT_22b:
                sb.append(String.format("0x%x", value, (byte) value));
                break;

            case FORMAT_21h:
                // The printed format varies a bit based on the actual opcode.
                if (instruction.getOpcode() == DexOpCode.CONST_HIGH16) {
                    short v = (short) (value >> 16);
                    sb.append(String.format("#int %d // #%x", value, v));
                } else {
                    short v = (short) (value >> 48);
                    sb.append(String.format("#long %d // #%x", value, v));
                }
                break;

            case FORMAT_21s:
            case FORMAT_22s:
                sb.append(String.format("#int %d // #%x", value, (short) value));
                break;

            case FORMAT_31i:
                sb.append(String.format("#float %g // #%08x", Float.intBitsToFloat((int) value), value ));
                break;

            case FORMAT_51l:
                //sb.append(String.format("#double %g // #%016lx", Double.longBitsToDouble(value), value));
                break;

        }

        printer.println(sb.toString());
    }

    @Override
    public void visitMethodInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodInstruction instruction) {
        printer.println();

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
        printGeneric(code, instruction, false);
        printer.println(", \"" + instruction.getString(dexFile) + "\"");
    }

    @Override
    public void visitTypeInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, TypeInstruction instruction) {
        printer.println();
        printGeneric(code, instruction, false);

        printer.print(", ");

        TypeID typeID = instruction.getTypeID(dexFile);
        printer.println(typeID.getType(dexFile));
    }

    @Override
    public void visitFillArrayPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FillArrayPayload payload) {
        printer.println();
        printer.println(payload.toString());
    }

    @Override
    public void visitPackedSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PackedSwitchPayload payload) {        printer.println();
        printer.println();
        printer.println(payload.toString());
    }

    @Override
    public void visitSparseSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, SparseSwitchPayload payload) {
        printer.println();
        printer.println(payload.toString());
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
        int localVars = code.registersSize - code.insSize;

        for (int idx = 0; idx < instruction.registers.length; idx++) {
            if (idx > 0) {
                printer.print(", ");
            }

            String registerPrefix = idx < localVars ? "v" : "p";
            int    registerIndex  = idx < localVars ?
                instruction.registers[idx] :
                instruction.registers[idx] - localVars;

            printer.print(registerPrefix + registerIndex);
        }
    }
}

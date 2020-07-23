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
package com.github.netomi.bat.dump;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.instruction.*;
import com.github.netomi.bat.dexfile.util.Mutf8;
import com.github.netomi.bat.dexfile.util.Primitives;
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;

class      InstructionPrinter
implements InstructionVisitor
{
    private final BufferedPrinter printer;

    public InstructionPrinter(BufferedPrinter printer) {
        this.printer = printer;
    }

    @Override
    public void visitAnyInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction) {
        printGeneric(instruction);
    }

    @Override
    public void visitArithmeticInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArithmeticInstruction instruction) {
        printGeneric(instruction);

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

        printer.print(sb.toString());
    }

    @Override
    public void visitBranchInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BranchInstruction instruction) {
        printGeneric(instruction);

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

        printer.print(sb.toString());
    }

    @Override
    public void visitFieldInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FieldInstruction instruction) {
        printGeneric(instruction);

        StringBuilder sb = new StringBuilder();
        sb.append(", ");

        FieldID fieldID = instruction.getField(dexFile);

        sb.append(fieldID.getClassName(dexFile));
        sb.append('.');
        sb.append(fieldID.getName(dexFile));
        sb.append(':');
        sb.append(fieldID.getType(dexFile));

        sb.append(" // field@");
        sb.append(Primitives.asHexValue(instruction.getFieldIndex(), 4));

        printer.print(sb.toString());
    }

    @Override
    public void visitLiteralInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, LiteralInstruction instruction) {
        printGeneric(instruction);

        StringBuilder sb = new StringBuilder();
        sb.append(", ");

        long value = instruction.getValue();

        switch (instruction.getOpcode().getFormat()) {
            case FORMAT_11n:
            case FORMAT_22b:
                sb.append(String.format("#int %d // #%x", value, (byte) value));
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
                sb.append(new PrintfFormat("#float %g // #%08x").sprintf(new Object[] { Float.intBitsToFloat((int) value), value }));
                break;

            case FORMAT_51l:
                sb.append(new PrintfFormat("#double %g // #%016lx").sprintf(new Object[] { Double.longBitsToDouble(value), value }));
                break;

        }

        printer.print(sb.toString());
    }

    @Override
    public void visitMethodInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodInstruction instruction) {
        StringBuilder sb = new StringBuilder();

        sb.append(instruction.getMnemonic());

        if (instruction.registers.length > 0) {
            sb.append(' ');
            sb.append('{');
            for (int idx = 0; idx < instruction.registers.length; idx++) {
                if (idx > 0) {
                    sb.append(", ");
                }
                sb.append('v');
                sb.append(instruction.registers[idx]);
            }
            sb.append('}');
        } else {
            sb.append("{}");
        }

        sb.append(", ");

        MethodID methodID = instruction.getMethod(dexFile);

        sb.append(methodID.getClassName(dexFile));
        sb.append('.');
        sb.append(methodID.getName(dexFile));
        sb.append(':');
        sb.append(methodID.getProtoID(dexFile).getDescriptor(dexFile));

        sb.append(" // method@");
        sb.append(Primitives.asHexValue(instruction.getMethodIndex(), 4));

        printer.print(sb.toString());
    }

    @Override
    public void visitPayloadInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PayloadInstruction instruction) {
        printGeneric(instruction);

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

        printer.print(sb.toString());
    }

    @Override
    public void visitStringInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, StringInstruction instruction) {
        printGeneric(instruction);

        printer.printAsMutf8(", \"" + instruction.getString(dexFile) + "\" // string@", false);

        if (instruction.getOpcode() == DexOpCode.CONST_STRING) {
            printer.print(Primitives.asHexValue(instruction.getStringIndex(), 4));
        } else {
            printer.print(Primitives.asHexValue(instruction.getStringIndex(), 8));
        }
    }

    @Override
    public void visitTypeInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, TypeInstruction instruction) {
        printGeneric(instruction);

        StringBuilder sb = new StringBuilder();

        sb.append(", ");

        TypeID typeID = instruction.getTypeID(dexFile);
        sb.append(typeID.getType(dexFile));

        sb.append(" // type@");
        sb.append(Primitives.asHexValue(instruction.getTypeIndex(), 4));

        printer.print(sb.toString());
    }

    @Override
    public void visitFillArrayPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FillArrayPayload payload) {
        printer.print(payload.toString());
    }

    @Override
    public void visitPackedSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PackedSwitchPayload payload) {
        printer.print(payload.toString());
    }

    @Override
    public void visitSparseSwitchPayload(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, SparseSwitchPayload payload) {
        printer.print(payload.toString());
    }

    private void printGeneric(DexInstruction instruction) {
        StringBuilder sb = new StringBuilder();

        sb.append(instruction.getMnemonic());

        if (instruction.getOpcode() == DexOpCode.NOP) {
            sb.append(" // spacer");
        }

        if (instruction.registers.length > 0) {
            sb.append(' ');
            for (int idx = 0; idx < instruction.registers.length; idx++) {
                if (idx > 0) {
                    sb.append(", ");
                }
                sb.append('v');
                sb.append(instruction.registers[idx]);
            }
        }

        printer.print(sb.toString());
    }
}

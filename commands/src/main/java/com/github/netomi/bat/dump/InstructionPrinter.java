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
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;
import com.github.netomi.bat.util.Primitives;

import static com.github.netomi.bat.dexfile.instruction.DexOpCode.GOTO_32;

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

        int literal = instruction.getLiteral();
        if (instruction.containsLiteral())  {
            printer.print(", ");
            printer.print("#int ");
            printer.print(Integer.toString(literal));
            printer.print(" // #");

            switch (instruction.getOpcode().getFormat()) {
                case FORMAT_22s:
                    printer.print(Primitives.asHexValue((short) literal));
                    break;

                case FORMAT_22b:
                    printer.print(Primitives.asHexValue((byte) literal));
                    break;
            }
        }
    }

    @Override
    public void visitBranchInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, BranchInstruction instruction) {
        printGeneric(instruction);

        if (instruction.registers.length > 0) {
            printer.print(", ");
        } else {
            printer.print(" ");
        }

        if (instruction.getOpcode() == GOTO_32) {
            printer.print("#");
            if (instruction.getBranchOffset() < 0) {
                printer.print("-");
                printer.print(Primitives.asHexValue(-instruction.getBranchOffset(), 8));
            }
            else {
                printer.print(Primitives.asHexValue(instruction.getBranchOffset(), 8));
            }
        } else {
            printer.print(Primitives.asHexValue(offset + instruction.getBranchOffset(), 4));
            printer.print(" // ");

            if (instruction.getBranchOffset() < 0) {
                printer.print("-");
                printer.print(Primitives.asHexValue(-instruction.getBranchOffset(), 4));
            }
            else {
                printer.print("+");
                printer.print(Primitives.asHexValue(instruction.getBranchOffset(), 4));
            }
        }
    }

    @Override
    public void visitFieldInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FieldInstruction instruction) {
        printGeneric(instruction);

        printer.print(", ");

        FieldID fieldID = instruction.getField(dexFile);

        printer.print(fieldID.getClassType(dexFile));
        printer.print(".");
        printer.print(fieldID.getName(dexFile));
        printer.print(":");
        printer.print(fieldID.getType(dexFile));

        printer.print(" // field@");
        printer.print(Primitives.asHexValue(instruction.getFieldIndex(), 4));
    }

    @Override
    public void visitLiteralInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, LiteralInstruction instruction) {
        printGeneric(instruction);

        printer.print(", ");

        long value = instruction.getValue();

        switch (instruction.getOpcode().getFormat()) {
            case FORMAT_11n:
            case FORMAT_22b:
                printer.print(String.format("#int %d // #%x", value, (byte) value));
                break;

            case FORMAT_21h:
                // The printed format varies a bit based on the actual opcode.
                if (instruction.getOpcode() == DexOpCode.CONST_HIGH16) {
                    short v = (short) (value >> 16);
                    printer.print(String.format("#int %d // #%x", value, v));
                } else {
                    short v = (short) (value >> 48);
                    printer.print(String.format("#long %d // #%x", value, v));
                }
                break;

            case FORMAT_21s:
            case FORMAT_22s:
                printer.print(String.format("#int %d // #%x", value, (short) value));
                break;

            case FORMAT_31i:
                printer.print(new PrintfFormat("#float %g // #%08x").sprintf(new Object[] { Float.intBitsToFloat((int) value), value }));
                break;

            case FORMAT_51l:
                printer.print(new PrintfFormat("#double %g // #%016lx").sprintf(new Object[] { Double.longBitsToDouble(value), value }));
                break;
        }
    }

    @Override
    public void visitMethodInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, MethodInstruction instruction) {
        printer.print(instruction.getMnemonic());

        if (instruction.registers.length > 0) {
            printer.print(" ");
            printer.print("{");
            for (int idx = 0; idx < instruction.registers.length; idx++) {
                if (idx > 0) {
                    printer.print(", ");
                }
                printer.print("v");
                printer.print(Integer.toString(instruction.registers[idx]));
            }
            printer.print("}");
        } else {
            printer.print("{}");
        }

        printer.print(", ");

        MethodID methodID = instruction.getMethod(dexFile);

        printer.print(methodID.getClassType(dexFile));
        printer.print(".");
        printer.print(methodID.getName(dexFile));
        printer.print(":");
        printer.print(methodID.getProtoID(dexFile).getDescriptor(dexFile));

        printer.print(" // method@");
        printer.print(Primitives.asHexValue(instruction.getMethodIndex(), 4));
    }

    @Override
    public void visitPayloadInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, PayloadInstruction instruction) {
        printGeneric(instruction);

        if (instruction.registers.length > 0) {
            printer.print(", ");
        } else {
            printer.print(" ");
        }

        printer.print(Primitives.asHexValue(offset + instruction.getPayloadOffset(), 8));
        printer.print(" // ");

        if (instruction.getPayloadOffset() < 0) {
            printer.print("-");
            printer.print(Primitives.asHexValue(-instruction.getPayloadOffset(), 8));
        } else {
            printer.print("+");
            printer.print(Primitives.asHexValue(instruction.getPayloadOffset(), 8));
        }
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

        printer.print(", ");

        TypeID typeID = instruction.getTypeID(dexFile);
        printer.print(typeID.getType(dexFile));

        printer.print(" // type@");
        printer.print(Primitives.asHexValue(instruction.getTypeIndex(), 4));
    }

    @Override
    public void visitArrayTypeInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, ArrayTypeInstruction instruction) {
        printer.print(instruction.getMnemonic());

        if (instruction.registers.length > 0) {
            printer.print(" ");
            printer.print("{");
            for (int idx = 0; idx < instruction.registers.length; idx++) {
                if (idx > 0) {
                    printer.print(", ");
                }
                printer.print("v");
                printer.print(Integer.toString(instruction.registers[idx]));
            }
            printer.print("}");
        } else {
            printer.print("{}");
        }

        printer.print(", ");

        TypeID typeID = instruction.getTypeID(dexFile);
        printer.print(typeID.getType(dexFile));

        printer.print(" // type@");
        printer.print(Primitives.asHexValue(instruction.getTypeIndex(), 4));
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
        printer.print(instruction.getMnemonic());

        if (instruction.getOpcode() == DexOpCode.NOP) {
            printer.print(" // spacer");
        }

        if (instruction.registers.length > 0) {
            printer.print(" ");
            for (int idx = 0; idx < instruction.registers.length; idx++) {
                if (idx > 0) {
                    printer.print(", ");
                }
                printer.print("v");
                printer.print(Integer.toString(instruction.registers[idx]));
            }
        }
    }
}

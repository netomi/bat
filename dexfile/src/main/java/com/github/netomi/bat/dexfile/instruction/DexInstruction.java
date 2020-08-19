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
package com.github.netomi.bat.dexfile.instruction;

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.Code;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.EncodedMethod;
import com.github.netomi.bat.util.Primitives;
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;

/**
 * @author Thomas Neidhart
 */
public class DexInstruction
{
    private static final int[] EMPTY_REGISTERS = new int[0];

    protected final DexOpCode opcode;
    public          int[]     registers;

    public static DexInstruction create(short[] instructions, int offset) {
        byte opcode = (byte) (instructions[offset]         & 0xff);
        byte ident  = (byte) ((instructions[offset] >>> 8) & 0xff);

        DexOpCode opCode = DexOpCode.get(opcode);

        if (opCode != null) {
            DexInstruction instruction = opCode.createInstruction(ident);
            instruction.read(instructions, offset);
            return instruction;
        } else {
            throw new IllegalArgumentException("unknown opcode " + Primitives.toHexString(opcode));
        }
    }

    public DexInstruction(DexOpCode opcode) {
        this.opcode    = opcode;
        this.registers = EMPTY_REGISTERS;
    }

    public DexOpCode getOpcode() {
        return opcode;
    }

    public int getLength() {
        return opcode.getLength();
    }

    public String getMnemonic() {
        return opcode.getMnemonic();
    }

    public void read(short[] instructions, int offset) {
        switch (opcode.getFormat()) {
            case FORMAT_00x:
            case FORMAT_10x:
            case FORMAT_10t:
            case FORMAT_20t:
            case FORMAT_30t:
                registers = EMPTY_REGISTERS;
                break;

            case FORMAT_11n:
                registers = new int[] {
                    instructions[offset] >>> 8  & 0xf,
                };
                break;

            case FORMAT_12x:
            case FORMAT_22c:
            case FORMAT_22s:
            case FORMAT_22t:
                registers = new int[] {
                    instructions[offset] >>> 8  & 0xf,
                    instructions[offset] >>> 12 & 0xf,
                };
                break;

            case FORMAT_11x:
            case FORMAT_21c:
            case FORMAT_21t:
            case FORMAT_21s:
            case FORMAT_21h:
            case FORMAT_31c:
            case FORMAT_31i:
            case FORMAT_31t:
            case FORMAT_51l:
                registers = new int[] {
                    instructions[offset] >>> 8  & 0xff,
                };
                break;

            case FORMAT_22b:
                registers = new int[] {
                    instructions[offset] >>> 8 & 0xff,
                    instructions[offset + 1]   & 0xff
                };
                break;

            case FORMAT_22x:
                registers = new int[] {
                    instructions[offset] >>> 8 & 0xff,
                    instructions[offset + 1]   & 0xffff
                };
                break;

            case FORMAT_23x:
                registers = new int[] {
                    instructions[offset] >>> 8      & 0xff,
                    instructions[offset + 1]        & 0xff,
                    instructions[offset + 1] >>> 8  & 0xff
                };
                break;

            case FORMAT_32x:
                registers = new int[] {
                        instructions[offset + 1] & 0xffff,
                        instructions[offset + 2] & 0xffff
                };
                break;

            case FORMAT_35c:
                {
                    int registerCount = (instructions[offset] >>> 12) & 0xf;
                    switch (registerCount) {
                        case 0:
                            registers = EMPTY_REGISTERS;
                            break;

                        case 1:
                            registers = new int[] {
                                instructions[offset + 2] & 0xf
                            };
                            break;

                        case 2:
                            registers = new int[] {
                                 instructions[offset + 2] & 0xf,
                                (instructions[offset + 2] >>> 4) & 0xf
                            };
                            break;

                        case 3:
                            registers = new int[] {
                                 instructions[offset + 2]         & 0xf,
                                (instructions[offset + 2] >>> 4) & 0xf,
                                (instructions[offset + 2] >>> 8) & 0xf,
                            };
                            break;

                        case 4:
                            registers = new int[] {
                                 instructions[offset + 2]          & 0xf,
                                (instructions[offset + 2] >>>  4) & 0xf,
                                (instructions[offset + 2] >>>  8) & 0xf,
                                (instructions[offset + 2] >>> 12) & 0xf
                            };
                            break;

                        case 5:
                            registers = new int[] {
                                 instructions[offset + 2]          & 0xf,
                                (instructions[offset + 2] >>>  4) & 0xf,
                                (instructions[offset + 2] >>>  8) & 0xf,
                                (instructions[offset + 2] >>> 12) & 0xf,
                                (instructions[offset]     >>>  8) & 0xf
                            };
                            break;

                        default:
                            throw new IllegalArgumentException("unexpected value when reading instruction with opcode " + opcode);
                    }
                }
                break;

            case FORMAT_3rc:
            case FORMAT_4rcc:
                {
                    int registerCount = (instructions[offset] >>> 8) & 0xff;
                    int register      = instructions[offset + 2] & 0xffff;

                    registers = new int[registerCount];

                    for (int i = 0; i < registerCount; i++) {
                        registers[i] = register++;
                    }
                }
                break;

            case FORMAT_45cc:
            {
                int registerCount = (instructions[offset] >>> 12) & 0xf;
                switch (registerCount) {
                    case 1:
                        registers = new int[] {
                            instructions[offset + 2] & 0xf
                        };
                        break;

                    case 2:
                        registers = new int[] {
                             instructions[offset + 2] & 0xf,
                            (instructions[offset + 2] >>> 4) & 0xf
                        };
                        break;

                    case 3:
                        registers = new int[] {
                             instructions[offset + 2]         & 0xf,
                            (instructions[offset + 2] >>> 4) & 0xf,
                            (instructions[offset + 2] >>> 8) & 0xf,
                        };
                        break;

                    case 4:
                        registers = new int[] {
                             instructions[offset + 2]          & 0xf,
                            (instructions[offset + 2] >>>  4) & 0xf,
                            (instructions[offset + 2] >>>  8) & 0xf,
                            (instructions[offset + 2] >>> 12) & 0xf
                        };
                        break;

                    case 5:
                        registers = new int[] {
                             instructions[offset + 2]          & 0xf,
                            (instructions[offset + 2] >>>  4) & 0xf,
                            (instructions[offset + 2] >>>  8) & 0xf,
                            (instructions[offset + 2] >>> 12) & 0xf,
                            (instructions[offset]     >>>  8) & 0xf
                        };
                        break;

                    default:
                        throw new IllegalArgumentException("unexpected value when reading instruction with opcode " + opcode);
                }
            }
            break;
        }
    }

    public void accept(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, InstructionVisitor visitor) {
        // hack till all instructions are correctly added.
        visitor.visitAnyInstruction(dexFile, classDef, method, code, offset, this);
    }

    public String toString() {
        return opcode.getMnemonic();
    }
}

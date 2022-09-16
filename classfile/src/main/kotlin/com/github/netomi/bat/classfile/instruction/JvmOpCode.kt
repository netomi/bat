/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
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

package com.github.netomi.bat.classfile.instruction

import com.github.netomi.bat.util.toHexStringWithPrefix

enum class JvmOpCode constructor(
            val value:    Int,
            val mnemonic: String,
            val length:   Int = 1,
    private val supplier: InstructionSupplier? = null) {

    // array instructions

    AALOAD         (0x32, "aaload",  1, { ArrayInstruction.create(it) }),
    AASTORE        (0x53, "aastore", 1, { ArrayInstruction.create(it) }),

    BALOAD         (0x33, "baload",  1, { ArrayInstruction.create(it) }),
    BASTORE        (0x54, "bastore", 1, { ArrayInstruction.create(it) }),

    CALOAD         (0x34, "caload",  1, { ArrayInstruction.create(it) }),
    CASTORE        (0x55, "castore", 1, { ArrayInstruction.create(it) }),

    DALOAD         (0x31, "daload",  1, { ArrayInstruction.create(it) }),
    DASTORE        (0x52, "dastore", 1, { ArrayInstruction.create(it) }),

    FALOAD         (0x30, "faload",  1, { ArrayInstruction.create(it) }),
    FASTORE        (0x51, "fastore", 1, { ArrayInstruction.create(it) }),

    IALOAD         (0x2e, "iaload",  1, { ArrayInstruction.create(it) }),
    IASTORE        (0x4f, "iastore", 1, { ArrayInstruction.create(it) }),

    LALOAD         (0x2f, "laload",  1, { ArrayInstruction.create(it) }),
    LASTORE        (0x50, "lastore", 1, { ArrayInstruction.create(it) }),

    SALOAD         (0x35, "saload",  1, { ArrayInstruction.create(it) }),
    SASTORE        (0x56, "sastore", 1, { ArrayInstruction.create(it) }),

    // arithmetic instructions

    DADD           (0x63, "dadd",  1, { ArithmeticInstruction.create(it) }),
    DDIV           (0x6f, "ddiv",  1, { ArithmeticInstruction.create(it) }),
    DMUL           (0x6b, "dmul",  1, { ArithmeticInstruction.create(it) }),
    DNEG           (0x77, "dneg",  1, { ArithmeticInstruction.create(it) }),
    DREM           (0x73, "drem",  1, { ArithmeticInstruction.create(it) }),
    DSUB           (0x67, "dsub",  1, { ArithmeticInstruction.create(it) }),

    FADD           (0x62, "fadd",  1, { ArithmeticInstruction.create(it) }),
    FDIV           (0x6e, "fdiv",  1, { ArithmeticInstruction.create(it) }),
    FMUL           (0x6a, "fmul",  1, { ArithmeticInstruction.create(it) }),
    FNEG           (0x76, "fneg",  1, { ArithmeticInstruction.create(it) }),
    FREM           (0x72, "frem",  1, { ArithmeticInstruction.create(it) }),
    FSUB           (0x66, "fsub",  1, { ArithmeticInstruction.create(it) }),

    IADD           (0x60, "iadd",  1, { ArithmeticInstruction.create(it) }),
    IAND           (0x7e, "iand",  1, { ArithmeticInstruction.create(it) }),
    IDIV           (0x6c, "idiv",  1, { ArithmeticInstruction.create(it) }),
    IMUL           (0x68, "imul",  1, { ArithmeticInstruction.create(it) }),
    INEG           (0x74, "ineg",  1, { ArithmeticInstruction.create(it) }),
    IOR            (0x80, "ior",   1, { ArithmeticInstruction.create(it) }),
    IREM           (0x70, "irem",  1, { ArithmeticInstruction.create(it) }),
    ISHL           (0x78, "ishl",  1, { ArithmeticInstruction.create(it) }),
    ISHR           (0x7a, "ishr",  1, { ArithmeticInstruction.create(it) }),
    ISUB           (0x64, "isub",  1, { ArithmeticInstruction.create(it) }),
    IUSHR          (0x7c, "iushr", 1, { ArithmeticInstruction.create(it) }),
    IXOR           (0x82, "ixor",  1, { ArithmeticInstruction.create(it) }),

    LADD           (0x61, "ladd",  1, { ArithmeticInstruction.create(it) }),
    LMUL           (0x69, "lmul",  1, { ArithmeticInstruction.create(it) }),
    LDIV           (0x6d, "ldiv",  1, { ArithmeticInstruction.create(it) }),
    LNEG           (0x75, "lneg",  1, { ArithmeticInstruction.create(it) }),
    LAND           (0x7f, "land",  1, { ArithmeticInstruction.create(it) }),
    LOR            (0x81, "lor",   1, { ArithmeticInstruction.create(it) }),
    LREM           (0x71, "lrem",  1, { ArithmeticInstruction.create(it) }),
    LSHL           (0x79, "lshl",  1, { ArithmeticInstruction.create(it) }),
    LSHR           (0x7b, "lshr",  1, { ArithmeticInstruction.create(it) }),
    LSUB           (0x65, "lsub",  1, { ArithmeticInstruction.create(it) }),
    LUSHR          (0x7d, "lushr", 1, { ArithmeticInstruction.create(it) }),
    LXOR           (0x83, "lxor",  1, { ArithmeticInstruction.create(it) }),

    // branch instructions

    IF_ACMPEQ      (0xa5, "if_acmpeq", 3, { BranchInstruction.create(it) }),
    IF_ACMPNE      (0xa6, "if_acmpne", 3, { BranchInstruction.create(it) }),
    IF_ICMPEQ      (0x9f, "if_icmpeq", 3, { BranchInstruction.create(it) }),
    IF_ICMPNE      (0xa0, "if_icmpne", 3, { BranchInstruction.create(it) }),
    IF_ICMPLT      (0xa1, "if_icmplt", 3, { BranchInstruction.create(it) }),
    IF_ICMPGE      (0xa2, "if_icmpge", 3, { BranchInstruction.create(it) }),
    IF_ICMPGT      (0xa3, "if_icmpgt", 3, { BranchInstruction.create(it) }),
    IF_ICMPLE      (0xa4, "if_icmple", 3, { BranchInstruction.create(it) }),

    IFEQ           (0x99, "ifeq",      3, { BranchInstruction.create(it) }),
    IFNE           (0x9a, "ifne",      3, { BranchInstruction.create(it) }),
    IFLT           (0x9b, "iflt",      3, { BranchInstruction.create(it) }),
    IFGE           (0x9c, "ifge",      3, { BranchInstruction.create(it) }),
    IFGT           (0x9d, "ifgt",      3, { BranchInstruction.create(it) }),
    IFLE           (0x9e, "ifle",      3, { BranchInstruction.create(it) }),

    IFNONNULL      (0xc7, "ifnonnull", 3, { BranchInstruction.create(it) }),
    IFNULL         (0xc6, "ifnull",    3, { BranchInstruction.create(it) }),

    GOTO           (0xa7, "goto",      3, { BranchInstruction.create(it) }),
    GOTO_W         (0xc8, "goto_w",    5, { BranchInstruction.create(it) }),

    JSR            (0x8a, "jsr",       3, { BranchInstruction.create(it) }),
    JSR_W          (0xc9, "jsr_w",     5, { BranchInstruction.create(it) }),

    // compare instructions

    DCMPG          (0x98, "dcmpg", 1, { CompareInstruction.create(it) }),
    DCMPL          (0x97, "dcmpl", 1, { CompareInstruction.create(it) }),
    FCMPG          (0x96, "fcmpg", 1, { CompareInstruction.create(it) }),
    FCMPL          (0x95, "fcmpl", 1, { CompareInstruction.create(it) }),
    LCMP           (0x94, "lcmp",  1, { CompareInstruction.create(it) }),

    // conversion instructions

    D2F            (0x90, "d2f", 1, { ConversionInstruction.create(it) }),
    D2I            (0x8e, "d2i", 1, { ConversionInstruction.create(it) }),
    D2L            (0x8f, "d2l", 1, { ConversionInstruction.create(it) }),

    F2D            (0x8d, "f2d", 1, { ConversionInstruction.create(it) }),
    F2I            (0x8b, "f2i", 1, { ConversionInstruction.create(it) }),
    F2L            (0x8c, "f2l", 1, { ConversionInstruction.create(it) }),

    I2B            (0x91, "i2b", 1, { ConversionInstruction.create(it) }),
    I2C            (0x92, "i2c", 1, { ConversionInstruction.create(it) }),
    I2D            (0x87, "i2d", 1, { ConversionInstruction.create(it) }),
    I2F            (0x86, "i2f", 1, { ConversionInstruction.create(it) }),
    I2L            (0x85, "i2l", 1, { ConversionInstruction.create(it) }),
    I2S            (0x93, "i2s", 1, { ConversionInstruction.create(it) }),

    L2D            (0x8a, "l2d", 1, { ConversionInstruction.create(it) }),
    L2F            (0x89, "l2f", 1, { ConversionInstruction.create(it) }),
    L2I            (0x88, "l2i", 1, { ConversionInstruction.create(it) }),

    // literal instructions

    BIPUSH         (0x10, "bipush",    2, { LiteralInstruction.create(it) }),

    DCONST_0       (0x0e, "dconst_0",  1, { LiteralInstruction.create(it) }),
    DCONST_1       (0x0f, "dconst_1",  1, { LiteralInstruction.create(it) }),

    FCONST_0       (0x0b, "fconst_0",  1, { LiteralInstruction.create(it) }),
    FCONST_1       (0x0c, "fconst_1",  1, { LiteralInstruction.create(it) }),
    FCONST_2       (0x0d, "fconst_2",  1, { LiteralInstruction.create(it) }),

    ICONST_M1      (0x02, "iconst_m1", 1, { LiteralInstruction.create(it) }),
    ICONST_0       (0x03, "iconst_0",  1, { LiteralInstruction.create(it) }),
    ICONST_1       (0x04, "iconst_1",  1, { LiteralInstruction.create(it) }),
    ICONST_2       (0x05, "iconst_2",  1, { LiteralInstruction.create(it) }),
    ICONST_3       (0x06, "iconst_3",  1, { LiteralInstruction.create(it) }),
    ICONST_4       (0x07, "iconst_4",  1, { LiteralInstruction.create(it) }),
    ICONST_5       (0x08, "iconst_5",  1, { LiteralInstruction.create(it) }),

    LCONST_0       (0x09, "lconst_0",  1, { LiteralInstruction.create(it) }),
    LCONST_1       (0x0a, "lconst_1",  1, { LiteralInstruction.create(it) }),

    SIPUSH         (0x11, "sipush",    3, { LiteralInstruction.create(it) }),

    // return instructions

    ARETURN        (0xb0, "areturn", 1, { ReturnInstruction.create(it) }),
    DRETURN        (0xaf, "dreturn", 1, { ReturnInstruction.create(it) }),
    FRETURN        (0xae, "freturn", 1, { ReturnInstruction.create(it) }),
    IRETURN        (0xac, "ireturn", 1, { ReturnInstruction.create(it) }),
    LRETURN        (0xad, "lreturn", 1, { ReturnInstruction.create(it) }),
    RET            (0xa9, "ret",     2, { ReturnInstruction.create(it) }),
    RETURN         (0xb1, "return",  1, { ReturnInstruction.create(it) }),

    // stack instructions

    DUP            (0x59, "dup",     1, { StackInstruction.create(it) }),
    DUP_X1         (0x5a, "dup_x1",  1, { StackInstruction.create(it) }),
    DUP_X2         (0x5b, "dup_x2",  1, { StackInstruction.create(it) }),
    DUP2           (0x5c, "dup2",    1, { StackInstruction.create(it) }),
    DUP2_X1        (0x5d, "dup2_x1", 1, { StackInstruction.create(it) }),
    DUP2_X2        (0x5e, "dup2_x2", 1, { StackInstruction.create(it) }),
    POP            (0x57, "pop",     1, { StackInstruction.create(it) }),
    POP2           (0x58, "pop2",    1, { StackInstruction.create(it) }),
    SWAP           (0x5f, "swap",    1, { StackInstruction.create(it) }),

    // switch instructions

    LOOKUPSWITCH   (0xab, "lookupswitch", 0, { LookupSwitchInstruction.create(it) }),
    TABLESWITCH    (0xaa, "tableswitch",  0, { TableSwitchInstruction.create(it) }),

    // variable instructions

    ALOAD          (0x19, "aload",    2, { VariableInstruction.create(it) }),
    ALOAD_0        (0x2a, "aload_0",  1, { VariableInstruction.create(it) }),
    ALOAD_1        (0x2b, "aload_1",  1, { VariableInstruction.create(it) }),
    ALOAD_2        (0x2c, "aload_2",  1, { VariableInstruction.create(it) }),
    ALOAD_3        (0x2d, "aload_3",  1, { VariableInstruction.create(it) }),

    ASTORE         (0x3a, "astore",   2, { VariableInstruction.create(it) }),
    ASTORE_0       (0x4b, "astore_0", 1, { VariableInstruction.create(it) }),
    ASTORE_1       (0x4c, "astore_1", 1, { VariableInstruction.create(it) }),
    ASTORE_2       (0x4d, "astore_2", 1, { VariableInstruction.create(it) }),
    ASTORE_3       (0x4e, "astore_3", 1, { VariableInstruction.create(it) }),

    DLOAD          (0x18, "dload",    2, { VariableInstruction.create(it) }),
    DLOAD_0        (0x26, "dload_0",  1, { VariableInstruction.create(it) }),
    DLOAD_1        (0x27, "dload_1",  1, { VariableInstruction.create(it) }),
    DLOAD_2        (0x28, "dload_2",  1, { VariableInstruction.create(it) }),
    DLOAD_3        (0x29, "dload_3",  1, { VariableInstruction.create(it) }),

    DSTORE         (0x39, "dstore",   2, { VariableInstruction.create(it) }),
    DSTORE_0       (0x47, "dstore_0", 1, { VariableInstruction.create(it) }),
    DSTORE_1       (0x48, "dstore_1", 1, { VariableInstruction.create(it) }),
    DSTORE_2       (0x49, "dstore_2", 1, { VariableInstruction.create(it) }),
    DSTORE_3       (0x4a, "dstore_3", 1, { VariableInstruction.create(it) }),

    FLOAD          (0x17, "fload",    2, { VariableInstruction.create(it) }),
    FLOAD_0        (0x22, "fload_0",  1, { VariableInstruction.create(it) }),
    FLOAD_1        (0x23, "fload_1",  1, { VariableInstruction.create(it) }),
    FLOAD_2        (0x24, "fload_2",  1, { VariableInstruction.create(it) }),
    FLOAD_3        (0x25, "fload_3",  1, { VariableInstruction.create(it) }),

    FSTORE         (0x38, "fstore",   2, { VariableInstruction.create(it) }),
    FSTORE_0       (0x43, "fstore_0", 1, { VariableInstruction.create(it) }),
    FSTORE_1       (0x44, "fstore_1", 1, { VariableInstruction.create(it) }),
    FSTORE_2       (0x45, "fstore_2", 1, { VariableInstruction.create(it) }),
    FSTORE_3       (0x46, "fstore_3", 1, { VariableInstruction.create(it) }),

    ILOAD          (0x15, "iload",    2, { VariableInstruction.create(it) }),
    ILOAD_0        (0x1a, "iload_0",  1, { VariableInstruction.create(it) }),
    ILOAD_1        (0x1b, "iload_1",  1, { VariableInstruction.create(it) }),
    ILOAD_2        (0x1c, "iload_2",  1, { VariableInstruction.create(it) }),
    ILOAD_3        (0x1d, "iload_3",  1, { VariableInstruction.create(it) }),

    ISTORE         (0x36, "istore",   2, { VariableInstruction.create(it) }),
    ISTORE_0       (0x3b, "istore_0", 1, { VariableInstruction.create(it) }),
    ISTORE_1       (0x3c, "istore_1", 1, { VariableInstruction.create(it) }),
    ISTORE_2       (0x3d, "istore_2", 1, { VariableInstruction.create(it) }),
    ISTORE_3       (0x3e, "istore_3", 1, { VariableInstruction.create(it) }),

    LLOAD          (0x16, "lload",    2, { VariableInstruction.create(it) }),
    LLOAD_0        (0x1e, "lload_0",  1, { VariableInstruction.create(it) }),
    LLOAD_1        (0x1f, "lload_1",  1, { VariableInstruction.create(it) }),
    LLOAD_2        (0x20, "lload_2",  1, { VariableInstruction.create(it) }),
    LLOAD_3        (0x21, "lload_3",  1, { VariableInstruction.create(it) }),

    LSTORE         (0x37, "lstore",   2, { VariableInstruction.create(it) }),
    LSTORE_0       (0x3f, "lstore_0", 1, { VariableInstruction.create(it) }),
    LSTORE_1       (0x40, "lstore_1", 1, { VariableInstruction.create(it) }),
    LSTORE_2       (0x41, "lstore_2", 1, { VariableInstruction.create(it) }),
    LSTORE_3       (0x42, "lstore_3", 1, { VariableInstruction.create(it) }),

    // field instructions

    GETFIELD       (0xb4, "getfield",  3, { FieldInstruction.create(it) }),
    GETSTATIC      (0xb2, "getstatic", 3, { FieldInstruction.create(it) }),
    PUTFIELD       (0xb5, "putfield",  3, { FieldInstruction.create(it) }),
    PUTSTATIC      (0xb3, "putstatic", 3, { FieldInstruction.create(it) }),

    // class instructions

    NEW            (0xbb, "new",        3, { ClassInstruction.create(it) }),
    CHECKCAST      (0xc0, "checkcast",  3, { ClassInstruction.create(it) }),
    INSTANCEOF     (0xc1, "instanceof", 3, { ClassInstruction.create(it) }),

    // invocation instructions

    INVOKEDYNAMIC  (0xba, "invokedynamic",   5, { InvokeDynamicInstruction.create(it) }),
    INVOKEINTERFACE(0xb9, "invokeinterface", 5, { InterfaceMethodInstruction.create(it) }),
    INVOKESPECIAL  (0xb7, "invokespecial",   3, { MethodInstruction.create(it) }),
    INVOKESTATIC   (0xb8, "invokestatic",    3, { MethodInstruction.create(it) }),
    INVOKEVIRTUAL  (0xb6, "invokevirtual",   3, { MethodInstruction.create(it) }),

    // literal constant instructions

    LDC            (0x12, "ldc",    2, { LiteralConstantInstruction.create(it) }),
    LDC_W          (0x13, "ldc_w",  3, { LiteralConstantInstruction.create(it) }),
    LDC2_W         (0x14, "ldc2_w", 3, { LiteralConstantInstruction.create(it) }),

    // uncategorized instructions

    NOP            (0x00, "nop",         1, { NopInstruction.create(it) }),
    ACONST_NULL    (0x01, "aconst_null", 1, { NullReferenceInstruction.create(it) }),
    ATHROW         (0xbf, "athrow",      1, { ExceptionInstruction.create(it) }),

    MONITORENTER   (0xc2, "monitorenter", 1, { MonitorInstruction.create(it) }),
    MONITOREXIT    (0xc3, "monitorexit",  1, { MonitorInstruction.create(it) }),

    ANEWARRAY      (0xbd, "anewarray",      3, { ArrayClassInstruction.create(it) }),
    MULTIANEWARRAY (0xc5, "multianewarray", 4, { ArrayClassInstruction.create(it) }),
    ARRAYLENGTH    (0xbe, "arraylength",    1, { ArrayInstruction.create(it) }),
    NEWARRAY       (0xbc, "newarray",       2, { ArrayPrimitiveTypeInstruction.create(it) }),

    IINC           (0x84, "iinc", 3, { LiteralVariableInstruction.create(it) }),

    WIDE           (0xc4, "wide", -1); // TODO: implement handling of wide instructions

    fun createInstruction(): JvmInstruction {
        return supplier?.create(this) ?: throw RuntimeException("failed to create instruction for opcode $this")
    }

    private fun interface InstructionSupplier {
        fun create(opCode: JvmOpCode): JvmInstruction
    }

    companion object {
        private val opcodeArray:             Array<JvmOpCode?> = arrayOfNulls(0x100)
        private val mnemonicToOpCodeMapping: MutableMap<String, JvmOpCode> = hashMapOf()

        init {
            for (opCode in values().filter { it.value <= 0xff }) {
                opcodeArray[opCode.value]                = opCode
                mnemonicToOpCodeMapping[opCode.mnemonic] = opCode
            }
        }

        operator fun get(opcode: Byte): JvmOpCode {
            return opcodeArray[opcode.toInt() and 0xff] ?: throw IllegalArgumentException("unknown opcode ${toHexStringWithPrefix(opcode)}")
        }
    }
}
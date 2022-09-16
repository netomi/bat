/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance wopCodeh the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in wropCodeing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WopCodeHOUT WARRANTIES OR CONDopCodeIONS OF ANY KIND, eopCodeher express or implied.
 *  See the License for the specific language governing permissions and
 *  limopCodeations under the License.
 */

package com.github.netomi.bat.classfile.instruction

import com.github.netomi.bat.util.toHexStringWithPrefix

enum class JvmOpCode constructor(
            val value:    Int,
            val mnemonic: String,
            val length:   Int = 1,
    private val supplier: InstructionSupplier) {

    // array instructions

    AALOAD         (0x32, "aaload",  1, { opCode, _ -> ArrayInstruction.create(opCode) }),
    AASTORE        (0x53, "aastore", 1, { opCode, _ -> ArrayInstruction.create(opCode) }),

    BALOAD         (0x33, "baload",  1, { opCode, _ -> ArrayInstruction.create(opCode) }),
    BASTORE        (0x54, "bastore", 1, { opCode, _ -> ArrayInstruction.create(opCode) }),

    CALOAD         (0x34, "caload",  1, { opCode, _ -> ArrayInstruction.create(opCode) }),
    CASTORE        (0x55, "castore", 1, { opCode, _ -> ArrayInstruction.create(opCode) }),

    DALOAD         (0x31, "daload",  1, { opCode, _ -> ArrayInstruction.create(opCode) }),
    DASTORE        (0x52, "dastore", 1, { opCode, _ -> ArrayInstruction.create(opCode) }),

    FALOAD         (0x30, "faload",  1, { opCode, _ -> ArrayInstruction.create(opCode) }),
    FASTORE        (0x51, "fastore", 1, { opCode, _ -> ArrayInstruction.create(opCode) }),

    IALOAD         (0x2e, "iaload",  1, { opCode, _ -> ArrayInstruction.create(opCode) }),
    IASTORE        (0x4f, "iastore", 1, { opCode, _ -> ArrayInstruction.create(opCode) }),

    LALOAD         (0x2f, "laload",  1, { opCode, _ -> ArrayInstruction.create(opCode) }),
    LASTORE        (0x50, "lastore", 1, { opCode, _ -> ArrayInstruction.create(opCode) }),

    SALOAD         (0x35, "saload",  1, { opCode, _ -> ArrayInstruction.create(opCode) }),
    SASTORE        (0x56, "sastore", 1, { opCode, _ -> ArrayInstruction.create(opCode) }),

    // arithmetic instructions

    DADD           (0x63, "dadd",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    DDIV           (0x6f, "ddiv",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    DMUL           (0x6b, "dmul",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    DNEG           (0x77, "dneg",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    DREM           (0x73, "drem",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    DSUB           (0x67, "dsub",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),

    FADD           (0x62, "fadd",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    FDIV           (0x6e, "fdiv",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    FMUL           (0x6a, "fmul",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    FNEG           (0x76, "fneg",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    FREM           (0x72, "frem",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    FSUB           (0x66, "fsub",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),

    IADD           (0x60, "iadd",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    IAND           (0x7e, "iand",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    IDIV           (0x6c, "idiv",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    IMUL           (0x68, "imul",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    INEG           (0x74, "ineg",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    IOR            (0x80, "ior",   1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    IREM           (0x70, "irem",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    ISHL           (0x78, "ishl",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    ISHR           (0x7a, "ishr",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    ISUB           (0x64, "isub",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    IUSHR          (0x7c, "iushr", 1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    IXOR           (0x82, "ixor",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),

    LADD           (0x61, "ladd",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    LMUL           (0x69, "lmul",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    LDIV           (0x6d, "ldiv",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    LNEG           (0x75, "lneg",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    LAND           (0x7f, "land",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    LOR            (0x81, "lor",   1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    LREM           (0x71, "lrem",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    LSHL           (0x79, "lshl",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    LSHR           (0x7b, "lshr",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    LSUB           (0x65, "lsub",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    LUSHR          (0x7d, "lushr", 1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),
    LXOR           (0x83, "lxor",  1, { opCode, _ -> ArithmeticInstruction.create(opCode) }),

    // branch instructions

    IF_ACMPEQ      (0xa5, "if_acmpeq", 3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IF_ACMPNE      (0xa6, "if_acmpne", 3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IF_ICMPEQ      (0x9f, "if_icmpeq", 3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IF_ICMPNE      (0xa0, "if_icmpne", 3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IF_ICMPLT      (0xa1, "if_icmplt", 3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IF_ICMPGE      (0xa2, "if_icmpge", 3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IF_ICMPGT      (0xa3, "if_icmpgt", 3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IF_ICMPLE      (0xa4, "if_icmple", 3, { opCode, _ -> BranchInstruction.create(opCode) }),

    IFEQ           (0x99, "ifeq",      3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IFNE           (0x9a, "ifne",      3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IFLT           (0x9b, "iflt",      3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IFGE           (0x9c, "ifge",      3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IFGT           (0x9d, "ifgt",      3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IFLE           (0x9e, "ifle",      3, { opCode, _ -> BranchInstruction.create(opCode) }),

    IFNONNULL      (0xc7, "ifnonnull", 3, { opCode, _ -> BranchInstruction.create(opCode) }),
    IFNULL         (0xc6, "ifnull",    3, { opCode, _ -> BranchInstruction.create(opCode) }),

    GOTO           (0xa7, "goto",      3, { opCode, _ -> BranchInstruction.create(opCode) }),
    GOTO_W         (0xc8, "goto_w",    5, { opCode, _ -> BranchInstruction.create(opCode) }),

    JSR            (0x8a, "jsr",       3, { opCode, _ -> BranchInstruction.create(opCode) }),
    JSR_W          (0xc9, "jsr_w",     5, { opCode, _ -> BranchInstruction.create(opCode) }),

    // compare instructions

    DCMPG          (0x98, "dcmpg", 1, { opCode, _ -> CompareInstruction.create(opCode) }),
    DCMPL          (0x97, "dcmpl", 1, { opCode, _ -> CompareInstruction.create(opCode) }),
    FCMPG          (0x96, "fcmpg", 1, { opCode, _ -> CompareInstruction.create(opCode) }),
    FCMPL          (0x95, "fcmpl", 1, { opCode, _ -> CompareInstruction.create(opCode) }),
    LCMP           (0x94, "lcmp",  1, { opCode, _ -> CompareInstruction.create(opCode) }),

    // conversion instructions

    D2F            (0x90, "d2f", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),
    D2I            (0x8e, "d2i", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),
    D2L            (0x8f, "d2l", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),

    F2D            (0x8d, "f2d", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),
    F2I            (0x8b, "f2i", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),
    F2L            (0x8c, "f2l", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),

    I2B            (0x91, "i2b", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),
    I2C            (0x92, "i2c", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),
    I2D            (0x87, "i2d", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),
    I2F            (0x86, "i2f", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),
    I2L            (0x85, "i2l", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),
    I2S            (0x93, "i2s", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),

    L2D            (0x8a, "l2d", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),
    L2F            (0x89, "l2f", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),
    L2I            (0x88, "l2i", 1, { opCode, _ -> ConversionInstruction.create(opCode) }),

    // literal instructions

    BIPUSH         (0x10, "bipush",    2, { opCode, _ -> LiteralInstruction.create(opCode) }),

    DCONST_0       (0x0e, "dconst_0",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),
    DCONST_1       (0x0f, "dconst_1",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),

    FCONST_0       (0x0b, "fconst_0",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),
    FCONST_1       (0x0c, "fconst_1",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),
    FCONST_2       (0x0d, "fconst_2",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),

    ICONST_M1      (0x02, "iconst_m1", 1, { opCode, _ -> LiteralInstruction.create(opCode) }),
    ICONST_0       (0x03, "iconst_0",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),
    ICONST_1       (0x04, "iconst_1",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),
    ICONST_2       (0x05, "iconst_2",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),
    ICONST_3       (0x06, "iconst_3",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),
    ICONST_4       (0x07, "iconst_4",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),
    ICONST_5       (0x08, "iconst_5",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),

    LCONST_0       (0x09, "lconst_0",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),
    LCONST_1       (0x0a, "lconst_1",  1, { opCode, _ -> LiteralInstruction.create(opCode) }),

    SIPUSH         (0x11, "sipush",    3, { opCode, _ -> LiteralInstruction.create(opCode) }),

    // return instructions

    ARETURN        (0xb0, "areturn", 1, { opCode, _ -> ReturnInstruction.create(opCode) }),
    DRETURN        (0xaf, "dreturn", 1, { opCode, _ -> ReturnInstruction.create(opCode) }),
    FRETURN        (0xae, "freturn", 1, { opCode, _ -> ReturnInstruction.create(opCode) }),
    IRETURN        (0xac, "ireturn", 1, { opCode, _ -> ReturnInstruction.create(opCode) }),
    LRETURN        (0xad, "lreturn", 1, { opCode, _ -> ReturnInstruction.create(opCode) }),
    RETURN         (0xb1, "return",  1, { opCode, _ -> ReturnInstruction.create(opCode) }),

    // stack instructions

    DUP            (0x59, "dup",     1, { opCode, _ -> StackInstruction.create(opCode) }),
    DUP_X1         (0x5a, "dup_x1",  1, { opCode, _ -> StackInstruction.create(opCode) }),
    DUP_X2         (0x5b, "dup_x2",  1, { opCode, _ -> StackInstruction.create(opCode) }),
    DUP2           (0x5c, "dup2",    1, { opCode, _ -> StackInstruction.create(opCode) }),
    DUP2_X1        (0x5d, "dup2_x1", 1, { opCode, _ -> StackInstruction.create(opCode) }),
    DUP2_X2        (0x5e, "dup2_x2", 1, { opCode, _ -> StackInstruction.create(opCode) }),
    POP            (0x57, "pop",     1, { opCode, _ -> StackInstruction.create(opCode) }),
    POP2           (0x58, "pop2",    1, { opCode, _ -> StackInstruction.create(opCode) }),
    SWAP           (0x5f, "swap",    1, { opCode, _ -> StackInstruction.create(opCode) }),

    // switch instructions

    LOOKUPSWITCH   (0xab, "lookupswitch", 0, { opCode, _ -> LookupSwitchInstruction.create(opCode) }),
    TABLESWITCH    (0xaa, "tableswitch",  0, { opCode, _ -> TableSwitchInstruction.create(opCode) }),

    // variable instructions

    ALOAD          (0x19, "aload",    2, { opCode, wide -> VariableInstruction.create(opCode, wide) }),
    ALOAD_0        (0x2a, "aload_0",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ALOAD_1        (0x2b, "aload_1",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ALOAD_2        (0x2c, "aload_2",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ALOAD_3        (0x2d, "aload_3",  1, { opCode, _    -> VariableInstruction.create(opCode) }),

    ASTORE         (0x3a, "astore",   2, { opCode, wide -> VariableInstruction.create(opCode, wide) }),
    ASTORE_0       (0x4b, "astore_0", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ASTORE_1       (0x4c, "astore_1", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ASTORE_2       (0x4d, "astore_2", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ASTORE_3       (0x4e, "astore_3", 1, { opCode, _    -> VariableInstruction.create(opCode) }),

    DLOAD          (0x18, "dload",    2, { opCode, wide -> VariableInstruction.create(opCode, wide) }),
    DLOAD_0        (0x26, "dload_0",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    DLOAD_1        (0x27, "dload_1",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    DLOAD_2        (0x28, "dload_2",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    DLOAD_3        (0x29, "dload_3",  1, { opCode, _    -> VariableInstruction.create(opCode) }),

    DSTORE         (0x39, "dstore",   2, { opCode, wide -> VariableInstruction.create(opCode, wide) }),
    DSTORE_0       (0x47, "dstore_0", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    DSTORE_1       (0x48, "dstore_1", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    DSTORE_2       (0x49, "dstore_2", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    DSTORE_3       (0x4a, "dstore_3", 1, { opCode, _    -> VariableInstruction.create(opCode) }),

    FLOAD          (0x17, "fload",    2, { opCode, wide -> VariableInstruction.create(opCode, wide) }),
    FLOAD_0        (0x22, "fload_0",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    FLOAD_1        (0x23, "fload_1",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    FLOAD_2        (0x24, "fload_2",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    FLOAD_3        (0x25, "fload_3",  1, { opCode, _    -> VariableInstruction.create(opCode) }),

    FSTORE         (0x38, "fstore",   2, { opCode, wide -> VariableInstruction.create(opCode, wide) }),
    FSTORE_0       (0x43, "fstore_0", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    FSTORE_1       (0x44, "fstore_1", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    FSTORE_2       (0x45, "fstore_2", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    FSTORE_3       (0x46, "fstore_3", 1, { opCode, _    -> VariableInstruction.create(opCode) }),

    ILOAD          (0x15, "iload",    2, { opCode, wide -> VariableInstruction.create(opCode, wide) }),
    ILOAD_0        (0x1a, "iload_0",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ILOAD_1        (0x1b, "iload_1",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ILOAD_2        (0x1c, "iload_2",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ILOAD_3        (0x1d, "iload_3",  1, { opCode, _    -> VariableInstruction.create(opCode) }),

    ISTORE         (0x36, "istore",   2, { opCode, wide -> VariableInstruction.create(opCode, wide) }),
    ISTORE_0       (0x3b, "istore_0", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ISTORE_1       (0x3c, "istore_1", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ISTORE_2       (0x3d, "istore_2", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    ISTORE_3       (0x3e, "istore_3", 1, { opCode, _    -> VariableInstruction.create(opCode) }),

    LLOAD          (0x16, "lload",    2, { opCode, wide -> VariableInstruction.create(opCode, wide) }),
    LLOAD_0        (0x1e, "lload_0",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    LLOAD_1        (0x1f, "lload_1",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    LLOAD_2        (0x20, "lload_2",  1, { opCode, _    -> VariableInstruction.create(opCode) }),
    LLOAD_3        (0x21, "lload_3",  1, { opCode, _    -> VariableInstruction.create(opCode) }),

    LSTORE         (0x37, "lstore",   2, { opCode, wide -> VariableInstruction.create(opCode, wide) }),
    LSTORE_0       (0x3f, "lstore_0", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    LSTORE_1       (0x40, "lstore_1", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    LSTORE_2       (0x41, "lstore_2", 1, { opCode, _    -> VariableInstruction.create(opCode) }),
    LSTORE_3       (0x42, "lstore_3", 1, { opCode, _    -> VariableInstruction.create(opCode) }),

    RET            (0xa9, "ret",      2, { opCode, wide -> VariableInstruction.create(opCode, wide) }),

    IINC           (0x84, "iinc",     3, { opCode, wide -> LiteralVariableInstruction.create(opCode, wide) }),

    // field instructions

    GETFIELD       (0xb4, "getfield",  3, { opCode, _ -> FieldInstruction.create(opCode) }),
    GETSTATIC      (0xb2, "getstatic", 3, { opCode, _ -> FieldInstruction.create(opCode) }),
    PUTFIELD       (0xb5, "putfield",  3, { opCode, _ -> FieldInstruction.create(opCode) }),
    PUTSTATIC      (0xb3, "putstatic", 3, { opCode, _ -> FieldInstruction.create(opCode) }),

    // class instructions

    NEW            (0xbb, "new",        3, { opCode, _ -> ClassInstruction.create(opCode) }),
    CHECKCAST      (0xc0, "checkcast",  3, { opCode, _ -> ClassInstruction.create(opCode) }),
    INSTANCEOF     (0xc1, "instanceof", 3, { opCode, _ -> ClassInstruction.create(opCode) }),

    // invocation instructions

    INVOKEDYNAMIC  (0xba, "invokedynamic",   5, { opCode, _ -> InvokeDynamicInstruction.create(opCode) }),
    INVOKEINTERFACE(0xb9, "invokeinterface", 5, { opCode, _ -> InterfaceMethodInstruction.create(opCode) }),
    INVOKESPECIAL  (0xb7, "invokespecial",   3, { opCode, _ -> MethodInstruction.create(opCode) }),
    INVOKESTATIC   (0xb8, "invokestatic",    3, { opCode, _ -> MethodInstruction.create(opCode) }),
    INVOKEVIRTUAL  (0xb6, "invokevirtual",   3, { opCode, _ -> MethodInstruction.create(opCode) }),

    // literal constant instructions

    LDC            (0x12, "ldc",    2, { opCode, _ -> LiteralConstantInstruction.create(opCode) }),
    LDC_W          (0x13, "ldc_w",  3, { opCode, _ -> LiteralConstantInstruction.create(opCode) }),
    LDC2_W         (0x14, "ldc2_w", 3, { opCode, _ -> LiteralConstantInstruction.create(opCode) }),

    // uncategorized instructions

    NOP            (0x00, "nop",         1, { opCode, _ -> NopInstruction.create(opCode) }),
    ACONST_NULL    (0x01, "aconst_null", 1, { opCode, _ -> NullReferenceInstruction.create(opCode) }),
    ATHROW         (0xbf, "athrow",      1, { opCode, _ -> ExceptionInstruction.create(opCode) }),

    MONITORENTER   (0xc2, "monitorenter", 1, { opCode, _ -> MonitorInstruction.create(opCode) }),
    MONITOREXIT    (0xc3, "monitorexit",  1, { opCode, _ -> MonitorInstruction.create(opCode) }),

    ANEWARRAY      (0xbd, "anewarray",      3, { opCode, _ -> ArrayClassInstruction.create(opCode) }),
    MULTIANEWARRAY (0xc5, "multianewarray", 4, { opCode, _ -> ArrayClassInstruction.create(opCode) }),
    ARRAYLENGTH    (0xbe, "arraylength",    1, { opCode, _ -> ArrayInstruction.create(opCode) }),
    NEWARRAY       (0xbc, "newarray",       2, { opCode, _ -> ArrayPrimitiveTypeInstruction.create(opCode) }),

    WIDE           (0xc4, "wide", -1, { _, _ -> error("tried to create a wide instruction")});

    fun createInstruction(wide: Boolean): JvmInstruction {
        return supplier.create(this, wide)
    }

    private fun interface InstructionSupplier {
        fun create(opCode: JvmOpCode, wide: Boolean): JvmInstruction
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
            return opcodeArray[opcode.toInt() and 0xff] ?: throw IllegalArgumentException("unknown opcode '${toHexStringWithPrefix(opcode)}'")
        }
    }
}
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

enum class JvmOpCode constructor(
        val value:    Int,
        val mnemonic: String) {

    AALOAD         (0x32, "aaload"),
    AASTORE        (0x53, "aastore"),
    ACONST_NULL    (0x1, "aconst_null"),
    ALOAD          (0x19, "aload"),
    ALOAD_0        (0x2a, "aload_0"),
    ALOAD_1        (0x2b, "aload_1"),
    ALOAD_2        (0x2c, "aload_2"),
    ALOAD_3        (0x2d, "aload_3"),
    ANEWARRAY      (0xbd, "anewarray"),
    ARETURN        (0xb0, "areturn"),
    ARRAYLENGTH    (0xbe, "arraylength"),
    ASTORE         (0x3a, "astore"),
    ASTORE_0       (0x4b, "astore_0"),
    ASTORE_1       (0x4c, "astore_1"),
    ASTORE_2       (0x4d, "astore_2"),
    ASTORE_3       (0x4e, "astore_3"),
    ATHROW         (0xbf, "athrow"),
    BALOAD         (0x33, "baload"),
    BASTORE        (0x54, "bastore"),
    BIPUSH         (0x10, "bipush"),
    CALOAD         (0x34, "caload"),
    CASTORE        (0x55, "castore"),
    CHECKCAST      (0xc0, "checkcast"),
    D2F            (0x90, "d2f"),
    D2I            (0x8e, "d2i"),
    D2L            (0x8f, "d2l"),
    DADD           (0x63, "dadd"),
    DALOAD         (0x31, "daload"),
    DASTORE        (0x52, "dastore"),
    DCMPG          (0x98, "dcmpg"),
    DCMPL          (0x97, "dcmpl"),
    DCONST_0       (0xe, "dconst_0"),
    DCONST_1       (0xf, "dconst_1"),
    DDIV           (0x6f, "ddiv"),
    DLOAD          (0x18, "dload"),
    DLOAD_0        (0x26, "dload_0"),
    DLOAD_1        (0x27, "dload_1"),
    DLOAD_2        (0x28, "dload_2"),
    DLOAD_3        (0x29, "dload_3"),
    DMUL           (0x6b, "dmul"),
    DNEG           (0x77, "dneg"),
    DREM           (0x73, "drem"),
    DRETURN        (0xaf, "dreturn"),
    DSTORE         (0x39, "dstore"),
    DSTORE_0       (0x47, "dstore_0"),
    DSTORE_1       (0x48, "dstore_1"),
    DSTORE_2       (0x49, "dstore_2"),
    DSTORE_3       (0x4a, "dstore_3"),
    DSUB           (0x67, "dsub"),
    DUP            (0x59, "dup"),
    DUP_X1         (0x5a, "dup_x1"),
    DUP_X2         (0x5b, "dup_x2"),
    DUP2           (0x5c, "dup2"),
    DUP2_X1        (0x5d, "dup2_x1"),
    DUP2_X2        (0x5e, "dup2_x2"),
    F2D            (0x8d, "f2d"),
    F2I            (0x8b, "f2i"),
    F2L            (0x8c, "f2l"),
    FADD           (0x62, "fadd"),
    FALOAD         (0x30, "faload"),
    FASTORE        (0x51, "fastore"),
    FCMPG          (0x96, "fcmpg"),
    FCMPL          (0x95, "fcmpl"),
    FCONST_0       (0xb, "fconst_0"),
    FCONST_1       (0xc, "fconst_1"),
    FCONST_2       (0xd, "fconst_2"),
    FDIV           (0x6e, "fdiv"),
    FLOAD          (0x17, "fload"),
    FLOAD_0        (0x22, "fload_0"),
    FLOAD_1        (0x23, "fload_1"),
    FLOAD_2        (0x24, "fload_2"),
    FLOAD_3        (0x25, "fload_3"),
    FMUL           (0x6a, "fmul"),
    FNEG           (0x76, "fneg"),
    FREM           (0x72, "frem"),
    FRETURN        (0xae, "freturn"),
    FSTORE         (0x38, "fstore"),
    FSTORE_0       (0x43, "fstore_0"),
    FSTORE_1       (0x44, "fstore_1"),
    FSTORE_2       (0x45, "fstore_2"),
    FSTORE_3       (0x46, "fstore_3"),
    FSUB           (0x66, "fsub"),
    GETFIELD       (0xb4, "getfield"),
    GETSTATIC      (0xb2, "getstatic"),
    GOTO           (0xa7, "goto"),
    GOTO_W         (0xc8, "goto_w"),
    I2B            (0x91, "i2b"),
    I2C            (0x92, "i2c"),
    I2D            (0x87, "i2d"),
    I2F            (0x86, "i2f"),
    I2L            (0x85, "i2l"),
    I2S            (0x93, "i2s"),
    IADD           (0x60, "iadd"),
    IALOAD         (0x2e, "iaload"),
    IAND           (0x7e, "iand"),
    IASTORE        (0x4f, "iastore"),
    ICONST_M1      (0x2, "iconst_m1"),
    ICONST_0       (0x3, "iconst_0"),
    ICONST_1       (0x4, "iconst_1"),
    ICONST_2       (0x5, "iconst_2"),
    ICONST_3       (0x6, "iconst_3"),
    ICONST_4       (0x7, "iconst_4"),
    ICONST_5       (0x8, "iconst_5"),
    IDIV           (0x6d, "idiv"),
    IF_ACMPEQ      (0xa5, "if_acmpeq"),
    IF_ACMPNE      (0xa6, "if_acmpne"),
    IF_ICMPEQ      (0x9f, "if_icmpeq"),
    IF_ICMPNE      (0xa0, "if_icmpne"),
    IF_ICMPLT      (0xa1, "if_icmplt"),
    IF_ICMPGE      (0xa2, "if_icmpge"),
    IF_ICMPGT      (0xa3, "if_icmpgt"),
    IF_ICMPLE      (0xa4, "if_icmple"),
    IFEQ           (0x99, "ifeq"),
    IFNE           (0x9a, "ifne"),
    IFLT           (0x9b, "iflt"),
    IFGE           (0x9c, "ifge"),
    IFGT           (0x9d, "ifgt"),
    IFLE           (0x9e, "ifle"),
    IFNONNULL      (0xc7, "ifnonnull"),
    IFNULL         (0xc6, "ifnull"),
    IINC           (0x84, "iinc"),
    ILOAD          (0x15, "iload"),
    ILOAD_0        (0x1a, "iload_0"),
    ILOAD_1        (0x1b, "iload_1"),
    ILOAD_2        (0x1c, "iload_2"),
    ILOAD_3        (0x1d, "iload_3"),
    IMUL           (0x68, "imul"),
    INEG           (0x74, "ineg"),
    INSTANCEOF     (0xc1, "instanceof"),
    INVOKEDYNAMIC  (0xba, "invokedynamic"),
    INVOKEINTERFACE(0xb9, "invokeinterface"),
    INVOKESPECIAL  (0xb7, "invokespecial"),
    INVOKESTATIC   (0xb8, "invokestatic"),
    INVOKEVIRTUAL  (0xb6, "invokevirtual"),
    IOR            (0x80, "ior"),
    IREM           (0x70, "irem"),
    IRETURN        (0xac, "ireturn"),
    ISHL           (0x78, "ishl"),
    ISHR           (0x7a, "ishr"),
    ISTORE         (0x36, "istore"),
    ISTORE_0       (0x3b, "istore_0"),
    ISTORE_1       (0x3c, "istore_1"),
    ISTORE_2       (0x3d, "istore_2"),
    ISTORE_3       (0x3e, "istore_3"),
    ISUB           (0x64, "isub"),
    IUSHR          (0x7c, "iushr"),
    IXOR           (0x82, "ixor"),
    JSR            (0x8a, "jsr"),
    JSR_W          (0xc9, "jsr_w"),
    L2D            (0x8a, "l2d"),
    L2F            (0x89, "l2f"),
    L2I            (0x88, "l2i"),
    LADD           (0x61, "ladd"),
    LALOAD         (0x2f, "laload"),
    LAND           (0x7f, "land"),
    LASTORE        (0x50, "lastore"),
    LCMP           (0x94, "lcmp"),
    LCONST_0       (0x9, "lconst_0"),
    LCONST_1       (0xa, "lconst_1"),
    LDC            (0x12, "ldc"),
    LDC_W          (0x13, "ldc_w"),
    LDC2_W         (0x14, "ldc2_w"),
    LDIV           (0x6d, "ldiv"),
    LLOAD          (0x16, "lload"),
    LLOAD_0        (0x1e, "lload_0"),
    LLOAD_1        (0x1f, "lload_1"),
    LLOAD_2        (0x20, "lload_2"),
    LLOAD_3        (0x21, "lload_3"),
    LMUL           (0x69, "lmul"),
    LNEG           (0x75, "lneg"),
    LOOKUPSWITCH   (0xab, "lookupswitch"),
    LOR            (0x81, "lor"),
    LREM           (0x71, "lrem"),
    LRETURN        (0xad, "lreturn"),
    LSHL           (0x79, "lshl"),
    LSHR           (0x7b, "lshr"),
    LSTORE         (0x37, "lstore"),
    LSTORE_0       (0x3f, "lstore_0"),
    LSTORE_1       (0x40, "lstore_1"),
    LSTORE_2       (0x41, "lstore_2"),
    LSTORE_3       (0x42, "lstore_3"),
    LSUB           (0x65, "lsub"),
    LUSHR          (0x7d, "lushr"),
    LXOR           (0x83, "lxor"),
    MONITORENTER   (0xc2, "monitorenter"),
    MONITOREXIT    (0xc3, "monitorexit"),
    MULTINEWARRAY  (0xc5, "multinewarray"),
    NEW            (0xbb, "new"),
    NEWARRAY       (0xbc, "newarray"),
    NOP            (0x0, "nop"),
    POP            (0x57, "pop"),
    POP2           (0x58, "pop2"),
    PUTFIELD       (0xb5, "putfield"),
    PUTSTATIC      (0xb3, "putstatic"),
    RET            (0xa9, "ret"),
    RETURN         (0xb1, "return"),
    SALOAD         (0x35, "saload"),
    SASTORE        (0x56, "sastore"),
    SIPUSH         (0x11, "sipush"),
    SWAP           (0x5f, "swap"),
    TABLESWITCH    (0xaa, "tableswitch"),
    WIDE           (0xc4, "wide")
}
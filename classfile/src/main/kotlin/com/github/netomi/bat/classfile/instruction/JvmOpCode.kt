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
    private val supplier: InstructionSupplier? = InstructionSupplier { opCode -> SimpleInstruction.create(opCode) } ) {

    AALOAD         (0x32, "aaload",  1),
    AASTORE        (0x53, "aastore", 1),
    ACONST_NULL    (0x01, "aconst_null", 1),

    ALOAD          (0x19, "aload",   2, VariableInstruction::create),
    ALOAD_0        (0x2a, "aload_0", 1, VariableInstruction::create),
    ALOAD_1        (0x2b, "aload_1", 1, VariableInstruction::create),
    ALOAD_2        (0x2c, "aload_2", 1, VariableInstruction::create),
    ALOAD_3        (0x2d, "aload_3", 1, VariableInstruction::create),

    ANEWARRAY      (0xbd, "anewarray", 3),
    ARETURN        (0xb0, "areturn", 1),
    ARRAYLENGTH    (0xbe, "arraylength", 1),

    ASTORE         (0x3a, "astore",   2, VariableInstruction::create),
    ASTORE_0       (0x4b, "astore_0", 1, VariableInstruction::create),
    ASTORE_1       (0x4c, "astore_1", 1, VariableInstruction::create),
    ASTORE_2       (0x4d, "astore_2", 1, VariableInstruction::create),
    ASTORE_3       (0x4e, "astore_3", 1, VariableInstruction::create),

    ATHROW         (0xbf, "athrow", 1),
    BALOAD         (0x33, "baload", 1),
    BASTORE        (0x54, "bastore", 1),
    BIPUSH         (0x10, "bipush", 2),
    CALOAD         (0x34, "caload", 1),
    CASTORE        (0x55, "castore", 1),
    CHECKCAST      (0xc0, "checkcast", 3),
    D2F            (0x90, "d2f", 1),
    D2I            (0x8e, "d2i", 1),
    D2L            (0x8f, "d2l", 1),
    DADD           (0x63, "dadd",1 ),
    DALOAD         (0x31, "daload", 1),
    DASTORE        (0x52, "dastore", 1),
    DCMPG          (0x98, "dcmpg", 1),
    DCMPL          (0x97, "dcmpl", 1),
    DCONST_0       (0x0e, "dconst_0", 1),
    DCONST_1       (0x0f, "dconst_1", 1),
    DDIV           (0x6f, "ddiv", 1),

    DLOAD          (0x18, "dload",   2, VariableInstruction::create),
    DLOAD_0        (0x26, "dload_0", 1, VariableInstruction::create),
    DLOAD_1        (0x27, "dload_1", 1, VariableInstruction::create),
    DLOAD_2        (0x28, "dload_2", 1, VariableInstruction::create),
    DLOAD_3        (0x29, "dload_3", 1, VariableInstruction::create),

    DMUL           (0x6b, "dmul", 1),
    DNEG           (0x77, "dneg", 1),
    DREM           (0x73, "drem", 1),
    DRETURN        (0xaf, "dreturn", 1),

    DSTORE         (0x39, "dstore",   2, VariableInstruction::create),
    DSTORE_0       (0x47, "dstore_0", 1, VariableInstruction::create),
    DSTORE_1       (0x48, "dstore_1", 1, VariableInstruction::create),
    DSTORE_2       (0x49, "dstore_2", 1, VariableInstruction::create),
    DSTORE_3       (0x4a, "dstore_3", 1, VariableInstruction::create),

    DSUB           (0x67, "dsub", 1),
    DUP            (0x59, "dup", 1),
    DUP_X1         (0x5a, "dup_x1", 1),
    DUP_X2         (0x5b, "dup_x2", 1),
    DUP2           (0x5c, "dup2", 1),
    DUP2_X1        (0x5d, "dup2_x1", 1),
    DUP2_X2        (0x5e, "dup2_x2", 1),
    F2D            (0x8d, "f2d", 1),
    F2I            (0x8b, "f2i", 1),
    F2L            (0x8c, "f2l", 1),
    FADD           (0x62, "fadd", 1),
    FALOAD         (0x30, "faload", 1),
    FASTORE        (0x51, "fastore", 1),
    FCMPG          (0x96, "fcmpg", 1),
    FCMPL          (0x95, "fcmpl", 1),
    FCONST_0       (0x0b, "fconst_0", 1),
    FCONST_1       (0x0c, "fconst_1", 1),
    FCONST_2       (0x0d, "fconst_2", 1),
    FDIV           (0x6e, "fdiv", 1),

    FLOAD          (0x17, "fload",   2, VariableInstruction::create),
    FLOAD_0        (0x22, "fload_0", 1, VariableInstruction::create),
    FLOAD_1        (0x23, "fload_1", 1, VariableInstruction::create),
    FLOAD_2        (0x24, "fload_2", 1, VariableInstruction::create),
    FLOAD_3        (0x25, "fload_3", 1, VariableInstruction::create),

    FMUL           (0x6a, "fmul", 1),
    FNEG           (0x76, "fneg", 1),
    FREM           (0x72, "frem", 1),
    FRETURN        (0xae, "freturn", 1),

    FSTORE         (0x38, "fstore",   2, VariableInstruction::create),
    FSTORE_0       (0x43, "fstore_0", 1, VariableInstruction::create),
    FSTORE_1       (0x44, "fstore_1", 1, VariableInstruction::create),
    FSTORE_2       (0x45, "fstore_2", 1, VariableInstruction::create),
    FSTORE_3       (0x46, "fstore_3", 1, VariableInstruction::create),

    FSUB           (0x66, "fsub", 1),
    GETFIELD       (0xb4, "getfield", 3),
    GETSTATIC      (0xb2, "getstatic", 3),
    GOTO           (0xa7, "goto", 3),
    GOTO_W         (0xc8, "goto_w", 5),
    I2B            (0x91, "i2b", 1),
    I2C            (0x92, "i2c", 1),
    I2D            (0x87, "i2d", 1),
    I2F            (0x86, "i2f", 1),
    I2L            (0x85, "i2l", 1),
    I2S            (0x93, "i2s", 1),
    IADD           (0x60, "iadd", 1),
    IALOAD         (0x2e, "iaload", 1),
    IAND           (0x7e, "iand", 1),
    IASTORE        (0x4f, "iastore", 1),
    ICONST_M1      (0x02, "iconst_m1", 1),
    ICONST_0       (0x03, "iconst_0", 1),
    ICONST_1       (0x04, "iconst_1", 1),
    ICONST_2       (0x05, "iconst_2", 1),
    ICONST_3       (0x06, "iconst_3", 1),
    ICONST_4       (0x07, "iconst_4", 1),
    ICONST_5       (0x08, "iconst_5", 1),
    IDIV           (0x6d, "idiv", 1),
    IF_ACMPEQ      (0xa5, "if_acmpeq", 3),
    IF_ACMPNE      (0xa6, "if_acmpne", 3),
    IF_ICMPEQ      (0x9f, "if_icmpeq", 3),
    IF_ICMPNE      (0xa0, "if_icmpne", 3),
    IF_ICMPLT      (0xa1, "if_icmplt", 3),
    IF_ICMPGE      (0xa2, "if_icmpge", 3),
    IF_ICMPGT      (0xa3, "if_icmpgt", 3),
    IF_ICMPLE      (0xa4, "if_icmple", 3),
    IFEQ           (0x99, "ifeq", 3),
    IFNE           (0x9a, "ifne", 3),
    IFLT           (0x9b, "iflt", 3),
    IFGE           (0x9c, "ifge", 3),
    IFGT           (0x9d, "ifgt", 3),
    IFLE           (0x9e, "ifle", 3),
    IFNONNULL      (0xc7, "ifnonnull", 3),
    IFNULL         (0xc6, "ifnull", 3),
    IINC           (0x84, "iinc", 3),

    ILOAD          (0x15, "iload",   2, VariableInstruction::create),
    ILOAD_0        (0x1a, "iload_0", 1, VariableInstruction::create),
    ILOAD_1        (0x1b, "iload_1", 1, VariableInstruction::create),
    ILOAD_2        (0x1c, "iload_2", 1, VariableInstruction::create),
    ILOAD_3        (0x1d, "iload_3", 1, VariableInstruction::create),

    IMUL           (0x68, "imul", 1),
    INEG           (0x74, "ineg", 1),
    INSTANCEOF     (0xc1, "instanceof", 3),
    INVOKEDYNAMIC  (0xba, "invokedynamic", 5),
    INVOKEINTERFACE(0xb9, "invokeinterface", 5),
    INVOKESPECIAL  (0xb7, "invokespecial", 3),
    INVOKESTATIC   (0xb8, "invokestatic", 3),
    INVOKEVIRTUAL  (0xb6, "invokevirtual", 3),
    IOR            (0x80, "ior", 1),
    IREM           (0x70, "irem", 1),
    IRETURN        (0xac, "ireturn", 1),
    ISHL           (0x78, "ishl", 1),
    ISHR           (0x7a, "ishr", 1),

    ISTORE         (0x36, "istore",   2, VariableInstruction::create),
    ISTORE_0       (0x3b, "istore_0", 1, VariableInstruction::create),
    ISTORE_1       (0x3c, "istore_1", 1, VariableInstruction::create),
    ISTORE_2       (0x3d, "istore_2", 1, VariableInstruction::create),
    ISTORE_3       (0x3e, "istore_3", 1, VariableInstruction::create),

    ISUB           (0x64, "isub", 1),
    IUSHR          (0x7c, "iushr", 1),
    IXOR           (0x82, "ixor", 1),
    JSR            (0x8a, "jsr", 3),
    JSR_W          (0xc9, "jsr_w", 5),
    L2D            (0x8a, "l2d", 1),
    L2F            (0x89, "l2f", 1),
    L2I            (0x88, "l2i", 1),
    LADD           (0x61, "ladd", 1),
    LALOAD         (0x2f, "laload", 1),
    LAND           (0x7f, "land", 1),
    LASTORE        (0x50, "lastore", 1),
    LCMP           (0x94, "lcmp", 1),
    LCONST_0       (0x09, "lconst_0", 1),
    LCONST_1       (0x0a, "lconst_1", 1),
    LDC            (0x12, "ldc", 2),
    LDC_W          (0x13, "ldc_w", 3),
    LDC2_W         (0x14, "ldc2_w", 3),
    LDIV           (0x6d, "ldiv", 1),

    LLOAD          (0x16, "lload",   2, VariableInstruction::create),
    LLOAD_0        (0x1e, "lload_0", 1, VariableInstruction::create),
    LLOAD_1        (0x1f, "lload_1", 1, VariableInstruction::create),
    LLOAD_2        (0x20, "lload_2", 1, VariableInstruction::create),
    LLOAD_3        (0x21, "lload_3", 1, VariableInstruction::create),

    LMUL           (0x69, "lmul", 1),
    LNEG           (0x75, "lneg", 1),
    LOOKUPSWITCH   (0xab, "lookupswitch", -1),
    LOR            (0x81, "lor", 1),
    LREM           (0x71, "lrem", 1),
    LRETURN        (0xad, "lreturn", 1),
    LSHL           (0x79, "lshl", 1),
    LSHR           (0x7b, "lshr", 1),

    LSTORE         (0x37, "lstore",   2, VariableInstruction::create),
    LSTORE_0       (0x3f, "lstore_0", 1, VariableInstruction::create),
    LSTORE_1       (0x40, "lstore_1", 1, VariableInstruction::create),
    LSTORE_2       (0x41, "lstore_2", 1, VariableInstruction::create),
    LSTORE_3       (0x42, "lstore_3", 1, VariableInstruction::create),

    LSUB           (0x65, "lsub", 1),
    LUSHR          (0x7d, "lushr", 1),
    LXOR           (0x83, "lxor", 1),
    MONITORENTER   (0xc2, "monitorenter", 1),
    MONITOREXIT    (0xc3, "monitorexit", 1),
    MULTINEWARRAY  (0xc5, "multinewarray", 4),
    NEW            (0xbb, "new", 3),
    NEWARRAY       (0xbc, "newarray", 2),
    NOP            (0x00, "nop", 1),
    POP            (0x57, "pop", 1),
    POP2           (0x58, "pop2", 1),
    PUTFIELD       (0xb5, "putfield", 3),
    PUTSTATIC      (0xb3, "putstatic", 3),
    RET            (0xa9, "ret", 2),
    RETURN         (0xb1, "return", 1),
    SALOAD         (0x35, "saload", 1),
    SASTORE        (0x56, "sastore", 1),
    SIPUSH         (0x11, "sipush", 3),
    SWAP           (0x5f, "swap", 1),
    TABLESWITCH    (0xaa, "tableswitch", -1),
    WIDE           (0xc4, "wide", -1);

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
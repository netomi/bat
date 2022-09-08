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

    AALOAD         (0x32, "aaload",  1, ArrayInstruction::create),
    AASTORE        (0x53, "aastore", 1, ArrayInstruction::create),
    ACONST_NULL    (0x01, "aconst_null", 1),

    ALOAD          (0x19, "aload",   2, VariableInstruction::create),
    ALOAD_0        (0x2a, "aload_0", 1, VariableInstruction::create),
    ALOAD_1        (0x2b, "aload_1", 1, VariableInstruction::create),
    ALOAD_2        (0x2c, "aload_2", 1, VariableInstruction::create),
    ALOAD_3        (0x2d, "aload_3", 1, VariableInstruction::create),

    ANEWARRAY      (0xbd, "anewarray", 3, ClassInstruction::create),
    ARETURN        (0xb0, "areturn", 1, ReturnInstruction::create),
    ARRAYLENGTH    (0xbe, "arraylength", 1, ArrayInstruction::create),

    ASTORE         (0x3a, "astore",   2, VariableInstruction::create),
    ASTORE_0       (0x4b, "astore_0", 1, VariableInstruction::create),
    ASTORE_1       (0x4c, "astore_1", 1, VariableInstruction::create),
    ASTORE_2       (0x4d, "astore_2", 1, VariableInstruction::create),
    ASTORE_3       (0x4e, "astore_3", 1, VariableInstruction::create),

    ATHROW         (0xbf, "athrow", 1),
    BALOAD         (0x33, "baload",  1, ArrayInstruction::create),
    BASTORE        (0x54, "bastore", 1, ArrayInstruction::create),
    BIPUSH         (0x10, "bipush", 2, LiteralInstruction::create),
    CALOAD         (0x34, "caload",  1, ArrayInstruction::create),
    CASTORE        (0x55, "castore", 1, ArrayInstruction::create),
    CHECKCAST      (0xc0, "checkcast", 3, ClassInstruction::create),

    D2F            (0x90, "d2f", 1, ConversionInstruction::create),
    D2I            (0x8e, "d2i", 1, ConversionInstruction::create),
    D2L            (0x8f, "d2l", 1, ConversionInstruction::create),

    DADD           (0x63, "dadd", 1, ArithmeticInstruction::create),
    DALOAD         (0x31, "daload",  1, ArrayInstruction::create),
    DASTORE        (0x52, "dastore", 1, ArrayInstruction::create),
    DCMPG          (0x98, "dcmpg", 1, CompareInstruction::create),
    DCMPL          (0x97, "dcmpl", 1, CompareInstruction::create),
    DCONST_0       (0x0e, "dconst_0", 1, LiteralInstruction::create),
    DCONST_1       (0x0f, "dconst_1", 1, LiteralInstruction::create),
    DDIV           (0x6f, "ddiv", 1, ArithmeticInstruction::create),

    DLOAD          (0x18, "dload",   2, VariableInstruction::create),
    DLOAD_0        (0x26, "dload_0", 1, VariableInstruction::create),
    DLOAD_1        (0x27, "dload_1", 1, VariableInstruction::create),
    DLOAD_2        (0x28, "dload_2", 1, VariableInstruction::create),
    DLOAD_3        (0x29, "dload_3", 1, VariableInstruction::create),

    DMUL           (0x6b, "dmul", 1, ArithmeticInstruction::create),
    DNEG           (0x77, "dneg", 1, ArithmeticInstruction::create),
    DREM           (0x73, "drem", 1, ArithmeticInstruction::create),
    DRETURN        (0xaf, "dreturn", 1, ReturnInstruction::create),

    DSTORE         (0x39, "dstore",   2, VariableInstruction::create),
    DSTORE_0       (0x47, "dstore_0", 1, VariableInstruction::create),
    DSTORE_1       (0x48, "dstore_1", 1, VariableInstruction::create),
    DSTORE_2       (0x49, "dstore_2", 1, VariableInstruction::create),
    DSTORE_3       (0x4a, "dstore_3", 1, VariableInstruction::create),

    DSUB           (0x67, "dsub", 1, ArithmeticInstruction::create),

    DUP            (0x59, "dup",     1, StackInstruction::create),
    DUP_X1         (0x5a, "dup_x1",  1, StackInstruction::create),
    DUP_X2         (0x5b, "dup_x2",  1, StackInstruction::create),
    DUP2           (0x5c, "dup2",    1, StackInstruction::create),
    DUP2_X1        (0x5d, "dup2_x1", 1, StackInstruction::create),
    DUP2_X2        (0x5e, "dup2_x2", 1, StackInstruction::create),

    F2D            (0x8d, "f2d", 1, ConversionInstruction::create),
    F2I            (0x8b, "f2i", 1, ConversionInstruction::create),
    F2L            (0x8c, "f2l", 1, ConversionInstruction::create),

    FADD           (0x62, "fadd", 1, ArithmeticInstruction::create),

    FALOAD         (0x30, "faload",  1, ArrayInstruction::create),
    FASTORE        (0x51, "fastore", 1, ArrayInstruction::create),

    FCMPG          (0x96, "fcmpg", 1, CompareInstruction::create),
    FCMPL          (0x95, "fcmpl", 1, CompareInstruction::create),

    FCONST_0       (0x0b, "fconst_0", 1, LiteralInstruction::create),
    FCONST_1       (0x0c, "fconst_1", 1, LiteralInstruction::create),
    FCONST_2       (0x0d, "fconst_2", 1, LiteralInstruction::create),

    FDIV           (0x6e, "fdiv", 1, ArithmeticInstruction::create),

    FLOAD          (0x17, "fload",   2, VariableInstruction::create),
    FLOAD_0        (0x22, "fload_0", 1, VariableInstruction::create),
    FLOAD_1        (0x23, "fload_1", 1, VariableInstruction::create),
    FLOAD_2        (0x24, "fload_2", 1, VariableInstruction::create),
    FLOAD_3        (0x25, "fload_3", 1, VariableInstruction::create),

    FMUL           (0x6a, "fmul", 1, ArithmeticInstruction::create),
    FNEG           (0x76, "fneg", 1, ArithmeticInstruction::create),
    FREM           (0x72, "frem", 1, ArithmeticInstruction::create),

    FRETURN        (0xae, "freturn", 1, ReturnInstruction::create),

    FSTORE         (0x38, "fstore",   2, VariableInstruction::create),
    FSTORE_0       (0x43, "fstore_0", 1, VariableInstruction::create),
    FSTORE_1       (0x44, "fstore_1", 1, VariableInstruction::create),
    FSTORE_2       (0x45, "fstore_2", 1, VariableInstruction::create),
    FSTORE_3       (0x46, "fstore_3", 1, VariableInstruction::create),

    FSUB           (0x66, "fsub", 1, ArithmeticInstruction::create),

    GETFIELD       (0xb4, "getfield",  3, FieldInstruction::create),
    GETSTATIC      (0xb2, "getstatic", 3, FieldInstruction::create),

    GOTO           (0xa7, "goto",   3, BranchInstruction::create),
    GOTO_W         (0xc8, "goto_w", 5, BranchInstruction::create),

    I2B            (0x91, "i2b", 1, ConversionInstruction::create),
    I2C            (0x92, "i2c", 1, ConversionInstruction::create),
    I2D            (0x87, "i2d", 1, ConversionInstruction::create),
    I2F            (0x86, "i2f", 1, ConversionInstruction::create),
    I2L            (0x85, "i2l", 1, ConversionInstruction::create),
    I2S            (0x93, "i2s", 1, ConversionInstruction::create),

    IADD           (0x60, "iadd", 1, ArithmeticInstruction::create),

    IALOAD         (0x2e, "iaload", 1, ArrayInstruction::create),

    IAND           (0x7e, "iand", 1, ArithmeticInstruction::create),

    IASTORE        (0x4f, "iastore", 1, ArrayInstruction::create),

    ICONST_M1      (0x02, "iconst_m1", 1, LiteralInstruction::create),
    ICONST_0       (0x03, "iconst_0",  1, LiteralInstruction::create),
    ICONST_1       (0x04, "iconst_1",  1, LiteralInstruction::create),
    ICONST_2       (0x05, "iconst_2",  1, LiteralInstruction::create),
    ICONST_3       (0x06, "iconst_3",  1, LiteralInstruction::create),
    ICONST_4       (0x07, "iconst_4",  1, LiteralInstruction::create),
    ICONST_5       (0x08, "iconst_5",  1, LiteralInstruction::create),

    IDIV           (0x6c, "idiv", 1, ArithmeticInstruction::create),

    IF_ACMPEQ      (0xa5, "if_acmpeq", 3, BranchInstruction::create),
    IF_ACMPNE      (0xa6, "if_acmpne", 3, BranchInstruction::create),
    IF_ICMPEQ      (0x9f, "if_icmpeq", 3, BranchInstruction::create),
    IF_ICMPNE      (0xa0, "if_icmpne", 3, BranchInstruction::create),
    IF_ICMPLT      (0xa1, "if_icmplt", 3, BranchInstruction::create),
    IF_ICMPGE      (0xa2, "if_icmpge", 3, BranchInstruction::create),
    IF_ICMPGT      (0xa3, "if_icmpgt", 3, BranchInstruction::create),
    IF_ICMPLE      (0xa4, "if_icmple", 3, BranchInstruction::create),

    IFEQ           (0x99, "ifeq", 3, BranchInstruction::create),
    IFNE           (0x9a, "ifne", 3, BranchInstruction::create),
    IFLT           (0x9b, "iflt", 3, BranchInstruction::create),
    IFGE           (0x9c, "ifge", 3, BranchInstruction::create),
    IFGT           (0x9d, "ifgt", 3, BranchInstruction::create),
    IFLE           (0x9e, "ifle", 3, BranchInstruction::create),

    IFNONNULL      (0xc7, "ifnonnull", 3, BranchInstruction::create),
    IFNULL         (0xc6, "ifnull",    3, BranchInstruction::create),

    IINC           (0x84, "iinc", 3, LiteralVariableInstruction::create),

    ILOAD          (0x15, "iload",   2, VariableInstruction::create),
    ILOAD_0        (0x1a, "iload_0", 1, VariableInstruction::create),
    ILOAD_1        (0x1b, "iload_1", 1, VariableInstruction::create),
    ILOAD_2        (0x1c, "iload_2", 1, VariableInstruction::create),
    ILOAD_3        (0x1d, "iload_3", 1, VariableInstruction::create),

    IMUL           (0x68, "imul", 1, ArithmeticInstruction::create),
    INEG           (0x74, "ineg", 1, ArithmeticInstruction::create),

    INSTANCEOF     (0xc1, "instanceof", 3, ClassInstruction::create),

    INVOKEDYNAMIC  (0xba, "invokedynamic",   5, InvokeDynamicInstruction::create),
    INVOKEINTERFACE(0xb9, "invokeinterface", 5, InterfaceMethodInstruction::create),
    INVOKESPECIAL  (0xb7, "invokespecial",   3, MethodInstruction::create),
    INVOKESTATIC   (0xb8, "invokestatic",    3, MethodInstruction::create),
    INVOKEVIRTUAL  (0xb6, "invokevirtual",   3, MethodInstruction::create),

    IOR            (0x80, "ior",  1, ArithmeticInstruction::create),
    IREM           (0x70, "irem", 1, ArithmeticInstruction::create),

    IRETURN        (0xac, "ireturn", 1, ReturnInstruction::create),

    ISHL           (0x78, "ishl", 1, ArithmeticInstruction::create),
    ISHR           (0x7a, "ishr", 1, ArithmeticInstruction::create),

    ISTORE         (0x36, "istore",   2, VariableInstruction::create),
    ISTORE_0       (0x3b, "istore_0", 1, VariableInstruction::create),
    ISTORE_1       (0x3c, "istore_1", 1, VariableInstruction::create),
    ISTORE_2       (0x3d, "istore_2", 1, VariableInstruction::create),
    ISTORE_3       (0x3e, "istore_3", 1, VariableInstruction::create),

    ISUB           (0x64, "isub",  1, ArithmeticInstruction::create),
    IUSHR          (0x7c, "iushr", 1, ArithmeticInstruction::create),
    IXOR           (0x82, "ixor",  1, ArithmeticInstruction::create),

    JSR            (0x8a, "jsr", 3),
    JSR_W          (0xc9, "jsr_w", 5),

    L2D            (0x8a, "l2d", 1, ConversionInstruction::create),
    L2F            (0x89, "l2f", 1, ConversionInstruction::create),
    L2I            (0x88, "l2i", 1, ConversionInstruction::create),

    LADD           (0x61, "ladd", 1, ArithmeticInstruction::create),

    LALOAD         (0x2f, "laload", 1, ArrayInstruction::create),

    LAND           (0x7f, "land", 1, ArithmeticInstruction::create),

    LASTORE        (0x50, "lastore", 1, ArrayInstruction::create),

    LCMP           (0x94, "lcmp", 1, CompareInstruction::create),

    LCONST_0       (0x09, "lconst_0", 1, LiteralInstruction::create),
    LCONST_1       (0x0a, "lconst_1", 1, LiteralInstruction::create),

    LDC            (0x12, "ldc",    2, ConstantInstruction::create),
    LDC_W          (0x13, "ldc_w",  3, ConstantInstruction::create),
    LDC2_W         (0x14, "ldc2_w", 3, ConstantInstruction::create),

    LDIV           (0x6d, "ldiv", 1, ArithmeticInstruction::create),

    LLOAD          (0x16, "lload",   2, VariableInstruction::create),
    LLOAD_0        (0x1e, "lload_0", 1, VariableInstruction::create),
    LLOAD_1        (0x1f, "lload_1", 1, VariableInstruction::create),
    LLOAD_2        (0x20, "lload_2", 1, VariableInstruction::create),
    LLOAD_3        (0x21, "lload_3", 1, VariableInstruction::create),

    LMUL           (0x69, "lmul", 1, ArithmeticInstruction::create),
    LNEG           (0x75, "lneg", 1, ArithmeticInstruction::create),

    LOOKUPSWITCH   (0xab, "lookupswitch", 0, LookupSwitchInstruction::create),

    LOR            (0x81, "lor",  1, ArithmeticInstruction::create),
    LREM           (0x71, "lrem", 1, ArithmeticInstruction::create),

    LRETURN        (0xad, "lreturn", 1, ReturnInstruction::create),

    LSHL           (0x79, "lshl", 1, ArithmeticInstruction::create),
    LSHR           (0x7b, "lshr", 1, ArithmeticInstruction::create),

    LSTORE         (0x37, "lstore",   2, VariableInstruction::create),
    LSTORE_0       (0x3f, "lstore_0", 1, VariableInstruction::create),
    LSTORE_1       (0x40, "lstore_1", 1, VariableInstruction::create),
    LSTORE_2       (0x41, "lstore_2", 1, VariableInstruction::create),
    LSTORE_3       (0x42, "lstore_3", 1, VariableInstruction::create),

    LSUB           (0x65, "lsub",  1, ArithmeticInstruction::create),
    LUSHR          (0x7d, "lushr", 1, ArithmeticInstruction::create),
    LXOR           (0x83, "lxor",  1, ArithmeticInstruction::create),

    MONITORENTER   (0xc2, "monitorenter", 1, MonitorInstruction::create),
    MONITOREXIT    (0xc3, "monitorexit",  1, MonitorInstruction::create),

    MULTINEWARRAY  (0xc5, "multinewarray", 4),

    NEW            (0xbb, "new", 3, ClassInstruction::create),

    NEWARRAY       (0xbc, "newarray", 2, ArrayTypeInstruction::create),

    NOP            (0x00, "nop", 1),

    POP            (0x57, "pop",  1, StackInstruction::create),
    POP2           (0x58, "pop2", 1, StackInstruction::create),

    PUTFIELD       (0xb5, "putfield",  3, FieldInstruction::create),
    PUTSTATIC      (0xb3, "putstatic", 3, FieldInstruction::create),

    RET            (0xa9, "ret", 2),

    RETURN         (0xb1, "return", 1, ReturnInstruction::create),

    SALOAD         (0x35, "saload",  1, ArrayInstruction::create),
    SASTORE        (0x56, "sastore", 1, ArrayInstruction::create),

    SIPUSH         (0x11, "sipush", 3, LiteralInstruction::create),

    SWAP           (0x5f, "swap", 1, StackInstruction::create),

    TABLESWITCH    (0xaa, "tableswitch", 0, TableSwitchInstruction::create),

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
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

/**
 * @author Thomas Neidhart
 */
public enum DexOpCode
{
    NOP((byte) 0x00, DexInstructionFormat.FORMAT_10x, "nop"),
    MOVE((byte) 0x01, DexInstructionFormat.FORMAT_12x, "move"),
    MOVE_FROM16((byte) 0x02, DexInstructionFormat.FORMAT_22x, "move/from16"),
    MOVE_16((byte) 0x03, DexInstructionFormat.FORMAT_32x, "move/16"),
    MOVE_WIDE((byte) 0x04, DexInstructionFormat.FORMAT_12x, "move-wide"),
    MOVE_WIDE_FROM16((byte) 0x05, DexInstructionFormat.FORMAT_22x, "move-wide/from16"),
    MOVE_WIDE_16((byte) 0x06, DexInstructionFormat.FORMAT_32x, "move-wide/16"),
    MOVE_OBJECT((byte) 0x07, DexInstructionFormat.FORMAT_12x, "move-object"),
    MOVE_OBJECT_FROM16((byte) 0x08, DexInstructionFormat.FORMAT_22x, "move-object/from16"),
    MOVE_OBJECT_16((byte) 0x09, DexInstructionFormat.FORMAT_32x, "move-object/16"),
    MOVE_RESULT((byte) 0x0a, DexInstructionFormat.FORMAT_11x, "move-result"),
    MOVE_RESULT_WIDE((byte) 0x0b, DexInstructionFormat.FORMAT_11x, "move-result-wide"),
    MOVE_RESULT_OBJECT((byte) 0x0c, DexInstructionFormat.FORMAT_11x, "move-result-object"),
    MOVE_EXCEPTION((byte) 0x0d, DexInstructionFormat.FORMAT_11x, "move-exception"),
    RETURN_VOID((byte) 0x0e, DexInstructionFormat.FORMAT_10x, "return-void"),
    RETURN((byte) 0x0f, DexInstructionFormat.FORMAT_11x, "return"),
    RETURN_WIDE((byte) 0x10, DexInstructionFormat.FORMAT_11x, "return-wide"),
    RETURN_OBJECT((byte) 0x11, DexInstructionFormat.FORMAT_11x, "return-object"),
    CONST_4((byte) 0x12, DexInstructionFormat.FORMAT_11n, "const/4"),
    CONST_16((byte) 0x13, DexInstructionFormat.FORMAT_21s, "const/16"),
    CONST((byte) 0x14, DexInstructionFormat.FORMAT_31i, "const"),
    CONST_HIGH16((byte) 0x15, DexInstructionFormat.FORMAT_21h, "const/high16"),
    CONST_WIDE_16((byte) 0x16, DexInstructionFormat.FORMAT_21s, "const-wide/high16"),
    CONST_WIDE_32((byte) 0x17, DexInstructionFormat.FORMAT_31i, "const-wide/32"),
    CONST_WIDE((byte) 0x18, DexInstructionFormat.FORMAT_51l, "const-wide"),
    CONST_WIDE_HIGH16((byte) 0x19, DexInstructionFormat.FORMAT_21h, "const-wide/high16"),
    CONST_STRING((byte) 0x1a, DexInstructionFormat.FORMAT_21c, "const-string"),
    CONST_STRING_JUMBO((byte) 0x1b, DexInstructionFormat.FORMAT_31c, "const-string/jumbo"),
    CONST_CLASS((byte) 0x1c, DexInstructionFormat.FORMAT_21c, "const-class"),
    MONITOR_ENTER((byte) 0x1d, DexInstructionFormat.FORMAT_11x, "monitor-enter"),
    MONITOR_EXIT((byte) 0x1e, DexInstructionFormat.FORMAT_11x, "monitor-exit"),
    CHECK_CAST((byte) 0x1f, DexInstructionFormat.FORMAT_21c, "check-cast"),
    INSTANCE_OF((byte) 0x20, DexInstructionFormat.FORMAT_22c, "instance-of"),
    ARRAY_LENGTH((byte) 0x21, DexInstructionFormat.FORMAT_12x, "array-length"),
    NEW_INSTANCE((byte) 0x22, DexInstructionFormat.FORMAT_21c, "new-instance"),
    NEW_ARRAY((byte) 0x23, DexInstructionFormat.FORMAT_22c, "new-array"),
    FILLED_NEW_ARRAY((byte) 0x24, DexInstructionFormat.FORMAT_35c, "filled-new-array"),
    FILLED_NEW_ARRAY_RANGE((byte) 0x25, DexInstructionFormat.FORMAT_3rc, "filled-new-array/range"),
    FILL_ARRAY_DATA((byte) 0x26, DexInstructionFormat.FORMAT_31t, "fill-array-data"),
    THROW((byte) 0x27, DexInstructionFormat.FORMAT_11x, "throw"),
    GOTO((byte) 0x28, DexInstructionFormat.FORMAT_10t, "goto"),
    GOTO_16((byte) 0x29, DexInstructionFormat.FORMAT_20t, "goto/16"),
    GOTO_32((byte) 0x2a, DexInstructionFormat.FORMAT_30t, "goto/32"),
    PACKED_SWITCH((byte) 0x2b, DexInstructionFormat.FORMAT_31t, "packed-switch"),
    SPARSE_SWITCH((byte) 0x2c, DexInstructionFormat.FORMAT_31t, "sparse-switch"),
    CMPL_FLOAT((byte) 0x2d, DexInstructionFormat.FORMAT_23x, "cmpl-float"),
    CMPG_FLOAT((byte) 0x2e, DexInstructionFormat.FORMAT_23x, "cmpg-float"),
    CMPL_DOUBLE((byte) 0x2f, DexInstructionFormat.FORMAT_23x, "cmpl-double"),
    CMPG_DOUBLE((byte) 0x30, DexInstructionFormat.FORMAT_23x, "cmpg-double"),
    CMP_LONG((byte) 0x31, DexInstructionFormat.FORMAT_23x, "cmp-long"),
    IF_EQ((byte) 0x32, DexInstructionFormat.FORMAT_22t, "if-eq"),
    IF_NE((byte) 0x33, DexInstructionFormat.FORMAT_22t, "if-ne"),
    IF_LT((byte) 0x34, DexInstructionFormat.FORMAT_22t, "if-lt"),
    IF_GE((byte) 0x35, DexInstructionFormat.FORMAT_22t, "if-ge"),
    IF_GT((byte) 0x36, DexInstructionFormat.FORMAT_22t, "if-gt"),
    IF_LE((byte) 0x37, DexInstructionFormat.FORMAT_22t, "if-le"),
    IF_EQZ((byte) 0x38, DexInstructionFormat.FORMAT_21t, "if-eqz"),
    IF_NEZ((byte) 0x39, DexInstructionFormat.FORMAT_21t, "if-nez"),
    IF_LTZ((byte) 0x3a, DexInstructionFormat.FORMAT_21t, "if-ltz"),
    IF_GEZ((byte) 0x3b, DexInstructionFormat.FORMAT_21t, "if-gez"),
    IF_GTZ((byte) 0x3c, DexInstructionFormat.FORMAT_21t, "if-gtz"),
    IF_LEZ((byte) 0x3d, DexInstructionFormat.FORMAT_21t, "if-lez"),

    AGET((byte) 0x44, DexInstructionFormat.FORMAT_23x, "aget"),
    AGET_WIDE((byte) 0x45, DexInstructionFormat.FORMAT_23x, "aget-wide"),
    AGET_OBJECT((byte) 0x46, DexInstructionFormat.FORMAT_23x, "aget-object"),
    AGET_BOOLEAN((byte) 0x47, DexInstructionFormat.FORMAT_23x, "aget-boolean"),
    AGET_BYTE((byte) 0x48, DexInstructionFormat.FORMAT_23x, "aget-byte"),
    AGET_CHAR((byte) 0x49, DexInstructionFormat.FORMAT_23x, "aget-char"),
    AGET_SHORT((byte) 0x4a, DexInstructionFormat.FORMAT_23x, "aget-short"),
    APUT((byte) 0x4b, DexInstructionFormat.FORMAT_23x, "aput"),
    APUT_WIDE((byte) 0x4c, DexInstructionFormat.FORMAT_23x, "aput-wide"),
    APUT_OBJECT((byte) 0x4d, DexInstructionFormat.FORMAT_23x, "aput-object"),
    APUT_BOOLEAN((byte) 0x4e, DexInstructionFormat.FORMAT_23x, "aput-boolean"),
    APUT_BYTE((byte) 0x4f, DexInstructionFormat.FORMAT_23x, "aput-byte"),
    APUT_CHAR((byte) 0x50, DexInstructionFormat.FORMAT_23x, "aput-char"),
    APUT_SHORT((byte) 0x51, DexInstructionFormat.FORMAT_23x, "aput-short"),

    IGET((byte) 0x52, DexInstructionFormat.FORMAT_22c, "iget"),
    IGET_WIDE((byte) 0x53, DexInstructionFormat.FORMAT_22c, "iget-wide"),
    IGET_OBJECT((byte) 0x54, DexInstructionFormat.FORMAT_22c, "iget-object"),
    IGET_BOOLEAN((byte) 0x55, DexInstructionFormat.FORMAT_22c, "iget-boolean"),
    IGET_BYTE((byte) 0x56, DexInstructionFormat.FORMAT_22c, "iget-byte"),
    IGET_CHAR((byte) 0x57, DexInstructionFormat.FORMAT_22c, "iget-char"),
    IGET_SHORT((byte) 0x58, DexInstructionFormat.FORMAT_22c, "iget-short"),
    IPUT((byte) 0x59, DexInstructionFormat.FORMAT_22c, "iput"),
    IPUT_WIDE((byte) 0x5a, DexInstructionFormat.FORMAT_22c, "iput-wide"),
    IPUT_OBJECT((byte) 0x5b, DexInstructionFormat.FORMAT_22c, "iput-object"),
    IPUT_BOOLEAN((byte) 0x5c, DexInstructionFormat.FORMAT_22c, "iput-boolean"),
    IPUT_BYTE((byte) 0x5d, DexInstructionFormat.FORMAT_22c, "iput-byte"),
    IPUT_CHAR((byte) 0x5e, DexInstructionFormat.FORMAT_22c, "iput-char"),
    IPUT_SHORT((byte) 0x5f, DexInstructionFormat.FORMAT_22c, "iput-short"),

    SGET((byte) 0x60, DexInstructionFormat.FORMAT_21c, "sget"),
    SGET_WIDE((byte) 0x61, DexInstructionFormat.FORMAT_21c, "sget-wide"),
    SGET_OBJECT((byte) 0x62, DexInstructionFormat.FORMAT_21c, "sget-object"),
    SGET_BOOLEAN((byte) 0x63, DexInstructionFormat.FORMAT_21c, "sget-boolean"),
    SGET_BYTE((byte) 0x64, DexInstructionFormat.FORMAT_21c, "sget-byte"),
    SGET_CHAR((byte) 0x65, DexInstructionFormat.FORMAT_21c, "sget-char"),
    SGET_SHORT((byte) 0x66, DexInstructionFormat.FORMAT_21c, "sget-short"),
    SPUT((byte) 0x67, DexInstructionFormat.FORMAT_21c, "sput"),
    SPUT_WIDE((byte) 0x68, DexInstructionFormat.FORMAT_21c, "sput-wide"),
    SPUT_OBJECT((byte) 0x69, DexInstructionFormat.FORMAT_21c, "sput-object"),
    SPUT_BOOLEAN((byte) 0x6a, DexInstructionFormat.FORMAT_21c, "sput-boolean"),
    SPUT_BYTE((byte) 0x6b, DexInstructionFormat.FORMAT_21c, "sput-byte"),
    SPUT_CHAR((byte) 0x6c, DexInstructionFormat.FORMAT_21c, "sput-char"),
    SPUT_SHORT((byte) 0x6d, DexInstructionFormat.FORMAT_21c, "sput-short"),

    INVOKE_VIRTUAL((byte) 0x6e, DexInstructionFormat.FORMAT_35c, "invoke-virtual"),
    INVOKE_SUPER((byte) 0x6f, DexInstructionFormat.FORMAT_35c, "invoke-super"),
    INVOKE_DIRECT((byte) 0x70, DexInstructionFormat.FORMAT_35c, "invoke-direct"),
    INVOKE_STATIC((byte) 0x71, DexInstructionFormat.FORMAT_35c, "invoke-static"),
    INVOKE_INTERFACE((byte) 0x72, DexInstructionFormat.FORMAT_35c, "invoke-interface"),

    INVOKE_VIRTUAL_RANGE((byte) 0x74, DexInstructionFormat.FORMAT_3rc, "invoke-virtual/range"),
    INVOKE_SUPER_RANGE((byte) 0x75, DexInstructionFormat.FORMAT_3rc, "invoke-super/range"),
    INVOKE_DIRECT_RANGE((byte) 0x76, DexInstructionFormat.FORMAT_3rc, "invoke-direct/range"),
    INVOKE_STATIC_RANGE((byte) 0x77, DexInstructionFormat.FORMAT_3rc, "invoke-static/range"),
    INVOKE_INTERFACE_RANGE((byte) 0x78, DexInstructionFormat.FORMAT_3rc, "invoke-interface/range");


    private final byte opCode;
    private final DexInstructionFormat format;
    private final String mnemonic;

    private static final DexOpCode[] opcodes;

    static
    {
        opcodes = new DexOpCode[0xff];
        for (DexOpCode opCode : values())
        {
            opcodes[opCode.opCode & 0xFF] = opCode;
        }
    }

    DexOpCode(byte opcode, DexInstructionFormat format, String mnemonic) {
        this.opCode = opcode;
        this.format = format;
        this.mnemonic = mnemonic;
    }

    public byte getOpCode() {
        return opCode;
    }

    public int getLength() {
        return format.getInstructionLength();
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public static DexOpCode get(byte opcode)
    {
        return opcodes[opcode & 0xFF];
    }
}

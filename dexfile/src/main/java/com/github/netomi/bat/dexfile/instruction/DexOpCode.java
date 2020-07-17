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
    // basic instructions.

    NOP(               (byte) 0x00, DexInstructionFormat.FORMAT_10x, BasicInstruction::create, "nop"),
    MOVE(              (byte) 0x01, DexInstructionFormat.FORMAT_12x, BasicInstruction::create, "move"),
    MOVE_FROM16(       (byte) 0x02, DexInstructionFormat.FORMAT_22x, BasicInstruction::create, "move/from16"),
    MOVE_16(           (byte) 0x03, DexInstructionFormat.FORMAT_32x, BasicInstruction::create, "move/16"),
    MOVE_WIDE(         (byte) 0x04, DexInstructionFormat.FORMAT_12x, BasicInstruction::create, "move-wide"),
    MOVE_WIDE_FROM16(  (byte) 0x05, DexInstructionFormat.FORMAT_22x, BasicInstruction::create, "move-wide/from16"),
    MOVE_WIDE_16(      (byte) 0x06, DexInstructionFormat.FORMAT_32x, BasicInstruction::create, "move-wide/16"),
    MOVE_OBJECT(       (byte) 0x07, DexInstructionFormat.FORMAT_12x, BasicInstruction::create, "move-object"),
    MOVE_OBJECT_FROM16((byte) 0x08, DexInstructionFormat.FORMAT_22x, BasicInstruction::create, "move-object/from16"),
    MOVE_OBJECT_16(    (byte) 0x09, DexInstructionFormat.FORMAT_32x, BasicInstruction::create, "move-object/16"),
    MOVE_RESULT(       (byte) 0x0a, DexInstructionFormat.FORMAT_11x, BasicInstruction::create, "move-result"),
    MOVE_RESULT_WIDE(  (byte) 0x0b, DexInstructionFormat.FORMAT_11x, BasicInstruction::create, "move-result-wide"),
    MOVE_RESULT_OBJECT((byte) 0x0c, DexInstructionFormat.FORMAT_11x, BasicInstruction::create, "move-result-object"),
    MOVE_EXCEPTION(    (byte) 0x0d, DexInstructionFormat.FORMAT_11x, BasicInstruction::create, "move-exception"),
    RETURN_VOID(       (byte) 0x0e, DexInstructionFormat.FORMAT_10x, BasicInstruction::create, "return-void"),
    RETURN(            (byte) 0x0f, DexInstructionFormat.FORMAT_11x, BasicInstruction::create, "return"),
    RETURN_WIDE(       (byte) 0x10, DexInstructionFormat.FORMAT_11x, BasicInstruction::create, "return-wide"),
    RETURN_OBJECT(     (byte) 0x11, DexInstructionFormat.FORMAT_11x, BasicInstruction::create, "return-object"),

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

    // array instructions.

    AGET(        (byte) 0x44, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget"),
    AGET_WIDE(   (byte) 0x45, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-wide"),
    AGET_OBJECT( (byte) 0x46, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-object"),
    AGET_BOOLEAN((byte) 0x47, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-boolean"),
    AGET_BYTE(   (byte) 0x48, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-byte"),
    AGET_CHAR(   (byte) 0x49, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-char"),
    AGET_SHORT(  (byte) 0x4a, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-short"),
    APUT(        (byte) 0x4b, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput"),
    APUT_WIDE(   (byte) 0x4c, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-wide"),
    APUT_OBJECT( (byte) 0x4d, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-object"),
    APUT_BOOLEAN((byte) 0x4e, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-boolean"),
    APUT_BYTE(   (byte) 0x4f, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-byte"),
    APUT_CHAR(   (byte) 0x50, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-char"),
    APUT_SHORT(  (byte) 0x51, DexInstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-short"),

    // field instructions.

    IGET(        (byte) 0x52, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iget"),
    IGET_WIDE(   (byte) 0x53, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-wide"),
    IGET_OBJECT( (byte) 0x54, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-object"),
    IGET_BOOLEAN((byte) 0x55, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-boolean"),
    IGET_BYTE(   (byte) 0x56, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-byte"),
    IGET_CHAR(   (byte) 0x57, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-char"),
    IGET_SHORT(  (byte) 0x58, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-short"),
    IPUT(        (byte) 0x59, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iput"),
    IPUT_WIDE(   (byte) 0x5a, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-wide"),
    IPUT_OBJECT( (byte) 0x5b, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-object"),
    IPUT_BOOLEAN((byte) 0x5c, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-boolean"),
    IPUT_BYTE(   (byte) 0x5d, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-byte"),
    IPUT_CHAR(   (byte) 0x5e, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-char"),
    IPUT_SHORT(  (byte) 0x5f, DexInstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-short"),

    SGET        ((byte) 0x60, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sget"),
    SGET_WIDE   ((byte) 0x61, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-wide"),
    SGET_OBJECT ((byte) 0x62, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-object"),
    SGET_BOOLEAN((byte) 0x63, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-boolean"),
    SGET_BYTE   ((byte) 0x64, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-byte"),
    SGET_CHAR   ((byte) 0x65, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-char"),
    SGET_SHORT  ((byte) 0x66, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-short"),
    SPUT        ((byte) 0x67, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sput"),
    SPUT_WIDE   ((byte) 0x68, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-wide"),
    SPUT_OBJECT ((byte) 0x69, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-object"),
    SPUT_BOOLEAN((byte) 0x6a, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-boolean"),
    SPUT_BYTE   ((byte) 0x6b, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-byte"),
    SPUT_CHAR   ((byte) 0x6c, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-char"),
    SPUT_SHORT  ((byte) 0x6d, DexInstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-short"),

    // method instructions.

    INVOKE_VIRTUAL((byte) 0x6e, DexInstructionFormat.FORMAT_35c, "invoke-virtual"),
    INVOKE_SUPER((byte) 0x6f, DexInstructionFormat.FORMAT_35c, "invoke-super"),
    INVOKE_DIRECT((byte) 0x70, DexInstructionFormat.FORMAT_35c, "invoke-direct"),
    INVOKE_STATIC((byte) 0x71, DexInstructionFormat.FORMAT_35c, "invoke-static"),
    INVOKE_INTERFACE((byte) 0x72, DexInstructionFormat.FORMAT_35c, "invoke-interface"),

    INVOKE_VIRTUAL_RANGE((byte) 0x74, DexInstructionFormat.FORMAT_3rc, "invoke-virtual/range"),
    INVOKE_SUPER_RANGE((byte) 0x75, DexInstructionFormat.FORMAT_3rc, "invoke-super/range"),
    INVOKE_DIRECT_RANGE((byte) 0x76, DexInstructionFormat.FORMAT_3rc, "invoke-direct/range"),
    INVOKE_STATIC_RANGE((byte) 0x77, DexInstructionFormat.FORMAT_3rc, "invoke-static/range"),
    INVOKE_INTERFACE_RANGE((byte) 0x78, DexInstructionFormat.FORMAT_3rc, "invoke-interface/range"),

    NEG_INT((byte) 0x7b, DexInstructionFormat.FORMAT_12x, "neg-int"),
    NOT_INT((byte) 0x7c, DexInstructionFormat.FORMAT_12x, "not-int"),
    NEG_LONG((byte) 0x7d, DexInstructionFormat.FORMAT_12x, "neg-long"),
    NOT_LONG((byte) 0x7e, DexInstructionFormat.FORMAT_12x, "not-long"),
    NEG_FLOAT((byte) 0x7f, DexInstructionFormat.FORMAT_12x, "neg-float"),
    NEG_DOUBLE((byte) 0x80, DexInstructionFormat.FORMAT_12x, "neg-double"),
    INT_TO_LONG((byte) 0x81, DexInstructionFormat.FORMAT_12x, "int-to-long"),
    INT_TO_FLOAT((byte) 0x82, DexInstructionFormat.FORMAT_12x, "int-to-float"),
    INT_TO_DOUBLE((byte) 0x83, DexInstructionFormat.FORMAT_12x, "int-to-double"),
    LONG_TO_INT((byte) 0x84, DexInstructionFormat.FORMAT_12x, "long-to-int"),
    LONG_TO_FLOAT((byte) 0x85, DexInstructionFormat.FORMAT_12x, "long-to-float"),
    LONG_TO_DOUBLE((byte) 0x86, DexInstructionFormat.FORMAT_12x, "long-to-double"),
    FLOAT_TO_INT((byte) 0x87, DexInstructionFormat.FORMAT_12x, "float-to-int"),
    FLOAT_TO_LONG((byte) 0x88, DexInstructionFormat.FORMAT_12x, "float-to-long"),
    FLOAT_TO_DOUBLE((byte) 0x89, DexInstructionFormat.FORMAT_12x, "float-to-double"),
    DOUBLE_TO_INT((byte) 0x8a, DexInstructionFormat.FORMAT_12x, "double-to-int"),
    DOUBLE_TO_LONG((byte) 0x8b, DexInstructionFormat.FORMAT_12x, "double-to-long"),
    DOUBLE_TO_FLOAT((byte) 0x8c, DexInstructionFormat.FORMAT_12x, "double-to-float"),
    INT_TO_BYTE((byte) 0x8d, DexInstructionFormat.FORMAT_12x, "int-to-byte"),
    INT_TO_CHAR((byte) 0x8e, DexInstructionFormat.FORMAT_12x, "int-to-char"),
    INT_TO_SHORT((byte) 0x8f, DexInstructionFormat.FORMAT_12x, "int-to-short"),

    ADD_INT((byte) 0x90, DexInstructionFormat.FORMAT_23x, "add-int"),
    SUB_INT((byte) 0x91, DexInstructionFormat.FORMAT_23x, "sub-int"),
    MUL_INT((byte) 0x92, DexInstructionFormat.FORMAT_23x, "mul-int"),
    DIV_INT((byte) 0x93, DexInstructionFormat.FORMAT_23x, "div-int"),
    REM_INT((byte) 0x94, DexInstructionFormat.FORMAT_23x, "rem-int"),
    AND_INT((byte) 0x95, DexInstructionFormat.FORMAT_23x, "and-int"),
    OR_INT((byte) 0x96, DexInstructionFormat.FORMAT_23x, "or-int"),
    XOR_INT((byte) 0x97, DexInstructionFormat.FORMAT_23x, "xor-int"),
    SHL_INT((byte) 0x98, DexInstructionFormat.FORMAT_23x, "shl-int"),
    SHR_INT((byte) 0x99, DexInstructionFormat.FORMAT_23x, "shr-int"),
    USHR_INT((byte) 0x9a, DexInstructionFormat.FORMAT_23x, "ushr-int"),
    ADD_LONG((byte) 0x9b, DexInstructionFormat.FORMAT_23x, "add-long"),
    SUB_LONG((byte) 0x9c, DexInstructionFormat.FORMAT_23x, "sub-long"),
    MUL_LONG((byte) 0x9d, DexInstructionFormat.FORMAT_23x, "mul-long"),
    DIV_LONG((byte) 0x9e, DexInstructionFormat.FORMAT_23x, "div-long"),
    REM_LONG((byte) 0x9f, DexInstructionFormat.FORMAT_23x, "rem-long"),
    AND_LONG((byte) 0xa0, DexInstructionFormat.FORMAT_23x, "and-long"),
    OR_LONG((byte) 0xa1, DexInstructionFormat.FORMAT_23x, "or-long"),
    XOR_LONG((byte) 0xa2, DexInstructionFormat.FORMAT_23x, "xor-long"),
    SHL_LONG((byte) 0xa3, DexInstructionFormat.FORMAT_23x, "shl-long"),
    SHR_LONG((byte) 0xa4, DexInstructionFormat.FORMAT_23x, "shr-long"),
    USHR_LONG((byte) 0xa5, DexInstructionFormat.FORMAT_23x, "ushr-long"),
    ADD_FLOAT((byte) 0xa6, DexInstructionFormat.FORMAT_23x, "add-float"),
    SUB_FLOAT((byte) 0xa7, DexInstructionFormat.FORMAT_23x, "sub-float"),
    MUL_FLOAT((byte) 0xa8, DexInstructionFormat.FORMAT_23x, "mul-float"),
    DIV_FLOAT((byte) 0xa9, DexInstructionFormat.FORMAT_23x, "div-float"),
    REM_FLOAT((byte) 0xaa, DexInstructionFormat.FORMAT_23x, "rem-float"),
    ADD_DOUBLE((byte) 0xab, DexInstructionFormat.FORMAT_23x, "add-double"),
    SUB_DOUBLE((byte) 0xac, DexInstructionFormat.FORMAT_23x, "sub-double"),
    MUL_DOUBLE((byte) 0xad, DexInstructionFormat.FORMAT_23x, "mul-double"),
    DIV_DOUBLE((byte) 0xae, DexInstructionFormat.FORMAT_23x, "div-double"),
    REM_DOUBLE((byte) 0xaf, DexInstructionFormat.FORMAT_23x, "rem-double"),

    ADD_INT_2ADDR((byte) 0xb0, DexInstructionFormat.FORMAT_12x, "add-int/2addr"),
    SUB_INT_2ADDR((byte) 0xb1, DexInstructionFormat.FORMAT_12x, "sub-int/2addr"),
    MUL_INT_2ADDR((byte) 0xb2, DexInstructionFormat.FORMAT_12x, "mul-int/2addr"),
    DIV_INT_2ADDR((byte) 0xb3, DexInstructionFormat.FORMAT_12x, "div-int/2addr"),
    REM_INT_2ADDR((byte) 0xb4, DexInstructionFormat.FORMAT_12x, "rem-int/2addr"),
    AND_INT_2ADDR((byte) 0xb5, DexInstructionFormat.FORMAT_12x, "and-int/2addr"),
    OR_INT_2ADDR((byte) 0xb6, DexInstructionFormat.FORMAT_12x, "or-int/2addr"),
    XOR_INT_2ADDR((byte) 0xb7, DexInstructionFormat.FORMAT_12x, "xor-int/2addr"),
    SHL_INT_2ADDR((byte) 0xb8, DexInstructionFormat.FORMAT_12x, "shl-int/2addr"),
    SHR_INT_2ADDR((byte) 0xb9, DexInstructionFormat.FORMAT_12x, "shr-int/2addr"),
    USHR_INT_2ADDR((byte) 0xba, DexInstructionFormat.FORMAT_12x, "ushr-int/2addr"),
    ADD_LONG_2ADDR((byte) 0xbb, DexInstructionFormat.FORMAT_12x, "add-long/2addr"),
    SUB_LONG_2ADDR((byte) 0xbc, DexInstructionFormat.FORMAT_12x, "sub-long/2addr"),
    MUL_LONG_2ADDR((byte) 0xbd, DexInstructionFormat.FORMAT_12x, "mul-long/2addr"),
    DIV_LONG_2ADDR((byte) 0xbe, DexInstructionFormat.FORMAT_12x, "div-long/2addr"),
    REM_LONG_2ADDR((byte) 0xbf, DexInstructionFormat.FORMAT_12x, "rem-long/2addr"),
    AND_LONG_2ADDR((byte) 0xc0, DexInstructionFormat.FORMAT_12x, "and-long/2addr"),
    OR_LONG_2ADDR((byte) 0xc1, DexInstructionFormat.FORMAT_12x, "or-long/2addr"),
    XOR_LONG_2ADDR((byte) 0xc2, DexInstructionFormat.FORMAT_12x, "xor-long/2addr"),
    SHL_LONG_2ADDR((byte) 0xc3, DexInstructionFormat.FORMAT_12x, "shl-long/2addr"),
    SHR_LONG_2ADDR((byte) 0xc4, DexInstructionFormat.FORMAT_12x, "shr-long/2addr"),
    USHR_LONG_2ADDR((byte) 0xc5, DexInstructionFormat.FORMAT_12x, "ushr-long/2addr"),
    ADD_FLOAT_2ADDR((byte) 0xc6, DexInstructionFormat.FORMAT_12x, "add-float/2addr"),
    SUB_FLOAT_2ADDR((byte) 0xc7, DexInstructionFormat.FORMAT_12x, "sub-float/2addr"),
    MUL_FLOAT_2ADDR((byte) 0xc8, DexInstructionFormat.FORMAT_12x, "mul-float/2addr"),
    DIV_FLOAT_2ADDR((byte) 0xc9, DexInstructionFormat.FORMAT_12x, "div-float/2addr"),
    REM_FLOAT_2ADDR((byte) 0xca, DexInstructionFormat.FORMAT_12x, "rem-float/2addr"),
    ADD_DOUBLE_2ADDR((byte) 0xcb, DexInstructionFormat.FORMAT_12x, "add-double/2addr"),
    SUB_DOUBLE_2ADDR((byte) 0xcc, DexInstructionFormat.FORMAT_12x, "sub-double/2addr"),
    MUL_DOUBLE_2ADDR((byte) 0xcd, DexInstructionFormat.FORMAT_12x, "mul-double/2addr"),
    DIV_DOUBLE_2ADDR((byte) 0xce, DexInstructionFormat.FORMAT_12x, "div-double/2addr"),
    REM_DOUBLE_2ADDR((byte) 0xcf, DexInstructionFormat.FORMAT_12x, "rem-double/2addr"),

    ADD_INT_LIT16((byte) 0xd0, DexInstructionFormat.FORMAT_22s, "add-int/lit16"),
    RSUB_INT((byte) 0xd1, DexInstructionFormat.FORMAT_22s, "rsub-int"),
    MUL_INT_LIT16((byte) 0xd2, DexInstructionFormat.FORMAT_22s, "mul-int/lit16"),
    DIV_INT_LIT16((byte) 0xd3, DexInstructionFormat.FORMAT_22s, "div-int/lit16"),
    REM_INT_LIT16((byte) 0xd4, DexInstructionFormat.FORMAT_22s, "rem-int/lit16"),
    AND_INT_LIT16((byte) 0xd5, DexInstructionFormat.FORMAT_22s, "and-int/lit16"),
    OR_INT_LIT16((byte) 0xd6, DexInstructionFormat.FORMAT_22s, "or-int/lit16"),
    XOR_INT_LIT16((byte) 0xd7, DexInstructionFormat.FORMAT_22s, "xor-int/lit16"),

    ADD_INT_LIT8((byte) 0xd8, DexInstructionFormat.FORMAT_22b, "add-int/lit8"),
    RSUB_INT_LIT8((byte) 0xd9, DexInstructionFormat.FORMAT_22b, "rsub-int/lit8"),
    MUL_INT_LIT8((byte) 0xda, DexInstructionFormat.FORMAT_22b, "mul-int/lit8"),
    DIV_INT_LIT8((byte) 0xdb, DexInstructionFormat.FORMAT_22b, "div-int/lit8"),
    REM_INT_LIT8((byte) 0xdc, DexInstructionFormat.FORMAT_22b, "rem-int/lit8"),
    AND_INT_LIT8((byte) 0xdd, DexInstructionFormat.FORMAT_22b, "and-int/lit8"),
    OR_INT_LIT8((byte) 0xde, DexInstructionFormat.FORMAT_22b, "or-int/lit8"),
    XOR_INT_LIT8((byte) 0xdf, DexInstructionFormat.FORMAT_22b, "xor-int/lit8"),
    SHL_INT_LIT8((byte) 0xe0, DexInstructionFormat.FORMAT_22b, "shl-int/lit8"),
    SHR_INT_LIT8((byte) 0xe1, DexInstructionFormat.FORMAT_22b, "shr-int/lit8"),
    USHR_INT_LIT8((byte) 0xe2, DexInstructionFormat.FORMAT_22b, "ushr-int/lit8"),

    INVOKE_POLYMORPHIC((byte) 0xfa, DexInstructionFormat.FORMAT_45cc, "invoke-polymorphic"),
    INVOKE_POLYMORPHIC_RANGE((byte) 0xfb, DexInstructionFormat.FORMAT_4rcc, "invoke-polymorphic/range"),

    INVOKE_CUSTOM((byte) 0xfc, DexInstructionFormat.FORMAT_35c, "invoke-custom"),
    INVOKE_CUSTOM_RANGE((byte) 0xfd, DexInstructionFormat.FORMAT_3rc, "invoke-custom/range"),

    CONST_METHOD_HANDLE((byte) 0xfe, DexInstructionFormat.FORMAT_21c, "const-method-handle"),
    CONST_METHOD_TYPE((byte) 0xff, DexInstructionFormat.FORMAT_21c, "const-method-type");


    private final byte                 opCode;
    private final DexInstructionFormat format;
    private final InstructionSupplier  supplier;
    private final String               mnemonic;

    private static final DexOpCode[] opcodes;

    static
    {
        opcodes = new DexOpCode[0x100];
        for (DexOpCode opCode : values())
        {
            opcodes[opCode.opCode & 0xff] = opCode;
        }
    }

    DexOpCode(byte opcode, DexInstructionFormat format, String mnemonic) {
        this(opcode, format, DexInstruction::createGeneric, mnemonic);
    }

    DexOpCode(byte opcode, DexInstructionFormat format, InstructionSupplier supplier, String mnemonic) {
        this.opCode   = opcode;
        this.format   = format;
        this.supplier = supplier;
        this.mnemonic = mnemonic;
    }

    public byte getOpCode() {
        return opCode;
    }

    public DexInstructionFormat getFormat() {
        return format;
    }

    public int getLength() {
        return format.getInstructionLength();
    }

    public int getArguments() {
        return format.getArgumentCount();
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public DexInstruction createInstruction(byte ident) {
        return supplier.get(this, ident);
    }

    public static DexOpCode get(byte opcode)
    {
        return opcodes[opcode & 0xff];
    }

    @FunctionalInterface
    private interface InstructionSupplier {
        DexInstruction get(DexOpCode opCode, byte ident);
    }
}

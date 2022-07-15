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

import java.util.HashMap;
import java.util.Map;

import static com.github.netomi.bat.dexfile.instruction.DexInstructionFormat.*;

/**
 * @author Thomas Neidhart
 */
public enum DexOpCode
{
    // basic instructions.

    NOP(               (byte) 0x00, FORMAT_10x, BasicInstruction::create, "nop"),
    MOVE(              (byte) 0x01, FORMAT_12x, BasicInstruction::create, "move"),
    MOVE_FROM16(       (byte) 0x02, FORMAT_22x, BasicInstruction::create, "move/from16"),
    MOVE_16(           (byte) 0x03, FORMAT_32x, BasicInstruction::create, "move/16"),
    MOVE_WIDE(         (byte) 0x04, FORMAT_12x, BasicInstruction::create, "move-wide",        true),
    MOVE_WIDE_FROM16(  (byte) 0x05, FORMAT_22x, BasicInstruction::create, "move-wide/from16", true),
    MOVE_WIDE_16(      (byte) 0x06, FORMAT_32x, BasicInstruction::create, "move-wide/16",     true),
    MOVE_OBJECT(       (byte) 0x07, FORMAT_12x, BasicInstruction::create, "move-object"),
    MOVE_OBJECT_FROM16((byte) 0x08, FORMAT_22x, BasicInstruction::create, "move-object/from16"),
    MOVE_OBJECT_16(    (byte) 0x09, FORMAT_32x, BasicInstruction::create, "move-object/16"),
    MOVE_RESULT(       (byte) 0x0a, FORMAT_11x, BasicInstruction::create, "move-result"),
    MOVE_RESULT_WIDE(  (byte) 0x0b, FORMAT_11x, BasicInstruction::create, "move-result-wide", true),
    MOVE_RESULT_OBJECT((byte) 0x0c, FORMAT_11x, BasicInstruction::create, "move-result-object"),
    MOVE_EXCEPTION(    (byte) 0x0d, FORMAT_11x, BasicInstruction::create, "move-exception"),
    RETURN_VOID(       (byte) 0x0e, FORMAT_10x, BasicInstruction::create, "return-void"),
    RETURN(            (byte) 0x0f, FORMAT_11x, BasicInstruction::create, "return"),
    RETURN_WIDE(       (byte) 0x10, FORMAT_11x, BasicInstruction::create, "return-wide",      true),
    RETURN_OBJECT(     (byte) 0x11, FORMAT_11x, BasicInstruction::create, "return-object"),

    // literal instructions.

    CONST_4(          (byte) 0x12, FORMAT_11n, LiteralInstruction::create, "const/4"),
    CONST_16(         (byte) 0x13, FORMAT_21s, LiteralInstruction::create, "const/16"),
    CONST(            (byte) 0x14, FORMAT_31i, LiteralInstruction::create, "const"),
    CONST_HIGH16(     (byte) 0x15, FORMAT_21h, LiteralInstruction::create, "const/high16"),
    CONST_WIDE_16(    (byte) 0x16, FORMAT_21s, LiteralInstruction::create, "const-wide/16",     true),
    CONST_WIDE_32(    (byte) 0x17, FORMAT_31i, LiteralInstruction::create, "const-wide/32",     true),
    CONST_WIDE(       (byte) 0x18, FORMAT_51l, LiteralInstruction::create, "const-wide",        true),
    CONST_WIDE_HIGH16((byte) 0x19, FORMAT_21h, LiteralInstruction::create, "const-wide/high16", true),

    // string instructions.

    CONST_STRING(      (byte) 0x1a, FORMAT_21c, StringInstruction::create, "const-string"),
    CONST_STRING_JUMBO((byte) 0x1b, FORMAT_31c, StringInstruction::create, "const-string/jumbo"),

    // type instructions.

    CONST_CLASS((byte) 0x1c, FORMAT_21c, TypeInstruction::create, "const-class"),

    // basic instructions.

    MONITOR_ENTER((byte) 0x1d, FORMAT_11x, BasicInstruction::create, "monitor-enter"),
    MONITOR_EXIT( (byte) 0x1e, FORMAT_11x, BasicInstruction::create, "monitor-exit"),

    // type instructions.

    CHECK_CAST( (byte) 0x1f, FORMAT_21c, TypeInstruction::create, "check-cast"),
    INSTANCE_OF((byte) 0x20, FORMAT_22c, TypeInstruction::create, "instance-of"),

    // array instructions.

    ARRAY_LENGTH((byte) 0x21, FORMAT_12x, ArrayInstruction::create, "array-length"),

    // type instructions.

    NEW_INSTANCE((byte) 0x22, FORMAT_21c, TypeInstruction::create, "new-instance"),
    NEW_ARRAY(   (byte) 0x23, FORMAT_22c, TypeInstruction::create, "new-array"),

    FILLED_NEW_ARRAY(      (byte) 0x24, FORMAT_35c, ArrayTypeInstruction::create, "filled-new-array"),
    FILLED_NEW_ARRAY_RANGE((byte) 0x25, FORMAT_3rc, ArrayTypeInstruction::create, "filled-new-array/range"),

    // payload instruction.

    FILL_ARRAY_DATA((byte) 0x26, FORMAT_31t, PayloadInstruction::create, "fill-array-data"),

    // basic instruction.

    THROW((byte) 0x27, FORMAT_11x, BasicInstruction::create, "throw"),

    // branch instructions.

    GOTO(   (byte) 0x28, FORMAT_10t, BranchInstruction::create, "goto"),
    GOTO_16((byte) 0x29, FORMAT_20t, BranchInstruction::create, "goto/16"),
    GOTO_32((byte) 0x2a, FORMAT_30t, BranchInstruction::create, "goto/32"),

    // payload instructions.

    PACKED_SWITCH((byte) 0x2b, FORMAT_31t, PayloadInstruction::create, "packed-switch"),
    SPARSE_SWITCH((byte) 0x2c, FORMAT_31t, PayloadInstruction::create, "sparse-switch"),

    // basic instructions.

    CMPL_FLOAT( (byte) 0x2d, FORMAT_23x, BasicInstruction::create, "cmpl-float"),
    CMPG_FLOAT( (byte) 0x2e, FORMAT_23x, BasicInstruction::create, "cmpg-float"),
    CMPL_DOUBLE((byte) 0x2f, FORMAT_23x, BasicInstruction::create, "cmpl-double", true),
    CMPG_DOUBLE((byte) 0x30, FORMAT_23x, BasicInstruction::create, "cmpg-double", true),
    CMP_LONG(   (byte) 0x31, FORMAT_23x, BasicInstruction::create, "cmp-long",    true),

    // branch instructions.

    IF_EQ( (byte) 0x32, FORMAT_22t, BranchInstruction::create, "if-eq"),
    IF_NE( (byte) 0x33, FORMAT_22t, BranchInstruction::create, "if-ne"),
    IF_LT( (byte) 0x34, FORMAT_22t, BranchInstruction::create, "if-lt"),
    IF_GE( (byte) 0x35, FORMAT_22t, BranchInstruction::create, "if-ge"),
    IF_GT( (byte) 0x36, FORMAT_22t, BranchInstruction::create, "if-gt"),
    IF_LE( (byte) 0x37, FORMAT_22t, BranchInstruction::create, "if-le"),
    IF_EQZ((byte) 0x38, FORMAT_21t, BranchInstruction::create, "if-eqz"),
    IF_NEZ((byte) 0x39, FORMAT_21t, BranchInstruction::create, "if-nez"),
    IF_LTZ((byte) 0x3a, FORMAT_21t, BranchInstruction::create, "if-ltz"),
    IF_GEZ((byte) 0x3b, FORMAT_21t, BranchInstruction::create, "if-gez"),
    IF_GTZ((byte) 0x3c, FORMAT_21t, BranchInstruction::create, "if-gtz"),
    IF_LEZ((byte) 0x3d, FORMAT_21t, BranchInstruction::create, "if-lez"),

    // array instructions.

    AGET(        (byte) 0x44, FORMAT_23x, ArrayInstruction::create, "aget"),
    AGET_WIDE(   (byte) 0x45, FORMAT_23x, ArrayInstruction::create, "aget-wide", true),
    AGET_OBJECT( (byte) 0x46, FORMAT_23x, ArrayInstruction::create, "aget-object"),
    AGET_BOOLEAN((byte) 0x47, FORMAT_23x, ArrayInstruction::create, "aget-boolean"),
    AGET_BYTE(   (byte) 0x48, FORMAT_23x, ArrayInstruction::create, "aget-byte"),
    AGET_CHAR(   (byte) 0x49, FORMAT_23x, ArrayInstruction::create, "aget-char"),
    AGET_SHORT(  (byte) 0x4a, FORMAT_23x, ArrayInstruction::create, "aget-short"),
    APUT(        (byte) 0x4b, FORMAT_23x, ArrayInstruction::create, "aput"),
    APUT_WIDE(   (byte) 0x4c, FORMAT_23x, ArrayInstruction::create, "aput-wide", true),
    APUT_OBJECT( (byte) 0x4d, FORMAT_23x, ArrayInstruction::create, "aput-object"),
    APUT_BOOLEAN((byte) 0x4e, FORMAT_23x, ArrayInstruction::create, "aput-boolean"),
    APUT_BYTE(   (byte) 0x4f, FORMAT_23x, ArrayInstruction::create, "aput-byte"),
    APUT_CHAR(   (byte) 0x50, FORMAT_23x, ArrayInstruction::create, "aput-char"),
    APUT_SHORT(  (byte) 0x51, FORMAT_23x, ArrayInstruction::create, "aput-short"),

    // field instructions.

    IGET(        (byte) 0x52, FORMAT_22c, FieldInstruction::create, "iget"),
    IGET_WIDE(   (byte) 0x53, FORMAT_22c, FieldInstruction::create, "iget-wide", true),
    IGET_OBJECT( (byte) 0x54, FORMAT_22c, FieldInstruction::create, "iget-object"),
    IGET_BOOLEAN((byte) 0x55, FORMAT_22c, FieldInstruction::create, "iget-boolean"),
    IGET_BYTE(   (byte) 0x56, FORMAT_22c, FieldInstruction::create, "iget-byte"),
    IGET_CHAR(   (byte) 0x57, FORMAT_22c, FieldInstruction::create, "iget-char"),
    IGET_SHORT(  (byte) 0x58, FORMAT_22c, FieldInstruction::create, "iget-short"),
    IPUT(        (byte) 0x59, FORMAT_22c, FieldInstruction::create, "iput"),
    IPUT_WIDE(   (byte) 0x5a, FORMAT_22c, FieldInstruction::create, "iput-wide", true),
    IPUT_OBJECT( (byte) 0x5b, FORMAT_22c, FieldInstruction::create, "iput-object"),
    IPUT_BOOLEAN((byte) 0x5c, FORMAT_22c, FieldInstruction::create, "iput-boolean"),
    IPUT_BYTE(   (byte) 0x5d, FORMAT_22c, FieldInstruction::create, "iput-byte"),
    IPUT_CHAR(   (byte) 0x5e, FORMAT_22c, FieldInstruction::create, "iput-char"),
    IPUT_SHORT(  (byte) 0x5f, FORMAT_22c, FieldInstruction::create, "iput-short"),

    SGET        ((byte) 0x60, FORMAT_21c, FieldInstruction::create, "sget"),
    SGET_WIDE   ((byte) 0x61, FORMAT_21c, FieldInstruction::create, "sget-wide", true),
    SGET_OBJECT ((byte) 0x62, FORMAT_21c, FieldInstruction::create, "sget-object"),
    SGET_BOOLEAN((byte) 0x63, FORMAT_21c, FieldInstruction::create, "sget-boolean"),
    SGET_BYTE   ((byte) 0x64, FORMAT_21c, FieldInstruction::create, "sget-byte"),
    SGET_CHAR   ((byte) 0x65, FORMAT_21c, FieldInstruction::create, "sget-char"),
    SGET_SHORT  ((byte) 0x66, FORMAT_21c, FieldInstruction::create, "sget-short"),
    SPUT        ((byte) 0x67, FORMAT_21c, FieldInstruction::create, "sput"),
    SPUT_WIDE   ((byte) 0x68, FORMAT_21c, FieldInstruction::create, "sput-wide", true),
    SPUT_OBJECT ((byte) 0x69, FORMAT_21c, FieldInstruction::create, "sput-object"),
    SPUT_BOOLEAN((byte) 0x6a, FORMAT_21c, FieldInstruction::create, "sput-boolean"),
    SPUT_BYTE   ((byte) 0x6b, FORMAT_21c, FieldInstruction::create, "sput-byte"),
    SPUT_CHAR   ((byte) 0x6c, FORMAT_21c, FieldInstruction::create, "sput-char"),
    SPUT_SHORT  ((byte) 0x6d, FORMAT_21c, FieldInstruction::create, "sput-short"),

    // method instructions.

    INVOKE_VIRTUAL(  (byte) 0x6e, FORMAT_35c, MethodInstruction::create, "invoke-virtual"),
    INVOKE_SUPER(    (byte) 0x6f, FORMAT_35c, MethodInstruction::create, "invoke-super"),
    INVOKE_DIRECT(   (byte) 0x70, FORMAT_35c, MethodInstruction::create, "invoke-direct"),
    INVOKE_STATIC(   (byte) 0x71, FORMAT_35c, MethodInstruction::create, "invoke-static"),
    INVOKE_INTERFACE((byte) 0x72, FORMAT_35c, MethodInstruction::create, "invoke-interface"),

    INVOKE_VIRTUAL_RANGE(  (byte) 0x74, FORMAT_3rc, MethodInstruction::create, "invoke-virtual/range"),
    INVOKE_SUPER_RANGE(    (byte) 0x75, FORMAT_3rc, MethodInstruction::create, "invoke-super/range"),
    INVOKE_DIRECT_RANGE(   (byte) 0x76, FORMAT_3rc, MethodInstruction::create, "invoke-direct/range"),
    INVOKE_STATIC_RANGE(   (byte) 0x77, FORMAT_3rc, MethodInstruction::create, "invoke-static/range"),
    INVOKE_INTERFACE_RANGE((byte) 0x78, FORMAT_3rc, MethodInstruction::create, "invoke-interface/range"),

    // arithmetic instructions.

    NEG_INT(        (byte) 0x7b, FORMAT_12x, ArithmeticInstruction::create, "neg-int"),
    NOT_INT(        (byte) 0x7c, FORMAT_12x, ArithmeticInstruction::create, "not-int"),
    NEG_LONG(       (byte) 0x7d, FORMAT_12x, ArithmeticInstruction::create, "neg-long",        true),
    NOT_LONG(       (byte) 0x7e, FORMAT_12x, ArithmeticInstruction::create, "not-long",        true),
    NEG_FLOAT(      (byte) 0x7f, FORMAT_12x, ArithmeticInstruction::create, "neg-float"),
    NEG_DOUBLE(     (byte) 0x80, FORMAT_12x, ArithmeticInstruction::create, "neg-double",      true),

    // conversion instructions.

    INT_TO_LONG(    (byte) 0x81, FORMAT_12x, ConversionInstruction::create, "int-to-long",     true),
    INT_TO_FLOAT(   (byte) 0x82, FORMAT_12x, ConversionInstruction::create, "int-to-float"),
    INT_TO_DOUBLE(  (byte) 0x83, FORMAT_12x, ConversionInstruction::create, "int-to-double",   true),
    LONG_TO_INT(    (byte) 0x84, FORMAT_12x, ConversionInstruction::create, "long-to-int",     true),
    LONG_TO_FLOAT(  (byte) 0x85, FORMAT_12x, ConversionInstruction::create, "long-to-float",   true),
    LONG_TO_DOUBLE( (byte) 0x86, FORMAT_12x, ConversionInstruction::create, "long-to-double",  true),
    FLOAT_TO_INT(   (byte) 0x87, FORMAT_12x, ConversionInstruction::create, "float-to-int"),
    FLOAT_TO_LONG(  (byte) 0x88, FORMAT_12x, ConversionInstruction::create, "float-to-long",   true),
    FLOAT_TO_DOUBLE((byte) 0x89, FORMAT_12x, ConversionInstruction::create, "float-to-double", true),
    DOUBLE_TO_INT(  (byte) 0x8a, FORMAT_12x, ConversionInstruction::create, "double-to-int",   true),
    DOUBLE_TO_LONG( (byte) 0x8b, FORMAT_12x, ConversionInstruction::create, "double-to-long",  true),
    DOUBLE_TO_FLOAT((byte) 0x8c, FORMAT_12x, ConversionInstruction::create, "double-to-float", true),
    INT_TO_BYTE(    (byte) 0x8d, FORMAT_12x, ConversionInstruction::create, "int-to-byte"),
    INT_TO_CHAR(    (byte) 0x8e, FORMAT_12x, ConversionInstruction::create, "int-to-char"),
    INT_TO_SHORT(   (byte) 0x8f, FORMAT_12x, ConversionInstruction::create, "int-to-short"),

    // arithmetic instructions.

    ADD_INT(   (byte) 0x90, FORMAT_23x, ArithmeticInstruction::create, "add-int"),
    SUB_INT(   (byte) 0x91, FORMAT_23x, ArithmeticInstruction::create, "sub-int"),
    MUL_INT(   (byte) 0x92, FORMAT_23x, ArithmeticInstruction::create, "mul-int"),
    DIV_INT(   (byte) 0x93, FORMAT_23x, ArithmeticInstruction::create, "div-int"),
    REM_INT(   (byte) 0x94, FORMAT_23x, ArithmeticInstruction::create, "rem-int"),
    AND_INT(   (byte) 0x95, FORMAT_23x, ArithmeticInstruction::create, "and-int"),
    OR_INT(    (byte) 0x96, FORMAT_23x, ArithmeticInstruction::create, "or-int"),
    XOR_INT(   (byte) 0x97, FORMAT_23x, ArithmeticInstruction::create, "xor-int"),
    SHL_INT(   (byte) 0x98, FORMAT_23x, ArithmeticInstruction::create, "shl-int"),
    SHR_INT(   (byte) 0x99, FORMAT_23x, ArithmeticInstruction::create, "shr-int"),
    USHR_INT(  (byte) 0x9a, FORMAT_23x, ArithmeticInstruction::create, "ushr-int"),
    ADD_LONG(  (byte) 0x9b, FORMAT_23x, ArithmeticInstruction::create, "add-long",  true),
    SUB_LONG(  (byte) 0x9c, FORMAT_23x, ArithmeticInstruction::create, "sub-long",  true),
    MUL_LONG(  (byte) 0x9d, FORMAT_23x, ArithmeticInstruction::create, "mul-long",  true),
    DIV_LONG(  (byte) 0x9e, FORMAT_23x, ArithmeticInstruction::create, "div-long",  true),
    REM_LONG(  (byte) 0x9f, FORMAT_23x, ArithmeticInstruction::create, "rem-long",  true),
    AND_LONG(  (byte) 0xa0, FORMAT_23x, ArithmeticInstruction::create, "and-long",  true),
    OR_LONG(   (byte) 0xa1, FORMAT_23x, ArithmeticInstruction::create, "or-long",   true),
    XOR_LONG(  (byte) 0xa2, FORMAT_23x, ArithmeticInstruction::create, "xor-long",  true),
    SHL_LONG(  (byte) 0xa3, FORMAT_23x, ArithmeticInstruction::create, "shl-long",  true),
    SHR_LONG(  (byte) 0xa4, FORMAT_23x, ArithmeticInstruction::create, "shr-long",  true),
    USHR_LONG( (byte) 0xa5, FORMAT_23x, ArithmeticInstruction::create, "ushr-long", true),
    ADD_FLOAT( (byte) 0xa6, FORMAT_23x, ArithmeticInstruction::create, "add-float"),
    SUB_FLOAT( (byte) 0xa7, FORMAT_23x, ArithmeticInstruction::create, "sub-float"),
    MUL_FLOAT( (byte) 0xa8, FORMAT_23x, ArithmeticInstruction::create, "mul-float"),
    DIV_FLOAT( (byte) 0xa9, FORMAT_23x, ArithmeticInstruction::create, "div-float"),
    REM_FLOAT( (byte) 0xaa, FORMAT_23x, ArithmeticInstruction::create, "rem-float"),
    ADD_DOUBLE((byte) 0xab, FORMAT_23x, ArithmeticInstruction::create, "add-double", true),
    SUB_DOUBLE((byte) 0xac, FORMAT_23x, ArithmeticInstruction::create, "sub-double", true),
    MUL_DOUBLE((byte) 0xad, FORMAT_23x, ArithmeticInstruction::create, "mul-double", true),
    DIV_DOUBLE((byte) 0xae, FORMAT_23x, ArithmeticInstruction::create, "div-double", true),
    REM_DOUBLE((byte) 0xaf, FORMAT_23x, ArithmeticInstruction::create, "rem-double", true),

    ADD_INT_2ADDR(   (byte) 0xb0, FORMAT_12x, ArithmeticInstruction::create, "add-int/2addr"),
    SUB_INT_2ADDR(   (byte) 0xb1, FORMAT_12x, ArithmeticInstruction::create, "sub-int/2addr"),
    MUL_INT_2ADDR(   (byte) 0xb2, FORMAT_12x, ArithmeticInstruction::create, "mul-int/2addr"),
    DIV_INT_2ADDR(   (byte) 0xb3, FORMAT_12x, ArithmeticInstruction::create, "div-int/2addr"),
    REM_INT_2ADDR(   (byte) 0xb4, FORMAT_12x, ArithmeticInstruction::create, "rem-int/2addr"),
    AND_INT_2ADDR(   (byte) 0xb5, FORMAT_12x, ArithmeticInstruction::create, "and-int/2addr"),
    OR_INT_2ADDR(    (byte) 0xb6, FORMAT_12x, ArithmeticInstruction::create, "or-int/2addr"),
    XOR_INT_2ADDR(   (byte) 0xb7, FORMAT_12x, ArithmeticInstruction::create, "xor-int/2addr"),
    SHL_INT_2ADDR(   (byte) 0xb8, FORMAT_12x, ArithmeticInstruction::create, "shl-int/2addr"),
    SHR_INT_2ADDR(   (byte) 0xb9, FORMAT_12x, ArithmeticInstruction::create, "shr-int/2addr"),
    USHR_INT_2ADDR(  (byte) 0xba, FORMAT_12x, ArithmeticInstruction::create, "ushr-int/2addr"),
    ADD_LONG_2ADDR(  (byte) 0xbb, FORMAT_12x, ArithmeticInstruction::create, "add-long/2addr",  true),
    SUB_LONG_2ADDR(  (byte) 0xbc, FORMAT_12x, ArithmeticInstruction::create, "sub-long/2addr",  true),
    MUL_LONG_2ADDR(  (byte) 0xbd, FORMAT_12x, ArithmeticInstruction::create, "mul-long/2addr",  true),
    DIV_LONG_2ADDR(  (byte) 0xbe, FORMAT_12x, ArithmeticInstruction::create, "div-long/2addr",  true),
    REM_LONG_2ADDR(  (byte) 0xbf, FORMAT_12x, ArithmeticInstruction::create, "rem-long/2addr",  true),
    AND_LONG_2ADDR(  (byte) 0xc0, FORMAT_12x, ArithmeticInstruction::create, "and-long/2addr",  true),
    OR_LONG_2ADDR(   (byte) 0xc1, FORMAT_12x, ArithmeticInstruction::create, "or-long/2addr",   true),
    XOR_LONG_2ADDR(  (byte) 0xc2, FORMAT_12x, ArithmeticInstruction::create, "xor-long/2addr",  true),
    SHL_LONG_2ADDR(  (byte) 0xc3, FORMAT_12x, ArithmeticInstruction::create, "shl-long/2addr",  true),
    SHR_LONG_2ADDR(  (byte) 0xc4, FORMAT_12x, ArithmeticInstruction::create, "shr-long/2addr",  true),
    USHR_LONG_2ADDR( (byte) 0xc5, FORMAT_12x, ArithmeticInstruction::create, "ushr-long/2addr", true),
    ADD_FLOAT_2ADDR( (byte) 0xc6, FORMAT_12x, ArithmeticInstruction::create, "add-float/2addr"),
    SUB_FLOAT_2ADDR( (byte) 0xc7, FORMAT_12x, ArithmeticInstruction::create, "sub-float/2addr"),
    MUL_FLOAT_2ADDR( (byte) 0xc8, FORMAT_12x, ArithmeticInstruction::create, "mul-float/2addr"),
    DIV_FLOAT_2ADDR( (byte) 0xc9, FORMAT_12x, ArithmeticInstruction::create, "div-float/2addr"),
    REM_FLOAT_2ADDR( (byte) 0xca, FORMAT_12x, ArithmeticInstruction::create, "rem-float/2addr"),
    ADD_DOUBLE_2ADDR((byte) 0xcb, FORMAT_12x, ArithmeticInstruction::create, "add-double/2addr", true),
    SUB_DOUBLE_2ADDR((byte) 0xcc, FORMAT_12x, ArithmeticInstruction::create, "sub-double/2addr", true),
    MUL_DOUBLE_2ADDR((byte) 0xcd, FORMAT_12x, ArithmeticInstruction::create, "mul-double/2addr", true),
    DIV_DOUBLE_2ADDR((byte) 0xce, FORMAT_12x, ArithmeticInstruction::create, "div-double/2addr", true),
    REM_DOUBLE_2ADDR((byte) 0xcf, FORMAT_12x, ArithmeticInstruction::create, "rem-double/2addr", true),

    ADD_INT_LIT16((byte) 0xd0, FORMAT_22s, ArithmeticLiteralInstruction::create, "add-int/lit16"),
    RSUB_INT(     (byte) 0xd1, FORMAT_22s, ArithmeticLiteralInstruction::create, "rsub-int"),
    MUL_INT_LIT16((byte) 0xd2, FORMAT_22s, ArithmeticLiteralInstruction::create, "mul-int/lit16"),
    DIV_INT_LIT16((byte) 0xd3, FORMAT_22s, ArithmeticLiteralInstruction::create, "div-int/lit16"),
    REM_INT_LIT16((byte) 0xd4, FORMAT_22s, ArithmeticLiteralInstruction::create, "rem-int/lit16"),
    AND_INT_LIT16((byte) 0xd5, FORMAT_22s, ArithmeticLiteralInstruction::create, "and-int/lit16"),
    OR_INT_LIT16( (byte) 0xd6, FORMAT_22s, ArithmeticLiteralInstruction::create, "or-int/lit16"),
    XOR_INT_LIT16((byte) 0xd7, FORMAT_22s, ArithmeticLiteralInstruction::create, "xor-int/lit16"),

    ADD_INT_LIT8( (byte) 0xd8, FORMAT_22b, ArithmeticLiteralInstruction::create, "add-int/lit8"),
    RSUB_INT_LIT8((byte) 0xd9, FORMAT_22b, ArithmeticLiteralInstruction::create, "rsub-int/lit8"),
    MUL_INT_LIT8( (byte) 0xda, FORMAT_22b, ArithmeticLiteralInstruction::create, "mul-int/lit8"),
    DIV_INT_LIT8( (byte) 0xdb, FORMAT_22b, ArithmeticLiteralInstruction::create, "div-int/lit8"),
    REM_INT_LIT8( (byte) 0xdc, FORMAT_22b, ArithmeticLiteralInstruction::create, "rem-int/lit8"),
    AND_INT_LIT8( (byte) 0xdd, FORMAT_22b, ArithmeticLiteralInstruction::create, "and-int/lit8"),
    OR_INT_LIT8(  (byte) 0xde, FORMAT_22b, ArithmeticLiteralInstruction::create, "or-int/lit8"),
    XOR_INT_LIT8( (byte) 0xdf, FORMAT_22b, ArithmeticLiteralInstruction::create, "xor-int/lit8"),
    SHL_INT_LIT8( (byte) 0xe0, FORMAT_22b, ArithmeticLiteralInstruction::create, "shl-int/lit8"),
    SHR_INT_LIT8( (byte) 0xe1, FORMAT_22b, ArithmeticLiteralInstruction::create, "shr-int/lit8"),
    USHR_INT_LIT8((byte) 0xe2, FORMAT_22b, ArithmeticLiteralInstruction::create, "ushr-int/lit8"),

    // method proto instructions.

    INVOKE_POLYMORPHIC(      (byte) 0xfa, FORMAT_45cc, MethodProtoInstruction::create, "invoke-polymorphic"),
    INVOKE_POLYMORPHIC_RANGE((byte) 0xfb, FORMAT_4rcc, MethodProtoInstruction::create, "invoke-polymorphic/range"),

    // callsite instructions.

    INVOKE_CUSTOM(      (byte) 0xfc, FORMAT_35c, CallSiteInstruction::create, "invoke-custom"),
    INVOKE_CUSTOM_RANGE((byte) 0xfd, FORMAT_3rc, CallSiteInstruction::create, "invoke-custom/range"),

    // method handle and proto instructions.

    CONST_METHOD_HANDLE((byte) 0xfe, FORMAT_21c, MethodHandleRefInstruction::create, "const-method-handle"),
    CONST_METHOD_TYPE(  (byte) 0xff, FORMAT_21c, MethodTypeRefInstruction::create,   "const-method-type");


    private final byte                 opCode;
    private final boolean              wide;
    private final DexInstructionFormat format;
    private final InstructionSupplier  supplier;
    private final String               mnemonic;

    private static final DexOpCode[] opcodes;

    private static final Map<String, DexOpCode> mnemonicToOpCodeMapping;

    static
    {
        opcodes = new DexOpCode[0x100];
        for (DexOpCode opCode : values())
        {
            opcodes[opCode.opCode & 0xff] = opCode;
        }

        mnemonicToOpCodeMapping = new HashMap<>();
        for (DexOpCode opCode : values()) {
            mnemonicToOpCodeMapping.put(opCode.mnemonic, opCode);
        }
    }

    DexOpCode(byte opcode, DexInstructionFormat format, InstructionSupplier supplier, String mnemonic) {
        this(opcode, format, supplier, mnemonic, false);
    }

    DexOpCode(byte opcode, DexInstructionFormat format, InstructionSupplier supplier, String mnemonic, boolean wide) {
        this.opCode   = opcode;
        this.format   = format;
        this.supplier = supplier;
        this.mnemonic = mnemonic;
        this.wide     = wide;
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

    public boolean targetsWideRegister() {
        return wide;
    }

    public DexInstruction createInstruction(byte ident) {
        return supplier.get(this, ident);
    }

    public static DexOpCode get(byte opcode)
    {
        return opcodes[opcode & 0xff];
    }

    public static DexOpCode get(String mnemonic) {
        return mnemonicToOpCodeMapping.get(mnemonic);
    }

    @FunctionalInterface
    private interface InstructionSupplier {
        DexInstruction get(DexOpCode opCode, byte ident);
    }
}

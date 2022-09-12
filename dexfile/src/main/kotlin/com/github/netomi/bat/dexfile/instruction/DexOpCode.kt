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
package com.github.netomi.bat.dexfile.instruction

import com.github.netomi.bat.dexfile.DexFormat
import com.github.netomi.bat.util.toHexStringWithPrefix

enum class DexOpCode constructor(
            val value:     Int,
            val format:    InstructionFormat,
    private val supplier:  InstructionSupplier?,
            val mnemonic:  String,
            val minFormat: DexFormat = DexFormat.FORMAT_009,
    private val wide:      Boolean = false) {

    // nop instructions.
    NOP               (0x00, InstructionFormat.FORMAT_10x, { NopInstruction.create(it) }, "nop"),

    // move instructions.
    MOVE              (0x01, InstructionFormat.FORMAT_12x, { MoveInstruction.create(it) }, "move"),
    MOVE_FROM16       (0x02, InstructionFormat.FORMAT_22x, { MoveInstruction.create(it) }, "move/from16"),
    MOVE_16           (0x03, InstructionFormat.FORMAT_32x, { MoveInstruction.create(it) }, "move/16"),
    MOVE_WIDE         (0x04, InstructionFormat.FORMAT_12x, { MoveInstruction.create(it) }, "move-wide", wide = true),
    MOVE_WIDE_FROM16  (0x05, InstructionFormat.FORMAT_22x, { MoveInstruction.create(it) }, "move-wide/from16", wide = true),
    MOVE_WIDE_16      (0x06, InstructionFormat.FORMAT_32x, { MoveInstruction.create(it) }, "move-wide/16", wide = true),
    MOVE_OBJECT       (0x07, InstructionFormat.FORMAT_12x, { MoveInstruction.create(it) }, "move-object"),
    MOVE_OBJECT_FROM16(0x08, InstructionFormat.FORMAT_22x, { MoveInstruction.create(it) }, "move-object/from16"),
    MOVE_OBJECT_16    (0x09, InstructionFormat.FORMAT_32x, { MoveInstruction.create(it) }, "move-object/16"),
    MOVE_RESULT       (0x0a, InstructionFormat.FORMAT_11x, { MoveInstruction.create(it) }, "move-result"),
    MOVE_RESULT_WIDE  (0x0b, InstructionFormat.FORMAT_11x, { MoveInstruction.create(it) }, "move-result-wide", wide = true),
    MOVE_RESULT_OBJECT(0x0c, InstructionFormat.FORMAT_11x, { MoveInstruction.create(it) }, "move-result-object"),
    MOVE_EXCEPTION    (0x0d, InstructionFormat.FORMAT_11x, { MoveInstruction.create(it) }, "move-exception"),

    // return instructions.
    RETURN_VOID       (0x0e, InstructionFormat.FORMAT_10x, { ReturnInstruction.create(it) }, "return-void"),
    RETURN            (0x0f, InstructionFormat.FORMAT_11x, { ReturnInstruction.create(it) }, "return"),
    RETURN_WIDE       (0x10, InstructionFormat.FORMAT_11x, { ReturnInstruction.create(it) }, "return-wide", wide = true),
    RETURN_OBJECT     (0x11, InstructionFormat.FORMAT_11x, { ReturnInstruction.create(it) }, "return-object"),

    // literal instructions.
    CONST_4          (0x12, InstructionFormat.FORMAT_11n, { LiteralInstruction.create(it) }, "const/4"),
    CONST_16         (0x13, InstructionFormat.FORMAT_21s, { LiteralInstruction.create(it) }, "const/16"),
    CONST            (0x14, InstructionFormat.FORMAT_31i, { LiteralInstruction.create(it) }, "const"),
    CONST_HIGH16     (0x15, InstructionFormat.FORMAT_21h, { LiteralInstruction.create(it) }, "const/high16"),
    CONST_WIDE_16    (0x16, InstructionFormat.FORMAT_21s, { LiteralInstruction.create(it) }, "const-wide/16", wide = true),
    CONST_WIDE_32    (0x17, InstructionFormat.FORMAT_31i, { LiteralInstruction.create(it) }, "const-wide/32", wide = true),
    CONST_WIDE       (0x18, InstructionFormat.FORMAT_51l, { LiteralInstruction.create(it) }, "const-wide", wide = true),
    CONST_WIDE_HIGH16(0x19, InstructionFormat.FORMAT_21h, { LiteralInstruction.create(it) }, "const-wide/high16", wide = true),

    // string instructions.
    CONST_STRING      (0x1a, InstructionFormat.FORMAT_21c, { StringInstruction.create(it) }, "const-string"),
    CONST_STRING_JUMBO(0x1b, InstructionFormat.FORMAT_31c, { StringInstruction.create(it) }, "const-string/jumbo"),

    // type instructions.
    CONST_CLASS(0x1c, InstructionFormat.FORMAT_21c, { TypeInstruction.create(it) }, "const-class"),

    // monitor instructions.
    MONITOR_ENTER(0x1d, InstructionFormat.FORMAT_11x, { MonitorInstruction.create(it) }, "monitor-enter"),
    MONITOR_EXIT (0x1e, InstructionFormat.FORMAT_11x, { MonitorInstruction.create(it) }, "monitor-exit"),

    // type instructions.
    CHECK_CAST (0x1f, InstructionFormat.FORMAT_21c, { TypeInstruction.create(it) }, "check-cast"),
    INSTANCE_OF(0x20, InstructionFormat.FORMAT_22c, { TypeInstruction.create(it) }, "instance-of"),

    // array instructions.
    ARRAY_LENGTH(0x21, InstructionFormat.FORMAT_12x, { ArrayInstruction.create(it) }, "array-length"),

    // type instructions.
    NEW_INSTANCE(0x22, InstructionFormat.FORMAT_21c, { TypeInstruction.create(it) }, "new-instance"),
    NEW_ARRAY   (0x23, InstructionFormat.FORMAT_22c, { TypeInstruction.create(it) }, "new-array"),

    // array type instructions.
    FILLED_NEW_ARRAY      (0x24, InstructionFormat.FORMAT_35c, { ArrayTypeInstruction.create(it) }, "filled-new-array"),
    FILLED_NEW_ARRAY_RANGE(0x25, InstructionFormat.FORMAT_3rc, { ArrayTypeInstruction.create(it) }, "filled-new-array/range"),

    // payload instruction.
    FILL_ARRAY_DATA(0x26, InstructionFormat.FORMAT_31t, { FillArrayDataInstruction.create(it) }, "fill-array-data"),

    // exception instruction.
    THROW(0x27, InstructionFormat.FORMAT_11x, { ExceptionInstruction.create(it) }, "throw"),

    // branch instructions.
    GOTO   (0x28, InstructionFormat.FORMAT_10t, { BranchInstruction.create(it) }, "goto"),
    GOTO_16(0x29, InstructionFormat.FORMAT_20t, { BranchInstruction.create(it) }, "goto/16"),
    GOTO_32(0x2a, InstructionFormat.FORMAT_30t, { BranchInstruction.create(it) }, "goto/32"),

    // payload instructions.
    PACKED_SWITCH(0x2b, InstructionFormat.FORMAT_31t, { PackedSwitchInstruction.create(it) }, "packed-switch"),
    SPARSE_SWITCH(0x2c, InstructionFormat.FORMAT_31t, { SparseSwitchInstruction.create(it) }, "sparse-switch"),

    // compare instructions.
    CMPL_FLOAT (0x2d, InstructionFormat.FORMAT_23x, { CompareInstruction.create(it) }, "cmpl-float"),
    CMPG_FLOAT (0x2e, InstructionFormat.FORMAT_23x, { CompareInstruction.create(it) }, "cmpg-float"),
    CMPL_DOUBLE(0x2f, InstructionFormat.FORMAT_23x, { CompareInstruction.create(it) }, "cmpl-double", wide = true),
    CMPG_DOUBLE(0x30, InstructionFormat.FORMAT_23x, { CompareInstruction.create(it) }, "cmpg-double", wide = true),
    CMP_LONG   (0x31, InstructionFormat.FORMAT_23x, { CompareInstruction.create(it) }, "cmp-long",    wide = true),

    // branch instructions.
    IF_EQ (0x32, InstructionFormat.FORMAT_22t, { BranchInstruction.create(it) }, "if-eq"),
    IF_NE (0x33, InstructionFormat.FORMAT_22t, { BranchInstruction.create(it) }, "if-ne"),
    IF_LT (0x34, InstructionFormat.FORMAT_22t, { BranchInstruction.create(it) }, "if-lt"),
    IF_GE (0x35, InstructionFormat.FORMAT_22t, { BranchInstruction.create(it) }, "if-ge"),
    IF_GT (0x36, InstructionFormat.FORMAT_22t, { BranchInstruction.create(it) }, "if-gt"),
    IF_LE (0x37, InstructionFormat.FORMAT_22t, { BranchInstruction.create(it) }, "if-le"),
    IF_EQZ(0x38, InstructionFormat.FORMAT_21t, { BranchInstruction.create(it) }, "if-eqz"),
    IF_NEZ(0x39, InstructionFormat.FORMAT_21t, { BranchInstruction.create(it) }, "if-nez"),
    IF_LTZ(0x3a, InstructionFormat.FORMAT_21t, { BranchInstruction.create(it) }, "if-ltz"),
    IF_GEZ(0x3b, InstructionFormat.FORMAT_21t, { BranchInstruction.create(it) }, "if-gez"),
    IF_GTZ(0x3c, InstructionFormat.FORMAT_21t, { BranchInstruction.create(it) }, "if-gtz"),
    IF_LEZ(0x3d, InstructionFormat.FORMAT_21t, { BranchInstruction.create(it) }, "if-lez"),

    // array instructions.
    AGET        (0x44, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aget"),
    AGET_WIDE   (0x45, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aget-wide", wide = true),
    AGET_OBJECT (0x46, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aget-object"),
    AGET_BOOLEAN(0x47, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aget-boolean"),
    AGET_BYTE   (0x48, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aget-byte"),
    AGET_CHAR   (0x49, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aget-char"),
    AGET_SHORT  (0x4a, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aget-short"),
    APUT        (0x4b, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aput"),
    APUT_WIDE   (0x4c, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aput-wide", wide = true),
    APUT_OBJECT (0x4d, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aput-object"),
    APUT_BOOLEAN(0x4e, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aput-boolean"),
    APUT_BYTE   (0x4f, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aput-byte"),
    APUT_CHAR   (0x50, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aput-char"),
    APUT_SHORT  (0x51, InstructionFormat.FORMAT_23x, { ArrayInstruction.create(it) }, "aput-short"),

    // field instructions.
    IGET        (0x52, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iget"),
    IGET_WIDE   (0x53, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iget-wide", wide = true),
    IGET_OBJECT (0x54, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iget-object"),
    IGET_BOOLEAN(0x55, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iget-boolean"),
    IGET_BYTE   (0x56, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iget-byte"),
    IGET_CHAR   (0x57, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iget-char"),
    IGET_SHORT  (0x58, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iget-short"),
    IPUT        (0x59, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iput"),
    IPUT_WIDE   (0x5a, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iput-wide", wide = true),
    IPUT_OBJECT (0x5b, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iput-object"),
    IPUT_BOOLEAN(0x5c, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iput-boolean"),
    IPUT_BYTE   (0x5d, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iput-byte"),
    IPUT_CHAR   (0x5e, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iput-char"),
    IPUT_SHORT  (0x5f, InstructionFormat.FORMAT_22c, { FieldInstruction.create(it) }, "iput-short"),
    SGET        (0x60, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sget"),
    SGET_WIDE   (0x61, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sget-wide", wide = true),
    SGET_OBJECT (0x62, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sget-object"),
    SGET_BOOLEAN(0x63, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sget-boolean"),
    SGET_BYTE   (0x64, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sget-byte"),
    SGET_CHAR   (0x65, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sget-char"),
    SGET_SHORT  (0x66, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sget-short"),
    SPUT        (0x67, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sput"),
    SPUT_WIDE   (0x68, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sput-wide", wide = true),
    SPUT_OBJECT (0x69, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sput-object"),
    SPUT_BOOLEAN(0x6a, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sput-boolean"),
    SPUT_BYTE   (0x6b, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sput-byte"),
    SPUT_CHAR   (0x6c, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sput-char"),
    SPUT_SHORT  (0x6d, InstructionFormat.FORMAT_21c, { FieldInstruction.create(it) }, "sput-short"),

    // method instructions.
    INVOKE_VIRTUAL        (0x6e, InstructionFormat.FORMAT_35c, { MethodInstruction.create(it) }, "invoke-virtual"),
    INVOKE_SUPER          (0x6f, InstructionFormat.FORMAT_35c, { MethodInstruction.create(it) }, "invoke-super"),
    INVOKE_DIRECT         (0x70, InstructionFormat.FORMAT_35c, { MethodInstruction.create(it) }, "invoke-direct"),
    INVOKE_STATIC         (0x71, InstructionFormat.FORMAT_35c, { MethodInstruction.create(it) }, "invoke-static"),
    INVOKE_INTERFACE      (0x72, InstructionFormat.FORMAT_35c, { MethodInstruction.create(it) }, "invoke-interface"),
    INVOKE_VIRTUAL_RANGE  (0x74, InstructionFormat.FORMAT_3rc, { MethodInstruction.create(it) }, "invoke-virtual/range"),
    INVOKE_SUPER_RANGE    (0x75, InstructionFormat.FORMAT_3rc, { MethodInstruction.create(it) }, "invoke-super/range"),
    INVOKE_DIRECT_RANGE   (0x76, InstructionFormat.FORMAT_3rc, { MethodInstruction.create(it) }, "invoke-direct/range"),
    INVOKE_STATIC_RANGE   (0x77, InstructionFormat.FORMAT_3rc, { MethodInstruction.create(it) }, "invoke-static/range"),
    INVOKE_INTERFACE_RANGE(0x78, InstructionFormat.FORMAT_3rc, { MethodInstruction.create(it) }, "invoke-interface/range"),

    // arithmetic instructions.
    NEG_INT   (0x7b, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "neg-int"),
    NOT_INT   (0x7c, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "not-int"),
    NEG_LONG  (0x7d, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "neg-long",   wide = true),
    NOT_LONG  (0x7e, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "not-long",   wide = true),
    NEG_FLOAT (0x7f, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "neg-float"),
    NEG_DOUBLE(0x80, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "neg-double", wide = true),

    // conversion instructions.
    INT_TO_LONG    (0x81, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "int-to-long",     wide = true),
    INT_TO_FLOAT   (0x82, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "int-to-float"),
    INT_TO_DOUBLE  (0x83, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "int-to-double",   wide = true),
    LONG_TO_INT    (0x84, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "long-to-int",     wide = true),
    LONG_TO_FLOAT  (0x85, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "long-to-float",   wide = true),
    LONG_TO_DOUBLE (0x86, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "long-to-double",  wide = true),
    FLOAT_TO_INT   (0x87, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "float-to-int"),
    FLOAT_TO_LONG  (0x88, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "float-to-long",   wide = true),
    FLOAT_TO_DOUBLE(0x89, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "float-to-double", wide = true),
    DOUBLE_TO_INT  (0x8a, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "double-to-int",   wide = true),
    DOUBLE_TO_LONG (0x8b, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "double-to-long",  wide = true),
    DOUBLE_TO_FLOAT(0x8c, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "double-to-float", wide = true),
    INT_TO_BYTE    (0x8d, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "int-to-byte"),
    INT_TO_CHAR    (0x8e, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "int-to-char"),
    INT_TO_SHORT   (0x8f, InstructionFormat.FORMAT_12x, { ConversionInstruction.create(it) }, "int-to-short"),

    // arithmetic instructions.
    ADD_INT         (0x90, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "add-int"),
    SUB_INT         (0x91, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "sub-int"),
    MUL_INT         (0x92, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "mul-int"),
    DIV_INT         (0x93, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "div-int"),
    REM_INT         (0x94, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "rem-int"),
    AND_INT         (0x95, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "and-int"),
    OR_INT          (0x96, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "or-int"),
    XOR_INT         (0x97, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "xor-int"),
    SHL_INT         (0x98, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "shl-int"),
    SHR_INT         (0x99, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "shr-int"),
    USHR_INT        (0x9a, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "ushr-int"),
    ADD_LONG        (0x9b, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "add-long",  wide = true),
    SUB_LONG        (0x9c, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "sub-long",  wide = true),
    MUL_LONG        (0x9d, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "mul-long",  wide = true),
    DIV_LONG        (0x9e, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "div-long",  wide = true),
    REM_LONG        (0x9f, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "rem-long",  wide = true),
    AND_LONG        (0xa0, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "and-long",  wide = true),
    OR_LONG         (0xa1, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "or-long",   wide = true),
    XOR_LONG        (0xa2, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "xor-long",  wide = true),
    SHL_LONG        (0xa3, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "shl-long",  wide = true),
    SHR_LONG        (0xa4, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "shr-long",  wide = true),
    USHR_LONG       (0xa5, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "ushr-long", wide = true),
    ADD_FLOAT       (0xa6, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "add-float"),
    SUB_FLOAT       (0xa7, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "sub-float"),
    MUL_FLOAT       (0xa8, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "mul-float"),
    DIV_FLOAT       (0xa9, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "div-float"),
    REM_FLOAT       (0xaa, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "rem-float"),
    ADD_DOUBLE      (0xab, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "add-double", wide = true),
    SUB_DOUBLE      (0xac, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "sub-double", wide = true),
    MUL_DOUBLE      (0xad, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "mul-double", wide = true),
    DIV_DOUBLE      (0xae, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "div-double", wide = true),
    REM_DOUBLE      (0xaf, InstructionFormat.FORMAT_23x, { ArithmeticInstruction.create(it) }, "rem-double", wide = true),
    ADD_INT_2ADDR   (0xb0, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "add-int/2addr"),
    SUB_INT_2ADDR   (0xb1, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "sub-int/2addr"),
    MUL_INT_2ADDR   (0xb2, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "mul-int/2addr"),
    DIV_INT_2ADDR   (0xb3, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "div-int/2addr"),
    REM_INT_2ADDR   (0xb4, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "rem-int/2addr"),
    AND_INT_2ADDR   (0xb5, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "and-int/2addr"),
    OR_INT_2ADDR    (0xb6, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "or-int/2addr"),
    XOR_INT_2ADDR   (0xb7, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "xor-int/2addr"),
    SHL_INT_2ADDR   (0xb8, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "shl-int/2addr"),
    SHR_INT_2ADDR   (0xb9, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "shr-int/2addr"),
    USHR_INT_2ADDR  (0xba, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "ushr-int/2addr"),
    ADD_LONG_2ADDR  (0xbb, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "add-long/2addr",  wide = true),
    SUB_LONG_2ADDR  (0xbc, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "sub-long/2addr",  wide = true),
    MUL_LONG_2ADDR  (0xbd, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "mul-long/2addr",  wide = true),
    DIV_LONG_2ADDR  (0xbe, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "div-long/2addr",  wide = true),
    REM_LONG_2ADDR  (0xbf, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "rem-long/2addr",  wide = true),
    AND_LONG_2ADDR  (0xc0, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "and-long/2addr",  wide = true),
    OR_LONG_2ADDR   (0xc1, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "or-long/2addr",   wide = true),
    XOR_LONG_2ADDR  (0xc2, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "xor-long/2addr",  wide = true),
    SHL_LONG_2ADDR  (0xc3, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "shl-long/2addr",  wide = true),
    SHR_LONG_2ADDR  (0xc4, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "shr-long/2addr",  wide = true),
    USHR_LONG_2ADDR (0xc5, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "ushr-long/2addr", wide = true),
    ADD_FLOAT_2ADDR (0xc6, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "add-float/2addr"),
    SUB_FLOAT_2ADDR (0xc7, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "sub-float/2addr"),
    MUL_FLOAT_2ADDR (0xc8, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "mul-float/2addr"),
    DIV_FLOAT_2ADDR (0xc9, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "div-float/2addr"),
    REM_FLOAT_2ADDR (0xca, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "rem-float/2addr"),
    ADD_DOUBLE_2ADDR(0xcb, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "add-double/2addr", wide = true),
    SUB_DOUBLE_2ADDR(0xcc, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "sub-double/2addr", wide = true),
    MUL_DOUBLE_2ADDR(0xcd, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "mul-double/2addr", wide = true),
    DIV_DOUBLE_2ADDR(0xce, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "div-double/2addr", wide = true),
    REM_DOUBLE_2ADDR(0xcf, InstructionFormat.FORMAT_12x, { ArithmeticInstruction.create(it) }, "rem-double/2addr", wide = true),
    ADD_INT_LIT16   (0xd0, InstructionFormat.FORMAT_22s, { ArithmeticLiteralInstruction.create(it) }, "add-int/lit16"),
    RSUB_INT        (0xd1, InstructionFormat.FORMAT_22s, { ArithmeticLiteralInstruction.create(it) }, "rsub-int"),
    MUL_INT_LIT16   (0xd2, InstructionFormat.FORMAT_22s, { ArithmeticLiteralInstruction.create(it) }, "mul-int/lit16"),
    DIV_INT_LIT16   (0xd3, InstructionFormat.FORMAT_22s, { ArithmeticLiteralInstruction.create(it) }, "div-int/lit16"),
    REM_INT_LIT16   (0xd4, InstructionFormat.FORMAT_22s, { ArithmeticLiteralInstruction.create(it) }, "rem-int/lit16"),
    AND_INT_LIT16   (0xd5, InstructionFormat.FORMAT_22s, { ArithmeticLiteralInstruction.create(it) }, "and-int/lit16"),
    OR_INT_LIT16    (0xd6, InstructionFormat.FORMAT_22s, { ArithmeticLiteralInstruction.create(it) }, "or-int/lit16"),
    XOR_INT_LIT16   (0xd7, InstructionFormat.FORMAT_22s, { ArithmeticLiteralInstruction.create(it) }, "xor-int/lit16"),
    ADD_INT_LIT8    (0xd8, InstructionFormat.FORMAT_22b, { ArithmeticLiteralInstruction.create(it) }, "add-int/lit8"),
    RSUB_INT_LIT8   (0xd9, InstructionFormat.FORMAT_22b, { ArithmeticLiteralInstruction.create(it) }, "rsub-int/lit8"),
    MUL_INT_LIT8    (0xda, InstructionFormat.FORMAT_22b, { ArithmeticLiteralInstruction.create(it) }, "mul-int/lit8"),
    DIV_INT_LIT8    (0xdb, InstructionFormat.FORMAT_22b, { ArithmeticLiteralInstruction.create(it) }, "div-int/lit8"),
    REM_INT_LIT8    (0xdc, InstructionFormat.FORMAT_22b, { ArithmeticLiteralInstruction.create(it) }, "rem-int/lit8"),
    AND_INT_LIT8    (0xdd, InstructionFormat.FORMAT_22b, { ArithmeticLiteralInstruction.create(it) }, "and-int/lit8"),
    OR_INT_LIT8     (0xde, InstructionFormat.FORMAT_22b, { ArithmeticLiteralInstruction.create(it) }, "or-int/lit8"),
    XOR_INT_LIT8    (0xdf, InstructionFormat.FORMAT_22b, { ArithmeticLiteralInstruction.create(it) }, "xor-int/lit8"),
    SHL_INT_LIT8    (0xe0, InstructionFormat.FORMAT_22b, { ArithmeticLiteralInstruction.create(it) }, "shl-int/lit8"),
    SHR_INT_LIT8    (0xe1, InstructionFormat.FORMAT_22b, { ArithmeticLiteralInstruction.create(it) }, "shr-int/lit8"),
    USHR_INT_LIT8   (0xe2, InstructionFormat.FORMAT_22b, { ArithmeticLiteralInstruction.create(it) }, "ushr-int/lit8"),

    // method proto instructions.
    INVOKE_POLYMORPHIC      (0xfa, InstructionFormat.FORMAT_45cc, { MethodProtoInstruction.create(it) }, "invoke-polymorphic",       DexFormat.FORMAT_038),
    INVOKE_POLYMORPHIC_RANGE(0xfb, InstructionFormat.FORMAT_4rcc, { MethodProtoInstruction.create(it) }, "invoke-polymorphic/range", DexFormat.FORMAT_038),

    // callsite instructions.
    INVOKE_CUSTOM      (0xfc, InstructionFormat.FORMAT_35c, { CallSiteInstruction.create(it) }, "invoke-custom",       DexFormat.FORMAT_038),
    INVOKE_CUSTOM_RANGE(0xfd, InstructionFormat.FORMAT_3rc, { CallSiteInstruction.create(it) }, "invoke-custom/range", DexFormat.FORMAT_038),

    // method handle and proto instructions.
    CONST_METHOD_HANDLE(0xfe, InstructionFormat.FORMAT_21c, { MethodHandleRefInstruction.create(it) }, "const-method-handle", DexFormat.FORMAT_039),
    CONST_METHOD_TYPE  (0xff, InstructionFormat.FORMAT_21c, { MethodTypeRefInstruction.create(it) }, "const-method-type",     DexFormat.FORMAT_039),

    // for internal use only
    INTERNAL_LABEL(0x100, InstructionFormat.FORMAT_00x, null, "label");

    val length: Int
        get() = format.instructionLength

    val arguments: Int
        get() = format.argumentCount

    val targetsWideRegister: Boolean
        get() = wide

    fun createInstruction(): DexInstruction {
        return supplier?.create(this) ?: throw RuntimeException("failed to create instruction for opcode $this")
    }

    private fun interface InstructionSupplier {
        fun create(opCode: DexOpCode): DexInstruction
    }

    companion object {
        private val opcodeArray:             Array<DexOpCode?>             = arrayOfNulls(0x100)
        private val mnemonicToOpCodeMapping: MutableMap<String, DexOpCode> = hashMapOf()

        init {
            // ignore pseudo opcodes
            for (opCode in values().filter { it.value <= 0xff && it.format != InstructionFormat.FORMAT_00x }) {
                opcodeArray[opCode.value]                = opCode
                mnemonicToOpCodeMapping[opCode.mnemonic] = opCode
            }
        }

        fun isValidMnemonic(mnemonic: String): Boolean {
            return mnemonicToOpCodeMapping[mnemonic] != null
        }

        operator fun get(opcode: Byte): DexOpCode {
            return opcodeArray[opcode.toInt() and 0xff] ?: throw IllegalArgumentException("unknown opcode ${toHexStringWithPrefix(opcode)}")
        }

        operator fun get(mnemonic: String): DexOpCode {
            return mnemonicToOpCodeMapping[mnemonic] ?: throw IllegalArgumentException("unknown mnemonic $mnemonic")
        }
    }
}
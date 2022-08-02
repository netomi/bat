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
            val opCode:    Int,
            val format:    InstructionFormat,
    private val supplier:  InstructionSupplier?,
            val mnemonic:  String,
            val minFormat: DexFormat = DexFormat.FORMAT_009,
    private val wide:      Boolean = false) {

    // basic instructions.
    NOP               (0x00, InstructionFormat.FORMAT_10x, BasicInstruction::create, "nop"),
    MOVE              (0x01, InstructionFormat.FORMAT_12x, BasicInstruction::create, "move"),
    MOVE_FROM16       (0x02, InstructionFormat.FORMAT_22x, BasicInstruction::create, "move/from16"),
    MOVE_16           (0x03, InstructionFormat.FORMAT_32x, BasicInstruction::create, "move/16"),
    MOVE_WIDE         (0x04, InstructionFormat.FORMAT_12x, BasicInstruction::create, "move-wide", wide = true),
    MOVE_WIDE_FROM16  (0x05, InstructionFormat.FORMAT_22x, BasicInstruction::create, "move-wide/from16", wide = true),
    MOVE_WIDE_16      (0x06, InstructionFormat.FORMAT_32x, BasicInstruction::create, "move-wide/16", wide = true),
    MOVE_OBJECT       (0x07, InstructionFormat.FORMAT_12x, BasicInstruction::create, "move-object"),
    MOVE_OBJECT_FROM16(0x08, InstructionFormat.FORMAT_22x, BasicInstruction::create, "move-object/from16"),
    MOVE_OBJECT_16    (0x09, InstructionFormat.FORMAT_32x, BasicInstruction::create, "move-object/16"),
    MOVE_RESULT       (0x0a, InstructionFormat.FORMAT_11x, BasicInstruction::create, "move-result"),
    MOVE_RESULT_WIDE  (0x0b, InstructionFormat.FORMAT_11x, BasicInstruction::create, "move-result-wide", wide = true),
    MOVE_RESULT_OBJECT(0x0c, InstructionFormat.FORMAT_11x, BasicInstruction::create, "move-result-object"),
    MOVE_EXCEPTION    (0x0d, InstructionFormat.FORMAT_11x, BasicInstruction::create, "move-exception"),
    RETURN_VOID       (0x0e, InstructionFormat.FORMAT_10x, BasicInstruction::create, "return-void"),
    RETURN            (0x0f, InstructionFormat.FORMAT_11x, BasicInstruction::create, "return"),
    RETURN_WIDE       (0x10, InstructionFormat.FORMAT_11x, BasicInstruction::create, "return-wide", wide = true),
    RETURN_OBJECT     (0x11, InstructionFormat.FORMAT_11x, BasicInstruction::create, "return-object"),

    // literal instructions.
    CONST_4          (0x12, InstructionFormat.FORMAT_11n, LiteralInstruction::create, "const/4"),
    CONST_16         (0x13, InstructionFormat.FORMAT_21s, LiteralInstruction::create, "const/16"),
    CONST            (0x14, InstructionFormat.FORMAT_31i, LiteralInstruction::create, "const"),
    CONST_HIGH16     (0x15, InstructionFormat.FORMAT_21h, LiteralInstruction::create, "const/high16"),
    CONST_WIDE_16    (0x16, InstructionFormat.FORMAT_21s, LiteralInstruction::create, "const-wide/16", wide = true),
    CONST_WIDE_32    (0x17, InstructionFormat.FORMAT_31i, LiteralInstruction::create, "const-wide/32", wide = true),
    CONST_WIDE       (0x18, InstructionFormat.FORMAT_51l, LiteralInstruction::create, "const-wide", wide = true),
    CONST_WIDE_HIGH16(0x19, InstructionFormat.FORMAT_21h, LiteralInstruction::create, "const-wide/high16", wide = true),

    // string instructions.
    CONST_STRING      (0x1a, InstructionFormat.FORMAT_21c, StringInstruction::create, "const-string"),
    CONST_STRING_JUMBO(0x1b, InstructionFormat.FORMAT_31c, StringInstruction::create, "const-string/jumbo"),

    // type instructions.
    CONST_CLASS(0x1c, InstructionFormat.FORMAT_21c, TypeInstruction::create, "const-class"),

    // basic instructions.
    MONITOR_ENTER(0x1d, InstructionFormat.FORMAT_11x, BasicInstruction::create, "monitor-enter"),
    MONITOR_EXIT (0x1e, InstructionFormat.FORMAT_11x, BasicInstruction::create, "monitor-exit"),

    // type instructions.
    CHECK_CAST (0x1f, InstructionFormat.FORMAT_21c, TypeInstruction::create, "check-cast"),
    INSTANCE_OF(0x20, InstructionFormat.FORMAT_22c, TypeInstruction::create, "instance-of"),

    // array instructions.
    ARRAY_LENGTH(0x21, InstructionFormat.FORMAT_12x, ArrayInstruction::create, "array-length"),

    // type instructions.
    NEW_INSTANCE(0x22, InstructionFormat.FORMAT_21c, TypeInstruction::create, "new-instance"),
    NEW_ARRAY   (0x23, InstructionFormat.FORMAT_22c, TypeInstruction::create, "new-array"),

    // array type instructions.
    FILLED_NEW_ARRAY      (0x24, InstructionFormat.FORMAT_35c, ArrayTypeInstruction::create, "filled-new-array"),
    FILLED_NEW_ARRAY_RANGE(0x25, InstructionFormat.FORMAT_3rc, ArrayTypeInstruction::create, "filled-new-array/range"),

    // payload instruction.
    FILL_ARRAY_DATA(0x26, InstructionFormat.FORMAT_31t, FillArrayDataInstruction::create, "fill-array-data"),

    // basic instruction.
    THROW(0x27, InstructionFormat.FORMAT_11x, BasicInstruction::create, "throw"),

    // branch instructions.
    GOTO   (0x28, InstructionFormat.FORMAT_10t, BranchInstruction::create, "goto"),
    GOTO_16(0x29, InstructionFormat.FORMAT_20t, BranchInstruction::create, "goto/16"),
    GOTO_32(0x2a, InstructionFormat.FORMAT_30t, BranchInstruction::create, "goto/32"),

    // payload instructions.
    PACKED_SWITCH(0x2b, InstructionFormat.FORMAT_31t, PackedSwitchInstruction::create, "packed-switch"),
    SPARSE_SWITCH(0x2c, InstructionFormat.FORMAT_31t, SparseSwitchInstruction::create, "sparse-switch"),

    // basic instructions.
    CMPL_FLOAT (0x2d, InstructionFormat.FORMAT_23x, BasicInstruction::create, "cmpl-float"),
    CMPG_FLOAT (0x2e, InstructionFormat.FORMAT_23x, BasicInstruction::create, "cmpg-float"),
    CMPL_DOUBLE(0x2f, InstructionFormat.FORMAT_23x, BasicInstruction::create, "cmpl-double", wide = true),
    CMPG_DOUBLE(0x30, InstructionFormat.FORMAT_23x, BasicInstruction::create, "cmpg-double", wide = true),
    CMP_LONG   (0x31, InstructionFormat.FORMAT_23x, BasicInstruction::create, "cmp-long",    wide = true),

    // branch instructions.
    IF_EQ (0x32, InstructionFormat.FORMAT_22t, BranchInstruction::create, "if-eq"),
    IF_NE (0x33, InstructionFormat.FORMAT_22t, BranchInstruction::create, "if-ne"),
    IF_LT (0x34, InstructionFormat.FORMAT_22t, BranchInstruction::create, "if-lt"),
    IF_GE (0x35, InstructionFormat.FORMAT_22t, BranchInstruction::create, "if-ge"),
    IF_GT (0x36, InstructionFormat.FORMAT_22t, BranchInstruction::create, "if-gt"),
    IF_LE (0x37, InstructionFormat.FORMAT_22t, BranchInstruction::create, "if-le"),
    IF_EQZ(0x38, InstructionFormat.FORMAT_21t, BranchInstruction::create, "if-eqz"),
    IF_NEZ(0x39, InstructionFormat.FORMAT_21t, BranchInstruction::create, "if-nez"),
    IF_LTZ(0x3a, InstructionFormat.FORMAT_21t, BranchInstruction::create, "if-ltz"),
    IF_GEZ(0x3b, InstructionFormat.FORMAT_21t, BranchInstruction::create, "if-gez"),
    IF_GTZ(0x3c, InstructionFormat.FORMAT_21t, BranchInstruction::create, "if-gtz"),
    IF_LEZ(0x3d, InstructionFormat.FORMAT_21t, BranchInstruction::create, "if-lez"),

    // array instructions.
    AGET        (0x44, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget"),
    AGET_WIDE   (0x45, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-wide", wide = true),
    AGET_OBJECT (0x46, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-object"),
    AGET_BOOLEAN(0x47, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-boolean"),
    AGET_BYTE   (0x48, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-byte"),
    AGET_CHAR   (0x49, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-char"),
    AGET_SHORT  (0x4a, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aget-short"),
    APUT        (0x4b, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput"),
    APUT_WIDE   (0x4c, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-wide", wide = true),
    APUT_OBJECT (0x4d, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-object"),
    APUT_BOOLEAN(0x4e, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-boolean"),
    APUT_BYTE   (0x4f, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-byte"),
    APUT_CHAR   (0x50, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-char"),
    APUT_SHORT  (0x51, InstructionFormat.FORMAT_23x, ArrayInstruction::create, "aput-short"),

    // field instructions.
    IGET        (0x52, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iget"),
    IGET_WIDE   (0x53, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-wide", wide = true),
    IGET_OBJECT (0x54, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-object"),
    IGET_BOOLEAN(0x55, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-boolean"),
    IGET_BYTE   (0x56, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-byte"),
    IGET_CHAR   (0x57, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-char"),
    IGET_SHORT  (0x58, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iget-short"),
    IPUT        (0x59, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iput"),
    IPUT_WIDE   (0x5a, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-wide", wide = true),
    IPUT_OBJECT (0x5b, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-object"),
    IPUT_BOOLEAN(0x5c, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-boolean"),
    IPUT_BYTE   (0x5d, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-byte"),
    IPUT_CHAR   (0x5e, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-char"),
    IPUT_SHORT  (0x5f, InstructionFormat.FORMAT_22c, FieldInstruction::create, "iput-short"),
    SGET        (0x60, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sget"),
    SGET_WIDE   (0x61, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-wide", wide = true),
    SGET_OBJECT (0x62, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-object"),
    SGET_BOOLEAN(0x63, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-boolean"),
    SGET_BYTE   (0x64, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-byte"),
    SGET_CHAR   (0x65, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-char"),
    SGET_SHORT  (0x66, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sget-short"),
    SPUT        (0x67, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sput"),
    SPUT_WIDE   (0x68, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-wide", wide = true),
    SPUT_OBJECT (0x69, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-object"),
    SPUT_BOOLEAN(0x6a, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-boolean"),
    SPUT_BYTE   (0x6b, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-byte"),
    SPUT_CHAR   (0x6c, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-char"),
    SPUT_SHORT  (0x6d, InstructionFormat.FORMAT_21c, FieldInstruction::create, "sput-short"),

    // method instructions.
    INVOKE_VIRTUAL        (0x6e, InstructionFormat.FORMAT_35c, MethodInstruction::create, "invoke-virtual"),
    INVOKE_SUPER          (0x6f, InstructionFormat.FORMAT_35c, MethodInstruction::create, "invoke-super"),
    INVOKE_DIRECT         (0x70, InstructionFormat.FORMAT_35c, MethodInstruction::create, "invoke-direct"),
    INVOKE_STATIC         (0x71, InstructionFormat.FORMAT_35c, MethodInstruction::create, "invoke-static"),
    INVOKE_INTERFACE      (0x72, InstructionFormat.FORMAT_35c, MethodInstruction::create, "invoke-interface"),
    INVOKE_VIRTUAL_RANGE  (0x74, InstructionFormat.FORMAT_3rc, MethodInstruction::create, "invoke-virtual/range"),
    INVOKE_SUPER_RANGE    (0x75, InstructionFormat.FORMAT_3rc, MethodInstruction::create, "invoke-super/range"),
    INVOKE_DIRECT_RANGE   (0x76, InstructionFormat.FORMAT_3rc, MethodInstruction::create, "invoke-direct/range"),
    INVOKE_STATIC_RANGE   (0x77, InstructionFormat.FORMAT_3rc, MethodInstruction::create, "invoke-static/range"),
    INVOKE_INTERFACE_RANGE(0x78, InstructionFormat.FORMAT_3rc, MethodInstruction::create, "invoke-interface/range"),

    // arithmetic instructions.
    NEG_INT   (0x7b, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "neg-int"),
    NOT_INT   (0x7c, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "not-int"),
    NEG_LONG  (0x7d, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "neg-long",   wide = true),
    NOT_LONG  (0x7e, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "not-long",   wide = true),
    NEG_FLOAT (0x7f, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "neg-float"),
    NEG_DOUBLE(0x80, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "neg-double", wide = true),

    // conversion instructions.
    INT_TO_LONG    (0x81, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "int-to-long",     wide = true),
    INT_TO_FLOAT   (0x82, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "int-to-float"),
    INT_TO_DOUBLE  (0x83, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "int-to-double",   wide = true),
    LONG_TO_INT    (0x84, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "long-to-int",     wide = true),
    LONG_TO_FLOAT  (0x85, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "long-to-float",   wide = true),
    LONG_TO_DOUBLE (0x86, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "long-to-double",  wide = true),
    FLOAT_TO_INT   (0x87, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "float-to-int"),
    FLOAT_TO_LONG  (0x88, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "float-to-long",   wide = true),
    FLOAT_TO_DOUBLE(0x89, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "float-to-double", wide = true),
    DOUBLE_TO_INT  (0x8a, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "double-to-int",   wide = true),
    DOUBLE_TO_LONG (0x8b, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "double-to-long",  wide = true),
    DOUBLE_TO_FLOAT(0x8c, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "double-to-float", wide = true),
    INT_TO_BYTE    (0x8d, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "int-to-byte"),
    INT_TO_CHAR    (0x8e, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "int-to-char"),
    INT_TO_SHORT   (0x8f, InstructionFormat.FORMAT_12x, ConversionInstruction::create, "int-to-short"),

    // arithmetic instructions.
    ADD_INT         (0x90, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "add-int"),
    SUB_INT         (0x91, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "sub-int"),
    MUL_INT         (0x92, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "mul-int"),
    DIV_INT         (0x93, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "div-int"),
    REM_INT         (0x94, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "rem-int"),
    AND_INT         (0x95, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "and-int"),
    OR_INT          (0x96, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "or-int"),
    XOR_INT         (0x97, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "xor-int"),
    SHL_INT         (0x98, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "shl-int"),
    SHR_INT         (0x99, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "shr-int"),
    USHR_INT        (0x9a, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "ushr-int"),
    ADD_LONG        (0x9b, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "add-long",  wide = true),
    SUB_LONG        (0x9c, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "sub-long",  wide = true),
    MUL_LONG        (0x9d, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "mul-long",  wide = true),
    DIV_LONG        (0x9e, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "div-long",  wide = true),
    REM_LONG        (0x9f, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "rem-long",  wide = true),
    AND_LONG        (0xa0, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "and-long",  wide = true),
    OR_LONG         (0xa1, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "or-long",   wide = true),
    XOR_LONG        (0xa2, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "xor-long",  wide = true),
    SHL_LONG        (0xa3, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "shl-long",  wide = true),
    SHR_LONG        (0xa4, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "shr-long",  wide = true),
    USHR_LONG       (0xa5, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "ushr-long", wide = true),
    ADD_FLOAT       (0xa6, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "add-float"),
    SUB_FLOAT       (0xa7, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "sub-float"),
    MUL_FLOAT       (0xa8, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "mul-float"),
    DIV_FLOAT       (0xa9, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "div-float"),
    REM_FLOAT       (0xaa, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "rem-float"),
    ADD_DOUBLE      (0xab, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "add-double", wide = true),
    SUB_DOUBLE      (0xac, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "sub-double", wide = true),
    MUL_DOUBLE      (0xad, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "mul-double", wide = true),
    DIV_DOUBLE      (0xae, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "div-double", wide = true),
    REM_DOUBLE      (0xaf, InstructionFormat.FORMAT_23x, ArithmeticInstruction::create, "rem-double", wide = true),
    ADD_INT_2ADDR   (0xb0, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "add-int/2addr"),
    SUB_INT_2ADDR   (0xb1, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "sub-int/2addr"),
    MUL_INT_2ADDR   (0xb2, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "mul-int/2addr"),
    DIV_INT_2ADDR   (0xb3, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "div-int/2addr"),
    REM_INT_2ADDR   (0xb4, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "rem-int/2addr"),
    AND_INT_2ADDR   (0xb5, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "and-int/2addr"),
    OR_INT_2ADDR    (0xb6, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "or-int/2addr"),
    XOR_INT_2ADDR   (0xb7, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "xor-int/2addr"),
    SHL_INT_2ADDR   (0xb8, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "shl-int/2addr"),
    SHR_INT_2ADDR   (0xb9, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "shr-int/2addr"),
    USHR_INT_2ADDR  (0xba, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "ushr-int/2addr"),
    ADD_LONG_2ADDR  (0xbb, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "add-long/2addr",  wide = true),
    SUB_LONG_2ADDR  (0xbc, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "sub-long/2addr",  wide = true),
    MUL_LONG_2ADDR  (0xbd, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "mul-long/2addr",  wide = true),
    DIV_LONG_2ADDR  (0xbe, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "div-long/2addr",  wide = true),
    REM_LONG_2ADDR  (0xbf, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "rem-long/2addr",  wide = true),
    AND_LONG_2ADDR  (0xc0, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "and-long/2addr",  wide = true),
    OR_LONG_2ADDR   (0xc1, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "or-long/2addr",   wide = true),
    XOR_LONG_2ADDR  (0xc2, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "xor-long/2addr",  wide = true),
    SHL_LONG_2ADDR  (0xc3, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "shl-long/2addr",  wide = true),
    SHR_LONG_2ADDR  (0xc4, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "shr-long/2addr",  wide = true),
    USHR_LONG_2ADDR (0xc5, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "ushr-long/2addr", wide = true),
    ADD_FLOAT_2ADDR (0xc6, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "add-float/2addr"),
    SUB_FLOAT_2ADDR (0xc7, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "sub-float/2addr"),
    MUL_FLOAT_2ADDR (0xc8, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "mul-float/2addr"),
    DIV_FLOAT_2ADDR (0xc9, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "div-float/2addr"),
    REM_FLOAT_2ADDR (0xca, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "rem-float/2addr"),
    ADD_DOUBLE_2ADDR(0xcb, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "add-double/2addr", wide = true),
    SUB_DOUBLE_2ADDR(0xcc, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "sub-double/2addr", wide = true),
    MUL_DOUBLE_2ADDR(0xcd, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "mul-double/2addr", wide = true),
    DIV_DOUBLE_2ADDR(0xce, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "div-double/2addr", wide = true),
    REM_DOUBLE_2ADDR(0xcf, InstructionFormat.FORMAT_12x, ArithmeticInstruction::create, "rem-double/2addr", wide = true),
    ADD_INT_LIT16   (0xd0, InstructionFormat.FORMAT_22s, ArithmeticLiteralInstruction::create, "add-int/lit16"),
    RSUB_INT        (0xd1, InstructionFormat.FORMAT_22s, ArithmeticLiteralInstruction::create, "rsub-int"),
    MUL_INT_LIT16   (0xd2, InstructionFormat.FORMAT_22s, ArithmeticLiteralInstruction::create, "mul-int/lit16"),
    DIV_INT_LIT16   (0xd3, InstructionFormat.FORMAT_22s, ArithmeticLiteralInstruction::create, "div-int/lit16"),
    REM_INT_LIT16   (0xd4, InstructionFormat.FORMAT_22s, ArithmeticLiteralInstruction::create, "rem-int/lit16"),
    AND_INT_LIT16   (0xd5, InstructionFormat.FORMAT_22s, ArithmeticLiteralInstruction::create, "and-int/lit16"),
    OR_INT_LIT16    (0xd6, InstructionFormat.FORMAT_22s, ArithmeticLiteralInstruction::create, "or-int/lit16"),
    XOR_INT_LIT16   (0xd7, InstructionFormat.FORMAT_22s, ArithmeticLiteralInstruction::create, "xor-int/lit16"),
    ADD_INT_LIT8    (0xd8, InstructionFormat.FORMAT_22b, ArithmeticLiteralInstruction::create, "add-int/lit8"),
    RSUB_INT_LIT8   (0xd9, InstructionFormat.FORMAT_22b, ArithmeticLiteralInstruction::create, "rsub-int/lit8"),
    MUL_INT_LIT8    (0xda, InstructionFormat.FORMAT_22b, ArithmeticLiteralInstruction::create, "mul-int/lit8"),
    DIV_INT_LIT8    (0xdb, InstructionFormat.FORMAT_22b, ArithmeticLiteralInstruction::create, "div-int/lit8"),
    REM_INT_LIT8    (0xdc, InstructionFormat.FORMAT_22b, ArithmeticLiteralInstruction::create, "rem-int/lit8"),
    AND_INT_LIT8    (0xdd, InstructionFormat.FORMAT_22b, ArithmeticLiteralInstruction::create, "and-int/lit8"),
    OR_INT_LIT8     (0xde, InstructionFormat.FORMAT_22b, ArithmeticLiteralInstruction::create, "or-int/lit8"),
    XOR_INT_LIT8    (0xdf, InstructionFormat.FORMAT_22b, ArithmeticLiteralInstruction::create, "xor-int/lit8"),
    SHL_INT_LIT8    (0xe0, InstructionFormat.FORMAT_22b, ArithmeticLiteralInstruction::create, "shl-int/lit8"),
    SHR_INT_LIT8    (0xe1, InstructionFormat.FORMAT_22b, ArithmeticLiteralInstruction::create, "shr-int/lit8"),
    USHR_INT_LIT8   (0xe2, InstructionFormat.FORMAT_22b, ArithmeticLiteralInstruction::create, "ushr-int/lit8"),

    // method proto instructions.
    INVOKE_POLYMORPHIC      (0xfa, InstructionFormat.FORMAT_45cc, MethodProtoInstruction::create, "invoke-polymorphic",       DexFormat.FORMAT_038),
    INVOKE_POLYMORPHIC_RANGE(0xfb, InstructionFormat.FORMAT_4rcc, MethodProtoInstruction::create, "invoke-polymorphic/range", DexFormat.FORMAT_038),

    // callsite instructions.
    INVOKE_CUSTOM      (0xfc, InstructionFormat.FORMAT_35c, CallSiteInstruction::create, "invoke-custom",       DexFormat.FORMAT_038),
    INVOKE_CUSTOM_RANGE(0xfd, InstructionFormat.FORMAT_3rc, CallSiteInstruction::create, "invoke-custom/range", DexFormat.FORMAT_038),

    // method handle and proto instructions.
    CONST_METHOD_HANDLE(0xfe, InstructionFormat.FORMAT_21c, MethodHandleRefInstruction::create, "const-method-handle", DexFormat.FORMAT_039),
    CONST_METHOD_TYPE  (0xff, InstructionFormat.FORMAT_21c, MethodTypeRefInstruction::create, "const-method-type",     DexFormat.FORMAT_039),

    // for internal use only
    LABEL(0xffff, InstructionFormat.FORMAT_00x, null, "label");


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
        private val opcodeArray: Array<DexOpCode?> = arrayOfNulls(0x100)
        private val mnemonicToOpCodeMapping: MutableMap<String, DexOpCode> = hashMapOf()

        init {
            for (opCode in values()) {
                opcodeArray[opCode.opCode and 0xff] = opCode
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
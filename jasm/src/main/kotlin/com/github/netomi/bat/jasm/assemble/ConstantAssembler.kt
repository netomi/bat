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

package com.github.netomi.bat.jasm.assemble

import com.github.netomi.bat.classfile.constant.editor.ConstantPoolEditor
import com.github.netomi.bat.jasm.parser.JasmLexer
import com.github.netomi.bat.jasm.parser.JasmParser
import com.github.netomi.bat.jasm.parser.JasmParser.SBaseValueContext
import org.antlr.v4.runtime.tree.TerminalNode

internal class ConstantAssembler constructor(private val constantPoolEditor: ConstantPoolEditor) {

    fun parseBaseValue(ctx: SBaseValueContext): Int {
        // a base value is usually only a single token, only exception: enum fields
        val (value, isEnum) = if (ctx.childCount == 1) {
            val tn = ctx.getChild(0) as TerminalNode
            Pair(tn.symbol, false)
        } else {
            // in case of an enum, the first child is the ".enum" fragment.
            val first = (ctx.getChild(0) as TerminalNode).symbol
            assert(first.type == JasmLexer.DENUM)

            val tn = ctx.getChild(1) as TerminalNode
            Pair(tn.symbol, true)
        }

        return when (value.type) {
            JasmParser.STRING  -> constantPoolEditor.addOrGetUtf8ConstantIndex(parseString(value.text))
            JasmParser.BOOLEAN -> constantPoolEditor.addOrGetIntegerConstantIndex(("true" == value.text).toInt())
            JasmParser.BYTE    -> constantPoolEditor.addOrGetIntegerConstantIndex(parseByte(value.text).toInt())
            JasmParser.SHORT   -> constantPoolEditor.addOrGetIntegerConstantIndex(parseShort(value.text).toInt())
            JasmParser.CHAR    -> constantPoolEditor.addOrGetIntegerConstantIndex(parseChar(value.text).code)
            JasmParser.INT     -> constantPoolEditor.addOrGetIntegerConstantIndex(parseInt(value.text))
            JasmParser.LONG    -> constantPoolEditor.addOrGetLongConstantIndex(parseLong(value.text))

            JasmParser.BASE_FLOAT,
            JasmParser.FLOAT_INFINITY,
            JasmParser.FLOAT_NAN -> constantPoolEditor.addOrGetFloatConstantIndex(parseFloat(value.text))

            JasmParser.BASE_DOUBLE,
            JasmParser.DOUBLE_INFINITY,
            JasmParser.DOUBLE_NAN -> constantPoolEditor.addOrGetDoubleConstantIndex(parseDouble(value.text))

//            JasmLexer.METHOD_FULL -> {
//                val (classType, methodName, parameterTypes, returnType) = parseMethodObject(value.text)
//                val methodIndex = dexEditor.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)
//                EncodedMethodValue.of(methodIndex)
//            }
//            JasmLexer.METHOD_PROTO -> {
//                val (_, _, parameterTypes, returnType) = parseMethodObject(value.text)
//                val protoIndex = dexEditor.addOrGetProtoIDIndex(parameterTypes, returnType)
//                EncodedMethodTypeValue.of(protoIndex)
//            }
//            JasmLexer.FIELD_FULL -> {
//                val (classType, fieldName, type) = parseFieldObject(value.text)
//                val fieldIndex = dexEditor.addOrGetFieldIDIndex(classType!!, fieldName, type)
//
//                if (isEnum) EncodedEnumValue.of(fieldIndex) else EncodedFieldValue.of(fieldIndex)
//            }
//            JasmLexer.OBJECT_TYPE ->   EncodedTypeValue.of(dexEditor.addOrGetTypeIDIndex(value.text))
//            JasmLexer.NULL ->          EncodedNullValue
            else -> null
        } ?: error("failure to parse base value")
    }
}

private fun Boolean.toInt() = if (this) 1 else 0
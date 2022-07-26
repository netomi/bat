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

package com.github.netomi.bat.smali.assemble

import com.github.netomi.bat.dexfile.editor.DexEditor
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.smali.parser.SmaliLexer
import com.github.netomi.bat.smali.parser.SmaliParser
import org.antlr.v4.runtime.tree.TerminalNode

internal class EncodedValueAssembler constructor(private val dexEditor: DexEditor) {

    fun parseBaseValue(ctx: SmaliParser.SBaseValueContext): EncodedValue {

        // a base value is usually only a single token, only exception: enum fields
        val (value, isEnum) = if (ctx.childCount == 1) {
            val tn = ctx.getChild(0) as TerminalNode
            Pair(tn.symbol, false)
        } else {
            // in case of an enum, the first child is the ".enum" fragment.
            val first = (ctx.getChild(0) as TerminalNode).symbol
            assert(first.type == SmaliLexer.DENUM)

            val tn = ctx.getChild(1) as TerminalNode
            Pair(tn.symbol, true)
        }

        return when (value.type) {
            SmaliLexer.STRING ->        EncodedStringValue.of(dexEditor.addOrGetStringIDIndex(parseString(value.text)))
            SmaliLexer.BOOLEAN ->       EncodedBooleanValue.of("true" == value.text)
            SmaliLexer.BYTE ->          EncodedByteValue.of(parseByte(value.text))
            SmaliLexer.SHORT ->         EncodedShortValue.of(parseShort(value.text))
            SmaliLexer.CHAR ->          EncodedCharValue.of(parseChar(value.text))
            SmaliLexer.INT ->           EncodedIntValue.of(parseInt(value.text))
            SmaliLexer.LONG ->          EncodedLongValue.of(parseLong(value.text))
            SmaliLexer.BASE_FLOAT,
            SmaliLexer.FLOAT_INFINITY,
            SmaliLexer.FLOAT_NAN ->     EncodedFloatValue.of(parseFloat(value.text))
            SmaliLexer.BASE_DOUBLE,
            SmaliLexer.DOUBLE_INFINITY,
            SmaliLexer.DOUBLE_NAN ->    EncodedDoubleValue.of(parseDouble(value.text))
            SmaliLexer.METHOD_FULL -> {
                val (classType, methodName, parameterTypes, returnType) = parseMethodObject(value.text)
                val methodIndex = dexEditor.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)
                EncodedMethodValue.of(methodIndex)
            }
            SmaliLexer.METHOD_PROTO -> {
                val (_, _, parameterTypes, returnType) = parseMethodObject(value.text)
                val protoIndex = dexEditor.addOrGetProtoIDIndex(parameterTypes, returnType)
                EncodedMethodTypeValue.of(protoIndex)
            }
            SmaliLexer.FIELD_FULL -> {
                val (classType, fieldName, type) = parseFieldObject(value.text)
                val fieldIndex = dexEditor.addOrGetFieldIDIndex(classType!!, fieldName, type)

                if (isEnum) EncodedEnumValue.of(fieldIndex) else EncodedFieldValue.of(fieldIndex)
            }
            SmaliLexer.OBJECT_TYPE ->   EncodedTypeValue.of(dexEditor.addOrGetTypeIDIndex(value.text))
            SmaliLexer.NULL ->          EncodedNullValue
            else -> null
        } ?: parserError(ctx, "failure to parse base value")
    }
}
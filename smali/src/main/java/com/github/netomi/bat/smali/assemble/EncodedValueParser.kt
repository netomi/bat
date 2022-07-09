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

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.smali.parser.SmaliLexer
import com.github.netomi.bat.smali.parser.SmaliParser
import com.github.netomi.bat.util.Strings
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode

object EncodedValueParser {

    fun parseBaseValue(ctx: SmaliParser.SBaseValueContext, dexFile: DexFile): EncodedValue? {
        val value: Token = if (ctx.childCount == 1) {
            val tn = ctx.getChild(0) as TerminalNode
            tn.symbol
        } else {
            val first = (ctx.getChild(0) as TerminalNode).symbol
            assert(first.type == SmaliLexer.DENUM)

            val tn = ctx.getChild(1) as TerminalNode
            tn.symbol
        }

        return when (value.type) {
            SmaliLexer.STRING ->        EncodedStringValue.of(dexFile.addOrGetStringIDIndex(Strings.unescapeJavaString(value.text.removeSurrounding("\""))))
            SmaliLexer.BOOLEAN ->       EncodedBooleanValue.of("true" == value.text)
            SmaliLexer.BYTE ->          EncodedByteValue.of(java.lang.Byte.decode(value.text.removeSuffix("t")))
            SmaliLexer.SHORT ->         EncodedShortValue.of(java.lang.Short.decode(value.text.removeSuffix("s")))
            SmaliLexer.CHAR ->          EncodedCharValue.of(Strings.unescapeJavaString(value.text.removeSurrounding("'")).first())
            SmaliLexer.INT ->           EncodedIntValue.of(Integer.decode(value.text))
            SmaliLexer.LONG ->          EncodedLongValue.of(java.lang.Long.decode(value.text.removeSuffix("L")))
            SmaliLexer.BASE_FLOAT,
            SmaliLexer.FLOAT_INFINITY,
            SmaliLexer.FLOAT_NAN ->     EncodedFloatValue.of(value.text.toFloat())
            SmaliLexer.BASE_DOUBLE,
            SmaliLexer.DOUBLE_INFINITY,
            SmaliLexer.DOUBLE_NAN ->    EncodedDoubleValue.of(value.text.toDouble())
            SmaliLexer.METHOD_FULL ->   null //value.text
            SmaliLexer.OBJECT_TYPE ->   EncodedTypeValue.of(dexFile.addOrGetTypeIDIndex(value.text))
            SmaliLexer.NULL ->          EncodedNullValue
            SmaliLexer.FIELD_FULL ->    null // value.text
            else -> null
        }
    }

}
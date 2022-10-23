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
import com.github.netomi.bat.jasm.parser.JasmParser.*
import com.github.netomi.bat.jasm.parser.JasmParser.SBaseValueContext
import org.antlr.v4.runtime.tree.TerminalNode

internal class ConstantAssembler constructor(private val constantPoolEditor: ConstantPoolEditor) {

    fun parseBaseValue(ctx: SBaseValueContext): Int {
        // a base value is usually only a single token, only exception: enum fields
        val value = if (ctx.childCount == 1) {
            val tn = ctx.getChild(0) as TerminalNode
            tn.symbol
        } else {
            parserError(ctx, "unexpected constant base value")
        }

        return when (value.type) {
            STRING  -> constantPoolEditor.addOrGetUtf8ConstantIndex(parseString(value.text))
            BOOLEAN -> constantPoolEditor.addOrGetIntegerConstantIndex(("true" == value.text).toInt())
            BYTE    -> constantPoolEditor.addOrGetIntegerConstantIndex(parseByte(value.text).toInt())
            SHORT   -> constantPoolEditor.addOrGetIntegerConstantIndex(parseShort(value.text).toInt())
            CHAR    -> constantPoolEditor.addOrGetIntegerConstantIndex(parseChar(value.text).code)
            INT     -> constantPoolEditor.addOrGetIntegerConstantIndex(parseInt(value.text))
            LONG    -> constantPoolEditor.addOrGetLongConstantIndex(parseLong(value.text))

            BASE_FLOAT,
            FLOAT_INFINITY,
            FLOAT_NAN -> constantPoolEditor.addOrGetFloatConstantIndex(parseFloat(value.text))

            BASE_DOUBLE,
            DOUBLE_INFINITY,
            DOUBLE_NAN -> constantPoolEditor.addOrGetDoubleConstantIndex(parseDouble(value.text))

            ARRAY_TYPE,
            CLASS_NAME -> constantPoolEditor.addOrGetClassConstantIndex(value.text)

            else -> null
        } ?: error("failed to parse constant base value")
    }
}

private fun Boolean.toInt() = if (this) 1 else 0
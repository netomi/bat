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

import com.github.netomi.bat.classfile.AccessFlag
import com.github.netomi.bat.jasm.JasmParseException
import com.github.netomi.bat.jasm.parser.JasmParser
import com.github.netomi.bat.util.parseDescriptorParameters
import com.github.netomi.bat.util.unescapeJavaString
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree

internal fun parserError(ctx: ParserRuleContext, cause: Throwable): Nothing {
    val lineNumber = ctx.getStart().line
    val column     = ctx.getStart().charPositionInLine

    val list = mutableListOf<String>()
    fillParseContextText(ctx, list)
    throw JasmParseException("${cause.message} at line: $lineNumber col: $column -> ${list.joinToString(" ")}")
}

internal fun parserError(ctx: ParserRuleContext, message: String): Nothing {
    val lineNumber = ctx.getStart().line
    val column     = ctx.getStart().charPositionInLine

    val list = mutableListOf<String>()
    fillParseContextText(ctx, list)
    throw JasmParseException("$message at line: $lineNumber col: $column -> ${list.joinToString(" ")}")
}

private fun fillParseContextText(ctx: ParserRuleContext, list: MutableList<String>) {
    if (ctx.childCount == 0) list.add(ctx.text)
    else {
        for (child in ctx.children) {
            fillParseContextText(child, list)
        }
    }
}

private fun fillParseContextText(node: ParseTree, list: MutableList<String>) {
    if (node.childCount == 0) list.add(node.text)
    else {
        for (i in 0 until node.childCount) {
            fillParseContextText(node.getChild(i), list)
        }
    }
}

internal data class FieldInfo(val classType:String?, val name: String, val type: String)

internal fun parseFieldObject(text: String): FieldInfo {
    val arrowIndex = text.indexOf("->")
    val classType = if (arrowIndex != -1) {
        text.substring(0, arrowIndex)
    } else {
        null
    }

    val startNameIndex = if (arrowIndex != -1) { arrowIndex + 2 } else { 0 }

    val colonIndex = text.indexOf(':')

    val name = text.substring(startNameIndex, colonIndex)
    val type = text.substring(colonIndex + 1)

    return FieldInfo(classType, name, type)
}

internal data class MethodInfo(val classType: String?, val methodName: String, val parameterTypes: List<String>, val returnType: String)

internal fun parseMethodObject(text: String): MethodInfo {
    val arrowIndex = text.indexOf("->")
    val classType = if (arrowIndex != -1) {
        text.substring(0, arrowIndex)
    } else {
        null
    }

    val startNameIndex = if (arrowIndex != -1) { arrowIndex + 2 } else { 0 }

    val parameterStartIndex = text.indexOf('(')
    val parameterEndIndex   = text.indexOf(')')

    val name = text.substring(startNameIndex, parameterStartIndex)

    val parameters     = text.substring(parameterStartIndex + 1, parameterEndIndex)
    val parameterTypes = parseDescriptorParameters(parameters)
    val returnType     = text.substring(parameterEndIndex + 1)
    return MethodInfo(classType, name, parameterTypes, returnType)
}

internal fun parseAccessFlags(sAccListContext: JasmParser.SAccListContext): Int {
    var accessFlags = 0
    sAccListContext.ACC().forEach {
        val flag = AccessFlag.of(it.text)
        accessFlags = accessFlags or flag.value
    }
    return accessFlags
}

internal fun parseString(value: String): String {
    return value.removeSurrounding("\"").unescapeJavaString()
}

internal fun parseChar(value: String): Char {
    return value.removeSurrounding("'").unescapeJavaString().first()
}

internal fun parseInt(value: String): Int {
    return Integer.decode(value)
}

internal fun parseLong(value: String): Long {
    return java.lang.Long.decode(value.removeSuffix("l").removeSuffix("L"))
}

internal fun parseByte(value: String): Byte {
    return java.lang.Short.decode(value.removeSuffix("t").removeSuffix("T")).toByte()
}

internal fun parseShort(value: String): Short {
    return java.lang.Short.decode(value.removeSuffix("s").removeSuffix("S"))
}

internal fun parseFloat(value: String): Float {
    return value.toFloat()
}

internal fun parseDouble(value: String): Double {
    return value.toDouble()
}

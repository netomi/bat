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

import com.github.netomi.bat.dexfile.DexAccessFlag
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.smali.parser.SmaliParser
import com.github.netomi.bat.smali.parser.SmaliParser.SParameterContext
import com.github.netomi.bat.util.unescapeJavaString
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree

internal fun parserError(ctx: ParserRuleContext, message: String): Nothing {
    val lineNumber = ctx.getStart().line
    val column     = ctx.getStart().charPositionInLine

    val list = mutableListOf<String>()
    fillParseContextText(ctx, list)
    throw RuntimeException("$message at line: $lineNumber col: $column -> ${list.joinToString(" ")}")
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

internal fun parseParameterIndex(ctx: SParameterContext, dexFile: DexFile, method: EncodedMethod): Int {
    // check whether the parameter number is in the following formats:
    //  * pX where X denotes the parameter register (backwards compatible to smali 2.x)
    //  *  X where X denotes the parameter index (0 based) from the descriptor

    // when using the pX format, you have to take into account the argument size of each parameter,
    // whereas the direct format just references the parameter from the descriptor (starting from 0),
    // regardless of its argument size, making it more convenient to use.

    var parameterIndex = 0

    if (ctx.registerNumber != null) {
        val parameterRegisterNumber = ctx.registerNumber.text.substring(1).toInt()

        var currRegister = if (method.isStatic) 0 else 1
        val parameters   = method.getProtoID(dexFile).parameters
        for (type in parameters.getTypes(dexFile)) {
            if (currRegister == parameterRegisterNumber) {
                break
            } else {
                parameterIndex++
                currRegister += DexClasses.getArgumentSizeForType(type)
            }
        }
    } else if (ctx.parameterIndex != null) {
        parameterIndex = ctx.parameterIndex.text.toInt()
        if (parameterIndex < 0) {
            parserError(ctx, "parameter number must not be negative")
        }
    } else {
        parserError(ctx, "unexpected parameter number")
    }

    return parameterIndex
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

    val parameters = text.substring(parameterStartIndex + 1, parameterEndIndex)
    val parameterTypes = if (parameters.isEmpty()) emptyList() else DexClasses.parseParameters(parameters)
    val returnType = text.substring(parameterEndIndex + 1)
    return MethodInfo(classType, name, parameterTypes, returnType)
}

internal fun parseAccessFlags(sAccListContext: SmaliParser.SAccListContext): Int {
    var accessFlags = 0
    sAccListContext.ACC().forEach {
        val flag = DexAccessFlag.of(it.text)
        accessFlags = accessFlags or flag.value
    }
    return accessFlags
}

internal fun parseLiteral(value: String): Long {
    return if (value.contains("0x") || value.contains("0X")) {
        parseLong(value)
    } else {
        when (value.last()) {
            'l',
            'L',
            's',
            'S',
            't',
            'T' -> parseLong(value.dropLast(1))

            '\'' -> parseChar(value).code.toLong()

            'f',
            'F' -> parseFloat(value.dropLast(1)).toBits().toLong()

            'd',
            'D' -> parseDouble(value.dropLast(1)).toBits()

            else -> {
                if (value.contains(".")) {
                    parseDouble(value).toBits()
                } else {
                    parseLong(value)
                }
            }
        }
    }
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
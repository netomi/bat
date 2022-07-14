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

import com.github.netomi.bat.dexfile.DexAccessFlags
import com.github.netomi.bat.smali.parser.SmaliParser
import org.antlr.v4.runtime.ParserRuleContext

fun parserError(ctx: ParserRuleContext, message: String): Nothing {
    val lineNumber = ctx.getStart().line
    val column     = ctx.getStart().charPositionInLine

    throw RuntimeException("$message at line: $lineNumber col: $column -> ${ctx.text}")
}

data class FieldInfo(val classType:String?, val name: String, val type: String)

fun parseFieldObject(text: String): FieldInfo {
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

data class MethodInfo(val classType: String?, val methodName: String, val parameterTypes: List<String>, val returnType: String)

fun parseMethodObject(text: String): MethodInfo {
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
    val parameterTypes = if (parameters.isEmpty()) emptyList() else parameters.split(",")
    val returnType = text.substring(parameterEndIndex + 1)
    return MethodInfo(classType, name, parameterTypes, returnType)
}

fun parseAccessFlags(sAccListContext: SmaliParser.SAccListContext): Int {
    var accessFlags = 0
    sAccListContext.ACC().forEach {
        val flag = DexAccessFlags.of(it.text)
        accessFlags = accessFlags or flag.value
    }
    return accessFlags
}
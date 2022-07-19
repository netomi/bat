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
package com.github.netomi.bat.util

import java.util.*
import java.util.regex.Pattern

fun interface StringMatcher {
    fun matches(input: String): Boolean

    fun or(other: StringMatcher): StringMatcher {
        return StringMatcher { input -> this@StringMatcher.matches(input) || other.matches(input) }
    }

    fun and(other: StringMatcher): StringMatcher {
        return StringMatcher { input -> this@StringMatcher.matches(input) && other.matches(input) }
    }
}

fun classNameMatcher(regularExpression: String): StringMatcher {
    return ClassNameMatcher(regularExpression)
}

fun fileNameMatcher(regularExpression: String): StringMatcher {
    return FileNameMatcher(regularExpression)
}

private class ClassNameMatcher(regularExpression: String) : RegexStringMatcher("\\.\\/", regularExpression)

private class FileNameMatcher(regularExpression: String) : RegexStringMatcher("\\/", regularExpression)

private abstract class RegexStringMatcher protected constructor(separatorCharacters: String, regularExpression: String) : StringMatcher {

    private val patterns: MutableList<Pattern> = mutableListOf()

    init {
        val st = StringTokenizer(regularExpression, ",")
        while (st.hasMoreTokens()) {
            val expression = st.nextToken()
            patterns.add(compilePattern(separatorCharacters, expression))
        }
    }

    private fun compilePattern(separatorCharacters: String, expression: String): Pattern {
        // Clean the expression first.
        var cleanedExpression = expression.replace("\\.".toRegex(), "\\\\.")

        // '**' means to match anything till the end.
        // Replace with '@' temporarily to avoid problems with the next rule.
        cleanedExpression = cleanedExpression.replace("\\*\\*".toRegex(), ".@")
        // '*' means to match anything till the next separator character.
        cleanedExpression = cleanedExpression.replace("\\*".toRegex(), "[^$separatorCharacters]*")
        // '?' means to match a single character till the next separator character.
        cleanedExpression = cleanedExpression.replace("\\?".toRegex(), "[^$separatorCharacters]{1}")
        // Replace '@' with '*' at the end.
        cleanedExpression = cleanedExpression.replace("@".toRegex(), "*")

        // '!' is only allowed at the start of the expression and negates it.
        if (cleanedExpression.startsWith("!")) {
            cleanedExpression = "^(?!" + cleanedExpression.substring(1) + "$).*$"
        } else if (cleanedExpression.contains("!")) {
            throw RuntimeException("'!' only allowed at start of expression.")
        }

        return Pattern.compile(cleanedExpression)
    }

    override fun matches(input: String): Boolean {
        for (p in patterns) {
            if (p.matcher(input).matches()) {
                return true
            }
        }
        return false
    }
}

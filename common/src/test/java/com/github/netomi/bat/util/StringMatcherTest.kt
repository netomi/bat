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

package com.github.netomi.bat.util

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

abstract class StringMatcherTest {
    abstract fun matcher(regularExpression: String): StringMatcher
}

class ClassNameMatcherTest : StringMatcherTest() {

    override fun matcher(regularExpression: String): StringMatcher {
        return classNameMatcher(regularExpression)
    }

    @Test
    fun noWildCard() {
        val matcher = matcher("abc")

        assertTrue(matcher.matches("abc"))
        assertFalse(matcher.matches(""))
        assertFalse(matcher.matches("abcde"))
        assertFalse(matcher.matches("123abc"))
    }

    @Test
    fun singleStar() {
        val matcher = matcher("abc/def*")

        assertTrue(matcher.matches("abc/def"))
        assertFalse(matcher.matches(""))
        assertTrue(matcher.matches("abc/defgh"))
        assertFalse(matcher.matches("123abc/def"))
        assertFalse(matcher.matches("abc/def/ghi"))
    }

    @Test
    fun doubleStar() {
        val matcher = matcher("abc/def**")

        assertTrue(matcher.matches("abc/def"))
        assertFalse(matcher.matches(""))
        assertTrue(matcher.matches("abc/defgh"))
        assertFalse(matcher.matches("123abc/def"))
        assertTrue(matcher.matches("abc/def/ghi"))
    }

    @Test
    fun questionMark() {
        val matcher = matcher("abc/def?")

        assertFalse(matcher.matches("abc/def"))
        assertFalse(matcher.matches(""))
        assertTrue(matcher.matches("abc/defg"))
        assertFalse(matcher.matches("abc/defgh"))
        assertFalse(matcher.matches("123abc/def"))
        assertFalse(matcher.matches("abc/def/"))
    }

    @Test
    fun exclamationMark() {
        val matcher = matcher("!abc/def*")

        assertFalse(matcher.matches("abc/def"))
        assertTrue(matcher.matches(""))
        assertFalse(matcher.matches("abc/defg"))
        assertFalse(matcher.matches("abc/defgh"))
        assertTrue(matcher.matches("123abc/def"))
        assertTrue(matcher.matches("abc/def/ghi"))

        assertFailsWith(RuntimeException::class) { matcher("abc/def!/ghi") }
    }
}

class SimpleNameMatcherTest : StringMatcherTest() {

    override fun matcher(regularExpression: String): StringMatcher {
        return simpleNameMatcher(regularExpression)
    }

    @Test
    fun noWildCard() {
        val matcher = matcher("abc")

        assertTrue(matcher.matches("abc"))
        assertFalse(matcher.matches(""))
        assertFalse(matcher.matches("abcde"))
        assertFalse(matcher.matches("123abc"))
    }

    @Test
    fun singleStar() {
        val matcher = matcher("abc*")

        assertTrue(matcher.matches("abcdef"))
        assertFalse(matcher.matches(""))
        assertTrue(matcher.matches("abcdefgh"))
        assertFalse(matcher.matches("123abcdef"))
    }

    @Test
    fun doubleStar() {
        val matcher = matcher("abcdef**")

        assertTrue(matcher.matches("abcdef"))
        assertFalse(matcher.matches(""))
        assertTrue(matcher.matches("abcdefgh"))
        assertFalse(matcher.matches("123abcdef"))
        assertTrue(matcher.matches("abcdefghi"))
    }

    @Test
    fun questionMark() {
        val matcher = matcher("abcdef?")

        assertFalse(matcher.matches("abcdef"))
        assertFalse(matcher.matches(""))
        assertTrue(matcher.matches("abcdefg"))
        assertFalse(matcher.matches("abcdefgh"))
        assertFalse(matcher.matches("123abcdef"))
    }

    @Test
    fun exclamationMark() {
        val matcher = matcher("!abcdef*")

        assertFalse(matcher.matches("abcdef"))
        assertTrue(matcher.matches(""))
        assertFalse(matcher.matches("abcdefg"))
        assertFalse(matcher.matches("abcdefgh"))
        assertTrue(matcher.matches("123abcdef"))

        assertFailsWith(RuntimeException::class) { matcher("abcdef!ghi") }
    }

    @Test
    fun reservedChars() {
        val matcher = matcher("abc\$def.ghi^")

        assertTrue(matcher.matches("abc\$def.ghi^"))
        assertFalse(matcher.matches(""))
    }
}
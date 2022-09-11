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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class JvmSignatureTest {

    @ParameterizedTest
    @MethodSource("getClassSignatures")
    fun classSignatures(signature: String, isInterface: Boolean, expectedOutput: String) {
        val result = getExternalClassSignature(signature, isInterface)
        assertEquals(expectedOutput, result)
    }

    @ParameterizedTest
    @MethodSource("getFieldSignatures")
    fun fieldSignatures(signature: String, expectedOutput: String) {
        val result = getExternalFieldSignature(signature)
        assertEquals(expectedOutput, result)
    }

//    @ParameterizedTest
//    @MethodSource("getMethodSignatures")
//    fun fieldSignatures(signature: String, expectedOutput: String) {
//        val result = getExternalMethodSignature(signature)
//        assertEquals(expectedOutput, result)
//    }

    companion object {
        @JvmStatic
        fun getClassSignatures(): List<Arguments> {
            return listOf(
                Arguments.of("Ljava/lang/Object;Ljava/io/Serializable;", false,
                             " extends java.lang.Object implements java.io.Serializable"),
                Arguments.of("Ljava/lang/Object;Ljava/io/Serializable;", true,
                             " extends java.io.Serializable"),
                Arguments.of("<C::Ljava/lang/Comparable;>Lcom/google/common/collect/Cut<TC;>;", false,
                             "<C extends java.lang.Comparable> extends com.google.common.collect.Cut<C>"),
                Arguments.of("Ljava/lang/Object;Lcom/google/common/base/Predicate<Ljava/lang/CharSequence;>;Ljava/io/Serializable;", true,
                             " extends com.google.common.base.Predicate<java.lang.CharSequence>, java.io.Serializable"),
                Arguments.of("<T:Ljava/lang/Object;>Lcom/google/common/base/Optional<TT;>;", false,
                             "<T extends java.lang.Object> extends com.google.common.base.Optional<T>"),
                Arguments.of("Lcom/google/common/cache/LocalCache<TK;TV;>.AbstractCacheSet<Ljava/util/Map\$Entry<TK;TV;>;>;", false,
                             " extends com.google.common.cache.LocalCache<K, V>.AbstractCacheSet<java.util.Map\$Entry<K, V>>")
            )
        }

        @JvmStatic
        fun getFieldSignatures(): List<Arguments> {
            return listOf(
                Arguments.of("[[TV;", "V[][]")
            )
        }

        @JvmStatic
        fun getMethodSignatures(): List<Arguments> {
            return listOf(
                Arguments.of("<E:Ljava/lang/Object;>([TE;)[TE;", ""),
                Arguments.of("<A::Ljava/lang/Appendable;>(TA;Ljava/util/Map<**>;)TA;", "")
            )
        }
    }
}
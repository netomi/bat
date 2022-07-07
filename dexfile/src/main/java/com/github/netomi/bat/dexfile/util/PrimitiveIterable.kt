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
package com.github.netomi.bat.dexfile.util

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.util.IntArray
import java.util.function.BiFunction

class PrimitiveIterable<E> private constructor(
    private val dexFile:          DexFile,
    private val accessorFunction: BiFunction<DexFile, Int, E>,
    private val array:            IntArray) : Iterable<E> {

    override fun iterator(): Iterator<E> {
        return object : Iterator<E> {
            var index     = 0
            val arraySize = array.size()

            override fun hasNext(): Boolean {
                return index < arraySize
            }

            override fun next(): E {
                return accessorFunction.apply(dexFile, array[index++])
            }
        }
    }

    companion object {
        @JvmStatic
        fun <E> of(dexFile: DexFile, accessorFunction: BiFunction<DexFile, Int, E>, array: IntArray): Iterable<E> {
            return PrimitiveIterable(dexFile, accessorFunction, array)
        }
    }
}
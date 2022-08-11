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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

interface Copyable<T> {
    fun copy(): T
}

fun <T: Copyable<T>> List<T>.deepCopy(): List<T> {
    return map { it.copy() }
}

fun <T> mutableListOfCapacity(size: Int): MutableList<T> {
    return ArrayList(size)
}

fun <A, B> List<A>.parallelForEachIndexed(coroutineContext: CoroutineContext = Dispatchers.Default, f: suspend (Int, A) -> B): List<B> {
    return runBlocking {
        mapIndexed { index, a -> async(coroutineContext) { f(index, a) } }.map { it.await() }
    }
}

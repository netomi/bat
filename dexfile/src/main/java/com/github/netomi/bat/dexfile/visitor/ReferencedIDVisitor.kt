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

package com.github.netomi.bat.dexfile.visitor

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.util.IntArray
import kotlin.reflect.KMutableProperty

internal interface ReferencedIDVisitor {
    fun visitStringID(dexFile: DexFile, accessor: IDAccessor) {}

    fun visitTypeID(dexFile: DexFile, accessor: IDAccessor) {}

    fun visitProtoID(dexFile: DexFile, accessor: IDAccessor) {}

    fun visitFieldID(dexFile: DexFile, accessor: IDAccessor) {}

    fun visitMethodID(dexFile: DexFile, accessor: IDAccessor) {}
}

internal interface IDAccessor {
    fun get(): Int
    fun set(value: Int)
}

internal class PropertyAccessor(private val property: KMutableProperty<Int>): IDAccessor {
    override fun get(): Int {
        return property.getter.call()
    }

    override fun set(value: Int) {
        property.setter.call(value)
    }
}

internal class ArrayElementAccessor(private val array: IntArray, private val index: Int): IDAccessor {
    override fun get(): Int {
        return array[index]
    }

    override fun set(value: Int) {
        array[index] = value
    }
}
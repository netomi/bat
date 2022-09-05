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

package com.github.netomi.bat.classfile.attribute.annotation

import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import com.github.netomi.bat.util.mutableListOfCapacity

data class TypePath
    private constructor(private var _path: MutableList<PathElement> = mutableListOfCapacity(0)): ClassFileContent(), Sequence<PathElement> {

    override val dataSize: Int
        get() = _path.fold(1) { acc, element -> acc + element.dataSize }

    val size: Int
        get() = _path.size

    operator fun get(index: Int): PathElement {
        return _path[index]
    }

    override fun iterator(): Iterator<PathElement> {
        return _path.iterator()
    }

    internal fun read(input: ClassDataInput) {
        val pathLength = input.readUnsignedByte()
        _path = mutableListOfCapacity(pathLength)
        for (i in 0 until pathLength) {
            _path.add(PathElement.read(input))
        }
    }

    override fun write(output: ClassDataOutput) {
        output.writeByte(_path.size)
        for (element in _path) {
            element.write(output)
        }
    }

    companion object {
        internal fun empty(): TypePath {
            return TypePath()
        }

        internal fun read(input: ClassDataInput): TypePath {
            val path = TypePath()
            path.read(input)
            return path
        }
    }
}

data class PathElement private constructor(private var _typePathKind:      Int = 0,
                                           private var _typeArgumentIndex: Int = -1): ClassFileContent() {

    override val dataSize: Int
        get() = 2

    val typePathKind: Int
        get() = _typePathKind

    val typeArgumentIndex: Int
        get() = _typeArgumentIndex

    private fun read(input: ClassDataInput) {
        _typePathKind      = input.readUnsignedByte()
        _typeArgumentIndex = input.readUnsignedByte()
    }

    override fun write(output: ClassDataOutput) {
        output.writeByte(_typePathKind)
        output.writeByte(_typeArgumentIndex)
    }

    companion object {
        internal fun read(input: ClassDataInput): PathElement {
            val element = PathElement()
            element.read(input)
            return element
        }
    }
}
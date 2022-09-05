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

import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.DataInput
import java.io.DataOutput

data class TypePath
    private constructor(private var _path: MutableList<PathElement> = mutableListOfCapacity(0)): Sequence<PathElement> {

    val size: Int
        get() = _path.size

    operator fun get(index: Int): PathElement {
        return _path[index]
    }

    override fun iterator(): Iterator<PathElement> {
        return _path.iterator()
    }

    internal fun read(input: DataInput) {
        val pathLength = input.readByte().toInt() and 0xff
        _path = mutableListOfCapacity(pathLength)
        for (i in 0 until pathLength) {
            _path.add(PathElement.read(input))
        }
    }

    internal fun write(output: DataOutput) {
        output.writeByte(_path.size)
        for (element in _path) {
            element.writeData(output)
        }
    }

    companion object {
        internal fun empty(): TypePath {
            return TypePath()
        }

        internal fun read(input: DataInput): TypePath {
            val path = TypePath()
            path.read(input)
            return path
        }
    }
}

data class PathElement private constructor(private var _typePathKind:      Int = 0,
                                           private var _typeArgumentIndex: Int = -1) {

    val typePathKind: Int
        get() = _typePathKind

    val typeArgumentIndex: Int
        get() = _typeArgumentIndex

    internal fun readData(input: DataInput) {
        _typePathKind      = input.readByte().toInt() and 0xff
        _typeArgumentIndex = input.readByte().toInt() and 0xff
    }

    internal fun writeData(output: DataOutput) {
        output.writeByte(_typePathKind)
        output.writeByte(_typeArgumentIndex)
    }

    companion object {
        internal fun read(input: DataInput): PathElement {
            val element = PathElement()
            element.readData(input)
            return element
        }
    }
}
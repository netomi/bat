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

package com.github.netomi.bat.classfile.io

import com.github.netomi.bat.classfile.attribute.Attribute
import java.io.Closeable
import java.io.DataOutputStream
import java.io.OutputStream

internal class ClassDataOutput private constructor(outputStream: OutputStream): Closeable {

    private val dataOutput = DataOutputStream(outputStream)

    fun writeByte(v: Int) {
        dataOutput.writeByte(v)
    }

    fun writeShort(v: Int) {
        dataOutput.writeShort(v)
    }

    fun writeInt(v: Int) {
        dataOutput.writeInt(v)
    }

    fun write(b: ByteArray) {
        dataOutput.write(b)
    }

    fun writeUTF(str: String) {
        dataOutput.writeUTF(str)
    }

    fun <T: ClassFileContent> writeContentList(list: List<T>) {
        writeShort(list.size)
        for (element in list) {
            element.write(this)
        }
    }

    fun writeShortIndexArray(array: IntArray) {
        writeShort(array.size)
        for (index in array) {
            dataOutput.writeShort(index)
        }
    }

    fun writeAttributes(attributes: List<Attribute>) {
        writeShort(attributes.size)
        for (element in attributes) {
            element.write(this)
        }
    }

    fun size(): Int {
        return dataOutput.size()
    }

    override fun close() {
        dataOutput.flush()
        dataOutput.close()
    }

    companion object {
        fun of(outputStream: OutputStream): ClassDataOutput {
            return ClassDataOutput(outputStream)
        }
    }
}
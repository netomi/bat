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

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.constant.Constant
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.Closeable
import java.io.DataInputStream
import java.io.InputStream

internal class ClassDataInput private constructor(            `is`:           InputStream,
                                                  private val classFile:      ClassFile,
                                                  private val skipAttributes: Boolean): Closeable {

    private val dataInput: DataInputStream = DataInputStream(`is`)

    fun skipBytes(n: Int) {
        dataInput.skipBytes(n)
    }

    fun readUnsignedByte(): Int {
        return dataInput.readUnsignedByte()
    }

    fun readUnsignedShort(): Int {
        return dataInput.readUnsignedShort()
    }

    fun readInt(): Int {
        return dataInput.readInt()
    }

    fun readUnsignedInt(): Long {
        return dataInput.readInt().toLong() and 0xffffffff
    }

    fun readUTF(): String {
        return dataInput.readUTF()
    }

    fun readFully(array: ByteArray) {
        dataInput.readFully(array)
    }

    fun readConstant(): Constant {
        return Constant.read(this)
    }

    fun readShortIndexArray(): IntArray {
        val count = readUnsignedShort()
        val array = IntArray(count)
        for (i in 0 until count) {
            array[i] = readUnsignedShort()
        }
        return array
    }

    fun <T: ClassFileContent> readContentList(supplier: (ClassDataInput) -> T): MutableList<T> {
        val count = readUnsignedShort()
        val list  = mutableListOfCapacity<T>(count)
        for (i in 0 until count) {
            list.add(supplier.invoke(this))
        }
        return list
    }

    fun readAttributes(): MutableList<Attribute> {
        val attributeCount = readUnsignedShort()
        return if (skipAttributes) {
            for (i in 0 until attributeCount) {
                Attribute.skipAttribute(this)
            }
            mutableListOfCapacity(0)
        } else {
            val attributes = mutableListOfCapacity<Attribute>(attributeCount)
            for (i in 0 until attributeCount) {
                attributes.add(Attribute.readAttribute(this, classFile))
            }
            attributes
        }
    }

    override fun close() {
        dataInput.close()
    }

    companion object {
        fun of(`is`: InputStream, classFile: ClassFile = ClassFile.empty(), skipAttributes: Boolean = false): ClassDataInput {
            return ClassDataInput(`is`, classFile, skipAttributes)
        }
    }
}
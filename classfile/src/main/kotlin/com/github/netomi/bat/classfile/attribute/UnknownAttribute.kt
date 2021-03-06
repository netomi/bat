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
package com.github.netomi.bat.classfile.attribute

import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class UnknownAttribute internal constructor(override val attributeNameIndex: Int = -1,
                                                          var info:               ByteArray) : Attribute(attributeNameIndex) {

    override val type: Type
        get() = Type.UNKNOWN

    @Throws(IOException::class)
    override fun readAttributeData(input: DataInput) {
        val length = input.readInt()
        info = ByteArray(length)
        input.readFully(info)
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: DataOutput) {
        info.let {
            output.writeInt(info.size)
            output.write(info)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnknownAttribute

        if (attributeNameIndex != other.attributeNameIndex) return false
        if (!info.contentEquals(other.info)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = attributeNameIndex
        result = 31 * result + info.contentHashCode()
        return result
    }

    companion object {
        @JvmStatic
        fun create(attributeNameIndex: Int): UnknownAttribute {
            return create(attributeNameIndex)
        }
    }
}
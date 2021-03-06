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

import com.github.netomi.bat.classfile.ConstantPool
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class EnclosingMethodAttribute internal constructor(override var attributeNameIndex: Int = -1,
                                                                  var classIndex:         Int = -1,
                                                                  var methodIndex:        Int = -1) : Attribute(attributeNameIndex) {

    override val type: Type
        get() = Type.ENCLOSING_METHOD

    fun getClassName(constantPool: ConstantPool): String {
        return constantPool.getClassName(classIndex)
    }

    fun getMethodName(constantPool: ConstantPool): String {
        return constantPool.getName(methodIndex);
    }

    fun getMethodType(constantPool: ConstantPool): String {
        return constantPool.getType(methodIndex)
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: DataInput) {
        val length = input.readInt()
        assert(length == 4)
        classIndex  = input.readUnsignedShort()
        methodIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(4)
        output.writeShort(classIndex)
        output.writeShort(methodIndex)
    }

    companion object {
        @JvmStatic
        fun create(attributeNameIndex: Int): EnclosingMethodAttribute {
            return EnclosingMethodAttribute(attributeNameIndex)
        }
    }
}
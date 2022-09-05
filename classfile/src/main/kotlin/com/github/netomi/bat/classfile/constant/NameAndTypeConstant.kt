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
package com.github.netomi.bat.classfile.constant

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitorIndexed
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A constant representing a CONSTANT_NameAndType_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.6">CONSTANT_NameAndType_info Structure</a>
 */
data class NameAndTypeConstant private constructor(private var _nameIndex:       Int = -1,
                                                   private var _descriptorIndex: Int = -1) : Constant() {

    override val type: ConstantType
        get() = ConstantType.NAME_AND_TYPE

    val nameIndex: Int
        get() = _nameIndex

    val descriptorIndex: Int
        get() = _descriptorIndex

    fun getMemberName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    fun getDescriptor(classFile: ClassFile): String {
        return classFile.getString(descriptorIndex)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: DataInput) {
        _nameIndex       = input.readUnsignedShort()
        _descriptorIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: DataOutput) {
        output.writeShort(nameIndex)
        output.writeShort(descriptorIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ConstantVisitor) {
        visitor.visitNameAndTypeConstant(classFile, this)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitorIndexed) {
        visitor.visitNameAndTypeConstant(classFile, index, this)
    }

    companion object {
        internal fun empty(): NameAndTypeConstant {
            return NameAndTypeConstant()
        }

        fun of(nameIndex: Int, descriptorIndex: Int): NameAndTypeConstant {
            require(nameIndex >= 1) { "nameIndex must be a positive number" }
            require(descriptorIndex >= 1) { "descriptorIndex must be a positive number" }
            return NameAndTypeConstant(nameIndex, descriptorIndex)
        }
    }
}
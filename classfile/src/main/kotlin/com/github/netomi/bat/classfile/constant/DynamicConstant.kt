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
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A constant representing a CONSTANT_Dynamic_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.10">CONSTANT_Dynamic_info Structure</a>
 */
data class DynamicConstant private constructor(private var _bootstrapMethodAttrIndex: Int = -1,
                                               private var _nameAndTypeIndex:         Int = -1) : Constant() {

    override val type: ConstantType
        get() = ConstantType.DYNAMIC

    val bootstrapMethodAttrIndex: Int
        get() = _bootstrapMethodAttrIndex

    val nameAndTypeIndex: Int
        get() = _nameAndTypeIndex

    fun getNameAndType(classFile: ClassFile): NameAndTypeConstant {
        return classFile.getNameAndType(nameAndTypeIndex)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: DataInput) {
        _bootstrapMethodAttrIndex = input.readUnsignedShort()
        _nameAndTypeIndex         = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: DataOutput) {
        output.writeShort(bootstrapMethodAttrIndex)
        output.writeShort(nameAndTypeIndex)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitDynamicConstant(classFile, index, this)
    }

    companion object {
        internal fun empty(): DynamicConstant {
            return DynamicConstant()
        }

        fun of(bootstrapMethodAttrIndex: Int, nameAndTypeIndex: Int): DynamicConstant {
            require(bootstrapMethodAttrIndex >= 1) { "bootstrapMethodAttrIndex must be a positive number" }
            require(nameAndTypeIndex >= 1) { "nameAndTypeIndex must be a positive number" }
            return DynamicConstant(bootstrapMethodAttrIndex, nameAndTypeIndex)
        }
    }
}
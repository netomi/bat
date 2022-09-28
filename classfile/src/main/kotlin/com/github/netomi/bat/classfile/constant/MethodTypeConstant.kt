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
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import java.io.IOException

/**
 * A constant representing a CONSTANT_MethodType_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.9">CONSTANT_MethodType_info Structure</a>
 */
data class MethodTypeConstant private constructor(private var _descriptorIndex: Int = -1) : Constant() {

    override val type: ConstantType
        get() = ConstantType.METHOD_TYPE

    val descriptorIndex: Int
        get() = _descriptorIndex

    fun getDescriptor(classFile: ClassFile): String {
        return classFile.getString(descriptorIndex)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        _descriptorIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeShort(descriptorIndex)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitMethodTypeConstant(classFile, index, this)
    }

    fun descriptorConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(descriptorIndex, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_descriptorIndex))
    }

    companion object {
        internal fun empty(): MethodTypeConstant {
            return MethodTypeConstant()
        }

        fun of(descriptorIndex: Int): MethodTypeConstant {
            require(descriptorIndex >= 1) { "descriptorIndex must be a positive number" }
            return MethodTypeConstant(descriptorIndex)
        }
    }
}
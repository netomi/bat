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
import com.github.netomi.bat.classfile.constant.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A constant representing a CONSTANT_Class_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.1">CONSTANT_Class_info Structure</a>
 */
data class ClassConstant private constructor(private var _nameIndex: Int = -1) : Constant() {

    override val type: ConstantType
        get() = ConstantType.CLASS

    val nameIndex: Int
        get() = _nameIndex

    fun getClassName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: DataInput) {
        _nameIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: DataOutput) {
        output.writeShort(nameIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ConstantVisitor) {
        visitor.visitClassConstant(classFile, this)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantPoolVisitor) {
        visitor.visitClassConstant(classFile, index, this)
    }

    companion object {
        internal fun empty(): ClassConstant {
            return ClassConstant()
        }

        fun of(nameIndex: Int): ClassConstant {
            require(nameIndex >= 1) { "nameIndex must be a positive number" }
            return ClassConstant(nameIndex)
        }
    }
}
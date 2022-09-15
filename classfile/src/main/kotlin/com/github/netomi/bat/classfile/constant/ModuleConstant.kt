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
 * A constant representing a CONSTANT_Module_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.11">CONSTANT_Module_info Structure</a>
 */
data class ModuleConstant private constructor(private var _nameIndex: Int = -1): Constant() {

    override val type: ConstantType
        get() = ConstantType.MODULE

    val nameIndex: Int
        get() = _nameIndex

    fun getModuleName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        _nameIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeShort(nameIndex)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitModuleConstant(classFile, index, this)
    }

    override fun referencedConstantVisitor(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_nameIndex))
    }

    companion object {
        internal fun empty(): ModuleConstant {
            return ModuleConstant()
        }

        fun of(nameIndex: Int): ModuleConstant {
            require(nameIndex >= 1) { "nameIndex must be a positive number" }
            return ModuleConstant(nameIndex)
        }
    }
}
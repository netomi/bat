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
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.util.JvmClassName
import java.io.IOException

/**
 * An abstract base class for constants representing a member reference,
 * i.e. Fieldref, Methodref or InterfaceMethodref.
 */
abstract class RefConstant(protected open var _classIndex:       Int = -1,
                           protected open var _nameAndTypeIndex: Int = -1): Constant() {

    val classIndex: Int
        get() = _classIndex

    val nameAndTypeIndex: Int
        get() = _nameAndTypeIndex

    fun getClassName(classFile: ClassFile): JvmClassName {
        return classFile.getClassName(classIndex)
    }

    private fun getNameAndType(classFile: ClassFile): NameAndTypeConstant {
        return classFile.getNameAndType(nameAndTypeIndex)
    }

    fun getMemberName(classFile: ClassFile): String {
        return getNameAndType(classFile).getMemberName(classFile)
    }

    fun getDescriptor(classFile: ClassFile): String {
        return getNameAndType(classFile).getDescriptor(classFile)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        _classIndex       = input.readUnsignedShort()
        _nameAndTypeIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeShort(classIndex)
        output.writeShort(nameAndTypeIndex)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitClassConstant(classFile, this, PropertyAccessor(::_classIndex))
        visitor.visitNameAndTypeConstant(classFile, this, PropertyAccessor(::_nameAndTypeIndex))
    }
}
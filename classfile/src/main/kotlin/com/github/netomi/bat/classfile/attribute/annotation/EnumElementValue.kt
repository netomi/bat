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

package com.github.netomi.bat.classfile.attribute.annotation

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.util.JvmType
import com.github.netomi.bat.util.asJvmType
import java.io.IOException

data class EnumElementValue private constructor(private var _typeNameIndex:  Int = -1,
                                                private var _constNameIndex: Int = -1) : ElementValue() {

    override val type: ElementValueType
        get() = ElementValueType.ENUM

    override val contentSize: Int
        get() = 5

    val typeNameIndex: Int
        get() = _typeNameIndex

    val constNameIndex: Int
        get() = _constNameIndex

    fun getType(classFile: ClassFile): JvmType {
        return classFile.getString(typeNameIndex).asJvmType()
    }

    fun getConstName(classFile: ClassFile): String {
        return classFile.getString(constNameIndex)
    }

    @Throws(IOException::class)
    override fun readElementValue(input: ClassDataInput) {
        _typeNameIndex  = input.readUnsignedShort()
        _constNameIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: ClassDataOutput) {
        output.writeShort(_typeNameIndex)
        output.writeShort(_constNameIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ElementValueVisitor) {
        visitor.visitEnumElementValue(classFile, this)
    }

    fun typeNameConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(typeNameIndex, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_typeNameIndex))
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_constNameIndex))
    }

    companion object {
        internal fun empty(): EnumElementValue {
            return EnumElementValue()
        }

        fun of(typeNameIndex: Int, constNameIndex: Int): EnumElementValue {
            return EnumElementValue(typeNameIndex, constNameIndex)
        }
    }
}
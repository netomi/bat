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

package com.github.netomi.bat.classfile.attribute

import com.github.netomi.bat.classfile.AccessFlag
import com.github.netomi.bat.classfile.AccessFlagTarget
import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.accessFlagsToSet
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import com.github.netomi.bat.classfile.io.contentSize
import com.github.netomi.bat.util.JvmClassName
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A class representing an InnerClasses attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.6">InnerClasses Attribute</a>
 */
data class InnerClassesAttribute
    private constructor(override var attributeNameIndex: Int,
                         private var innerClasses:       MutableList<InnerClassEntry> = mutableListOfCapacity(0))
    : Attribute(attributeNameIndex), AttachedToClass, Sequence<InnerClassEntry> {

    override val type: AttributeType
        get() = AttributeType.INNER_CLASSES

    override val dataSize: Int
        get() = innerClasses.contentSize()

    val size: Int
        get() = innerClasses.size

    operator fun get(index: Int): InnerClassEntry {
        return innerClasses[index]
    }

    override fun iterator(): Iterator<InnerClassEntry> {
        return innerClasses.iterator()
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        innerClasses = input.readContentList(InnerClassEntry::read)
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeContentList(innerClasses)
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitInnerClasses(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        super.referencedConstantsAccept(classFile, visitor)
        for (entry in innerClasses) {
            entry.referencedConstantsAccept(classFile, visitor)
        }
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): InnerClassesAttribute {
            return InnerClassesAttribute(attributeNameIndex)
        }
    }
}

data class InnerClassEntry
    private constructor(private var _innerClassIndex:       Int = -1,
                        private var _outerClassIndex:       Int = -1,
                        private var _innerNameIndex:        Int = -1,
                        private var _innerClassAccessFlags: Int = -1): ClassFileContent() {

    override val contentSize: Int
        get() = 8

    val innerClassIndex: Int
        get() = _innerClassIndex

    val outerClassIndex: Int
        get() = _outerClassIndex

    val innerNameIndex: Int
        get() = _innerNameIndex

    val innerClassAccessFlags: Int
        get() = _innerClassAccessFlags

    val innerClassAccessFlagsAsSet: Set<AccessFlag>
        get() = accessFlagsToSet(innerClassAccessFlags, AccessFlagTarget.INNER_CLASS)

    fun getInnerClass(classFile: ClassFile): JvmClassName {
        return classFile.getClassName(innerClassIndex)
    }

    fun getOuterClass(classFile: ClassFile): JvmClassName? {
        return if (outerClassIndex > 0) classFile.getClassName(outerClassIndex) else null
    }

    fun getInnerName(classFile: ClassFile): String? {
        return classFile.getStringOrNull(innerNameIndex)
    }

    private fun read(input: ClassDataInput) {
        _innerClassIndex       = input.readUnsignedShort()
        _outerClassIndex       = input.readUnsignedShort()
        _innerNameIndex        = input.readUnsignedShort()
        _innerClassAccessFlags = input.readUnsignedShort()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(innerClassIndex)
        output.writeShort(outerClassIndex)
        output.writeShort(innerNameIndex)
        output.writeShort(innerClassAccessFlags)
    }

    fun innerClassConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(innerClassIndex, visitor)
    }

    fun outerClassConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(outerClassIndex, visitor)
    }

    fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitClassConstant(classFile, this, PropertyAccessor(::_innerClassIndex))
        if (_outerClassIndex > 0) {
            visitor.visitClassConstant(classFile, this, PropertyAccessor(::_outerClassIndex))
        }
        if (_innerNameIndex > 0) {
            visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_innerNameIndex))
        }
    }

    companion object {
        internal fun read(input: ClassDataInput): InnerClassEntry {
            val element = InnerClassEntry()
            element.read(input)
            return element
        }
    }
}
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

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import java.io.IOException

/**
 * A class representing a SourceFile attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.10">SourceFile Attribute</a>
 */
data class SourceFileAttribute internal constructor(override var attributeNameIndex: Int,
                                                     private var _sourceFileIndex:   Int = -1) : Attribute(attributeNameIndex), AttachedToClass {

    override val type: AttributeType
        get() = AttributeType.SOURCE_FILE

    override val dataSize: Int
        get() = ATTRIBUTE_LENGTH

    val sourceFileIndex: Int
        get() = _sourceFileIndex

    fun getSourceFile(classFile: ClassFile): String {
        return classFile.getString(sourceFileIndex)
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: ClassDataInput, length: Int) {
        assert(length == ATTRIBUTE_LENGTH)
        _sourceFileIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeShort(sourceFileIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitSourceFile(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        super.referencedConstantsAccept(classFile, visitor)
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_sourceFileIndex))
    }

    companion object {
        private const val ATTRIBUTE_LENGTH = 2

        internal fun empty(attributeNameIndex: Int): SourceFileAttribute {
            return SourceFileAttribute(attributeNameIndex)
        }
    }
}
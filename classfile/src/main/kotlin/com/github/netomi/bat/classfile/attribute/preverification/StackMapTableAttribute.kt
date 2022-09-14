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

package com.github.netomi.bat.classfile.attribute.preverification

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.preverification.visitor.StackMapFrameVisitor
import com.github.netomi.bat.classfile.attribute.visitor.CodeAttributeVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.contentSize
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.IOException

/**
 * A class representing an StackMapTable attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.4">StackMapTable Attribute</a>
 */
data class StackMapTableAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var frameEntries:       MutableList<StackMapFrame> = mutableListOfCapacity(0)
    ) : Attribute(attributeNameIndex), AttachedToCodeAttribute, Sequence<StackMapFrame> {

    override val type: AttributeType
        get() = AttributeType.STACK_MAP_TABLE

    override val dataSize: Int
        get() = frameEntries.contentSize()

    val size: Int
        get() = frameEntries.size

    operator fun get(index: Int): StackMapFrame {
        return frameEntries[index]
    }

    override fun iterator(): Iterator<StackMapFrame> {
        return frameEntries.iterator()
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: ClassDataInput, length: Int) {
        frameEntries = input.readContentList(StackMapFrame.Companion::read)
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeContentList(frameEntries)
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, visitor: CodeAttributeVisitor) {
        visitor.visitStackMapTable(classFile, method, code, this)
    }

    fun stackMapFramesAccept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        for (frame in frameEntries) {
            frame.accept(classFile, visitor)
        }
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): StackMapTableAttribute {
            return StackMapTableAttribute(attributeNameIndex)
        }
    }
}
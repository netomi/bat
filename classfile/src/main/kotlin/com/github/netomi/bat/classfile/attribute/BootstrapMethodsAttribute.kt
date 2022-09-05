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

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.DataInput
import java.io.DataOutput

/**
 * A class representing a BootstrapMethods attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.23">BootstrapMethods Attribute</a>
 */
data class BootstrapMethodsAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var bootstrapMethods:   MutableList<BootstrapMethodElement> = mutableListOfCapacity(0)
    ): Attribute(attributeNameIndex), AttachedToClass, Sequence<BootstrapMethodElement> {

    override val type: AttributeType
        get() = AttributeType.BOOTSTRAP_METHOD

    override val dataSize: Int
        get() = 2 + bootstrapMethods.fold(0) { acc, bootstrapMethodElement -> acc + bootstrapMethodElement.dataSize }

    val size: Int
        get() = bootstrapMethods.size

    operator fun get(index: Int): BootstrapMethodElement {
        return bootstrapMethods[index]
    }

    override fun iterator(): Iterator<BootstrapMethodElement> {
        return bootstrapMethods.iterator()
    }

    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        @Suppress("UNUSED_VARIABLE")
        val length = input.readInt()
        val numberOfBootstrapMethods = input.readUnsignedShort()
        bootstrapMethods = mutableListOfCapacity(numberOfBootstrapMethods)
        for (i in 0 until numberOfBootstrapMethods) {
            bootstrapMethods.add(BootstrapMethodElement.read(input))
        }
    }

    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(dataSize)
        output.writeShort(bootstrapMethods.size)
        for (element in bootstrapMethods) {
            element.write(output)
        }
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitBootstrapMethodsAttribute(classFile, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): BootstrapMethodsAttribute {
            return BootstrapMethodsAttribute(attributeNameIndex)
        }
    }
}

data class BootstrapMethodElement
    private constructor(private var _bootstrapMethodRefIndex: Int      = -1,
                        private var bootstrapArguments:       IntArray = IntArray(0)): Sequence<Int> {

    val bootstrapMethodRefIndex: Int
        get() = _bootstrapMethodRefIndex

    val size: Int
        get() = bootstrapArguments.size

    operator fun get(index: Int): Int {
        return bootstrapArguments[index]
    }

    override fun iterator(): Iterator<Int> {
        return bootstrapArguments.iterator()
    }

    internal val dataSize: Int
        get() = 4 + bootstrapArguments.size * 2

    private fun read(input: DataInput) {
        _bootstrapMethodRefIndex = input.readUnsignedShort()
        val numBootstrapArguments = input.readUnsignedShort()
        bootstrapArguments = IntArray(numBootstrapArguments)
        for (i in 0 until numBootstrapArguments) {
            bootstrapArguments[i] = input.readUnsignedShort()
        }
    }

    internal fun write(output: DataOutput) {
        output.writeShort(bootstrapMethodRefIndex)
        output.writeShort(bootstrapArguments.size)
        for (index in bootstrapArguments) {
            output.writeShort(index)
        }
    }

    fun bootstrapMethodRefAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(_bootstrapMethodRefIndex, visitor)
    }

    fun bootstrapArgumentsAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        for (constantIndex in bootstrapArguments) {
            classFile.constantAccept(constantIndex, visitor)
        }
    }

    companion object {
        internal fun read(input: DataInput): BootstrapMethodElement {
            val element = BootstrapMethodElement()
            element.read(input)
            return element
        }
    }
}
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
import com.github.netomi.bat.classfile.io.*
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.util.mutableListOfCapacity
import java.util.*

/**
 * A class representing a BootstrapMethods attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.23">BootstrapMethods Attribute</a>
 */
data class BootstrapMethodsAttribute
    private constructor(override var attributeNameIndex: Int,
                         private var bootstrapMethods:   MutableList<BootstrapMethod> = mutableListOfCapacity(0)
    ): Attribute(attributeNameIndex), AttachedToClass, Sequence<BootstrapMethod> {

    override val type: AttributeType
        get() = AttributeType.BOOTSTRAP_METHOD

    override val dataSize: Int
        get() = bootstrapMethods.contentSize()

    val size: Int
        get() = bootstrapMethods.size

    operator fun get(index: Int): BootstrapMethod {
        return bootstrapMethods[index]
    }

    override fun iterator(): Iterator<BootstrapMethod> {
        return bootstrapMethods.iterator()
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        bootstrapMethods = input.readContentList(BootstrapMethod::read)
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeContentList(bootstrapMethods)
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitBootstrapMethods(classFile, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): BootstrapMethodsAttribute {
            return BootstrapMethodsAttribute(attributeNameIndex)
        }
    }
}

data class BootstrapMethod
    private constructor(private var _bootstrapMethodRefIndex: Int      = -1,
                        private var arguments:                IntArray = IntArray(0)): ClassFileContent(), Sequence<Int> {

    override val contentSize: Int
        get() = 4 + arguments.size * 2

    val bootstrapMethodRefIndex: Int
        get() = _bootstrapMethodRefIndex

    val size: Int
        get() = arguments.size

    operator fun get(index: Int): Int {
        return arguments[index]
    }

    override fun iterator(): Iterator<Int> {
        return arguments.iterator()
    }

    private fun read(input: ClassDataInput) {
        _bootstrapMethodRefIndex = input.readUnsignedShort()
        arguments                = input.readShortIndexArray()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(bootstrapMethodRefIndex)
        output.writeShortIndexArray(arguments)
    }

    fun bootstrapMethodRefAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(_bootstrapMethodRefIndex, visitor)
    }

    fun bootstrapArgumentsAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        for (constantIndex in arguments) {
            classFile.constantAccept(constantIndex, visitor)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BootstrapMethod) return false

        return _bootstrapMethodRefIndex == other._bootstrapMethodRefIndex &&
               arguments.contentEquals(other.arguments)
    }

    override fun hashCode(): Int {
        return Objects.hash(_bootstrapMethodRefIndex, arguments.contentHashCode())
    }

    companion object {
        internal fun read(input: ClassDataInput): BootstrapMethod {
            val element = BootstrapMethod()
            element.read(input)
            return element
        }
    }
}
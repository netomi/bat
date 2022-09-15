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
import com.github.netomi.bat.classfile.attribute.annotation.visitor.AnnotationVisitor
import com.github.netomi.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import com.github.netomi.bat.classfile.io.contentSize
import com.github.netomi.bat.util.JvmType
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.IOException
import java.util.*

open class Annotation
    protected constructor(protected var _typeIndex:  Int                              = -1,
                          protected var _components: MutableList<AnnotationComponent> = mutableListOfCapacity(0))
    : ClassFileContent(), Sequence<AnnotationComponent> {

    override val contentSize: Int
        get() = 2 + _components.contentSize()

    val typeIndex: Int
        get() = _typeIndex

    fun getType(classFile: ClassFile): JvmType {
        return classFile.getType(typeIndex)
    }

    val size: Int
        get() = _components.size

    operator fun get(index: Int): AnnotationComponent {
        return _components[index]
    }

    override fun iterator(): Iterator<AnnotationComponent> {
        return _components.iterator()
    }

    @Throws(IOException::class)
    internal open fun read(input: ClassDataInput) {
        _typeIndex  = input.readUnsignedShort()
        _components = input.readContentList(AnnotationComponent::read)
    }

    @Throws(IOException::class)
    override fun write(output: ClassDataOutput) {
        output.writeShort(_typeIndex)
        output.writeContentList(_components)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Annotation) return false

        return _typeIndex  == other._typeIndex &&
               _components == other._components
    }

    override fun hashCode(): Int {
        return Objects.hash(_typeIndex, _components)
    }

    open fun accept(classFile: ClassFile, visitor: AnnotationVisitor) {
        visitor.visitAnnotation(classFile, this)
    }

    fun elementValuesAccept(classFile: ClassFile, visitor: ElementValueVisitor) {
        for (component in _components) {
            component.elementValue.accept(classFile, visitor)
        }
    }

    fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_typeIndex))
        for (component in _components) {
            component.referencedConstantsAccept(classFile, visitor)
        }
    }

    companion object {
        internal fun empty(): Annotation {
            return Annotation()
        }

        internal fun readAnnotation(input: ClassDataInput): Annotation {
            val annotation = Annotation()
            annotation.read(input)
            return annotation
        }
    }
}

data class AnnotationComponent private constructor(private var _nameIndex:    Int = -1,
                                                   private var _elementValue: ElementValue = ElementValue.empty()): ClassFileContent() {

    override val contentSize: Int
        get() = 2 + _elementValue.contentSize

    val nameIndex: Int
        get() = _nameIndex

    fun getName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    val elementValue: ElementValue
        get() = _elementValue

    private fun read(input: ClassDataInput) {
        _nameIndex    = input.readUnsignedShort()
        _elementValue = ElementValue.read(input)
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(_nameIndex)
        _elementValue.write(output)
    }

    fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_nameIndex))
        elementValue.referencedConstantsAccept(classFile, visitor)
    }

    companion object {
        internal fun read(input: ClassDataInput): AnnotationComponent {
            val entry = AnnotationComponent()
            entry.read(input)
            return entry
        }
    }
}
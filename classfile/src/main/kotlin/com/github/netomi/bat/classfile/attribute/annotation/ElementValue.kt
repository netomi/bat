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
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import java.io.IOException

abstract class ElementValue: ClassFileContent() {

    abstract val type: ElementValueType

    @Throws(IOException::class)
    internal abstract fun readElementValue(input: ClassDataInput)

    @Throws(IOException::class)
    internal abstract fun writeElementValue(output: ClassDataOutput)

    @Throws(IOException::class)
    override fun write(output: ClassDataOutput) {
        output.writeByte(type.tag.code)
        writeElementValue(output)
    }

    abstract fun accept(classFile: ClassFile, visitor: ElementValueVisitor)

    open fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {}

    companion object {
        internal fun empty(): ElementValue {
            // Pick an arbitrary empty element value as default, empty() is used to
            // initialize data classes prior to read content from an input stream.
            return ClassElementValue.empty()
        }

        internal fun read(input : ClassDataInput): ElementValue {
            val tag           = input.readUnsignedByte().toChar()
            val elementValue  = ElementValueType.of(tag).createElementValue()
            elementValue.readElementValue(input)
            return elementValue
        }
    }
}

/**
 * Known element value elements contained in an annotation.
 */
enum class ElementValueType constructor(val tag: Char, private val supplier: () -> ElementValue) {

    // Valid element values and their corresponding tags:
    // https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.7.16.1-130

    BYTE      ('B', { ConstElementValue.create(BYTE) }),
    CHAR      ('C', { ConstElementValue.create(CHAR) }),
    DOUBLE    ('D', { ConstElementValue.create(DOUBLE) }),
    FLOAT     ('F', { ConstElementValue.create(FLOAT) }),
    INT       ('I', { ConstElementValue.create(INT) }),
    LONG      ('J', { ConstElementValue.create(LONG) }),
    SHORT     ('S', { ConstElementValue.create(SHORT) }),
    BOOLEAN   ('Z', { ConstElementValue.create(BOOLEAN) }),
    STRING    ('s', { ConstElementValue.create(STRING) }),
    CLASS     ('c', ClassElementValue.Companion::empty),
    ENUM      ('e', EnumElementValue.Companion::empty),
    ANNOTATION('@', AnnotationElementValue.Companion::empty),
    ARRAY     ('[', ArrayElementValue.Companion::empty);

    companion object {
        private val tagToElementValueMap: Map<Char, ElementValueType> by lazy {
            values().associateBy { it.tag }
        }

        fun of(tag: Char) : ElementValueType {
            return tagToElementValueMap[tag] ?: throw IllegalArgumentException("unknown element value tag '$tag'")
        }
    }

    fun createElementValue(): ElementValue {
        return supplier.invoke()
    }
}

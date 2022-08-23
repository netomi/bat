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
package com.github.netomi.bat.classfile

import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.FieldAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.classAttributes
import com.github.netomi.bat.classfile.attribute.visitor.fieldAttributes
import com.github.netomi.bat.util.toHexString
import java.io.DataInput
import java.io.IOException

/**
 * A class representing a field in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.5">Field_info structure</a>
 */
class Field private constructor(): Member() {

    override val accessFlagTarget: AccessFlagTarget
        get() = AccessFlagTarget.FIELD

    fun attributesAccept(classFile: ClassFile, visitor: FieldAttributeVisitor) {
        val adapter = if (visitor is AttributeVisitor) visitor else fieldAttributes(visitor)
        for (attribute in attributes) {
            attribute.accept(classFile, adapter)
        }
    }

    override fun toString(): String {
        return "Field[nameIndex=%d,descriptorIndex=%d,accessFlags=%s]".format(nameIndex, descriptorIndex, toHexString(accessFlags, 4))
    }

    companion object {
        @Throws(IOException::class)
        internal fun readField(input: DataInput, classFile: ClassFile): Field {
            val field = Field()
            field.read(input, classFile)
            return field
        }
    }
}
/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotations.visitor.ElementValueVisitor
import com.github.netomi.bat.util.JvmType
import com.github.netomi.bat.util.asJvmType
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class Annotation internal constructor(
    var typeIndex:     Int                                  = -1,
    var elementValues: MutableList<Pair<Int, ElementValue>> = mutableListOf()) {

    fun getType(classFile: ClassFile): String {
        return classFile.getString(typeIndex)
    }

    fun getJvmType(classFile: ClassFile): JvmType {
        return getType(classFile).asJvmType()
    }

    @Throws(IOException::class)
    private fun read(input: DataInput) {
        typeIndex = input.readUnsignedShort()

        val elementValuesCount = input.readUnsignedShort()
        for (i in 0 until elementValuesCount) {
            val elementNameIndex = input.readUnsignedShort()
            elementValues.add(Pair(elementNameIndex, ElementValue.read(input)))
        }
    }

    @Throws(IOException::class)
    fun write(output: DataOutput) {
        output.writeShort(typeIndex)
        output.writeShort(elementValues.size)
        elementValues.forEach { (elementNameIndex, elementValue) ->
            output.writeShort(elementNameIndex)
            elementValue.write(output)
        }
    }

    fun acceptElementValues(classFile: ClassFile, visitor: ElementValueVisitor) {
        elementValues.forEach { (_, elementValue) -> elementValue.accept(classFile, visitor) }
    }

    companion object {
        internal fun readAnnotation(input: DataInput): Annotation {
            val annotation = Annotation()
            annotation.read(input)
            return annotation
        }
    }
}
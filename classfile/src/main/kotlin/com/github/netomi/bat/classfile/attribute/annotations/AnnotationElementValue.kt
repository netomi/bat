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
import com.github.netomi.bat.classfile.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class AnnotationElementValue internal constructor(var annotation: Annotation = Annotation()) : ElementValue() {

    override val type: Type
        get() = Type.ANNOTATION

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        annotation = Annotation.readAnnotation(input)
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        annotation.write(output)
    }

    override fun accept(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, visitor: ElementValueVisitor) {
        visitor.visitAnnotationElementValue(classFile, annotation, index, elementName, this)
    }

    companion object {
        @JvmStatic
        fun create(): AnnotationElementValue {
            return AnnotationElementValue()
        }
    }
}
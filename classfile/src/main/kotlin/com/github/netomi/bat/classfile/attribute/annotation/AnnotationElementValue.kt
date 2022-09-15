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
import java.io.IOException

data class AnnotationElementValue private constructor(private var _annotation: Annotation = Annotation.empty()) : ElementValue() {

    override val type: ElementValueType
        get() = ElementValueType.ANNOTATION

    override val contentSize: Int
        get() = 1 + _annotation.contentSize

    val annotation: Annotation
        get() = _annotation

    @Throws(IOException::class)
    override fun readElementValue(input: ClassDataInput) {
        _annotation = Annotation.readAnnotation(input)
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: ClassDataOutput) {
        annotation.write(output)
    }

    override fun accept(classFile: ClassFile, visitor: ElementValueVisitor) {
        visitor.visitAnnotationElementValue(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        annotation.referencedConstantsAccept(classFile, visitor)
    }

    companion object {
        internal fun empty(): AnnotationElementValue {
            return AnnotationElementValue()
        }
    }
}
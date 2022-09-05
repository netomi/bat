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
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

// TODO: finish implementation
class TypeAnnotation private constructor(): Annotation() {

    override val dataSize: Int
        get() = TODO("implement")

    @Throws(IOException::class)
    override fun read(input: DataInput) {
        super.read(input)
    }

    @Throws(IOException::class)
    override fun write(output: DataOutput) {
        super.write(output)
    }

    override fun accept(classFile: ClassFile, visitor: AnnotationVisitor) {
        visitor.visitTypeAnnotation(classFile, this)
    }

    companion object {
        internal fun empty(): TypeAnnotation {
            return TypeAnnotation()
        }

        internal fun readTypeAnnotation(input: DataInput): TypeAnnotation {
            val annotation = TypeAnnotation()
            annotation.read(input)
            return annotation
        }
    }
}
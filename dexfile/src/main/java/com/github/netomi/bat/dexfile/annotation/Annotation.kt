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
package com.github.netomi.bat.dexfile.annotation

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.TYPE_ANNOTATION_ITEM
import com.github.netomi.bat.dexfile.annotation.AnnotationVisibility.Companion.of
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.value.EncodedAnnotationValue
import com.github.netomi.bat.dexfile.value.EncodedValue.Companion.readAnnotationValue
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import java.util.*

/**
 * A class representing an annotation item inside a dex file.
 *
 * @see [annotation item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.annotation-item)
 */
@DataItemAnn(
    type          = TYPE_ANNOTATION_ITEM,
    dataAlignment = 1,
    dataSection   = true)
class Annotation private constructor(_visibility:      AnnotationVisibility   = AnnotationVisibility.BUILD,
                                     _annotationValue: EncodedAnnotationValue = EncodedAnnotationValue()) : DataItem() {

    var visibility: AnnotationVisibility = _visibility
        private set

    var annotationValue: EncodedAnnotationValue = _annotationValue
        private set

    override val isEmpty: Boolean
        get() = false

    override fun read(input: DexDataInput) {
        val visibilityValue = input.readUnsignedByte().toInt()
        visibility      = of(visibilityValue)
        annotationValue = readAnnotationValue(input)
    }

    override fun write(output: DexDataOutput) {
        output.writeUnsignedByte(visibility.value)
        annotationValue.write(output)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        annotationValue.referencedIDsAccept(dexFile, visitor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        val o = other as Annotation

        return visibility      == o.visibility &&
               annotationValue == o.annotationValue
    }

    override fun hashCode(): Int {
        return Objects.hash(visibility, annotationValue)
    }

    override fun toString(): String {
        return "Annotation[visibility='%s',value=%s]".format(visibility.simpleName, annotationValue)
    }

    companion object {
        fun of(visibility: AnnotationVisibility, value: EncodedAnnotationValue): Annotation {
            return Annotation(visibility, value)
        }

        fun readContent(input: DexDataInput): Annotation {
            val annotation = Annotation()
            annotation.read(input)
            return annotation
        }
    }
}
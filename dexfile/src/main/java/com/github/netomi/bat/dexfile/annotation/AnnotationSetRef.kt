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

import com.github.netomi.bat.dexfile.DataItem
import com.github.netomi.bat.dexfile.DexContent
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.annotation.visitor.AnnotationVisitor
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor

/**
 * A class representing an annotation set ref item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#set-ref-item">annotation set ref item @ dex format</a>
 */
class AnnotationSetRef private constructor(annotationSet: AnnotationSet = AnnotationSet.empty()) : DexContent() {

    var annotationsOffset = 0
        private set

    var annotationSet: AnnotationSet = annotationSet
        private set

    override fun read(input: DexDataInput) {
        annotationsOffset = input.readInt()
    }

    val isEmpty: Boolean
        get() = annotationSet.isEmpty

    override fun readLinkedDataItems(input: DexDataInput) {
        if (annotationsOffset != 0) {
            input.offset = annotationsOffset
            annotationSet = AnnotationSet.read(input)
            annotationSet.readLinkedDataItems(input)
        }
    }

    override fun updateOffsets(dataItemMap: DataItem.Map) {
        annotationsOffset = dataItemMap.getOffset(annotationSet)
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(annotationsOffset)
    }

    fun accept(dexFile: DexFile, visitor: AnnotationVisitor) {
        annotationSet.accept(dexFile, visitor)
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitAnnotationSet(dexFile, this, annotationSet)
        annotationSet.dataItemsAccept(dexFile, visitor)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        annotationSet.referencedIDsAccept(dexFile, visitor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val o = other as AnnotationSetRef

        return annotationSet == o.annotationSet
    }

    override fun hashCode(): Int {
        return annotationSet.hashCode()
    }

    override fun toString(): String {
        return "AnnotationSetRef[annotationSet=${annotationSet.size} items]"
    }

    companion object {
        fun of(annotationSet: AnnotationSet): AnnotationSetRef {
            return AnnotationSetRef(annotationSet)
        }

        internal fun read(input: DexDataInput): AnnotationSetRef {
            val annotationSetRef = AnnotationSetRef()
            annotationSetRef.read(input)
            return annotationSetRef
        }
    }
}
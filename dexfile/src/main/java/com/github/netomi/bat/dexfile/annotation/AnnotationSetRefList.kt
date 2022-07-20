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
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.AnnotationVisitor
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import java.util.*
import kotlin.collections.ArrayList

/**
 * A class representing an annotation set ref list inside a dex file.
 *
 * @see [annotation set ref list @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.set-ref-list)
 */
@DataItemAnn(
    type          = TYPE_ANNOTATION_SET_REF_LIST,
    dataAlignment = 4,
    dataSection   = true)
class AnnotationSetRefList private constructor(val annotationSetRefs: ArrayList<AnnotationSetRef> = ArrayList(0)) : DataItem() {

    val annotationSetRefCount: Int
        get() = annotationSetRefs.size

    fun getAnnotationSetRef(index: Int): AnnotationSetRef {
        return annotationSetRefs[index]
    }

    override val isEmpty: Boolean
        get() = !annotationSetRefs.any { !it.isEmpty }

    override fun read(input: DexDataInput) {
        val size = input.readInt()
        annotationSetRefs.clear()
        annotationSetRefs.ensureCapacity(size)
        for (i in 0 until size) {
            val annotationSetRef = AnnotationSetRef.readContent(input)
            annotationSetRefs.add(annotationSetRef)
        }
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        for (annotationSetRef in annotationSetRefs) {
            annotationSetRef.readLinkedDataItems(input)
        }
    }

    override fun updateOffsets(dataItemMap: Map) {
        for (annotationSetRef in annotationSetRefs) {
            annotationSetRef.updateOffsets(dataItemMap)
        }
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(annotationSetRefs.size)
        for (annotationSetRef in annotationSetRefs) {
            annotationSetRef.write(output)
        }
    }

    fun accept(dexFile: DexFile, classDef: ClassDef, index: Int, visitor: AnnotationVisitor) {
        if (index in 0 until annotationSetRefCount) {
            val annotationSetRef = annotationSetRefs[index]
            annotationSetRef.accept(dexFile, classDef, visitor)
        }
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        for (annotationSetRef in annotationSetRefs) {
            annotationSetRef.dataItemsAccept(dexFile, visitor)
        }
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        annotationSetRefs.forEach { it.referencedIDsAccept(dexFile, visitor) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        val o = other as AnnotationSetRefList

        return annotationSetRefs == o.annotationSetRefs
    }

    override fun hashCode(): Int {
        return Objects.hash(annotationSetRefs)
    }

    override fun toString(): String {
        return "AnnotationSetRefList[annotationSetRefs=${annotationSetRefs.size} items]"
    }

    companion object {
        fun empty(): AnnotationSetRefList {
            return AnnotationSetRefList()
        }

        fun readContent(input: DexDataInput): AnnotationSetRefList {
            val annotationSetRefList = AnnotationSetRefList()
            annotationSetRefList.read(input)
            return annotationSetRefList
        }
    }
}
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
import com.github.netomi.bat.dexfile.annotation.visitor.AnnotationVisitor
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A class representing an annotation set item inside a dex file.
 *
 * @see [annotation set item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.annotation-set-item)
 */
@DataItemAnn(
    type          = TYPE_ANNOTATION_SET_ITEM,
    dataAlignment = 4,
    dataSection   = true)
class AnnotationSet
    private constructor(private var annotations: MutableList<Annotation> = mutableListOfCapacity(0)): DataItem(), Sequence<Annotation> {

    private var annotationOffsetEntries: IntArray = intArrayOf()

    val annotationCount: Int
        get() = annotations.size

    fun getAnnotation(index: Int): Annotation {
        return annotations[index]
    }

    internal fun addAnnotation(dexFile: DexFile, annotation: Annotation) {
        require(!annotations.any { it.annotationValue.typeIndex == annotation.annotationValue.typeIndex })
            { "annotation with type '${dexFile.getType(annotation.annotationValue.typeIndex)}' already exists in this AnnotationSet" }
        annotations.add(annotation)
    }

    override fun iterator(): Iterator<Annotation> {
        return annotations.iterator()
    }

    override val isEmpty: Boolean
        get() = annotations.isEmpty()

    internal fun sort() {
        annotations.sortWith(compareBy { it.annotationValue.typeIndex })
    }

    override fun read(input: DexDataInput) {
        val size = input.readInt()
        if (annotationOffsetEntries.size != size) {
            annotationOffsetEntries = IntArray(size)
        }
        for (i in annotationOffsetEntries.indices) {
            annotationOffsetEntries[i] = input.readInt()
        }
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        annotations = mutableListOfCapacity(annotationOffsetEntries.size)
        for (i in annotationOffsetEntries.indices) {
            input.offset = annotationOffsetEntries[i]
            val annotation = Annotation.read(input)
            annotations.add(i, annotation)
        }
    }

    override fun updateOffsets(dataItemMap: Map) {
        if (annotationOffsetEntries.size != annotations.size) {
            annotationOffsetEntries = IntArray(annotations.size)
        }
        for (i in annotations.indices) {
            val offset = dataItemMap.getOffset(annotations[i])
            annotationOffsetEntries[i] = offset
        }
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(annotationOffsetEntries.size)
        for (element in annotationOffsetEntries) {
            output.writeInt(element)
        }
    }

    fun accept(dexFile: DexFile, classDef: ClassDef, visitor: AnnotationVisitor) {
        annotations.forEachIndexed { index, annotation -> annotation.accept(dexFile, classDef, this, index, visitor) }
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        annotations.forEachIndexed { index, annotation -> visitor.visitAnnotation(dexFile, this, index, annotation) }
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        annotations.forEach { it.referencedIDsAccept(dexFile, visitor) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        val o = other as AnnotationSet

        return annotations == o.annotations
    }

    override fun hashCode(): Int {
        return annotations.hashCode()
    }

    override fun toString(): String {
        return "AnnotationSet[annotations=${annotations.size} items]"
    }

    companion object {
        internal fun empty(): AnnotationSet {
            return AnnotationSet()
        }

        fun of(annotations: List<Annotation>): AnnotationSet {
            return AnnotationSet(annotations.toMutableList())
        }

        internal fun read(input: DexDataInput): AnnotationSet {
            val annotationSet = AnnotationSet()
            annotationSet.read(input)
            return annotationSet
        }
    }
}
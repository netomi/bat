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
package com.github.netomi.bat.dexfile.visitor

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.annotation.AnnotationSet
import java.util.function.BiConsumer

fun multiAnnotationVisitorOf(visitor: AnnotationVisitor, vararg visitors: AnnotationVisitor): AnnotationVisitor {
    return MultiAnnotationVisitor(visitor, *visitors)
}

fun interface AnnotationVisitor {
    fun visitAnnotation(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet, index: Int, annotation: Annotation)

    fun joinedByAnnotationConsumer(consumer: BiConsumer<DexFile, Annotation>): AnnotationVisitor {
        val joiner: AnnotationVisitor = object : AnnotationVisitor {
            private var firstVisited = false
            override fun visitAnnotation(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet, index: Int, annotation: Annotation) {
                if (firstVisited) {
                    consumer.accept(dexFile, annotation)
                } else {
                    firstVisited = true
                }
            }
        }
        return multiAnnotationVisitorOf(joiner, this)
    }
}

private class MultiAnnotationVisitor constructor(       visitor:       AnnotationVisitor,
                                                 vararg otherVisitors: AnnotationVisitor)

    : AbstractMultiVisitor<AnnotationVisitor>(visitor, *otherVisitors), AnnotationVisitor {

    override fun visitAnnotation(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet, index: Int, annotation: Annotation) {
        for (visitor in visitors) {
            visitor.visitAnnotation(dexFile, classDef, annotationSet, index, annotation)
        }
    }
}
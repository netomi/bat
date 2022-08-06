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
package com.github.netomi.bat.dexfile.annotation.visitor

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.annotation.*

fun allAnnotations(visitor: AnnotationVisitor): AnnotationSetVisitor {
    return AnnotationSetVisitor { dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet -> annotationSet.accept(dexFile, classDef, visitor) }
}

fun interface AnnotationSetVisitor {
    fun visitAnyAnnotationSet(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet)

    fun visitClassAnnotationSet(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet) {
        visitAnyAnnotationSet(dexFile, classDef, annotationSet)
    }

    fun visitFieldAnnotationSet(dexFile: DexFile, classDef: ClassDef, fieldAnnotation: FieldAnnotation, annotationSet: AnnotationSet) {
        visitAnyAnnotationSet(dexFile, classDef, annotationSet)
    }

    fun visitMethodAnnotationSet(dexFile: DexFile, classDef: ClassDef, methodAnnotation: MethodAnnotation, annotationSet: AnnotationSet) {
        visitAnyAnnotationSet(dexFile, classDef, annotationSet)
    }

    fun visitParameterAnnotationSet(dexFile: DexFile, classDef: ClassDef, parameterAnnotation: ParameterAnnotation, parameterIndex: Int, annotationSet: AnnotationSet) {
        visitAnyAnnotationSet(dexFile, classDef, annotationSet)
    }

    fun visitParameterAnnotationSetRefList(dexFile: DexFile, classDef: ClassDef, parameterAnnotation: ParameterAnnotation, annotationSetRefList: AnnotationSetRefList) {
        for (index in 0 until annotationSetRefList.annotationSetRefCount) {
            visitParameterAnnotationSet(dexFile, classDef, parameterAnnotation, index, annotationSetRefList.getAnnotationSetRef(index).annotationSet)
        }
    }
}
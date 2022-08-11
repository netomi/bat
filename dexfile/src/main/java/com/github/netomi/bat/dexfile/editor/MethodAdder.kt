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

package com.github.netomi.bat.dexfile.editor

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.annotation.AnnotationSet
import com.github.netomi.bat.dexfile.annotation.ParameterAnnotation
import com.github.netomi.bat.dexfile.annotation.editor.copyTo
import com.github.netomi.bat.dexfile.annotation.visitor.AnnotationSetVisitor
import com.github.netomi.bat.dexfile.visitor.EncodedMethodVisitor

internal class MethodAdder constructor(private val targetClassDefEditor: ClassDefEditor): EncodedMethodVisitor {

    private val targetDexEditor = targetClassDefEditor.dexEditor

    override fun visitAnyMethod(dexFile: DexFile, classDef: ClassDef, index: Int, method: EncodedMethod) {
        val addedMethodEditor =
            targetClassDefEditor.addMethod(method.getName(dexFile),
                                           method.accessFlags,
                                           method.getParameterTypes(dexFile),
                                           method.getReturnType(dexFile))

        val addedMethod = addedMethodEditor.method

        method.codeAccept(dexFile, classDef, CodeAdder(addedMethodEditor))

        method.annotationSetAccept(dexFile, classDef) { _, _, methodAnnotations ->
            if (!methodAnnotations.isEmpty) {
                val targetAnnotationSet = targetClassDefEditor.addOrGetMethodAnnotationSet(addedMethod)
                methodAnnotations.copyTo(dexFile, targetDexEditor, targetAnnotationSet)
            }
        }

        classDef.parameterAnnotationSetRefListAccept(dexFile, classDef, method, object: AnnotationSetVisitor {
            override fun visitAnyAnnotationSet(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet) {}

            override fun visitParameterAnnotationSet(dexFile: DexFile, classDef: ClassDef, parameterAnnotation: ParameterAnnotation, parameterIndex: Int, annotationSet: AnnotationSet) {
                if (!annotationSet.isEmpty) {
                    val targetAnnotationSet = targetClassDefEditor.addOrGetParameterAnnotationSet(addedMethod, parameterIndex)
                    annotationSet.copyTo(dexFile, targetDexEditor, targetAnnotationSet)
                }
            }
        })
    }
}
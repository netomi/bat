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
import com.github.netomi.bat.dexfile.annotation.editor.copyTo
import com.github.netomi.bat.dexfile.visitor.EncodedMethodVisitor

class MethodAdder constructor(private val targetClassDefEditor: ClassDefEditor): EncodedMethodVisitor {

    private val targetDexEditor = targetClassDefEditor.dexEditor
    private val targetDexFile   = targetDexEditor.dexFile

    override fun visitAnyMethod(dexFile: DexFile, classDef: ClassDef, index: Int, method: EncodedMethod) {
        val addedMethodEditor =
            targetClassDefEditor.addMethod(method.getName(dexFile),
                                           method.accessFlags,
                                           method.getParameterTypes(dexFile),
                                           method.getReturnType(dexFile))

        val addedMethod = addedMethodEditor.method

        classDef.annotationsDirectory.methodAnnotationSetAccept(dexFile, classDef, method) { _, _, methodAnnotations ->
            if (!methodAnnotations.isEmpty) {
                val targetAnnotationSet = targetClassDefEditor.addOrGetMethodAnnotationSet(addedMethod)

                for (index in 0 until methodAnnotations.annotationCount) {
                    val targetAnnotation = methodAnnotations.getAnnotation(index).copyTo(dexFile, targetDexEditor)
                    targetAnnotationSet.addAnnotation(targetDexFile, targetAnnotation)
                }
            }
        }
    }
}
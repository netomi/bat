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
import com.github.netomi.bat.dexfile.annotation.editor.copyTo
import com.github.netomi.bat.dexfile.visitor.ClassDefVisitor

class ClassDefAdder constructor(private val targetDexEditor: DexEditor): ClassDefVisitor {

    private val targetDexFile = targetDexEditor.dexFile

    constructor(dexFile: DexFile): this(DexEditor.of(dexFile))

    override fun visitClassDef(dexFile: DexFile, index: Int, classDef: ClassDef) {
        val targetClassDefEditor =
            targetDexEditor.addClassDef(classDef.getType(dexFile),
                                        classDef.accessFlags,
                                        classDef.getSuperClassType(dexFile),
                                        classDef.getSourceFile(dexFile))

        classDef.interfacesAccept(dexFile) { _, _, _, _, type -> targetClassDefEditor.addInterface(type) }

        val classAnnotations = classDef.annotationsDirectory.classAnnotations
        if (!classAnnotations.isEmpty) {
            val targetClassDef = targetClassDefEditor.classDef
            val targetAnnotationSet = targetClassDef.annotationsDirectory.classAnnotations

            for (index in 0 .. classAnnotations.annotationCount) {
                val targetAnnotation = classAnnotations.getAnnotation(index).copyTo(dexFile, targetDexEditor)
                targetAnnotationSet.addAnnotation(targetDexFile, targetAnnotation)
            }
        }

        classDef.fieldsAccept(dexFile, FieldAdder(targetClassDefEditor))
    }
}
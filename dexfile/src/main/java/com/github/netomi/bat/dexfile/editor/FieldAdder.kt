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
import com.github.netomi.bat.dexfile.EncodedField
import com.github.netomi.bat.dexfile.annotation.editor.copyTo
import com.github.netomi.bat.dexfile.value.editor.copyTo
import com.github.netomi.bat.dexfile.visitor.EncodedFieldVisitor

class FieldAdder constructor(private val targetClassDefEditor: ClassDefEditor): EncodedFieldVisitor {

    private val targetDexEditor = targetClassDefEditor.dexEditor

    override fun visitAnyField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        val addedField = targetClassDefEditor.addField(field.getName(dexFile), field.accessFlags, field.getType(dexFile))
        field.staticValueAccept(dexFile) { _, value ->
            targetClassDefEditor.setStaticValue(addedField, value.copyTo(dexFile, targetDexEditor))
        }

        classDef.fieldAnnotationSetAccept(dexFile, classDef, field) { _, _, fieldAnnotations ->
            if (!fieldAnnotations.isEmpty) {
                val targetAnnotationSet = targetClassDefEditor.addOrGetFieldAnnotationSet(addedField)
                fieldAnnotations.copyTo(dexFile, targetDexEditor, targetAnnotationSet)
            }
        }
    }
}
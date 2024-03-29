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
import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.debug.DebugInfo

class MethodEditor private constructor(val dexEditor: DexEditor, private val classDefEditor: ClassDefEditor, val method: EncodedMethod) {

    private val dexFile: DexFile
        get() = dexEditor.dexFile

    private val classDef: ClassDef
        get() = classDefEditor.classDef

    fun addCode(): CodeEditor {
        // we can already compute the insSize
        val protoID = method.getProtoID(dexFile)
        var insSize = if (method.isStatic) 0 else 1
        val argumentSize = protoID.getParametersArgumentSize(dexFile)
        insSize += argumentSize

        val code = Code.of(registersSize = 0, insSize = insSize, outsSize = 0)
        code.debugInfo = DebugInfo.empty(protoID.parameters.size)

        method.code = code
        return CodeEditor.of(dexEditor, classDef, method, code)
    }

    fun addAnnotation(annotation: Annotation) {
        val annotationSet = classDefEditor.addOrGetMethodAnnotationSet(method)
        annotationSet.addAnnotation(dexFile, annotation)
    }

    fun addParameterAnnotation(parameterIndex: Int, annotation: Annotation) {
        val annotationSet = classDefEditor.addOrGetParameterAnnotationSet(method, parameterIndex)
        annotationSet.addAnnotation(dexFile, annotation)
    }

    companion object {
        fun of(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod): MethodEditor {
            val dexEditor = DexEditor.of(dexFile)
            return MethodEditor(dexEditor, ClassDefEditor.of(dexEditor, classDef), method)
        }

        fun of(dexEditor: DexEditor, classDefEditor: ClassDefEditor, method: EncodedMethod): MethodEditor {
            return MethodEditor(dexEditor, classDefEditor, method)
        }
    }
}
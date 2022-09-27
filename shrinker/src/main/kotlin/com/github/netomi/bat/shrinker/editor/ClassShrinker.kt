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

package com.github.netomi.bat.shrinker.editor

import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.editor.ClassEditor
import com.github.netomi.bat.shrinker.classfile.AnyClass
import com.github.netomi.bat.shrinker.marker.UsageMarker
import com.github.netomi.bat.shrinker.classfile.ProgramClass
import com.github.netomi.bat.shrinker.visitor.AnyClassVisitor

class ClassShrinker constructor(private val usageMarker: UsageMarker): AnyClassVisitor {

    override fun visitAnyClass(clazz: AnyClass) {}

    override fun visitProgramClass(clazz: ProgramClass) {
        val methodsToRemove = mutableListOf<Method>()
        clazz.methodsAccept { _, method -> if (usageMarker.isNotUsed(method)) methodsToRemove.add(method) }

        val classEditor = ClassEditor.of(clazz)
        for (method in methodsToRemove) {
            classEditor.removeMethod(method)
        }
    }
}
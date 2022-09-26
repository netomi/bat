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

package com.github.netomi.bat.shrinker.util

import com.github.netomi.bat.classfile.util.ClassPool
import com.github.netomi.bat.classfile.visitor.ClassPoolVisitor
import com.github.netomi.bat.shrinker.classfile.AnalysisClass
import com.github.netomi.bat.shrinker.visitor.AnalysisClassVisitor

internal class ClassInitializer constructor(private val context: AnalysisContext): ClassPoolVisitor<AnalysisClass>, AnalysisClassVisitor {
    override fun visitClassPool(classPool: ClassPool<out AnalysisClass>) {
        classPool.classesAccept(this)
    }

    override fun visitAnyWPOClass(clazz: AnalysisClass) {
        val superClassName = clazz.superClassName
        if (superClassName != null) {
            val superClass   = context.getClass(superClassName)
            clazz.superClass = superClass
            superClass?.addSubClass(clazz)
        }

        for (interfaceClassName in clazz.interfaces) {
            val interfaceClass = context.getClass(interfaceClassName)
            if (interfaceClass != null) {
                clazz.interfaceClasses.add(interfaceClass)
                interfaceClass.addSubClass(clazz)
            }
        }
    }
}
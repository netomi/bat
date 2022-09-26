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

package com.github.netomi.bat.shrinker.marker

import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.Visibility
import com.github.netomi.bat.shrinker.classfile.AnalysisClass
import com.github.netomi.bat.shrinker.classfile.LibraryClass
import com.github.netomi.bat.shrinker.classfile.ProgramClass
import com.github.netomi.bat.shrinker.visitor.AnalysisClassVisitor
import com.github.netomi.bat.shrinker.visitor.AnalysisMethodVisitor
import com.github.netomi.bat.shrinker.visitor.filterMethodsByNameAndDescriptor

class ClassUsageMarker constructor(private val marker: UsageMarker): AnalysisClassVisitor, AnalysisMethodVisitor {

    override fun visitAnyWPOClass(clazz: AnalysisClass) {}

    override fun visitProgramClass(clazz: ProgramClass) {}

    override fun visitLibraryClass(clazz: LibraryClass) {
        if (!marker.isUsed(clazz)) {
            marker.markAsUsed(clazz)

            clazz.superClass?.accept(this)

            for (interfaceClass in clazz.interfaceClasses) {
                interfaceClass.accept(this)
            }

            clazz.methodsAccept(this)
        }
    }

    override fun visitAnyMethod(clazz: AnalysisClass, method: Method) {}

    override fun visitProgramMethod(clazz: AnalysisClass, method: Method) {
        if (!marker.isUsed(method)) {
            marker.markAsUsed(method)
        }
    }

    override fun visitLibraryMethod(clazz: AnalysisClass, method: Method) {
        if (!marker.isUsed(method)) {
            marker.markAsUsed(method)

            markMethodHierarchy(clazz, method)
        }
    }

    private fun markMethodHierarchy(clazz: AnalysisClass, method: Method) {
        if (!method.isInitializer(clazz) &&
            !method.isStatic             &&
             method.visibility >= Visibility.PACKAGE_PRIVATE) {
            for (subClass in clazz.subClasses) {
                subClass.methodsAccept(
                    filterMethodsByNameAndDescriptor(method.getName(clazz),
                                                     method.getDescriptor(clazz),
                    this)
                )
            }
        }
    }
}
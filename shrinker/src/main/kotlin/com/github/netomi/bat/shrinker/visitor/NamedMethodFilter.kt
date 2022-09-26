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

package com.github.netomi.bat.shrinker.visitor

import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.shrinker.classfile.AnalysisClass
import com.github.netomi.bat.shrinker.classfile.accept

fun filterMethodsByNameAndDescriptor(name: String, descriptor: String, visitor: AnalysisMethodVisitor): AnalysisMethodVisitor {
    return MethodFilter(name, descriptor, visitor)
}

private class MethodFilter constructor(val name:       String,
                                       val descriptor: String,
                                       val visitor: AnalysisMethodVisitor): AnalysisMethodVisitor {

    override fun visitAnyMethod(clazz: AnalysisClass, method: Method) {
        if (name       == method.getName(clazz) &&
            descriptor == method.getDescriptor(clazz)) {
            method.accept(clazz, visitor)
        }
    }
}
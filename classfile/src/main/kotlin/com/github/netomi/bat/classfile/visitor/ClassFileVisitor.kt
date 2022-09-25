/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.classfile.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.util.StringMatcher
import com.github.netomi.bat.util.classNameMatcher

fun ClassFileVisitor.filteredByExternalClassName(regularExpression: String): ClassFileVisitor {
    return filteredByExternalClassName(regularExpression, this)
}

fun filteredByExternalClassName(regularExpression: String, visitor: ClassFileVisitor): ClassFileVisitor {
    return ExternalClassNameFilter(regularExpression, visitor)
}

fun allMethods(visitor: MethodVisitor): ClassFileVisitor {
    return ClassFileVisitor { it.methodsAccept(visitor) }
}

fun interface ClassFileVisitor {
    fun visitClassFile(classFile: ClassFile)
}

private class ExternalClassNameFilter(regularExpression: String, private val visitor: ClassFileVisitor) : ClassFileVisitor {

    private val matcher: StringMatcher = classNameMatcher(regularExpression)

    override fun visitClassFile(classFile: ClassFile) {
        val externalClassName = classFile.className.toExternalClassName()
        if (accepted(externalClassName)) {
            classFile.accept(visitor)
        }
    }

    private fun accepted(className: String): Boolean {
        return matcher.matches(className)
    }
}

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
package com.github.netomi.bat.dexfile.visitor

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.util.Classes
import com.github.netomi.bat.util.StringMatcher
import com.github.netomi.bat.util.classNameMatcher

fun multiClassDefVisitorOf(visitor: ClassDefVisitor, vararg visitors: ClassDefVisitor): ClassDefVisitor {
    return MultiClassDefVisitor(visitor, *visitors)
}

fun filteredByExternalClassName(regularExpression: String, visitor: ClassDefVisitor): ClassDefVisitor {
    return ExternalClassNameFilter(regularExpression, visitor)
}

fun interface ClassDefVisitor {
    fun visitClassDef(dexFile: DexFile, index: Int, classDef: ClassDef)
}

private class ExternalClassNameFilter(regularExpression: String, private val visitor: ClassDefVisitor) : ClassDefVisitor {

    private val matcher: StringMatcher = classNameMatcher(regularExpression)

    override fun visitClassDef(dexFile: DexFile, index: Int, classDef: ClassDef) {
        val className = classDef.getClassName(dexFile)

        if (accepted(Classes.externalClassNameFromInternalName(className))) {
            visitor.visitClassDef(dexFile, index, classDef)
        }
    }

    private fun accepted(className: String): Boolean {
        return matcher.matches(className)
    }
}

private class MultiClassDefVisitor constructor(       visitor:       ClassDefVisitor,
                                               vararg otherVisitors: ClassDefVisitor)

    : AbstractMultiVisitor<ClassDefVisitor>(visitor, *otherVisitors), ClassDefVisitor {

    override fun visitClassDef(dexFile: DexFile, index: Int, classDef: ClassDef) {
        for (visitor in visitors) {
            visitor.visitClassDef(dexFile, index, classDef)
        }
    }
}
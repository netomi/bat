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
import com.github.netomi.bat.util.StringMatcher
import com.github.netomi.bat.util.asInternalJavaClassName
import com.github.netomi.bat.util.classNameMatcher

fun multiClassDefVisitorOf(visitor: ClassDefVisitor, vararg visitors: ClassDefVisitor): ClassDefVisitor {
    return MultiClassDefVisitor(visitor, *visitors)
}

fun filteredByExternalClassName(regularExpression: String, visitor: ClassDefVisitorIndexed): ClassDefVisitorIndexed {
    return ExternalClassNameFilter(regularExpression, visitor)
}

fun filteredByExternalClassName(regularExpression: String, visitor: ClassDefVisitor): ClassDefVisitor {
    return ExternalClassNameFilter(regularExpression, visitor).toClassDefVisitor()
}

internal fun allClassData(visitor: ClassDataVisitor): ClassDefVisitor {
    return ClassDefVisitor { dexFile, classDef -> classDef.classDataAccept(dexFile, visitor) }
}

fun allStaticFields(visitor: EncodedFieldVisitor): ClassDefVisitor {
    return allClassData(allStaticFieldsOfClassData(visitor))
}

fun allInstanceFields(visitor: EncodedFieldVisitor): ClassDefVisitor {
    return allClassData(allInstanceFieldsOfClassData(visitor))
}

fun allFields(visitor: EncodedFieldVisitor): ClassDefVisitor {
    return allStaticFields(visitor).andThen(allInstanceFields(visitor))
}

fun allDirectMethods(visitor: EncodedMethodVisitor): ClassDefVisitor {
    return allClassData(allDirectMethodsOfClassData(visitor))
}

fun allVirtualMethods(visitor: EncodedMethodVisitor): ClassDefVisitor {
    return allClassData(allVirtualMethodsOfClassData(visitor))
}

fun allMethods(visitor: EncodedMethodVisitor): ClassDefVisitor {
    return allDirectMethods(visitor).andThen(allVirtualMethods(visitor))
}

fun interface ClassDefVisitorIndexed {
    fun visitClassDef(dexFile: DexFile, index: Int, classDef: ClassDef)
}

fun interface ClassDefVisitor: ClassDefVisitorIndexed {
    fun visitClassDef(dexFile: DexFile, classDef: ClassDef)

    override fun visitClassDef(dexFile: DexFile, index: Int, classDef: ClassDef) {
        visitClassDef(dexFile, classDef)
    }

    fun andThen(vararg visitors: ClassDefVisitor): ClassDefVisitor {
        return multiClassDefVisitorOf(this, *visitors)
    }
}

private fun ClassDefVisitorIndexed.toClassDefVisitor(): ClassDefVisitor {
    return ClassDefVisitor { dexFile, classDef -> this@toClassDefVisitor.visitClassDef(dexFile, 0, classDef) }
}

private class ExternalClassNameFilter(regularExpression: String, private val visitor: ClassDefVisitorIndexed) : ClassDefVisitorIndexed {

    private val matcher: StringMatcher = classNameMatcher(regularExpression)

    override fun visitClassDef(dexFile: DexFile, index: Int, classDef: ClassDef) {
        val externalClassName = classDef.getClassName(dexFile).asInternalJavaClassName().toExternalClassName()
        if (accepted(externalClassName)) {
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

    override fun visitClassDef(dexFile: DexFile, classDef: ClassDef) {
        for (visitor in visitors) {
            visitor.visitClassDef(dexFile, classDef)
        }
    }
}
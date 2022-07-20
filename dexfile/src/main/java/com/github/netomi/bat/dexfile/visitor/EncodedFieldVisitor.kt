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

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.util.StringMatcher
import com.github.netomi.bat.util.simpleNameMatcher
import java.util.function.BiConsumer

fun multiFieldVisitorOf(visitor: EncodedFieldVisitor, vararg visitors: EncodedFieldVisitor): EncodedFieldVisitor {
    return MultiFieldVisitor(visitor, *visitors)
}

fun filterFieldsByName(name: String, visitor: EncodedFieldVisitor): EncodedFieldVisitor {
    return FieldNameAndTypeFilter(name, null, visitor)
}

fun filterFieldsByNameAndType(nameExpression: String, type: String, visitor: EncodedFieldVisitor): EncodedFieldVisitor {
    return FieldNameAndTypeFilter(nameExpression, type, visitor)
}

fun interface EncodedFieldVisitor {

    fun visitAnyField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField)

    fun visitStaticField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        visitAnyField(dexFile, classDef, index, field)
    }

    fun visitInstanceField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        visitAnyField(dexFile, classDef, index, field)
    }

    fun andThen(vararg visitors: EncodedFieldVisitor): EncodedFieldVisitor {
        return multiFieldVisitorOf(this, *visitors)
    }

    fun joinedByFieldConsumer(consumer: BiConsumer<DexFile, EncodedField>): EncodedFieldVisitor {
        val joiner: EncodedFieldVisitor = object : EncodedFieldVisitor {
            private var firstVisited = false
            override fun visitAnyField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
                if (firstVisited) {
                    consumer.accept(dexFile, field)
                } else {
                    firstVisited = true
                }
            }
        }
        return multiFieldVisitorOf(joiner, this)
    }

}

private class FieldNameAndTypeFilter(nameExpression:      String?,
                                     private val type:    String?,
                                     private val visitor: EncodedFieldVisitor) : EncodedFieldVisitor {

    private val nameMatcher: StringMatcher?

    init {
        nameMatcher = if (nameExpression != null) {
            simpleNameMatcher(nameExpression)
        } else {
            null
        }
    }

    override fun visitAnyField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        if (accepted(field.getName(dexFile), field.getType(dexFile))) {
            visitor.visitAnyField(dexFile, classDef, index, field)
        }
    }

    override fun visitStaticField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        if (accepted(field.getName(dexFile), field.getType(dexFile))) {
            visitor.visitStaticField(dexFile, classDef, index, field)
        }
    }

    override fun visitInstanceField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        if (accepted(field.getName(dexFile), field.getType(dexFile))) {
            visitor.visitInstanceField(dexFile, classDef, index, field)
        }
    }

    // Private utility methods.
    private fun accepted(name: String, type: String): Boolean {
        return (this.nameMatcher == null || this.nameMatcher.matches(name)) &&
                (this.type       == null || this.type == type)
    }
}

private class MultiFieldVisitor constructor(       visitor:       EncodedFieldVisitor,
                                            vararg otherVisitors: EncodedFieldVisitor)
    : AbstractMultiVisitor<EncodedFieldVisitor>(visitor, *otherVisitors), EncodedFieldVisitor {

    override fun visitAnyField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        for (visitor in visitors) {
            visitor.visitAnyField(dexFile, classDef, index, field)
        }
    }

    override fun visitStaticField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        for (visitor in visitors) {
            visitor.visitStaticField(dexFile, classDef, index, field)
        }
    }

    override fun visitInstanceField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        for (visitor in visitors) {
            visitor.visitInstanceField(dexFile, classDef, index, field)
        }
    }
}

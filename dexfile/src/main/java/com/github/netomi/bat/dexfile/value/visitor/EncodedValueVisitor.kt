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
package com.github.netomi.bat.dexfile.value.visitor

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.dexfile.visitor.AbstractCollector
import com.github.netomi.bat.dexfile.visitor.AbstractMultiVisitor
import java.util.*
import java.util.function.BiConsumer

fun multiValueVisitorOf(visitor: EncodedValueVisitor, vararg visitors: EncodedValueVisitor): EncodedValueVisitor {
    return Multi(visitor, *visitors)
}

fun filterValuesByType(acceptedType: EncodedValueType, visitor: EncodedValueVisitor): EncodedValueVisitor {
    return filterValuesByType(EnumSet.of(acceptedType), visitor)
}

fun filterValuesByType(acceptedTypes: EnumSet<EncodedValueType>, visitor: EncodedValueVisitor): EncodedValueVisitor {
    return EncodedValueFilter(acceptedTypes, visitor)
}

fun valueCollector(): EncodedValueCollector {
    return EncodedValueCollector()
}

fun interface EncodedValueVisitor {
    fun visitAnyValue(dexFile: DexFile, value: EncodedValue)

    fun visitAnnotationValue(dexFile: DexFile, value: EncodedAnnotationValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitArrayValue(dexFile: DexFile, value: EncodedArrayValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitBooleanValue(dexFile: DexFile, value: EncodedBooleanValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitByteValue(dexFile: DexFile, value: EncodedByteValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitCharValue(dexFile: DexFile, value: EncodedCharValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitDoubleValue(dexFile: DexFile, value: EncodedDoubleValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitEnumValue(dexFile: DexFile, value: EncodedEnumValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitFieldValue(dexFile: DexFile, value: EncodedFieldValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitFloatValue(dexFile: DexFile, value: EncodedFloatValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitIntValue(dexFile: DexFile, value: EncodedIntValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitLongValue(dexFile: DexFile, value: EncodedLongValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitMethodHandleValue(dexFile: DexFile, value: EncodedMethodHandleValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitMethodTypeValue(dexFile: DexFile, value: EncodedMethodTypeValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitMethodValue(dexFile: DexFile, value: EncodedMethodValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitNullValue(dexFile: DexFile, value: EncodedNullValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitShortValue(dexFile: DexFile, value: EncodedShortValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitStringValue(dexFile: DexFile, value: EncodedStringValue) {
        visitAnyValue(dexFile, value)
    }

    fun visitTypeValue(dexFile: DexFile, value: EncodedTypeValue) {
        visitAnyValue(dexFile, value)
    }

    fun andThen(vararg visitors: EncodedValueVisitor): EncodedValueVisitor {
        return multiValueVisitorOf(this, *visitors)
    }

    fun joinedByValueConsumer(consumer: BiConsumer<DexFile, EncodedValue>): EncodedValueVisitor {
        val joiner: EncodedValueVisitor = object : EncodedValueVisitor {
            private var firstVisited = false
            override fun visitAnyValue(dexFile: DexFile, value: EncodedValue) {
                if (firstVisited) {
                    consumer.accept(dexFile, value)
                } else {
                    firstVisited = true
                }
            }
        }
        return multiValueVisitorOf(joiner, this)
    }
}

class EncodedValueCollector: AbstractCollector<EncodedValue>(), EncodedValueVisitor {
    override fun visitAnyValue(dexFile: DexFile, value: EncodedValue) {
        addItem(value)
    }
}

private class EncodedValueFilter(private val acceptedTypes: EnumSet<EncodedValueType>, private val visitor: EncodedValueVisitor) : EncodedValueVisitor {

    override fun visitAnyValue(dexFile: DexFile, value: EncodedValue) {
        if (accepted(value)) {
            visitor.visitAnyValue(dexFile, value)
        }
    }

    override fun visitAnnotationValue(dexFile: DexFile, value: EncodedAnnotationValue) {
        if (accepted(value)) {
            visitor.visitAnnotationValue(dexFile, value)
        }
    }

    override fun visitArrayValue(dexFile: DexFile, value: EncodedArrayValue) {
        if (accepted(value)) {
            visitor.visitArrayValue(dexFile, value)
        }
    }

    override fun visitBooleanValue(dexFile: DexFile, value: EncodedBooleanValue) {
        if (accepted(value)) {
            visitor.visitBooleanValue(dexFile, value)
        }
    }

    override fun visitByteValue(dexFile: DexFile, value: EncodedByteValue) {
        if (accepted(value)) {
            visitor.visitByteValue(dexFile, value)
        }
    }

    override fun visitCharValue(dexFile: DexFile, value: EncodedCharValue) {
        if (accepted(value)) {
            visitor.visitCharValue(dexFile, value)
        }
    }

    override fun visitDoubleValue(dexFile: DexFile, value: EncodedDoubleValue) {
        if (accepted(value)) {
            visitor.visitDoubleValue(dexFile, value)
        }
    }

    override fun visitEnumValue(dexFile: DexFile, value: EncodedEnumValue) {
        if (accepted(value)) {
            visitor.visitEnumValue(dexFile, value)
        }
    }

    override fun visitFieldValue(dexFile: DexFile, value: EncodedFieldValue) {
        if (accepted(value)) {
            visitor.visitFieldValue(dexFile, value)
        }
    }

    override fun visitFloatValue(dexFile: DexFile, value: EncodedFloatValue) {
        if (accepted(value)) {
            visitor.visitFloatValue(dexFile, value)
        }
    }

    override fun visitIntValue(dexFile: DexFile, value: EncodedIntValue) {
        if (accepted(value)) {
            visitor.visitIntValue(dexFile, value)
        }
    }

    override fun visitLongValue(dexFile: DexFile, value: EncodedLongValue) {
        if (accepted(value)) {
            visitor.visitLongValue(dexFile, value)
        }
    }

    override fun visitMethodHandleValue(dexFile: DexFile, value: EncodedMethodHandleValue) {
        if (accepted(value)) {
            visitor.visitMethodHandleValue(dexFile, value)
        }
    }

    override fun visitMethodTypeValue(dexFile: DexFile, value: EncodedMethodTypeValue) {
        if (accepted(value)) {
            visitor.visitMethodTypeValue(dexFile, value)
        }
    }

    override fun visitMethodValue(dexFile: DexFile, value: EncodedMethodValue) {
        if (accepted(value)) {
            visitor.visitMethodValue(dexFile, value)
        }
    }

    override fun visitNullValue(dexFile: DexFile, value: EncodedNullValue) {
        if (accepted(value)) {
            visitor.visitNullValue(dexFile, value)
        }
    }

    override fun visitShortValue(dexFile: DexFile, value: EncodedShortValue) {
        if (accepted(value)) {
            visitor.visitShortValue(dexFile, value)
        }
    }

    override fun visitStringValue(dexFile: DexFile, value: EncodedStringValue) {
        if (accepted(value)) {
            visitor.visitStringValue(dexFile, value)
        }
    }

    override fun visitTypeValue(dexFile: DexFile, value: EncodedTypeValue) {
        if (accepted(value)) {
            visitor.visitTypeValue(dexFile, value)
        }
    }

    private fun accepted(value: EncodedValue): Boolean {
        return acceptedTypes.contains(value.valueType)
    }
}

private class Multi constructor(       visitor:       EncodedValueVisitor,
                                vararg otherVisitors: EncodedValueVisitor)

    : AbstractMultiVisitor<EncodedValueVisitor>(visitor, *otherVisitors), EncodedValueVisitor {

    override fun visitAnyValue(dexFile: DexFile, value: EncodedValue) {
        for (visitor in visitors) {
            visitor.visitAnyValue(dexFile, value)
        }
    }

    override fun visitAnnotationValue(dexFile: DexFile, value: EncodedAnnotationValue) {
        for (visitor in visitors) {
            visitor.visitAnnotationValue(dexFile, value)
        }
    }

    override fun visitArrayValue(dexFile: DexFile, value: EncodedArrayValue) {
        for (visitor in visitors) {
            visitor.visitArrayValue(dexFile, value)
        }
    }

    override fun visitBooleanValue(dexFile: DexFile, value: EncodedBooleanValue) {
        for (visitor in visitors) {
            visitor.visitBooleanValue(dexFile, value)
        }
    }

    override fun visitByteValue(dexFile: DexFile, value: EncodedByteValue) {
        for (visitor in visitors) {
            visitor.visitByteValue(dexFile, value)
        }
    }

    override fun visitCharValue(dexFile: DexFile, value: EncodedCharValue) {
        for (visitor in visitors) {
            visitor.visitCharValue(dexFile, value)
        }
    }

    override fun visitDoubleValue(dexFile: DexFile, value: EncodedDoubleValue) {
        for (visitor in visitors) {
            visitor.visitDoubleValue(dexFile, value)
        }
    }

    override fun visitEnumValue(dexFile: DexFile, value: EncodedEnumValue) {
        for (visitor in visitors) {
            visitor.visitEnumValue(dexFile, value)
        }
    }

    override fun visitFieldValue(dexFile: DexFile, value: EncodedFieldValue) {
        for (visitor in visitors) {
            visitor.visitFieldValue(dexFile, value)
        }
    }

    override fun visitFloatValue(dexFile: DexFile, value: EncodedFloatValue) {
        for (visitor in visitors) {
            visitor.visitFloatValue(dexFile, value)
        }
    }

    override fun visitIntValue(dexFile: DexFile, value: EncodedIntValue) {
        for (visitor in visitors) {
            visitor.visitIntValue(dexFile, value)
        }
    }

    override fun visitLongValue(dexFile: DexFile, value: EncodedLongValue) {
        for (visitor in visitors) {
            visitor.visitLongValue(dexFile, value)
        }
    }

    override fun visitMethodHandleValue(dexFile: DexFile, value: EncodedMethodHandleValue) {
        for (visitor in visitors) {
            visitor.visitMethodHandleValue(dexFile, value)
        }
    }

    override fun visitMethodTypeValue(dexFile: DexFile, value: EncodedMethodTypeValue) {
        for (visitor in visitors) {
            visitor.visitMethodTypeValue(dexFile, value)
        }
    }

    override fun visitMethodValue(dexFile: DexFile, value: EncodedMethodValue) {
        for (visitor in visitors) {
            visitor.visitMethodValue(dexFile, value)
        }
    }

    override fun visitNullValue(dexFile: DexFile, value: EncodedNullValue) {
        for (visitor in visitors) {
            visitor.visitNullValue(dexFile, value)
        }
    }

    override fun visitShortValue(dexFile: DexFile, value: EncodedShortValue) {
        for (visitor in visitors) {
            visitor.visitShortValue(dexFile, value)
        }
    }

    override fun visitStringValue(dexFile: DexFile, value: EncodedStringValue) {
        for (visitor in visitors) {
            visitor.visitStringValue(dexFile, value)
        }
    }

    override fun visitTypeValue(dexFile: DexFile, value: EncodedTypeValue) {
        for (visitor in visitors) {
            visitor.visitTypeValue(dexFile, value)
        }
    }
}
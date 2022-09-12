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
import com.github.netomi.bat.visitor.AbstractCollector
import com.github.netomi.bat.visitor.AbstractMultiVisitor
import java.util.*
import java.util.function.BiConsumer

fun multiValueVisitorOf(visitor: EncodedValueVisitor, vararg visitors: EncodedValueVisitor): EncodedValueVisitor {
    return Multi(visitor, *visitors)
}

fun filterValuesByType(acceptedType: EncodedValueType, visitor: EncodedValueVisitor): EncodedValueVisitor {
    return filterValuesByType(EnumSet.of(acceptedType), visitor)
}

fun filterValuesByType(acceptedTypes: EnumSet<EncodedValueType>, visitor: EncodedValueVisitor): EncodedValueVisitor {
    return EncodedValueVisitor { dexFile, value ->
        if (acceptedTypes.contains(value.valueType)) {
            value.accept(dexFile, visitor)
        }
    }
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

private class Multi constructor(       visitor:       EncodedValueVisitor,
                                vararg otherVisitors: EncodedValueVisitor)

    : AbstractMultiVisitor<EncodedValueVisitor>(visitor, *otherVisitors), EncodedValueVisitor {

    override fun visitAnyValue(dexFile: DexFile, value: EncodedValue) {
        for (visitor in visitors) {
            value.accept(dexFile, visitor)
        }
    }
}
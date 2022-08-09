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

package com.github.netomi.bat.tinydvm.data

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.JAVA_LANG_STRING_TYPE
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.google.common.base.Objects

abstract class DvmValue {
    abstract val value: Any?
    abstract val type:  String

    abstract fun valueOfType(type: String): Any?

    abstract fun withType(newType: String): DvmValue

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        val o = other as DvmValue

        return value == o.value &&
               type  == o.type
    }

    override fun hashCode(): Int {
        return Objects.hashCode(value, type)
    }

    companion object {
        fun of(obj: Any, type: String): DvmValue {
            return when (obj::class.java) {
                Integer.TYPE           -> DvmPrimitiveValue.of((obj as Number).toInt())
                Character.TYPE         -> DvmPrimitiveValue.of((obj as Char))
                java.lang.Long.TYPE    -> DvmPrimitiveValue.of((obj as Number).toLong())
                java.lang.Short.TYPE   -> DvmPrimitiveValue.of((obj as Number).toShort())
                java.lang.Byte.TYPE    -> DvmPrimitiveValue.of((obj as Number).toByte())
                java.lang.Float.TYPE   -> DvmPrimitiveValue.of((obj as Number).toFloat())
                java.lang.Double.TYPE  -> DvmPrimitiveValue.of((obj as Number).toDouble())
                java.lang.Boolean.TYPE -> DvmPrimitiveValue.of(obj as Boolean)

                else -> DvmReferenceValue.of(obj, type)
            }
        }
    }
}

internal fun EncodedValue.toDVMValue(dexFile: DexFile, type: String): DvmValue {
    val converter = EncodedValueConverter(type)
    accept(dexFile, converter)
    return converter.dvmValue
}

private class EncodedValueConverter constructor(private val type: String): EncodedValueVisitor {

    lateinit var dvmValue: DvmValue

    override fun visitAnyValue(dexFile: DexFile, value: EncodedValue) {}

    override fun visitBooleanValue(dexFile: DexFile, value: EncodedBooleanValue) {
        dvmValue = DvmPrimitiveValue.of(value.value)
    }

    override fun visitByteValue(dexFile: DexFile, value: EncodedByteValue) {
        dvmValue = DvmPrimitiveValue.of(value.value)
    }

    override fun visitCharValue(dexFile: DexFile, value: EncodedCharValue) {
        dvmValue = DvmPrimitiveValue.of(value.value)
    }

    override fun visitDoubleValue(dexFile: DexFile, value: EncodedDoubleValue) {
        dvmValue = DvmPrimitiveValue.of(value.value)
    }

    override fun visitFloatValue(dexFile: DexFile, value: EncodedFloatValue) {
        dvmValue = DvmPrimitiveValue.of(value.value)
    }

    override fun visitIntValue(dexFile: DexFile, value: EncodedIntValue) {
        dvmValue = DvmPrimitiveValue.of(value.value)
    }

    override fun visitLongValue(dexFile: DexFile, value: EncodedLongValue) {
        dvmValue = DvmPrimitiveValue.of(value.value)
    }

    override fun visitShortValue(dexFile: DexFile, value: EncodedShortValue) {
        dvmValue = DvmPrimitiveValue.of(value.value)
    }

    override fun visitNullValue(dexFile: DexFile, value: EncodedNullValue) {
        dvmValue = DvmReferenceValue.of(null, type)
    }

    override fun visitStringValue(dexFile: DexFile, value: EncodedStringValue) {
        dvmValue = DvmReferenceValue.of(value.getStringValue(dexFile), JAVA_LANG_STRING_TYPE)
    }
}

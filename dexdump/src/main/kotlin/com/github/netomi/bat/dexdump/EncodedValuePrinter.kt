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

package com.github.netomi.bat.dexdump

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor

internal class EncodedValuePrinter constructor(private val printer: Mutf8Printer): EncodedValueVisitor {

    override fun visitAnyValue(dexFile: DexFile, value: EncodedValue) {
        printer.print(value.toString())
    }

    override fun visitArrayValue(dexFile: DexFile, value: EncodedArrayValue) {
        if (!value.isEmpty) {
            printer.print("{ ")
            value.valuesAccept(dexFile, joinedByValueConsumer { _, _ -> printer.print(" ") })
            printer.print(" }")
        } else {
            printer.print("{ }")
        }
    }

    override fun visitEnumValue(dexFile: DexFile, value: EncodedEnumValue) {
        printer.print(value.getFieldID(dexFile).getName(dexFile))
    }

    override fun visitMethodValue(dexFile: DexFile, value: EncodedMethodValue) {
        printer.print(value.getMethodID(dexFile).getName(dexFile))
    }

    override fun visitFieldValue(dexFile: DexFile, value: EncodedFieldValue) {
        printer.print(value.getFieldID(dexFile).getName(dexFile))
    }

    override fun visitStringValue(dexFile: DexFile, value: EncodedStringValue) {
        printer.print("\"")
        printer.printAsMutf8(value.getStringValue(dexFile), true)
        printer.print("\"")
    }

    override fun visitCharValue(dexFile: DexFile, value: EncodedCharValue) {
        printer.print(value.value.code.toString())
    }

    override fun visitByteValue(dexFile: DexFile, value: EncodedByteValue) {
        printer.print(value.value.toInt().toString())
    }

    override fun visitShortValue(dexFile: DexFile, value: EncodedShortValue) {
        printer.print(value.value.toString())
    }

    override fun visitBooleanValue(dexFile: DexFile, value: EncodedBooleanValue) {
        printer.print(java.lang.Boolean.toString(value.value))
    }

    override fun visitIntValue(dexFile: DexFile, value: EncodedIntValue) {
        printer.print(value.value.toString())
    }

    override fun visitLongValue(dexFile: DexFile, value: EncodedLongValue) {
        printer.print(value.value.toString())
    }

    override fun visitDoubleValue(dexFile: DexFile, value: EncodedDoubleValue) {
        printer.print("%g".format(value.value))
    }

    override fun visitFloatValue(dexFile: DexFile, value: EncodedFloatValue) {
        printer.print("%g".format(value.value.toDouble()))
    }

    override fun visitTypeValue(dexFile: DexFile, value: EncodedTypeValue) {
        printer.print(value.getType(dexFile))
    }

    override fun visitAnnotationValue(dexFile: DexFile, value: EncodedAnnotationValue) {
        printer.print(value.getType(dexFile))
        for (i in 0 until value.elements.size) {
            val element = value.elements[i]
            printer.print(" " + element.getName(dexFile) + "=")
            element.value.accept(dexFile, this)
        }
    }

    override fun visitNullValue(dexFile: DexFile, value: EncodedNullValue) {
        printer.print("null")
    }
}
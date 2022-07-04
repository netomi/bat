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
package com.github.netomi.bat.dexdump

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor

internal class CallSiteArgumentPrinter(private val printer: Mutf8Printer) : EncodedValueVisitor {

    override fun visitAnyValue(dexFile: DexFile, value: EncodedValue) {}

    override fun visitBooleanValue(dexFile: DexFile, value: EncodedBooleanValue) {
        printer.print("${value.value} (boolean)")
    }

    override fun visitByteValue(dexFile: DexFile, value: EncodedByteValue) {
        printer.print("${value.value} (byte)")
    }

    override fun visitCharValue(dexFile: DexFile, value: EncodedCharValue) {
        printer.print("${value.value} (char)")
    }

    override fun visitDoubleValue(dexFile: DexFile, value: EncodedDoubleValue) {
        printer.print("%g (double)".format(value.value))
    }

    override fun visitEnumValue(dexFile: DexFile, value: EncodedEnumValue) {
        printer.print("${value.fieldIndex} (Enum)")
    }

    override fun visitFieldValue(dexFile: DexFile, value: EncodedFieldValue) {
        printer.print("${value.fieldIndex} (Field)")
    }

    override fun visitFloatValue(dexFile: DexFile, value: EncodedFloatValue) {
        printer.print("%g (float)".format(value.value.toDouble()))
    }

    override fun visitIntValue(dexFile: DexFile, value: EncodedIntValue) {
        printer.print("${value.value} (int)")
    }

    override fun visitLongValue(dexFile: DexFile, value: EncodedLongValue) {
        printer.print("${value.value} (long)")
    }

    override fun visitMethodHandleValue(dexFile: DexFile, value: EncodedMethodHandleValue) {
        printer.print("${value.handleIndex} (MethodHandle)")
    }

    override fun visitMethodTypeValue(dexFile: DexFile, value: EncodedMethodTypeValue) {
        printer.print("${value.getProtoID(dexFile).getDescriptor(dexFile)} (MethodType)")
    }

    override fun visitMethodValue(dexFile: DexFile, value: EncodedMethodValue) {
        printer.print("${value.methodIndex} (Method)")
    }

    override fun visitNullValue(dexFile: DexFile, value: EncodedNullValue) {
        printer.print("null")
    }

    override fun visitShortValue(dexFile: DexFile, value: EncodedShortValue) {
        printer.print("${value.value} (short)")
    }

    override fun visitStringValue(dexFile: DexFile, value: EncodedStringValue) {
        printer.print("${value.getString(dexFile)} (String)")
    }

    override fun visitTypeValue(dexFile: DexFile, value: EncodedTypeValue) {
        printer.print("${value.getType(dexFile)} (Class)")
    }
}
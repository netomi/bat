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
package com.github.netomi.bat.smali.disassemble

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.escapeAsJavaString

internal class CallSiteArgumentPrinter(private val printer: IndentingPrinter) : EncodedValueVisitor {

    override fun visitAnyValue(dexFile: DexFile, value: EncodedValue) {
        throw RuntimeException("unexpected value $value")
    }

    override fun visitTypeValue(dexFile: DexFile, value: EncodedTypeValue) {
        printer.print(value.getType(dexFile))
    }

    override fun visitFieldValue(dexFile: DexFile, value: EncodedFieldValue) {
        val fieldID = value.getFieldID(dexFile)
        printer.print("${fieldID.getClassType(dexFile)}->${fieldID.getName(dexFile)}:${fieldID.getType(dexFile)}")
    }

    override fun visitMethodValue(dexFile: DexFile, value: EncodedMethodValue) {
        val methodID = value.getMethodID(dexFile)
        printer.print("${methodID.getClassTypeID(dexFile).getType(dexFile)}->${methodID.getName(dexFile)}${methodID.getProtoID(dexFile).getDescriptor(dexFile)}")
    }

    override fun visitMethodHandleValue(dexFile: DexFile, value: EncodedMethodHandleValue) {
        printer.print(value.getMethodHandle(dexFile).getTargetClassType(dexFile))
    }

    override fun visitMethodTypeValue(dexFile: DexFile, value: EncodedMethodTypeValue) {
        printer.print(value.getProtoID(dexFile).getDescriptor(dexFile))
    }

    override fun visitBooleanValue(dexFile: DexFile, value: EncodedBooleanValue) {
        printer.print(value.value.toString())
    }

    override fun visitByteValue(dexFile: DexFile, value: EncodedByteValue) {
        val v = value.value.toInt()
        if (v < 0) {
            printer.print("-0x%xt".format(-v))
        } else {
            printer.print("0x%xt".format(v))
        }
    }

    override fun visitShortValue(dexFile: DexFile, value: EncodedShortValue) {
        val v = value.value.toInt()
        if (v < 0) {
            printer.print("-0x%xs".format(-v))
        } else {
            printer.print("0x%xs".format(v))
        }
    }

    override fun visitCharValue(dexFile: DexFile, value: EncodedCharValue) {
        printer.print("'" + value.value.escapeAsJavaString() + "'")
    }

    override fun visitDoubleValue(dexFile: DexFile, value: EncodedDoubleValue) {
        printer.print(value.value.toString())
    }

    override fun visitEnumValue(dexFile: DexFile, value: EncodedEnumValue) {
        val fieldID = value.getFieldID(dexFile)
        printer.print(".enum " + fieldID.getClassType(dexFile) + "->" + fieldID.getName(dexFile) + ":" + fieldID.getType(dexFile))
    }

    override fun visitFloatValue(dexFile: DexFile, value: EncodedFloatValue) {
        printer.print(value.value.toString() + "f")
    }

    override fun visitIntValue(dexFile: DexFile, value: EncodedIntValue) {
        val v = value.value
        if (v < 0) {
            printer.print("-0x%x".format(-v))
        } else {
            printer.print("0x%x".format(v))
        }
    }

    override fun visitLongValue(dexFile: DexFile, value: EncodedLongValue) {
        val v = value.value
        if (v < 0) {
            printer.print("-0x%xL".format(-v))
        } else {
            printer.print("0x%xL".format(v))
        }
    }

    override fun visitNullValue(dexFile: DexFile, value: EncodedNullValue) {
        printer.print("null")
    }

    override fun visitStringValue(dexFile: DexFile, value: EncodedStringValue) {
        printer.print("\"" + value.getStringValue(dexFile).escapeAsJavaString() + "\"")
    }
}
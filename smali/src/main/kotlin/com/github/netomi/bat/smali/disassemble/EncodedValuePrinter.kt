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
import com.github.netomi.bat.dexfile.value.visitor.AnnotationElementVisitor
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.escapeAsJavaString

internal class EncodedValuePrinter @JvmOverloads constructor(
    private val printer:                  IndentingPrinter,
    private val annotationElementVisitor: AnnotationElementVisitor? = null,
    private val prefix:                   String?                   = null) : EncodedValueVisitor {

    override fun visitAnyValue(dexFile: DexFile, value: EncodedValue) {
        throw RuntimeException("unexpected value $value")
    }

    override fun visitAnnotationValue(dexFile: DexFile, value: EncodedAnnotationValue) {
        val resetIndentation = if (printer.currentPosition > 0) {
            printer.resetIndentation(printer.currentPosition)
            true
        } else {
            false
        }

        appendWithPrefix(".subannotation " + value.getType(dexFile))
        printer.println()
        printer.levelUp()
        if (annotationElementVisitor != null) {
            value.annotationElementsAccept(dexFile, annotationElementVisitor)
        } else {
            throw RuntimeException("no AnnotationElementVisitor specified while visiting value $value")
        }
        printer.levelDown()
        printer.print(".end subannotation")

        if (resetIndentation) {
            printer.levelDown()
        }
    }

    override fun visitArrayValue(dexFile: DexFile, value: EncodedArrayValue) {
        appendWithPrefix("{")

        if (!value.isEmpty) {
            printer.println()
            printer.levelUp()
            value.valuesAccept(dexFile, this.joinedByValueConsumer { _, _ -> printer.println(",") })
            printer.println()
            printer.levelDown()
        }
        printer.print("}")
    }

    override fun visitTypeValue(dexFile: DexFile, value: EncodedTypeValue) {
        appendWithPrefix(value.getType(dexFile))
    }

    override fun visitFieldValue(dexFile: DexFile, value: EncodedFieldValue) {
        val fieldID = value.getFieldID(dexFile)
        appendWithPrefix("${fieldID.getClassType(dexFile)}->${fieldID.getName(dexFile)}:${fieldID.getType(dexFile)}")
    }

    override fun visitMethodValue(dexFile: DexFile, value: EncodedMethodValue) {
        val methodID = value.getMethodID(dexFile)
        appendWithPrefix("${methodID.getClassTypeID(dexFile).getType(dexFile)}->${methodID.getName(dexFile)}${methodID.getProtoID(dexFile).getDescriptor(dexFile)}")
    }

    override fun visitBooleanValue(dexFile: DexFile, value: EncodedBooleanValue) {
        appendWithPrefix(value.value.toString())
    }

    override fun visitByteValue(dexFile: DexFile, value: EncodedByteValue) {
        val v = value.value.toInt()
        if (v < 0) {
            appendWithPrefix(String.format("-0x%xt", -v))
        } else {
            appendWithPrefix(String.format("0x%xt", v))
        }
    }

    override fun visitShortValue(dexFile: DexFile, value: EncodedShortValue) {
        val v = value.value.toInt()
        if (v < 0) {
            appendWithPrefix(String.format("-0x%xs", -v))
        } else {
            appendWithPrefix(String.format("0x%xs", v))
        }
    }

    override fun visitCharValue(dexFile: DexFile, value: EncodedCharValue) {
        appendWithPrefix("'" + value.value.escapeAsJavaString() + "'")
    }

    override fun visitDoubleValue(dexFile: DexFile, value: EncodedDoubleValue) {
        appendWithPrefix(value.value.toString())
    }

    override fun visitEnumValue(dexFile: DexFile, value: EncodedEnumValue) {
        val fieldID = value.getFieldID(dexFile)
        appendWithPrefix(".enum " + fieldID.getClassType(dexFile) + "->" + fieldID.getName(dexFile) + ":" + fieldID.getType(dexFile))
    }

    override fun visitFloatValue(dexFile: DexFile, value: EncodedFloatValue) {
        appendWithPrefix(value.value.toString() + "f")
    }

    override fun visitIntValue(dexFile: DexFile, value: EncodedIntValue) {
        val v = value.value
        if (v < 0) {
            appendWithPrefix(String.format("-0x%x", -v))
        } else {
            appendWithPrefix(String.format("0x%x", v))
        }
    }

    override fun visitLongValue(dexFile: DexFile, value: EncodedLongValue) {
        val v = value.value
        if (v < 0) {
            appendWithPrefix(String.format("-0x%xL", -v))
        } else {
            appendWithPrefix(String.format("0x%xL", v))
        }
    }

    override fun visitNullValue(dexFile: DexFile, value: EncodedNullValue) {
        appendWithPrefix("null")
    }

    override fun visitStringValue(dexFile: DexFile, value: EncodedStringValue) {
        appendWithPrefix("\"" + value.getStringValue(dexFile).escapeAsJavaString() + "\"")
    }

    // private utility methods.

    private fun appendWithPrefix(obj: Any?) {
        appendWithPrefix(java.lang.String.valueOf(obj))
    }

    private fun appendWithPrefix(text: String) {
        if (prefix != null) {
            printer.print(prefix + text)
        } else {
            printer.print(text)
        }
    }
}
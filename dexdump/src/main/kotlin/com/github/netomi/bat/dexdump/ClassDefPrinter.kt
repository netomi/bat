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

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.annotation.*
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.annotation.visitor.AnnotationSetVisitor
import com.github.netomi.bat.dexfile.annotation.visitor.AnnotationVisitor
import com.github.netomi.bat.dexfile.instruction.DexInstruction
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.*
import com.github.netomi.bat.util.asInternalJavaClassName
import com.github.netomi.bat.util.toHexString
import com.github.netomi.bat.util.toHexStringWithPrefix
import com.github.netomi.bat.util.toPrintableAsciiString

internal class ClassDefPrinter constructor(private val printer: Mutf8Printer, private val disassembleCode: Boolean) :
    DexHeaderVisitor,
    ClassDefVisitor,
    EncodedFieldVisitor,
    EncodedMethodVisitor,
    TypeVisitor,
    CodeVisitor,
    InstructionVisitor,
    TryVisitor,
    AnnotationSetVisitor,
    AnnotationVisitor {

    private val instructionPrinter: InstructionVisitor = InstructionPrinter(printer)
    private val encodedValuePrinter: EncodedValueVisitor = EncodedValuePrinter(printer)

    private var fileOffset = 0
    private var codeOffset = 0

    override fun visitHeader(dexFile: DexFile, header: DexHeader) {
        printer.println("DEX file header:")
        printer.println("magic               : '" + header.magic.toPrintableAsciiString() + "'")
        printer.println("checksum            : " + toHexString(header.checksum, 8))
        printer.println("signature           : " + formatSignatureByteArray(header.signature))
        printer.println("file_size           : " + header.fileSize)
        printer.println("header_size         : " + header.headerSize)
        printer.println("link_size           : " + header.linkSize)
        printer.println("link_off            : " + formatNumber(header.linkOffset.toLong()))
        printer.println("string_ids_size     : " + header.stringIDsSize)
        printer.println("string_ids_off      : " + formatNumber(header.stringIDsOffsets.toLong()))
        printer.println("type_ids_size       : " + header.typeIDsSize)
        printer.println("type_ids_off        : " + formatNumber(header.typeIDsOffset.toLong()))
        printer.println("proto_ids_size      : " + header.protoIDsSize)
        printer.println("proto_ids_off       : " + formatNumber(header.protoIDsOffset.toLong()))
        printer.println("field_ids_size      : " + header.fieldIDsSize)
        printer.println("field_ids_off       : " + formatNumber(header.fieldIDsOffset.toLong()))
        printer.println("method_ids_size     : " + header.methodIDsSize)
        printer.println("method_ids_off      : " + formatNumber(header.methodIDsOffset.toLong()))
        printer.println("class_defs_size     : " + header.classDefsSize)
        printer.println("class_defs_off      : " + formatNumber(header.classDefsOffset.toLong()))
        printer.println("data_size           : " + header.dataSize)
        printer.println("data_off            : " + formatNumber(header.dataOffset.toLong()))
        printer.println()
    }

    override fun visitClassDef(dexFile: DexFile, index: Int, classDef: ClassDef) {
        printer.println("Static fields     -")
        printer.levelUp()
        classDef.staticFieldsAccept(dexFile, this)
        printer.levelDown()
        printer.println("Instance fields   -")
        printer.levelUp()
        classDef.instanceFieldsAccept(dexFile, this)
        printer.levelDown()
        printer.println("Direct methods    -")
        printer.levelUp()
        classDef.directMethodsAccept(dexFile, this)
        printer.levelDown()
        printer.println("Virtual methods   -")
        printer.levelUp()
        classDef.virtualMethodsAccept(dexFile, this)
        printer.levelDown()
    }

    override fun visitAnyField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        printer.println("#%-14d : (in %s)".format(index, classDef.getType(dexFile)))
        printer.println("  name          : '" + field.getName(dexFile) + "'")
        printer.println("  type          : '" + field.getType(dexFile) + "'")
        printer.println("  access        : " + formatAccessFlags(field.accessFlags, DexAccessFlagTarget.FIELD))
        if (field.isStatic) {
            val staticValue = field.staticValue(dexFile)
            if (staticValue != null) {
                printer.print("  value         : ")
                staticValue.accept(dexFile, encodedValuePrinter)
                printer.println()
            }
        }
    }

    override fun visitAnyMethod(dexFile: DexFile, classDef: ClassDef, index: Int, method: EncodedMethod) {
        printer.println("#%-14d : (in %s)".format(index, classDef.getType(dexFile)))
        printer.println("  name          : '" + method.getName(dexFile) + "'")
        printer.println("  type          : '" + method.getDescriptor(dexFile) + "'")
        printer.println("  access        : " + formatAccessFlags(method.accessFlags, DexAccessFlagTarget.METHOD))

        if (!method.code.isEmpty) {
            method.codeAccept(dexFile, classDef, this)
        } else {
            printer.println("  code          : (none)")
        }

        printer.println()
    }

    override fun visitCode(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code) {
        printer.println("  code          -")
        printer.println("  registers     : " + code.registersSize)
        printer.println("  ins           : " + code.insSize)
        printer.println("  outs          : " + code.outsSize)
        printer.println("  insns size    : " + code.insnsSize + " 16-bit code units")
        fileOffset = method.codeOffset

        if (disassembleCode) {
            printer.resetIndentation(0)

            printer.println(
                toHexString(fileOffset, 6) + ":                                        |[" +
                toHexString(fileOffset, 6) + "] " +
                fullExternalMethodSignature(dexFile, classDef, method)
            )

            fileOffset = align(fileOffset, 4)
            fileOffset += 16
            codeOffset = 0

            code.instructionsAccept(dexFile, classDef, method, this)

            printer.levelDown()
        }

        val catchCount = if (code.tryList.isEmpty()) "(none)" else code.tryList.size.toString()
        printer.println("  catches       : $catchCount")
        code.triesAccept(dexFile, classDef, method, this)

        printer.println("  positions     : ")
        if (!code.debugInfo.isEmpty) {
            code.debugInfo.debugSequenceAccept(dexFile, SourceLinePrinter(code.debugInfo.lineStart, printer))
        }

        printer.println("  locals        : ")
        if (!code.debugInfo.isEmpty) {
            val localVariablePrinter = LocalVariablePrinter(dexFile, method, code, printer)
            code.debugInfo.debugSequenceAccept(dexFile, localVariablePrinter)
            localVariablePrinter.finish()
        }
    }

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {
        val output = buildString {
            append(toHexString(fileOffset, 6))
            append(": ")
            var codeUnitOffset = offset
            run {
                var i = 0
                while (i < instruction.length && i < 7) {
                    val codeUnit = code.insns[codeUnitOffset++]
                    // print code units in little endian format.
                    append(toHexString(codeUnit.toInt() and 0xff, 2))
                    append(toHexString(codeUnit.toInt() shr 8 and 0xff, 2))
                    append(' ')
                    i++
                }
            }
            if (instruction.length >= 7) {
                append("...")
            }
            for (i in length..46) {
                append(' ')
            }
            append('|')
            append(toHexString(codeOffset, 4))
            append(": ")
        }

        printer.print(output)
        instruction.accept(dexFile, classDef, method, code, offset, instructionPrinter)
        printer.println()
        fileOffset += instruction.length * 2
        codeOffset += instruction.length
    }

    override fun visitTry(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, index: Int, tryElement: Try) {
        val startAddr = toHexStringWithPrefix(tryElement.startAddr.toShort())
        val endAddr   = toHexStringWithPrefix((tryElement.startAddr + tryElement.insnCount).toShort())
        printer.println("    %s - %s".format(startAddr, endAddr))
        val catchHandler = tryElement.catchHandler
        for (addrPair in catchHandler.handlers) {
            printer.println("      %s -> %s".format(addrPair.getType(dexFile), toHexStringWithPrefix(addrPair.address.toShort())))
        }
        if (catchHandler.catchAllAddr != -1) {
            printer.println("      %s -> %s".format("<any>", toHexStringWithPrefix(catchHandler.catchAllAddr.toShort())))
        }
    }

    override fun visitType(dexFile: DexFile, typeList: TypeList, index: Int, typeIndex: Int, type: String) {
        printer.println("#%-14d : '%s'".format(index, type))
    }

    override fun visitAnyAnnotationSet(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet) {}

    override fun visitClassAnnotationSet(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet) {
        if (!annotationSet.isEmpty) {
            printer.println("Annotations on class")
            annotationSet.accept(dexFile, this)
        }
    }

    override fun visitFieldAnnotationSet(dexFile: DexFile, classDef: ClassDef, fieldAnnotation: FieldAnnotation, annotationSet: AnnotationSet) {
        val fieldID = fieldAnnotation.getFieldID(dexFile)
        printer.println("Annotations on field #" + fieldAnnotation.fieldIndex + " '" + fieldID.getName(dexFile) + "'")
        annotationSet.accept(dexFile, this)
    }

    override fun visitMethodAnnotationSet(dexFile: DexFile, classDef: ClassDef, methodAnnotation: MethodAnnotation, annotationSet: AnnotationSet) {
        val methodID = methodAnnotation.getMethodID(dexFile)
        printer.println("Annotations on method #" + methodAnnotation.methodIndex + " '" + methodID.getName(dexFile) + "'")
        annotationSet.accept(dexFile, this)
    }

    override fun visitParameterAnnotationSetRefList(dexFile: DexFile, classDef: ClassDef, parameterAnnotation: ParameterAnnotation, annotationSetRefList: AnnotationSetRefList) {
        val methodID = parameterAnnotation.getMethodID(dexFile)
        printer.println("Annotations on method #" + parameterAnnotation.methodIndex + " '" + methodID.getName(dexFile) + "' parameters")
        for (i in 0 until annotationSetRefList.annotationSetRefCount) {
            printer.println("#$i")
            val annotationSetRef = annotationSetRefList.getAnnotationSetRef(i)
            annotationSetRef.accept(dexFile, this)
            if (annotationSetRef.annotationSet.annotationCount == 0) {
                printer.println("   empty-annotation-set")
            }
        }
    }

    override fun visitAnnotation(dexFile: DexFile, annotation: Annotation) {
        printer.print("  VISIBILITY_${annotation.visibility} ")
        val annotationValue = annotation.annotationValue
        annotationValue.accept(dexFile, encodedValuePrinter)
        printer.println()
    }

    companion object {
        private fun align(offset: Int, alignment: Int): Int {
            return if (alignment > 1) {
                val currentAlignment = offset % alignment
                val padding = (alignment - currentAlignment) % alignment
                offset + padding
            } else {
                offset
            }
        }

        private fun formatSignatureByteArray(array: ByteArray): String {
            return buildString {
                val len = array.size
                append(toHexString(array[0]))
                append(toHexString(array[1]))
                append("...")
                append(toHexString(array[len - 2]))
                append(toHexString(array[len - 1]))
            }
        }

        private fun fullExternalMethodSignature(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod): String {
            return "%s.%s:%s".format(classDef.getClassName(dexFile).asInternalJavaClassName().toExternalClassName(),
                                     method.getName(dexFile),
                                     method.getDescriptor(dexFile)
            )
        }
    }
}

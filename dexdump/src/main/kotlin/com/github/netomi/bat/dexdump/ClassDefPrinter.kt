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
import com.github.netomi.bat.dexfile.instruction.DexInstruction
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.*
import com.github.netomi.bat.util.Primitives

internal class ClassDefPrinter constructor(private val printer: Mutf8Printer) :
    DexHeaderVisitor,
    ClassDataVisitor,
    EncodedFieldVisitor,
    EncodedMethodVisitor,
    TypeVisitor,
    CodeVisitor,
    InstructionVisitor,
    TryVisitor,
    DebugSequenceVisitor,
    AnnotationSetVisitor,
    AnnotationVisitor {

    private val instructionPrinter: InstructionVisitor = InstructionPrinter(printer)
    private val encodedValuePrinter: EncodedValueVisitor = EncodedValuePrinter(printer)

    private var fileOffset = 0
    private var codeOffset = 0

    override fun visitHeader(dexFile: DexFile, header: DexHeader) {
        printer.println("DEX file header:")
        printer.println("magic               : '" + Primitives.toAsciiString(header.magic) + "'")
        printer.println("checksum            : " + Primitives.asHexValue(header.checksum.toLong(), 8))
        printer.println("signature           : " + formatSignatureByteArray(header.signature))
        printer.println("file_size           : " + header.fileSize)
        printer.println("header_size         : " + header.headerSize)
        printer.println("link_size           : " + header.linkSize)
        printer.println("link_off            : " + DexDumpPrinter.formatNumber(header.linkOffset.toLong()))
        printer.println("string_ids_size     : " + header.stringIDsSize)
        printer.println("string_ids_off      : " + DexDumpPrinter.formatNumber(header.stringIDsOffsets.toLong()))
        printer.println("type_ids_size       : " + header.typeIDsSize)
        printer.println("type_ids_off        : " + DexDumpPrinter.formatNumber(header.typeIDsOffset.toLong()))
        printer.println("proto_ids_size      : " + header.protoIDsSize)
        printer.println("proto_ids_off       : " + DexDumpPrinter.formatNumber(header.protoIDsOffset.toLong()))
        printer.println("field_ids_size      : " + header.fieldIDsSize)
        printer.println("field_ids_off       : " + DexDumpPrinter.formatNumber(header.fieldIDsOffset.toLong()))
        printer.println("method_ids_size     : " + header.methodIDsSize)
        printer.println("method_ids_off      : " + DexDumpPrinter.formatNumber(header.methodIDsOffset.toLong()))
        printer.println("class_defs_size     : " + header.classDefsSize)
        printer.println("class_defs_off      : " + DexDumpPrinter.formatNumber(header.classDefsOffset.toLong()))
        printer.println("data_size           : " + header.dataSize)
        printer.println("data_off            : " + DexDumpPrinter.formatNumber(header.dataOffset.toLong()))
        printer.println()
    }

    override fun visitClassData(dexFile: DexFile, classDef: ClassDef, classData: ClassData) {
        printer.println("Static fields     -")
        printer.levelUp()
        classData.staticFieldsAccept(dexFile, classDef, this)
        printer.levelDown()
        printer.println("Instance fields   -")
        printer.levelUp()
        classData.instanceFieldsAccept(dexFile, classDef, this)
        printer.levelDown()
        printer.println("Direct methods    -")
        printer.levelUp()
        classData.directMethodsAccept(dexFile, classDef, this)
        printer.levelDown()
        printer.println("Virtual methods   -")
        printer.levelUp()
        classData.virtualMethodsAccept(dexFile, classDef, this)
        printer.levelDown()
    }

    override fun visitAnyField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        printer.println("#%-14d : (in %s)".format(index, classDef.getType(dexFile)))
        printer.println("  name          : '" + field.getName(dexFile) + "'")
        printer.println("  type          : '" + field.getType(dexFile) + "'")
        printer.println("  access        : " + DexDumpPrinter.formatAccessFlags(field.accessFlags, DexAccessFlags.Target.FIELD))
        val staticValues = if (!classDef.staticValues.isEmpty) classDef.staticValues.array else null
        if (field.isStatic && staticValues != null && index < staticValues.values.size) {
            printer.print("  value         : ")
            staticValues.valueAccept(dexFile, index, encodedValuePrinter)
            printer.println()
        }
    }

    override fun visitAnyMethod(dexFile: DexFile, classDef: ClassDef, index: Int, method: EncodedMethod) {
        printer.println("#%-14d : (in %s)".format(index, classDef.getType(dexFile)))
        printer.println("  name          : '" + method.getName(dexFile) + "'")
        printer.println("  type          : '" + method.getDescriptor(dexFile) + "'")
        printer.println("  access        : " + DexDumpPrinter.formatAccessFlags(method.accessFlags, DexAccessFlags.Target.METHOD))

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

        printer.resetIndentation(0)

        printer.println(
            Primitives.asHexValue(fileOffset, 6) + ":                                        |[" +
            Primitives.asHexValue(fileOffset, 6) + "] " +
            DexClasses.fullExternalMethodSignature(dexFile, classDef, method)
        )

        fileOffset = align(fileOffset, 4)
        fileOffset += 16
        codeOffset = 0

        code.instructionsAccept(dexFile, classDef, method, this)

        printer.levelDown()

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
        val sb = StringBuilder()
        sb.append(Primitives.asHexValue(fileOffset, 6))
        sb.append(": ")
        var codeUnitOffset = offset
        run {
            var i = 0
            while (i < instruction.length && i < 7) {
                val codeUnit = code.insns[codeUnitOffset++]
                // print code units in little endian format.
                sb.append(Primitives.asHexValue(codeUnit.toInt() and 0xff, 2))
                sb.append(Primitives.asHexValue(codeUnit.toInt() shr 8 and 0xff, 2))
                sb.append(' ')
                i++
            }
        }
        if (instruction.length >= 7) {
            sb.append("...")
        }
        for (i in sb.length..46) {
            sb.append(' ')
        }
        sb.append('|')
        sb.append(Primitives.asHexValue(codeOffset, 4))
        sb.append(": ")
        printer.print(sb.toString())
        instruction.accept(dexFile, classDef, method, code, offset, instructionPrinter)
        printer.println()
        fileOffset += instruction.length * 2
        codeOffset += instruction.length
    }

    override fun visitTry(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, index: Int, tryObject: Try) {
        val startAddr = Primitives.toHexString(tryObject.startAddr.toShort())
        val endAddr = Primitives.toHexString((tryObject.startAddr + tryObject.insnCount).toShort())
        printer.println("    %s - %s".format(startAddr, endAddr))
        val catchHandler = tryObject.catchHandler
        for (addrPair in catchHandler.handlers) {
            printer.println("      %s -> %s".format(addrPair.getType(dexFile), Primitives.toHexString(addrPair.address.toShort())))
        }
        if (catchHandler.catchAllAddr != -1) {
            printer.println("      %s -> %s".format("<any>", Primitives.toHexString(catchHandler.catchAllAddr.toShort())))
        }
    }

    override fun visitType(dexFile: DexFile, typeList: TypeList, index: Int, typeIndex: Int, type: String) {
        printer.println("#%-14d : '%s'".format(index, type))
    }

    override fun visitAnyAnnotationSet(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet) {}

    override fun visitClassAnnotationSet(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet) {
        if (!annotationSet.isEmpty) {
            printer.println("Annotations on class")
            annotationSet.accept(dexFile, classDef, this)
        }
    }

    override fun visitFieldAnnotationSet(dexFile: DexFile, classDef: ClassDef, fieldAnnotation: FieldAnnotation, annotationSet: AnnotationSet) {
        val fieldID = fieldAnnotation.getFieldID(dexFile)
        printer.println("Annotations on field #" + fieldAnnotation.fieldIndex + " '" + fieldID.getName(dexFile) + "'")
        annotationSet.accept(dexFile, classDef, this)
    }

    override fun visitMethodAnnotationSet(dexFile: DexFile, classDef: ClassDef, methodAnnotation: MethodAnnotation, annotationSet: AnnotationSet) {
        val methodID = methodAnnotation.getMethodID(dexFile)
        printer.println("Annotations on method #" + methodAnnotation.methodIndex + " '" + methodID.getName(dexFile) + "'")
        annotationSet.accept(dexFile, classDef, this)
    }

    override fun visitParameterAnnotationSet(dexFile: DexFile, classDef: ClassDef, parameterAnnotation: ParameterAnnotation, annotationSetRefList: AnnotationSetRefList) {
        val methodID = parameterAnnotation.getMethodID(dexFile)
        printer.println("Annotations on method #" + parameterAnnotation.methodIndex + " '" + methodID.getName(dexFile) + "' parameters")
        val annotationSetRefCount = annotationSetRefList.annotationSetRefCount
        for (i in 0 until annotationSetRefCount) {
            printer.println("#$i")
            val annotationSetRef = annotationSetRefList.getAnnotationSetRef(i)
            annotationSetRef.accept(dexFile, classDef, this)
            if (annotationSetRef.annotationSet.annotationCount == 0) {
                printer.println("   empty-annotation-set")
            }
        }
    }

    override fun visitAnnotation(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet, index: Int, annotation: Annotation) {
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
            val sb = StringBuilder()
            val len = array.size
            sb.append(Primitives.asHexValue(array[0]))
            sb.append(Primitives.asHexValue(array[1]))
            sb.append("...")
            sb.append(Primitives.asHexValue(array[len - 2]))
            sb.append(Primitives.asHexValue(array[len - 1]))
            return sb.toString()
        }
    }
}

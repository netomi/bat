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

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.*
import java.io.OutputStream

class DexDumpPrinter constructor(
                outputStream:     OutputStream = System.out,
    private val printFileSummary: Boolean      = true,
    private val printHeaders:     Boolean      = true,
    private val printAnnotations: Boolean      = true,
                disassembleCode:  Boolean      = true) : DexFileVisitor, ClassDefVisitorIndexed, MethodHandleVisitor, CallSiteIDVisitor {

    private val printer:                 Mutf8Printer        = Mutf8Printer(outputStream)
    private val classDefPrinter:         ClassDefPrinter     = ClassDefPrinter(printer, disassembleCode)
    private val callSiteArgumentPrinter: EncodedValueVisitor = CallSiteArgumentPrinter(printer)

    override fun visitDexFile(dexFile: DexFile) {
        if (printFileSummary) {
            dexFile.headerAccept(classDefPrinter)
        }

        dexFile.classDefsAcceptIndexed(this)
        dexFile.methodHandlesAccept(this)
        dexFile.callSiteIDsAccept(this)

        printer.flush()
    }

    override fun visitClassDef(dexFile: DexFile, index: Int, classDef: ClassDef) {
        if (printHeaders) {
            printer.println("Class #$index header:")
            printer.println("class_idx           : " + classDef.classIndex)
            printer.println("access_flags        : " + formatNumber(classDef.accessFlags))
            printer.println("superclass_idx      : " + classDef.superClassIndex)
            printer.println("interfaces_off      : " + formatNumber(classDef.interfacesOffset.toLong()))
            printer.println("source_file_idx     : " + classDef.sourceFileIndex)
            printer.println("annotations_off     : " + formatNumber(classDef.annotationsOffset.toLong()))
            printer.println("class_data_off      : " + formatNumber(classDef.classDataOffset.toLong()))

            printer.println("static_fields_size  : " + classDef.staticFields.size)
            printer.println("instance_fields_size: " + classDef.instanceFields.size)
            printer.println("direct_methods_size : " + classDef.directMethods.size)
            printer.println("virtual_methods_size: " + classDef.virtualMethods.size)
            printer.println()
        }

        if (printAnnotations && classDef.hasAnnotations()) {
            printer.println("Class #%d annotations:".format(index))
            classDef.annotationSetsAccept(dexFile, classDefPrinter)
            printer.println()
        }

        printer.println("Class #%-5d        -".format(index))
        printer.levelUp()
        printer.println("Class descriptor  : '" + classDef.getType(dexFile) + "'")
        printer.println("Access flags      : " + formatAccessFlags(classDef.accessFlags, DexAccessFlagTarget.CLASS))
        printer.println("Superclass        : '" + classDef.getSuperClassType(dexFile) + "'")
        printer.println("Interfaces        -")
        printer.levelUp()
        classDef.interfacesAccept(dexFile, classDefPrinter)
        printer.levelDown()

        if (classDef.hasMembers) {
            classDef.accept(dexFile, classDefPrinter)
        } else {
            printer.println("Static fields     -")
            printer.println("Instance fields   -")
            printer.println("Direct methods    -")
            printer.println("Virtual methods   -")
        }

        printer.println("source_file_idx   : " + getSourceFileIndex(dexFile, classDef))
        printer.levelDown()
        printer.println()

        printer.flush()
    }

    override fun visitMethodHandle(dexFile: DexFile, index: Int, methodHandle: MethodHandle) {
        printer.println("Method handle #%d:".format(index))
        printer.levelUp()
        printer.println("type        : " + methodHandle.methodHandleType.simpleName)
        printer.println("target      : " + methodHandle.getTargetClassType(dexFile) + " " + methodHandle.getTargetMemberName(dexFile))
        printer.println("target_type : " + methodHandle.getTargetDescriptor(dexFile))
        printer.levelDown()
    }

    override fun visitCallSiteID(dexFile: DexFile, index: Int, callSiteID: CallSiteID) {
        printer.println("Call site #%d: // offset %d".format(index, callSiteID.callSiteOffset))
        printer.levelUp()
        val callSite = callSiteID.callSite
        val arrayValue = callSite.array
        for (i in 0 until arrayValue.size) {
            printer.print("link_argument[$i] : ")
            arrayValue.valueAccept(dexFile, i, callSiteArgumentPrinter)
            printer.println()
        }
        printer.levelDown()
    }

    companion object {
        private fun getSourceFileIndex(dexFile: DexFile, classDefItem: ClassDef): String {
            val sourceFile = classDefItem.getSourceFile(dexFile)
            return classDefItem.sourceFileIndex.toString() + " (" + (sourceFile ?: "unknown") + ")"
        }
    }
}

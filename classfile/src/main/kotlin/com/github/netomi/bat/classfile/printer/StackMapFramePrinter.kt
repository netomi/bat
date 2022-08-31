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

package com.github.netomi.bat.classfile.printer

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.preverification.*
import com.github.netomi.bat.classfile.attribute.preverification.ItemType
import com.github.netomi.bat.classfile.attribute.preverification.visitor.StackMapFrameVisitor
import com.github.netomi.bat.io.IndentingPrinter

internal class StackMapFramePrinter constructor(private val printer: IndentingPrinter): StackMapFrameVisitor {

    // TODO: implement all frame types

    override fun visitAnyFrame(classFile: ClassFile, frame: StackMapFrame) {}

    override fun visitAppendFrame(classFile: ClassFile, frame: AppendFrame) {
        printer.println("frame_type = ${frame.frameType} /* append */")
        printer.levelUp()
        printer.println("offset_delta = ${frame.offsetDelta}")
        val locals = frame.map { it.toHumanReadableString() }.joinToString(separator = ", ", prefix = "[ ", postfix = " ]")
        printer.println("locals = $locals")
        printer.levelDown()
    }

    override fun visitSameFrame(classFile: ClassFile, frame: SameFrame) {
        printer.println("frame_type = ${frame.frameType} /* same */")
    }
}

internal fun VerificationType.toHumanReadableString(): String {
    return when (this.type) {
        ItemType.INTEGER -> "int"
        else             -> "unknown"
    }
}
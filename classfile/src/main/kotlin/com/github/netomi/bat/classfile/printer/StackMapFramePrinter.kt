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

    override fun visitAnyFrame(classFile: ClassFile, frame: StackMapFrame) {}

    override fun visitAppendFrame(classFile: ClassFile, frame: AppendFrame) {
        printer.println("frame_type = ${frame.frameType} /* append */")
        printer.levelUp()
        printer.println("offset_delta = ${frame.offsetDelta}")
        val locals = frame.map { it.toHumanReadableString(classFile) }.joinToString(separator = ", ", prefix = "[ ", postfix = " ]")
        printer.println("locals = $locals")
        printer.levelDown()
    }

    override fun visitSameFrame(classFile: ClassFile, frame: SameFrame) {
        printer.println("frame_type = ${frame.frameType} /* same */")
    }

    override fun visitSameExtendedFrame(classFile: ClassFile, frame: SameExtendedFrame) {
        printer.println("frame_type = ${frame.frameType} /* same_frame_extended */")
        printer.levelUp()
        printer.println("offset_delta = ${frame.offsetDelta}")
        printer.levelDown()
    }

    override fun visitChopFrame(classFile: ClassFile, frame: ChopFrame) {
        printer.println("frame_type = ${frame.frameType} /* chop */")
        printer.levelUp()
        printer.println("offset_delta = ${frame.offsetDelta}")
        printer.levelDown()
    }

    override fun visitFullFrame(classFile: ClassFile, frame: FullFrame) {
        printer.println("frame_type = ${frame.frameType} /* full_frame */")
        printer.levelUp()
        printer.println("offset_delta = ${frame.offsetDelta}")
        val locals = frame.locals.joinToString(separator = ", ", prefix = "[ ", postfix = " ]") { it.toHumanReadableString(classFile) }
        printer.println("locals = $locals")
        val stack = if (frame.stack.isEmpty()) "[]" else frame.stack.joinToString(separator = ", ", prefix = "[ ", postfix = " ]") { it.toHumanReadableString(classFile) }
        printer.println("stack = $stack")
        printer.levelDown()
    }

    override fun visitSameLocalsOneStackItemFrame(classFile: ClassFile, frame: SameLocalsOneStackItemFrame) {
        printer.println("frame_type = ${frame.frameType} /* same_locals_1_stack_item */")
        printer.levelUp()
        printer.println("stack = [ ${frame.stackItem.toHumanReadableString(classFile)} ]")
        printer.levelDown()
    }

    override fun visitSameLocalsOneStackItemExtendedFrame(classFile: ClassFile, frame: SameLocalsOneStackItemExtendedFrame) {
        printer.println("frame_type = ${frame.frameType} /* same_locals_1_stack_item_frame_extended */")
        printer.levelUp()
        printer.println("offset_delta = ${frame.offsetDelta}")
        printer.println("stack = [ ${frame.stackItem.toHumanReadableString(classFile)} ]")
        printer.levelDown()
    }
}

internal fun VerificationType.toHumanReadableString(classFile: ClassFile): String {
    return when (this.type) {
        ItemType.INTEGER            -> "int"
        ItemType.LONG               -> "long"
        ItemType.FLOAT              -> "float"
        ItemType.DOUBLE             -> "double"
        ItemType.TOP                -> "top"
        ItemType.NULL               -> "null"
        ItemType.UNINITIALIZED_THIS -> "this"
        ItemType.UNINITIALIZED      -> {
            val offset = (this as UninitializedVariable).offset
            "uninitialized $offset"
        }
        ItemType.OBJECT             -> {
            val className = (this as ObjectVariable).getClassName(classFile)
            if (className.isArrayClass) {
                "class \"${className}\""
            } else {
                "class $className"
            }
        }
    }
}
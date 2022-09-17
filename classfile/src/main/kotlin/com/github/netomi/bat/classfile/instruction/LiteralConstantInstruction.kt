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

package com.github.netomi.bat.classfile.instruction

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.instruction.JvmOpCode.*
import com.github.netomi.bat.classfile.instruction.editor.InstructionWriter
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor

class LiteralConstantInstruction private constructor(opCode: JvmOpCode): ConstantInstruction(opCode) {

    override fun read(instructions: ByteArray, offset: Int) {
        constantIndex = when (opCode) {
            LDC    -> instructions[offset + 1].toInt() and 0xff

            LDC_W,
            LDC2_W -> getIndex(instructions[offset + 1], instructions[offset + 2])

            else -> error("unexpected opCode '${opCode.mnemonic}")
        }
    }

    override fun write(writer: InstructionWriter, offset: Int) {
        writer.write(offset, opCode.value.toByte())
        when (opCode) {
            LDC    -> writer.write(offset + 1, constantIndex.toByte())

            LDC_W,
            LDC2_W -> writeIndex(writer, offset + 1, constantIndex)

            else -> error("unexpected opCode '${opCode.mnemonic}")
        }
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitLiteralConstantInstruction(classFile, method, code, offset, this)
    }

    companion object {
        internal fun create(opCode: JvmOpCode): JvmInstruction {
            return LiteralConstantInstruction(opCode)
        }
    }
}
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
import java.awt.Dimension

class ArrayClassInstruction: ClassInstruction {

    var dimension: Int = 0
        private set

    val dimensionIsImplicit: Boolean
        get() = hasImplicitDimension(opCode)

    private constructor(opCode: JvmOpCode): super(opCode)

    private constructor(opCode: JvmOpCode, constantIndex: Int, dimension: Int): super(opCode, constantIndex) {
        this.dimension = dimension
    }

    override fun read(instructions: ByteArray, offset: Int) {
        super.read(instructions, offset)

        dimension = when (opCode) {
            MULTIANEWARRAY -> instructions[offset + 3].toInt()
            ANEWARRAY      -> 1
            else           -> error("unexpected opcode '$opCode'")
        }
    }

    override fun writeData(writer: InstructionWriter, offset: Int) {
        super.writeData(writer, offset)
        if (opCode == MULTIANEWARRAY) {
            writer.write(offset + 3, dimension.toByte())
        }
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitArrayClassInstruction(classFile, method, code, offset, this)
    }

    override fun toString(classFile: ClassFile): String {
        return if (dimensionIsImplicit) {
            super.toString(classFile)
        } else {
            super.toString(classFile) + ", $dimension"
        }
    }

    companion object {
        private fun hasImplicitDimension(opCode: JvmOpCode): Boolean {
            return opCode == ANEWARRAY
        }

        internal fun create(opCode: JvmOpCode): ArrayClassInstruction {
            return ArrayClassInstruction(opCode)
        }

        fun of(opCode: JvmOpCode, constantIndex: Int): ArrayClassInstruction {
            return ArrayClassInstruction(opCode, constantIndex, 1)
        }

        fun of(opCode: JvmOpCode, constantIndex: Int, dimension: Int): ArrayClassInstruction {
            require(!hasImplicitDimension(opCode)) { "instruction '$opCode' has an implicit dimension" }
            return ArrayClassInstruction(opCode, constantIndex, dimension)
        }
    }
}
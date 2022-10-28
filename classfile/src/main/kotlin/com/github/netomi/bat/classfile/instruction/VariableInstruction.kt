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
import com.github.netomi.bat.classfile.instruction.editor.InstructionWriter
import com.github.netomi.bat.classfile.instruction.editor.OffsetMap
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor

open class VariableInstruction: JvmInstruction {

    var variable: Int
        private set

    val variableIsImplicit: Boolean
        get() = hasImplicitVariable(opCode)

    var wide: Boolean
        private set

    protected constructor(opCode: JvmOpCode,
                          wide:   Boolean = false): super(opCode) {
        this.variable =
            if (hasImplicitVariable(opCode)) {
                val (variableString) = VARIABLE_REGEX.find(mnemonic)!!.destructured
                variableString.toInt()
            } else {
                -1
            }

        this.wide = wide
    }

    protected constructor(opCode:   JvmOpCode,
                          variable: Int): super(opCode) {
        this.variable = variable
        this.wide     = variable > 0xff
    }

    override fun getLength(offset: Int): Int {
        return if (wide) {
            super.getLength(offset) + 2
        } else {
            super.getLength(offset)
        }
    }

    override fun read(instructions: ByteArray, offset: Int) {
        if (!variableIsImplicit) {
            variable = if (wide) {
                getIndex(instructions[offset + 2], instructions[offset + 3])
            } else {
                instructions[offset + 1].toInt() and 0xff
            }
        }
    }

    override fun writeData(writer: InstructionWriter, offset: Int) {
        if (!variableIsImplicit) {
            var currOffset = offset
            if (wide) {
                writer.write(currOffset++, JvmOpCode.WIDE.value.toByte())
            }
            writer.write(currOffset++, opCode.value.toByte())
            if (wide) {
                writeIndex(writer, currOffset, variable)
            } else {
                writer.write(currOffset, variable.toByte())
            }
        } else {
            writer.write(offset, opCode.value.toByte())
        }
    }

    override fun updateOffsets(offset: Int, offsetMap: OffsetMap) {}

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitVariableInstruction(classFile, method, code, offset, this)
    }

    override fun toString(): String {
        return if (variableIsImplicit) {
            mnemonic
        } else {
            "$mnemonic $variable"
        }
    }

    override fun toString(classFile: ClassFile): String {
        return toString()
    }

    companion object {
        private val VARIABLE_REGEX = "\\w+_(\\d)".toRegex()

        private fun hasImplicitVariable(opCode: JvmOpCode): Boolean {
            return opCode.length == 1
        }

        internal fun create(opCode: JvmOpCode, wide: Boolean = false): VariableInstruction {
            return VariableInstruction(opCode, wide)
        }

        fun of(opCode: JvmOpCode): VariableInstruction {
            require(hasImplicitVariable(opCode))
            return create(opCode, false)
        }

        fun of(opCode: JvmOpCode, variable: Int): VariableInstruction {
            require(!hasImplicitVariable(opCode))
            return VariableInstruction(opCode, variable)
        }
    }
}
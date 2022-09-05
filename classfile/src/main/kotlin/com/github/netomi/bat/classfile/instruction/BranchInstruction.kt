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
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor

class BranchInstruction private constructor(opCode: JvmOpCode): JvmInstruction(opCode) {

    var branchOffset: Int = 0
        private set

    override fun read(instructions: ByteArray, offset: Int) {
        super.read(instructions, offset)

        branchOffset = when (opCode) {
            JvmOpCode.GOTO_W -> {
                val offsetByte1 = instructions[offset + 1]
                val offsetByte2 = instructions[offset + 2]
                val offsetByte3 = instructions[offset + 3]
                val offsetByte4 = instructions[offset + 4]

                getOffset(offsetByte1, offsetByte2, offsetByte3, offsetByte4)
            }

            else -> {
                val offsetByte1 = instructions[offset + 1]
                val offsetByte2 = instructions[offset + 2]

                getOffset(offsetByte1, offsetByte2)
            }
        }
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitBranchInstruction(classFile, method, code, offset, this)
    }

    companion object {
        internal fun create(opCode: JvmOpCode): JvmInstruction {
            return BranchInstruction(opCode)
        }
    }
}
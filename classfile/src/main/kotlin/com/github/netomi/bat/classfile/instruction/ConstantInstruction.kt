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
import com.github.netomi.bat.classfile.constant.Constant
import com.github.netomi.bat.classfile.constant.FieldrefConstant
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.instruction.JvmOpCode.*
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor

class ConstantInstruction private constructor(opCode: JvmOpCode): JvmInstruction(opCode) {

    var constantIndex: Int = 0
        private set

    fun getConstant(classFile: ClassFile): Constant {
        return classFile.getConstant(constantIndex)
    }

    override fun read(instructions: ByteArray, offset: Int) {
        super.read(instructions, offset)

        constantIndex = when (opCode) {
            LDC    -> instructions[offset + 1].toInt() and 0xff
            LDC_W  -> getIndex(instructions[offset + 1], instructions[offset + 2])
            LDC2_W -> getIndex(instructions[offset + 1], instructions[offset + 2])

            else -> error("unexpected opCode '${opCode.mnemonic}")
        }
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitConstantInstruction(classFile, method, code, offset, this)
    }

    fun constantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        getConstant(classFile).accept(classFile, visitor)
    }

    companion object {
        internal fun create(opCode: JvmOpCode): JvmInstruction {
            return ConstantInstruction(opCode)
        }
    }
}
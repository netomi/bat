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
import com.github.netomi.bat.classfile.constant.Constant
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.instruction.editor.InstructionWriter

abstract class ConstantInstruction: JvmInstruction {

    var constantIndex: Int = 0
        internal set

    protected constructor(opCode: JvmOpCode): super(opCode)

    protected constructor(opCode: JvmOpCode, constantIndex: Int): super(opCode) {
        require(constantIndex > 0) { "constantIndex must be positive for instruction ${opCode.mnemonic}" }
        this.constantIndex = constantIndex
    }

    open fun getConstant(classFile: ClassFile): Constant {
        return classFile.getConstant(constantIndex)
    }

    override fun read(instructions: ByteArray, offset: Int) {
        constantIndex = getIndex(instructions[offset + 1], instructions[offset + 2])
    }

    override fun write(writer: InstructionWriter, offset: Int) {
        writer.write(offset, opCode.value.toByte())
        writeIndex(writer, offset + 1, constantIndex)
    }

    fun constantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(constantIndex, visitor)
    }

    override fun toString(): String {
        return "$mnemonic #$constantIndex"
    }

    override fun toString(classFile: ClassFile): String {
        return "%s %s".format(mnemonic, classFile.getConstant(constantIndex))
    }
}
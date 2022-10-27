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
import com.github.netomi.bat.classfile.constant.InterfaceMethodrefConstant
import com.github.netomi.bat.classfile.instruction.editor.InstructionWriter
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor

class InterfaceMethodInstruction: InvocationInstruction {

    var argumentCount: Int
        private set

    private constructor(opCode: JvmOpCode): super(opCode) {
        this.argumentCount = 0
    }

    private constructor(opCode: JvmOpCode, constantIndex: Int, argumentCount: Int): super(opCode, constantIndex) {
        this.argumentCount = argumentCount
    }

    fun getMethodName(classFile: ClassFile): String {
        return getConstant(classFile).getMemberName(classFile)
    }

    fun getDescriptor(classFile: ClassFile): String {
        return getConstant(classFile).getDescriptor(classFile)
    }

    override fun getConstant(classFile: ClassFile): InterfaceMethodrefConstant {
        return classFile.getInterfaceMethodref(constantIndex)
    }

    override fun read(instructions: ByteArray, offset: Int) {
        super.read(instructions, offset)
        argumentCount = instructions[offset + 3].toInt()
    }

    override fun write(writer: InstructionWriter, offset: Int) {
        super.write(writer, offset)
        writer.write(offset + 3, argumentCount.toByte())
        writer.write(offset + 4, 0x0)
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitInterfaceMethodInstruction(classFile, method, code, offset, this)
    }

    companion object {
        internal fun create(opCode: JvmOpCode): InterfaceMethodInstruction {
            return InterfaceMethodInstruction(opCode)
        }

        fun of(opCode: JvmOpCode, constantIndex: Int, argumentCount: Int): InterfaceMethodInstruction {
            return InterfaceMethodInstruction(opCode, constantIndex, argumentCount)
        }
    }
}
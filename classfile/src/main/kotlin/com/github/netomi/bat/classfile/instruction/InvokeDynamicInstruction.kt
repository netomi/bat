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
import com.github.netomi.bat.classfile.constant.InvokeDynamicConstant
import com.github.netomi.bat.classfile.instruction.editor.InstructionWriter
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor

class InvokeDynamicInstruction : InvocationInstruction {

    private constructor(opCode: JvmOpCode): super(opCode)

    private constructor(opCode: JvmOpCode, constantIndex: Int): super(opCode, constantIndex)

    fun getMethodName(classFile: ClassFile): String {
        return getConstant(classFile).getMemberName(classFile)
    }

    fun getDescriptor(classFile: ClassFile): String {
        return getConstant(classFile).getDescriptor(classFile)
    }

    override fun getConstant(classFile: ClassFile): InvokeDynamicConstant {
        return classFile.getInvokeDynamic(constantIndex)
    }

    override fun writeData(writer: InstructionWriter, offset: Int) {
        super.writeData(writer, offset)
        writer.write(offset + 3, 0x0)
        writer.write(offset + 4, 0x0)
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitInvokeDynamicInstruction(classFile, method, code, offset, this)
    }

    companion object {
        internal fun create(opCode: JvmOpCode): InvokeDynamicInstruction {
            return InvokeDynamicInstruction(opCode)
        }

        fun of(opCode: JvmOpCode, constantIndex: Int): InvokeDynamicInstruction {
            return InvokeDynamicInstruction(opCode, constantIndex)
        }
    }
}
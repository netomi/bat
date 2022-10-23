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
import com.github.netomi.bat.classfile.constant.ClassConstant
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.util.JvmClassName

open class ClassInstruction: ConstantInstruction {

    protected constructor(opCode: JvmOpCode): super(opCode)

    protected constructor(opCode: JvmOpCode, constantIndex: Int): super(opCode, constantIndex)

    override fun getConstant(classFile: ClassFile): ClassConstant {
        return classFile.getClass(constantIndex)
    }

    fun getClassName(classFile: ClassFile): JvmClassName {
        return classFile.getClassName(constantIndex)
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitClassInstruction(classFile, method, code, offset, this)
    }

    companion object {
        internal fun create(opCode: JvmOpCode): JvmInstruction {
            return ClassInstruction(opCode)
        }

        fun of(opCode: JvmOpCode, constantIndex: Int): ClassInstruction {
            return ClassInstruction(opCode, constantIndex)
        }
    }
}
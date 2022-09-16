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

abstract class ConstantInstruction protected constructor(opCode: JvmOpCode): JvmInstruction(opCode) {

    var constantIndex: Int = 0
        protected set

    open fun getConstant(classFile: ClassFile): Constant {
        return classFile.getConstant(constantIndex)
    }

    override fun read(instructions: ByteArray, offset: Int) {
        constantIndex = getIndex(instructions[offset + 1], instructions[offset + 2])
    }

    fun constantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(constantIndex, visitor)
    }
}
/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.classfile.constant

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.constant.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A constant representing a CONSTANT_Float_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.4">CONSTANT_Float_info Structure</a>
 */
data class FloatConstant private constructor(var value: Float = 0.0f) : Constant() {

    override val type: Type
        get() = Type.FLOAT

    @Throws(IOException::class)
    override fun readConstantInfo(input: DataInput) {
        value = Float.fromBits(input.readInt())
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: DataOutput) {
        output.writeInt(value.toBits())
    }

    override fun accept(classFile: ClassFile, visitor: ConstantVisitor) {
        visitor.visitFloatConstant(classFile, this)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantPoolVisitor) {
        visitor.visitFloatConstant(classFile, index, this)
    }

    companion object {
        internal fun empty(): FloatConstant {
            return FloatConstant()
        }

        fun of(value: Float): FloatConstant {
            return FloatConstant(value)
        }
    }
}
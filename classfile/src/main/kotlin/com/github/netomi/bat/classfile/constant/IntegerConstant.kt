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
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import java.io.IOException

/**
 * A constant representing a CONSTANT_Integer_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.4">CONSTANT_Integer_info Structure</a>
 */
data class IntegerConstant private constructor(private var _value: Int = 0) : Constant() {

    override val type: ConstantType
        get() = ConstantType.INTEGER

    val value: Int
        get() = _value

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        _value = input.readInt()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeInt(value)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitIntegerConstant(classFile, index, this)
    }

    companion object {
        internal fun empty(): IntegerConstant {
            return IntegerConstant()
        }

        fun of(value: Int): IntegerConstant {
            return IntegerConstant(value)
        }
    }
}
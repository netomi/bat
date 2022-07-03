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
import com.github.netomi.bat.classfile.ConstantPool
import com.github.netomi.bat.classfile.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.visitor.ConstantVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A constant representing a CONSTANT_Integer_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.4">CONSTANT_Integer_info Structure</a>
 *
 * @author Thomas Neidhart
 */
data class IntegerConstant internal constructor(override val owner: ConstantPool,
                                                         var value: Int = 0) : Constant() {

    override val type: Type
        get() = Type.INTEGER

    @Throws(IOException::class)
    override fun readConstantInfo(input: DataInput) {
        value = input.readInt()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: DataOutput) {
        output.writeInt(value)
    }

    override fun accept(classFile: ClassFile,
                        visitor:   ConstantVisitor) {
        visitor.visitIntegerConstant(classFile, this)
    }

    override fun accept(classFile: ClassFile,
                        index:     Int,
                        visitor:   ConstantPoolVisitor) {
        visitor.visitIntegerConstant(classFile, index, this)
    }

    companion object {
        @JvmStatic
        fun create(owner: ConstantPool): IntegerConstant {
            return IntegerConstant(owner)
        }

        @JvmStatic
        fun create(owner: ConstantPool, value: Int): IntegerConstant {
            return IntegerConstant(owner, value)
        }
    }
}
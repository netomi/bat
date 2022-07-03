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

/**
 * A constant representing a CONSTANT_String_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.3">CONSTANT_String_info Structure</a>
 *
 * @author Thomas Neidhart
 */
data class StringConstant internal constructor(override val owner:       ConstantPool,
                                                        var stringIndex: Int = -1): Constant() {

    override val type: Type
        get() = Type.STRING

    val value: String
        get() = owner.getString(stringIndex)

    override fun readConstantInfo(input: DataInput) {
        stringIndex = input.readUnsignedShort()
    }

    override fun writeConstantInfo(output: DataOutput) {
        output.writeShort(stringIndex)
    }

    override fun accept(classFile: ClassFile,
                        visitor:   ConstantVisitor) {
        visitor.visitStringConstant(classFile, this);
    }

    override fun accept(classFile: ClassFile,
                        index:     Int,
                        visitor:   ConstantPoolVisitor) {
        visitor.visitStringConstant(classFile, index, this);
    }

    companion object {
        @JvmStatic
        fun create(owner: ConstantPool): StringConstant {
            return StringConstant(owner)
        }

        @JvmStatic
        fun create(owner: ConstantPool, stringIndex: Int): StringConstant {
            return StringConstant(owner, stringIndex)
        }
    }
}
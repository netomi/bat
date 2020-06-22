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
package org.netomi.bat.classfile.constant

import org.netomi.bat.classfile.ClassFile
import org.netomi.bat.classfile.ConstantPool
import org.netomi.bat.classfile.visitor.ConstantPoolVisitor
import org.netomi.bat.classfile.visitor.ConstantVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A constant representing a CONSTANT_InvokeDynamic_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.4.7">CONSTANT_Utf8_info Structure</a>
 *
 * @author Thomas Neidhart
 */
data class InvokeDynamicConstant internal constructor(var bootstrapMethodAttrIndex: Int = -1,
                                                      var nameAndTypeIndex:         Int = -1) : Constant() {

    override val type: Type
        get() = Type.INVOKE_DYNAMIC

    @Throws(IOException::class)
    override fun readConstantInfo(input: DataInput) {
        bootstrapMethodAttrIndex = input.readUnsignedShort()
        nameAndTypeIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: DataOutput) {
        output.writeShort(bootstrapMethodAttrIndex)
        output.writeShort(nameAndTypeIndex)
    }

    override fun accept(classFile: ClassFile,
                        visitor:   ConstantVisitor) {
        visitor.visitInvokeDynamicConstant(classFile, this)
    }

    override fun accept(classFile:    ClassFile,
                        constantPool: ConstantPool,
                        index:        Int,
                        visitor:      ConstantPoolVisitor) {
        visitor.visitInvokeDynamicConstant(classFile, constantPool, index, this)
    }

    companion object {
        @JvmStatic
        fun create(): InvokeDynamicConstant {
            return InvokeDynamicConstant()
        }

        @JvmStatic
        fun create(bootstrapMethodAttrIndex: Int, nameAndTypeIndex: Int): InvokeDynamicConstant {
            return InvokeDynamicConstant(bootstrapMethodAttrIndex, nameAndTypeIndex)
        }
    }
}
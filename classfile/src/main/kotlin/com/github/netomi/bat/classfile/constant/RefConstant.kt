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

import com.github.netomi.bat.classfile.ConstantPool
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * An abstract base class for constants representing a member reference, i.e Fieldref, Methodref or
 * InterfaceMethodref.
 *
 * @author Thomas Neidhart
 */
abstract class RefConstant(open var classIndex: Int = -1, open var nameAndTypeIndex: Int = -1): Constant() {

    fun getClassName(constantPool: ConstantPool): String {
        return constantPool.getClassName(classIndex)
    }

    fun getMemberName(constantPool: ConstantPool): String {
        return constantPool.getNameAndType(nameAndTypeIndex).getMemberName(constantPool)
    }

    fun getDescriptor(constantPool: ConstantPool): String {
        return constantPool.getNameAndType(nameAndTypeIndex).getDescriptor(constantPool)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: DataInput) {
        classIndex = input.readUnsignedShort()
        nameAndTypeIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: DataOutput) {
        output.writeShort(classIndex)
        output.writeShort(nameAndTypeIndex)
    }
}
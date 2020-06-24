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
package com.github.netomi.bat.classfile

import com.github.netomi.bat.classfile.constant.ClassConstant
import com.github.netomi.bat.classfile.constant.Constant
import com.github.netomi.bat.classfile.constant.NameAndTypeConstant
import com.github.netomi.bat.classfile.constant.Utf8Constant
import com.github.netomi.bat.classfile.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.visitor.ConstantVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class ConstantPool {
    private val constants = mutableListOf<Constant?>()

    fun addConstant(constant: Constant) {
        constants.add(constant)
    }

    fun getString(constantIndex: Int): String {
        return (constants[constantIndex] as Utf8Constant).value
    }

    fun getClassName(classIndex: Int): String {
        return (constants[classIndex] as ClassConstant).getName(this)
    }

    fun getName(nameAndTypeIndex: Int): String {
        return (constants[nameAndTypeIndex] as NameAndTypeConstant).getMemberName(this)
    }

    fun getType(nameAndTypeIndex: Int): String {
        return (constants[nameAndTypeIndex] as NameAndTypeConstant).getDescriptor(this)
    }

    @Throws(IOException::class)
    fun read(input: DataInput) {
        check(constants.isEmpty()) { "Trying to populate a non-empty ConstantPool." }
        val entries = input.readUnsignedShort()
        constants.add(null)
        var i = 1
        while (i < entries) {
            val constant = Constant.read(input)
            constants.add(constant)
            val constantPoolSize = constant.type.constantPoolSize
            if (constantPoolSize > 1) {
                constants.add(null)
                i++
            }
            i++
        }
    }

    @Throws(IOException::class)
    fun write(output: DataOutput) {
        val entries = constants.size
        output.writeShort(entries)
        val it: ListIterator<Constant?> = constants.listIterator(1)
        while (it.hasNext()) {
            val constant = it.next()
            constant?.let {
                output.writeByte(it.type.tag)
                it.writeConstantInfo(output)
            }
        }
    }

    fun accept(classFile: ClassFile, visitor: ConstantPoolVisitor) {
        visitor.visitConstantPoolStart(classFile, this)
        val it: ListIterator<Constant?> = constants.listIterator()
        while (it.hasNext()) {
            val index = it.nextIndex()
            val constant = it.next()
            constant?.accept(classFile, this, index, visitor)
        }
        visitor.visitConstantPoolEnd(classFile, this)
    }

    fun constantAccept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        constants[index]!!.accept(classFile, visitor)
    }
}
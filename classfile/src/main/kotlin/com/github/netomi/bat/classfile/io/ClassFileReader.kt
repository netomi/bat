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

package com.github.netomi.bat.classfile.io

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.MAGIC
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.InputStream

class ClassFileReader(private val `is`:           InputStream,
                      private val skipAttributes: Boolean = false): ClassFileVisitor {

    private lateinit var input: ClassDataInput

    override fun visitClassFile(classFile: ClassFile) {
        input = ClassDataInput.of(`is`, classFile, skipAttributes)
        read(classFile)
    }

    private fun read(classFile: ClassFile) {
        val magic = input.readInt()
        require(magic == MAGIC) { "invalid magic bytes when trying to read a class file." }

        classFile.apply {
            minorVersion    = input.readUnsignedShort()
            majorVersion    = input.readUnsignedShort()
            constantPool.read(input)
            accessFlags     = input.readUnsignedShort()
            thisClassIndex  = input.readUnsignedShort()
            superClassIndex = input.readUnsignedShort()

            val interfacesCount = input.readUnsignedShort()
            _interfaces = mutableListOfCapacity(interfacesCount)
            for (i in 0 until interfacesCount) {
                val idx = input.readUnsignedShort()
                _interfaces.add(idx)
            }

            val fieldCount = input.readUnsignedShort()
            _fields = mutableListOfCapacity(fieldCount)
            for (i in 0 until fieldCount) {
                addField(Field.readField(input))
            }

            val methodCount = input.readUnsignedShort()
            _methods = mutableListOfCapacity(methodCount)
            for (i in 0 until methodCount) {
                _methods.add(Method.readMethod(input))
            }

            _attributes = input.readAttributes()
        }
    }
}
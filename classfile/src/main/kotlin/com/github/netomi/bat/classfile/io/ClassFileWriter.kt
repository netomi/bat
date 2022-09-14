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
import com.github.netomi.bat.classfile.MAGIC
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import java.io.Closeable
import java.io.OutputStream

class ClassFileWriter constructor(outputStream: OutputStream): ClassFileVisitor, Closeable {

    private val output: ClassDataOutput = ClassDataOutput.of(outputStream)

    override fun visitClassFile(classFile: ClassFile) {
        write(classFile)
    }

    private fun write(classFile: ClassFile) {
        output.writeInt(MAGIC)

        classFile.apply {
            output.writeShort(minorVersion)
            output.writeShort(majorVersion)
            constantPool.write(output)
            output.writeShort(accessFlags)
            output.writeShort(thisClassIndex)
            output.writeShort(superClassIndex)

            output.writeShort(_interfaces.size)
            for (index in _interfaces) {
                output.writeShort(index)
            }

            output.writeShort(_fields.size)
            for (field in _fields) {
                field.write(output)
            }

            output.writeShort(_methods.size)
            for (method in _methods) {
                method.write(output)
            }

            _attributes.write(output)
        }
    }

    override fun close() {
        output.close()
    }
}
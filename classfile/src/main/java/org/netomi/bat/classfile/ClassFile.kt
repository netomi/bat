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
package org.netomi.bat.classfile

import org.netomi.bat.classfile.visitor.ClassFileVisitor
import org.netomi.bat.classfile.visitor.ConstantPoolVisitor
import java.io.DataInput
import java.io.IOException

class ClassFile internal constructor() {
    var minorVersion = 0
        private set
    var majorVersion = 0
        private set
    var accessFlags: AccessFlags? = null
        private set
    var thisClassIndex = 0
        private set
    var superClassIndex = 0
        private set
    val constantPool: ConstantPool = ConstantPool()
    private  val interfaces = mutableListOf<Int>()

    val className: String
        get() = constantPool.getClassName(thisClassIndex)

    val superClassName: String
        get() = constantPool.getClassName(superClassIndex)

    fun getInterfaces(): Collection<String> {
        return if (interfaces.isEmpty()) {
            emptyList()
        } else {
            val interfaceNames: MutableCollection<String> = ArrayList(interfaces.size)
            for (index in interfaces) {
                interfaceNames.add(constantPool.getClassName(index))
            }
            interfaceNames
        }
    }

    @Throws(IOException::class)
    private fun read(input: DataInput) {
        val magic = input.readInt()
        require(magic == MAGIC) { "invalid magic bytes when trying to read a class file." }

        minorVersion = input.readUnsignedShort()
        majorVersion = input.readUnsignedShort()
        constantPool.read(input)
        accessFlags = AccessFlags(input.readUnsignedShort())
        thisClassIndex = input.readUnsignedShort()
        superClassIndex = input.readUnsignedShort()
        val interfacesCount = input.readUnsignedShort()
        for (i in 0 until interfacesCount) {
            val idx = input.readUnsignedShort()
            interfaces.add(idx)
        }
    }

    fun accept(visitor: ClassFileVisitor) {
        visitor.visitClassFile(this)
    }

    fun constantPoolAccept(visitor: ConstantPoolVisitor) {
        constantPool.accept(this, visitor)
    }

    companion object {
        @Throws(IOException::class)
        fun readClassFile(input: DataInput): ClassFile {
            val classFile = ClassFile()
            classFile.read(input)
            return classFile
        }

        @JvmStatic
        fun externalClassName(internalClassName: String): String {
            return internalClassName.replace("/".toRegex(), ".")
        }
    }
}


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
package com.github.netomi.bat.classfile.attribute

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.visitor.AttributeVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class SignatureAttribute internal constructor(override var attributeNameIndex: Int,
                                                            var signatureIndex:     Int = -1) : Attribute(attributeNameIndex) {

    override val type: Type
        get() = Type.SIGNATURE

    fun signature(classFile: ClassFile): String {
        return classFile.constantPool.getString(signatureIndex)
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: DataInput) {
        val length = input.readInt()
        assert(length == 2)
        signatureIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(2)
        output.writeShort(signatureIndex)
    }

    override fun accept(classFile: ClassFile, visitor: AttributeVisitor) {
        visitor.visitSignatureAttribute(classFile, this)
    }

    companion object {
        @JvmStatic
        fun create(attributeNameIndex: Int): SignatureAttribute {
            return SignatureAttribute(attributeNameIndex)
        }
    }
}
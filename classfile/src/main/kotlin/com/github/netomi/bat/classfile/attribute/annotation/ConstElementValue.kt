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

package com.github.netomi.bat.classfile.attribute.annotation

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import java.io.IOException

data class ConstElementValue private constructor(override val type: ElementValueType,
                                                  private var _constValueIndex: Int = -1) : ElementValue() {

    override val contentSize: Int
        get() = 3

    val constValueIndex: Int
        get() = _constValueIndex

    fun getBoolean(classFile: ClassFile): Boolean {
        check(type == ElementValueType.BOOLEAN)
        return classFile.getInteger(constValueIndex) == 1
    }

    @Throws(IOException::class)
    override fun readElementValue(input: ClassDataInput) {
        _constValueIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: ClassDataOutput) {
        output.writeShort(constValueIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ElementValueVisitor) {
        when(type) {
            ElementValueType.BYTE -> visitor.visitByteElementValue(classFile, this)
            ElementValueType.CHAR -> visitor.visitCharElementValue(classFile, this)
            ElementValueType.DOUBLE -> visitor.visitDoubleElementValue(classFile, this)
            ElementValueType.FLOAT -> visitor.visitFloatElementValue(classFile, this)
            ElementValueType.INT -> visitor.visitIntElementValue(classFile, this)
            ElementValueType.LONG -> visitor.visitLongElementValue(classFile, this)
            ElementValueType.SHORT -> visitor.visitShortElementValue(classFile, this)
            ElementValueType.BOOLEAN -> visitor.visitBooleanElementValue(classFile, this)
            ElementValueType.STRING -> visitor.visitStringElementValue(classFile, this)
            else -> error("ConstElementValue has unexpected type $type")
        }
    }

    fun constantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(constValueIndex, visitor)
    }

    companion object {
        internal fun create(type: ElementValueType): ConstElementValue {
            return ConstElementValue(type)
        }
    }
}
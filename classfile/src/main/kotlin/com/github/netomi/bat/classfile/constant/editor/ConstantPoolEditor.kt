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

package com.github.netomi.bat.classfile.constant.editor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.constant.*
import com.github.netomi.bat.classfile.constant.ConstantPool

class ConstantPoolEditor private constructor(private val constantPool: ConstantPool) {

    fun addOrGetUtf8ConstantIndex(string: String): Int {
        val index = constantPool.getUtf8ConstantIndex(string)
        return if (index == -1) {
            constantPool.addConstant(Utf8Constant.of(string))
        } else {
            index
        }
    }

    fun addOrGetIntegerConstantIndex(value: Int): Int {
        val index = constantPool.getIntegerConstantIndex(value)
        return if (index == -1) {
            constantPool.addConstant(IntegerConstant.of(value))
        } else {
            index
        }
    }

    fun addOrGetLongConstantIndex(value: Long): Int {
        val index = constantPool.getLongConstantIndex(value)
        return if (index == -1) {
            constantPool.addConstant(LongConstant.of(value))
        } else {
            index
        }
    }

    fun addOrGetClassConstantIndex(className: String): Int {
        val nameIndex = addOrGetUtf8ConstantIndex(className)
        val index = constantPool.getClassConstantIndex(nameIndex)
        return if (index == -1) {
            constantPool.addConstant(ClassConstant.of(nameIndex))
        } else {
            index
        }
    }

    companion object {
        fun of(classFile: ClassFile): ConstantPoolEditor {
            return ConstantPoolEditor(classFile.constantPool)
        }
    }
}
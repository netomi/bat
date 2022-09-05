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
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor

/**
 * A constant representing a CONSTANT_Fieldref_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.2">CONSTANT_Fieldref_info Structure</a>
 */
data class FieldrefConstant private constructor(override var _classIndex:       Int = -1,
                                                override var _nameAndTypeIndex: Int = -1) : RefConstant(_classIndex, _nameAndTypeIndex) {

    override val type: ConstantType
        get() = ConstantType.FIELD_REF

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitFieldRefConstant(classFile, index, this)
    }

    companion object {
        internal fun empty(): FieldrefConstant {
            return FieldrefConstant()
        }

        fun of(classIndex: Int, nameAndTypeIndex: Int): FieldrefConstant {
            require(classIndex >= 1) { "classIndex must be a positive number" }
            require(nameAndTypeIndex >= 1) { "nameAndTypeIndex must be a positive number" }
            return FieldrefConstant(classIndex, nameAndTypeIndex)
        }
    }
}
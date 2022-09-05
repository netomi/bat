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

import com.github.netomi.bat.classfile.*
import com.github.netomi.bat.classfile.constant.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A constant representing a CONSTANT_MethodHandle_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.8">CONSTANT_MethodHandle_info Structure</a>
 */
data class MethodHandleConstant private constructor(private var _referenceKind:  Int =  0,
                                                    private var _referenceIndex: Int = -1) : Constant() {

    override val type: ConstantType
        get() = ConstantType.METHOD_HANDLE

    val referenceKind: ReferenceKind
        get() = ReferenceKind.of(_referenceKind)

    val referenceIndex: Int
        get() = _referenceIndex

    @Throws(IOException::class)
    override fun readConstantInfo(input: DataInput) {
        _referenceKind  = input.readUnsignedByte()
        _referenceIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: DataOutput) {
        output.writeByte(_referenceKind)
        output.writeShort(_referenceIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ConstantVisitor) {
        visitor.visitMethodHandleConstant(classFile, this)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantPoolVisitor) {
        visitor.visitMethodHandleConstant(classFile, index, this)
    }

    fun referenceAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(referenceIndex, visitor)
    }

    companion object {
        internal fun empty(): MethodHandleConstant {
            return MethodHandleConstant()
        }

        fun of(referenceKind: Int, referenceIndex: Int): MethodHandleConstant {
            require(referenceIndex >= 1) { "referenceIndex must be a positive number" }
            return MethodHandleConstant(referenceKind, referenceIndex)
        }
    }
}

enum class ReferenceKind constructor(val value: Int, val simpleName: String) {
    GET_FIELD         (REF_getField,         "REF_getField"),
    GET_STATIC        (REF_getStatic,        "REF_getStatic"),
    PUT_FIELD         (REF_putField,         "REF_putField"),
    PUT_STATIC        (REF_putStatic,        "REF_putStatic"),
    INVOKE_VIRTUAL    (REF_invokeVirtual,    "REF_invokeVirtual"),
    INVOKE_STATIC     (REF_invokeStatic,     "REF_invokeStatic"),
    INVOKE_SPECIAL    (REF_invokeSpecial,    "REF_invokeSpecial"),
    NEW_INVOKE_SPECIAL(REF_newInvokeSpecial, "REF_newInvokeSpecial"),
    INVOKE_INTERFACE  (REF_invokeInterface,  "REF_invokeInterface");

    companion object {
        private val valueToReferenceKindMap: Map<Int, ReferenceKind> by lazy {
            ReferenceKind.values().associateBy { it.value }
        }

        fun of(value: Int) : ReferenceKind {
            return valueToReferenceKindMap[value] ?: throw IllegalArgumentException("unknown reference kind '$value'")
        }
    }
}
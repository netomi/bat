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
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import java.io.DataOutput
import java.io.IOException

/**
 * Base class for constants as contained in a constant pool.
 */
abstract class Constant {

    abstract val type: ConstantType

    internal val constantPoolSize: Int
        get() = type.constantPoolSize

    @Throws(IOException::class)
    internal abstract fun readConstantInfo(input: ClassDataInput)

    @Throws(IOException::class)
    internal abstract fun writeConstantInfo(output: ClassDataOutput)

    @Throws(IOException::class)
    internal fun write(output: ClassDataOutput) {
        output.writeByte(type.tag)
        writeConstantInfo(output)
    }

    fun accept(classFile: ClassFile, visitor: ConstantVisitor) {
        accept(classFile, -1, visitor)
    }

    abstract fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor)

    companion object {
        internal fun read(input: ClassDataInput): Constant {
            val tag      = input.readUnsignedByte()
            val constant = ConstantType.of(tag).createConstant()
            constant.readConstantInfo(input)

            return constant
        }
    }
}

/**
 * Known constant types as contained in a java class file.
 */
enum class ConstantType constructor(val tag: Int, val constantPoolSize: Int, private val supplier: () -> Constant) {

    // Valid constants and their corresponding tags:
    // https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.4-210

    UTF8                ( 1, 1, Utf8Constant.Companion::empty),
    INTEGER             ( 3, 1, IntegerConstant.Companion::empty),
    FLOAT               ( 4, 1, FloatConstant.Companion::empty),
    LONG                ( 5, 2, LongConstant.Companion::empty),
    DOUBLE              ( 6, 2, DoubleConstant.Companion::empty),
    CLASS               ( 7, 1, ClassConstant.Companion::empty),
    STRING              ( 8, 1, StringConstant.Companion::empty),
    FIELD_REF           ( 9, 1, FieldrefConstant.Companion::empty),
    METHOD_REF          (10, 1, MethodrefConstant.Companion::empty),
    INTERFACE_METHOD_REF(11, 1, InterfaceMethodrefConstant.Companion::empty),
    NAME_AND_TYPE       (12, 1, NameAndTypeConstant.Companion::empty),
    METHOD_HANDLE       (15, 1, MethodHandleConstant.Companion::empty),
    METHOD_TYPE         (16, 1, MethodTypeConstant.Companion::empty),
    DYNAMIC             (17, 1, DynamicConstant.Companion::empty),
    INVOKE_DYNAMIC      (18, 1, InvokeDynamicConstant.Companion::empty),
    MODULE              (19, 1, ModuleConstant.Companion::empty),
    PACKAGE             (20, 1, PackageConstant.Companion::empty);

    companion object {
        private val tagToConstantMap: Map<Int, ConstantType> by lazy {
            values().associateBy { it.tag }
        }

        fun of(tag: Int) : ConstantType {
            return tagToConstantMap[tag] ?: throw IllegalArgumentException("unknown constant tag '$tag'")
        }
    }

    fun createConstant(): Constant {
        return supplier.invoke()
    }
}

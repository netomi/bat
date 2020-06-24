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
import com.github.netomi.bat.classfile.ConstantPool
import com.github.netomi.bat.classfile.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.visitor.ConstantVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * Base class for constants as contained in a constant pool.
 *
 * @author Thomas Neidhart
 */
abstract class Constant {

    abstract val type: Type

    @Throws(IOException::class)
    abstract fun readConstantInfo(input: DataInput)

    @Throws(IOException::class)
    abstract fun writeConstantInfo(output: DataOutput)

    @Throws(IOException::class)
    fun write(output: DataOutput) {
        output.writeByte(type.tag)
        writeConstantInfo(output)
    }

    abstract fun accept(classFile: ClassFile,
                        visitor:   ConstantVisitor)

    abstract fun accept(classFile: ClassFile,
                        constantPool: ConstantPool,
                        index:        Int,
                        visitor:      ConstantPoolVisitor)

    companion object {
        @JvmStatic
        fun read(input : DataInput): Constant {
            val tag      = input.readUnsignedByte()
            val constant = Type.of(tag).createConstant()
            constant.readConstantInfo(input)

            return constant
        }
    }

    /**
     * Known constant types as contained in a java class file.
     */
    enum class Type constructor(val tag: Int, val constantPoolSize: Int, val supplier: () -> Constant) {

        // Valid constants and their corresponding tags:
        // https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.4-210

        UTF8                ( 1, 1, Utf8Constant.Companion::create),
        INTEGER             ( 3, 1, IntegerConstant.Companion::create),
        FLOAT               ( 4, 1, FloatConstant.Companion::create),
        LONG                ( 5, 2, LongConstant.Companion::create),
        DOUBLE              ( 6, 2, DoubleConstant.Companion::create),
        CLASS               ( 7, 1, ClassConstant.Companion::create),
        STRING              ( 8, 1, StringConstant.Companion::create),
        FIELD_REF           ( 9, 1, FieldrefConstant.Companion::create),
        METHOD_REF          (10, 1, MethodrefConstant.Companion::create),
        INTERFACE_METHOD_REF(11, 1, InterfaceMethodrefConstant.Companion::create),
        NAME_AND_TYPE       (12, 1, NameAndTypeConstant.Companion::create),
        METHOD_HANDLE       (15, 1, MethodHandleConstant.Companion::create),
        METHOD_TYPE         (16, 1, MethodTypeConstant.Companion::create),
        DYNAMIC             (17, 1, DynamicConstant.Companion::create),
        INVOKE_DYNAMIC      (18, 1, InvokeDynamicConstant.Companion::create),
        MODULE              (19, 1, ModuleConstant.Companion::create),
        PACKAGE             (20, 1, PackageConstant.Companion::create);

        companion object {
            private val tagToConstantMap: Map<Int, Type> by lazy {
                values().map { it.tag to it }.toMap()
            }

            fun of(tag: Int) : Type {
                return tagToConstantMap[tag] ?: throw IllegalArgumentException("Unknown constant tag '$tag'")
            }
        }

        fun createConstant(): Constant {
            return supplier.invoke()
        }
    }
}


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

package com.github.netomi.bat.classfile.instruction

import com.github.netomi.bat.classfile.*
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.instruction.editor.InstructionWriter
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import java.util.*

class ArrayPrimitiveTypeInstruction : JvmInstruction {

    var arrayType: ArrayType = ArrayType.INT
        private set

    private constructor(opCode: JvmOpCode): super(opCode)

    private constructor(opCode: JvmOpCode, arrayType: ArrayType): super(opCode) {
        this.arrayType = arrayType
    }

    override fun read(instructions: ByteArray, offset: Int) {
        arrayType = ArrayType.of(instructions[offset + 1].toInt())
    }

    override fun write(writer: InstructionWriter, offset: Int) {
        writer.write(offset, opCode.value.toByte())
        writer.write(offset + 1, arrayType.value.toByte())
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitArrayPrimitiveTypeInstruction(classFile, method, code, offset, this)
    }

    override fun toString(): String {
        return "$mnemonic ${arrayType.toString().lowercase(Locale.getDefault())}"
    }

    companion object {
        internal fun create(opCode: JvmOpCode): ArrayPrimitiveTypeInstruction {
            return ArrayPrimitiveTypeInstruction(opCode)
        }

        fun of(opCode: JvmOpCode, arrayType: String): ArrayPrimitiveTypeInstruction {
            return of(opCode, ArrayType.of(arrayType))
        }

        fun of(opCode: JvmOpCode, arrayType: ArrayType): ArrayPrimitiveTypeInstruction {
            return ArrayPrimitiveTypeInstruction(opCode, arrayType)
        }
    }
}

enum class ArrayType constructor(val value: Int) {
    BOOLEAN(T_BOOLEAN),
    CHAR   (T_CHAR),
    FLOAT  (T_FLOAT),
    DOUBLE (T_DOUBLE),
    BYTE   (T_BYTE),
    SHORT  (T_SHORT),
    INT    (T_INT),
    LONG   (T_LONG);

    companion object {
        fun of(value: Int): ArrayType {
            for (type in values()) {
                if (type.value == value) {
                    return type
                }
            }

            error("unexpected array type value '$value'")
        }

        fun of(str: String): ArrayType {
            val value = str.uppercase(Locale.getDefault())
            for (type in values()) {
                if (type.name == value) {
                    return type
                }
            }

            error("unexpected array type value '$value'")
        }
    }
}
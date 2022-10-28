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
import com.github.netomi.bat.classfile.instruction.editor.OffsetMap
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.util.*
import java.util.*

class ArrayPrimitiveTypeInstruction : JvmInstruction {

    var primitiveType: PrimitiveType = PrimitiveType.INT
        private set

    val arrayType: JvmType
        get() = "[${primitiveType.type}".asJvmType()

    private constructor(opCode: JvmOpCode): super(opCode)

    private constructor(opCode: JvmOpCode, primitiveType: PrimitiveType): super(opCode) {
        this.primitiveType = primitiveType
    }

    override fun read(instructions: ByteArray, offset: Int) {
        primitiveType = PrimitiveType.of(instructions[offset + 1].toInt())
    }

    override fun writeData(writer: InstructionWriter, offset: Int) {
        writer.write(offset, opCode.value.toByte())
        writer.write(offset + 1, primitiveType.value.toByte())
    }

    override fun updateOffsets(offset: Int, offsetMap: OffsetMap) {}

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitArrayPrimitiveTypeInstruction(classFile, method, code, offset, this)
    }

    override fun toString(): String {
        return "$mnemonic ${primitiveType.toString().lowercase(Locale.getDefault())}"
    }

    companion object {
        internal fun create(opCode: JvmOpCode): ArrayPrimitiveTypeInstruction {
            return ArrayPrimitiveTypeInstruction(opCode)
        }

        fun of(opCode: JvmOpCode, primitiveType: String): ArrayPrimitiveTypeInstruction {
            return of(opCode, PrimitiveType.of(primitiveType))
        }

        fun of(opCode: JvmOpCode, primitiveType: PrimitiveType): ArrayPrimitiveTypeInstruction {
            return ArrayPrimitiveTypeInstruction(opCode, primitiveType)
        }
    }
}

enum class PrimitiveType constructor(val value: Int, val type: String) {
    BOOLEAN(T_BOOLEAN, BOOLEAN_TYPE),
    CHAR   (T_CHAR,    CHAR_TYPE),
    FLOAT  (T_FLOAT,   FLOAT_TYPE),
    DOUBLE (T_DOUBLE,  DOUBLE_TYPE),
    BYTE   (T_BYTE,    BYTE_TYPE),
    SHORT  (T_SHORT,   SHORT_TYPE),
    INT    (T_INT,     INT_TYPE),
    LONG   (T_LONG,    LONG_TYPE);

    companion object {
        fun of(value: Int): PrimitiveType {
            for (type in values()) {
                if (type.value == value) {
                    return type
                }
            }

            error("unexpected primitive type value '$value'")
        }

        fun of(str: String): PrimitiveType {
            val value = str.uppercase(Locale.getDefault())
            for (type in values()) {
                if (type.name == value) {
                    return type
                }
            }

            error("unexpected primitive type value '$value'")
        }
    }
}
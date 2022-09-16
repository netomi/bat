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

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.instruction.JvmOpCode.*
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor

class LiteralInstruction private constructor(opCode: JvmOpCode): JvmInstruction(opCode) {

    var value: Long = 0
        private set

    val valueIsImplicit: Boolean
        get() = opCode.length == 1

    val type: LiteralType = LiteralType.of(opCode.mnemonic.first())

    val valueAsInt: Int
        get() {
            check(type == LiteralType.INT)
            return value.toInt()
        }

    val valueAsFloat: Float
        get() {
            check(type == LiteralType.FLOAT)
            return Float.fromBits(value.toInt())
        }

    val valueAsDouble: Double
        get() {
            check(type == LiteralType.DOUBLE)
            return Double.fromBits(value)
        }

    override fun read(instructions: ByteArray, offset: Int) {
        value = when (opCode) {
            DCONST_0 -> 0.0.toBits()
            DCONST_1 -> 1.0.toBits()

            FCONST_0 -> 0.0f.toBits().toLong()
            FCONST_1 -> 1.0f.toBits().toLong()
            FCONST_2 -> 2.0f.toBits().toLong()

            ICONST_M1 -> -1
            ICONST_0  ->  0
            ICONST_1  ->  1
            ICONST_2  ->  2
            ICONST_3  ->  3
            ICONST_4  ->  4
            ICONST_5  ->  5

            LCONST_0 -> 0
            LCONST_1 -> 1

            BIPUSH -> instructions[offset + 1].toLong()
            SIPUSH -> getLiteral(instructions[offset + 1], instructions[offset + 2]).toLong()

            else -> error("unexpected opCode '${opCode.mnemonic}'")
        }
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitLiteralInstruction(classFile, method, code, offset, this)
    }

    companion object {
        internal fun create(opCode: JvmOpCode): JvmInstruction {
            return LiteralInstruction(opCode)
        }
    }
}

enum class LiteralType constructor(private val tag: Char) {
    INT   ('i'),
    LONG  ('l'),
    FLOAT ('f'),
    DOUBLE('d');

    companion object {
        private val tagToTypeMap: Map<Char, LiteralType> = values().associateBy { it.tag  }

        fun of(tag: Char): LiteralType {
            return tagToTypeMap[tag] ?: INT
        }
    }
}
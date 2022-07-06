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

package com.github.netomi.bat.dexfile.value

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor

/**
 * A class representing a byte value inside a dex file.
 */
data class EncodedByteValue internal constructor(private var value_: Byte = 0.toByte()) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_BYTE

    val value: Byte
        get() = value_

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value_ = input.readByte()
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, 0)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeByte(value_)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitByteValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedByteValue[value=0x%02x]".format(value_)
    }

    companion object {
        @JvmStatic
        fun of(value: Byte): EncodedByteValue {
            return EncodedByteValue(value)
        }
    }
}

/**
 * A class representing a int value inside a dex file.
 */
data class EncodedIntValue internal constructor(private var value_: Int = 0) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_INT

    val value: Int
        get() = value_

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value_ = input.readInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForSignedInt(value_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(value_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitIntValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedIntValue[value=%d]".format(value_)
    }

    companion object {
        @JvmStatic
        fun of(value: Int): EncodedIntValue {
            return EncodedIntValue(value)
        }
    }
}

/**
 * A class representing a char value inside a dex file.
 */
data class EncodedCharValue internal constructor(private var value_: Char = 0.toChar()) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_CHAR

    val value: Char
        get() = value_

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value_ = input.readChar(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedChar(value_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeChar(value_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitCharValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedCharValue[value=%d,\'%c\']".format(value_.code, value_)
    }

    companion object {
        @JvmStatic
        fun of(value: Char): EncodedCharValue {
            return EncodedCharValue(value)
        }
    }
}

/**
 * A class representing a boolean value inside a dex file.
 */
data class EncodedBooleanValue internal constructor(private var value_: Boolean = false) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_BOOLEAN

    val value: Boolean
        get() = value_

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value_ = valueArg and 0x1 == 1
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, if (value_) 1 else 0)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {}
    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitBooleanValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedBooleanValue[value=%s]".format(value_)
    }

    companion object {
        @JvmStatic
        fun of(value: Boolean): EncodedBooleanValue {
            return EncodedBooleanValue(value)
        }
    }
}

/**
 * A class representing a short value inside a dex file.
 */
data class EncodedShortValue internal constructor(private var value_: Short = 0.toShort()) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_SHORT

    val value: Short
        get() = value_

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value_ = input.readShort(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForSignedShort(value_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeShort(value_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitShortValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedShortValue[value=%d]".format(value_)
    }

    companion object {
        @JvmStatic
        fun of(value: Short): EncodedShortValue {
            return EncodedShortValue(value)
        }
    }
}

/**
 * A class representing a double value inside a dex file.
 */
data class EncodedDoubleValue internal constructor(private var value_: Double = 0.0) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_DOUBLE

    val value: Double
        get() = value_

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value_ = input.readDouble(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForDouble(value_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeDouble(value_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitDoubleValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedDoubleValue[value=%f]".format(value_)
    }

    companion object {
        @JvmStatic
        fun of(value: Double): EncodedDoubleValue {
            return EncodedDoubleValue(value)
        }
    }
}

/**
 * A class representing a float value inside a dex file.
 */
data class EncodedFloatValue internal constructor(private var value_: Float = 0f) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_FLOAT

    val value: Float
        get() = value_

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value_ = input.readFloat(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForFloat(value_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeFloat(value_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitFloatValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedFloatValue[value=%f]".format(value_)
    }

    companion object {
        @JvmStatic
        fun of(value: Float): EncodedFloatValue {
            return EncodedFloatValue(value)
        }
    }
}

/**
 * A class representing a int value inside a dex file.
 */
data class EncodedLongValue internal constructor(private var value_: Long = 0) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_LONG

    val value: Long
        get() = value_

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value_ = input.readLong(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForSignedLong(value_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeLong(value_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitLongValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedLongValue[value=%d]".format(value_)
    }

    companion object {
        @JvmStatic
        fun of(value: Long): EncodedLongValue {
            return EncodedLongValue(value)
        }
    }
}

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
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor

/**
 * A class representing a byte value inside a dex file.
 */
data class EncodedByteValue internal constructor(var value: Byte = 0.toByte()) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.BYTE

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value = input.readByte()
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, 0)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeByte(value)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitByteValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {}

    override fun toString(): String {
        return "EncodedByteValue[value=0x%02x]".format(value)
    }

    companion object {
        @JvmStatic
        fun of(value: Byte): EncodedByteValue {
            return EncodedByteValue(value)
        }
    }
}

/**
 * A class representing an int value inside a dex file.
 */
data class EncodedIntValue internal constructor(var value: Int = 0) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.INT

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value = input.readInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForSignedInt(value) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(value, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitIntValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {}

    override fun toString(): String {
        return "EncodedIntValue[value=${value}]"
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
data class EncodedCharValue internal constructor(var value: Char = 0.toChar()) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.CHAR

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value = input.readChar(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedChar(value) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeChar(value, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitCharValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {}

    override fun toString(): String {
        return "EncodedCharValue[value=%d,\'%c\']".format(value.code, value)
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
data class EncodedBooleanValue internal constructor(var value: Boolean = false) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.BOOLEAN

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value = valueArg and 0x1 == 1
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, if (value) 1 else 0)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {}

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitBooleanValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {}

    override fun toString(): String {
        return "EncodedBooleanValue[value=${value}]"
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
data class EncodedShortValue internal constructor(var value: Short = 0.toShort()) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.SHORT

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value = input.readShort(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForSignedShort(value) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeShort(value, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitShortValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {}

    override fun toString(): String {
        return "EncodedShortValue[value=${value}]"
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
data class EncodedDoubleValue internal constructor(var value: Double = 0.0) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.DOUBLE

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value = input.readDouble(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForDouble(value) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeDouble(value, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitDoubleValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {}

    override fun toString(): String {
        return "EncodedDoubleValue[value=%f]".format(value)
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
data class EncodedFloatValue internal constructor(var value: Float = 0f) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.FLOAT

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value = input.readFloat(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForFloat(value) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeFloat(value, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitFloatValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {}

    override fun toString(): String {
        return "EncodedFloatValue[value=%f]".format(value)
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
data class EncodedLongValue internal constructor(var value: Long = 0) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.LONG

    override fun readValue(input: DexDataInput, valueArg: Int) {
        value = input.readLong(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForSignedLong(value) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeLong(value, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitLongValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {}

    override fun toString(): String {
        return "EncodedLongValue[value=${value}]"
    }

    companion object {
        @JvmStatic
        fun of(value: Long): EncodedLongValue {
            return EncodedLongValue(value)
        }
    }
}

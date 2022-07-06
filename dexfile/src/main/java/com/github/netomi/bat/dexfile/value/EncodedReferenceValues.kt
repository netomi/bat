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

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.DexConstants.NO_INDEX
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor
import com.github.netomi.bat.util.Preconditions
import java.util.*

/**
 * A class representing a referenced string (StringID) value inside a dex file.
 */
data class EncodedStringValue internal constructor(private var stringIndex_: Int) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_STRING

    val stringIndex: Int
        get() = stringIndex_

    internal constructor() : this(DexConstants.NO_INDEX) {}

    fun getStringValue(dexFile: DexFile): String {
        return dexFile.getStringID(stringIndex_).stringValue
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        stringIndex_ = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(stringIndex_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(stringIndex_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitStringValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedStringValue[stringIdx=%d]".format(stringIndex_)
    }

    companion object {
        @JvmStatic
        fun of(stringIndex: Int): EncodedStringValue {
            Preconditions.checkArgument(stringIndex >= 0, "stringIndex must not be negative")
            return EncodedStringValue(stringIndex)
        }
    }
}

/**
 * A class representing a referenced field (FieldID) value inside a dex file.
 */
data class EncodedFieldValue internal constructor(private var fieldIndex_: Int) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_FIELD

    val fieldIndex: Int
        get() = fieldIndex_

    internal constructor() : this(DexConstants.NO_INDEX)

    fun getFieldID(dexFile: DexFile): FieldID {
        return dexFile.getFieldID(fieldIndex_)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        fieldIndex_ = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(fieldIndex_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(fieldIndex_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitFieldValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedFieldValue[fieldIdx=%d]".format(fieldIndex_)
    }

    companion object {
        @JvmStatic
        fun of(fieldIndex: Int): EncodedFieldValue {
            Preconditions.checkArgument(fieldIndex >= 0, "fieldIndex must not be negative")
            return EncodedFieldValue(fieldIndex)
        }
    }
}

/**
 * A class representing a referenced method (MethodID) value inside a dex file.
 */
data class EncodedMethodValue internal constructor(private var methodIndex_: Int) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_METHOD

    val methodIndex: Int
        get() = methodIndex_

    internal constructor() : this(DexConstants.NO_INDEX)

    fun getMethodID(dexFile: DexFile): MethodID {
        return dexFile.getMethodID(methodIndex_)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        methodIndex_ = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(methodIndex_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(methodIndex_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitMethodValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedMethodValue[methodIdx=%d]".format(methodIndex_)
    }

    companion object {
        @JvmStatic
        fun of(methodIndex: Int): EncodedMethodValue {
            Preconditions.checkArgument(methodIndex >= 0, "methodIndex must not be negative")
            return EncodedMethodValue(methodIndex)
        }
    }
}

/**
 * A class representing a referenced type (TypeID) value inside a dex file.
 */
data class EncodedTypeValue internal constructor(private var typeIndex_: Int) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_TYPE

    val typeIndex: Int
        get() = typeIndex_

    internal constructor() : this(DexConstants.NO_INDEX)

    fun getType(dexFile: DexFile): String {
        return dexFile.getTypeID(typeIndex_).getType(dexFile)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        typeIndex_ = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(typeIndex_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(typeIndex_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitTypeValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedTypeValue[typeIdx=%d]".format(typeIndex)
    }

    companion object {
        @JvmStatic
        fun of(typeIndex: Int): EncodedTypeValue {
            Preconditions.checkArgument(typeIndex >= 0, "typeIndex must not be negative")
            return EncodedTypeValue(typeIndex)
        }
    }
}

/**
 * A class representing a referenced enum (FieldID) value inside a dex file.
 */
data class EncodedEnumValue internal constructor(private var fieldIndex_: Int = NO_INDEX) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_ENUM

    val fieldIndex: Int
        get() = fieldIndex_

    fun getFieldID(dexFile: DexFile): FieldID {
        return dexFile.getFieldID(fieldIndex_)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        fieldIndex_ = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(fieldIndex_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(fieldIndex_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitEnumValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedEnumValue[fieldIdx=%d]".format(fieldIndex)
    }

    companion object {
        @JvmStatic
        fun of(fieldIndex: Int): EncodedEnumValue {
            Preconditions.checkArgument(fieldIndex >= 0, "fieldIndex must not be negative")
            return EncodedEnumValue(fieldIndex)
        }
    }
}

/**
 * A class representing a referenced method handle (MethodHandle) value inside a dex file.
 */
data class EncodedMethodHandleValue internal constructor(private var handleIndex_: Int = NO_INDEX) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_METHOD_HANDLE

    val handleIndex: Int
        get() = handleIndex_

    fun getMethodHandle(dexFile: DexFile): MethodHandle {
        return dexFile.getMethodHandle(handleIndex_)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        handleIndex_ = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(handleIndex_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(handleIndex_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitMethodHandleValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedMethodHandleValue[methodHandleIdx=%d]".format(handleIndex_)
    }

    companion object {
        @JvmStatic
        fun of(handleIndex: Int): EncodedMethodHandleValue {
            Preconditions.checkArgument(handleIndex >= 0, "handleIndex must not be negative")
            return EncodedMethodHandleValue(handleIndex)
        }
    }
}

/**
 * A class representing a referenced method type (ProtoID) value inside a dex file.
 */
data class EncodedMethodTypeValue internal constructor(private var protoIndex_: Int = NO_INDEX) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_METHOD_TYPE

    val protoIndex: Int
        get() = protoIndex_

    fun getProtoID(dexFile: DexFile): ProtoID {
        return dexFile.getProtoID(protoIndex_)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        protoIndex_ = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(protoIndex_) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(protoIndex_, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitMethodTypeValue(dexFile, this)
    }

    override fun toString(): String {
        return "EncodedMethodTypeValue[protoIdx=%d]".format(protoIndex_)
    }

    companion object {
        @JvmStatic
        fun of(protoIndex: Int): EncodedMethodTypeValue {
            Preconditions.checkArgument(protoIndex >= 0, "protoIndex must not be negative")
            return EncodedMethodTypeValue(protoIndex)
        }
    }
}
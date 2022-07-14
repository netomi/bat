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
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.PropertyAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.github.netomi.bat.util.Preconditions

/**
 * A class representing a referenced string (StringID) value inside a dex file.
 */
data class EncodedStringValue internal constructor(var stringIndex: Int = NO_INDEX) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.STRING

    fun getStringValue(dexFile: DexFile): String {
        return dexFile.getStringID(stringIndex).stringValue
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        stringIndex = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(stringIndex) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(stringIndex, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitStringValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitStringID(dexFile, PropertyAccessor(this::stringIndex))
    }

    override fun toString(): String {
        return "EncodedStringValue[stringIdx=${stringIndex}]"
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
data class EncodedFieldValue internal constructor(var fieldIndex: Int = NO_INDEX) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.FIELD

    fun getFieldID(dexFile: DexFile): FieldID {
        return dexFile.getFieldID(fieldIndex)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        fieldIndex = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(fieldIndex) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(fieldIndex, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitFieldValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitFieldID(dexFile, PropertyAccessor(this::fieldIndex))
    }

    override fun toString(): String {
        return "EncodedFieldValue[fieldIdx=${fieldIndex}]"
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
data class EncodedMethodValue internal constructor(var methodIndex: Int = NO_INDEX) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.METHOD

    fun getMethodID(dexFile: DexFile): MethodID {
        return dexFile.getMethodID(methodIndex)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        methodIndex = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(methodIndex) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(methodIndex, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitMethodValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitMethodID(dexFile, PropertyAccessor(this::methodIndex))
    }

    override fun toString(): String {
        return "EncodedMethodValue[methodIdx=${methodIndex}]"
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
data class EncodedTypeValue internal constructor(var typeIndex: Int = NO_INDEX) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.TYPE

    fun getType(dexFile: DexFile): String {
        return dexFile.getTypeID(typeIndex).getType(dexFile)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        typeIndex = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(typeIndex) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(typeIndex, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitTypeValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitTypeID(dexFile, PropertyAccessor(this::typeIndex))
    }

    override fun toString(): String {
        return "EncodedTypeValue[typeIdx=${typeIndex}]"
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
data class EncodedEnumValue internal constructor(var fieldIndex: Int = NO_INDEX) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.ENUM

    fun getFieldID(dexFile: DexFile): FieldID {
        return dexFile.getFieldID(fieldIndex)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        fieldIndex = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(fieldIndex) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(fieldIndex, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitEnumValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitFieldID(dexFile, PropertyAccessor(this::fieldIndex))
    }

    override fun toString(): String {
        return "EncodedEnumValue[fieldIdx=${fieldIndex}]"
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
data class EncodedMethodHandleValue internal constructor(var handleIndex: Int = NO_INDEX) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.METHOD_HANDLE

    fun getMethodHandle(dexFile: DexFile): MethodHandle {
        return dexFile.getMethodHandle(handleIndex)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        handleIndex = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(handleIndex) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(handleIndex, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitMethodHandleValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitMethodHandle(dexFile, PropertyAccessor(this::handleIndex))
    }

    override fun toString(): String {
        return "EncodedMethodHandleValue[methodHandleIdx=${handleIndex}]"
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
data class EncodedMethodTypeValue internal constructor(var protoIndex: Int = NO_INDEX) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.METHOD_TYPE

    fun getProtoID(dexFile: DexFile): ProtoID {
        return dexFile.getProtoID(protoIndex)
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        protoIndex = input.readUnsignedInt(valueArg + 1)
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, requiredBytesForUnsignedInt(protoIndex) - 1)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeInt(protoIndex, valueArg + 1)
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitMethodTypeValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitProtoID(dexFile, PropertyAccessor(this::protoIndex))
    }

    override fun toString(): String {
        return "EncodedMethodTypeValue[protoIdx=${protoIndex}]"
    }

    companion object {
        @JvmStatic
        fun of(protoIndex: Int): EncodedMethodTypeValue {
            Preconditions.checkArgument(protoIndex >= 0, "protoIndex must not be negative")
            return EncodedMethodTypeValue(protoIndex)
        }
    }
}
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
package com.github.netomi.bat.dexfile

import com.github.netomi.bat.dexfile.DexConstants.NO_INDEX
import com.github.netomi.bat.dexfile.FieldModifier.Companion.setOf
import com.github.netomi.bat.dexfile.Visibility.Companion.of
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor
import com.google.common.base.Preconditions
import dev.ahmedmourad.nocopy.annotations.NoCopy
import java.util.*

/**
 * A class representing an encoded field inside a dex file.
 *
 * @see [encoded field @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.encoded-field-format)
 */
@NoCopy
data class EncodedField private constructor(
    private var fieldIndex_:  Int = NO_INDEX, // uleb128
    private var accessFlags_: Int = 0
) : DexContent() {

    private var deltaFieldIndex = 0 // uleb128

    val fieldIndex: Int
        get() = fieldIndex_

    val accessFlags: Int
        get() = accessFlags_

    val visibility: Visibility
        get() = of(accessFlags_)

    val modifiers: EnumSet<FieldModifier>
        get() = setOf(accessFlags_)

    fun getFieldID(dexFile: DexFile): FieldID {
        return dexFile.getFieldID(fieldIndex)
    }

    fun getName(dexFile: DexFile): String {
        return getFieldID(dexFile).getName(dexFile)
    }

    fun getType(dexFile: DexFile): String {
        return getFieldID(dexFile).getType(dexFile)
    }

    val isStatic: Boolean
        get() = accessFlags and DexConstants.ACC_STATIC != 0

    override fun read(input: DexDataInput) {
        deltaFieldIndex = input.readUleb128()
        accessFlags_    = input.readUleb128()
    }

    private fun updateFieldIndex(lastIndex: Int) {
        fieldIndex_ = deltaFieldIndex + lastIndex
    }

    private fun updateDeltaFieldIndex(lastIndex: Int) {
        deltaFieldIndex = fieldIndex - lastIndex
    }

    fun write(output: DexDataOutput, lastIndex: Int): Int {
        updateDeltaFieldIndex(lastIndex)
        write(output)
        return fieldIndex
    }

    override fun write(output: DexDataOutput) {
        output.writeUleb128(deltaFieldIndex)
        output.writeUleb128(accessFlags)
    }

    fun staticValueAccept(dexFile: DexFile?, classDef: ClassDef, index: Int, visitor: EncodedValueVisitor?) {
        if (isStatic) {
            classDef.staticValueAccept(dexFile, index, visitor)
        }
    }

    override fun toString(): String {
        return "EncodedField[fieldIndex=%d,accessFlags=%04x]".format(fieldIndex, accessFlags)
    }

    companion object {
        @JvmStatic
        fun of(fieldIndex: Int, visibility: Visibility, vararg modifiers: FieldModifier): EncodedField {
            Preconditions.checkArgument(fieldIndex >= 0, "fieldIndex must not be negative")
            var accessFlags = visibility.flagValue
            for (modifier in modifiers) {
                accessFlags = accessFlags or modifier.flagValue
            }
            return EncodedField(fieldIndex, accessFlags)
        }

        @JvmStatic
        fun of(fieldIndex: Int, accessFlags: Int): EncodedField {
            Preconditions.checkArgument(fieldIndex >= 0, "fieldIndex must not be negative")
            return EncodedField(fieldIndex, accessFlags)
        }

        @JvmStatic
        fun readContent(input: DexDataInput, lastIndex: Int): EncodedField {
            val encodedField = EncodedField()
            encodedField.read(input)
            encodedField.updateFieldIndex(lastIndex)
            return encodedField
        }
    }
}
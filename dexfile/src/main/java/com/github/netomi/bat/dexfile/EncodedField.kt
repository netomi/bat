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

import com.github.netomi.bat.dexfile.FieldModifier.Companion.setOf
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.PropertyAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.google.common.base.Preconditions
import java.util.*

/**
 * A class representing an encoded field inside a dex file.
 *
 * @see [encoded field @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.encoded-field-format)
 */
class EncodedField private constructor(_fieldIndex: Int = NO_INDEX, _accessFlags: Int = 0) : EncodedMember(_accessFlags) {

    private var deltaFieldIndex = 0

    var fieldIndex: Int = _fieldIndex
        internal set

    val modifiers: EnumSet<FieldModifier>
        get() = setOf(accessFlags)

    fun getFieldID(dexFile: DexFile): FieldID {
        return dexFile.getFieldID(fieldIndex)
    }

    override fun getName(dexFile: DexFile): String {
        return getFieldID(dexFile).getName(dexFile)
    }

    fun getType(dexFile: DexFile): String {
        return getFieldID(dexFile).getType(dexFile)
    }

    val isStatic: Boolean
        get() = modifiers.contains(FieldModifier.STATIC)

    override fun read(input: DexDataInput) {
        deltaFieldIndex = input.readUleb128()
        accessFlags     = input.readUleb128()
    }

    private fun updateFieldIndex(lastIndex: Int) {
        fieldIndex = deltaFieldIndex + lastIndex
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

    fun staticValueAccept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        if (isStatic) {
            val classType = getFieldID(dexFile).getClassType(dexFile)
            val classDef  = dexFile.getClassDefByType(classType)

            if (classDef != null) {
                val staticFieldIndex = classDef.getStaticFieldIndex(this)
                classDef.staticValueAccept(dexFile, staticFieldIndex, visitor)
            }
        }
    }

    fun staticValueAccept(dexFile: DexFile, classDef: ClassDef, index: Int, visitor: EncodedValueVisitor) {
        if (isStatic) {
            classDef.staticValueAccept(dexFile, index, visitor)
        }
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor)
    {
        visitor.visitFieldID(dexFile, PropertyAccessor({ fieldIndex }, { fieldIndex = it }))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncodedField

        return fieldIndex  == other.fieldIndex &&
               accessFlags == other.accessFlags
    }

    override fun hashCode(): Int {
        return Objects.hash(fieldIndex, accessFlags)
    }

    override fun toString(): String {
        return "EncodedField[fieldIndex=%d,accessFlags=%04x]".format(fieldIndex, accessFlags)
    }

    companion object {
        fun of(fieldIndex: Int, visibility: Visibility, vararg modifiers: FieldModifier): EncodedField {
            Preconditions.checkArgument(fieldIndex >= 0, "fieldIndex must not be negative")
            var accessFlags = visibility.flagValue
            for (modifier in modifiers) {
                accessFlags = accessFlags or modifier.flagValue
            }
            return EncodedField(fieldIndex, accessFlags)
        }

        fun of(fieldIndex: Int, accessFlags: Int): EncodedField {
            Preconditions.checkArgument(fieldIndex >= 0, "fieldIndex must not be negative")
            return EncodedField(fieldIndex, accessFlags)
        }

        fun readContent(input: DexDataInput, lastIndex: Int): EncodedField {
            val encodedField = EncodedField()
            encodedField.read(input)
            encodedField.updateFieldIndex(lastIndex)
            return encodedField
        }
    }
}
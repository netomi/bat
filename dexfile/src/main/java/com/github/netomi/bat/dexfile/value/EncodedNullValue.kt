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
package com.github.netomi.bat.dexfile.value

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor

/**
 * An class representing a null value inside a dex file.
 */
object EncodedNullValue : EncodedValue() {
    override val valueType: Int
        get() = VALUE_NULL

    override fun readValue(input: DexDataInput, valueArg: Int) {}

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, 0)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {}

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitNullValue(dexFile, this)
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(obj: Any?): Boolean {
        return this === obj
    }

    override fun toString(): String {
        return "EncodedNullValue[]"
    }
}
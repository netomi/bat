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

import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.value.EncodedMethodHandleValue
import com.github.netomi.bat.dexfile.value.EncodedMethodTypeValue
import com.github.netomi.bat.dexfile.value.EncodedStringValue
import com.github.netomi.bat.dexfile.value.EncodedValue
import com.github.netomi.bat.dexfile.visitor.CallSiteVisitor
import com.google.common.base.Preconditions

/**
 * A class representing a callsite item inside a dex file.
 *
 * @see [callsite item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.call-site-item)
 */
class CallSite private constructor() : EncodedArray() {

    val methodHandle: EncodedMethodHandleValue
        get() = array[0] as EncodedMethodHandleValue

    fun getMethodHandle(dexFile: DexFile): MethodHandle {
        return methodHandle.getMethodHandle(dexFile)
    }

    val methodName: EncodedStringValue
        get() = array[1] as EncodedStringValue

    fun getMethodName(dexFile: DexFile): String {
        return methodName.getStringValue(dexFile)
    }

    val methodType: EncodedMethodTypeValue
        get() = array[2] as EncodedMethodTypeValue

    fun getMethodType(dexFile: DexFile): ProtoID {
        return methodType.getProtoID(dexFile)
    }

    fun accept(dexFile: DexFile, visitor: CallSiteVisitor) {
        visitor.visitCallSite(dexFile, this)
    }

    override fun toString(): String {
        return "CallSite[values=${array}]"
    }

    companion object {
        internal fun empty(): CallSite {
            return CallSite()
        }

        fun of(methodHandleIndex: Int, nameIndex: Int, protoIndex: Int): CallSite {
            val callSite = CallSite()
            callSite.array.add(EncodedMethodHandleValue.of(methodHandleIndex))
            callSite.array.add(EncodedStringValue.of(nameIndex))
            callSite.array.add(EncodedMethodTypeValue.of(protoIndex))
            return callSite
        }

        fun of(methodHandleIndex: Int, vararg encodedValues: EncodedValue): CallSite {
            Preconditions.checkArgument(encodedValues.size >= 2)

            val callSite = CallSite()
            callSite.array.add(EncodedMethodHandleValue.of(methodHandleIndex))

            Preconditions.checkArgument(encodedValues[0] is EncodedStringValue)
            Preconditions.checkArgument(encodedValues[1] is EncodedMethodTypeValue)

            for (encodedValue in encodedValues) {
                callSite.array.add(encodedValue)
            }

            return callSite
        }

        internal fun read(input: DexDataInput): CallSite {
            val callSite = CallSite()
            callSite.read(input)
            return callSite
        }
    }
}
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

/**
 * A class representing a callsite item inside a dex file.
 *
 * @see [callsite item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.call-site-item)
 */
class CallSite private constructor() : EncodedArray() {
    val methodHandle: EncodedMethodHandleValue
        get() = array.values[0] as EncodedMethodHandleValue

    fun getMethodHandle(dexFile: DexFile): MethodHandle {
        return methodHandle.getMethodHandle(dexFile)
    }

    val methodName: EncodedStringValue
        get() = array.values[1] as EncodedStringValue

    fun getMethodName(dexFile: DexFile): String {
        return methodName.getStringValue(dexFile)
    }

    val methodType: EncodedMethodTypeValue
        get() = array.values[2] as EncodedMethodTypeValue

    fun getMethodType(dexFile: DexFile): ProtoID {
        return methodType.getProtoID(dexFile)
    }

    override fun toString(): String {
        return "CallSite[values=${array}]"
    }

    companion object {
        @JvmStatic
        fun empty(): CallSite {
            return CallSite()
        }

        @JvmStatic
        fun of(methodHandleIndex: Int, nameIndex: Int, protoIndex: Int): CallSite {
            val callSite = CallSite()
            callSite.array.addEncodedValue(EncodedMethodHandleValue.of(methodHandleIndex))
            callSite.array.addEncodedValue(EncodedStringValue.of(nameIndex))
            callSite.array.addEncodedValue(EncodedMethodTypeValue.of(protoIndex))
            return callSite
        }

        @JvmStatic
        fun readContent(input: DexDataInput): CallSite {
            val callSite = CallSite()
            callSite.read(input)
            return callSite
        }
    }
}
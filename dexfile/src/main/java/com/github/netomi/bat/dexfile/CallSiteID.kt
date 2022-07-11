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
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import java.util.*

/**
 * A class representing a callsite id item inside a dex file.
 *
 * @see [callsite id item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.call-site-id-item)
 */
@DataItemAnn(
    type          = DexConstants.TYPE_CALL_SITE_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false)
class CallSiteID private constructor() : DataItem() {

    var callSite: CallSite = CallSite.empty()
        private set

    var callSiteOffset = 0
        private set

    private constructor(callSite: CallSite): this() {
        this.callSite = callSite
    }

    override fun read(input: DexDataInput) {
        input.skipAlignmentPadding(dataAlignment)
        callSiteOffset = input.readInt()
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        input.offset = callSiteOffset
        callSite = CallSite.readContent(input)
    }

    override fun updateOffsets(dataItemMap: Map) {
        callSiteOffset = dataItemMap.getOffset(callSite)
    }

    override fun write(output: DexDataOutput) {
        output.writeAlignmentPadding(dataAlignment)
        output.writeInt(callSiteOffset)
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitCallSite(dexFile, this, callSite)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CallSiteID

        return callSite == other.callSite
    }

    override fun hashCode(): Int {
        return Objects.hash(callSite)
    }

    override fun toString(): String {
        return "CallSiteID[callSite=${callSite}]"
    }

    companion object {
        @JvmStatic
        fun of(callSite: CallSite): CallSiteID {
            Objects.requireNonNull(callSite, "callSite must not be null")
            return CallSiteID(callSite)
        }

        @JvmStatic
        fun readContent(input: DexDataInput): CallSiteID {
            val callSiteID = CallSiteID()
            callSiteID.read(input)
            return callSiteID
        }
    }
}
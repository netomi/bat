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
import com.github.netomi.bat.dexfile.visitor.CallSiteVisitor
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor

/**
 * A class representing a callsite id item inside a dex file.
 *
 * @see [callsite id item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.call-site-id-item)
 */
@DataItemAnn(
    type          = TYPE_CALL_SITE_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false)
class CallSiteID private constructor(callSite: CallSite = CallSite.empty()) : DataItem() {

    var callSite: CallSite = callSite
        private set

    var callSiteOffset = 0
        private set

    override val isEmpty: Boolean
        get() = callSite.isEmpty

    override fun read(input: DexDataInput) {
        callSiteOffset = input.readInt()
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        input.offset = callSiteOffset
        callSite = CallSite.read(input)
    }

    override fun updateOffsets(dataItemMap: Map) {
        callSiteOffset = dataItemMap.getOffset(callSite)
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(callSiteOffset)
    }

    fun accept(dexFile: DexFile, visitor: CallSiteVisitor) {
        callSite.accept(dexFile, visitor)
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitCallSite(dexFile, this, callSite)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        callSite.referencedIDsAccept(dexFile, visitor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CallSiteID

        return callSite == other.callSite
    }

    override fun hashCode(): Int {
        return callSite.hashCode()
    }

    override fun toString(): String {
        return "CallSiteID[callSite=${callSite}]"
    }

    companion object {
        fun of(callSite: CallSite): CallSiteID {
            return CallSiteID(callSite)
        }

        internal fun read(input: DexDataInput): CallSiteID {
            val callSiteID = CallSiteID()
            callSiteID.read(input)
            return callSiteID
        }
    }
}
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
package com.github.netomi.bat.dexfile;

import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;

import java.util.Objects;

/**
 * A class representing a callsite id item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#call-site-id-item">callsite id item @ dex format</a>
 *
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_CALL_SITE_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class CallSiteID
extends      DataItem
{
    private int      callSiteOffset; // uint
    private CallSite callSite;

    public static CallSiteID of(CallSite callSite) {
        Objects.requireNonNull(callSite, "callSite must not be null");
        return new CallSiteID(callSite);
    }

    public static CallSiteID readContent(DexDataInput input) {
        CallSiteID callSiteID = new CallSiteID();
        callSiteID.read(input);
        return callSiteID;
    }

    private CallSiteID() {
        this(null);
    }

    private CallSiteID(CallSite callSite) {
        this.callSiteOffset = 0;
        this.callSite       = callSite;
    }

    public int getCallSiteOffset() {
        return callSiteOffset;
    }

    public CallSite getCallSite() {
        return callSite;
    }

    @Override
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());
        callSiteOffset = input.readInt();
    }

    @Override
    protected void readLinkedDataItems(DexDataInput input) {
        input.setOffset(callSiteOffset);
        callSite = CallSite.readContent(input);
    }

    @Override
    protected void updateOffsets(DataItem.Map dataItemMap) {
        callSiteOffset = dataItemMap.getOffset(callSite);
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());
        output.writeInt(callSiteOffset);
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        if (callSite != null) {
            visitor.visitCallSite(dexFile, this, callSite);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallSiteID other = (CallSiteID) o;
        return Objects.equals(callSite, other.callSite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callSite);
    }

    @Override
    public String toString() {
        return String.format("CallSiteID[callSite=%s]", callSite);
    }
}

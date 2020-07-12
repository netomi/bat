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

/**
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_CALL_SITE_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class CallSiteID
implements   DataItem
{
    private int      callSiteOffset; // uint
    public  CallSite callSite;

    public CallSiteID() {
        callSiteOffset = 0;
        callSite       = null;
    }

    public int getCallSiteOffset() {
        return callSiteOffset;
    }

    @Override
    public void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());
        callSiteOffset = input.readInt();
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        input.setOffset(callSiteOffset);
        callSite = new CallSite();
        callSite.read(input);
    }

    @Override
    public void updateOffsets(DataItem.Map dataItemMap) {
        callSiteOffset = dataItemMap.getOffset(callSite);
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());
        output.writeInt(callSiteOffset);
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        if (callSite != null) {
            visitor.visitCallSite(dexFile, this, callSite);
        }
    }
}

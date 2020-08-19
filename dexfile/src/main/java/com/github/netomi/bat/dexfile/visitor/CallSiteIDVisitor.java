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
package com.github.netomi.bat.dexfile.visitor;

import com.github.netomi.bat.dexfile.CallSiteID;
import com.github.netomi.bat.dexfile.DexFile;

import java.util.function.BiConsumer;

public interface CallSiteIDVisitor
{
    void visitCallSiteID(DexFile dexFile, int index, CallSiteID callSiteID);

    default CallSiteIDVisitor andThen(CallSiteIDVisitor... visitors) {
        return Multi.of(this, visitors);
    }

    default CallSiteIDVisitor joinedByCallSiteConsumer(BiConsumer<DexFile, CallSiteID> consumer) {
        CallSiteIDVisitor joiner = new CallSiteIDVisitor() {
            private boolean firstVisited = false;
            @Override
            public void visitCallSiteID(DexFile dexFile, int index, CallSiteID callSiteID) {
                if (firstVisited) {
                    consumer.accept(dexFile, callSiteID);
                } else {
                    firstVisited = true;
                }
            }
        };

        return Multi.of(joiner, this);
    }

    class      Multi
    extends    AbstractMultiVisitor<CallSiteIDVisitor>
    implements CallSiteIDVisitor
    {
        public static CallSiteIDVisitor of(CallSiteIDVisitor visitor, CallSiteIDVisitor... visitors) {
            return new Multi(visitor, visitors);
        }

        private Multi(CallSiteIDVisitor visitor, CallSiteIDVisitor... otherVisitors) {
            super(visitor, otherVisitors);
        }

        @Override
        public void visitCallSiteID(DexFile dexFile, int index, CallSiteID callSiteID) {
            for (CallSiteIDVisitor visitor : visitors()) {
                visitor.visitCallSiteID(dexFile, index, callSiteID);
            }
        }
    }
}

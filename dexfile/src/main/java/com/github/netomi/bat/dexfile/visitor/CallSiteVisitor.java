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

import com.github.netomi.bat.dexfile.CallSite;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.MethodHandle;

import java.util.function.BiConsumer;

public interface CallSiteVisitor
{
    void visitCallSite(DexFile dexFile, int index, CallSite callSite);

    default CallSiteVisitor andThen(CallSiteVisitor... visitors) {
        return Multi.of(this, visitors);
    }

    default CallSiteVisitor joinedByCallSiteConsumer(BiConsumer<DexFile, CallSite> consumer) {
        CallSiteVisitor joiner = new CallSiteVisitor() {
            private boolean firstVisited = false;
            @Override
            public void visitCallSite(DexFile dexFile, int index, CallSite callSite) {
                if (firstVisited) {
                    consumer.accept(dexFile, callSite);
                } else {
                    firstVisited = true;
                }
            }
        };

        return Multi.of(joiner, this);
    }

    class      Multi
    extends    AbstractMultiVisitor<CallSiteVisitor>
    implements CallSiteVisitor
    {
        public static CallSiteVisitor of(CallSiteVisitor visitor, CallSiteVisitor... visitors) {
            return new Multi(visitor, visitors);
        }

        private Multi(CallSiteVisitor visitor, CallSiteVisitor... otherVisitors) {
            super(visitor, otherVisitors);
        }

        @Override
        public void visitCallSite(DexFile dexFile, int index, CallSite callSite) {
            for (CallSiteVisitor visitor : visitors()) {
                visitor.visitCallSite(dexFile, index, callSite);
            }
        }
    }
}

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

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.MethodHandle;

import java.util.function.BiConsumer;

public interface MethodHandleVisitor
{
    void visitMethodHandle(DexFile dexFile, int index, MethodHandle methodHandle);

    default MethodHandleVisitor andThen(MethodHandleVisitor... visitors) {
        return Multi.of(this, visitors);
    }

    default MethodHandleVisitor joinedByMethodHandleConsumer(BiConsumer<DexFile, MethodHandle> consumer) {
        MethodHandleVisitor joiner = new MethodHandleVisitor() {
            private boolean firstVisited = false;
            @Override
            public void visitMethodHandle(DexFile dexFile, int index, MethodHandle methodHandle) {
                if (firstVisited) {
                    consumer.accept(dexFile, methodHandle);
                } else {
                    firstVisited = true;
                }
            }
        };

        return Multi.of(joiner, this);
    }

    class      Multi
    extends    AbstractMultiVisitor<MethodHandleVisitor>
    implements MethodHandleVisitor
    {
        public static MethodHandleVisitor of(MethodHandleVisitor visitor, MethodHandleVisitor... visitors) {
            return new Multi(visitor, visitors);
        }

        private Multi(MethodHandleVisitor visitor, MethodHandleVisitor... otherVisitors) {
            super(visitor, otherVisitors);
        }

        @Override
        public void visitMethodHandle(DexFile dexFile, int index, MethodHandle methodHandle) {
            for (MethodHandleVisitor visitor : visitors()) {
                visitor.visitMethodHandle(dexFile, index, methodHandle);
            }
        }
    }
}

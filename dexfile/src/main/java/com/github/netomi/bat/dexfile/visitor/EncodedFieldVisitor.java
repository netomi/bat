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

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.EncodedField;

import java.util.function.BiConsumer;

public interface EncodedFieldVisitor
{
    void visitAnyField(DexFile dexFile, ClassDef classDef, int index, EncodedField field);

    default void visitStaticField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
        visitAnyField(dexFile, classDef, index, field);
    }

    default void visitInstanceField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
        visitAnyField(dexFile, classDef, index, field);
    }

    default EncodedFieldVisitor andThen(EncodedFieldVisitor... visitors) {
        return Multi.of(this, visitors);
    }

    default EncodedFieldVisitor joinedByFieldConsumer(BiConsumer<DexFile, EncodedField> consumer) {
        EncodedFieldVisitor joiner = new EncodedFieldVisitor() {
            private boolean firstVisited = false;
            @Override
            public void visitAnyField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
                if (firstVisited) {
                    consumer.accept(dexFile, field);
                } else {
                    firstVisited = true;
                }
            }
        };

        return Multi.of(joiner, this);
    }

    class      Multi
    extends    AbstractMultiVisitor<EncodedFieldVisitor>
    implements EncodedFieldVisitor
    {
        public static EncodedFieldVisitor of(EncodedFieldVisitor visitor, EncodedFieldVisitor... visitors) {
            return new Multi(visitor, visitors);
        }

        private Multi(EncodedFieldVisitor visitor, EncodedFieldVisitor... otherVisitors) {
            super(visitor, otherVisitors);
        }

        @Override
        public void visitAnyField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
            for (EncodedFieldVisitor visitor : visitors()) {
                visitor.visitAnyField(dexFile, classDef, index, field);
            }
        }

        @Override
        public void visitStaticField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
            for (EncodedFieldVisitor visitor : visitors()) {
                visitor.visitStaticField(dexFile, classDef, index, field);
            }
        }

        @Override
        public void visitInstanceField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
            for (EncodedFieldVisitor visitor : visitors()) {
                visitor.visitInstanceField(dexFile, classDef, index, field);
            }
        }
    }
}

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

public interface EncodedFieldVisitor
{
    void visitAnyField(DexFile dexFile, ClassDef classDef, int index, EncodedField field);

    default void visitStaticField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
        visitAnyField(dexFile, classDef, index, field);
    }

    default void visitInstanceField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
        visitAnyField(dexFile, classDef, index, field);
    }

    static EncodedFieldVisitor concatenate(EncodedFieldVisitor... visitors) {
        return new EncodedFieldVisitor() {
            @Override
            public void visitAnyField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
                for (EncodedFieldVisitor visitor : visitors) {
                    visitor.visitAnyField(dexFile, classDef, index, field);
                }
            }

            @Override
            public void visitStaticField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
                for (EncodedFieldVisitor visitor : visitors) {
                    visitor.visitStaticField(dexFile, classDef, index, field);
                }
            }

            @Override
            public void visitInstanceField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
                for (EncodedFieldVisitor visitor : visitors) {
                    visitor.visitInstanceField(dexFile, classDef, index, field);
                }
            }
        };
    }
}

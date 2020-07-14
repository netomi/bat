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

import com.github.netomi.bat.dexfile.*;

public interface EncodedMethodVisitor {
    default void visitAnyMethod(DexFile dexFile, ClassDef classDef, int index, EncodedMethod method) {
        throw new RuntimeException("Need to implement in class '" + this.getClass().getName() + "'.");
    }

    default void visitDirectMethod(DexFile dexFile, ClassDef classDef, int index, EncodedMethod method) {
        visitAnyMethod(dexFile, classDef, index, method);
    }

    default void visitVirtualMethod(DexFile dexFile, ClassDef classDef, int index, EncodedMethod method) {
        visitAnyMethod(dexFile, classDef, index, method);
    }
}

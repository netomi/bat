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
import com.github.netomi.bat.dexfile.value.*;

public interface EncodedValueVisitor
{
    default void visitAnyValue(DexFile dexFile, EncodedValue value) {
        throw new RuntimeException("Need to implement in class '" + this.getClass().getName() + "'.");
    }

    default void visitAnnotationValue(DexFile dexFile, EncodedAnnotationValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitArrayValue(DexFile dexFile, EncodedArrayValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitBooleanValue(DexFile dexFile, EncodedBooleanValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitByteValue(DexFile dexFile, EncodedByteValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitCharValue(DexFile dexFile, EncodedCharValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitDoubleValue(DexFile dexFile, EncodedDoubleValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitEnumValue(DexFile dexFile, EncodedEnumValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitFieldValue(DexFile dexFile, EncodedFieldValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitFloatValue(DexFile dexFile, EncodedFloatValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitIntValue(DexFile dexFile, EncodedIntValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitLongValue(DexFile dexFile, EncodedLongValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitMethodHandleValue(DexFile dexFile, EncodedMethodHandleValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitMethodTypeValue(DexFile dexFile, EncodedMethodTypeValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitMethodValue(DexFile dexFile, EncodedMethodValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitNullValue(DexFile dexFile, EncodedNullValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitShortValue(DexFile dexFile, EncodedShortValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitStringValue(DexFile dexFile, EncodedStringValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitTypeValue(DexFile dexFile, EncodedTypeValue value) {
        visitAnyValue(dexFile, value);
    }
}

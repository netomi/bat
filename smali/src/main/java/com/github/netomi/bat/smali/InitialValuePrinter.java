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
package com.github.netomi.bat.smali;

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.value.*;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;

import java.io.IOException;

class InitialValuePrinter
implements EncodedValueVisitor
{
    private final Appendable appendable;
    private final String     prefix;

    public InitialValuePrinter(Appendable appendable, String prefix) {
        this.appendable = appendable;
        this.prefix     = prefix;
    }

    @Override
    public void visitAnyValue(DexFile dexFile, EncodedValue value) {
        throw new RuntimeException("unexpected value " + value);
    }

    @Override
    public void visitBooleanValue(DexFile dexFile, EncodedBooleanValue value) {
        append(Boolean.toString(value.getValue()));
    }

    @Override
    public void visitByteValue(DexFile dexFile, EncodedByteValue value) {
        append(Byte.toString(value.getValue()));
    }

    @Override
    public void visitCharValue(DexFile dexFile, EncodedCharValue value) {
        append(Character.toString(value.getValue()));
    }

    @Override
    public void visitDoubleValue(DexFile dexFile, EncodedDoubleValue value) {
        append(Double.toString(value.getValue()));
    }

    @Override
    public void visitEnumValue(DexFile dexFile, EncodedEnumValue value) {
        append(value.getEnumField(dexFile).getName(dexFile));
    }

    @Override
    public void visitFloatValue(DexFile dexFile, EncodedFloatValue value) {
        append(Float.toString(value.getValue()));
    }

    @Override
    public void visitIntValue(DexFile dexFile, EncodedIntValue value) {
        append(Integer.toString(value.getValue()));
    }

    @Override
    public void visitLongValue(DexFile dexFile, EncodedLongValue value) {
        append(Long.toString(value.getValue()));
    }

    @Override
    public void visitNullValue(DexFile dexFile, EncodedNullValue value) {
        append("null");
    }

    @Override
    public void visitShortValue(DexFile dexFile, EncodedShortValue value) {
        append(Short.toString(value.getValue()));
    }

    @Override
    public void visitStringValue(DexFile dexFile, EncodedStringValue value) {
        append("\"" + value.getString(dexFile) + "\"");
    }

    // private utility methods.

    private void append(String text) {
        try {
            appendable.append(prefix + text);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}

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
package com.github.netomi.bat.smali.disassemble;

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.FieldID;
import com.github.netomi.bat.dexfile.MethodID;
import com.github.netomi.bat.dexfile.value.*;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;
import com.github.netomi.bat.io.IndentingPrinter;
import com.github.netomi.bat.util.Strings;

class      CallSiteArgumentPrinter
implements EncodedValueVisitor
{
    private final IndentingPrinter printer;

    public CallSiteArgumentPrinter(IndentingPrinter printer) {
        this.printer = printer;
    }

    @Override
    public void visitAnyValue(DexFile dexFile, EncodedValue value) {
        throw new RuntimeException("unexpected value " + value);
    }

    @Override
    public void visitTypeValue(DexFile dexFile, EncodedTypeValue value) {
        append(value.getType(dexFile));
    }

    @Override
    public void visitFieldValue(DexFile dexFile, EncodedFieldValue value) {
        FieldID fieldID = value.getField(dexFile);
        append(fieldID.getClassType(dexFile) + "->" +
               fieldID.getName(dexFile) + ":" +
               fieldID.getType(dexFile));
    }

    @Override
    public void visitMethodValue(DexFile dexFile, EncodedMethodValue value) {
        MethodID methodID = value.getMethod(dexFile);
        append(methodID.getClassTypeID(dexFile).getType(dexFile) + "->" +
               methodID.getName(dexFile) +
               methodID.getProtoID(dexFile).getDescriptor(dexFile));
    }

    @Override
    public void visitMethodHandleValue(DexFile dexFile, EncodedMethodHandleValue value) {
        append(value.getMethodHandle(dexFile).getTargetClassType(dexFile));
    }

    @Override
    public void visitMethodTypeValue(DexFile dexFile, EncodedMethodTypeValue value) {
        append(value.getProtoID(dexFile).getDescriptor(dexFile));
    }

    @Override
    public void visitBooleanValue(DexFile dexFile, EncodedBooleanValue value) {
        append(Boolean.toString(value.getValue()));
    }

    @Override
    public void visitByteValue(DexFile dexFile, EncodedByteValue value) {
        int v = value.getValue();
        if (v < 0) {
            append(String.format("-0x%xt", -v));
        } else {
            append(String.format("0x%xt", v));
        }
    }

    @Override
    public void visitShortValue(DexFile dexFile, EncodedShortValue value) {
        int v = value.getValue();
        if (v < 0) {
            append(String.format("-0x%xs", -v));
        } else {
            append(String.format("0x%xs", v));
        }
    }

    @Override
    public void visitCharValue(DexFile dexFile, EncodedCharValue value) {
        append("'" + Strings.escapeChar(value.getValue()) + "'");
    }

    @Override
    public void visitDoubleValue(DexFile dexFile, EncodedDoubleValue value) {
        append(Double.toString(value.getValue()));
    }

    @Override
    public void visitEnumValue(DexFile dexFile, EncodedEnumValue value) {
        FieldID fieldID = value.getEnumField(dexFile);
        append(".enum " +
               fieldID.getClassType(dexFile) + "->" +
               fieldID.getName(dexFile) + ":" +
               fieldID.getType(dexFile));
    }

    @Override
    public void visitFloatValue(DexFile dexFile, EncodedFloatValue value) {
        append(value.getValue() + "f");
    }

    @Override
    public void visitIntValue(DexFile dexFile, EncodedIntValue value) {
        int v = value.getValue();
        if (v < 0) {
            append(String.format("-0x%x", -v));
        } else {
            append(String.format("0x%x", v));
        }
    }

    @Override
    public void visitLongValue(DexFile dexFile, EncodedLongValue value) {
        long v = value.getValue();
        if (v < 0) {
            append(String.format("-0x%xL", -v));
        } else {
            append(String.format("0x%xL", v));
        }
    }

    @Override
    public void visitNullValue(DexFile dexFile, EncodedNullValue value) {
        append("null");
    }

    @Override
    public void visitStringValue(DexFile dexFile, EncodedStringValue value) {
        append("\"" + Strings.escapeString(value.getString(dexFile)) + "\"");
    }

    // private utility methods.

    private void append(String text) {
        printer.print(text);
    }
}

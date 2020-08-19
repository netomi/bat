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
package com.github.netomi.bat.dexdump;

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.value.*;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;

class      CallSiteArgumentPrinter
implements EncodedValueVisitor
{
    private final PrintfFormat FLOATING_FORMAT = new PrintfFormat("%g");

    private final BufferedPrinter printer;

    public CallSiteArgumentPrinter(BufferedPrinter printer) {
        this.printer = printer;
    }

    @Override
    public void visitAnyValue(DexFile dexFile, EncodedValue value) {}

    @Override
    public void visitBooleanValue(DexFile dexFile, EncodedBooleanValue value) {
        printer.print(value.getValue() + " (boolean)");
    }

    @Override
    public void visitByteValue(DexFile dexFile, EncodedByteValue value) {
        printer.print(value.getValue() + " (byte)");
    }

    @Override
    public void visitCharValue(DexFile dexFile, EncodedCharValue value) {
        printer.print(value.getValue() + " (char)");
    }

    @Override
    public void visitDoubleValue(DexFile dexFile, EncodedDoubleValue value) {
        printer.print(FLOATING_FORMAT.sprintf(value.getValue()) + " (double)");
    }

    @Override
    public void visitEnumValue(DexFile dexFile, EncodedEnumValue value) {
        printer.print(value.getFieldIndex() + " (Enum)");
    }

    @Override
    public void visitFieldValue(DexFile dexFile, EncodedFieldValue value) {
        printer.print(value.getFieldIndex() + " (Field)");
    }

    @Override
    public void visitFloatValue(DexFile dexFile, EncodedFloatValue value) {
        printer.print(FLOATING_FORMAT.sprintf(value.getValue()) + " (float)");
    }

    @Override
    public void visitIntValue(DexFile dexFile, EncodedIntValue value) {
        printer.print(value.getValue() + " (int)");
    }

    @Override
    public void visitLongValue(DexFile dexFile, EncodedLongValue value) {
        printer.print(value.getValue() + " (long)");
    }

    @Override
    public void visitMethodHandleValue(DexFile dexFile, EncodedMethodHandleValue value) {
        printer.print(value.getHandleIndex() + " (MethodHandle)");
    }

    @Override
    public void visitMethodTypeValue(DexFile dexFile, EncodedMethodTypeValue value) {
        printer.print(value.getProtoID(dexFile).getDescriptor(dexFile) + " (MethodType)");
    }

    @Override
    public void visitMethodValue(DexFile dexFile, EncodedMethodValue value) {
        printer.print(value.getMethodIndex() + " (Method)");
    }

    @Override
    public void visitNullValue(DexFile dexFile, EncodedNullValue value) {
        printer.print("null");
    }

    @Override
    public void visitShortValue(DexFile dexFile, EncodedShortValue value) {
        printer.print(value.getValue() + " (short)");
    }

    @Override
    public void visitStringValue(DexFile dexFile, EncodedStringValue value) {
        printer.print(value.getString(dexFile) + " (String)");
    }

    @Override
    public void visitTypeValue(DexFile dexFile, EncodedTypeValue value) {
        printer.print(value.getType(dexFile) + " (Class)");
    }
}

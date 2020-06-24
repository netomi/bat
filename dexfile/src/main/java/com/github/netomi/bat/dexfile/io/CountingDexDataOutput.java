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

package com.github.netomi.bat.dexfile.io;

import java.nio.ByteOrder;

/**
 * @author Thomas Neidhart
 */
public class CountingDexDataOutput
implements   DexDataOutput
{
    private int offset = 0;

    @Override
    public ByteOrder order() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    public void order(ByteOrder byteOrder) {}

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public void writePadding(int bytes) {
        offset += bytes;
    }

    @Override
    public void writeAlignmentPadding(int alignment) {
        int currentAlignment = getOffset() % alignment;
        int padding = (alignment - currentAlignment) % alignment;
        offset += padding;
    }

    @Override
    public void writeBytes(byte[] bytes) {
        offset += bytes.length;
    }

    @Override
    public void writeByte(byte b) {
        offset++;
    }

    @Override
    public void writeUnsignedByte(short b) {
        offset++;
    }

    @Override
    public void writeShort(short value) {
        offset += 2;
    }

    @Override
    public void writeShort(short value, int bytes) {
        offset += bytes;
    }

    @Override
    public void writeUnsignedShort(int value) {
        offset += 2;
    }

    @Override
    public void writeChar(char value) {
        offset += 2;
    }

    @Override
    public void writeChar(char value, int bytes) {
        offset += bytes;
    }

    @Override
    public void writeInt(int value) {
        offset += 4;
    }

    @Override
    public void writeInt(int value, int bytes) {
        offset += bytes;
    }

    @Override
    public void writeUnsignedInt(long value) {
        offset += 4;
    }

    @Override
    public void writeUnsignedInt(int value, int bytes) {
        offset += bytes;
    }

    @Override
    public void writeLong(long value) {
        offset += 8;
    }

    @Override
    public void writeLong(long value, int bytes) {
        offset += bytes;
    }

    @Override
    public void writeFloat(float value) {
        offset += 4;
    }

    @Override
    public void writeFloat(float value, int bytes) {
        offset += bytes;
    }

    @Override
    public void writeDouble(double value) {
        offset += 8;
    }

    @Override
    public void writeDouble(double value, int bytes) {
        offset += bytes;
    }

    @Override
    public void writeUleb128(int value) {
        offset += 2;
    }

    @Override
    public void writeUleb128p1(int value) {
        offset += 2;
    }

    @Override
    public void writeSleb128(int value) {
        offset += 2;
    }

    @Override
    public void setLastMemberIndex(int index) {}

    @Override
    public int getLastMemberIndex() {
        return 0;
    }
}

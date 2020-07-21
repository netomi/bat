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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * @author Thomas Neidhart
 */
public class ByteBufferBackedDexDataOutput
implements   DexDataOutput
{
    private ByteBuffer byteBuffer;
    private int lastMemberIndex;

    public ByteBufferBackedDexDataOutput(int bytes) {
        byteBuffer = ByteBuffer.allocate(bytes);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public ByteOrder order() {
        return byteBuffer.order();
    }

    @Override
    public void order(ByteOrder byteOrder) {
        byteBuffer.order(byteOrder);
    }

    @Override
    public int getOffset() {
        return byteBuffer.position();
    }

    @Override
    public void setOffset(int offset) {
        byteBuffer.position(offset);
    }

    @Override
    public void writePadding(int bytes) {
        for (int i = 0; i < bytes; i++) {
            writeByte((byte) 0);
        }
    }

    @Override
    public void writeAlignmentPadding(int alignment) {
        int currentAlignment = getOffset() % alignment;
        int padding = (alignment - currentAlignment) % alignment;
        for (int i = 0; i < padding; i++) {
            byteBuffer.put((byte) 0x0);
        }
    }

    @Override
    public void writeBytes(byte[] bytes) {
        byteBuffer.put(bytes);
    }

    @Override
    public void writeByte(byte b) {
        byteBuffer.put(b);
    }

    @Override
    public void writeUnsignedByte(short b) {
    }

    @Override
    public void writeShort(short value) {
        byteBuffer.putShort(value);
    }

    @Override
    public void writeShort(short value, int bytes) {
        writePadding(bytes);
    }

    @Override
    public void writeUnsignedShort(int value) {
        byteBuffer.putShort((short) value);
    }

    @Override
    public void writeChar(char value) {
        byteBuffer.putChar(value);
    }

    @Override
    public void writeChar(char value, int bytes) {
        writePadding(bytes);
    }

    @Override
    public void writeInt(int value) {
        byteBuffer.putInt(value);
    }

    @Override
    public void writeInt(int value, int bytes) {
        writePadding(bytes);
    }

    @Override
    public void writeUnsignedInt(long value) {
        byteBuffer.putInt((int) value);
    }

    @Override
    public void writeUnsignedInt(int value, int bytes) {
        writePadding(bytes);
    }

    @Override
    public void writeLong(long value) {
        byteBuffer.putLong(value);
    }

    @Override
    public void writeLong(long value, int bytes) {
        writePadding(bytes);
    }

    @Override
    public void writeFloat(float value) {
        byteBuffer.putFloat(value);
    }

    @Override
    public void writeFloat(float value, int bytes) {
        writePadding(bytes);
    }

    @Override
    public void writeDouble(double value) {
        byteBuffer.putDouble(value);
    }

    @Override
    public void writeDouble(double value, int bytes) {
        writePadding(bytes);
    }

    @Override
    public void writeUleb128(int value) {
        do {
            byte b = (byte) (value & 0x7f);
            value >>>= 7;
            if (value != 0) {
                b |= 0x80;
            }
            byteBuffer.put(b);
        } while (value != 0);
    }

    @Override
    public void writeUleb128p1(int value) {
        writeUleb128(value + 1);
    }

    @Override
    public void writeSleb128(int value) {
        while (true) {
            byte b = (byte) (value & 0x7f);
            value >>= 7;
            if (value ==  0 && ((b & 0x40) == 0) ||
                value == -1 && ((b & 0x40) != 0)) {
                byteBuffer.put(b);
                break;
            }

            byteBuffer.put((byte) (b | 0x80));
        }
    }

    public void copy(OutputStream output) throws IOException {
        byteBuffer.position(0);
        WritableByteChannel channel = Channels.newChannel(output);
        channel.write(byteBuffer);
    }
}

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.Checksum;

public class DexDataInput
{
    private final ByteBuffer byteBuffer;
    private int lastMemberIndex;

    public DexDataInput(InputStream inputStream) throws IOException {
        byteBuffer = toByteBuffer(inputStream);
    }

    public void order(ByteOrder byteOrder) {
        byteBuffer.order(byteOrder);
    }

    public int getOffset() {
        return byteBuffer.position();
    }

    public void setOffset(int offset) {
        byteBuffer.position(offset);
    }

    public void skipBytes(int byteToSkip) {
        byteBuffer.position(byteBuffer.position() + byteToSkip);
    }

    public void skipAlignmentPadding(int alignment) {
        if (alignment > 1) {
            int currentAligment = getOffset() % alignment;
            int padding = (alignment - currentAligment) % alignment;
            skipBytes(padding);
        }
    }

    public void readFully(byte[] bytes) {
        byteBuffer.get(bytes);
    }

    public byte readByte() {
        return byteBuffer.get();
    }

    public short readUnsignedByte() {
        return (short) (byteBuffer.get() & 0xFF);
    }

    public short readShort() {
        return byteBuffer.getShort();
    }

    public short readShort(int bytes) {
        short result = 0;
        for (int i = 0; i < bytes; i++) {
            int b = readUnsignedByte();
            result |= b << (8*i);

            // sign-extend
            if ((b & 0x80) != 0) {
                result |= 0x8000;
            }
        }
        return result;
    }

    public int readUnsignedShort() {
        return byteBuffer.getShort() & 0xFFFF;
    }

    public char readChar() {
        return byteBuffer.getChar();
    }

    public char readChar(int bytes) {
        char result = 0;
        for (int i = 0; i < bytes; i++) {
            result |= readUnsignedByte() << (8*i);
        }
        return result;
    }

    public int readInt() {
        if (getOffset() == 0x32a03c)
            System.out.println(getOffset());

        return byteBuffer.getInt();
    }

    public int readInt(int bytes) {
        int result = 0;
        for (int i = 0; i < bytes; i++) {
            int b = readUnsignedByte();
            result |= b << (8*i);

            // sign-extend
            if ((b & 0x80) != 0) {
                result |= 0x80000000;
            }
        }
        return result;
    }

    public long readUnsignedInt() {
        return byteBuffer.getInt() & 0xFFFFFFFFL;
    }

    public int readUnsignedInt(int bytes) {
        int result = 0;
        for (int i = 0; i < bytes; i++) {
            result |= readUnsignedByte() << (8*i);
        }
        return result;
    }

    public long readLong() {
        return byteBuffer.getLong();
    }

    public long readLong(int bytes) {
        long result = 0;
        for (int i = 0; i < bytes; i++) {
            int b = readUnsignedByte();
            result |= ((long) b) << (8*i);

            // sign-extend
            if ((b & 0x80) != 0) {
                result |= 0x8000000000000000l;
            }
        }
        return result;
    }

    public float readFloat() {
        return byteBuffer.getFloat();
    }

    public float readFloat(int bytes) {
        int value = 0;

        for (int i = 0; i < bytes; i++)
        {
            int b = readUnsignedByte();

            // Prepend the byte.
            value = (value >>> 8) | (b << 24);
        }

        return Float.intBitsToFloat(value);
    }

    public double readDouble() {
        return byteBuffer.getDouble();
    }

    public double readDouble(int bytes) {
        long value = 0;

        for (int i = 0; i < bytes; i++)
        {
            long b = readUnsignedByte();

            // Prepend the byte.
            value = (value >>> 8) | (b << 56);
        }

        return Double.longBitsToDouble(value);
    }

    public int readUleb128() {
        int b;

        int value  = 0;
        int length = 0;
        do {
            b = readUnsignedByte();
            value |= ((b & 0x7f) << (length * 7));
            length++;
        } while ((b & 0x80) != 0);

        return value;
    }

    public int readUleb128p1() {
        return readUleb128() - 1;
    }

    public int readSleb128() {
        int value = 0;
        int shift = 0;
        byte b;

        do {
            b = byteBuffer.get();
            value |= (b & (byte) 0x7f) << shift;
            shift += 7;
        } while ((b & ~(byte) 0x7f) == ~(byte) 0x7f);
        int mask = 1 << (shift - 1);
        return (value ^ mask) - mask;
    }

    public byte[] readMUTF8Bytes(int len) {
        byte buf[] = new byte[len * 3];
        int readBytes = 0;
        while (readBytes < buf.length) {
            byte b = readByte();
            if (b != 0x00) {
                buf[readBytes++] = b;
            } else {
                break;
            }
        }
        return Arrays.copyOf(buf, readBytes);
    }

    public void update(Checksum checksum) {
        while (byteBuffer.hasRemaining()) {
            checksum.update(byteBuffer.get());
        }
    }

    // HACK

    public void setLastMemberIndex(int index) {
        lastMemberIndex = index;
    }

    public int getLastMemberIndex() {
        return lastMemberIndex;
    }

    // Private utility methods.

    private static ByteBuffer toByteBuffer(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int read;
        byte[] buf = new byte[1024];
        while ((read = inputStream.read(buf, 0, buf.length)) != -1) {
            baos.write(buf, 0, read);
        }

        return ByteBuffer.wrap(baos.toByteArray());
    }

}

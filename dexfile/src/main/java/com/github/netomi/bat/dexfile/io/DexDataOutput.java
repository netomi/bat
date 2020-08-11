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

public interface DexDataOutput
{
    ByteOrder order();

    void order(ByteOrder byteOrder);

    int getOffset();

    void setOffset(int offset);

    void writePadding(int bytes);

    void writeAlignmentPadding(int alignment);

    void writeBytes(byte[] bytes);

    void writeByte(byte b);

    void writeUnsignedByte(short b);

    void writeShort(short value);

    void writeShort(short value, int bytes);

    void writeUnsignedShort(int value);

    void writeChar(char value, int bytes);

    void writeInt(int value);

    void writeInt(int value, int bytes);

    void writeUnsignedInt(long value);

    void writeLong(long value, int bytes);

    void writeFloat(float value, int bytes);

    void writeDouble(double value, int bytes);

    void writeUleb128(int value);

    void writeUleb128p1(int value);

    void writeSleb128(int value);
}

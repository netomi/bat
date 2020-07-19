/*
 * Copyright (c) 2018 Thomas Neidhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.netomi.bat.dexfile.util;

public final class Primitives
{
    private Primitives() {}

    private static final String[][] JAVA_CTRL_CHARS_ESCAPE = {
            {"\b", "\\b"},
            {"\n", "\\n"},
            {"\t", "\\t"},
            {"\f", "\\f"},
            {"\r", "\\r"},
            {"\0", "\\0"}
    };

    public static String toAsciiString(byte[] arr) {
        StringBuilder sb = new StringBuilder(arr.length);
        for (byte b : arr) {
            boolean replaced = false;
            for (String[] replacement : JAVA_CTRL_CHARS_ESCAPE) {
                if (replacement[0].equals(String.valueOf((char) b))) {
                    sb.append(replacement[1]);
                    replaced = true;
                }
            }
            if (!replaced) {
                sb.append((char) b);
            }
        }
        return sb.toString();
    }

    public static String toHexString(byte[] arr) {
        StringBuilder sb = new StringBuilder(arr.length * 4 + 2);
        sb.append('[');
        for (byte b : arr) {
            sb.append(toHexString(b));
        }
        sb.setLength(sb.length() - 2);
        sb.append(']');
        return sb.toString();
    }

    public static String toHexString(byte value) {
        return "0x" + asHexValue(value);
    }

    public static String toHexString(short value) {
        return "0x" + asHexValue(value);
    }

    public static String toHexString(int value) {
        return "0x" + asHexValue(value, 8);
    }

    public static String toHexString(long value) {
        return String.format("0x%08x", value);
    }

    public static String asHexValue(byte value) {
        return asHexValue(value & 0xff, 2);
    }

    public static String asHexValue(short value) {
        return asHexValue(value & 0xffff, 4);
    }

    public static String asHexValue(int value, int digits) {
        StringBuilder sb = new StringBuilder();

        String hexValue = Integer.toHexString(value);
        int leadingZeros = digits - hexValue.length();
        for (int i = 0; i < leadingZeros; i++) {
            sb.append('0');
        }
        sb.append(hexValue);
        return sb.toString();
    }
}

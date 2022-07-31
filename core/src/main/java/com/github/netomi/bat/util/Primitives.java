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
package com.github.netomi.bat.util;

public final class Primitives
{
    private Primitives() {}

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
        sb.append("0".repeat(Math.max(0, leadingZeros)));
        sb.append(hexValue);
        return sb.toString();
    }

    public static String asSignedHexValue(int value, int digits) {
        StringBuilder sb = new StringBuilder();

        String hexValue;

        if (value < 0) {
            hexValue = Integer.toHexString(-value);
        } else {
            hexValue = Integer.toHexString(value);
        }

        int leadingZeros = digits - hexValue.length();
        sb.append("0".repeat(Math.max(0, leadingZeros)));
        sb.append(hexValue);

        sb.insert(0, (value < 0 ? "-" : "+"));
        return sb.toString();
    }

    public static String asHexValue(long value, int digits) {
        StringBuilder sb = new StringBuilder();

        String hexValue = Long.toHexString(value);
        int leadingZeros = digits - hexValue.length();
        sb.append("0".repeat(Math.max(0, leadingZeros)));
        sb.append(hexValue);
        return sb.toString();
    }
}

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
package com.github.netomi.bat.dexfile;

import java.util.Arrays;

public enum DexFormat
{
    FORMAT_009(new byte[] { 0x30, 0x30, 0x39, 0x00 }, "009"),
    FORMAT_013(new byte[] { 0x30, 0x31, 0x33, 0x00 }, "013"),
    FORMAT_035(new byte[] { 0x30, 0x33, 0x35, 0x00 }, "035"),
    FORMAT_037(new byte[] { 0x30, 0x33, 0x37, 0x00 }, "037"),
    FORMAT_038(new byte[] { 0x30, 0x33, 0x38, 0x00 }, "038"),
    FORMAT_039(new byte[] { 0x30, 0x33, 0x39, 0x00 }, "039");

    private final byte[] pattern;
    private final String version;

    DexFormat(byte[] pattern, String version) {
        this.pattern = pattern;
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public static DexFormat fromPattern(byte[] pattern, int from, int to) {
        for (DexFormat format : values()) {
            if (Arrays.equals(format.pattern, Arrays.copyOfRange(pattern, from, to))) {
                return format;
            }
        }
        return null;
    }
}

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
package com.github.netomi.bat.dexfile

enum class DexFormat(private val bytePattern: ByteArray, val version: String, val apiLevel: Int) {

    FORMAT_009(byteArrayOf(0x30, 0x30, 0x39, 0x00), "009", -1),
    FORMAT_013(byteArrayOf(0x30, 0x31, 0x33, 0x00), "013", -1),
    FORMAT_035(byteArrayOf(0x30, 0x33, 0x35, 0x00), "035", 15),
    FORMAT_037(byteArrayOf(0x30, 0x33, 0x37, 0x00), "037", 24),
    FORMAT_038(byteArrayOf(0x30, 0x33, 0x38, 0x00), "038", 26),
    FORMAT_039(byteArrayOf(0x30, 0x33, 0x39, 0x00), "039", 28);

    val pattern: ByteArray = bytePattern.copyOf(bytePattern.size)

    companion object {
        fun forApiLevel(apiLevel: Int): DexFormat {
            for (format in values().reversed()) {
                if (apiLevel >= format.apiLevel) {
                    return format
                }
            }
            throw IllegalArgumentException("unexpected apiLevel $apiLevel")
        }

        fun fromPattern(bytes: ByteArray, from: Int, to: Int): DexFormat? {
            val pattern = bytes.copyOfRange(from, to)
            for (format in values()) {
                if (format.bytePattern.contentEquals(pattern)) {
                    return format
                }
            }
            return null
        }
    }
}
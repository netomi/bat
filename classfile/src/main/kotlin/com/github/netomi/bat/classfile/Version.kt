/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
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

package com.github.netomi.bat.classfile

enum class Version(val majorVersion: Int, val minorVersion: Int = 0) {
    JAVA_1_0(MAJOR_VERSION_1_0, 0),
    JAVA_1_1(MAJOR_VERSION_1_1, 3),
    JAVA_1_2(MAJOR_VERSION_1_2),
    JAVA_1_3(MAJOR_VERSION_1_3),
    JAVA_1_4(MAJOR_VERSION_1_4),
    JAVA_5  (MAJOR_VERSION_5_0),
    JAVA_6  (MAJOR_VERSION_6_0),
    JAVA_7  (MAJOR_VERSION_7_0),
    JAVA_8  (MAJOR_VERSION_8_0),
    JAVA_9  (MAJOR_VERSION_9_0),
    JAVA_10 (MAJOR_VERSION_10_0),
    JAVA_11 (MAJOR_VERSION_11_0),
    JAVA_12 (MAJOR_VERSION_12_0),
    JAVA_13 (MAJOR_VERSION_13_0),
    JAVA_14 (MAJOR_VERSION_14_0),
    JAVA_15 (MAJOR_VERSION_15_0),
    JAVA_16 (MAJOR_VERSION_16_0),
    JAVA_17 (MAJOR_VERSION_17_0);

    companion object {
        fun of(versionString: String): Version {
            val separatorIndex = versionString.indexOf('.')
            val (major, minor) = if (separatorIndex != -1) {
                Pair(versionString.substring(0, separatorIndex).toInt(), versionString.substring(separatorIndex + 1).toInt())
            } else {
                Pair(versionString.toInt(), 0)
            }

            return values().firstOrNull { it.majorVersion == major &&
                                          it.minorVersion == minor } ?: error("unknown version for string '$versionString'")
        }
    }
}

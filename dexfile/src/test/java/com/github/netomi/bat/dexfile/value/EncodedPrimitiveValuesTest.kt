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

package com.github.netomi.bat.dexfile.value

class EncodedIntValueTest : EncodedValueTest<EncodedIntValue>() {
    override val testInstance: Array<EncodedIntValue>
        get() = arrayOf(
            EncodedIntValue.of(0),
            EncodedIntValue.of(1),
            EncodedIntValue.of(-1),
            EncodedIntValue.of(127),
            EncodedIntValue.of(-127),
            EncodedIntValue.of(128),
            EncodedIntValue.of(-128),
            EncodedIntValue.of(Short.MIN_VALUE.toInt()),
            EncodedIntValue.of(Short.MAX_VALUE.toInt()),
            EncodedIntValue.of(Int.MIN_VALUE),
            EncodedIntValue.of(Int.MAX_VALUE)
        )
}

class EncodedLongValueTest : EncodedValueTest<EncodedLongValue>() {
    override val testInstance: Array<EncodedLongValue>
        get() = arrayOf(
            EncodedLongValue.of(0),
            EncodedLongValue.of(1),
            EncodedLongValue.of(-1),
            EncodedLongValue.of(127),
            EncodedLongValue.of(-127),
            EncodedLongValue.of(128),
            EncodedLongValue.of(-128),
            EncodedLongValue.of(Long.MIN_VALUE),
            EncodedLongValue.of(Long.MAX_VALUE)
        )
}

class EncodedShortValueTest : EncodedValueTest<EncodedShortValue>() {
    override val testInstance: Array<EncodedShortValue>
        get() = arrayOf(
            EncodedShortValue.of(0.toShort()),
            EncodedShortValue.of(1.toShort()),
            EncodedShortValue.of(((-1).toShort())),
            EncodedShortValue.of(127.toShort()),
            EncodedShortValue.of(((-127).toShort())),
            EncodedShortValue.of(128.toShort()),
            EncodedShortValue.of(((-128).toShort())),
            EncodedShortValue.of(Short.MIN_VALUE),
            EncodedShortValue.of(Short.MAX_VALUE)
        )
}

class EncodedByteValueTest : EncodedValueTest<EncodedByteValue>() {
    override val testInstance: Array<EncodedByteValue>
        get() = arrayOf(
            EncodedByteValue.of(0.toByte()),
            EncodedByteValue.of(1.toByte()),
            EncodedByteValue.of(((-1).toByte())),
            EncodedByteValue.of(127.toByte()),
            EncodedByteValue.of(((-127).toByte())),
            EncodedByteValue.of(128.toByte()),
            EncodedByteValue.of(((-128).toByte())),
            EncodedByteValue.of(Byte.MIN_VALUE),
            EncodedByteValue.of(Byte.MAX_VALUE)
        )
}

class EncodedDoubleValueTest : EncodedValueTest<EncodedDoubleValue>() {
    override val testInstance: Array<EncodedDoubleValue>
        get() = arrayOf(
            EncodedDoubleValue.of(0.0),
            EncodedDoubleValue.of(1.0),
            EncodedDoubleValue.of(-1.0),
            EncodedDoubleValue.of(0.1),
            EncodedDoubleValue.of(-0.1),
            EncodedDoubleValue.of(Double.MIN_VALUE),
            EncodedDoubleValue.of(Double.MAX_VALUE)
        )
}

class EncodedFloatValueTest : EncodedValueTest<EncodedFloatValue>() {
    override val testInstance: Array<EncodedFloatValue>
        get() = arrayOf(
            EncodedFloatValue.of(0f),
            EncodedFloatValue.of(1f),
            EncodedFloatValue.of(-1f),
            EncodedFloatValue.of(0.1f),
            EncodedFloatValue.of(-0.1f),
            EncodedFloatValue.of(Float.MIN_VALUE),
            EncodedFloatValue.of(Float.MAX_VALUE)
        )
}

class EncodedBooleanValueTest : EncodedValueTest<EncodedBooleanValue>() {
    override val testInstance: Array<EncodedBooleanValue>
        get() = arrayOf(
            EncodedBooleanValue.of(true),
            EncodedBooleanValue.of(false)
        )
}

class EncodedCharValueTest : EncodedValueTest<EncodedCharValue>() {
    override val testInstance: Array<EncodedCharValue>
        get() = arrayOf(
            EncodedCharValue.of('a'),
            EncodedCharValue.of('A'),
            EncodedCharValue.of(Character.MIN_VALUE),
            EncodedCharValue.of(Character.MAX_VALUE)
        )
}
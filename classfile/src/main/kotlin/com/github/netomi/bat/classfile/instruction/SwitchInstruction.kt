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

package com.github.netomi.bat.classfile.instruction

import com.github.netomi.bat.util.mutableListOfCapacity

abstract class SwitchInstruction
    protected constructor(              opCode:           JvmOpCode,
                          protected var _defaultOffset:   Int                          = 0,
                          protected var matchOffsetPairs: MutableList<MatchOffsetPair> = mutableListOfCapacity(0)
    ): JvmInstruction(opCode), Sequence<MatchOffsetPair> {

    val defaultOffset: Int
        get() = _defaultOffset

    val size: Int
        get() = matchOffsetPairs.size

    operator fun get(index: Int): MatchOffsetPair {
        return matchOffsetPairs[index]
    }

    override fun iterator(): Iterator<MatchOffsetPair> {
        return matchOffsetPairs.iterator()
    }

    protected fun getPadding(offset: Int): Int {
        return (4 - (offset % 4)) % 4
    }
}

data class MatchOffsetPair(val match: Int, val offset: Int)
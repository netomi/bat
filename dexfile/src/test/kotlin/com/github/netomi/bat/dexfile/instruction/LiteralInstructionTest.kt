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

package com.github.netomi.bat.dexfile.instruction

class LiteralInstructionTest: DexInstructionTest<LiteralInstruction>() {

    override val testInstances: Array<LiteralInstruction>
        get() = arrayOf(
            // const/4
            LiteralInstruction.of(DexOpCode.CONST_4,  0x0, 0),
            LiteralInstruction.of(DexOpCode.CONST_4,  0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST_4, -0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST_4,  0x7, 0),
            LiteralInstruction.of(DexOpCode.CONST_4, -0x8, 0),

            // const/16
            LiteralInstruction.of(DexOpCode.CONST_16,  0x0, 0),
            LiteralInstruction.of(DexOpCode.CONST_16,  0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST_16, -0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST_16,  0x7fff, 0),
            LiteralInstruction.of(DexOpCode.CONST_16, -0x8000, 0),

            // const
            LiteralInstruction.of(DexOpCode.CONST,  0x0, 0),
            LiteralInstruction.of(DexOpCode.CONST,  0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST, -0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST,  0x7fffffff, 0),
            LiteralInstruction.of(DexOpCode.CONST, -0x80000000, 0),

            // const/high16
            LiteralInstruction.of(DexOpCode.CONST_HIGH16,  0x0, 0),
            LiteralInstruction.of(DexOpCode.CONST_HIGH16,  0x00010000, 0),
            LiteralInstruction.of(DexOpCode.CONST_HIGH16,  0x7fff0000, 0),
            LiteralInstruction.of(DexOpCode.CONST_HIGH16, -0x00010000, 0),
            LiteralInstruction.of(DexOpCode.CONST_HIGH16, -0x80000000, 0),

            // const-wide/16
            LiteralInstruction.of(DexOpCode.CONST_WIDE_16,  0x0, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_16,  0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_16, -0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_16,  0x7fff, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_16, -0x8000, 0),

            // const-wide/32
            LiteralInstruction.of(DexOpCode.CONST_WIDE_32,  0x0, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_32,  0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_32, -0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_32,  0x7fffffff, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_32, -0x80000000, 0),

            // const-wide
            LiteralInstruction.of(DexOpCode.CONST_WIDE,  0x0, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE,  0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE, -0x1, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE,  0x7fffffffffffffff, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE,  Long.MIN_VALUE, 0),

            // const-wide/high16
            LiteralInstruction.of(DexOpCode.CONST_WIDE_HIGH16,  0x0, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_HIGH16,  0x0001000000000000, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_HIGH16,  0x7fff000000000000, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_HIGH16, -0x0001000000000000, 0),
            LiteralInstruction.of(DexOpCode.CONST_WIDE_HIGH16, -0x7fff000000000000, 0)
        )

    override val failInstances: Array<() -> LiteralInstruction>
        get() = arrayOf(
            // const/4
            { LiteralInstruction.of(DexOpCode.CONST_4,  0x8, 0) },
            { LiteralInstruction.of(DexOpCode.CONST_4, -0x9, 0) },
            { LiteralInstruction.of(DexOpCode.CONST_4, -0x1, 20) },

            // const/16
            { LiteralInstruction.of(DexOpCode.CONST_16,  0x8000, 0) },
            { LiteralInstruction.of(DexOpCode.CONST_16, -0x8001, 0) },

            // const
            { LiteralInstruction.of(DexOpCode.CONST,  0x80000000, 0) },
            { LiteralInstruction.of(DexOpCode.CONST, -0x80000001, 0) },

            // const/high16
            { LiteralInstruction.of(DexOpCode.CONST_HIGH16,  0x00000001, 0) },
            { LiteralInstruction.of(DexOpCode.CONST_HIGH16, -0x00000001, 0) },
            { LiteralInstruction.of(DexOpCode.CONST_HIGH16,  0x80000000, 0) },
            { LiteralInstruction.of(DexOpCode.CONST_HIGH16, -0x80010000, 0) },

            // const-wide/16
            { LiteralInstruction.of(DexOpCode.CONST_16,  0x8000, 0) },
            { LiteralInstruction.of(DexOpCode.CONST_16, -0x8001, 0) },

            // const-wide/32
            { LiteralInstruction.of(DexOpCode.CONST_WIDE_32,  0x80000000, 0) },
            { LiteralInstruction.of(DexOpCode.CONST_WIDE_32, -0x80000001, 0) },

            // const-wide/high16
            { LiteralInstruction.of(DexOpCode.CONST_WIDE_HIGH16,  0x00000001, 0) },
            { LiteralInstruction.of(DexOpCode.CONST_WIDE_HIGH16, -0x00000001, 0) },
            { LiteralInstruction.of(DexOpCode.CONST_WIDE_HIGH16,  0x80000000, 0) },
            { LiteralInstruction.of(DexOpCode.CONST_WIDE_HIGH16, -0x80010000, 0) }
        )

    override fun equals(instructionA: LiteralInstruction, instructionB: LiteralInstruction): Boolean {
        return instructionA.opCode  == instructionB.opCode &&
               instructionA.literal == instructionB.literal
    }
}
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

package com.github.netomi.bat.classfile.instruction.editor

class OffsetMap constructor(var failOnMissingKey: Boolean = false) {
    private val labelOffsetMap    = mutableMapOf<String, Int>()

    private val oldToNewOffsetMap = mutableMapOf<Int, Int>()
    private val newToOldOffsetMap = mutableMapOf<Int, Int>()

    fun hasUpdates(): Boolean {
        return hasLabels() || hasOffsetUpdates()
    }

    fun hasLabels(): Boolean {
        return labelOffsetMap.isNotEmpty()
    }

    fun hasOffsetUpdates(): Boolean {
        return newToOldOffsetMap.any { it.key != it.value }
    }

    fun setLabel(label: String, offset: Int) {
        labelOffsetMap[label] = offset
    }

    fun getOffset(label: String): Int {
        return if (failOnMissingKey) {
            labelOffsetMap[label] ?: error("unknown label '$label'")
        } else {
            labelOffsetMap[label] ?: 0
        }
    }

    fun setOldToNewOffsetMapping(oldOffset: Int, newOffset: Int) {
        oldToNewOffsetMap[oldOffset] = newOffset
        newToOldOffsetMap[newOffset] = oldOffset
    }

    fun getOldOffset(newOffset: Int): Int {
        return newToOldOffsetMap[newOffset] ?: newOffset
    }

    fun getNewOffset(oldOffset: Int): Int {
        return oldToNewOffsetMap[oldOffset] ?: oldOffset
    }

    fun computeOffsetDiffToTargetLabel(currentOffset: Int, targetLabel: String): Int {
        val targetOffset = getOffset(targetLabel)
        return targetOffset - currentOffset
    }

    fun computeOffsetDiffToTargetOffset(currentOffset: Int, oldTargetOffset: Int): Int {
        val oldOffset = getOldOffset(currentOffset)
        val oldTarget = oldOffset + oldTargetOffset
        val newTarget = getNewOffset(oldTarget)

        return if (oldOffset != currentOffset || oldTarget != newTarget) {
            newTarget - currentOffset
        } else {
            oldTargetOffset
        }
    }
}

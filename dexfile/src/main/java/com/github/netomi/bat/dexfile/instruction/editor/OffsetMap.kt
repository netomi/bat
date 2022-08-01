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

package com.github.netomi.bat.dexfile.instruction.editor

class OffsetMap constructor(var failOnMissingKey: Boolean = false) {
    private val labelOffsetMap      = mutableMapOf<String, Int>()
    private val payloadReferenceMap = mutableMapOf<Int, Int>()

    private val oldToNewOffsetMap = mutableMapOf<Int, Int>()
    private val newToOldOffsetMap = mutableMapOf<Int, Int>()

    fun hasLabelsOrOffsetUpdates(): Boolean {
        return labelOffsetMap.isNotEmpty() || newToOldOffsetMap.any { it.key != it.value }
    }

    fun setLabel(label: String, offset: Int) {
        labelOffsetMap[label] = offset
    }

    fun setOldToNewOffsetMapping(oldOffset: Int, newOffset: Int) {
        oldToNewOffsetMap[oldOffset] = newOffset
        newToOldOffsetMap[newOffset] = oldOffset
    }

    fun getOldOffset(newOffset: Int): Int {
        return if (failOnMissingKey) {
            newToOldOffsetMap[newOffset] ?: throw RuntimeException("unknown old offset for $newOffset")
        } else {
            newOffset
        }
    }

    fun getNewOffset(oldOffset: Int): Int {
        return if (failOnMissingKey) {
            oldToNewOffsetMap[oldOffset] ?: throw RuntimeException("unknown new offset for $oldOffset")
        } else {
            oldOffset
        }
    }

    fun getOffset(label: String): Int {
        return if (failOnMissingKey) {
            labelOffsetMap[label] ?: throw RuntimeException("unknown label $label")
        } else {
            labelOffsetMap[label] ?: 0
        }
    }

    fun computeDiffToTargetLabel(currentOffset: Int, targetLabel: String): Int {
        val targetOffset = getOffset(targetLabel)
        return targetOffset - currentOffset
    }

    fun updateDiffToTargetOffset(currentOffset: Int, oldTargetOffset: Int): Int {
        val oldOffset = getOldOffset(currentOffset)
        val oldTarget = oldOffset + oldTargetOffset
        val newTarget = getNewOffset(oldTarget)

        return if (oldOffset != currentOffset || oldTarget != newTarget) {
            newTarget - currentOffset
        } else {
            oldTargetOffset
        }
    }

    fun setPayloadReferenceOffset(payloadOffset: Int, referenceOffset: Int) {
        payloadReferenceMap[payloadOffset] = referenceOffset
    }

    fun getPayloadReferenceOffset(payloadOffset: Int): Int {
        return if (failOnMissingKey) {
            payloadReferenceMap[payloadOffset] ?: throw RuntimeException("unknown payload reference for payload at offset $payloadOffset")
        } else {
            payloadReferenceMap[payloadOffset] ?: 0
        }
    }
}

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

class LabelMap constructor(var failOnMissingLabel: Boolean = false) {
    private val labelOffsetMap      = mutableMapOf<String, Int>()
    private val payloadReferenceMap = mutableMapOf<Int, Int>()

    fun isEmpty(): Boolean {
        return labelOffsetMap.isEmpty()
    }

    fun setLabel(label: String, offset: Int) {
        labelOffsetMap[label] = offset
    }

    fun getOffset(label: String): Int {
        return if (failOnMissingLabel) {
            labelOffsetMap[label] ?: throw RuntimeException("unknown label $label")
        } else {
            labelOffsetMap[label] ?: 0
        }
    }

    fun computeDiffToTarget(currentOffset: Int, targetLabel: String): Int {
        val targetOffset = getOffset(targetLabel)
        return targetOffset - currentOffset
    }

    fun setPayloadReferenceOffset(payloadOffset: Int, referenceOffset: Int) {
        payloadReferenceMap[payloadOffset] = referenceOffset
    }

    fun getPayloadReferenceOffset(payloadOffset: Int): Int {
        return if (failOnMissingLabel) {
            payloadReferenceMap[payloadOffset] ?: throw RuntimeException("unknown payload reference for payload at offset $payloadOffset")
        } else {
            payloadReferenceMap[payloadOffset] ?: 0
        }
    }
}

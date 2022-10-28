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

package com.github.netomi.bat.classfile.verifier

import com.github.netomi.bat.util.mutableListOfCapacity

class Frame private constructor(private val variables: MutableList<VerificationType?> = mutableListOfCapacity(0),
                                private val stack:     MutableList<VerificationType>  = mutableListOfCapacity(0)) {

    val stackSize: Int
        get() = stack.fold(0) { agg, type -> agg + type.operandSize }

    val variableSize: Int
        get() = variables.size

    fun copy(): Frame {
        return Frame(variables.toMutableList(), stack.toMutableList())
    }

    fun pop(): VerificationType {
        return stack.removeLast()
    }

    fun peek(): VerificationType {
        return stack.last()
    }

    fun pop(count: Int) {
        for (i in 0 until count) {
            stack.removeLast()
        }
    }

    fun push(type: VerificationType) {
        stack.add(type)
    }

    fun clearStack() {
        stack.clear()
    }

    fun load(variable: Int): VerificationType {
        val value = variables[variable]
        check(value != null && value !is TopType)
        return value
    }

    fun store(variable: Int, type: VerificationType) {
        val maxVariableIndex = if (type.isCategory2) variable + 1 else variable

        ensureVariableCapacity(maxVariableIndex)
        variables[variable] = type

        if (type.isCategory2) {
            variables[variable + 1] = TopType
        }
    }

    private fun ensureVariableCapacity(capacity: Int) {
        while (capacity >= variables.size) {
            variables.add(null)
        }
    }

    internal fun referenceInitialized(reference: VerificationType) {
        require(reference is UninitializedType ||
                reference is UninitializedThisType)

        if (reference is UninitializedType) {
            val initializedReference = JavaReferenceType.of(reference.classType)

            for (i in variables.indices) {
                if (variables[i] == reference) {
                    variables[i] = initializedReference
                }
            }

            for (i in stack.indices) {
                if (stack[i] == reference) {
                    stack[i] = initializedReference
                }
            }
        } else if (reference is UninitializedThisType) {
            val initializedReference = JavaReferenceType.of(reference.classType)

            for (i in variables.indices) {
                if (variables[i] is UninitializedThisType) {
                    variables[i] = initializedReference
                }
            }

            for (i in stack.indices) {
                if (stack[i] is UninitializedThisType) {
                    stack[i] = initializedReference
                }
            }
        }
    }

    override fun toString(): String {
        return buildString {
            append(variables.joinToString(separator = ", ", prefix = "{", postfix = "}", transform = { it?.name ?: "" }))
            append(" ")
            append(stack.joinToString(separator = ", ", prefix = "{", postfix = "}", transform = { it.name }))
        }
    }

    companion object {
        fun empty(): Frame {
            return Frame()
        }
    }
}

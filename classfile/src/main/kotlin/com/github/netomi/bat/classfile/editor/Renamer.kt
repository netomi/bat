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

package com.github.netomi.bat.classfile.editor

import com.github.netomi.bat.util.*

open class Renamer {

    open fun renameClass(className: JvmClassName): JvmClassName {
        return className
    }

    internal fun renameClassName(className: JvmClassName): JvmClassName {
        return if (className.isArrayClass) {
            renameArrayType(className.toJvmType()).toJvmClassName()
        } else {
            renameClass(className)
        }
    }

    internal fun renameFieldType(fieldType: JvmType): JvmType {
        return if (fieldType.isClassType) {
            val className = fieldType.toJvmClassName()
            renameClass(className).toJvmType()
        } else if (fieldType.isArrayType) {
            renameArrayType(fieldType)
        } else {
            fieldType
        }
    }

    internal fun renameMethodDescriptor(descriptor: String): String {
        val (parameterTypes, returnType) = parseDescriptorToJvmTypes(descriptor)

        val result = buildString {
            append("(")
            for (parameterType in parameterTypes) {
                append(renameFieldType(parameterType))
            }
            append(")")
            append(renameFieldType(returnType))
        }

        return result
    }

    private fun renameArrayType(arrayType: JvmType): JvmType {
        val componentType = arrayType.componentType
        return if (componentType.isClassType) {
            val arrayDimension     = arrayType.arrayDimension
            val componentClassName = componentType.toJvmClassName()
            val renamedClassName   = renameClass(componentClassName)

            ("[".repeat(arrayDimension) + renamedClassName.toJvmType()).asJvmType()
        } else {
            arrayType
        }
    }
}
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

    internal fun renameClassSignature(signature: String): String {
        val signatureBuilder = SignatureBuilder(::renameClassName)
        SignatureParser.of(signature).parseClass(signatureBuilder)
        return signatureBuilder.result
    }

    internal fun renameFieldSignature(signature: String): String {
        val signatureBuilder = SignatureBuilder(::renameClassName)
        SignatureParser.of(signature).parseType(signatureBuilder)
        return signatureBuilder.result
    }

    internal fun renameMethodSignature(signature: String): String {
        val signatureBuilder = SignatureBuilder(::renameClassName)
        SignatureParser.of(signature).parseMethod(signatureBuilder)
        return signatureBuilder.result
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

private class SignatureBuilder constructor(private val renameClass: (JvmClassName) -> JvmClassName): SignatureVisitor {

    private var builder = StringBuilder()

    val result: String
        get() = builder.toString()

    var hasClassBound     = false
    var hasInterfaceBound = false

    var nextTypeIsArray = false
    var arrayDimension  = 0

    override fun visitFormalTypeParameterStart() {
        builder.append("<")
    }

    override fun visitFormalTypeParameterEnd() {
        builder.append(">")
    }

    override fun visitFormalTypeParameter(name: String, formalParameterIndex: Int) {
        builder.append(name)

        hasClassBound = false
        hasInterfaceBound = false
    }

    override fun visitClassBound() {
        builder.append(":")
        hasClassBound = true
    }

    override fun visitInterfaceBound(boundIndex: Int) {
        if (!hasClassBound) {
            builder.append(":")
            hasClassBound = true
        }

        if (!hasInterfaceBound) {
            builder.append(":")
            hasInterfaceBound = true
        }
    }

    override fun visitParametersStart() {
        builder.append("(")
    }

    override fun visitParametersEnd() {
        builder.append(")")
    }

    override fun visitExceptionType(exceptionIndex: Int) {
        builder.append("^")
    }

    override fun visitBaseType(baseType: String) {
        if (nextTypeIsArray) {
            builder.append("${"[".repeat(arrayDimension)}$baseType")
            nextTypeIsArray = false
        } else {
            builder.append(baseType)
        }
    }

    override fun visitArrayType(dimension: Int) {
        nextTypeIsArray = true
        arrayDimension  = dimension
    }

    override fun visitInnerClassType(className: String) {
        builder.append(".$className")
    }

    override fun visitClassType(className: String) {
        val renamedClassName = renameClass(className.asInternalClassName())
        val renamedClassType = renamedClassName.toInternalType().removeSuffix(";")

        if (nextTypeIsArray) {
            builder.append("[".repeat(arrayDimension))
            builder.append(renamedClassType)
            nextTypeIsArray = false
        } else {
            builder.append(renamedClassType)
        }
    }

    override fun visitClassTypeEnd(className: String) {
        builder.append(";")
    }

    override fun visitTypeVariable(variableName: String) {
        if (nextTypeIsArray) {
            builder.append("${"[".repeat(arrayDimension)}T$variableName;")
            nextTypeIsArray = false
        } else {
            builder.append("T$variableName;")
        }
    }

    override fun visitTypeArgumentStart() {
        builder.append("<")
    }

    override fun visitTypeArgumentEnd() {
        builder.append(">")
    }

    override fun visitUnboundedTypeArgument(typeArgumentIndex: Int) {
        builder.append("*")
    }

    override fun visitBoundedTypeArgument(wildcard: Char, typeArgumentIndex: Int) {
        when (wildcard) {
            '+' -> builder.append("+")
            '-' -> builder.append("-")
            else -> {}
        }
    }
}
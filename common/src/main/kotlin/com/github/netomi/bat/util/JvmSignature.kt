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

package com.github.netomi.bat.util

import kotlin.text.StringBuilder

interface SignatureVisitor {
    fun visitFormalTypeParameterStart() {}
    fun visitFormalTypeParameterEnd() {}
    fun visitFormalTypeParameter(name: String, formalParameterIndex: Int) {}
    fun visitClassBound() {}
    fun visitInterfaceBound() {}

    fun visitSuperclass() {}
    fun visitInterface(interfaceIndex: Int) {}

    fun visitParameterType(parameterIndex: Int) {}
    fun visitParametersStart() {}
    fun visitParametersEnd() {}
    fun visitReturnType() {}
    fun visitReturnEnd() {}
    fun visitExceptionType(exceptionIndex: Int) {}
    fun visitExceptionEnd() {}

    fun visitBaseType(baseType: String) {}

    fun visitArrayType(dimension: Int) {}

    fun visitClassType(className: String) {}
    fun visitInnerClassType(className: String) {}
    fun visitClassTypeEnd(className: String) {}

    fun visitTypeVariable(variableName: String) {}

    fun visitTypeArgumentStart() {}
    fun visitTypeArgumentEnd() {}

    fun visitUnboundedTypeArgument(typeArgumentIndex: Int) {}

    fun visitBoundedTypeArgument(wildcard: Char, typeArgumentIndex: Int) {}
}

private class SignatureParser private constructor(val signature: String) {

    private var currentPosition = 0

    fun parseClass(visitor: SignatureVisitor) {
        var c = signature[currentPosition]
        if (c == '<') {
            visitor.visitFormalTypeParameterStart()
            currentPosition++
            var formalParameterIndex = 0
            do {
                val endPosition = signature.indexOf(':', currentPosition)
                visitor.visitFormalTypeParameter(signature.substring(currentPosition, endPosition), formalParameterIndex++)
                currentPosition = endPosition + 1

                c = signature[currentPosition]
                when (c) {
                    'L', '[', 'T' -> {
                        visitor.visitClassBound()
                        parseType(visitor)
                        c = signature[currentPosition]
                    }
                }

                while (c == ':') {
                    visitor.visitInterfaceBound()
                    currentPosition++
                    parseType(visitor)
                    c = signature[currentPosition]
                }
            } while (c != '>')
            visitor.visitFormalTypeParameterEnd()
            currentPosition++
        }

        visitor.visitSuperclass()
        parseType(visitor)

        var interfaceIndex = 0
        while (currentPosition < signature.length) {
            visitor.visitInterface(interfaceIndex++)
            parseType(visitor)
        }
    }

    fun parseMethod(visitor: SignatureVisitor) {
        var c = signature[currentPosition]
        if (c == '<') {
            visitor.visitFormalTypeParameterStart()
            currentPosition++
            var formalParameterIndex = 0
            do {
                val endPosition = signature.indexOf(':', currentPosition)
                visitor.visitFormalTypeParameter(signature.substring(currentPosition, endPosition), formalParameterIndex++)
                currentPosition = endPosition + 1

                c = signature[currentPosition]
                when (c) {
                    'L', '[', 'T' -> {
                        visitor.visitClassBound()
                        parseType(visitor)
                        c = signature[currentPosition]
                    }
                }

                while (c == ':') {
                    visitor.visitInterfaceBound()
                    currentPosition++
                    parseType(visitor)
                    c = signature[currentPosition]
                }
            } while (c != '>')
            visitor.visitFormalTypeParameterEnd()
            currentPosition++
        }

        if (signature[currentPosition++] == '(') {
            visitor.visitParametersStart()
            var parameterIndex = 0
            while(signature[currentPosition] != ')') {
                visitor.visitParameterType(parameterIndex++)
                parseType(visitor)
            }
            visitor.visitParametersEnd()
            currentPosition++
            visitor.visitReturnType()
            parseType(visitor)
            visitor.visitReturnEnd()

            currentPosition++
            var exceptionIndex = 0
            while (currentPosition < signature.length) {
                visitor.visitExceptionType(exceptionIndex++)
                parseType(visitor)
                currentPosition++
            }
            visitor.visitExceptionEnd()
        }
    }

    fun parseType(visitor: SignatureVisitor) {
        var c = signature[currentPosition++]

        if (isPrimitiveType(c.toString())) {
            visitor.visitBaseType(c.toString())
        } else if (c == 'V') {
            visitor.visitBaseType("V")
        } else {
            when (c) {
                '[' -> {
                    var dimension = 1
                    while (signature[currentPosition] == '[') {
                        dimension++
                        currentPosition++
                    }
                    visitor.visitArrayType(dimension)
                    parseType(visitor)
                    return
                }

                'T' -> {
                    val endPosition = signature.indexOf(';', currentPosition)
                    visitor.visitTypeVariable(signature.substring(currentPosition, endPosition))
                    currentPosition = endPosition + 1
                    return
                }

                'L' -> {
                    var startPosition = currentPosition
                    var visited = false
                    var innerClass = false

                    while (currentPosition < signature.length) {
                        when (signature[currentPosition++]) {
                            '.' -> {
                                val className = signature.substring(startPosition, currentPosition - 1)
                                if (!visited) {
                                    if (innerClass) {
                                        visitor.visitInnerClassType(className)
                                    } else {
                                        visitor.visitClassType(className)
                                    }
                                }
                                visitor.visitClassTypeEnd(className)
                                startPosition = currentPosition
                                innerClass = true
                                visited    = false
                            }
                            ';' -> {
                                val className = signature.substring(startPosition, currentPosition - 1)
                                if (!visited) {
                                    if (innerClass) {
                                        visitor.visitInnerClassType(className)
                                    } else {
                                        visitor.visitClassType(className)
                                    }
                                }
                                visitor.visitClassTypeEnd(className)
                                return
                            }

                            '<' -> {
                                val className = signature.substring(startPosition, currentPosition - 1)
                                if (innerClass) {
                                    visitor.visitInnerClassType(className)
                                } else {
                                    visitor.visitClassType(className)
                                }
                                visited = true

                                visitor.visitTypeArgumentStart()

                                var foundEnd = false
                                var typeArgumentIndex = 0
                                while (currentPosition < signature.length && !foundEnd) {
                                    c = signature[currentPosition]
                                    when (c) {
                                        '>' -> {
                                            foundEnd = true
                                            visitor.visitTypeArgumentEnd()
                                            visitor.visitClassTypeEnd(className)
                                            currentPosition++
                                        }

                                        '*' -> {
                                            visitor.visitUnboundedTypeArgument(typeArgumentIndex++)
                                            currentPosition++
                                        }

                                        '+', '-' -> {
                                            visitor.visitBoundedTypeArgument(c, typeArgumentIndex++)
                                            currentPosition++
                                            parseType(visitor)
                                        }

                                        else -> {
                                            visitor.visitBoundedTypeArgument('=', typeArgumentIndex++)
                                            parseType(visitor)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    error("unexpected end of signature string '$signature")
                }

                else -> error("unexpected character '$c' while parsing signature")
            }
        }
    }

    companion object {
        fun of(signature: String): SignatureParser {
            return SignatureParser(signature)
        }
    }
}

fun getExternalClassSignature(signature: String, isInterface: Boolean = false): String {
    val visitor = ExternalSignatureBuilder(isInterface)
    SignatureParser.of(signature).parseClass(visitor)
    return visitor.formalTypeParameters + visitor.result
}

fun getExternalFieldSignature(signature: String): String {
    val visitor = ExternalSignatureBuilder()
    SignatureParser.of(signature).parseType(visitor)
    return visitor.result
}

fun getExternalMethodSignature(signature: String): MethodSignature {
    val visitor = ExternalSignatureBuilder()
    SignatureParser.of(signature).parseMethod(visitor)
    return MethodSignature(visitor.formalTypeParameters, visitor.parameters, visitor.returnType, visitor.exceptions)
}

data class MethodSignature (val formalTypeParameters: String, val parameters: String, val returnType: String, val exceptions: String)

private class ExternalSignatureBuilder constructor(val isInterface: Boolean = false): SignatureVisitor {

    private var builder = StringBuilder()

    var formalTypeParameters: String = ""
    var parameters: String = ""
    var returnType: String = ""
    var exceptions: String = ""

    val result: String
        get() = builder.toString()

    var nextTypeIsArray = false
    var arrayDimension  = 0
    var arrayClassName:String? = null
    var ignoreNextType  = false

    override fun visitFormalTypeParameterStart() {
        builder.append("<")
    }

    override fun visitFormalTypeParameterEnd() {
        builder.append(">")
        formalTypeParameters = builder.toString()
        builder.clear()
    }

    override fun visitFormalTypeParameter(name: String, formalParameterIndex: Int) {
        if (formalParameterIndex > 0) {
            builder.append(", ")
        }
        builder.append(name)
    }

    override fun visitClassBound() {
        builder.append(" extends ")
    }

    override fun visitInterfaceBound() {
        builder.append(" extends ")
    }

    override fun visitSuperclass() {
        if (!isInterface) {
            builder.append(" extends ")
        } else {
            ignoreNextType = true
        }
    }

    override fun visitInterface(interfaceIndex: Int) {
        if (interfaceIndex == 0) {
            if (isInterface) {
                builder.append(" extends ")
            } else {
                builder.append(" implements ")
            }
        } else {
            builder.append(", ")
        }
    }

    override fun visitParameterType(parameterIndex: Int) {
        if (parameterIndex > 0) {
            builder.append(", ")
        }
    }

    override fun visitParametersStart() {
        builder.append("(")
    }

    override fun visitParametersEnd() {
        builder.append(")")
        parameters = builder.toString()
        builder.clear()
    }

    override fun visitReturnType() {}

    override fun visitReturnEnd() {
        returnType = builder.toString()
        builder.clear()
    }

    override fun visitExceptionType(exceptionIndex: Int) {
        if (exceptionIndex == 0) {
            builder.append(" throws ")
        } else {
            builder.append(", ")
        }
    }

    override fun visitExceptionEnd() {
        exceptions = builder.toString()
        builder.clear()
    }

    override fun visitBaseType(baseType: String) {
        if (nextTypeIsArray) {
            builder.append("${"[".repeat(arrayDimension)}$baseType".asJvmType().toExternalType())
            nextTypeIsArray = false
        } else {
            builder.append(baseType.asJvmType().toExternalType())
        }
    }

    override fun visitArrayType(dimension: Int) {
        nextTypeIsArray = true
        arrayDimension  = dimension
    }

    override fun visitInnerClassType(className: String) {
        builder.append(".${className.asInternalClassName().toExternalClassName()}")
    }

    override fun visitClassType(className: String) {
        if (nextTypeIsArray) {
            builder.append(className.asInternalClassName().toExternalClassName())
            arrayClassName = className
            nextTypeIsArray = false
        } else if (ignoreNextType) {
            ignoreNextType = false
        } else {
            builder.append(className.asInternalClassName().toExternalClassName())
        }
    }

    override fun visitClassTypeEnd(className: String) {
        if (className == arrayClassName) {
            builder.append("[]".repeat(arrayDimension))
            arrayClassName = null
        }
    }

    override fun visitTypeVariable(variableName: String) {
        if (nextTypeIsArray) {
            builder.append("${variableName}${"[]".repeat(arrayDimension)}")
            nextTypeIsArray = false
        } else {
            builder.append(variableName)
        }
    }

    override fun visitTypeArgumentStart() {
        builder.append("<")
    }

    override fun visitTypeArgumentEnd() {
        builder.append(">")
    }

    override fun visitUnboundedTypeArgument(typeArgumentIndex: Int) {
        if (typeArgumentIndex > 0) {
            builder.append(", ")
        }
        builder.append("?")
    }

    override fun visitBoundedTypeArgument(wildcard: Char, typeArgumentIndex: Int) {
        if (typeArgumentIndex > 0) {
            builder.append(", ")
        }

        when (wildcard) {
            '+' -> builder.append("? extends ")
            '-' -> builder.append("? super ")
            else -> {}
        }
    }
}

fun main(args: Array<String>) {
    val classSignature = "Lcom/google/common/cache/LocalCache<TK;TV;>.AbstractCacheSet<Ljava/util/Map\$Entry<TK;TV;>;>;"
    val fieldSignature = "[[TV;"
    val methodSignature = "<E:Ljava/lang/Object;>([TE;)[TE;"
    val signature = "<A::Ljava/lang/Appendable;>(TA;Ljava/util/Map<**>;)TA;"
    println(getExternalFieldSignature(fieldSignature))
}
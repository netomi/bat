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

import com.github.netomi.bat.util.*

abstract class VerificationType {
    abstract val name: String

    abstract val isCategory2: Boolean
    abstract val operandSize: Int

    companion object {
        fun of(jvmType: JvmType): VerificationType {
            return if (jvmType.isReferenceType) {
                JavaReferenceType.of(jvmType)
            } else if (jvmType.isPrimitiveType) {
                when (jvmType.type) {
                    BYTE_TYPE,
                    SHORT_TYPE,
                    CHAR_TYPE,
                    BOOLEAN_TYPE,
                    INT_TYPE    -> IntegerType
                    FLOAT_TYPE  -> FloatType
                    LONG_TYPE   -> LongType
                    DOUBLE_TYPE -> DoubleType
                    else        -> error("unexpected primitive type '$jvmType'")
                }
            } else {
                error("unexpected type '$jvmType'")
            }
        }
    }
}

object TopType: VerificationType() {
    override val name: String
        get() = "top"

    override val isCategory2: Boolean
        get() = error("should never be called")

    override val operandSize: Int
        get() = error("should never be called")
}

object IntegerType: VerificationType() {
    override val name: String
        get() = "int"

    override val isCategory2: Boolean
        get() = false

    override val operandSize: Int
        get() = 1
}

object FloatType: VerificationType() {
    override val name: String
        get() = "float"

    override val isCategory2: Boolean
        get() = false

    override val operandSize: Int
        get() = 1
}

object LongType: VerificationType() {
    override val name: String
        get() = "long"

    override val isCategory2: Boolean
        get() = true

    override val operandSize: Int
        get() = 2
}

object DoubleType: VerificationType() {
    override val name: String
        get() = "double"

    override val isCategory2: Boolean
        get() = true

    override val operandSize: Int
        get() = 2
}

abstract class ReferenceType constructor(protected val _classType: JvmType?): VerificationType() {
    override val isCategory2: Boolean
        get() = false

    override val operandSize: Int
        get() = 1
}

class JavaReferenceType private constructor(classType: JvmType): ReferenceType(classType) {
    override val name: String
        get() = "class($classType)"

    val classType: JvmType
        get() = _classType!!

    companion object {
        fun of(classType: JvmType): JavaReferenceType {
            return JavaReferenceType(classType)
        }
    }
}

class UninitializedThisType private constructor(classType: JvmType): ReferenceType(classType) {
    override val name: String
        get() = "uninitializedThis($classType)"

    val classType: JvmType
        get() = _classType!!

    companion object {
        fun of(classType: JvmType): UninitializedThisType {
            return UninitializedThisType(classType)
        }
    }
}

class UninitializedType private constructor(classType: JvmType, val offset: Int): ReferenceType(classType) {
    override val name: String
        get() = "uninitialized($classType, $offset)"

    val classType: JvmType
        get() = _classType!!

    companion object {
        fun of(classType: JvmType, offset: Int): UninitializedType {
            return UninitializedType(classType, offset)
        }
    }
}

object NullReference: ReferenceType(null) {
    override val name: String
        get() = "nullReference"
}
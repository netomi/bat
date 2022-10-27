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

package com.github.netomi.bat.classfile.eval

import com.github.netomi.bat.util.*

abstract class VerificationType {
    abstract val name: String

    abstract val isCategory2: Boolean

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
        get() = false
}

object IntegerType: VerificationType() {
    override val name: String
        get() = "int"

    override val isCategory2: Boolean
        get() = false
}

object FloatType: VerificationType() {
    override val name: String
        get() = "float"

    override val isCategory2: Boolean
        get() = false
}

object LongType: VerificationType() {
    override val name: String
        get() = "long"

    override val isCategory2: Boolean
        get() = true
}

object DoubleType: VerificationType() {
    override val name: String
        get() = "double"

    override val isCategory2: Boolean
        get() = true
}

abstract class ReferenceType constructor(protected val _classType: JvmType?): VerificationType()

class JavaReferenceType private constructor(classType: JvmType): ReferenceType(classType) {
    override val name: String
        get() = "class($classType)"

    override val isCategory2: Boolean
        get() = false

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

    override val isCategory2: Boolean
        get() = false

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

    override val isCategory2: Boolean
        get() = false

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

    override val isCategory2: Boolean
        get() = false
}
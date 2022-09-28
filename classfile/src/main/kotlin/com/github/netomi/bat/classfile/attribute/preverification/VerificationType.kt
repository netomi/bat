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

package com.github.netomi.bat.classfile.attribute.preverification

import com.github.netomi.bat.classfile.*
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import com.github.netomi.bat.util.JvmClassName

abstract class VerificationType: ClassFileContent() {
    abstract val type: ItemType

    override val contentSize: Int = 1

    internal open fun readInfo(input: ClassDataInput) {}
    internal open fun writeInfo(output: ClassDataOutput) {}

    open fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {}

    override fun write(output: ClassDataOutput) {
        output.writeByte(type.tag)
        writeInfo(output)
    }

    companion object {
        internal fun read(input: ClassDataInput): VerificationType {
            val tag  = input.readUnsignedByte()
            val type = ItemType.of(tag).createVerificationType()
            type.readInfo(input)
            return type
        }
    }
}

object TopVariable: VerificationType() {
    override val type: ItemType
        get() = ItemType.TOP

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return "TopVariable[]"
    }

    internal fun empty(): TopVariable {
        return TopVariable
    }
}

object IntegerVariable: VerificationType() {
    override val type: ItemType
        get() = ItemType.INTEGER

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return "IntegerVariable[]"
    }

    internal fun empty(): IntegerVariable {
        return IntegerVariable
    }
}

object LongVariable: VerificationType() {
    override val type: ItemType
        get() = ItemType.LONG

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return "LongVariable[]"
    }

    internal fun empty(): LongVariable {
        return LongVariable
    }
}

object FloatVariable: VerificationType() {
    override val type: ItemType
        get() = ItemType.FLOAT

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return "FloatVariable[]"
    }

    internal fun empty(): FloatVariable {
        return FloatVariable
    }
}

object DoubleVariable: VerificationType() {
    override val type: ItemType
        get() = ItemType.DOUBLE

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return "DoubleVariable[]"
    }

    internal fun empty(): DoubleVariable {
        return DoubleVariable
    }
}

object NullVariable: VerificationType() {
    override val type: ItemType
        get() = ItemType.NULL

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return "NullVariable[]"
    }

    internal fun empty(): NullVariable {
        return NullVariable
    }
}

object UninitializedThisVariable: VerificationType() {
    override val type: ItemType
        get() = ItemType.UNINITIALIZED_THIS

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return "UninitializedThisVariable[]"
    }

    internal fun empty(): UninitializedThisVariable {
        return UninitializedThisVariable
    }
}

data class ObjectVariable private constructor(private var _classIndex: Int = -1): VerificationType() {

    override val type: ItemType
        get() = ItemType.OBJECT

    override val contentSize: Int
        get() = 3

    val classIndex: Int
        get() = _classIndex

    fun getClassName(classFile: ClassFile): JvmClassName {
        return classFile.getClassName(classIndex)
    }

    override fun readInfo(input: ClassDataInput) {
        _classIndex = input.readUnsignedShort()
    }

    override fun writeInfo(output: ClassDataOutput) {
        output.writeShort(classIndex)
    }

    fun classConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(classIndex, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitClassConstant(classFile, this, PropertyAccessor(::_classIndex))
    }

    override fun toString(): String {
        return "ObjectVariable[classIndex=${classIndex}]"
    }

    companion object {
        internal fun empty(): ObjectVariable {
            return ObjectVariable()
        }
    }
}

data class UninitializedVariable private constructor(private var _offset: Int = -1): VerificationType() {

    override val type: ItemType
        get() = ItemType.UNINITIALIZED

    override val contentSize: Int
        get() = 3

    val offset: Int
        get() = _offset

    override fun readInfo(input: ClassDataInput) {
        _offset = input.readUnsignedShort()
    }

    override fun writeInfo(output: ClassDataOutput) {
        output.writeShort(offset)
    }

    override fun toString(): String {
        return "UninitializedVariable[offset=${offset}]"
    }

    companion object {
        internal fun empty(): UninitializedVariable {
            return UninitializedVariable()
        }
    }
}

enum class ItemType constructor(internal val tag: Int, private val supplier: () -> VerificationType) {
    TOP               (ITEM_Top,               { TopVariable.empty() }),
    INTEGER           (ITEM_Integer,           { IntegerVariable.empty() }),
    LONG              (ITEM_Long,              { LongVariable.empty() }),
    FLOAT             (ITEM_Float,             { FloatVariable.empty() }),
    DOUBLE            (ITEM_Double,            { DoubleVariable.empty() }),
    NULL              (ITEM_Null,              { NullVariable.empty() }),
    UNINITIALIZED_THIS(ITEM_UninitializedThis, { UninitializedThisVariable.empty() }),
    OBJECT            (ITEM_Object,            { ObjectVariable.empty() }),
    UNINITIALIZED     (ITEM_Uninitialized,     { UninitializedVariable.empty() });

    fun createVerificationType(): VerificationType {
        return supplier()
    }

    companion object {
        private val tagToItemMap: Map<Int, ItemType> by lazy {
            ItemType.values().associateBy { it.tag }
        }

        fun of(tag: Int) : ItemType {
            return tagToItemMap[tag] ?: throw IllegalArgumentException("unknown tag '$tag'")
        }
    }
}
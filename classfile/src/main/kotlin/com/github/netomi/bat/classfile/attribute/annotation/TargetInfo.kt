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

package com.github.netomi.bat.classfile.attribute.annotation

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotation.visitor.TargetInfoVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import com.github.netomi.bat.classfile.io.dataSize
import com.github.netomi.bat.util.mutableListOfCapacity

abstract class TargetInfo protected constructor(open val type: TargetInfoType): ClassFileContent() {

    internal abstract fun readInfo(input: ClassDataInput)
    internal abstract fun writeInfo(output: ClassDataOutput)

    override fun write(output: ClassDataOutput) {
        output.writeByte(type.targetType)
        writeInfo(output)
    }

    abstract fun accept(classFile: ClassFile, visitor: TargetInfoVisitor)

    companion object {
        internal fun empty(): TargetInfo {
            return EmptyTargetInfo.create(TargetInfoType.FIELD)
        }

        internal fun read(input: ClassDataInput): TargetInfo {
            val targetType = input.readUnsignedByte()

            val targetInfo = TargetInfoType.createTargetInfo(targetType)
            targetInfo.readInfo(input)
            return targetInfo
        }
    }
}

data class TypeParameterTargetInfo private constructor(override val type:                TargetInfoType,
                                                        private var _typeParameterIndex: Int = -1): TargetInfo(type) {

    override val dataSize: Int
        get() = 2

    val typeParameterIndex: Int
        get() = _typeParameterIndex

    override fun readInfo(input: ClassDataInput) {
        _typeParameterIndex = input.readUnsignedByte()
    }

    override fun writeInfo(output: ClassDataOutput) {
        output.writeByte(typeParameterIndex)
    }

    override fun accept(classFile: ClassFile, visitor: TargetInfoVisitor) {
        visitor.visitTypeParameterTargetInfo(classFile, this)
    }

    companion object {
        internal fun create(type: TargetInfoType): TargetInfo {
            return TypeParameterTargetInfo(type)
        }
    }
}

data class SuperTypeTargetInfo private constructor(override val type:            TargetInfoType,
                                                    private var _superTypeIndex: Int = -1): TargetInfo(type) {
    override val dataSize: Int
        get() = 3

    val superTypeIndex: Int
        get() = _superTypeIndex

    override fun readInfo(input: ClassDataInput) {
        _superTypeIndex = input.readUnsignedShort()
    }

    override fun writeInfo(output: ClassDataOutput) {
        output.writeShort(superTypeIndex)
    }

    override fun accept(classFile: ClassFile, visitor: TargetInfoVisitor) {
        visitor.visitSuperTypeTargetInfo(classFile, this)
    }

    companion object {
        internal fun create(type: TargetInfoType): TargetInfo {
            return SuperTypeTargetInfo(type)
        }
    }
}

data class TypeParameterBoundTargetInfo
    private constructor(override val type:                TargetInfoType,
                         private var _typeParameterIndex: Int = -1,
                         private var _boundIndex:         Int = -1): TargetInfo(type) {

    override val dataSize: Int
        get() = 3

    val typeParameterIndex: Int
        get() = _typeParameterIndex

    val boundIndex: Int
        get() = _boundIndex

    override fun readInfo(input: ClassDataInput) {
        _typeParameterIndex = input.readUnsignedByte()
        _boundIndex         = input.readUnsignedByte()
    }

    override fun writeInfo(output: ClassDataOutput) {
        output.writeByte(typeParameterIndex)
        output.writeByte(boundIndex)
    }

    override fun accept(classFile: ClassFile, visitor: TargetInfoVisitor) {
        visitor.visitTypeParameterBoundTargetInfo(classFile, this)
    }

    companion object {
        internal fun create(type: TargetInfoType): TargetInfo {
            return TypeParameterBoundTargetInfo(type)
        }
    }
}

data class EmptyTargetInfo private constructor(override val type: TargetInfoType): TargetInfo(type) {

    override val dataSize: Int
        get() = 1

    override fun readInfo(input: ClassDataInput) {}

    override fun writeInfo(output: ClassDataOutput) {}

    override fun accept(classFile: ClassFile, visitor: TargetInfoVisitor) {
        visitor.visitEmptyTargetInfo(classFile, this)
    }

    companion object {
        internal fun create(type: TargetInfoType): TargetInfo {
            return EmptyTargetInfo(type)
        }
    }
}

data class FormalParameterTargetInfo private constructor(override val type:                  TargetInfoType,
                                                          private var _formalParameterIndex: Int = -1): TargetInfo(type) {
    override val dataSize: Int
        get() = 2

    val formalParameterIndex: Int
        get() = _formalParameterIndex

    override fun readInfo(input: ClassDataInput) {
        _formalParameterIndex = input.readUnsignedByte()
    }

    override fun writeInfo(output: ClassDataOutput) {
        output.writeByte(formalParameterIndex)
    }

    override fun accept(classFile: ClassFile, visitor: TargetInfoVisitor) {
        visitor.visitFormalParameterTargetInfo(classFile, this)
    }

    companion object {
        internal fun create(type: TargetInfoType): TargetInfo {
            return FormalParameterTargetInfo(type)
        }
    }
}

data class ThrowsTargetInfo private constructor(override val type:             TargetInfoType,
                                                 private var _throwsTypeIndex: Int = -1): TargetInfo(type) {
    override val dataSize: Int
        get() = 3

    val throwsTypeIndex: Int
        get() = _throwsTypeIndex

    override fun readInfo(input: ClassDataInput) {
        _throwsTypeIndex = input.readUnsignedShort()
    }

    override fun writeInfo(output: ClassDataOutput) {
        output.writeShort(throwsTypeIndex)
    }

    override fun accept(classFile: ClassFile, visitor: TargetInfoVisitor) {
        visitor.visitThrowsTargetInfo(classFile, this)
    }

    companion object {
        internal fun create(type: TargetInfoType): TargetInfo {
            return ThrowsTargetInfo(type)
        }
    }
}

data class LocalVarTargetInfo
    private constructor(override val type:   TargetInfoType,
                         private var _table: MutableList<LocalVarElement> = mutableListOfCapacity(0))
    : TargetInfo(type), Sequence<LocalVarElement> {

    override val dataSize: Int
        get() = 1 + _table.dataSize()

    val size: Int
        get() = _table.size

    operator fun get(index: Int): LocalVarElement {
        return _table[index]
    }

    override fun iterator(): Iterator<LocalVarElement> {
        return _table.iterator()
    }

    override fun readInfo(input: ClassDataInput) {
        val tableLength = input.readUnsignedShort()
        _table = mutableListOfCapacity(tableLength)
        for (i in 0 until tableLength) {
            _table.add(LocalVarElement.read(input))
        }
    }

    override fun writeInfo(output: ClassDataOutput) {
        output.writeContentList(_table)
    }

    override fun accept(classFile: ClassFile, visitor: TargetInfoVisitor) {
        visitor.visitLocalVarTargetInfo(classFile, this)
    }

    companion object {
        internal fun create(type: TargetInfoType): TargetInfo {
            return LocalVarTargetInfo(type)
        }
    }
}

data class LocalVarElement private constructor(private var _startPC: Int = -1,
                                               private var _length:  Int = -1,
                                               private var _index:   Int = -1): ClassFileContent() {

    override val dataSize: Int
        get() = 6

    val startPC: Int
        get() = _startPC

    val length: Int
        get() = _length

    val index: Int
        get() = _index

    internal fun read(input: ClassDataInput) {
        _startPC = input.readUnsignedShort()
        _length  = input.readUnsignedShort()
        _index   = input.readUnsignedShort()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(startPC)
        output.writeShort(length)
        output.writeShort(index)
    }

    companion object {
        internal fun read(input: ClassDataInput): LocalVarElement {
            val element = LocalVarElement()
            element.read(input)
            return element
        }
    }
}

data class CatchTargetInfo private constructor(override val type:                 TargetInfoType,
                                                private var _exceptionTableIndex: Int = -1): TargetInfo(type) {
    override val dataSize: Int
        get() = 3

    val exceptionTableIndex: Int
        get() = _exceptionTableIndex

    override fun readInfo(input: ClassDataInput) {
        _exceptionTableIndex = input.readUnsignedShort()
    }

    override fun writeInfo(output: ClassDataOutput) {
        output.writeShort(exceptionTableIndex)
    }

    override fun accept(classFile: ClassFile, visitor: TargetInfoVisitor) {
        visitor.visitCatchTargetInfo(classFile, this)
    }

    companion object {
        internal fun create(type: TargetInfoType): TargetInfo {
            return CatchTargetInfo(type)
        }
    }
}

data class OffsetTargetInfo private constructor(override val type:    TargetInfoType,
                                                 private var _offset: Int = -1): TargetInfo(type) {
    override val dataSize: Int
        get() = 3

    val offset: Int
        get() = _offset

    override fun readInfo(input: ClassDataInput) {
        _offset = input.readUnsignedShort()
    }

    override fun writeInfo(output: ClassDataOutput) {
        output.writeShort(offset)
    }

    override fun accept(classFile: ClassFile, visitor: TargetInfoVisitor) {
        visitor.visitOffsetTargetInfo(classFile, this)
    }

    companion object {
        internal fun create(type: TargetInfoType): TargetInfo {
            return OffsetTargetInfo(type)
        }
    }
}

data class TypeArgumentTargetInfo
    private constructor(override val type:               TargetInfoType,
                         private var _offset:            Int = -1,
                         private var _typeArgumentIndex: Int = -1): TargetInfo(type) {

    override val dataSize: Int
        get() = 4

    val offset: Int
        get() = _offset

    val typeArgumentIndex: Int
        get() = _typeArgumentIndex

    override fun readInfo(input: ClassDataInput) {
        _offset            = input.readUnsignedShort()
        _typeArgumentIndex = input.readUnsignedByte()
    }

    override fun writeInfo(output: ClassDataOutput) {
        output.writeShort(offset)
        output.writeByte(typeArgumentIndex)
    }

    override fun accept(classFile: ClassFile, visitor: TargetInfoVisitor) {
        visitor.visitTypeArgumentTargetInfo(classFile, this)
    }

    companion object {
        internal fun create(type: TargetInfoType): TargetInfo {
            return TypeArgumentTargetInfo(type)
        }
    }
}

enum class TargetInfoType constructor(val targetType: Int, private val supplier: (TargetInfoType) -> TargetInfo ) {
    CLASS_TYPE_PARAMETER                (0x00, TypeParameterTargetInfo.Companion::create),
    METHOD_TYPE_PARAMETER_TARGET        (0x01, TypeParameterTargetInfo.Companion::create),
    CLASS_EXTENDS                       (0x10, SuperTypeTargetInfo.Companion::create),
    CLASS_TYPE_PARAMETER_BOUND          (0x11, TypeParameterBoundTargetInfo.Companion::create),
    METHOD_TYPE_PARAMETER_BOUND         (0x12, TypeParameterBoundTargetInfo.Companion::create),
    FIELD                               (0x13, EmptyTargetInfo.Companion::create),
    METHOD_RETURN                       (0x14, EmptyTargetInfo.Companion::create),
    METHOD_RECEIVER                     (0x15, EmptyTargetInfo.Companion::create),
    METHOD_FORMAL_PARAMETER             (0x16, FormalParameterTargetInfo.Companion::create),
    THROWS                              (0x17, ThrowsTargetInfo.Companion::create),
    LOCAL_VARIABLE                      (0x40, LocalVarTargetInfo.Companion::create),
    RESOURCE_VARIABLE                   (0x41, LocalVarTargetInfo.Companion::create),
    EXCEPTION_PARAMETER                 (0x42, CatchTargetInfo.Companion::create),
    INSTANCE_OF                         (0x43, OffsetTargetInfo.Companion::create),
    NEW                                 (0x44, OffsetTargetInfo.Companion::create),
    CONSTRUCTOR_REFERENCE_RECEIVER      (0x45, OffsetTargetInfo.Companion::create),
    METHOD_REFERENCE_RECEIVER           (0x46, OffsetTargetInfo.Companion::create),
    CAST                                (0x47, TypeArgumentTargetInfo.Companion::create),
    CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT(0x48, TypeArgumentTargetInfo.Companion::create),
    METHOD_INVOCATION_TYPE_ARGUMENT     (0x49, TypeArgumentTargetInfo.Companion::create),
    CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT (0x4A, TypeArgumentTargetInfo.Companion::create),
    METHOD_REFERENCE_TYPE_ARGUMENT      (0x4B, TypeArgumentTargetInfo.Companion::create);

    companion object {
        private val targetTypeToTypeMap: Map<Int, TargetInfoType> by lazy {
            TargetInfoType.values().associateBy { it.targetType }
        }

        fun createTargetInfo(targetType: Int) : TargetInfo {
            val type = targetTypeToTypeMap[targetType]
            return type?.supplier?.invoke(type) ?: throw IllegalArgumentException("unknown targetType '$targetType'")
        }
    }
}
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

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.preverification.visitor.StackMapFrameVisitor
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.DataInput

abstract class StackMapFrame protected constructor(val frameType: Int) {
    internal abstract val type: StackMapFrameType

    abstract val offsetDelta: Int

    protected open fun readData(input: DataInput) {}

    abstract fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor)

    companion object {
        internal fun read(input: DataInput): StackMapFrame {
            val tag = input.readByte()

            val stackMapFrame = StackMapFrameType.of(tag)
            stackMapFrame.readData(input)
            return stackMapFrame
        }
    }
}

class SameFrame private constructor(frameType: Int): StackMapFrame(frameType) {
    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_FRAME

    override val offsetDelta: Int
        get() = frameType

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameFrame(classFile, this)
    }

    companion object {
        internal fun of(frameType: Int): SameFrame {
            require(frameType in 0 .. 63)
            return SameFrame(frameType)
        }
    }
}

class ChopFrame private constructor(            frameType:    Int,
                                    private var _offsetDelta: Int = 0): StackMapFrame(frameType) {
    override val type: StackMapFrameType
        get() = StackMapFrameType.CHOP_FRAME

    override val offsetDelta: Int
        get() = _offsetDelta

    val choppedVariables: Int
        get() = 251 - frameType

    override fun readData(input: DataInput) {
        _offsetDelta = input.readUnsignedShort()
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitChopFrame(classFile, this)
    }

    companion object {
        internal fun of(frameType: Int): ChopFrame {
            require(frameType in 248 .. 250)
            return ChopFrame(frameType)
        }
    }
}

class SameExtendedFrame private constructor(            frameType:    Int,
                                            private var _offsetDelta: Int = 0): StackMapFrame(frameType) {
    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_EXTENDED_FRAME

    override val offsetDelta: Int
        get() = _offsetDelta

    override fun readData(input: DataInput) {
        _offsetDelta = input.readUnsignedShort()
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameExtendedFrame(classFile, this)
    }

    companion object {
        internal fun of(frameType: Int): SameExtendedFrame {
            require(frameType == 251)
            return SameExtendedFrame(frameType)
        }
    }
}

class AppendFrame private constructor(            frameType:    Int,
                                      private var _offsetDelta: Int = 0,
                                      private var locals:       MutableList<VerificationType> = mutableListOfCapacity(0))
    : StackMapFrame(frameType), Sequence<VerificationType> {

    override val type: StackMapFrameType
        get() = StackMapFrameType.APPEND_FRAME

    override val offsetDelta: Int
        get() = _offsetDelta

    val appendedVariables: Int
        get() = frameType - 251

    val size: Int
        get() = locals.size

    operator fun get(index: Int): VerificationType {
        return locals[index]
    }

    override fun iterator(): Iterator<VerificationType> {
        return locals.iterator()
    }

    override fun readData(input: DataInput) {
        _offsetDelta = input.readUnsignedShort()
        locals = mutableListOfCapacity(appendedVariables)
        for (i in 0 until appendedVariables) {
            locals.add(VerificationType.read(input))
        }
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitAppendFrame(classFile, this)
    }

    companion object {
        internal fun of(frameType: Int): AppendFrame {
            require(frameType in 252 .. 254)
            return AppendFrame(frameType)
        }
    }
}

class SameLocalsOneStackItemFrame private constructor(            frameType: Int,
                                                      private var _stack:    VerificationType = TopVariable.empty())
    : StackMapFrame(frameType) {

    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_LOCALS_1_STACK_ITEM_FRAME

    override val offsetDelta: Int
        get() = frameType - 64

    val stackItem: VerificationType
        get() = _stack

    override fun readData(input: DataInput) {
        _stack = VerificationType.read(input)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameLocalsOneStackItemFrame(classFile, this)
    }

    companion object {
        internal fun of(frameType: Int): SameLocalsOneStackItemFrame {
            require(frameType in 64 .. 127)
            return SameLocalsOneStackItemFrame(frameType)
        }
    }
}

class SameLocalsOneStackItemExtendedFrame private constructor(            frameType:    Int,
                                                              private var _offsetDelta: Int = 0,
                                                              private var _stack:       VerificationType = TopVariable.empty())
    : StackMapFrame(frameType) {

    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_LOCALS_1_STACK_ITEM_EXTENDED_FRAME

    override val offsetDelta: Int
        get() = _offsetDelta

    val stackItem: VerificationType
        get() = _stack

    override fun readData(input: DataInput) {
        _offsetDelta = input.readUnsignedShort()
        _stack = VerificationType.read(input)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameLocalsOneStackItemExtendedFrame(classFile, this)
    }

    companion object {
        internal fun of(frameType: Int): SameLocalsOneStackItemExtendedFrame {
            require(frameType == 247)
            return SameLocalsOneStackItemExtendedFrame(frameType)
        }
    }
}

class FullFrame private constructor(            frameType:    Int,
                                    private var _offsetDelta: Int = 0,
                                    private var _locals:      MutableList<VerificationType> = mutableListOfCapacity(0),
                                    private var _stack:       MutableList<VerificationType> = mutableListOfCapacity(0))
    : StackMapFrame(frameType) {

    override val type: StackMapFrameType
        get() = StackMapFrameType.FULL_FRAME

    override val offsetDelta: Int
        get() = _offsetDelta

    val locals: List<VerificationType>
        get() = _locals

    val stack: List<VerificationType>
        get() = _stack

    override fun readData(input: DataInput) {
        _offsetDelta = input.readUnsignedShort()
        val numberOfLocals = input.readUnsignedShort()
        _locals = mutableListOfCapacity(numberOfLocals)
        for (i in 0 until numberOfLocals) {
            _locals.add(VerificationType.read(input))
        }
        val numberOfStackItems = input.readUnsignedShort()
        _stack = mutableListOfCapacity(numberOfStackItems)
        for (i in 0 until numberOfStackItems) {
            _stack.add(VerificationType.read(input))
        }
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitFullFrame(classFile, this)
    }

    companion object {
        internal fun of(frameType: Int): FullFrame {
            require(frameType == 255)
            return FullFrame(frameType)
        }
    }
}

internal enum class StackMapFrameType constructor(private val supplier: (Int) -> StackMapFrame) {
    SAME_FRAME                             (SameFrame.Companion::of),
    SAME_LOCALS_1_STACK_ITEM_FRAME         (SameLocalsOneStackItemFrame.Companion::of),
    SAME_LOCALS_1_STACK_ITEM_EXTENDED_FRAME(SameLocalsOneStackItemExtendedFrame.Companion::of),
    CHOP_FRAME                             (ChopFrame.Companion::of),
    SAME_EXTENDED_FRAME                    (SameExtendedFrame.Companion::of),
    APPEND_FRAME                           (AppendFrame.Companion::of),
    FULL_FRAME                             (FullFrame.Companion::of);

    companion object {
        fun of(tag: Byte) : StackMapFrame {
            val tagAsInt = tag.toInt() and 0xff

            val type = when (tagAsInt) {
                in 0 .. 63    -> SAME_FRAME
                in 64 .. 127  -> SAME_LOCALS_1_STACK_ITEM_FRAME
                247           -> SAME_LOCALS_1_STACK_ITEM_EXTENDED_FRAME
                in 248 .. 250 -> CHOP_FRAME
                251           -> SAME_EXTENDED_FRAME
                in 252 .. 254 -> APPEND_FRAME
                255           -> FULL_FRAME
                else -> throw IllegalStateException("unexpected frameType tag $tagAsInt")
            }

            return type.supplier(tagAsInt)
        }
    }
}

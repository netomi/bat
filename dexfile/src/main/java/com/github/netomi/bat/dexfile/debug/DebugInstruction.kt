/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.dexfile.debug

import com.github.netomi.bat.dexfile.DexContent
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.NO_INDEX
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.debug.visitor.DebugSequenceVisitor
import com.github.netomi.bat.dexfile.visitor.PropertyAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import java.util.*
import kotlin.collections.HashMap

/**
 * Represents a debug instruction as contained in the debug sequence of a debug info item.
 */
abstract class DebugInstruction protected constructor(val opcode: Byte) : DexContent() {

    fun writeInternal(output: DexDataOutput) {
        write(output)
    }

    abstract fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor)

    internal open fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {}

    companion object {
        const val DBG_END_SEQUENCE:         Byte = 0x00
        const val DBG_ADVANCE_PC:           Byte = 0x01
        const val DBG_ADVANCE_LINE:         Byte = 0x02
        const val DBG_START_LOCAL:          Byte = 0x03
        const val DBG_START_LOCAL_EXTENDED: Byte = 0x04
        const val DBG_END_LOCAL:            Byte = 0x05
        const val DBG_RESTART_LOCAL:        Byte = 0x06
        const val DBG_SET_PROLOGUE_END:     Byte = 0x07
        const val DBG_SET_EPILOGUE_BEGIN:   Byte = 0x08
        const val DBG_SET_FILE:             Byte = 0x09
        const val DBG_FIRST_SPECIAL:        Byte = 0x0a

        const val DBG_LINE_BASE  = -4
        const val DBG_LINE_RANGE = 15

        fun readInstruction(input: DexDataInput): DebugInstruction {
            val opCode = input.readByte()
            val debugInstruction = create(opCode)
            debugInstruction.read(input)
            return debugInstruction
        }

        private fun create(opCode: Byte): DebugInstruction {
            return when (opCode) {
                DBG_END_SEQUENCE         -> DebugEndSequence
                DBG_ADVANCE_PC           -> DebugAdvancePC.empty()
                DBG_ADVANCE_LINE         -> DebugAdvanceLine.empty()
                DBG_START_LOCAL          -> DebugStartLocal.empty()
                DBG_START_LOCAL_EXTENDED -> DebugStartLocalExtended.empty()
                DBG_END_LOCAL            -> DebugEndLocal.empty()
                DBG_RESTART_LOCAL        -> DebugRestartLocal.empty()
                DBG_SET_PROLOGUE_END     -> DebugSetPrologueEnd
                DBG_SET_EPILOGUE_BEGIN   -> DebugSetEpilogueBegin
                DBG_SET_FILE             -> DebugSetFile.empty()
                else                     -> DebugAdvanceLineAndPC.of(opCode)
            }
        }
    }
}

/**
 * Represents a debug instruction that advances the line register.
 */
data class DebugAdvanceLine private constructor(private var _lineDiff: Int = 0): DebugInstruction(DBG_ADVANCE_LINE) {

    val lineDiff: Int
        get() = _lineDiff

    override fun read(input: DexDataInput) {
        _lineDiff = input.readSleb128()
    }

    override fun write(output: DexDataOutput) {
        output.writeByte(opcode)
        output.writeSleb128(_lineDiff)
    }

    override fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor) {
        visitor.visitAdvanceLine(dexFile, debugInfo, this)
    }

    override fun toString(): String {
        return "DebugAdvanceLine[lineDiff=${_lineDiff}]"
    }

    companion object {
        internal fun empty(): DebugAdvanceLine {
            return DebugAdvanceLine()
        }

        fun of(lineDiff: Int): DebugAdvanceLine {
            return DebugAdvanceLine(lineDiff)
        }
    }
}

/**
 * Represents a debug instruction that advances the line and address registers.
 */
class DebugAdvanceLineAndPC private constructor(opCode: Byte) : DebugInstruction(opCode) {

    var lineDiff = 0
        private set

    var addrDiff = 0
        private set

    init {
        val adjustedOpCode = (opcode.toInt() and 0xff) - DBG_FIRST_SPECIAL
        lineDiff = DBG_LINE_BASE + adjustedOpCode % DBG_LINE_RANGE
        addrDiff = adjustedOpCode / DBG_LINE_RANGE
    }

    override fun read(input: DexDataInput) {}

    override fun write(output: DexDataOutput) {
        output.writeByte(opcode)
    }

    override fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor) {
        visitor.visitAdvanceLineAndPC(dexFile, debugInfo, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val o = other as DebugAdvanceLineAndPC

        return opcode == o.opcode
    }

    override fun hashCode(): Int {
        return opcode.hashCode()
    }

    override fun toString(): String {
        return "DebugAdvanceLineAndPC[lineDiff=${lineDiff},addrDiff=${addrDiff}]"
    }

    companion object {
        private val possibleCombinations: Map<LineAddrPair, Int> by lazy {
            val result = HashMap<LineAddrPair, Int>()

            val opCodeRange = 0x0a..0xff
            for (opCode in opCodeRange) {
                val insn = DebugAdvanceLineAndPC(opCode.toByte())
                result[LineAddrPair(insn.lineDiff, insn.addrDiff)] = opCode
            }

            result
        }

        internal fun of(opCode: Byte): DebugAdvanceLineAndPC {
            return DebugAdvanceLineAndPC(opCode)
        }

        fun nop(): DebugAdvanceLineAndPC {
            return createIfPossible(0, 0)!!
        }

        fun createIfPossible(lineDiff: Int, addrDiff: Int): DebugAdvanceLineAndPC? {
            val opCode = possibleCombinations[LineAddrPair(lineDiff, addrDiff)]
            return if (opCode != null) {
                DebugAdvanceLineAndPC(opCode.toByte())
            } else {
                null
            }
        }
    }
}

private data class LineAddrPair(val lineDiff: Int, val addrDiff: Int)

/**
 * Represents a debug instruction that advances the address register.
 */
data class DebugAdvancePC private constructor(private var _addrDiff: Int = 0) : DebugInstruction(DBG_ADVANCE_PC) {

    val addrDiff: Int
        get() = _addrDiff

    override fun read(input: DexDataInput) {
        _addrDiff = input.readUleb128()
    }

    override fun write(output: DexDataOutput) {
        output.writeByte(opcode)
        output.writeUleb128(_addrDiff)
    }

    override fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor) {
        visitor.visitAdvancePC(dexFile, debugInfo, this)
    }

    override fun toString(): String {
        return "DebugAdvancePC[addrDiff=${_addrDiff}]"
    }

    companion object {
        internal fun empty(): DebugAdvancePC {
            return DebugAdvancePC()
        }

        fun of(addrDiff: Int): DebugAdvancePC {
            return DebugAdvancePC(addrDiff)
        }
    }
}

/**
 * Represents a debug instruction that ends a local variable at the current address.
 */
data class DebugEndLocal private constructor(private var _registerNum: Int = 0) : DebugInstruction(DBG_END_LOCAL) {

    val registerNum: Int
        get() = _registerNum

    override fun read(input: DexDataInput) {
        _registerNum = input.readUleb128()
    }

    override fun write(output: DexDataOutput) {
        output.writeByte(opcode)
        output.writeUleb128(_registerNum)
    }

    override fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor) {
        visitor.visitEndLocal(dexFile, debugInfo, this)
    }

    override fun toString(): String {
        return "DebugEndLocal[registerNum=${_registerNum}]"
    }

    companion object {
        internal fun empty(): DebugEndLocal {
            return DebugEndLocal()
        }

        fun of(registerNum: Int): DebugEndLocal {
            return DebugEndLocal(registerNum)
        }
    }
}

/**
 * Represents a debug instruction that ends a debug sequence of a debug info item.
 */
object DebugEndSequence : DebugInstruction(DBG_END_SEQUENCE) {

    override fun read(input: DexDataInput) {}

    override fun write(output: DexDataOutput) {
        output.writeByte(opcode)
    }

    override fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor) {
        visitor.visitEndSequence(dexFile, debugInfo, this)
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return "DebugEndSequence[]"
    }
}

/**
 * Represents a debug instruction that restarts a previously defined local variable at the current address.
 */
data class DebugRestartLocal private constructor(private var _registerNum: Int = 0) : DebugInstruction(DBG_RESTART_LOCAL) {

    val registerNum: Int
        get() = _registerNum

    override fun read(input: DexDataInput) {
        _registerNum = input.readUleb128()
    }

    override fun write(output: DexDataOutput) {
        output.writeByte(opcode)
        output.writeUleb128(_registerNum)
    }

    override fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor) {
        visitor.visitRestartLocal(dexFile, debugInfo, this)
    }

    override fun toString(): String {
        return "DebugRestartLocal[registerNum=${_registerNum}]"
    }

    companion object {
        internal fun empty(): DebugRestartLocal {
            return DebugRestartLocal()
        }

        fun of(registerNum: Int): DebugRestartLocal {
            return DebugRestartLocal(registerNum)
        }
    }
}

/**
 * Represents a debug instruction that sets the epilogue begin state machine register.
 */
object DebugSetEpilogueBegin : DebugInstruction(DBG_SET_EPILOGUE_BEGIN) {

    override fun read(input: DexDataInput) {}

    override fun write(output: DexDataOutput) {
        output.writeByte(opcode)
    }

    override fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor) {
        visitor.visitSetEpilogueBegin(dexFile, debugInfo, this)
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return "DebugSetEpilogueBegin[]"
    }
}

/**
 * Represents a debug instruction that sets associated source file for subsequent line number entries.
 */
data class DebugSetFile private constructor(private var _nameIndex: Int = 0) : DebugInstruction(DBG_SET_FILE) {

    val nameIndex: Int
        get() = _nameIndex

    fun getName(dexFile: DexFile): String? {
        return dexFile.getStringNullable(nameIndex)
    }

    override fun read(input: DexDataInput) {
        _nameIndex = input.readUleb128p1()
    }

    override fun write(output: DexDataOutput) {
        output.writeByte(opcode)
        output.writeUleb128p1(nameIndex)
    }

    override fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor) {
        visitor.visitSetFile(dexFile, debugInfo, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        if (nameIndex != NO_INDEX) {
            visitor.visitStringID(dexFile, PropertyAccessor({ _nameIndex }, { _nameIndex = it }))
        }
    }

    override fun toString(): String {
        return "DebugSetFile[nameIndex=${nameIndex}]"
    }

    companion object {
        internal fun empty(): DebugSetFile {
            return DebugSetFile()
        }

        fun of(nameIndex: Int): DebugSetFile {
            return DebugSetFile(nameIndex)
        }
    }
}

/**
 * Represents a debug instruction that sets the prologue end state machine register.
 */
object DebugSetPrologueEnd : DebugInstruction(DBG_SET_PROLOGUE_END) {

    override fun read(input: DexDataInput) {}

    override fun write(output: DexDataOutput) {
        output.writeByte(opcode)
    }

    override fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor) {
        visitor.visitSetPrologueEnd(dexFile, debugInfo, this)
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return "DebugSetPrologueEnd[]"
    }
}

/**
 * Represents a debug instruction that starts a local variable at the current address.
 */
open class DebugStartLocal : DebugInstruction {

    var registerNum = 0
        protected set

    var nameIndex = 0
        internal set

    var typeIndex = 0
        internal set

    private constructor() : this(DBG_START_LOCAL)

    protected constructor(opCode: Byte) : super(opCode)

    protected constructor(opCode: Byte, registerNum: Int, nameIndex: Int, typeIndex: Int) : super(opCode) {
        this.registerNum = registerNum
        this.nameIndex   = nameIndex
        this.typeIndex   = typeIndex
    }

    fun getName(dexFile: DexFile): String? {
        return dexFile.getStringNullable(nameIndex)
    }

    fun getType(dexFile: DexFile): String? {
        return dexFile.getTypeNullable(typeIndex)
    }

    override fun read(input: DexDataInput) {
        registerNum = input.readUleb128()
        nameIndex   = input.readUleb128p1()
        typeIndex   = input.readUleb128p1()
    }

    override fun write(output: DexDataOutput) {
        output.writeByte(opcode)
        output.writeUleb128(registerNum)
        output.writeUleb128p1(nameIndex)
        output.writeUleb128p1(typeIndex)
    }

    override fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor) {
        visitor.visitStartLocal(dexFile, debugInfo, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        if (nameIndex != NO_INDEX) {
            visitor.visitStringID(dexFile, PropertyAccessor({ nameIndex }, { nameIndex = it }))
        }

        if (typeIndex != NO_INDEX) {
            visitor.visitTypeID(dexFile, PropertyAccessor({ typeIndex }, { typeIndex = it }))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val o = other as DebugStartLocal

        return registerNum == o.registerNum &&
               nameIndex   == o.nameIndex   &&
               typeIndex   == o.typeIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(registerNum, nameIndex, typeIndex)
    }

    override fun toString(): String {
        return "DebugStartLocal[registerNum=${registerNum},nameIndex=${nameIndex},typeIndex=${typeIndex}]"
    }

    companion object {
        internal fun empty(): DebugStartLocal {
            return DebugStartLocal()
        }

        fun of(registerNum: Int, nameIndex: Int, typeIndex: Int): DebugStartLocal {
            return DebugStartLocal(DBG_START_LOCAL, registerNum, nameIndex, typeIndex)
        }
    }
}

/**
 * Represents a debug instruction that starts a local variable at the current address with extended information.
 */
class DebugStartLocalExtended : DebugStartLocal {

    var sigIndex = 0
        internal set

    private constructor() : super(DBG_START_LOCAL_EXTENDED)

    private constructor(registerNum: Int, nameIndex: Int, typeIndex: Int, sigIndex: Int) : super(DBG_START_LOCAL_EXTENDED, registerNum, nameIndex, typeIndex) {
        this.sigIndex = sigIndex
    }

    fun getSignature(dexFile: DexFile): String? {
        return dexFile.getStringNullable(sigIndex)
    }

    override fun read(input: DexDataInput) {
        super.read(input)
        sigIndex = input.readUleb128p1()
    }

    override fun write(output: DexDataOutput) {
        super.write(output)
        output.writeUleb128p1(sigIndex)
    }

    override fun accept(dexFile: DexFile, debugInfo: DebugInfo, visitor: DebugSequenceVisitor) {
        visitor.visitStartLocalExtended(dexFile, debugInfo, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        super.referencedIDsAccept(dexFile, visitor)

        if (sigIndex != NO_INDEX) {
            visitor.visitStringID(dexFile, PropertyAccessor({ sigIndex }, { sigIndex = it }))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false

        val o = other as DebugStartLocalExtended

        return sigIndex == o.sigIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), sigIndex)
    }

    override fun toString(): String {
        return "DebugStartLocalExtended[registerNum=${registerNum},nameIndex=${nameIndex},typeIndex=${typeIndex},sigIndex=${sigIndex}]"
    }

    companion object {
        internal fun empty(): DebugStartLocalExtended {
            return DebugStartLocalExtended()
        }

        fun of(registerNum: Int, nameIndex: Int, typeIndex: Int, sigIndex: Int): DebugStartLocalExtended {
            return DebugStartLocalExtended(registerNum, nameIndex, typeIndex, sigIndex)
        }
    }
}

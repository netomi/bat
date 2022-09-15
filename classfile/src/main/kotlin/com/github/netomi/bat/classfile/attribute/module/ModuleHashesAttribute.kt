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

package com.github.netomi.bat.classfile.attribute.module

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.AttachedToClass
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import com.github.netomi.bat.classfile.io.contentSize
import com.github.netomi.bat.util.mutableListOfCapacity
import java.util.*

/**
 * A class representing a ModuleHashes attribute in a class file.
 */
data class ModuleHashesAttribute
    private constructor(override var attributeNameIndex:  Int,
                         private var _hashAlgorithmIndex: Int                          = -1,
                         private var _moduleHashes:       MutableList<ModuleHashEntry> = mutableListOfCapacity(0)
    ): Attribute(attributeNameIndex), AttachedToClass, Sequence<ModuleHashEntry> {

    override val type: AttributeType
        get() = AttributeType.MODULE_HASHES

    override val dataSize: Int
        get() = 2 + _moduleHashes.contentSize()

    val hashAlgorithmIndex: Int
        get() = _hashAlgorithmIndex

    fun getHashAlgorithm(classFile: ClassFile): String {
        return classFile.getString(_hashAlgorithmIndex)
    }

    val size: Int
        get() = _moduleHashes.size

    operator fun get(index: Int): ModuleHashEntry {
        return _moduleHashes[index]
    }

    override fun iterator(): Iterator<ModuleHashEntry> {
        return _moduleHashes.iterator()
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        _hashAlgorithmIndex = input.readUnsignedShort()
        _moduleHashes = input.readContentList(ModuleHashEntry::read)
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeShort(_hashAlgorithmIndex)
        output.writeContentList(_moduleHashes)
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitModuleHashes(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        super.referencedConstantsAccept(classFile, visitor)
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_hashAlgorithmIndex))
        for (entry in _moduleHashes) {
            entry.referencedConstantsAccept(classFile, visitor)
        }
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): ModuleHashesAttribute {
            return ModuleHashesAttribute(attributeNameIndex)
        }
    }
}

data class ModuleHashEntry
    private constructor(private var _moduleIndex: Int = -1,
                        private var _hash:        ByteArray = ByteArray(0)): ClassFileContent() {

    override val contentSize: Int
        get() = 4 + _hash.size

    val moduleIndex: Int
        get() = _moduleIndex

    fun getModuleName(classFile: ClassFile): String {
        return classFile.getModule(moduleIndex).getModuleName(classFile)
    }

    val hash: ByteArray
        get() = _hash

    private fun read(input: ClassDataInput) {
        _moduleIndex   = input.readUnsignedShort()
        val hashLength = input.readUnsignedShort()
        _hash = ByteArray(hashLength)
        input.readFully(_hash)
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(_moduleIndex)
        output.writeShort(_hash.size)
        output.write(_hash)
    }

    fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitModuleConstant(classFile, this, PropertyAccessor(::_moduleIndex))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModuleHashEntry) return false

        return _moduleIndex == other._moduleIndex &&
               _hash.contentEquals(other._hash)
    }

    override fun hashCode(): Int {
        return Objects.hash(_moduleIndex, _hash.contentHashCode())
    }

    companion object {
        internal fun read(input: ClassDataInput): ModuleHashEntry {
            val entry = ModuleHashEntry()
            entry.read(input)
            return entry
        }
    }
}
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
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.constant.ModuleConstant
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.DataInput
import java.io.DataOutput

/**
 * A class representing a Module attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.25">Module Attribute</a>
 */
data class ModuleAttribute
    private constructor(override val attributeNameIndex:  Int,
                         private var _moduleNameIndex:    Int = -1,
                         private var _moduleFlags:        Int =  0,
                         private var _moduleVersionIndex: Int = -1,
                         private var _requiresList:       MutableList<RequiresElement> = mutableListOfCapacity(0),
                         private var _exportsList:        MutableList<ExportsElement>  = mutableListOfCapacity(0),
                         private var _opensList:          MutableList<OpensElement>    = mutableListOfCapacity(0),
                         private var _uses:               UsesElement                  = UsesElement.empty(),
                         private var _providesList:       MutableList<ProvidesElement> = mutableListOfCapacity(0)
    ): Attribute(attributeNameIndex), AttachedToClass {

    override val type: AttributeType
        get() = AttributeType.MODULE

    override val dataSize: Int
        get() {
            return 14 +
                   requires.fold(0) { acc, element -> acc + element.dataSize } +
                   exports.fold(0) { acc, element -> acc + element.dataSize } +
                   opens.fold(0) { acc, element -> acc + element.dataSize } +
                   provides.fold(0) { acc, element -> acc + element.dataSize } +
                   uses.dataSize
        }

    val moduleNameIndex: Int
        get() = _moduleNameIndex

    val moduleFlags: Int
        get() = _moduleFlags

    val moduleVersionIndex: Int
        get() = _moduleVersionIndex

    fun getModule(classFile: ClassFile): ModuleConstant {
        return classFile.getModule(moduleNameIndex)
    }

    fun getModuleName(classFile: ClassFile): String {
        return getModule(classFile).getModuleName(classFile)
    }

    fun getModuleVersion(classFile: ClassFile): String {
        return classFile.getString(moduleVersionIndex)
    }

    val requires: List<RequiresElement>
        get() = _requiresList

    val exports: List<ExportsElement>
        get() = _exportsList

    val opens: List<OpensElement>
        get() = _opensList

    val uses: UsesElement
        get() = _uses

    val provides: List<ProvidesElement>
        get() = _providesList

    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        @Suppress("UNUSED_VARIABLE")
        val length = input.readInt()
        _moduleNameIndex    = input.readUnsignedShort()
        _moduleFlags        = input.readUnsignedShort()
        _moduleVersionIndex = input.readUnsignedShort()

        val requiresCount = input.readUnsignedShort()
        _requiresList = mutableListOfCapacity(requiresCount)
        for (i in 0 until requiresCount) {
            _requiresList.add(RequiresElement.read(input))
        }

        val exportsCount = input.readUnsignedShort()
        _exportsList = mutableListOfCapacity(exportsCount)
        for (i in 0 until exportsCount) {
            _exportsList.add(ExportsElement.read(input))
        }

        val opensToCount = input.readUnsignedShort()
        _opensList = mutableListOfCapacity(opensToCount)
        for (i in 0 until opensToCount) {
            _opensList.add(OpensElement.read(input))
        }

        _uses = UsesElement.read(input)

        val providesWithCount = input.readUnsignedShort()
        _providesList = mutableListOfCapacity(providesWithCount)
        for (i in 0 until providesWithCount) {
            _providesList.add(ProvidesElement.read(input))
        }
    }

    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(dataSize)

        output.writeShort(_moduleNameIndex)
        output.writeShort(_moduleFlags)
        output.writeShort(_moduleVersionIndex)

        output.writeShort(requires.size)
        for (element in requires) {
            element.write(output)
        }

        output.writeShort(exports.size)
        for (element in exports) {
            element.write(output)
        }

        output.writeShort(opens.size)
        for (element in opens) {
            element.write(output)
        }

        uses.write(output)

        output.writeShort(provides.size)
        for (element in provides) {
            element.write(output)
        }
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitModuleAttribute(classFile, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): ModuleAttribute {
            return ModuleAttribute(attributeNameIndex)
        }
    }
}

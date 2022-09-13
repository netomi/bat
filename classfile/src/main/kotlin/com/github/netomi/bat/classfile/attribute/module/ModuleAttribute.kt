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

import com.github.netomi.bat.classfile.*
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.contentSize
import com.github.netomi.bat.util.mutableListOfCapacity

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
        get() = 6                      +
                requires.contentSize() +
                exports.contentSize()  +
                opens.contentSize()    +
                provides.contentSize() +
                uses.contentSize

    val moduleNameIndex: Int
        get() = _moduleNameIndex

    val moduleFlags: Int
        get() = _moduleFlags

    val moduleFlagsAsSet: Set<AccessFlag>
        get() = accessFlagsToSet(moduleFlags, AccessFlagTarget.MODULE)

    val moduleVersionIndex: Int
        get() = _moduleVersionIndex

    fun getModuleName(classFile: ClassFile): String {
        return classFile.getModule(moduleNameIndex).getModuleName(classFile)
    }

    fun getModuleVersion(classFile: ClassFile): String? {
        return classFile.getStringOrNull(moduleVersionIndex)
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

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        _moduleNameIndex    = input.readUnsignedShort()
        _moduleFlags        = input.readUnsignedShort()
        _moduleVersionIndex = input.readUnsignedShort()

        _requiresList = input.readContentList(RequiresElement.Companion::read)
        _exportsList  = input.readContentList(ExportsElement.Companion::read)
        _opensList    = input.readContentList(OpensElement.Companion::read)
        _uses         = UsesElement.read(input)
        _providesList = input.readContentList(ProvidesElement.Companion::read)
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeShort(_moduleNameIndex)
        output.writeShort(_moduleFlags)
        output.writeShort(_moduleVersionIndex)

        output.writeContentList(requires)
        output.writeContentList(exports)
        output.writeContentList(opens)
        uses.write(output)
        output.writeContentList(provides)
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

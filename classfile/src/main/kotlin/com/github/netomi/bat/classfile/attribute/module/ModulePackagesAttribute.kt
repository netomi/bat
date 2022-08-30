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
import com.github.netomi.bat.classfile.constant.PackageConstant
import java.io.DataInput
import java.io.DataOutput
import java.util.*

/**
 * A class representing a ModulePackages attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.26">ModulePackages Attribute</a>
 */
data class ModulePackagesAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var packages:           IntArray = IntArray(0)
    ): Attribute(attributeNameIndex), AttachedToClass, Sequence<Int> {

    override val type: AttributeType
        get() = AttributeType.MODULE_PACKAGES

    override val dataSize: Int
        get() = 2 + packages.size * 2

    val size: Int
        get() = packages.size

    operator fun get(index: Int): Int {
        return packages[index]
    }

    override fun iterator(): Iterator<Int> {
        return packages.iterator()
    }

    fun getPackageNames(classFile: ClassFile): List<String> {
        return packages.map { (classFile.getConstant(it) as PackageConstant).getPackageName(classFile) }
    }

    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        @Suppress("UNUSED_VARIABLE")
        val length = input.readInt()
        val numberOfClasses = input.readUnsignedShort()
        packages = IntArray(numberOfClasses)
        for (i in 0 until numberOfClasses) {
            packages[i] = input.readUnsignedShort()
        }
    }

    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(dataSize)
        output.writeShort(packages.size)
        for (packageIndex in packages) {
            output.writeShort(packageIndex)
        }
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitModulePackages(classFile, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModulePackagesAttribute) return false

        return attributeNameIndex == other.attributeNameIndex &&
               packages.contentEquals(other.packages)
    }

    override fun hashCode(): Int {
        return Objects.hash(attributeNameIndex, packages.contentHashCode())
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): ModulePackagesAttribute {
            return ModulePackagesAttribute(attributeNameIndex)
        }
    }
}
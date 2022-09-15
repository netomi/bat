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
import com.github.netomi.bat.classfile.constant.visitor.ArrayElementAccessor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import java.util.*

/**
 * A class representing a ModulePackages attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.26">ModulePackages Attribute</a>
 */
data class ModulePackagesAttribute
    private constructor(override var attributeNameIndex: Int,
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
        return packages.map { classFile.getPackage(it).getPackageName(classFile) }
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        packages = input.readShortIndexArray()
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeShortIndexArray(packages)
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitModulePackages(classFile, this)
    }

    fun packagesAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        for (constantIndex in packages) {
            classFile.constantAccept(constantIndex, visitor)
        }
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        super.referencedConstantsAccept(classFile, visitor)

        for (i in packages.indices) {
            visitor.visitPackageConstant(classFile, this, ArrayElementAccessor(packages, i))
        }
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
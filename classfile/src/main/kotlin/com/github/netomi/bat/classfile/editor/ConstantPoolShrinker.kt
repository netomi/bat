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

package com.github.netomi.bat.classfile.editor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.constant.ConstantPool
import com.github.netomi.bat.classfile.constant.visitor.IDAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor

class ConstantPoolShrinker: ClassFileVisitor {

    override fun visitClassFile(classFile: ClassFile) {
        val usageMarker = UsageMarker()
        classFile.referencedConstantsAccept(false, ReferencedConstantsMarker(usageMarker))

        val mapping = IntArray(classFile.constantPoolSize) { -1 }
        val shrunkConstantPool = ConstantPool.empty()

        classFile.constantsAccept { _, oldIndex, constant ->
            if (usageMarker.isUsed(constant)) {
                val newIndex      = shrunkConstantPool.addConstant(constant)
                mapping[oldIndex] = newIndex
            }
        }

        classFile.constantPool = shrunkConstantPool

        // update the new constant indices in all items of the class file.
        classFile.referencedConstantsAccept(true) { _, owner, accessor ->
            val constantIndex = accessor.get()
            val newIndex = mapping[constantIndex]
            if (newIndex < 0) {
                error("no mapping found for constant index '$constantIndex' referenced from '$owner")
            } else {
                accessor.set(newIndex)
            }
        }
    }
}

private class ReferencedConstantsMarker constructor(val usageMarker: UsageMarker): ReferencedConstantVisitor {
    override fun visitAnyConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        val constantIndex = accessor.get()
        val constant = classFile.getConstant(constantIndex)
        usageMarker.markUsed(constant)
        // recursively mark referenced constants
        constant.referencedConstantsAccept(classFile, this)
    }
}

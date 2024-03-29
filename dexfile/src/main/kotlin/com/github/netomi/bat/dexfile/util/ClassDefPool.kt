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
package com.github.netomi.bat.dexfile.util

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.visitor.ClassDefVisitor
import com.github.netomi.bat.util.asInternalClassName
import java.util.*

fun classDefPoolFiller(pool: ClassDefPool): ClassDefVisitor {
    return ClassDefVisitor { dexFile, classDef -> pool.addClassDef(dexFile, classDef) }
}

class ClassDefPool private constructor() {
    private val classDefMap: SortedMap<String, ClassDefData> = TreeMap()

    val size: Int
        get() = classDefMap.size

    fun addClassDef(dexFile: DexFile, classDef: ClassDef) {
        val classType = classDef.getType(dexFile)
        classDefMap.computeIfAbsent(classType.type) { ClassDefData(dexFile, classDef) }
    }

    fun getClassDefByType(type: DexType): ClassDefData? {
        return getClassDefByType(type.type)
    }

    fun getClassDefByType(type: String): ClassDefData? {
        return classDefMap[type]
    }

    fun getClassDefByClassName(internalClassName: String): ClassDefData? {
        val internalType = internalClassName.asInternalClassName().toInternalType()
        return classDefMap[internalType]
    }

    fun classDefsAccept(visitor: ClassDefVisitor) {
        for ((_, data) in classDefMap) {
            visitor.visitClassDef(data.dexFile, data.classDef)
        }
    }

    companion object {
        fun empty(): ClassDefPool {
            return ClassDefPool()
        }
    }

    // Inner helper classes.
    inner class ClassDefData internal constructor(val dexFile: DexFile, val classDef: ClassDef)
}
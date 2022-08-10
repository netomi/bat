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
import com.github.netomi.bat.dexfile.util.DexClasses.internalTypeFromInternalClassName
import com.github.netomi.bat.dexfile.visitor.ClassDefVisitor
import java.util.*

fun classDefPoolFiller(pool: ClassDefPool): ClassDefVisitor {
    return ClassDefVisitor { dexFile, index, classDef -> pool.addClassDef(dexFile, index, classDef) }
}

class ClassDefPool private constructor() {
    private val classDefMap: SortedMap<String, ClassDefData> = TreeMap()

    val size: Int
        get() = classDefMap.size

    fun addClassDef(dexFile: DexFile, index: Int, classDef: ClassDef) {
        val className = classDef.getType(dexFile)
        classDefMap.computeIfAbsent(className) { ClassDefData(dexFile, index, classDef) }
    }

    fun getClassDefByType(type: String): ClassDefData? {
        return classDefMap[type]
    }

    fun getClassDefByClassName(className: String): ClassDefData? {
        return classDefMap[internalTypeFromInternalClassName(className)]
    }

    fun classDefsAccept(visitor: ClassDefVisitor) {
        for ((_, data) in classDefMap) {
            visitor.visitClassDef(data.dexFile, data.index, data.classDef)
        }
    }

    companion object {
        fun empty(): ClassDefPool {
            return ClassDefPool()
        }
    }

    // Inner helper classes.
    inner class ClassDefData internal constructor(val dexFile: DexFile, val index: Int, val classDef: ClassDef)
}
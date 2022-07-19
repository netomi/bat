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
package com.github.netomi.bat.dexfile.visitor

import com.github.netomi.bat.dexfile.ClassData
import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.value.visitor.multiValueVisitorOf

fun multiClassDataVisitorOf(visitor: ClassDataVisitor, vararg visitors: ClassDataVisitor): ClassDataVisitor {
    return MultiClassDataVisitor(visitor, *visitors)
}

fun allStaticFields(visitor: EncodedFieldVisitor): ClassDataVisitor {
    return ClassDataVisitor { dexFile, classDef, classData ->
        classData.staticFieldsAccept(dexFile, classDef, visitor)
    }
}

fun allInstanceFields(visitor: EncodedFieldVisitor): ClassDataVisitor {
    return ClassDataVisitor { dexFile, classDef, classData ->
        classData.instanceFieldsAccept(dexFile, classDef, visitor)
    }
}

fun allFields(visitor: EncodedFieldVisitor): ClassDataVisitor {
    return allStaticFields(visitor).andThen(
           allInstanceFields(visitor))
}

fun allDirectMethods(visitor: EncodedMethodVisitor): ClassDataVisitor {
    return ClassDataVisitor { dexFile, classDef, classData ->
        classData.directMethodsAccept(dexFile, classDef, visitor)
    }
}

fun allVirtualMethods(visitor: EncodedMethodVisitor): ClassDataVisitor {
    return ClassDataVisitor { dexFile, classDef, classData ->
        classData.virtualMethodsAccept(dexFile, classDef, visitor)
    }
}

fun allMethods(visitor: EncodedMethodVisitor): ClassDataVisitor {
    return allDirectMethods(visitor).andThen(
           allVirtualMethods(visitor))
}

fun interface ClassDataVisitor {
    fun visitClassData(dexFile: DexFile, classDef: ClassDef, classData: ClassData)

    fun andThen(vararg visitors: ClassDataVisitor): ClassDataVisitor {
        return multiClassDataVisitorOf(this, *visitors)
    }
}

private class MultiClassDataVisitor constructor(       visitor:       ClassDataVisitor,
                                                vararg otherVisitors: ClassDataVisitor)

    : AbstractMultiVisitor<ClassDataVisitor>(visitor, *otherVisitors), ClassDataVisitor {

    override fun visitClassData(dexFile: DexFile, classDef: ClassDef, classData: ClassData) {
        for (visitor in visitors) {
            visitor.visitClassData(dexFile, classDef, classData)
        }
    }
}
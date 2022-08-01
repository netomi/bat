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

package com.github.netomi.bat.dexfile.editor

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.util.DexClasses
import java.util.*

class DexEditor private constructor(val dexFile: DexFile) {

    fun addOrGetStringIDIndex(string: String): Int {
        var index = dexFile.getStringIDIndex(string)
        if (index == NO_INDEX) {
            index = dexFile.addStringID(StringID.of(string))
        }
        return index
    }

    fun addOrGetTypeIDIndex(type: String): Int {
        var index = dexFile.getTypeIDIndex(type)
        if (index == NO_INDEX) {
            index = dexFile.addTypeID(TypeID.of(addOrGetStringIDIndex(type)))
        }
        return index
    }

    fun addOrGetProtoIDIndex(parameterTypes: List<String>, returnType: String): Int {
        val shorty               = DexClasses.toShortyFormat(parameterTypes, returnType)
        val shortyIndex          = addOrGetStringIDIndex(shorty)
        val returnTypeIndex      = addOrGetTypeIDIndex(returnType)
        val parameterTypeIndices = parameterTypes.map { addOrGetTypeIDIndex(it) }.toIntArray()

        val protoID = ProtoID.of(shortyIndex, returnTypeIndex, *parameterTypeIndices)

        var index = dexFile.getProtoIDIndex(protoID)
        if (index == NO_INDEX) {
            index = dexFile.addProtoID(protoID)
        }
        return index
    }

    fun addOrGetFieldIDIndex(classType: String, name: String, type: String): Int {
        val fieldID =
            FieldID.of(
                addOrGetTypeIDIndex(classType),
                addOrGetStringIDIndex(name),
                addOrGetTypeIDIndex(type)
            )

        var index = dexFile.getFieldIDIndex(fieldID)
        if (index == NO_INDEX) {
            index = dexFile.addFieldID(fieldID)
        }
        return index
    }

    fun addOrGetMethodIDIndex(classType: String, name: String, parameterTypes: List<String>, returnType: String): Int {
        val methodID =
            MethodID.of(
                addOrGetTypeIDIndex(classType),
                addOrGetStringIDIndex(name),
                addOrGetProtoIDIndex(parameterTypes, returnType)
            )

        var index = dexFile.getMethodIDIndex(methodID)
        if (index == NO_INDEX) {
            index = dexFile.addMethodID(methodID)
        }
        return index
    }

    fun addClassDef(classType:  String,
                    visibility: Visibility,
                    modifiers:  EnumSet<ClassModifier> = EnumSet.noneOf(ClassModifier::class.java),
                    superType:  String? = JAVA_LANG_OBJECT_TYPE,
                    sourceFile: String? = null): ClassDefEditor {
        return addClassDef(classType, accessFlagsOf(visibility, modifiers), superType, sourceFile)
    }

    fun addClassDef(classType: String, accessFlags: Int, superType: String? = JAVA_LANG_OBJECT_TYPE, sourceFile: String? = null): ClassDefEditor {
        val classTypeIndex  = addOrGetTypeIDIndex(classType)
        val superTypeIndex  = if (superType != null) addOrGetTypeIDIndex(superType) else NO_INDEX
        val sourceFileIndex = if (sourceFile != null) addOrGetStringIDIndex(sourceFile) else NO_INDEX

        val classDef =
            ClassDef.of(classTypeIndex,
                        accessFlags,
                        superTypeIndex,
                        sourceFileIndex)

        dexFile.addClassDef(classDef)
        return ClassDefEditor.of(this, classDef)
    }

    fun addOrGetCallSiteIDIndex(callSite: CallSite): Int {
        val callSiteID = CallSiteID.of(callSite)

        var index = dexFile.getCallSiteIDIndex(callSiteID)
        if (index == NO_INDEX) {
            index = dexFile.addCallSiteID(callSiteID)
        }
        return index
    }

    fun addOrGetMethodHandleIndex(methodHandle: MethodHandle): Int {
        var index = dexFile.getMethodHandleIndex(methodHandle)
        if (index == NO_INDEX) {
            index = dexFile.addMethodHandle(methodHandle)
        }
        return index
    }

    companion object {
        fun of(dexFile: DexFile): DexEditor {
            return DexEditor(dexFile)
        }
    }
}
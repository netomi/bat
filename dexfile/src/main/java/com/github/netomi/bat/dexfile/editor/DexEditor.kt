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

class DexEditor private constructor(val dexFile: DexFile) {

    private val stringMap: MutableMap<String, Int> by lazy {
        val map = mutableMapOf<String, Int>()
        dexFile.stringIDs.forEachIndexed { index, stringID -> map[stringID.stringValue]  = index }
        map
    }

    private val typeMap: MutableMap<String, Int> by lazy {
        val map = mutableMapOf<String, Int>()
        dexFile.typeIDs.forEachIndexed { index, typeID -> map[typeID.getType(dexFile)] = index }
        map
    }

    private val protoIDMap: MutableMap<ProtoID, Int> by lazy {
        val map = mutableMapOf<ProtoID, Int>()
        dexFile.protoIDs.forEachIndexed { index, protoID -> map[protoID] = index }
        map
    }

    private val fieldIDMap: MutableMap<FieldID, Int> by lazy {
        val map = mutableMapOf<FieldID, Int>()
        dexFile.fieldIDs.forEachIndexed { index, fieldID -> map[fieldID] = index }
        map
    }

    private val methodIDMap: MutableMap<MethodID, Int> by lazy {
        val map = mutableMapOf<MethodID, Int>()
        dexFile.methodIDs.forEachIndexed { index, methodID -> map[methodID] = index }
        map
    }

    private val callSiteIDMap: MutableMap<CallSiteID, Int> by lazy {
        val map = mutableMapOf<CallSiteID, Int>()
        dexFile.callSiteIDs.forEachIndexed { index, callSiteID -> map[callSiteID] = index }
        map
    }

    private val methodHandleMap: MutableMap<MethodHandle, Int> by lazy {
        val map = mutableMapOf<MethodHandle, Int>()
        dexFile.methodHandles.forEachIndexed { index, methodHandle -> map[methodHandle] = index }
        map
    }

    fun addOrGetStringIDIndex(string: String): Int {
        assert(dexFile.stringIDs.size == stringMap.size)

        var index = stringMap[string]
        if (index == null) {
            index = dexFile.addStringID(StringID.of(string))
            stringMap[string] = index
        }
        return index
    }

    fun addOrGetTypeIDIndex(type: String): Int {
        var index = typeMap[type]
        if (index == null) {
            index = dexFile.addTypeID(TypeID.of(addOrGetStringIDIndex(type)))
            typeMap[type] = index
        }
        return index
    }

    fun addOrGetProtoIDIndex(parameterTypes: List<String>, returnType: String): Int {
        val shorty               = DexClasses.toShortyFormat(parameterTypes, returnType)
        val shortyIndex          = addOrGetStringIDIndex(shorty)
        val returnTypeIndex      = addOrGetTypeIDIndex(returnType)
        val parameterTypeIndices = parameterTypes.map { addOrGetTypeIDIndex(it) }.toIntArray()

        val protoID = ProtoID.of(shortyIndex, returnTypeIndex, *parameterTypeIndices)

        var index = protoIDMap[protoID]
        if (index == null) {
            index = dexFile.addProtoID(protoID)
            protoIDMap[protoID] = index
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

        var index = fieldIDMap[fieldID]
        if (index == null) {
            index = dexFile.addFieldID(fieldID)
            fieldIDMap[fieldID] = index
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

        var index = methodIDMap[methodID]
        if (index == null) {
            index = dexFile.addMethodID(methodID)
            methodIDMap[methodID] = index
        }
        return index
    }

    fun addClassDef(classDef: ClassDef) {
        dexFile.addClassDef(classDef)
    }

    fun addOrGetCallSiteIDIndex(callSite: CallSite): Int {
        val callSiteID = CallSiteID.of(callSite)

        var index = callSiteIDMap[callSiteID]
        if (index == null) {
            index = dexFile.addCallSiteID(callSiteID)
            callSiteIDMap[callSiteID] = index
        }
        return index
    }

    fun addOrGetMethodHandleIndex(methodHandle: MethodHandle): Int {
        var index = methodHandleMap[methodHandle]
        if (index == null) {
            index = dexFile.addMethodHandle(methodHandle)
            methodHandleMap[methodHandle] = index
        }
        return index
    }

    companion object {
        fun of(dexFile: DexFile): DexEditor {
            return DexEditor(dexFile)
        }
    }
}
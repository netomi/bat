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

class DexComposer internal constructor(val dexFile: DexFile) {

    private val stringMap   : MutableMap<String, Int>   = mutableMapOf()
    private val typeMap     : MutableMap<String, Int>   = mutableMapOf()
    private val protoIDMap  : MutableMap<ProtoID, Int>  = mutableMapOf()
    private val fieldIDMap  : MutableMap<FieldID, Int>  = mutableMapOf()
    private val methodIDMap : MutableMap<MethodID, Int> = mutableMapOf()

    init {
        dexFile.stringIDs.forEachIndexed { index, stringID -> stringMap[stringID.stringValue]  = index }
        dexFile.typeIDs.forEachIndexed   { index, typeID   -> typeMap[typeID.getType(dexFile)] = index }
        dexFile.protoIDs.forEachIndexed  { index, protoID  -> protoIDMap[protoID] = index }
        dexFile.fieldIDs.forEachIndexed  { index, fieldID  -> fieldIDMap[fieldID]   = index }
        dexFile.methodIDs.forEachIndexed { index, methodID -> methodIDMap[methodID] = index }
    }

    fun addOrGetStringIDIndex(string: String): Int {
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
}
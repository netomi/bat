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
package com.github.netomi.bat.dexfile

import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.visitor.*
import java.util.*

class DexFile {

    private var dexFormatInternal: DexFormat? = null

    val dexFormat: DexFormat?
        get() = if (header != null) header?.dexFormat else dexFormatInternal

    /**
     * Used to create a DexFile instance used to read in dex files.
     * Use DexFile.of(DexFormat) to create an instance for an explicit dex version.
     */
    constructor()

    internal constructor(format: DexFormat): this() {
        dexFormatInternal = format
    }

    /**
     * The DexHeader instance associated with this DexFile if it is read from an existing dex file.
     */
    var header: DexHeader? = null
        internal set(value) {
            dexFormatInternal = null
            field = value
        }

    /**
     * The MapList instance associated with this DexFile if it is read from an existing dex file.
     */
    var mapList: MapList? = null
        internal set

    internal val stringIDs     = ArrayList<StringID>()
    internal val typeIDs       = ArrayList<TypeID>()
    internal val protoIDs      = ArrayList<ProtoID>()
    internal val fieldIDs      = ArrayList<FieldID>()
    internal val methodIDs     = ArrayList<MethodID>()
    internal val classDefs     = ArrayList<ClassDef>()
    internal val callSiteIDs   = ArrayList<CallSiteID>()
    internal val methodHandles = ArrayList<MethodHandle>()

    var linkData: ByteArray?   = null
        internal set

    private var stringMap:   MutableMap<String, Int>   = mutableMapOf()
    private var typeMap:     MutableMap<String, Int>   = mutableMapOf()
    private var protoIDMap:  MutableMap<ProtoID, Int>  = mutableMapOf()
    private var classDefMap: MutableMap<String, Int>   = mutableMapOf()
    private var fieldIDMap:  MutableMap<FieldID, Int>  = mutableMapOf()
    private var methodIDMap: MutableMap<MethodID, Int> = mutableMapOf()

    val stringIDCount: Int
        get() = stringIDs.size

    fun getStringIDs(): Iterable<StringID> {
        return stringIDs
    }

    fun getStringID(index: Int): StringID {
        return stringIDs[index]
    }

    fun getString(index: Int): String? {
        return if (index == DexConstants.NO_INDEX) null else getStringID(index).stringValue
    }

    fun getStringIDIndex(string: String): Int {
        val index = stringMap[string]
        return index ?: DexConstants.NO_INDEX
    }

    fun addOrGetStringIDIndex(string: String): Int {
        var index = stringMap[string]
        if (index == null) {
            stringIDs.add(StringID.of(string))
            index = stringIDs.size - 1
            stringMap[string] = index
        }
        return index
    }

    val typeIDCount: Int
        get() = typeIDs.size

    fun getTypeIDs(): Iterable<TypeID> {
        return typeIDs
    }

    fun getTypeID(index: Int): TypeID {
        return typeIDs[index]
    }

    fun getType(index: Int): String? {
        return if (index == DexConstants.NO_INDEX) null else getTypeID(index).getType(this)
    }

    fun getTypeIDIndex(type: String): Int {
        val index = typeMap[type]
        return index ?: DexConstants.NO_INDEX
    }

    fun addOrGetTypeIDIndex(type: String): Int {
        var index = typeMap[type]
        if (index == null) {
            typeIDs.add(TypeID.of(addOrGetStringIDIndex(type)))
            index = typeIDs.size - 1
            typeMap[type] = index
        }
        return index
    }

    val protoIDCount: Int
        get() = protoIDs.size

    fun getProtoIDs(): Iterable<ProtoID> {
        return protoIDs
    }

    fun getProtoID(protoIndex: Int): ProtoID {
        return protoIDs[protoIndex]
    }

    fun addOrGetProtoID(shorty: String, returnType: String, vararg parameterTypes: String): Int {
        val protoID =
            if (parameterTypes.isNotEmpty()) {
                val parameterTypeIndices = IntArray(parameterTypes.size)
                for (i in parameterTypes.indices) {
                    parameterTypeIndices[i] = addOrGetTypeIDIndex(parameterTypes[i])
                }
                ProtoID.of(addOrGetStringIDIndex(shorty), addOrGetTypeIDIndex(returnType), *parameterTypeIndices)
            } else {
                ProtoID.of(addOrGetStringIDIndex(shorty), addOrGetTypeIDIndex(returnType))
            }

        var index = protoIDMap[protoID]
        if (index == null) {
            protoIDs.add(protoID)
            index = protoIDs.size - 1
            protoIDMap[protoID] = index
        }
        return index
    }

    val fieldIDCount: Int
        get() = fieldIDs.size

    fun getFieldIDs(): Iterable<FieldID> {
        return fieldIDs
    }

    fun getFieldID(fieldIndex: Int): FieldID {
        return fieldIDs[fieldIndex]
    }

    fun addOrGetFieldID(classType: String, name: String, type: String): Int {
        val fieldID = FieldID.of(
            addOrGetTypeIDIndex(classType),
            addOrGetStringIDIndex(name),
            addOrGetTypeIDIndex(type)
        )
        var index = fieldIDMap[fieldID]
        if (index == null) {
            fieldIDs.add(fieldID)
            index = fieldIDs.size - 1
            fieldIDMap[fieldID] = index
        }
        return index
    }

    val methodIDCount: Int
        get() = methodIDs.size

    fun getMethodIDs(): Iterable<MethodID> {
        return methodIDs
    }

    fun getMethodID(methodIndex: Int): MethodID {
        return methodIDs[methodIndex]
    }

    fun addOrGetMethodID(classType: String, name: String, shorty: String, returnType: String, vararg parameterTypes: String): Int {
        val methodID = MethodID.of(
            addOrGetTypeIDIndex(classType),
            addOrGetProtoID(shorty, returnType, *parameterTypes),
            addOrGetStringIDIndex(name)
        )
        var index = methodIDMap[methodID]
        if (index == null) {
            methodIDs.add(methodID)
            index = methodIDs.size - 1
            methodIDMap[methodID] = index
        }
        return index
    }

    val classDefCount: Int
        get() = classDefs.size

    fun getClassDefs(): Iterable<ClassDef> {
        return classDefs
    }

    fun getClassDef(className: String): ClassDef? {
        val index = classDefMap[className]
        return if (index == null) null else classDefs[index]
    }

    fun getClassDef(classDefIndex: Int): ClassDef {
        return classDefs[classDefIndex]
    }

    fun addClassDef(classDef: ClassDef) {
        classDefs.add(classDef)
        classDefMap[classDef.getClassName(this)] = classDefs.size - 1
    }

    val callSiteIDCount: Int
        get() = callSiteIDs.size

    fun getCallSiteIDs(): Iterable<CallSiteID> {
        return callSiteIDs
    }

    fun getCallSiteID(callSiteIndex: Int): CallSiteID {
        return callSiteIDs[callSiteIndex]
    }

    val methodHandleCount: Int
        get() = methodHandles.size

    fun getMethodHandles(): Iterable<MethodHandle> {
        return methodHandles
    }

    fun getMethodHandle(methodHandleIndex: Int): MethodHandle {
        return methodHandles[methodHandleIndex]
    }

    fun accept(visitor: DexFileVisitor) {
        visitor.visitDexFile(this)
    }

    fun headerAccept(visitor: DexHeaderVisitor) {
        visitor.visitHeader(this, header)
    }

    fun classDefsAccept(visitor: ClassDefVisitor) {
        val classDefListIterator: ListIterator<ClassDef> = classDefs.listIterator()
        while (classDefListIterator.hasNext()) {
            val index = classDefListIterator.nextIndex()
            visitor.visitClassDef(this, index, classDefListIterator.next())
        }
    }

    fun methodHandlesAccept(visitor: MethodHandleVisitor) {
        val methodHandleListIterator: ListIterator<MethodHandle> = methodHandles.listIterator()
        while (methodHandleListIterator.hasNext()) {
            val index = methodHandleListIterator.nextIndex()
            visitor.visitMethodHandle(this, index, methodHandleListIterator.next())
        }
    }

    fun callSiteIDsAccept(visitor: CallSiteIDVisitor) {
        val callSiteIDListIterator: ListIterator<CallSiteID> = callSiteIDs.listIterator()
        while (callSiteIDListIterator.hasNext()) {
            val index = callSiteIDListIterator.nextIndex()
            visitor.visitCallSiteID(this, index, callSiteIDListIterator.next())
        }
    }

    fun dataItemsAccept(visitor: DataItemVisitor) {
        if (header != null) {
            visitor.visitHeader(this, header)
        }
        if (mapList != null) {
            visitor.visitMapList(this, mapList)
        }
        for (stringIDItem in stringIDs) {
            visitor.visitStringID(this, stringIDItem)
            stringIDItem.dataItemsAccept(this, visitor)
        }
        for (typeIDItem in typeIDs) {
            visitor.visitTypeID(this, typeIDItem)
        }
        for (protoIDItem in protoIDs) {
            visitor.visitProtoID(this, protoIDItem)
            protoIDItem.dataItemsAccept(this, visitor)
        }
        for (fieldIDItem in fieldIDs) {
            visitor.visitFieldID(this, fieldIDItem)
        }
        for (methodIDItem in methodIDs) {
            visitor.visitMethodID(this, methodIDItem)
        }
        for (classDefItem in classDefs) {
            visitor.visitClassDef(this, classDefItem)
            classDefItem.dataItemsAccept(this, visitor)
        }
        for (callSiteIDItem in callSiteIDs) {
            visitor.visitCallSiteID(this, callSiteIDItem)
            callSiteIDItem.dataItemsAccept(this, visitor)
        }
        for (methodHandleItem in methodHandles) {
            visitor.visitMethodHandle(this, methodHandleItem)
            methodHandleItem.dataItemsAccept(this, visitor)
        }
    }

    fun read(`in`: DexDataInput) {
        // update caches:
        stringMap = HashMap(stringIDCount)
        val stringIterator: ListIterator<StringID> = stringIDs.listIterator()
        while (stringIterator.hasNext()) {
            val index = stringIterator.nextIndex()
            stringMap[stringIterator.next().stringValue] = index
        }
        typeMap = HashMap(typeIDCount)
        val typeIterator: ListIterator<TypeID> = typeIDs.listIterator()
        while (typeIterator.hasNext()) {
            val index = typeIterator.nextIndex()
            typeMap[typeIterator.next().getType(this)] = index
        }
        protoIDMap = HashMap(protoIDCount)
        val protoIDIterator: ListIterator<ProtoID> = protoIDs.listIterator()
        while (protoIDIterator.hasNext()) {
            val index = protoIDIterator.nextIndex()
            protoIDMap[protoIDIterator.next()] = index
        }
        classDefMap = HashMap(classDefCount)
        val classDefIterator: ListIterator<ClassDef> = classDefs.listIterator()
        while (classDefIterator.hasNext()) {
            val index = classDefIterator.nextIndex()
            classDefMap[classDefIterator.next().getClassName(this)] = index
        }
        fieldIDMap = HashMap(fieldIDCount)
        val fieldIDIterator: ListIterator<FieldID> = fieldIDs.listIterator()
        while (fieldIDIterator.hasNext()) {
            val index = fieldIDIterator.nextIndex()
            fieldIDMap[fieldIDIterator.next()] = index
        }
        methodIDMap = HashMap(methodIDCount)
        val methodIDIterator: ListIterator<MethodID> = methodIDs.listIterator()
        while (methodIDIterator.hasNext()) {
            val index = methodIDIterator.nextIndex()
            methodIDMap[methodIDIterator.next()] = index
        }
    }

    override fun toString(): String {
        // TODO: implement a proper version.
        val sb = StringBuilder()
        sb.append(header)
        return sb.toString()
    }


    companion object {
        @JvmStatic
        fun of(dexFormat: DexFormat): DexFile {
            return DexFile(dexFormat)
        }
    }
}
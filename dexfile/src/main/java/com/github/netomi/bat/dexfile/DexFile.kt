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

import com.github.netomi.bat.dexfile.util.DexClasses
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

    fun getString(index: Int): String {
        return getStringID(index).stringValue
    }

    fun getStringNullable(index: Int): String? {
        return if (index == NO_INDEX) null else getString(index)
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

    fun getType(index: Int): String {
        return getTypeID(index).getType(this)
    }

    fun getTypeNullable(index: Int): String? {
        return if (index == NO_INDEX) null else getType(index)
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

    fun addOrGetProtoID(parameterTypes: List<String>, returnType: String): Int {
        val shorty = DexClasses.toShortyFormat(parameterTypes, returnType)
        val shortyIndex          = addOrGetStringIDIndex(shorty)
        val returnTypeIndex      = addOrGetTypeIDIndex(returnType)
        val parameterTypeIndices = parameterTypes.map { addOrGetTypeIDIndex(it) }.toIntArray()

        val protoID = ProtoID.of(shortyIndex, returnTypeIndex, *parameterTypeIndices)

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
        val fieldID =
            FieldID.of(
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

    fun addOrGetMethodID(classType: String, name: String, parameterTypes: List<String>, returnType: String): Int {
        val methodID =
            MethodID.of(
                addOrGetTypeIDIndex(classType),
                addOrGetStringIDIndex(name),
                addOrGetProtoID(parameterTypes, returnType)
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
            visitor.visitHeader(this, header!!)
        }
        if (mapList != null) {
            visitor.visitMapList(this, mapList!!)
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

    internal fun referencedIDsAccept(visitor: ReferencedIDVisitor) {
        for (typeIDItem in typeIDs) {
            typeIDItem.referencedIDsAccept(this, visitor)
        }

        for (protoIDItem in protoIDs) {
            protoIDItem.referencedIDsAccept(this, visitor)
        }

        for (fieldIDItem in fieldIDs) {
            fieldIDItem.referencedIDsAccept(this, visitor)
        }

        for (methodIDItem in methodIDs) {
            methodIDItem.referencedIDsAccept(this, visitor)
        }

        for (classDefItem in classDefs) {
            classDefItem.referencedIDsAccept(this, visitor)
        }
    }

    internal fun refreshCaches() {
        // update caches:
        stringMap.clear()
        val stringIterator: ListIterator<StringID> = stringIDs.listIterator()
        while (stringIterator.hasNext()) {
            val index = stringIterator.nextIndex()
            stringMap[stringIterator.next().stringValue] = index
        }
        typeMap.clear()
        val typeIterator: ListIterator<TypeID> = typeIDs.listIterator()
        while (typeIterator.hasNext()) {
            val index = typeIterator.nextIndex()
            typeMap[typeIterator.next().getType(this)] = index
        }
        protoIDMap.clear()
        val protoIDIterator: ListIterator<ProtoID> = protoIDs.listIterator()
        while (protoIDIterator.hasNext()) {
            val index = protoIDIterator.nextIndex()
            protoIDMap[protoIDIterator.next()] = index
        }
        classDefMap.clear()
        val classDefIterator: ListIterator<ClassDef> = classDefs.listIterator()
        while (classDefIterator.hasNext()) {
            val index = classDefIterator.nextIndex()
            classDefMap[classDefIterator.next().getClassName(this)] = index
        }
        fieldIDMap.clear()
        val fieldIDIterator: ListIterator<FieldID> = fieldIDs.listIterator()
        while (fieldIDIterator.hasNext()) {
            val index = fieldIDIterator.nextIndex()
            fieldIDMap[fieldIDIterator.next()] = index
        }
        methodIDMap.clear()
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
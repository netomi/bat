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

import com.github.netomi.bat.dexfile.editor.DexComposer
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

    private val classDefMap : MutableMap<String, Int> = mutableMapOf()

    private var _composer: DexComposer? = null
    val composer: DexComposer by lazy {
        _composer = DexComposer(this)
        _composer!!
    }

    var linkData: ByteArray?   = null
        internal set

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

    internal fun addStringID(stringID: StringID): Int {
        stringIDs.add(stringID)
        return stringIDs.size - 1
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

    internal fun addTypeID(typeID: TypeID): Int {
        typeIDs.add(typeID)
        return typeIDs.size - 1
    }

    val protoIDCount: Int
        get() = protoIDs.size

    fun getProtoIDs(): Iterable<ProtoID> {
        return protoIDs
    }

    fun getProtoID(protoIndex: Int): ProtoID {
        return protoIDs[protoIndex]
    }

    internal fun addProtoID(protoID: ProtoID): Int {
        protoIDs.add(protoID)
        return protoIDs.size - 1
    }

    val fieldIDCount: Int
        get() = fieldIDs.size

    fun getFieldIDs(): Iterable<FieldID> {
        return fieldIDs
    }

    fun getFieldID(fieldIndex: Int): FieldID {
        return fieldIDs[fieldIndex]
    }

    internal fun addFieldID(fieldID: FieldID): Int {
        fieldIDs.add(fieldID)
        return fieldIDs.size - 1
    }

    val methodIDCount: Int
        get() = methodIDs.size

    fun getMethodIDs(): Iterable<MethodID> {
        return methodIDs
    }

    fun getMethodID(methodIndex: Int): MethodID {
        return methodIDs[methodIndex]
    }

    internal fun addMethodID(methodID: MethodID): Int {
        methodIDs.add(methodID)
        return methodIDs.size - 1
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
        val className = classDef.getClassName(this)
        if (getClassDef(className) != null) {
            throw IllegalArgumentException("class with name $className already exists in dex file.")
        }

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

    fun addMethodHandle(methodHandle: MethodHandle): Int {
        methodHandles.add(methodHandle)
        return methodHandles.size - 1
    }

    fun accept(visitor: DexFileVisitor) {
        visitor.visitDexFile(this)
    }

    fun headerAccept(visitor: DexHeaderVisitor) {
        visitor.visitHeader(this, header)
    }

    fun classDefsAccept(visitor: ClassDefVisitor) {
        classDefs.forEachIndexed { index, classDef -> visitor.visitClassDef(this, index, classDef) }
    }

    fun classDefAccept(className: String, visitor: ClassDefVisitor) {
        getClassDef(className)?.accept(this, visitor)
    }

    fun methodHandlesAccept(visitor: MethodHandleVisitor) {
        methodHandles.forEachIndexed { index, methodHandle -> visitor.visitMethodHandle(this, index, methodHandle) }
    }

    fun callSiteIDsAccept(visitor: CallSiteIDVisitor) {
        callSiteIDs.forEachIndexed { index, callSiteID -> visitor.visitCallSiteID(this, index, callSiteID) }
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
        for (callSiteIDItem in callSiteIDs) {
            callSiteIDItem.referencedIDsAccept(this, visitor)
        }
        for (methodHandleItem in methodHandles) {
            methodHandleItem.referencedIDsAccept(this, visitor)
        }
    }

    internal fun refreshCaches() {
        classDefs.forEachIndexed  { index, classDef  -> classDefMap[classDef.getClassName(this)] = index }
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
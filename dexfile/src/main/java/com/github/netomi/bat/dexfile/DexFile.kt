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

import com.github.netomi.bat.dexfile.instruction.DexOpCode
import com.github.netomi.bat.dexfile.visitor.*
import com.github.netomi.bat.util.asInternalJavaClassName
import com.github.netomi.bat.util.asJavaType
import com.github.netomi.bat.util.parallelForEachIndexed
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class DexFile private constructor(private var dexFormatInternal: DexFormat? = DexFormat.FORMAT_035) {

    var dexFormat: DexFormat
        get() = (if (header != null) header?.dexFormat else dexFormatInternal)!!
        set(value) {
            require(header == null) { "can not set dex format for DexFile instances containing an header" }
            dexFormatInternal = value
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
    internal var classDefs     = ArrayList<ClassDef>()
    internal val callSiteIDs   = ArrayList<CallSiteID>()
    internal val methodHandles = ArrayList<MethodHandle>()

    private val classDefMap : MutableMap<String, Int> = mutableMapOf()

    var linkData: ByteArray?   = null
        internal set

    internal fun supportsOpcode(opCode: DexOpCode): Boolean {
        return dexFormat >= opCode.minFormat
    }

    internal fun clear() {
        stringIDs.clear()
        typeIDs.clear()
        protoIDs.clear()
        fieldIDs.clear()
        methodIDs.clear()
        classDefs.clear()
        callSiteIDs.clear()
        methodHandles.clear()

        stringMap.clear()
        typeMap.clear()
        protoIDMap.clear()
        fieldIDMap.clear()
        methodIDMap.clear()
        classDefMap.clear()
        callSiteIDMap.clear()
        methodHandleMap.clear()

        linkData = null
    }

    val stringIDCount: Int
        get() = stringIDs.size

    private val stringMap: MutableMap<String, Int> = mutableMapOf()

    fun getStringIDs(): Iterable<StringID> {
        return stringIDs
    }

    fun getStringID(index: Int): StringID {
        return stringIDs[index]
    }

    fun getStringIDIndex(string: String): Int {
        return stringMap[string] ?: NO_INDEX
    }

    fun getString(index: Int): String {
        return getStringID(index).stringValue
    }

    fun getStringNullable(index: Int): String? {
        return if (index == NO_INDEX) null else getString(index)
    }

    internal fun addStringID(stringID: StringID): Int {
        stringIDs.add(stringID)
        val index = stringIDs.lastIndex
        stringMap[stringID.stringValue] = index
        return index
    }

    val typeIDCount: Int
        get() = typeIDs.size

    private val typeMap: MutableMap<String, Int> = mutableMapOf()

    fun getTypeIDs(): Iterable<TypeID> {
        return typeIDs
    }

    fun getTypeID(index: Int): TypeID {
        return typeIDs[index]
    }

    fun getTypeIDIndex(type: String): Int {
        return typeMap[type] ?: NO_INDEX
    }

    fun getType(index: Int): String {
        return getTypeID(index).getType(this)
    }

    fun getTypeNullable(index: Int): String? {
        return if (index == NO_INDEX) null else getType(index)
    }

    internal fun addTypeID(typeID: TypeID): Int {
        typeIDs.add(typeID)
        val index = typeIDs.lastIndex
        typeMap[typeID.getType(this)] = index
        return index
    }

    val protoIDCount: Int
        get() = protoIDs.size

    private val protoIDMap: MutableMap<ProtoID, Int> = mutableMapOf()

    fun getProtoIDs(): Iterable<ProtoID> {
        return protoIDs
    }

    fun getProtoID(protoIndex: Int): ProtoID {
        return protoIDs[protoIndex]
    }

    fun getProtoIDIndex(protoID: ProtoID): Int {
        return protoIDMap[protoID] ?: NO_INDEX
    }

    internal fun addProtoID(protoID: ProtoID): Int {
        protoIDs.add(protoID)
        val index = protoIDs.lastIndex
        protoIDMap[protoID] = index
        return index
    }

    val fieldIDCount: Int
        get() = fieldIDs.size

    private val fieldIDMap: MutableMap<FieldID, Int> = mutableMapOf()

    fun getFieldIDs(): Iterable<FieldID> {
        return fieldIDs
    }

    fun getFieldID(fieldIndex: Int): FieldID {
        return fieldIDs[fieldIndex]
    }

    fun getFieldIDIndex(fieldID: FieldID): Int {
        return fieldIDMap[fieldID] ?: NO_INDEX
    }

    internal fun addFieldID(fieldID: FieldID): Int {
        fieldIDs.add(fieldID)
        val index = fieldIDs.lastIndex
        fieldIDMap[fieldID] = index
        return index
    }

    val methodIDCount: Int
        get() = methodIDs.size

    private val methodIDMap: MutableMap<MethodID, Int> = mutableMapOf()

    fun getMethodIDs(): Iterable<MethodID> {
        return methodIDs
    }

    fun getMethodID(methodIndex: Int): MethodID {
        return methodIDs[methodIndex]
    }

    fun getMethodIDIndex(methodID: MethodID): Int {
        return methodIDMap[methodID] ?: NO_INDEX
    }

    internal fun addMethodID(methodID: MethodID): Int {
        methodIDs.add(methodID)
        val index = methodIDs.lastIndex
        methodIDMap[methodID] = index
        return index
    }

    val classDefCount: Int
        get() = classDefs.size

    fun getClassDefs(): Iterable<ClassDef> {
        return classDefs
    }

    fun getClassDefByType(classType: String): ClassDef? {
        val index = classDefMap[classType]
        return if (index == null) null else classDefs[index]
    }

    fun getClassDefByClassName(internalClassName: String): ClassDef? {
        val classType = internalClassName.asInternalJavaClassName().toInternalType()
        val index = classDefMap[classType]
        return if (index == null) null else classDefs[index]
    }

    fun getClassDef(classDefIndex: Int): ClassDef {
        return classDefs[classDefIndex]
    }

    internal fun addClassDef(classDef: ClassDef): Int {
        val classType = classDef.getType(this)
        if (getClassDefByType(classType) != null) {
            val className = classType.asJavaType().toInternalClassName()
            throw IllegalArgumentException("class with name '$className' already exists")
        }

        classDefs.add(classDef)
        val index = classDefs.lastIndex
        classDefMap[classType] = index
        return index
    }

    val callSiteIDCount: Int
        get() = callSiteIDs.size

    private val callSiteIDMap: MutableMap<CallSiteID, Int> = mutableMapOf()

    fun getCallSiteIDs(): Iterable<CallSiteID> {
        return callSiteIDs
    }

    fun getCallSiteID(callSiteIndex: Int): CallSiteID {
        return callSiteIDs[callSiteIndex]
    }

    fun getCallSiteIDIndex(callSiteID: CallSiteID): Int {
        return callSiteIDMap[callSiteID] ?: NO_INDEX
    }

    internal fun addCallSiteID(callSiteID: CallSiteID): Int {
        require(dexFormat >= DexFormat.FORMAT_038) { "DexFile of format $dexFormat does not support adding CallSiteIDs" }

        callSiteIDs.add(callSiteID)
        val index = callSiteIDs.lastIndex
        callSiteIDMap[callSiteID] = index
        return index
    }

    val methodHandleCount: Int
        get() = methodHandles.size

    private val methodHandleMap: MutableMap<MethodHandle, Int> = mutableMapOf()

    fun getMethodHandles(): Iterable<MethodHandle> {
        return methodHandles
    }

    fun getMethodHandle(methodHandleIndex: Int): MethodHandle {
        return methodHandles[methodHandleIndex]
    }

    fun getMethodHandleIndex(methodHandle: MethodHandle): Int {
        return methodHandleMap[methodHandle] ?: NO_INDEX
    }

    internal fun addMethodHandle(methodHandle: MethodHandle): Int {
        require(dexFormat >= DexFormat.FORMAT_038) { "DexFile of format $dexFormat does not support adding MethodHandles" }

        methodHandles.add(methodHandle)
        val index = methodHandles.lastIndex
        methodHandleMap[methodHandle] = index
        return index
    }

    fun accept(visitor: DexFileVisitor) {
        visitor.visitDexFile(this)
    }

    fun headerAccept(visitor: DexHeaderVisitor) {
        if (header != null) {
            visitor.visitHeader(this, header!!)
        }
    }

    fun classDefsAccept(visitor: ClassDefVisitor) {
        classDefs.forEachIndexed { index, classDef -> visitor.visitClassDef(this, index, classDef) }
    }

    fun parallelClassDefsAccept(coroutineContext: CoroutineContext = Dispatchers.Default, visitorSupplier: () -> ClassDefVisitor) {
        val threadLocal = ThreadLocal.withInitial(visitorSupplier)
        classDefs.parallelForEachIndexed(coroutineContext) { index, classDef ->
            val visitor = threadLocal.get()
            visitor.visitClassDef(this, index, classDef)
        }
    }

    fun classDefAcceptByType(classType: String, visitor: ClassDefVisitor) {
        getClassDefByType(classType)?.accept(this, visitor)
    }

    fun classDefAcceptByClassName(internalClassName: String, visitor: ClassDefVisitor) {
        getClassDefByClassName(internalClassName)?.accept(this, visitor)
    }

    fun methodHandlesAccept(visitor: MethodHandleVisitor) {
        methodHandles.forEachIndexed { index, methodHandle -> visitor.visitMethodHandle(this, index, methodHandle) }
    }

    fun callSiteIDsAccept(visitor: CallSiteIDVisitor) {
        callSiteIDs.forEachIndexed { index, callSiteID -> visitor.visitCallSiteID(this, index, callSiteID) }
    }

    internal fun dataItemsAccept(visitor: DataItemVisitor) {
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
        classDefMap.clear()
        classDefs.forEachIndexed  { index, classDef  -> classDefMap[classDef.getType(this)] = index }
    }

    override fun toString(): String {
        return buildString {
            appendLine("DexFile[")
            appendLine("  format       : $dexFormat")
            appendLine("  stringIDs    : $stringIDCount items")
            appendLine("  typeIDs      : $typeIDCount items")
            appendLine("  protoIDs     : $protoIDCount items")
            appendLine("  fieldIDs     : $fieldIDCount items")
            appendLine("  methodIDs    : $methodIDCount items")
            appendLine("  classDefs    : $classDefCount items")
            appendLine("  callSiteIDs  : $callSiteIDCount items")
            appendLine("  methodHandles: $methodHandleCount items]")
        }
    }

    companion object {
        fun empty(): DexFile {
            return DexFile()
        }

        fun of(dexFormat: DexFormat): DexFile {
            return DexFile(dexFormat)
        }
    }
}
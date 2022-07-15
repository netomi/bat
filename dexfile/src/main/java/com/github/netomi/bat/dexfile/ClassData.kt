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
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.*
import java.util.*
import kotlin.collections.ArrayList

@DataItemAnn(
    type          = TYPE_CLASS_DATA_ITEM,
    dataAlignment = 1,
    dataSection   = true)
class ClassData private constructor(
    private val _staticFields:   ArrayList<EncodedField>  = ArrayList(0),
    private val _instanceFields: ArrayList<EncodedField>  = ArrayList(0),
    private val _directMethods:  ArrayList<EncodedMethod> = ArrayList(0),
    private val _virtualMethods: ArrayList<EncodedMethod> = ArrayList(0)) : DataItem() {

    val staticFields: List<EncodedField>
        get() = _staticFields

    val instanceFields: List<EncodedField>
        get() = _instanceFields

    val directMethods: MutableList<EncodedMethod>
        get() = _directMethods

    val virtualMethods: MutableList<EncodedMethod>
        get() = _virtualMethods

    val fields: List<EncodedField>
        get() = staticFields + instanceFields

    val methods: List<EncodedMethod>
        get() = directMethods + virtualMethods

    override val isEmpty: Boolean
        get() = fields.isEmpty() && methods.isEmpty()

    fun addField(field: EncodedField) {
        // TODO: throw exception for duplicates
        if (field.isStatic) {
            _staticFields.add(field)
        } else {
            _instanceFields.add(field)
        }
    }

    fun addMethod(method: EncodedMethod) {
        // TODO: throw exception for duplicates
        if (method.isDirectMethod) {
            _directMethods.add(method)
        } else {
            _virtualMethods.add(method)
        }
    }

    val staticFieldCount: Int
        get() = staticFields.size

    fun getStaticField(index: Int): EncodedField {
        return staticFields[index]
    }

    val instanceFieldCount: Int
        get() = instanceFields.size

    fun getInstanceField(index: Int): EncodedField {
        return instanceFields[index]
    }

    val directMethodCount: Int
        get() = directMethods.size

    fun getDirectMethod(index: Int): EncodedMethod {
        return directMethods[index]
    }

    val virtualMethodCount: Int
        get() = virtualMethods.size

    fun getVirtualMethod(index: Int): EncodedMethod {
        return virtualMethods[index]
    }

    override fun read(input: DexDataInput) {
        // field/method sizes are not stored explicitly,
        // use the size() method of the corresponding list instead.
        val staticFieldsSize = input.readUleb128()
        val instanceFieldsSize = input.readUleb128()
        val directMethodsSize = input.readUleb128()
        val virtualMethodsSize = input.readUleb128()
        var lastIndex = 0
        _staticFields.clear()
        _staticFields.ensureCapacity(staticFieldsSize)
        for (i in 0 until staticFieldsSize) {
            val encodedField = EncodedField.readContent(input, lastIndex)
            lastIndex = encodedField.fieldIndex
            _staticFields.add(encodedField)
        }
        lastIndex = 0
        _instanceFields.clear()
        _instanceFields.ensureCapacity(instanceFieldsSize)
        for (i in 0 until instanceFieldsSize) {
            val encodedField = EncodedField.readContent(input, lastIndex)
            lastIndex = encodedField.fieldIndex
            _instanceFields.add(encodedField)
        }
        lastIndex = 0
        _directMethods.clear()
        _directMethods.ensureCapacity(directMethodsSize)
        for (i in 0 until directMethodsSize) {
            val encodedMethod = EncodedMethod.readContent(input, lastIndex)
            lastIndex = encodedMethod.methodIndex
            _directMethods.add(encodedMethod)
        }
        lastIndex = 0
        _virtualMethods.clear()
        _virtualMethods.ensureCapacity(virtualMethodsSize)
        for (i in 0 until virtualMethodsSize) {
            val encodedMethod = EncodedMethod.readContent(input, lastIndex)
            lastIndex = encodedMethod.methodIndex
            _virtualMethods.add(encodedMethod)
        }
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        for (method in methods) {
            method.readLinkedDataItems(input)
        }
    }

    override fun updateOffsets(dataItemMap: Map) {
        for (method in methods) {
            method.updateOffsets(dataItemMap)
        }
    }

    override fun write(output: DexDataOutput) {
        output.writeUleb128(staticFields.size)
        output.writeUleb128(instanceFields.size)
        output.writeUleb128(directMethods.size)
        output.writeUleb128(virtualMethods.size)
        var lastIndex = 0
        for (field in staticFields) {
            lastIndex = field.write(output, lastIndex)
        }
        lastIndex = 0
        for (field in instanceFields) {
            lastIndex = field.write(output, lastIndex)
        }
        lastIndex = 0
        for (method in directMethods) {
            lastIndex = method.write(output, lastIndex)
        }
        lastIndex = 0
        for (method in virtualMethods) {
            lastIndex = method.write(output, lastIndex)
        }
    }

    fun fieldsAccept(dexFile: DexFile, classDef: ClassDef, visitor: EncodedFieldVisitor) {
        staticFieldsAccept(dexFile, classDef, visitor)
        instanceFieldsAccept(dexFile, classDef, visitor)
    }

    fun staticFieldsAccept(dexFile: DexFile, classDef: ClassDef, visitor: EncodedFieldVisitor) {
        staticFields.forEachIndexed { index, encodedField -> visitor.visitStaticField(dexFile, classDef, index, encodedField) }
    }

    fun instanceFieldsAccept(dexFile: DexFile, classDef: ClassDef, visitor: EncodedFieldVisitor) {
        instanceFields.forEachIndexed { index, encodedField -> visitor.visitInstanceField(dexFile, classDef, index, encodedField) }
    }

    fun methodsAccept(dexFile: DexFile, classDef: ClassDef, visitor: EncodedMethodVisitor) {
        directMethodsAccept(dexFile, classDef, visitor)
        virtualMethodsAccept(dexFile, classDef, visitor)
    }

    fun directMethodsAccept(dexFile: DexFile, classDef: ClassDef, visitor: EncodedMethodVisitor) {
        directMethods.forEachIndexed { index, encodedMethod -> visitor.visitDirectMethod(dexFile, classDef, index, encodedMethod) }
    }

    fun virtualMethodsAccept(dexFile: DexFile, classDef: ClassDef, visitor: EncodedMethodVisitor) {
        virtualMethods.forEachIndexed { index, encodedMethod -> visitor.visitVirtualMethod(dexFile, classDef, index, encodedMethod) }
    }

    fun membersAccept(dexFile: DexFile, classDef: ClassDef, visitor: EncodedMemberVisitor) {
        fieldsAccept(dexFile, classDef, visitor)
        methodsAccept(dexFile, classDef, visitor)
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        for (method in methods) {
            method.dataItemsAccept(dexFile, visitor)
        }
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        for (field in fields) {
            field.referencedIDsAccept(dexFile, visitor)
        }
        for (method in methods) {
            method.referencedIDsAccept(dexFile, visitor)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val o = other as ClassData

        return staticFields   == o.staticFields   &&
               instanceFields == o.instanceFields &&
               directMethods  == o.directMethods  &&
               virtualMethods == o.virtualMethods
    }

    override fun hashCode(): Int {
        return Objects.hash(staticFields, instanceFields, directMethods, virtualMethods)
    }

    override fun toString(): String {
        return "ClassData[staticFields=${staticFieldCount},instanceFields=${instanceFieldCount}," +
                         "directMethods=${directMethodCount},virtualMethods=${virtualMethodCount}]"
    }

    companion object {
        fun empty(): ClassData {
            return ClassData()
        }

        fun readContent(input: DexDataInput): ClassData {
            val classData = ClassData()
            classData.read(input)
            return classData
        }
    }
}
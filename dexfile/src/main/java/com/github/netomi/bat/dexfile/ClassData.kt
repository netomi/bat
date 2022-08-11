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
import com.github.netomi.bat.util.mutableListOfCapacity
import java.util.*

@DataItemAnn(
    type          = TYPE_CLASS_DATA_ITEM,
    dataAlignment = 1,
    dataSection   = true)
internal class ClassData
    private constructor(private var _staticFields:   MutableList<EncodedField>  = mutableListOfCapacity(0),
                        private var _instanceFields: MutableList<EncodedField>  = mutableListOfCapacity(0),
                        private var _directMethods:  MutableList<EncodedMethod> = mutableListOfCapacity(0),
                        private var _virtualMethods: MutableList<EncodedMethod> = mutableListOfCapacity(0)) : DataItem() {

    val staticFields: List<EncodedField>
        get() = _staticFields

    val instanceFields: List<EncodedField>
        get() = _instanceFields

    val directMethods: List<EncodedMethod>
        get() = _directMethods

    val virtualMethods: List<EncodedMethod>
        get() = _virtualMethods

    val fields: List<EncodedField>
        get() = staticFields + instanceFields

    val methods: List<EncodedMethod>
        get() = directMethods + virtualMethods

    override val isEmpty: Boolean
        get() = fields.isEmpty() && methods.isEmpty()

    internal fun addField(field: EncodedField) {
        if (field.isStatic) {
            _staticFields.add(field)
        } else {
            _instanceFields.add(field)
        }
    }

    internal fun addMethod(method: EncodedMethod) {
        if (method.isDirectMethod) {
            _directMethods.add(method)
        } else {
            _virtualMethods.add(method)
        }
    }

    internal fun sort() {
        _staticFields.sortWith(compareBy { it.fieldIndex })
        _instanceFields.sortWith(compareBy { it.fieldIndex })

        _directMethods.sortWith(compareBy { it.methodIndex })
        _virtualMethods.sortWith(compareBy { it.methodIndex })

        methods.forEach { it.sort() }
    }

    override fun read(input: DexDataInput) {
        // field/method sizes are not stored explicitly,
        // use the size() method of the corresponding list instead.
        val staticFieldsSize   = input.readUleb128()
        val instanceFieldsSize = input.readUleb128()
        val directMethodsSize  = input.readUleb128()
        val virtualMethodsSize = input.readUleb128()

        var lastIndex = 0
        _staticFields = mutableListOfCapacity(staticFieldsSize)
        for (i in 0 until staticFieldsSize) {
            val encodedField = EncodedField.readContent(input, lastIndex)
            lastIndex = encodedField.fieldIndex
            _staticFields.add(encodedField)
        }

        lastIndex = 0
        _instanceFields = mutableListOfCapacity(instanceFieldsSize)
        for (i in 0 until instanceFieldsSize) {
            val encodedField = EncodedField.readContent(input, lastIndex)
            lastIndex = encodedField.fieldIndex
            _instanceFields.add(encodedField)
        }

        lastIndex = 0
        _directMethods = mutableListOfCapacity(directMethodsSize)
        for (i in 0 until directMethodsSize) {
            val encodedMethod = EncodedMethod.readContent(input, lastIndex)
            lastIndex = encodedMethod.methodIndex
            _directMethods.add(encodedMethod)
        }

        lastIndex = 0
        _virtualMethods = mutableListOfCapacity(virtualMethodsSize)
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
        return "ClassData[staticFields=${staticFields.size} fields,instanceFields=${instanceFields.size} fields," +
                         "directMethods=${directMethods.size} methods,virtualMethods=${virtualMethods.size} methods]"
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
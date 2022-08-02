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

import com.github.netomi.bat.dexfile.annotation.AnnotationsDirectory
import com.github.netomi.bat.dexfile.annotation.visitor.AnnotationSetVisitor
import com.github.netomi.bat.dexfile.annotation.visitor.AnnotationsDirectoryVisitor
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.dexfile.util.DexClasses.getDefaultEncodedValueForType
import com.github.netomi.bat.dexfile.value.EncodedValue
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.*
import com.google.common.base.Preconditions
import java.util.*

/**
 * A class representing a class def item inside a dex file.
 *
 * @see [class def item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.class-def-item)
 */
@DataItemAnn(
    type          = TYPE_CLASS_DEF_ITEM,
    dataAlignment = 4,
    dataSection   = false)
class ClassDef private constructor(
    _classIndex:           Int                  = NO_INDEX,
    _accessFlags:          Int                  = 0,
    _superClassIndex:      Int                  = NO_INDEX,
    _sourceFileIndex:      Int                  = NO_INDEX,
    _interfaces:           TypeList             = TypeList.empty(),
    _annotationsDirectory: AnnotationsDirectory = AnnotationsDirectory.empty(),
    _classData:            ClassData            = ClassData.empty(),
    _staticValues:         EncodedArray         = EncodedArray.empty()) : DataItem() {

    var classIndex: Int = _classIndex
        internal set

    var accessFlags: Int = _accessFlags
        internal set

    val visibility: Visibility
        get() = Visibility.of(accessFlags)

    val modifiers: EnumSet<ClassModifier>
        get() = ClassModifier.setOf(accessFlags)

    var superClassIndex: Int = _superClassIndex
        internal set

    var sourceFileIndex: Int = _sourceFileIndex
        internal set

    var interfacesOffset = 0
        private set

    var annotationsOffset = 0
        private set

    var classDataOffset = 0
        private set

    var staticValuesOffset = 0
        private set

    var interfaces: TypeList = _interfaces
        private set

    var annotationsDirectory: AnnotationsDirectory = _annotationsDirectory
        private set

    var classData: ClassData = _classData
        private set

    var staticValues: EncodedArray = _staticValues
        private set

    fun getClassName(dexFile: DexFile): String {
        return DexClasses.internalClassNameFromInternalType(getType(dexFile))
    }

    fun getType(dexFile: DexFile): String {
        return dexFile.getTypeID(classIndex).getType(dexFile)
    }

    fun getSuperClassName(dexFile: DexFile): String? {
        val superClassType = getSuperClassType(dexFile)
        return if (superClassType == null) null else DexClasses.internalClassNameFromInternalType(superClassType)
    }

    fun getSuperClassType(dexFile: DexFile): String? {
        return dexFile.getTypeNullable(superClassIndex)
    }

    fun getSourceFile(dexFile: DexFile): String? {
        return dexFile.getStringNullable(sourceFileIndex)
    }

    internal fun addField(dexFile: DexFile, field: EncodedField) {
        val fieldClass = field.getFieldID(dexFile).getClassType(dexFile)
        Preconditions.checkArgument(fieldClass == getType(dexFile), "field class does not match this class")
        classData.fields.forEach { Preconditions.checkArgument(field.fieldIndex != it.fieldIndex, "field already exists in this class") }
        classData.addField(field)
    }

    internal fun addMethod(dexFile: DexFile, method: EncodedMethod) {
        val methodClass = method.getMethodID(dexFile).getClassType(dexFile)
        Preconditions.checkArgument(methodClass == getType(dexFile), "method class does not match this class")
        classData.methods.forEach { Preconditions.checkArgument(method.methodIndex != it.methodIndex, "method already exists in this class") }
        classData.addMethod(method)
    }

    internal fun getStaticFieldIndex(field: EncodedField): Int {
        return classData.staticFields.withIndex()
                                     .filter { it.value.fieldIndex == field.fieldIndex }
                                     .map { it.index }
                                     .firstOrNull() ?: NO_INDEX
    }

    fun getStaticValue(dexFile: DexFile, field: EncodedField): EncodedValue? {
        val fieldClass = field.getFieldID(dexFile).getClassType(dexFile)
        Preconditions.checkArgument(fieldClass == getType(dexFile), "field class does not match this class")

        val staticFieldIndex = getStaticFieldIndex(field)
        if (staticFieldIndex == NO_INDEX) {
            throw RuntimeException("trying to get a static value for a field that does not belong to this class: " + getType(dexFile))
        }

        return if (staticFieldIndex >= 0 &&
            staticFieldIndex < staticValues.array.values.size) {
            staticValues.array.values[staticFieldIndex]
        } else {
            null
        }
    }

    internal fun setStaticValue(dexFile: DexFile, field: EncodedField, value: EncodedValue) {
        val fieldClass = field.getFieldID(dexFile).getClassType(dexFile)
        Preconditions.checkArgument(fieldClass == getType(dexFile), "field class does not match this class")

        val staticFieldIndex = getStaticFieldIndex(field)
        if (staticFieldIndex == NO_INDEX) {
            throw RuntimeException("trying to add a static value for a field that does not belong to this class: " + getType(dexFile))
        }

        val currentStaticValues = staticValues.array.values.size
        if (currentStaticValues <= staticFieldIndex) {
            for (i in currentStaticValues until staticFieldIndex) {
                val currentField = classData.getStaticField(i)
                val type = currentField.getFieldID(dexFile).getType(dexFile)
                val encodedValue = getDefaultEncodedValueForType(type)
                staticValues.array.addEncodedValue(encodedValue)
            }
            staticValues.array.addEncodedValue(value)
        } else {
            staticValues.array.setEncodedValue(staticFieldIndex, value)
        }
    }

    override val isEmpty: Boolean
        get() = classIndex == NO_INDEX

    internal fun sort(dexFile: DexFile) {
        val staticValueMapping = classData.staticFields.associateWith { encodedField -> getStaticValue(dexFile, encodedField) }
        classData.staticFields.sortWith(compareBy { it.fieldIndex })
        classData.instanceFields.sortWith(compareBy { it.fieldIndex })

        // reconstruct the static values after the staticFields have been sorted.
        staticValues = EncodedArray.empty()
        for (i in classData.staticFields.indices.reversed()) {
            val field = classData.staticFields[i]
            val staticValue = staticValueMapping[field]
            if (staticValue != null) {
                setStaticValue(dexFile, field, staticValue)
            }
        }

        classData.directMethods.sortWith(compareBy { it.methodIndex })
        classData.virtualMethods.sortWith(compareBy { it.methodIndex })

        classData.methods.forEach { it.sort() }
    }

    public override fun read(input: DexDataInput) {
        classIndex         = input.readInt()
        accessFlags        = input.readInt()
        superClassIndex    = input.readInt()
        interfacesOffset   = input.readInt()
        sourceFileIndex    = input.readInt()
        annotationsOffset  = input.readInt()
        classDataOffset    = input.readInt()
        staticValuesOffset = input.readInt()
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        if (interfacesOffset != 0) {
            input.offset = interfacesOffset
            interfaces = TypeList.readContent(input)
        }

        if (annotationsOffset != 0) {
            input.offset = annotationsOffset
            annotationsDirectory = AnnotationsDirectory.readContent(input)
        }

        if (classDataOffset != 0) {
            input.offset = classDataOffset
            classData = ClassData.readContent(input)
        }

        if (staticValuesOffset != 0) {
            input.offset = staticValuesOffset
            staticValues = EncodedArray.readContent(input)
        }
    }

    override fun updateOffsets(dataItemMap: Map) {
        interfacesOffset   = dataItemMap.getOffset(interfaces)
        annotationsOffset  = dataItemMap.getOffset(annotationsDirectory)
        classDataOffset    = dataItemMap.getOffset(classData)
        staticValuesOffset = dataItemMap.getOffset(staticValues)
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(classIndex)
        output.writeInt(accessFlags)
        output.writeInt(superClassIndex)
        output.writeInt(interfacesOffset)
        output.writeInt(sourceFileIndex)
        output.writeInt(annotationsOffset)
        output.writeInt(classDataOffset)
        output.writeInt(staticValuesOffset)
    }

    fun accept(dexFile: DexFile, visitor: ClassDefVisitor) {
        visitor.visitClassDef(dexFile, 0, this)
    }

    fun fieldsAccept(dexFile: DexFile, visitor: EncodedFieldVisitor) {
        classDataAccept(dexFile, allFields(visitor))
    }

    fun methodsAccept(dexFile: DexFile, visitor: EncodedMethodVisitor) {
        classDataAccept(dexFile, allMethods(visitor))
    }

    fun methodsAccept(dexFile: DexFile, nameExpression: String, visitor: EncodedMethodVisitor) {
        classDataAccept(dexFile, allMethods(filterMethodsByName(nameExpression, visitor)))
    }

    fun interfacesAccept(dexFile: DexFile, visitor: TypeVisitor) {
        interfaces.typesAccept(dexFile, visitor)
    }

    fun classDataAccept(dexFile: DexFile, visitor: ClassDataVisitor) {
        visitor.visitClassData(dexFile, this, classData)
    }

    fun annotationsDirectoryAccept(dexFile: DexFile, visitor: AnnotationsDirectoryVisitor) {
        visitor.visitAnnotationsDirectory(dexFile, this, annotationsDirectory)
    }

    fun annotationSetsAccept(dexFile: DexFile, visitor: AnnotationSetVisitor) {
        annotationsDirectory.accept(dexFile, this, visitor)
    }

    fun staticValueAccept(dexFile: DexFile, index: Int, visitor: EncodedValueVisitor) {
        staticValues.accept(dexFile, index, visitor)
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitInterfaceTypes(dexFile, this, interfaces)

        visitor.visitAnnotationsDirectory(dexFile, this, annotationsDirectory)
        annotationsDirectory.dataItemsAccept(dexFile, visitor)

        visitor.visitClassData(dexFile, this, classData)
        classData.dataItemsAccept(dexFile, visitor)

        visitor.visitStaticValuesArray(dexFile, this, staticValues)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitTypeID(dexFile, PropertyAccessor({ classIndex }, { classIndex = it }))

        if (superClassIndex != NO_INDEX) {
            visitor.visitTypeID(dexFile, PropertyAccessor({ superClassIndex }, { superClassIndex = it }))
        }

        if (sourceFileIndex != NO_INDEX) {
            visitor.visitStringID(dexFile, PropertyAccessor({ sourceFileIndex }, { sourceFileIndex = it }))
        }

        interfaces.referencedIDsAccept(dexFile, visitor)
        classData.referencedIDsAccept(dexFile, visitor)
        staticValues.referencedIDsAccept(dexFile, visitor)
        annotationsDirectory.referencedIDsAccept(dexFile, visitor)
    }

    override fun toString(): String {
        return "ClassDef[classIndex=%d,accessFlags=%04x]".format(classIndex, accessFlags)
    }

    companion object {
        fun of(classIndex: Int, accessFlags: Int, superClassIndex: Int = NO_INDEX, sourceFileIndex: Int = NO_INDEX): ClassDef {
            return ClassDef(classIndex,
                            accessFlags,
                            superClassIndex,
                            sourceFileIndex,
                            TypeList.empty(),
                            AnnotationsDirectory.empty(),
                            ClassData.empty(),
                            EncodedArray.empty()
            )
        }

        fun readContent(input: DexDataInput): ClassDef {
            val classDef = ClassDef()
            classDef.read(input)
            return classDef
        }
    }
}
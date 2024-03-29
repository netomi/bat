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
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.util.DexType
import com.github.netomi.bat.dexfile.value.EncodedValue
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.*

/**
 * A class representing a class def item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#class-def-item">class def item @ dex format</a>
 */
@DataItemAnn(
    type          = TYPE_CLASS_DEF_ITEM,
    dataAlignment = 4,
    dataSection   = false)
class ClassDef private constructor(            classIndex:           Int                  = NO_INDEX,
                                               accessFlags:          Int                  = 0,
                                               superClassIndex:      Int                  = NO_INDEX,
                                               sourceFileIndex:      Int                  = NO_INDEX,
                                               interfaces:           TypeList             = TypeList.empty(),
                                               annotationsDirectory: AnnotationsDirectory = AnnotationsDirectory.empty(),
                                               classData:            ClassData            = ClassData.empty(),
                                   private var staticValues:         EncodedArray         = EncodedArray.empty()) : DataItem() {

    var classIndex: Int = classIndex
        internal set

    var accessFlags: Int = accessFlags
        internal set(value) {
            field = value
            visibility = Visibility.of(value)
            modifiers  = ClassModifier.setOf(value)
        }

    // TODO: according to https://source.android.com/docs/core/dalvik/dex-format#access-flags
    //       only public (and maybe package-private) should be allowed for a class def.
    var visibility: Visibility = Visibility.of(accessFlags)
        private set

    var modifiers: Set<ClassModifier> = ClassModifier.setOf(accessFlags)
        private set

    var superClassIndex: Int = superClassIndex
        internal set

    var sourceFileIndex: Int = sourceFileIndex
        internal set

    var interfacesOffset = 0
        private set

    var annotationsOffset = 0
        private set

    var classDataOffset = 0
        private set

    var staticValuesOffset = 0
        private set

    var interfaces: TypeList = interfaces
        private set

    fun getInterfaces(dexFile: DexFile): List<DexType> {
        val collector = typeCollector()
        interfacesAccept(dexFile, collector)
        return collector.items()
    }

    internal var annotationsDirectory: AnnotationsDirectory = annotationsDirectory
        private set

    private var classData: ClassData = classData
        private set

    val hasMembers: Boolean
        get() = !classData.isEmpty

    val staticFields: List<EncodedField>
        get() = classData.staticFields

    val instanceFields: List<EncodedField>
        get() = classData.instanceFields

    val fields: List<EncodedField>
        get() = classData.fields

    val directMethods: List<EncodedMethod>
        get() = classData.directMethods

    val virtualMethods: List<EncodedMethod>
        get() = classData.virtualMethods

    val methods: List<EncodedMethod>
        get() = classData.methods

    fun getClassName(dexFile: DexFile): String {
        return getType(dexFile).toInternalClassName()
    }

    fun getType(dexFile: DexFile): DexType {
        return dexFile.getTypeID(classIndex).getType(dexFile)
    }

    fun getSuperClassName(dexFile: DexFile): String? {
        return getSuperClassType(dexFile)?.toInternalClassName()
    }

    fun getSuperClassType(dexFile: DexFile): DexType? {
        return dexFile.getTypeOrNull(superClassIndex)
    }

    fun getSourceFile(dexFile: DexFile): String? {
        return dexFile.getStringNullable(sourceFileIndex)
    }

    fun hasAnnotations(): Boolean {
        return !annotationsDirectory.isEmpty
    }

    internal fun addField(dexFile: DexFile, field: EncodedField, validate: Boolean = true) {
        if (validate) {
            val fieldID        = field.getFieldID(dexFile)
            val fieldClassType = fieldID.getClassType(dexFile)
            require(fieldClassType == getType(dexFile)) { "field class does not match this class" }
            classData.fields.forEach {
                require(field.fieldIndex != it.fieldIndex)
                    { "field '${fieldID.getFullExternalFieldSignature(dexFile)}' already exists in this class" }
            }
        }
        classData.addField(field)
    }

    internal fun addMethod(dexFile: DexFile, method: EncodedMethod, validate: Boolean = true) {
        if (validate) {
            val methodID        = method.getMethodID(dexFile)
            val methodClassType = methodID.getClassType(dexFile)
            require(methodClassType == getType(dexFile)) { "method class does not match this class" }
            classData.methods.forEach {
                require(method.methodIndex != it.methodIndex)
                    { "method '${methodID.getFullExternalMethodSignature(dexFile)}' already exists in this class" }
            }
        }
        classData.addMethod(method)
    }

    internal fun getStaticFieldIndex(field: EncodedField): Int {
        return classData.staticFields.withIndex()
                                     .filter { it.value.fieldIndex == field.fieldIndex }
                                     .map { it.index }
                                     .firstOrNull() ?: NO_INDEX
    }

    internal fun setStaticValue(dexFile: DexFile, field: EncodedField, value: EncodedValue) {
        val fieldClassType = field.getFieldID(dexFile).getClassType(dexFile)
        require(fieldClassType == getType(dexFile)) { "field class does not match this class" }

        val staticFieldIndex = getStaticFieldIndex(field)
        require(staticFieldIndex != NO_INDEX)
            { "trying to add a static value for a field that does not belong to this class: ${getType(dexFile)}" }

        val currentStaticValueCount = staticValues.array.size
        if (currentStaticValueCount <= staticFieldIndex) {
            for (i in currentStaticValueCount until staticFieldIndex) {
                val currentField = staticFields[i]
                val type = currentField.getFieldID(dexFile).getType(dexFile)
                val encodedValue = type.getDefaultEncodedValueForType()
                staticValues.array.add(encodedValue)
            }
            staticValues.array.add(value)
        } else {
            staticValues.array[staticFieldIndex] = value
        }
    }

    override val isEmpty: Boolean
        get() = classIndex == NO_INDEX

    internal fun sort(dexFile: DexFile) {
        val staticValueMapping = staticFields.associateWith { encodedField -> encodedField.staticValue(dexFile) }

        classData.sort()

        // reconstruct the static values after the staticFields have been sorted.
        staticValues = EncodedArray.empty()
        for (i in staticFields.indices.reversed()) {
            val field = staticFields[i]
            val staticValue = staticValueMapping[field]
            if (staticValue != null) {
                setStaticValue(dexFile, field, staticValue)
            }
        }
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
            interfaces = TypeList.read(input)
        }

        if (annotationsOffset != 0) {
            input.offset = annotationsOffset
            annotationsDirectory = AnnotationsDirectory.read(input)
        }

        if (classDataOffset != 0) {
            input.offset = classDataOffset
            classData = ClassData.read(input)
        }

        if (staticValuesOffset != 0) {
            input.offset = staticValuesOffset
            staticValues = EncodedArray.read(input)
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
        visitor.visitClassDef(dexFile, this)
    }

    fun accept(dexFile: DexFile, index: Int, visitor: ClassDefVisitorIndexed) {
        visitor.visitClassDef(dexFile, index, this)
    }

    fun staticFieldsAccept(dexFile: DexFile, visitor: EncodedFieldVisitor) {
        accept(dexFile, allStaticFields(visitor))
    }

    fun instanceFieldsAccept(dexFile: DexFile, visitor: EncodedFieldVisitor) {
        accept(dexFile, allInstanceFields(visitor))
    }

    fun fieldsAccept(dexFile: DexFile, visitor: EncodedFieldVisitor) {
        accept(dexFile, allFields(visitor))
    }

    fun fieldsAccept(dexFile: DexFile, nameExpression: String, visitor: EncodedFieldVisitor) {
        accept(dexFile, allFields(filterFieldsByName(nameExpression, visitor)))
    }

    fun fieldsAccept(dexFile: DexFile, nameExpression: String, type: String, visitor: EncodedFieldVisitor) {
        accept(dexFile, allFields(filterFieldsByNameAndType(nameExpression, type, visitor)))
    }

    fun directMethodsAccept(dexFile: DexFile, visitor: EncodedMethodVisitor) {
        accept(dexFile, allDirectMethods(visitor))
    }

    fun virtualMethodsAccept(dexFile: DexFile, visitor: EncodedMethodVisitor) {
        accept(dexFile, allVirtualMethods(visitor))
    }

    fun methodsAccept(dexFile: DexFile, visitor: EncodedMethodVisitor) {
        accept(dexFile, allMethods(visitor))
    }

    fun methodsAccept(dexFile: DexFile, nameExpression: String, visitor: EncodedMethodVisitor) {
        accept(dexFile, allMethods(filterMethodsByName(nameExpression, visitor)))
    }

    fun interfacesAccept(dexFile: DexFile, visitor: TypeVisitor) {
        interfaces.typesAccept(dexFile, visitor)
    }

    internal fun classDataAccept(dexFile: DexFile, visitor: ClassDataVisitor) {
        visitor.visitClassData(dexFile, this, classData)
    }

    fun classAnnotationSetAccept(dexFile: DexFile, classDef: ClassDef, visitor: AnnotationSetVisitor) {
        visitor.visitClassAnnotationSet(dexFile, classDef, annotationsDirectory.classAnnotations)
    }

    fun fieldAnnotationSetAccept(dexFile: DexFile, classDef: ClassDef, field: EncodedField, visitor: AnnotationSetVisitor) {
        annotationsDirectory.fieldAnnotations.filter { it.fieldIndex == field.fieldIndex }.map { it.accept(dexFile, classDef, visitor) }
    }

    fun methodAnnotationSetAccept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, visitor: AnnotationSetVisitor) {
        annotationsDirectory.methodAnnotations.filter { it.methodIndex == method.methodIndex }.map { it.accept(dexFile, classDef, visitor) }
    }

    fun parameterAnnotationSetRefListAccept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, visitor: AnnotationSetVisitor) {
        annotationsDirectory.parameterAnnotations.filter { it.methodIndex == method.methodIndex }.map { it.accept(dexFile, classDef, visitor) }
    }

    fun parameterAnnotationSetAccept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, parameterIndex: Int, visitor: AnnotationSetVisitor) {
        annotationsDirectory.parameterAnnotations.filter { it.methodIndex == method.methodIndex }.map { it.accept(dexFile, classDef, parameterIndex, visitor) }
    }

    fun annotationSetsAccept(dexFile: DexFile, visitor: AnnotationSetVisitor) {
        annotationsDirectory.accept(dexFile, this, visitor)
    }

    fun staticValueAccept(dexFile: DexFile, field: EncodedField, visitor: EncodedValueVisitor) {
        val staticFieldIndex = getStaticFieldIndex(field)
        require(staticFieldIndex != NO_INDEX)
            { "trying to call staticValueAccept for a field that does not belong to this class: ${getType(dexFile)}" }
        staticValueAccept(dexFile, staticFieldIndex, visitor)
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
        visitor.visitTypeID(dexFile, PropertyAccessor(::classIndex))

        if (superClassIndex != NO_INDEX) {
            visitor.visitTypeID(dexFile, PropertyAccessor(::superClassIndex))
        }

        if (sourceFileIndex != NO_INDEX) {
            visitor.visitStringID(dexFile, PropertyAccessor(::sourceFileIndex))
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
            return ClassDef(classIndex, accessFlags, superClassIndex, sourceFileIndex)
        }

        internal fun read(input: DexDataInput): ClassDef {
            val classDef = ClassDef()
            classDef.read(input)
            return classDef
        }
    }
}
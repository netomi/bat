package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class EnumElementValue internal constructor(
    var typeNameIndex:  Int = -1,
    var constNameIndex: Int = -1) : ElementValue() {

    override val type: Type
        get() = Type.ENUM

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        typeNameIndex  = input.readUnsignedShort()
        constNameIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        output.writeShort(typeNameIndex)
        output.writeShort(constNameIndex)
    }

    override fun accept(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, visitor: ElementValueVisitor) {
        visitor.visitEnumElementValue(classFile, annotation, index, elementName, this)
    }

    companion object {
        @JvmStatic
        fun create(): EnumElementValue {
            return EnumElementValue()
        }
    }
}
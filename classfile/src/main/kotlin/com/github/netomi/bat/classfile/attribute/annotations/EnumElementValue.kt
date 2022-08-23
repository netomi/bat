package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotations.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class EnumElementValue private constructor(private var _typeNameIndex:  Int = -1,
                                                private var _constNameIndex: Int = -1) : ElementValue() {

    override val type: ElementValueType
        get() = ElementValueType.ENUM

    val typeNameIndex: Int
        get() = _typeNameIndex

    val constNameIndex: Int
        get() = _constNameIndex

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        _typeNameIndex  = input.readUnsignedShort()
        _constNameIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        output.writeShort(typeNameIndex)
        output.writeShort(constNameIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ElementValueVisitor) {
        visitor.visitEnumElementValue(classFile, this)
    }

    companion object {
        internal fun empty(): EnumElementValue {
            return EnumElementValue()
        }
    }
}
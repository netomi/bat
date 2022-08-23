package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotations.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class ClassElementValue private constructor(private var _classIndex: Int = -1) : ElementValue() {

    override val type: ElementValueType
        get() = ElementValueType.CLASS

    val classIndex: Int
        get() = _classIndex

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        _classIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        output.writeShort(classIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ElementValueVisitor) {
        visitor.visitClassElementValue(classFile, this)
    }

    companion object {
        internal fun empty(): ClassElementValue {
            return ClassElementValue()
        }
    }
}
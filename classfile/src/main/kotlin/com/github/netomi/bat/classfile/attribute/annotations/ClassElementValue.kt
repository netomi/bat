package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotations.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class ClassElementValue internal constructor(var classInfoIndex: Int = -1) : ElementValue() {

    override val type: ElementValueType
        get() = ElementValueType.CLASS

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        classInfoIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        output.writeShort(classInfoIndex)
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
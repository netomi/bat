package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class ClassElementValue internal constructor(var classInfoIndex: Int = -1) : ElementValue() {

    override val type: Type
        get() = Type.CLASS

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        classInfoIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        output.writeShort(classInfoIndex)
    }

    override fun accept(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, visitor: ElementValueVisitor) {
        visitor.visitClassElementValue(classFile, annotation, index, elementName, this)
    }

    companion object {
        @JvmStatic
        fun create(): ClassElementValue {
            return ClassElementValue()
        }
    }
}
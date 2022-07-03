package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class ArrayElementValue
    internal constructor(var elementValues: MutableList<ElementValue> = mutableListOf()) : ElementValue() {

    override val type: Type
        get() = Type.ARRAY

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        val elementValueCount = input.readUnsignedShort()
        for (i in 0 until elementValueCount) {
            elementValues.add(ElementValue.read(input))
        }
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        output.writeShort(elementValues.size)
        elementValues.forEach { it.write(output) }
    }

    override fun accept(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, visitor: ElementValueVisitor) {
        return visitor.visitArrayElementValue(classFile, annotation, index, elementName, this)
    }

    fun acceptElementValues(classFile: ClassFile, annotation: Annotation, visitor: ElementValueVisitor) {
        elementValues.forEachIndexed { index, elementValue ->
            elementValue.accept(classFile, annotation, index, null, visitor)
        }
    }

    companion object {
        @JvmStatic
        fun create(): ArrayElementValue {
            return ArrayElementValue()
        }
    }
}
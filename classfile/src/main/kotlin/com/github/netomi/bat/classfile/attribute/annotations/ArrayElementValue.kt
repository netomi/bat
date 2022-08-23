package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotations.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class ArrayElementValue
    internal constructor(var elementValues: MutableList<ElementValue> = mutableListOf()) : ElementValue() {

    override val type: ElementValueType
        get() = ElementValueType.ARRAY

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        val elementValueCount = input.readUnsignedShort()
        for (i in 0 until elementValueCount) {
            elementValues.add(read(input))
        }
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        output.writeShort(elementValues.size)
        elementValues.forEach { it.write(output) }
    }

    override fun accept(classFile: ClassFile, visitor: ElementValueVisitor) {
        return visitor.visitArrayElementValue(classFile, this)
    }

    fun acceptElementValues(classFile: ClassFile, visitor: ElementValueVisitor) {
        elementValues.forEach { elementValue -> elementValue.accept(classFile, visitor) }
    }

    companion object {
        internal fun empty(): ArrayElementValue {
            return ArrayElementValue()
        }
    }
}
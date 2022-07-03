package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.ConstantPool
import com.github.netomi.bat.classfile.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class Annotation internal constructor(
    var typeIndex:     Int                                  = -1,
    var elementValues: MutableList<Pair<Int, ElementValue>> = mutableListOf()) {

    fun type(constantPool: ConstantPool): String {
        return constantPool.getString(typeIndex)
    }

    @Throws(IOException::class)
    private fun read(input: DataInput) {
        typeIndex = input.readUnsignedShort()

        val elementValuesCount = input.readUnsignedShort()
        for (i in 0 until elementValuesCount) {
            val elementNameIndex = input.readUnsignedShort()
            elementValues.add(Pair(elementNameIndex, ElementValue.read(input)))
        }
    }

    @Throws(IOException::class)
    fun write(output: DataOutput) {
        output.writeShort(typeIndex)
        output.writeShort(elementValues.size)
        elementValues.forEach { (elementNameIndex, elementValue) ->
            output.writeShort(elementNameIndex)
            elementValue.write(output)
        }
    }

    fun acceptElementValues(classFile: ClassFile, visitor: ElementValueVisitor) {
        elementValues.forEachIndexed { index, (elementName, elementValue) ->
            elementValue.accept(classFile, this, index, classFile.cp.getString(elementName), visitor)
        }
    }

    companion object {
        @JvmStatic
        fun readAnnotation(input: DataInput): Annotation {
            val annotation = Annotation()
            annotation.read(input)
            return annotation
        }
    }
}
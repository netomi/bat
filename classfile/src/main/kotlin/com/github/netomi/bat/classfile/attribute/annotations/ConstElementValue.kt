package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class ConstElementValue internal constructor(override val type:   ElementValue.Type,
                                                  var constValueIndex: Int = -1) : ElementValue() {

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        constValueIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        output.writeShort(constValueIndex)
    }

    override fun accept(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, visitor: ElementValueVisitor) {
        when(type) {
            Type.BYTE    -> visitor.visitByteElementValue(classFile, annotation, index, elementName, this)
            Type.CHAR    -> visitor.visitCharElementValue(classFile, annotation, index, elementName, this)
            Type.DOUBLE  -> visitor.visitDoubleElementValue(classFile, annotation, index, elementName, this)
            Type.FLOAT   -> visitor.visitFloatElementValue(classFile, annotation, index, elementName, this)
            Type.INT     -> visitor.visitIntElementValue(classFile, annotation, index, elementName, this)
            Type.LONG    -> visitor.visitLongElementValue(classFile, annotation, index, elementName, this)
            Type.SHORT   -> visitor.visitShortElementValue(classFile, annotation, index, elementName, this)
            Type.BOOLEAN -> visitor.visitBooleanElementValue(classFile, annotation, index, elementName, this)
            Type.STRING  -> visitor.visitStringElementValue(classFile, annotation, index, elementName, this)
            else -> error("ConstElementValue has unexpected type $type")
        }
    }

    companion object {
        @JvmStatic
        fun create(type: ElementValue.Type): ConstElementValue {
            return ConstElementValue(type)
        }
    }
}
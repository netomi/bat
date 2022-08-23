package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotations.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class ConstElementValue private constructor(override val type:             ElementValueType,
                                                  private var _constValueIndex: Int = -1) : ElementValue() {

    val constValueIndex: Int
        get() = _constValueIndex

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        _constValueIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        output.writeShort(constValueIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ElementValueVisitor) {
        when(type) {
            ElementValueType.BYTE    -> visitor.visitByteElementValue(classFile, this)
            ElementValueType.CHAR    -> visitor.visitCharElementValue(classFile, this)
            ElementValueType.DOUBLE  -> visitor.visitDoubleElementValue(classFile, this)
            ElementValueType.FLOAT   -> visitor.visitFloatElementValue(classFile, this)
            ElementValueType.INT     -> visitor.visitIntElementValue(classFile, this)
            ElementValueType.LONG    -> visitor.visitLongElementValue(classFile, this)
            ElementValueType.SHORT   -> visitor.visitShortElementValue(classFile, this)
            ElementValueType.BOOLEAN -> visitor.visitBooleanElementValue(classFile, this)
            ElementValueType.STRING  -> visitor.visitStringElementValue(classFile, this)
            else -> error("ConstElementValue has unexpected type $type")
        }
    }

    companion object {
        internal fun create(type: ElementValueType): ConstElementValue {
            return ConstElementValue(type)
        }
    }
}
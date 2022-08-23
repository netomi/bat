package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotations.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

abstract class ElementValue {

    internal abstract val type: ElementValueType

    @Throws(IOException::class)
    abstract fun readElementValue(input: DataInput)

    @Throws(IOException::class)
    abstract fun writeElementValue(output: DataOutput)

    @Throws(IOException::class)
    fun write(output: DataOutput) {
        output.writeByte(type.tag.code)
        writeElementValue(output)
    }

    abstract fun accept(classFile: ClassFile, visitor: ElementValueVisitor)

    companion object {
        internal fun read(input : DataInput): ElementValue {
            val tag           = input.readUnsignedByte().toChar()
            val elementValue  = ElementValueType.of(tag).createElementValue()
            elementValue.readElementValue(input)
            return elementValue
        }
    }
}

/**
 * Known element value elements contained in an annotation.
 */
internal enum class ElementValueType constructor(val tag: Char, private val supplier: () -> ElementValue) {

    // Valid element values and their corresponding tags:
    // https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.7.16.1-130

    BYTE      ('B', { ConstElementValue.create(BYTE) }),
    CHAR      ('C', { ConstElementValue.create(CHAR) }),
    DOUBLE    ('D', { ConstElementValue.create(DOUBLE) }),
    FLOAT     ('F', { ConstElementValue.create(FLOAT) }),
    INT       ('I', { ConstElementValue.create(INT) }),
    LONG      ('J', { ConstElementValue.create(LONG) }),
    SHORT     ('S', { ConstElementValue.create(SHORT) }),
    BOOLEAN   ('Z', { ConstElementValue.create(BOOLEAN) }),
    STRING    ('s', { ConstElementValue.create(STRING) }),
    CLASS     ('c', ClassElementValue.Companion::empty),
    ENUM      ('e', EnumElementValue.Companion::empty),
    ANNOTATION('@', AnnotationElementValue.Companion::empty),
    ARRAY     ('[', ArrayElementValue.Companion::empty);

    companion object {
        private val tagToElementValueMap: Map<Char, ElementValueType> by lazy {
            values().associateBy { it.tag }
        }

        fun of(tag: Char) : ElementValueType {
            return tagToElementValueMap[tag] ?: throw IllegalArgumentException("unknown element value tag '$tag'")
        }
    }

    fun createElementValue(): ElementValue {
        return supplier.invoke()
    }
}

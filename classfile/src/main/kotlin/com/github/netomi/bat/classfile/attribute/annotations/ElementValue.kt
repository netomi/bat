package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

abstract class ElementValue {

    abstract val type: Type

    @Throws(IOException::class)
    abstract fun readElementValue(input: DataInput)

    @Throws(IOException::class)
    abstract fun writeElementValue(output: DataOutput)

    @Throws(IOException::class)
    fun write(output: DataOutput) {
        output.writeByte(type.tag.code)
        writeElementValue(output)
    }

    abstract fun accept(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, visitor: ElementValueVisitor)

    companion object {
        @JvmStatic
        fun read(input : DataInput): ElementValue {
            val tag           = input.readUnsignedByte().toChar()
            val elementValue  = Type.of(tag).createElementValue()
            elementValue.readElementValue(input)
            return elementValue
        }
    }

    /**
     * Known element value elements contained in an annotation.
     */
    enum class Type constructor(val tag: Char, val supplier: () -> ElementValue) {

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
        CLASS     ('c', ClassElementValue.Companion::create),
        ENUM      ('e', EnumElementValue.Companion::create),
        ANNOTATION('@', AnnotationElementValue.Companion::create),
        ARRAY     ('[', ArrayElementValue.Companion::create);

        companion object {
            private val tagToElementValueMap: Map<Char, Type> by lazy {
                values().associateBy { it.tag }
            }

            fun of(tag: Char) : Type {
                return tagToElementValueMap[tag] ?: throw IllegalArgumentException("Unknown element value tag '$tag'")
            }
        }

        fun createElementValue(): ElementValue {
            return supplier.invoke()
        }
    }
}
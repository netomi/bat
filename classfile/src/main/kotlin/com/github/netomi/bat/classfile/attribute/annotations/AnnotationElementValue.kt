package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.visitor.ElementValueVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class AnnotationElementValue internal constructor(var annotation: Annotation = Annotation()) : ElementValue() {

    override val type: Type
        get() = Type.ANNOTATION

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        annotation = Annotation.readAnnotation(input)
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        annotation.write(output)
    }

    override fun accept(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, visitor: ElementValueVisitor) {
        visitor.visitAnnotationElementValue(classFile, annotation, index, elementName, this)
    }

    companion object {
        @JvmStatic
        fun create(): AnnotationElementValue {
            return AnnotationElementValue()
        }
    }
}
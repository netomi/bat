package com.github.netomi.bat.classfile.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotations.Annotation
import com.github.netomi.bat.classfile.attribute.annotations.*

interface ElementValueVisitor {

    fun visitAnyElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ElementValue) {}

    fun visitClassElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ClassElementValue) {
        visitAnyElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitEnumElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: EnumElementValue) {
        visitAnyElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitArrayElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ArrayElementValue) {
        visitAnyElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitAnnotationElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: AnnotationElementValue) {
        visitAnyElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitAnyConstElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ConstElementValue) {
        visitAnyElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitByteElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitCharElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitIntElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitLongElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitShortElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitFloatElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitDoubleElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitBooleanElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, annotation, index, elementName, elementValue)
    }

    fun visitStringElementValue(classFile: ClassFile, annotation: Annotation, index: Int, elementName: String?, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, annotation, index, elementName, elementValue)
    }
}
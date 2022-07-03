package com.github.netomi.bat.classfile.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeInvisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeVisibleAnnotationsAttribute

interface AttributeVisitor {
    fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {}

    fun visitConstantValueAttribute(classFile: ClassFile, attribute: ConstantValueAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitDeprecatedAttribute(classFile: ClassFile, attribute: DeprecatedAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitEnclosingMethodAttribute(classFile: ClassFile, attribute: EnclosingMethodAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSignatureAttribute(classFile: ClassFile, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSourceFileAttribute(classFile: ClassFile, attribute: SourceFileAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSyntheticAttribute(classFile: ClassFile, attribute: SyntheticAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitUnknownAttribute(classFile: ClassFile, attribute: UnknownAttribute) {
        visitAnyAttribute(classFile, attribute)
    }
}
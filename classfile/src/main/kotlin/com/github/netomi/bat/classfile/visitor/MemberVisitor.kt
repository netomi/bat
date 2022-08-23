package com.github.netomi.bat.classfile.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.Member
import com.github.netomi.bat.classfile.Method

fun interface MemberVisitor {
    fun visitAnyMember(classFile: ClassFile, index: Int, member: Member)

    fun visitField(classFile: ClassFile, index: Int, field: Field) {
        visitAnyMember(classFile, index, field)
    }

    fun visitMethod(classFile: ClassFile, index: Int, method: Method) {
        visitAnyMember(classFile, index, method)
    }
}
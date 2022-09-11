package com.github.netomi.bat.classfile.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.Member
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.util.simpleNameMatcher

fun filterMembersByName(nameExpression: String, visitor: MemberVisitor): MemberVisitor {
    val nameMatcher = simpleNameMatcher(nameExpression)
    return MemberVisitor { classFile, index, member ->
        if (nameMatcher.matches(member.getName(classFile))) {
            member.accept(classFile, index, visitor)
        }
    }
}

fun interface MemberVisitor: FieldVisitor, MethodVisitor {
    fun visitAnyMember(classFile: ClassFile, index: Int, member: Member)

    override fun visitField(classFile: ClassFile, index: Int, field: Field) {
        visitAnyMember(classFile, index, field)
    }

    override fun visitMethod(classFile: ClassFile, index: Int, method: Method) {
        visitAnyMember(classFile, index, method)
    }
}

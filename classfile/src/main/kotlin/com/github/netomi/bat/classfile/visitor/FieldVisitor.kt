package com.github.netomi.bat.classfile.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.util.simpleNameMatcher
import com.github.netomi.bat.visitor.AbstractCollector

fun filterFieldsByName(nameExpression: String, visitor: FieldVisitor): FieldVisitor {
    val nameMatcher = simpleNameMatcher(nameExpression)
    return FieldVisitor { classFile, index, field ->
        if (nameMatcher.matches(field.getName(classFile))) {
            field.accept(classFile, index, visitor)
        }
    }
}

fun fieldCollector(): FieldCollector {
    return FieldCollector()
}

fun interface FieldVisitor {
    fun visitField(classFile: ClassFile, index: Int, field: Field)
}

class FieldCollector: AbstractCollector<Field>(), FieldVisitor {
    override fun visitField(classFile: ClassFile, index: Int, field: Field) {
        addItem(field)
    }
}

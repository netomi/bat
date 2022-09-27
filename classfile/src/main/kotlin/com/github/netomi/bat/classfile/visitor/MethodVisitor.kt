package com.github.netomi.bat.classfile.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.util.simpleNameMatcher
import com.github.netomi.bat.visitor.AbstractCollector

fun filterMethodsByName(nameExpression: String, visitor: MethodVisitor): MethodVisitor {
    val nameMatcher = simpleNameMatcher(nameExpression)
    return MethodVisitor { classFile, index, method ->
        if (nameMatcher.matches(method.getName(classFile))) {
            method.accept(classFile, index, visitor)
        }
    }
}

fun filterMethodsByNameAndDescriptor(nameExpression: String, descriptor: String, visitor: MethodVisitor): MethodVisitor {
    val nameMatcher = simpleNameMatcher(nameExpression)
    return MethodVisitor { classFile, index, method ->
        if (nameMatcher.matches(method.getName(classFile)) &&
            descriptor == method.getDescriptor(classFile)) {
            method.accept(classFile, index, visitor)
        }
    }
}

fun allCode(visitor: MethodAttributeVisitor): MethodVisitor {
    return MethodVisitor { classFile, _, method ->
        method.attributeMap.get<CodeAttribute>(AttributeType.CODE)?.accept(classFile, method, visitor)
    }
}

fun methodCollector(): MethodCollector {
    return MethodCollector()
}

fun interface MethodVisitor {
    fun visitMethod(classFile: ClassFile, index: Int, method: Method)
}

class MethodCollector: AbstractCollector<Method>(), MethodVisitor {
    override fun visitMethod(classFile: ClassFile, index: Int, method: Method) {
        addItem(method)
    }
}

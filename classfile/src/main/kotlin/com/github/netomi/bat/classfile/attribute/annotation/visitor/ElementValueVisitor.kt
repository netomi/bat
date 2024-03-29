/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.netomi.bat.classfile.attribute.annotation.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.visitor.AbstractMultiVisitor
import java.util.function.BiConsumer

fun multiElementValueVisitorOf(visitor: ElementValueVisitor, vararg visitors: ElementValueVisitor): ElementValueVisitor {
    return MultiElementValueVisitor(visitor, *visitors)
}

fun interface ElementValueVisitor {

    fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue)

    fun visitClassElementValue(classFile: ClassFile, elementValue: ClassElementValue) {
        visitAnyElementValue(classFile, elementValue)
    }

    fun visitEnumElementValue(classFile: ClassFile, elementValue: EnumElementValue) {
        visitAnyElementValue(classFile, elementValue)
    }

    fun visitArrayElementValue(classFile: ClassFile, elementValue: ArrayElementValue) {
        visitAnyElementValue(classFile, elementValue)
    }

    fun visitAnnotationElementValue(classFile: ClassFile, elementValue: AnnotationElementValue) {
        visitAnyElementValue(classFile, elementValue)
    }

    fun visitAnyConstElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        visitAnyElementValue(classFile, elementValue)
    }

    fun visitByteElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, elementValue)
    }

    fun visitCharElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, elementValue)
    }

    fun visitIntElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, elementValue)
    }

    fun visitLongElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, elementValue)
    }

    fun visitShortElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, elementValue)
    }

    fun visitFloatElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, elementValue)
    }

    fun visitDoubleElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, elementValue)
    }

    fun visitBooleanElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, elementValue)
    }

    fun visitStringElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        visitAnyConstElementValue(classFile, elementValue)
    }

    fun joinedByElementValueConsumer(consumer: BiConsumer<ClassFile, ElementValue>): ElementValueVisitor {
        val joiner: ElementValueVisitor = object : ElementValueVisitor {
            private var firstVisited = false
            override fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue) {
                if (firstVisited) {
                    consumer.accept(classFile, elementValue)
                } else {
                    firstVisited = true
                }
            }
        }
        return multiElementValueVisitorOf(joiner, this)
    }
}

private class MultiElementValueVisitor constructor(       visitor:       ElementValueVisitor,
                                                   vararg otherVisitors: ElementValueVisitor)
    : AbstractMultiVisitor<ElementValueVisitor>(visitor, *otherVisitors), ElementValueVisitor {

    override fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue) {
        for (visitor in visitors) {
            elementValue.accept(classFile, visitor)
        }
    }
}
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

package com.github.netomi.bat.classfile.annotation.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.annotation.*
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

private class MultiElementValueVisitor constructor(visitor: ElementValueVisitor,
                                                   vararg otherVisitors: ElementValueVisitor
)
    : AbstractMultiVisitor<ElementValueVisitor>(visitor, *otherVisitors), ElementValueVisitor {

    override fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue) {
        for (visitor in visitors) {
            visitor.visitAnyElementValue(classFile, elementValue)
        }
    }

    override fun visitClassElementValue(classFile: ClassFile, elementValue: ClassElementValue) {
        for (visitor in visitors) {
            visitor.visitClassElementValue(classFile, elementValue)
        }
    }

    override fun visitEnumElementValue(classFile: ClassFile, elementValue: EnumElementValue) {
        for (visitor in visitors) {
            visitor.visitEnumElementValue(classFile, elementValue)
        }
    }

    override fun visitArrayElementValue(classFile: ClassFile, elementValue: ArrayElementValue) {
        for (visitor in visitors) {
            visitor.visitArrayElementValue(classFile, elementValue)
        }
    }

    override fun visitAnnotationElementValue(classFile: ClassFile, elementValue: AnnotationElementValue) {
        for (visitor in visitors) {
            visitor.visitAnnotationElementValue(classFile, elementValue)
        }
    }

    override fun visitAnyConstElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        for (visitor in visitors) {
            visitor.visitAnyConstElementValue(classFile, elementValue)
        }
    }

    override fun visitByteElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        for (visitor in visitors) {
            visitor.visitByteElementValue(classFile, elementValue)
        }
    }

    override fun visitCharElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        for (visitor in visitors) {
            visitor.visitCharElementValue(classFile, elementValue)
        }
    }

    override fun visitIntElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        for (visitor in visitors) {
            visitor.visitIntElementValue(classFile, elementValue)
        }
    }

    override fun visitLongElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        for (visitor in visitors) {
            visitor.visitLongElementValue(classFile, elementValue)
        }
    }

    override fun visitShortElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        for (visitor in visitors) {
            visitor.visitShortElementValue(classFile, elementValue)
        }
    }

    override fun visitFloatElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        for (visitor in visitors) {
            visitor.visitFloatElementValue(classFile, elementValue)
        }
    }

    override fun visitDoubleElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        for (visitor in visitors) {
            visitor.visitDoubleElementValue(classFile, elementValue)
        }
    }

    override fun visitBooleanElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        for (visitor in visitors) {
            visitor.visitBooleanElementValue(classFile, elementValue)
        }
    }

    override fun visitStringElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        for (visitor in visitors) {
            visitor.visitStringElementValue(classFile, elementValue)
        }
    }
}
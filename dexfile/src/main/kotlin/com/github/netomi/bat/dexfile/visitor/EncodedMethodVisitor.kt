/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.dexfile.visitor

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.util.StringMatcher
import com.github.netomi.bat.util.simpleNameMatcher
import com.github.netomi.bat.visitor.AbstractCollector
import com.github.netomi.bat.visitor.AbstractMultiVisitor
import java.util.function.BiConsumer

fun multiMethodVisitorOf(visitor: EncodedMethodVisitor, vararg visitors: EncodedMethodVisitor): EncodedMethodVisitor {
    return MultiMethodVisitor(visitor, *visitors)
}

fun filterMethodsByName(name: String, visitor: EncodedMethodVisitor): EncodedMethodVisitor {
    return MethodNameAndProtoFilter(name, null, visitor)
}

fun filterMethodsByNameAndProtoID(nameExpression: String, protoID: ProtoID, visitor: EncodedMethodVisitor): EncodedMethodVisitor {
    return MethodNameAndProtoFilter(nameExpression, protoID, visitor)
}

fun allCode(visitor: CodeVisitor): EncodedMethodVisitor {
    return EncodedMethodVisitor { dexFile, classDef, method -> method.codeAccept(dexFile, classDef, visitor) }
}

fun methodCollector(): EncodedMethodCollector {
    return EncodedMethodCollector()
}

fun interface EncodedMethodVisitor {

    fun visitAnyMethod(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod)

    fun visitDirectMethod(dexFile: DexFile, classDef: ClassDef, index: Int, method: EncodedMethod) {
        visitAnyMethod(dexFile, classDef, method)
    }

    fun visitVirtualMethod(dexFile: DexFile, classDef: ClassDef, index: Int, method: EncodedMethod) {
        visitAnyMethod(dexFile, classDef, method)
    }

    fun andThen(vararg visitors: EncodedMethodVisitor): EncodedMethodVisitor {
        return multiMethodVisitorOf(this, *visitors)
    }

    fun joinedByMethodConsumer(consumer: BiConsumer<DexFile, EncodedMethod>): EncodedMethodVisitor {
        val joiner: EncodedMethodVisitor = object : EncodedMethodVisitor {
            private var firstVisited = false
            override fun visitAnyMethod(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod) {
                if (firstVisited) {
                    consumer.accept(dexFile, method)
                } else {
                    firstVisited = true
                }
            }
        }
        return multiMethodVisitorOf(joiner, this)
    }
}

class EncodedMethodCollector: AbstractCollector<EncodedMethod>(), EncodedMethodVisitor {
    override fun visitAnyMethod(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod) {
        addItem(method)
    }
}

private class MethodNameAndProtoFilter(            nameExpression: String?,
                                       private val protoID:        ProtoID?,
                                       private val visitor:        EncodedMethodVisitor) : EncodedMethodVisitor {

    private val nameMatcher: StringMatcher?

    init {
        nameMatcher = if (nameExpression != null) {
            simpleNameMatcher(nameExpression)
        } else {
            null
        }
    }

    override fun visitAnyMethod(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod) {
        if (accepted(method.getName(dexFile), method.getProtoID(dexFile))) {
            visitor.visitAnyMethod(dexFile, classDef, method)
        }
    }

    override fun visitDirectMethod(dexFile: DexFile, classDef: ClassDef, index: Int, method: EncodedMethod) {
        if (accepted(method.getName(dexFile), method.getProtoID(dexFile))) {
            visitor.visitDirectMethod(dexFile, classDef, index, method)
        }
    }

    override fun visitVirtualMethod(dexFile: DexFile, classDef: ClassDef, index: Int, method: EncodedMethod) {
        if (accepted(method.getName(dexFile), method.getProtoID(dexFile))) {
            visitor.visitVirtualMethod(dexFile, classDef, index, method)
        }
    }

    // Private utility methods.
    private fun accepted(name: String, protoID: ProtoID): Boolean {
        return (this.nameMatcher == null || this.nameMatcher.matches(name)) &&
               (this.protoID     == null || this.protoID == protoID)
    }
}

private class MultiMethodVisitor constructor(       visitor:       EncodedMethodVisitor,
                                             vararg otherVisitors: EncodedMethodVisitor)
    : AbstractMultiVisitor<EncodedMethodVisitor>(visitor, *otherVisitors), EncodedMethodVisitor {

    override fun visitAnyMethod(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod) {
        for (visitor in visitors) {
            visitor.visitAnyMethod(dexFile, classDef, method)
        }
    }

    override fun visitDirectMethod(dexFile: DexFile, classDef: ClassDef, index: Int, method: EncodedMethod) {
        for (visitor in visitors) {
            visitor.visitDirectMethod(dexFile, classDef, index, method)
        }
    }

    override fun visitVirtualMethod(dexFile: DexFile, classDef: ClassDef, index: Int, method: EncodedMethod) {
        for (visitor in visitors) {
            visitor.visitVirtualMethod(dexFile, classDef, index, method)
        }
    }
}

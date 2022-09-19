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

package com.github.netomi.bat.jasm.assemble

import com.github.netomi.bat.classfile.attribute.annotation.Annotation
import com.github.netomi.bat.classfile.attribute.annotation.AnnotationComponent
import com.github.netomi.bat.classfile.attribute.annotation.ArrayElementValue
import com.github.netomi.bat.classfile.attribute.annotation.ElementValue
import com.github.netomi.bat.classfile.constant.editor.ConstantPoolEditor
import com.github.netomi.bat.jasm.parser.JasmParser.*
import org.antlr.v4.runtime.ParserRuleContext

internal class AnnotationAssembler
    constructor(private val constantPoolEditor:    ConstantPoolEditor,
                private val elementValueAssembler: ElementValueAssembler) {

    fun parseAnnotation(ctx: SAnnotationContext): Annotation {
        val type      = ctx.type.text
        val typeIndex = constantPoolEditor.addOrGetUtf8ConstantIndex(type)

        val annotationComponents =
            parseAnnotationComponents(ctx.sAnnotationKeyName(), ctx.sAnnotationValue())

        return Annotation.of(typeIndex, annotationComponents)
    }

    private fun parseAnnotationComponents(keyContexts: List<SAnnotationKeyNameContext>,
                                          valueContexts: List<SAnnotationValueContext>): MutableList<AnnotationComponent> {

        val annotationElements = mutableListOf<AnnotationComponent>()

        keyContexts.forEachIndexed { index, sAnnotationKeyNameContext ->
            val sAnnotationValueContext = valueContexts[index]

            val annotationValue     = parseAnnotationValueContext(sAnnotationValueContext)
            val annotationNameIndex = constantPoolEditor.addOrGetUtf8ConstantIndex(sAnnotationKeyNameContext.name.text)
            val element             = AnnotationComponent.of(annotationNameIndex, annotationValue)
            annotationElements.add(element)
        }

        return annotationElements
    }

    private fun parseAnnotationValueContext(ctx: SAnnotationValueContext): ElementValue {
        val t = ctx.getChild(0) as ParserRuleContext
        return when (t.ruleIndex) {
            RULE_sArrayValue -> {
                val values = mutableListOf<ElementValue>()

                val arrayValueContext = t as SArrayValueContext
                arrayValueContext.sAnnotationValue().forEach {
                    values.add(parseAnnotationValueContext(it))
                }

                ArrayElementValue.of(values)
            }

            RULE_sSubannotation -> {
//                val subAnnotationContext = t as SmaliParser.SSubannotationContext
//
//                val annotationType = subAnnotationContext.OBJECT_TYPE().text
//                val annotationTypeIndex = dexEditor.addOrGetTypeIDIndex(annotationType)
//
//                val annotationElements =
//                    parseAnnotationAnnotationElements(subAnnotationContext.sAnnotationKeyName(),
//                                                      subAnnotationContext.sAnnotationValue())
//
//                return EncodedAnnotationValue.of(annotationTypeIndex, annotationElements)
                TODO("implement")
            }

            RULE_sBaseValue -> return elementValueAssembler.parseBaseValue(t as SBaseValueContext)
            else            -> null
        } ?: parserError(ctx, "failed to parse annotation value")
    }
}
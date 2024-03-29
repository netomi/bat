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

package com.github.netomi.bat.smali.assemble

import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.annotation.AnnotationVisibility
import com.github.netomi.bat.dexfile.editor.DexEditor
import com.github.netomi.bat.dexfile.value.AnnotationElement
import com.github.netomi.bat.dexfile.value.EncodedAnnotationValue
import com.github.netomi.bat.dexfile.value.EncodedArrayValue
import com.github.netomi.bat.dexfile.value.EncodedValue
import com.github.netomi.bat.smali.parser.SmaliParser
import org.antlr.v4.runtime.ParserRuleContext

internal class AnnotationAssembler constructor(
    private val encodedValueAssembler: EncodedValueAssembler,
    private val dexEditor:             DexEditor) {

    fun parseAnnotation(ctx: SmaliParser.SAnnotationContext): Annotation {
        val annotationTypeIndex = dexEditor.addOrGetTypeIDIndex(ctx.type.text)
        val annotationVisibility = AnnotationVisibility.of(ctx.visibility.text)

        val annotationElements =
            parseAnnotationAnnotationElements(ctx.sAnnotationKeyName(), ctx.sAnnotationValue())

        val encodedAnnotationValue = EncodedAnnotationValue.of(annotationTypeIndex, annotationElements)
        return Annotation.of(annotationVisibility, encodedAnnotationValue)
    }

    private fun parseAnnotationAnnotationElements(keyContexts:   List<SmaliParser.SAnnotationKeyNameContext>,
                                                  valueContexts: List<SmaliParser.SAnnotationValueContext>): MutableList<AnnotationElement> {

        val annotationElements = mutableListOf<AnnotationElement>()

        keyContexts.forEachIndexed { index, sAnnotationKeyNameContext ->
            val sAnnotationValueContext = valueContexts[index]

            val annotationValue     = parseAnnotationValueContext(sAnnotationValueContext)
            val annotationNameIndex = dexEditor.addOrGetStringIDIndex(sAnnotationKeyNameContext.name.text)
            val element             = AnnotationElement.of(annotationNameIndex, annotationValue)
            annotationElements.add(element)
        }

        return annotationElements
    }

    private fun parseAnnotationValueContext(ctx: SmaliParser.SAnnotationValueContext): EncodedValue {
        val t = ctx.getChild(0) as ParserRuleContext
        when (t.ruleIndex) {
            SmaliParser.RULE_sArrayValue -> {
                val values = mutableListOf<EncodedValue>()

                val arrayValueContext = t as SmaliParser.SArrayValueContext
                arrayValueContext.sAnnotationValue().forEach {
                    values.add(parseAnnotationValueContext(it))
                }

                return EncodedArrayValue.of(values)
            }

            SmaliParser.RULE_sSubannotation -> {
                val subAnnotationContext = t as SmaliParser.SSubannotationContext

                val annotationType = subAnnotationContext.OBJECT_TYPE().text
                val annotationTypeIndex = dexEditor.addOrGetTypeIDIndex(annotationType)

                val annotationElements =
                    parseAnnotationAnnotationElements(subAnnotationContext.sAnnotationKeyName(),
                                                      subAnnotationContext.sAnnotationValue())

                return EncodedAnnotationValue.of(annotationTypeIndex, annotationElements)
            }

            SmaliParser.RULE_sBaseValue -> {
                val baseValueContext = t as SmaliParser.SBaseValueContext
                return encodedValueAssembler.parseBaseValue(baseValueContext)
            }
        }

        parserError(ctx, "failed to parse annotation value")
    }
}
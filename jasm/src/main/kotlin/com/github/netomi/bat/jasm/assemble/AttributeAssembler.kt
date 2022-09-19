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

import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.SignatureAttribute
import com.github.netomi.bat.classfile.attribute.SourceFileAttribute
import com.github.netomi.bat.classfile.attribute.annotation.RuntimeAnnotationsAttribute
import com.github.netomi.bat.classfile.editor.AttributeEditor
import com.github.netomi.bat.jasm.disassemble.AnnotationVisibility
import com.github.netomi.bat.jasm.parser.JasmParser.*
import org.antlr.v4.runtime.ParserRuleContext

internal class AttributeAssembler constructor(private val attributeEditor: AttributeEditor) {

    private val constantPoolEditor    = attributeEditor.constantPoolEditor
    private val elementValueAssembler = ElementValueAssembler(constantPoolEditor)
    private val annotationAssembler   = AnnotationAssembler(constantPoolEditor, elementValueAssembler)

    fun parseAndAddAttribute(ctx: SAttributeContext) {
        val t = ctx.getChild(0) as ParserRuleContext
        when (t.ruleIndex) {
            RULE_sSignature  -> return parseAndAddSignatureAttribute(t as SSignatureContext)
            RULE_sSource     -> return parseAndAddSourceFileAttribute(t as SSourceContext)
            RULE_sAnnotation -> return parseAndAddAnnotationAttribute(t as SAnnotationContext)

            else -> {} // TODO: parserError(ctx, "failed to parse attribute")
        }
    }

    private fun parseAndAddSignatureAttribute(ctx: SSignatureContext) {
        val signature = ctx.sig.text.removeSurrounding("\"")
        val attribute = attributeEditor.addOrGetAttribute<SignatureAttribute>(AttributeType.SIGNATURE)
        attribute.setSignature(constantPoolEditor, signature)
    }

    private fun parseAndAddSourceFileAttribute(ctx: SSourceContext) {
        val sourceFile = ctx.src.text.removeSurrounding("\"")
        val attribute = attributeEditor.addOrGetAttribute<SourceFileAttribute>(AttributeType.SOURCE_FILE)
        attribute.setSourceFile(constantPoolEditor, sourceFile)
    }

    private fun parseAndAddAnnotationAttribute(ctx: SAnnotationContext) {
        val attributeType =
            when (AnnotationVisibility.of(ctx.visibility.text)) {
                AnnotationVisibility.BUILD   -> AttributeType.RUNTIME_INVISIBLE_ANNOTATIONS
                AnnotationVisibility.RUNTIME -> AttributeType.RUNTIME_VISIBLE_ANNOTATIONS
            }

        val attribute: RuntimeAnnotationsAttribute = attributeEditor.addOrGetAttribute(attributeType)
        val annotation = annotationAssembler.parseAnnotation(ctx)
        attribute.addAnnotation(annotation)
    }
}
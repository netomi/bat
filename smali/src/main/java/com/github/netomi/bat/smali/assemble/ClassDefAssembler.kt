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
package com.github.netomi.bat.smali.assemble

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.DexConstants.NO_INDEX
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.annotation.AnnotationVisibility
import com.github.netomi.bat.dexfile.value.EncodedAnnotationValue
import com.github.netomi.bat.dexfile.value.EncodedValue
import com.github.netomi.bat.smali.parser.SmaliBaseVisitor
import com.github.netomi.bat.smali.parser.SmaliLexer
import com.github.netomi.bat.smali.parser.SmaliParser
import com.github.netomi.bat.smali.parser.SmaliParser.*
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode


class ClassDefAssembler(private val dexFile: DexFile) : SmaliBaseVisitor<ClassDef?>() {

    private lateinit var classDef: ClassDef

    override fun visitSFile(ctx: SFileContext): ClassDef {
        val classType = ctx.className.text
        val superType = ctx.sSuper().firstOrNull()?.name?.text
        val sourceFile = ctx.sSource().firstOrNull()?.src?.text?.removeSurrounding("\"")

        var accessFlags = collectAccessFlags(ctx.sAccList())
        ctx.sAccList().ACC().forEach {
            val flag = DexAccessFlags.of(it.text)
            accessFlags = accessFlags or flag.value
        }

        val classTypeIndex  = dexFile.addOrGetTypeIDIndex(classType)
        val superTypeIndex  = if (superType != null) dexFile.addOrGetTypeIDIndex(superType) else NO_INDEX
        val sourceFileIndex = if (sourceFile != null) dexFile.addOrGetStringIDIndex(sourceFile) else NO_INDEX;

        classDef = ClassDef.of(classTypeIndex,
                               accessFlags,
                               superTypeIndex,
                               sourceFileIndex);

        ctx.sInterface().forEach {
            classDef.interfaces.addType(dexFile.addOrGetTypeIDIndex(it.name.text))
        }

        ctx.sField().forEach { visitSField(it, classType) }
        ctx.sMethod().forEach { visitSMethod(it, classType) }

        ctx.sAnnotation().forEach { visitSAnnotation(it) }

        return classDef
    }

    override fun visitSAnnotation(ctx: SmaliParser.SAnnotationContext): ClassDef? {
        val annotationType       = ctx.OBJECT_TYPE().text
        val annotationVisibility = AnnotationVisibility.of(ctx.ANN_VISIBLE().text)

        val annotationTypeIndex = dexFile.addOrGetTypeIDIndex(annotationType)

        val encodedValue = EncodedAnnotationValue.of(annotationTypeIndex)

        ctx.sAnnotationKeyName().forEachIndexed { index, sAnnotationKeyNameContext ->
            val sAnnotationValueContext = ctx.sAnnotationValue(index)

            println(sAnnotationKeyNameContext.text)
            println(sAnnotationValueContext.text)

            parseAnnotationValueContext(sAnnotationValueContext)
        }

        Annotation.of(annotationVisibility, encodedValue)
        
        return null
    }

    fun visitSField(ctx: SmaliParser.SFieldContext, classType: String) {
        val fieldElements = ctx.fieldObj.text.split(":")
        assert(fieldElements.size == 2)

        val name = fieldElements[0]
        val type = fieldElements[1]
        val accessFlags = collectAccessFlags(ctx.sAccList())

        val fieldIDIndex = dexFile.addOrGetFieldID(classType, name, type);
        val field = EncodedField.of(fieldIDIndex, accessFlags);

        classDef.addField(dexFile, field)

    }

    fun visitSMethod(ctx: SmaliParser.SMethodContext, classType: String) {
        val (name, parameterTypes, returnType) = parseMethodObj(ctx.methodObj.text)

        val accessFlags = collectAccessFlags(ctx.sAccList())

        val methodIDIndex = dexFile.addOrGetMethodID(classType, name, "", returnType, *parameterTypes.toTypedArray())
        val method = EncodedMethod.of(methodIDIndex, accessFlags);

        classDef.addMethod(dexFile, method)
    }

    companion object {
        fun collectAccessFlags(sAccListContext: SAccListContext): Int {
            var accessFlags = 0
            sAccListContext.ACC().forEach {
                val flag = DexAccessFlags.of(it.text)
                accessFlags = accessFlags or flag.value
            }
            return accessFlags
        }

        fun parseMethodObj(methodObj: String): Triple<String, List<String>, String> {
            val x = methodObj.indexOf('(')
            val y = methodObj.indexOf(')')

            val name = methodObj.substring(0, x)
            val parameterTypes = methodObj.substring(x + 1, y).split(",")
            val returnType = methodObj.substring(y + 1)
            return Triple(name, parameterTypes, returnType)
        }

        fun parseAnnotationValueContext(ctx: SAnnotationValueContext) {
            val t: ParserRuleContext = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                RULE_sArrayValue -> {
                    val arrayValueContext: SArrayValueContext = t as SArrayValueContext
                    for (annotationValueContext: SAnnotationValueContext in arrayValueContext.sAnnotationValue()) {
                        parseAnnotationValueContext(annotationValueContext)
                    }
                }

                RULE_sBaseValue -> {
                    val baseValueContext = t as SBaseValueContext
                    val value: Any? = parseBaseValue(baseValueContext)

                    println(value)
                }

            }
        }

        private fun parseBaseValue(ctx: SBaseValueContext): Any? {
            val value: Token = if (ctx.childCount == 1) {
                val tn: TerminalNode = ctx.getChild(0) as TerminalNode
                tn.symbol
            } else {
                val tn: TerminalNode = ctx.getChild(1) as TerminalNode
                tn.symbol
            }

            return when (value.type) {
                SmaliLexer.STRING -> value.text
                SmaliLexer.BOOLEAN -> "true" == value.text
                SmaliLexer.BYTE -> value.text
                SmaliLexer.SHORT -> value.text
                SmaliLexer.CHAR -> value.text
                SmaliLexer.INT -> value.text
                SmaliLexer.LONG -> value.text
                SmaliLexer.BASE_FLOAT, SmaliLexer.FLOAT_INFINITY, SmaliLexer.FLOAT_NAN -> value.text
                SmaliLexer.BASE_DOUBLE, SmaliLexer.DOUBLE_INFINITY, SmaliLexer.DOUBLE_NAN -> value.text
                SmaliLexer.METHOD_FULL -> value.text
                SmaliLexer.OBJECT_TYPE -> value.text
                SmaliLexer.NULL -> null
                SmaliLexer.FIELD_FULL -> value.text
                else -> null
            }
        }
    }
}
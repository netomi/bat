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
import com.github.netomi.bat.dexfile.annotation.*
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.instruction.DexInstruction
import com.github.netomi.bat.dexfile.instruction.DexInstructions
import com.github.netomi.bat.dexfile.io.InstructionWriter
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.smali.parser.SmaliBaseVisitor
import com.github.netomi.bat.smali.parser.SmaliParser.*
import org.antlr.v4.runtime.ParserRuleContext


internal class ClassDefAssembler(private val dexFile: DexFile) : SmaliBaseVisitor<ClassDef?>() {

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

        val annotationDirectory = classDef.annotationsDirectory

        ctx.sField().forEach { visitSField(it, classType) }
        ctx.sMethod().forEach { visitSMethod(it, classType) }

        ctx.sAnnotation().forEach { visitSAnnotation(it, annotationDirectory.classAnnotations) }

        return classDef
    }

    private fun visitSAnnotation(ctx: SAnnotationContext, annotationSet: AnnotationSet) {
        val annotationElements = mutableListOf<AnnotationElement>()

        ctx.sAnnotationKeyName().forEachIndexed { index, sAnnotationKeyNameContext ->
            val sAnnotationValueContext = ctx.sAnnotationValue(index)

            val annotationValue = parseAnnotationValueContext(sAnnotationValueContext)
            if (annotationValue != null) {
                val element =
                    AnnotationElement.of(dexFile.addOrGetStringIDIndex(sAnnotationKeyNameContext.text),
                                         annotationValue)
                annotationElements.add(element)
            }
        }

        val annotationType       = ctx.OBJECT_TYPE().text
        val annotationTypeIndex  = dexFile.addOrGetTypeIDIndex(annotationType)
        val annotationVisibility = AnnotationVisibility.of(ctx.ANN_VISIBLE().text)

        val encodedAnnotationValue = EncodedAnnotationValue.of(annotationTypeIndex, *annotationElements.toTypedArray())
        val annotation = Annotation.of(annotationVisibility, encodedAnnotationValue)

        annotationSet.addAnnotation(annotation)
    }

    fun visitSField(ctx: SFieldContext, classType: String) {
        val fieldElements = ctx.fieldObj.text.split(":")
        assert(fieldElements.size == 2)

        val name = fieldElements[0]
        val type = fieldElements[1]
        val accessFlags = collectAccessFlags(ctx.sAccList())

        val fieldIDIndex = dexFile.addOrGetFieldID(classType, name, type);
        val field = EncodedField.of(fieldIDIndex, accessFlags);

        classDef.addField(dexFile, field)

        if (field.isStatic && ctx.sBaseValue() != null) {
            val staticValue = EncodedValueParser.parseBaseValue(ctx.sBaseValue(), dexFile)

            classDef.setStaticValue(dexFile, field, staticValue)
        }

        val annotationSet = AnnotationSet.empty()
        ctx.sAnnotation().forEach { visitSAnnotation(it, annotationSet) }

        if (annotationSet.annotationCount > 0) {
            val fieldAnnotation = FieldAnnotation.of(field.fieldIndex, annotationSet)
            classDef.annotationsDirectory.fieldAnnotations.add(fieldAnnotation)
        }
    }

    fun visitSMethod(ctx: SMethodContext, classType: String) {
        val (name, parameterTypes, returnType) = parseMethodObj(ctx.methodObj.text)

        val accessFlags = collectAccessFlags(ctx.sAccList())

        val methodIDIndex =
            dexFile.addOrGetMethodID(classType,
                                     name,
                                     DexClasses.toShortyFormat(parameterTypes, returnType),
                                     returnType,
                                     *parameterTypes.toTypedArray())

        val method = EncodedMethod.of(methodIDIndex, accessFlags);

        ctx.sParameter().forEach { println(it) }

        visitSInstructions(ctx.sInstruction(), method)

        classDef.addMethod(dexFile, method)

        val annotationSet = AnnotationSet.empty()
        ctx.sAnnotation().forEach { visitSAnnotation(it, annotationSet) }

        if (annotationSet.annotationCount > 0) {
            val methodAnnotation = MethodAnnotation.of(method.methodIndex, annotationSet)
            classDef.annotationsDirectory.methodAnnotations.add(methodAnnotation)
        }
    }

    private fun visitSInstructions(lCtx: List<SInstructionContext>, method: EncodedMethod) {

        val instructions = mutableListOf<DexInstruction>()
        var registers = 0

        lCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                RULE_fregisters -> {
                    val c = t as FregistersContext
                    registers = c.xregisters.text.toInt()
                }

                RULE_fm5c -> {
                    val c = t as Fm5cContext
                    val opName = c.op.text

                    val insn = when (opName) {
                        "invoke-direct" -> {
                            val methodType = c.method.text

                            val (classType, methodName, descriptor) = parseMethodType(methodType)
                            val (_, parameterTypes, returnType) = parseMethodObj(descriptor)

                            val methodID = dexFile.addOrGetMethodID(classType, methodName, DexClasses.toShortyFormat(parameterTypes, returnType), returnType, *parameterTypes.toTypedArray())

                            val regs = intArrayOf(c.REGISTER(0).text.substring(1).toInt())
                            DexInstructions.invokeDirect(methodID, *regs)
                        }

                        "return-void" -> {
                            DexInstructions.returnVoid()
                        }
                        else -> DexInstructions.nop()
                    }

                    instructions.add(insn)
                }
            }
        }
        println(instructions)

        val code = Code.of(registers, 1, 0)

        val insns = writeInstructions(instructions)
        code.insns = insns
        code.insnsSize = insns.size

        method.code = code
    }

    private fun writeInstructions(instructions: List<DexInstruction>): ShortArray {
        val codeLen = instructions.stream().map { a: DexInstruction -> a.length }.reduce(0) { a: Int, b: Int -> a + b }

        val writer = InstructionWriter(codeLen)
        var offset = 0
        for (instruction in instructions) {
            instruction.write(writer, offset)
            offset += instruction.length
        }

        return writer.array
    }

    private fun parseAnnotationValueContext(ctx: SAnnotationValueContext): EncodedValue? {
        val t: ParserRuleContext = ctx.getChild(0) as ParserRuleContext
        when (t.ruleIndex) {
            RULE_sArrayValue -> {
                val values = mutableListOf<EncodedValue>()

                val arrayValueContext: SArrayValueContext = t as SArrayValueContext
                for (annotationValueContext: SAnnotationValueContext in arrayValueContext.sAnnotationValue()) {
                    val value = parseAnnotationValueContext(annotationValueContext)
                    value?.apply { values.add(this) }
                }

                return EncodedArrayValue.of(*values.toTypedArray())
            }

            RULE_sBaseValue -> {
                val baseValueContext = t as SBaseValueContext
                return EncodedValueParser.parseBaseValue(baseValueContext, dexFile)
            }
        }

        return null
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

        fun parseMethodType(methodType: String): Triple<String, String, String> {
            val x = methodType.indexOf("->")
            val y = methodType.indexOf("(")

            val classType = methodType.substring(0, x)
            val methodName = methodType.substring(x + 2, y)
            val descriptor = methodType.substring(y)

            return Triple(classType, methodName, descriptor)
        }
    }
}
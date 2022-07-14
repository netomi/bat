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
import com.github.netomi.bat.dexfile.annotation.*
import com.github.netomi.bat.dexfile.editor.DexComposer
import com.github.netomi.bat.dexfile.instruction.DexInstruction
import com.github.netomi.bat.dexfile.instruction.DexInstructions
import com.github.netomi.bat.dexfile.io.InstructionWriter
import com.github.netomi.bat.smali.parser.SmaliBaseVisitor
import com.github.netomi.bat.smali.parser.SmaliParser.*
import org.antlr.v4.runtime.ParserRuleContext

internal class ClassDefAssembler(private val dexFile: DexFile) : SmaliBaseVisitor<ClassDef?>() {

    private val dexComposer: DexComposer = dexFile.composer

    private lateinit var classDef: ClassDef

    override fun visitSFile(ctx: SFileContext): ClassDef {
        val classType   = ctx.className.text
        val superType   = ctx.sSuper().firstOrNull()?.name?.text
        val sourceFile  = ctx.sSource().firstOrNull()?.src?.text?.removeSurrounding("\"")
        val accessFlags = parseAccessFlags(ctx.sAccList())

        val classTypeIndex  = dexComposer.addOrGetTypeIDIndex(classType)
        val superTypeIndex  = if (superType != null) dexComposer.addOrGetTypeIDIndex(superType) else NO_INDEX
        val sourceFileIndex = if (sourceFile != null) dexComposer.addOrGetStringIDIndex(sourceFile) else NO_INDEX

        classDef =
            ClassDef.of(classTypeIndex,
                        accessFlags,
                        superTypeIndex,
                        sourceFileIndex)

        ctx.sInterface().forEach {
            classDef.interfaces.addType(dexComposer.addOrGetTypeIDIndex(it.name.text))
        }

        val annotationDirectory = classDef.annotationsDirectory
        ctx.sAnnotation().forEach { AnnotationParser.visitSAnnotation(it, annotationDirectory.classAnnotations, dexComposer) }

        ctx.sField().forEach  { visitSField(it, classType) }
        ctx.sMethod().forEach { visitSMethod(it, classType) }

        return classDef
    }

    private fun visitSField(ctx: SFieldContext, classType: String) {
        val (_, name, type) = parseFieldObject(ctx.fieldObj.text)
        val accessFlags  = parseAccessFlags(ctx.sAccList())

        val fieldIDIndex = dexComposer.addOrGetFieldIDIndex(classType, name, type)
        val field = EncodedField.of(fieldIDIndex, accessFlags)

        classDef.addField(dexFile, field)

        if (field.isStatic && ctx.sBaseValue() != null) {
            val staticValue = EncodedValueParser.parseBaseValue(ctx.sBaseValue(), dexComposer)
            classDef.setStaticValue(dexFile, field, staticValue)
        }

        val annotationSet = AnnotationSet.empty()
        ctx.sAnnotation().forEach { AnnotationParser.visitSAnnotation(it, annotationSet, dexComposer) }
        if (!annotationSet.isEmpty) {
            val fieldAnnotation = FieldAnnotation.of(field.fieldIndex, annotationSet)
            classDef.annotationsDirectory.fieldAnnotations.add(fieldAnnotation)
        }
    }

    private fun visitSMethod(ctx: SMethodContext, classType: String) {
        val (_, name, parameterTypes, returnType) = parseMethodObject(ctx.methodObj.text)
        val accessFlags = parseAccessFlags(ctx.sAccList())

        val methodIDIndex =
            dexComposer.addOrGetMethodIDIndex(classType,
                                              name,
                                              parameterTypes,
                                              returnType)

        val method = EncodedMethod.of(methodIDIndex, accessFlags)

        visitSInstructions(ctx.sInstruction(), method)

        classDef.addMethod(dexFile, method)

        val annotationSet = AnnotationSet.empty()
        ctx.sAnnotation().forEach { AnnotationParser.visitSAnnotation(it, annotationSet, dexComposer) }
        if (!annotationSet.isEmpty) {
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

                RULE_f0x -> {
                    val c = t as F0xContext
                    val insn = when (val opName = c.op.text) {
                        "return-void" -> DexInstructions.returnVoid()
                        "nop"         -> DexInstructions.nop()
                        else          -> parserError(ctx, "unexpected opname $opName")
                    }

                    instructions.add(insn)
                }

                RULE_fm5c -> {
                    val c = t as Fm5cContext
                    val opName = c.op.text

                    val insn = when (opName) {
                        "invoke-direct" -> {
                            val methodType = c.method.text

                            val (classType, methodName, parameterTypes, returnType) = parseMethodObject(methodType)
                            val methodID =
                                dexComposer.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)

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
}
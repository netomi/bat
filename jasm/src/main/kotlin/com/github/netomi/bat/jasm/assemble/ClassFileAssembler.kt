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

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Version
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.ConstantValueAttribute
import com.github.netomi.bat.classfile.editor.ClassEditor
import com.github.netomi.bat.jasm.parser.JasmBaseVisitor
import com.github.netomi.bat.jasm.parser.JasmParser.*
import java.io.PrintWriter

internal class ClassFileAssembler(private val lenientMode:    Boolean      = false,
                                  private val warningPrinter: PrintWriter? = null): JasmBaseVisitor<List<ClassFile>>() {

    private lateinit var classEditor: ClassEditor

    override fun aggregateResult(aggregate: List<ClassFile>?, nextResult: List<ClassFile>?): List<ClassFile> {
        return (aggregate ?: emptyList()) + (nextResult ?: emptyList())
    }

    override fun visitCFile(ctx: CFileContext): List<ClassFile> {
        val className      = ctx.className.text
        val versionString  = ctx.sBytecode().firstOrNull()?.version?.text?.removeSurrounding("\"")
        val superClassName = ctx.sSuper().firstOrNull()?.name?.text
        val accessFlags    = parseAccessFlags(ctx.sAccList())
        val version        = if (versionString != null) Version.of(versionString) else Version.JAVA_8

        val classFile = ClassFile.of(className, accessFlags, superClassName, version)
        classEditor   = ClassEditor.of(classFile)

        ctx.sInterface().forEach {
            classEditor.addInterface(it.name.text)
        }

        ctx.sField().forEach  { visitSField(it) }
        ctx.sMethod().forEach { visitSMethod(it) }

        val attributeAssembler = AttributeAssembler(classEditor)
        ctx.sAttribute().forEach { attributeAssembler.parseAndAddAttribute(it) }

        return listOf(classFile)
    }

    override fun visitSField(ctx: SFieldContext): List<ClassFile> {
        val (_, name, type) = parseFieldObject(ctx.fieldObj.text)
        val accessFlags     = parseAccessFlags(ctx.sAccList())

        val fieldEditor = classEditor.addField(name, accessFlags, type)
        val attributeAssembler = AttributeAssembler(fieldEditor)

        val field = fieldEditor.field
        if (field.isStatic && ctx.sBaseValue() != null) {
            val constantAssembler  = ConstantAssembler(classEditor.constantPoolEditor)
            val constantValueIndex = constantAssembler.parseBaseValue(ctx.sBaseValue())

            val constantValueAttribute = fieldEditor.addOrGetAttribute<ConstantValueAttribute>(AttributeType.CONSTANT_VALUE)
            constantValueAttribute.constantValueIndex = constantValueIndex
        }

        ctx.sAttribute().forEach { attributeAssembler.parseAndAddAttribute(it) }

        return emptyList()
    }

    override fun visitSMethod(ctx: SMethodContext): List<ClassFile> {
        val (_, name, parameterTypes, returnType) = parseMethodObject(ctx.methodObj.text)
        val accessFlags = parseAccessFlags(ctx.sAccList())

        val methodEditor = classEditor.addMethod(name, accessFlags, parameterTypes, returnType)
        val attributeAssembler = AttributeAssembler(methodEditor)

        ctx.sAttribute().forEach { attributeAssembler.parseAndAddAttribute(it) }

        return emptyList()
    }
}
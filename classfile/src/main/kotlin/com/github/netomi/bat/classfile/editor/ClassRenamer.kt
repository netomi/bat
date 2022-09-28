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

package com.github.netomi.bat.classfile.editor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.Member
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.annotation.RuntimeAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.preverification.*
import com.github.netomi.bat.classfile.attribute.preverification.visitor.StackMapFrameVisitor
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.classfile.constant.*
import com.github.netomi.bat.classfile.constant.editor.ConstantPoolShrinker
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.instruction.ConstantInstruction
import com.github.netomi.bat.classfile.instruction.JvmInstruction
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.util.asInternalClassName
import com.github.netomi.bat.util.asJvmType

class ClassRenamer constructor(private val renamer: Renamer): ClassFileVisitor {

    private val constantPoolShrinker = ConstantPoolShrinker()

    override fun visitClassFile(classFile: ClassFile) {
        val collector = ConstantCollector()
        collector.visitClassFile(classFile)

        for ((elementType, constant) in collector.collectedConstants.values) {
            when (elementType) {
                ElementType.CLASSNAME -> {
                    val className = constant.value.asInternalClassName()
                    constant.value = renamer.renameClassName(className).toInternalClassName()
                }

                ElementType.FIELD_TYPE -> {
                    constant.value = renamer.renameFieldType(constant.value.asJvmType()).toString()
                }

                ElementType.METHOD_DESCRIPTOR -> {
                    constant.value = renamer.renameMethodDescriptor(constant.value)
                }

                else -> {}
            }
        }

        constantPoolShrinker.visitClassFile(classFile)
    }

    private inner class ConstantCollector
        : ClassFileVisitor, MemberVisitor, AttributeVisitor, InstructionVisitor, StackMapFrameVisitor, ConstantVisitor {

        val collectedConstants: MutableMap<Int, Pair<ElementType, Utf8Constant>> = mutableMapOf()
        var currentType: ElementType = ElementType.UNKNOWN

        // ClassVisitor.

        override fun visitClassFile(classFile: ClassFile) {
            classFile.thisClassConstantAccept(this)
            classFile.superClassConstantAccept(this)
            classFile.interfaceConstantsAccept(this)

            classFile.membersAccept(this)
            classFile.attributesAccept(this)
        }

        // MemberVisitor.

        override fun visitAnyMember(classFile: ClassFile, index: Int, member: Member) {}

        override fun visitField(classFile: ClassFile, index: Int, field: Field) {
            currentType = ElementType.FIELD_TYPE
            field.descriptorConstantAccept(classFile, this)

            field.attributesAccept(classFile, this)
        }

        override fun visitMethod(classFile: ClassFile, index: Int, method: Method) {
            currentType = ElementType.METHOD_DESCRIPTOR
            method.descriptorConstantAccept(classFile, this)

            method.attributesAccept(classFile, this)
        }

        // AttributeVisitor.

        override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {}

        override fun visitAnyRuntimeAnnotations(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
            for (annotation in attribute) {
                currentType = ElementType.FIELD_TYPE
                classFile.constantAccept(annotation.typeIndex, this)
            }
        }

        override fun visitBootstrapMethods(classFile: ClassFile, attribute: BootstrapMethodsAttribute) {
            for (bootstrapMethod in attribute) {
                bootstrapMethod.bootstrapMethodRefAccept(classFile, this)
                bootstrapMethod.bootstrapArgumentsAccept(classFile, this)
            }
        }

        override fun visitEnclosingMethod(classFile: ClassFile, attribute: EnclosingMethodAttribute) {
            attribute.classConstantAccept(classFile, this)
            attribute.methodConstantAccept(classFile, this)
        }

        override fun visitInnerClasses(classFile: ClassFile, attribute: InnerClassesAttribute) {
            for (innerClassEntry in attribute) {
                innerClassEntry.innerClassConstantAccept(classFile, this)
                innerClassEntry.outerClassConstantAccept(classFile, this)
            }
        }

        override fun visitStackMapTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: StackMapTableAttribute) {
            attribute.stackMapFramesAccept(classFile, this)
        }

        override fun visitCode(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
            attribute.instructionsAccept(classFile, method, this)

            attribute.attributesAccept(classFile, method, this)
        }

        // StackMapFrameVisitor.

        override fun visitAnyFrame(classFile: ClassFile, frame: StackMapFrame) {
            for (verificationType in frame.verificationTypes) {
                if (verificationType is ObjectVariable) {
                    verificationType.classConstantAccept(classFile, this)
                }
            }
        }

        // InstructionVisitor.

        override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {}

        override fun visitAnyConstantInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConstantInstruction) {
            instruction.constantAccept(classFile, this)
        }

        // ConstantVisitor.

        override fun visitAnyConstant(classFile: ClassFile, index: Int, constant: Constant) {}

        override fun visitUtf8Constant(classFile: ClassFile, index: Int, constant: Utf8Constant) {
            if (currentType != ElementType.UNKNOWN) {
                collectedConstants[index] = Pair(currentType, constant)
                currentType = ElementType.UNKNOWN
            }
        }

        override fun visitNameAndTypeConstant(classFile: ClassFile, index: Int, constant: NameAndTypeConstant) {
            classFile.constantAccept(constant.descriptorIndex, this)
        }

        override fun visitClassConstant(classFile: ClassFile, index: Int, constant: ClassConstant) {
            currentType = ElementType.CLASSNAME
            classFile.constantAccept(constant.nameIndex, this)
        }

        override fun visitFieldRefConstant(classFile: ClassFile, index: Int, constant: FieldrefConstant) {
            classFile.constantAccept(constant.classIndex, this)
            currentType = ElementType.FIELD_TYPE
            classFile.constantAccept(constant.nameAndTypeIndex, this)
        }

        override fun visitMethodRefConstant(classFile: ClassFile, index: Int, constant: MethodrefConstant) {
            classFile.constantAccept(constant.classIndex, this)
            currentType = ElementType.METHOD_DESCRIPTOR
            classFile.constantAccept(constant.nameAndTypeIndex, this)
        }

        override fun visitInterfaceMethodRefConstant(classFile: ClassFile, index: Int, constant: InterfaceMethodrefConstant) {
            classFile.constantAccept(constant.classIndex, this)
            currentType = ElementType.METHOD_DESCRIPTOR
            classFile.constantAccept(constant.nameAndTypeIndex, this)
        }

        override fun visitMethodHandleConstant(classFile: ClassFile, index: Int, constant: MethodHandleConstant) {
            classFile.constantAccept(constant.referenceIndex, this)
        }

        override fun visitMethodTypeConstant(classFile: ClassFile, index: Int, constant: MethodTypeConstant) {
            currentType = ElementType.METHOD_DESCRIPTOR
            classFile.constantAccept(constant.descriptorIndex, this)
        }
    }

    private enum class ElementType {
        CLASSNAME,
        FIELD_TYPE,
        METHOD_DESCRIPTOR,
        SIGNATURE,
        UNKNOWN
    }
}
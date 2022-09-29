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
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.classfile.attribute.annotation.Annotation
import com.github.netomi.bat.classfile.attribute.annotation.visitor.AnnotationVisitor
import com.github.netomi.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
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

/**
 * This [ClassFileVisitor] will rename all *used* constants that reference a classname
 * using the given [Renamer]. The constant pool is shrunk after processing to ensure
 * that no constant is left dangling which might contain a classname that should have
 * been renamed but was not referenced from anywhere (e.g. import statements for constants
 * seem to be left over by the compiler).
 *
 * A simpler approach would be to just search/replace any constant in the constant pool,
 * but this approach is cleaner as the visitor knows exactly how to deconstruct and rebuild
 * each constant value based on how it is being used.
 *
 * There some oddities about array classes referenced from some instructions
 * (anewarray / multianewarray) and element values / signatures that are handled in a clean
 * way with this approach.
 */
class ClassRenamer constructor(private val renamer: Renamer): ClassFileVisitor {

    private val constantPoolShrinker = ConstantPoolShrinker()

    override fun visitClassFile(classFile: ClassFile) {
        // we fist collect all used constants that might contain a classname
        val collector = ConstantCollector()
        collector.visitClassFile(classFile)

        // now process all constants containing a classname and replace
        // the names using the given Renamer.
        for ((elementType, constant) in collector.collectedConstants.values) {
            when (elementType) {
                ElementType.CLASSNAME -> {
                    constant.value = renamer.renameClassType(constant.value.asInternalClassName()).toInternalClassName()
                }

                ElementType.FIELD_TYPE -> {
                    constant.value = renamer.renameFieldType(constant.value.asJvmType()).toString()
                }

                ElementType.METHOD_DESCRIPTOR -> {
                    constant.value = renamer.renameMethodDescriptor(constant.value)
                }

                ElementType.CLASS_SIGNATURE -> {
                    constant.value = renamer.renameClassSignature(constant.value)
                }

                ElementType.FIELD_SIGNATURE -> {
                    constant.value = renamer.renameFieldSignature(constant.value)
                }

                ElementType.METHOD_SIGNATURE -> {
                    constant.value = renamer.renameMethodSignature(constant.value)
                }

                else -> {}
            }
        }

        constantPoolShrinker.visitClassFile(classFile)
    }

    private inner class ConstantCollector
        : ClassFileVisitor, MemberVisitor, AttributeVisitor, AnnotationVisitor, InstructionVisitor,
          ElementValueVisitor, StackMapFrameVisitor, ConstantVisitor {

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
            attribute.annotationsAccept(classFile, this)
        }

        override fun visitAnyRuntimeTypeAnnotations(classFile: ClassFile, attribute: RuntimeTypeAnnotationsAttribute) {
            attribute.typeAnnotationsAccept(classFile, this)
        }

        override fun visitRuntimeParameterAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeParameterAnnotationsAttribute) {
            for (parameterIndex in 0 until attribute.size) {
                attribute.parameterAnnotationsAccept(classFile, parameterIndex, this)
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
            currentType = ElementType.METHOD_DESCRIPTOR
            attribute.methodConstantAccept(classFile, this)
        }

        override fun visitInnerClasses(classFile: ClassFile, attribute: InnerClassesAttribute) {
            for (innerClassEntry in attribute) {
                innerClassEntry.innerClassConstantAccept(classFile, this)
                innerClassEntry.outerClassConstantAccept(classFile, this)
            }
        }

        override fun visitSignature(classFile: ClassFile, attribute: SignatureAttribute) {
            currentType = ElementType.CLASS_SIGNATURE
            attribute.signatureConstantAccept(classFile, this)
        }

        override fun visitSignature(classFile: ClassFile, field: Field, attribute: SignatureAttribute) {
            currentType = ElementType.FIELD_SIGNATURE
            attribute.signatureConstantAccept(classFile, this)
        }

        override fun visitSignature(classFile: ClassFile, method: Method, attribute: SignatureAttribute) {
            currentType = ElementType.METHOD_SIGNATURE
            attribute.signatureConstantAccept(classFile, this)
        }

        override fun visitStackMapTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: StackMapTableAttribute) {
            attribute.stackMapFramesAccept(classFile, this)
        }

        override fun visitCode(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
            attribute.instructionsAccept(classFile, method, this)

            attribute.attributesAccept(classFile, method, this)
        }

        override fun visitLocalVariableTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LocalVariableTableAttribute) {
            for (localVariableEntry in attribute) {
                currentType = ElementType.FIELD_TYPE
                localVariableEntry.descriptorConstantAccept(classFile, this)
            }
        }

        override fun visitLocalVariableTypeTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LocalVariableTypeTableAttribute) {
            for (localVariableTypeEntry in attribute) {
                currentType = ElementType.FIELD_SIGNATURE
                localVariableTypeEntry.signatureConstantAccept(classFile, this)
            }
        }

        override fun visitAnnotationDefault(classFile: ClassFile, method: Method, attribute: AnnotationDefaultAttribute) {
            attribute.elementValue.accept(classFile, this)
        }

        // AnnotationVisitor.

        override fun visitAnyAnnotation(classFile: ClassFile, annotation: Annotation) {
            currentType = ElementType.FIELD_TYPE
            annotation.typeConstantAccept(classFile, this)
            annotation.elementValuesAccept(classFile, this)
        }

        // ElementValueVisitor.

        override fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue) {}

        override fun visitAnnotationElementValue(classFile: ClassFile, elementValue: AnnotationElementValue) {
            elementValue.annotation.accept(classFile, this)
        }

        override fun visitArrayElementValue(classFile: ClassFile, elementValue: ArrayElementValue) {
            elementValue.elementValuesAccept(classFile, this)
        }

        override fun visitEnumElementValue(classFile: ClassFile, elementValue: EnumElementValue) {
            currentType = ElementType.FIELD_TYPE
            elementValue.typeNameConstantAccept(classFile, this)
        }

        override fun visitClassElementValue(classFile: ClassFile, elementValue: ClassElementValue) {
            currentType = ElementType.FIELD_TYPE
            elementValue.classTypeConstantAccept(classFile, this)
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
            constant.descriptorConstantAccept(classFile, this)
        }

        override fun visitClassConstant(classFile: ClassFile, index: Int, constant: ClassConstant) {
            currentType = ElementType.CLASSNAME
            constant.nameConstantAccept(classFile, this)
        }

        override fun visitFieldRefConstant(classFile: ClassFile, index: Int, constant: FieldrefConstant) {
            classFile.constantAccept(constant.classIndex, this)
            currentType = ElementType.FIELD_TYPE
            constant.nameAndTypeConstantAccept(classFile, this)
        }

        override fun visitMethodRefConstant(classFile: ClassFile, index: Int, constant: MethodrefConstant) {
            classFile.constantAccept(constant.classIndex, this)
            currentType = ElementType.METHOD_DESCRIPTOR
            constant.nameAndTypeConstantAccept(classFile, this)
        }

        override fun visitInterfaceMethodRefConstant(classFile: ClassFile, index: Int, constant: InterfaceMethodrefConstant) {
            classFile.constantAccept(constant.classIndex, this)
            currentType = ElementType.METHOD_DESCRIPTOR
            constant.nameAndTypeConstantAccept(classFile, this)
        }

        override fun visitInvokeDynamicConstant(classFile: ClassFile, index: Int, constant: InvokeDynamicConstant) {
            currentType = ElementType.METHOD_DESCRIPTOR
            constant.nameAndTypeConstantAccept(classFile, this)
        }

        override fun visitDynamicConstant(classFile: ClassFile, index: Int, constant: DynamicConstant) {
            currentType = ElementType.FIELD_TYPE
            constant.nameAndTypeConstantAccept(classFile, this)
        }

        override fun visitMethodHandleConstant(classFile: ClassFile, index: Int, constant: MethodHandleConstant) {
            constant.referenceAccept(classFile, this)
        }

        override fun visitMethodTypeConstant(classFile: ClassFile, index: Int, constant: MethodTypeConstant) {
            currentType = ElementType.METHOD_DESCRIPTOR
            constant.descriptorConstantAccept(classFile, this)
        }
    }

    private enum class ElementType {
        CLASSNAME,
        FIELD_TYPE,
        METHOD_DESCRIPTOR,
        CLASS_SIGNATURE,
        FIELD_SIGNATURE,
        METHOD_SIGNATURE,
        UNKNOWN
    }
}
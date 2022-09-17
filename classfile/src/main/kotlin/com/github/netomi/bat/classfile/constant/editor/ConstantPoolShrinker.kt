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

package com.github.netomi.bat.classfile.constant.editor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.allInstructions
import com.github.netomi.bat.classfile.constant.Constant
import com.github.netomi.bat.classfile.constant.ConstantPool
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.IDAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.marker.UsageMarker
import com.github.netomi.bat.classfile.instruction.ConstantInstruction
import com.github.netomi.bat.classfile.instruction.JvmInstruction
import com.github.netomi.bat.classfile.instruction.editor.InstructionWriter
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.visitor.allCode
import com.github.netomi.bat.classfile.visitor.allMethods

class ConstantPoolShrinker: ClassFileVisitor {

    override fun visitClassFile(classFile: ClassFile) {
        val usageMarker    = UsageMarker()
        val constantMarker = ReferencedConstantsMarker(usageMarker)
        classFile.referencedConstantsAccept(false, constantMarker)
        classFile.accept(allMethods(allCode(allInstructions(InstructionConstantMarker(constantMarker)))))

        val mapping = IntArray(classFile.constantPoolSize) { -1 }
        val shrunkConstantPool = ConstantPool.empty()

        classFile.constantsAccept { _, oldIndex, constant ->
            if (usageMarker.isUsed(constant)) {
                val newIndex      = shrunkConstantPool.addConstant(constant)
                mapping[oldIndex] = newIndex
            }
        }

        if (shrunkConstantPool.size != classFile.constantPoolSize) {
            classFile.constantPool = shrunkConstantPool

            // update the new constant indices in all items of the class file.
            classFile.referencedConstantsAccept(true) { _, owner, accessor ->
                val constantIndex = accessor.get()
                val newIndex = mapping[constantIndex]
                if (newIndex < 0) {
                    error("no mapping found for constant index '$constantIndex' referenced from '$owner")
                } else {
                    accessor.set(newIndex)
                }
            }

            // update all code attributes by remapping any instruction referencing a constant pool entry.
            classFile.accept(allMethods(allCode(InstructionReMapper(mapping))))
        }
    }
}

private class ReferencedConstantsMarker constructor(val usageMarker: UsageMarker): ReferencedConstantVisitor, ConstantVisitor {
    override fun visitAnyConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        val constantIndex = accessor.get()
        val constant = classFile.getConstant(constantIndex)
        usageMarker.markUsed(constant)
        // recursively mark referenced constants
        constant.referencedConstantsAccept(classFile, this)
    }

    override fun visitAnyConstant(classFile: ClassFile, index: Int, constant: Constant) {
        usageMarker.markUsed(constant)
        constant.referencedConstantsAccept(classFile, this)
    }
}

private class InstructionConstantMarker constructor(val constantMarker: ConstantVisitor): InstructionVisitor {
    override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {}

    override fun visitAnyConstantInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConstantInstruction) {
        instruction.constantAccept(classFile, constantMarker)
    }
}

private class InstructionReMapper constructor(private val mapping: IntArray): AttributeVisitor, InstructionVisitor {
    private var instructions: MutableList<JvmInstruction> = mutableListOf()

    override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {}

    override fun visitCode(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
        instructions = mutableListOf()

        attribute.instructionsAccept(classFile, method, this)

        val modifiedInstructions = InstructionWriter.writeInstructions(instructions)
        val newLength = modifiedInstructions.size

        if (newLength == attribute.codeLength) {
            attribute._code = modifiedInstructions
        } else {
            error("remapped instructions have different size")
        }
    }

    override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {
        instructions.add(instruction)
    }

    override fun visitAnyConstantInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConstantInstruction) {
        val oldConstantIndex = instruction.constantIndex
        instruction.constantIndex = mapping[oldConstantIndex]
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }
}
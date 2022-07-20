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

package com.github.netomi.bat.dexfile.editor

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.annotation.AnnotationSet
import com.github.netomi.bat.dexfile.annotation.AnnotationSetRef
import com.github.netomi.bat.dexfile.annotation.AnnotationsDirectory
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.io.InstructionWriter
import com.github.netomi.bat.dexfile.visitor.*
import com.github.netomi.bat.dexfile.visitor.IDAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor

class DexSorter : DexFileVisitor {

    override fun visitDexFile(dexFile: DexFile) {
        sortDataItems(dexFile)
    }

    private fun sortDataItems(dexFile: DexFile) {
        val stringIDMapping = sortIDList(dexFile.stringIDs, compareBy { it })
        dexFile.referencedIDsAccept(object: ReferencedIDVisitor {
            override fun visitStringID(dexFile: DexFile, accessor: IDAccessor) {
                accessor.set(stringIDMapping[accessor.get()]!!)
            }
        })

        val typeIDMapping = sortIDList(dexFile.typeIDs, compareBy { it.descriptorIndex } )
        dexFile.referencedIDsAccept(object: ReferencedIDVisitor {
            override fun visitTypeID(dexFile: DexFile, accessor: IDAccessor) {
                accessor.set(typeIDMapping[accessor.get()]!!)
            }
        })

        val protoIDMapping = sortIDList(dexFile.protoIDs, compareBy({ it.returnTypeIndex }, { it.parameters }) )
        dexFile.referencedIDsAccept(object: ReferencedIDVisitor {
            override fun visitProtoID(dexFile: DexFile, accessor: IDAccessor) {
                accessor.set(protoIDMapping[accessor.get()]!!)
            }
        })

        val fieldIDMapping  = sortIDList(dexFile.fieldIDs,  compareBy({ it.classIndex }, { it.nameIndex }, { it.typeIndex }))
        val methodIDMapping = sortIDList(dexFile.methodIDs, compareBy({ it.classIndex }, { it.nameIndex }, { it.protoIndex }))

        dexFile.referencedIDsAccept(object: ReferencedIDVisitor {
            override fun visitFieldID(dexFile: DexFile, accessor: IDAccessor) {
                accessor.set(fieldIDMapping[accessor.get()]!!)
            }

            override fun visitMethodID(dexFile: DexFile, accessor: IDAccessor) {
                accessor.set(methodIDMapping[accessor.get()]!!)
            }
        })

        dexFile.classDefsAccept { _, _, classDef -> classDef.sort(dexFile) }

        dexFile.dataItemsAccept(object: DataItemVisitor {
            override fun visitAnyDataItem(dexFile: DexFile, dataItem: DataItem) {}

            override fun visitAnyAnnotationSet(dexFile: DexFile, annotationSet: AnnotationSet) {
                annotationSet.sort()
            }

            override fun visitAnnotationsDirectory(dexFile: DexFile, classDef: ClassDef, annotationsDirectory: AnnotationsDirectory) {
                annotationsDirectory.sort()
            }
        })

        // fix instructions that reference modified string/type/proto IDs.
        dexFile.classDefsAccept(allClassData(allMethods(allCode(InstructionFixer(stringIDMapping, typeIDMapping, protoIDMapping, fieldIDMapping, methodIDMapping)))))

        dexFile.refreshCaches()
    }

    private fun <T> sortIDList(list: MutableList<T>, comparator: Comparator<T>): Map<Int, Int> {
        val oldIndices = list.withIndex().associate { iv -> Pair(iv.value, iv.index) }
        list.sortWith(comparator)
        val newIndices = list.withIndex().associate { iv -> Pair(iv.value, iv.index) }
        return oldIndices.keys.associate { stringID -> Pair(oldIndices[stringID]!!, newIndices[stringID]!!) }
    }
}

private class InstructionFixer constructor(val stringIDMapping: Map<Int, Int>,
                                           val typeIDMapping:   Map<Int, Int>,
                                           val protoIDMapping:  Map<Int, Int>,
                                           val fieldIDMapping:  Map<Int, Int>,
                                           val methodIDMapping: Map<Int, Int>): CodeVisitor, InstructionVisitor {

    private var instructions: MutableList<DexInstruction> = mutableListOf()

    override fun visitCode(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code) {
        instructions = mutableListOf()

        code.instructionsAccept(dexFile, classDef, method, method.code, this)

        val modifiedInstructions = InstructionWriter.writeInstructions(instructions)
        val newLength = modifiedInstructions.size

        if (newLength == code.insnsSize) {
            code.insns = modifiedInstructions
        } else {
            throw RuntimeException("fixed instructions have different size")
        }
    }

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {
        instructions.add(instruction)
    }

    override fun visitStringInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: StringInstruction) {
        instruction.stringIndex = stringIDMapping[instruction.stringIndex] ?: throw RuntimeException("unable to map stringIndex ${instruction.stringIndex}")
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    override fun visitTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: TypeInstruction) {
        instruction.typeIndex = typeIDMapping[instruction.typeIndex] ?: throw RuntimeException("unable to map typeIndex ${instruction.typeIndex}")
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    override fun visitFieldInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: FieldInstruction) {
        instruction.fieldIndex = fieldIDMapping[instruction.fieldIndex] ?: throw RuntimeException("unable to map fieldIndex ${instruction.fieldIndex}")
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    override fun visitMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        instruction.methodIndex = methodIDMapping[instruction.methodIndex] ?: throw RuntimeException("unable to map methodIndex ${instruction.methodIndex}")
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    override fun visitMethodProtoInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodProtoInstruction) {
        instruction.methodIndex = methodIDMapping[instruction.methodIndex] ?: throw RuntimeException("unable to map methodIndex ${instruction.methodIndex}")
        instruction.protoIndex  = protoIDMapping[instruction.protoIndex]   ?: throw RuntimeException("unable to map protoIndex ${instruction.protoIndex}")
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }
}
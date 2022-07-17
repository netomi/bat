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

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DataItem
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.annotation.AnnotationSet
import com.github.netomi.bat.dexfile.annotation.AnnotationSetRef
import com.github.netomi.bat.dexfile.annotation.AnnotationsDirectory
import com.github.netomi.bat.dexfile.visitor.*
import com.github.netomi.bat.dexfile.visitor.IDAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor

class DexSorter : DexFileVisitor {

    override fun visitDexFile(dexFile: DexFile) {
        sortDataItems(dexFile)
    }

    private fun sortDataItems(dexFile: DexFile) {
        val stringIDMapping = sortIDList(dexFile.stringIDs, compareBy { it.stringData })
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

        dexFile.classDefsAccept { _, _, classDef ->
            classDef.classData.directMethods.sortWith(compareBy { it.methodIndex })
            classDef.classData.virtualMethods.sortWith(compareBy { it.methodIndex })
        }

        dexFile.dataItemsAccept(object: DataItemVisitor {
            override fun visitAnyDataItem(dexFile: DexFile, dataItem: DataItem) {}
            override fun visitAnnotationSet(dexFile: DexFile, annotationSetRef: AnnotationSetRef, annotationSet: AnnotationSet) {
                annotationSet.annotations.sortWith(compareBy { it.annotationValue.typeIndex })
            }

            override fun visitAnnotationsDirectory(dexFile: DexFile, classDef: ClassDef, annotationsDirectory: AnnotationsDirectory) {
                annotationsDirectory.fieldAnnotations.sortWith(compareBy { it.fieldIndex })
                annotationsDirectory.methodAnnotations.sortWith(compareBy { it.methodIndex })
                annotationsDirectory.parameterAnnotations.sortWith(compareBy { it.methodIndex })
            }
        })

        dexFile.refreshCaches()
    }

    private fun <T> sortIDList(list: MutableList<T>, comparator: Comparator<T>): Map<Int, Int> {
        val oldIndices = list.withIndex().associate { iv -> Pair(iv.value, iv.index) }
        list.sortWith(comparator)
        val newIndices = list.withIndex().associate { iv -> Pair(iv.value, iv.index) }
        return oldIndices.keys.associate { stringID -> Pair(oldIndices[stringID]!!, newIndices[stringID]!!) }
    }
}
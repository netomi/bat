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
package com.github.netomi.bat.dexfile.io

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.github.netomi.bat.dexfile.visitor.DexFileVisitor
import com.github.netomi.bat.util.Primitives
import com.google.common.hash.Hashing
import java.io.InputStream

class DexFileReader(`is`: InputStream, strict: Boolean = true) : DexFileVisitor {

    private val input: DexDataInput
    private val strictParsing: Boolean

    init {
        input = DexDataInput(`is`)
        strictParsing = strict
    }

    override fun visitDexFile(dexFile: DexFile) {
        read(dexFile)

        if (strictParsing) {
            verifyChecksum(dexFile)
            verifySignature(dexFile)
        }
    }

    private fun verifyChecksum(dexFile: DexFile) {
        assert(dexFile.header != null)
        val adlerHasher = Hashing.adler32().newHasher()

        input.offset = 12
        input.update(adlerHasher)

        val checksum = adlerHasher.hash().asInt().toUInt()
        if (checksum != dexFile.header!!.checksum) {
            throw DexFormatException("Calculated checksum %s does not match %s.".format(Primitives.toHexString(checksum.toLong()),
                                                                                        Primitives.toHexString(dexFile.header!!.checksum.toLong())))
        }
    }

    private fun verifySignature(dexFile: DexFile) {
        assert(dexFile.header != null)
        val sha1Hasher = Hashing.sha1().newHasher()

        input.offset = 32
        input.update(sha1Hasher)

        val signature = sha1Hasher.hash().asBytes()

        if (!signature.contentEquals(dexFile.header!!.signature)) {
            throw DexFormatException("Calculated signature %s does not match %s.".format(Primitives.toHexString(signature),
                                                                                         Primitives.toHexString(dexFile.header!!.signature)))
        }
    }

    private fun read(dexFile: DexFile) {
        val header = readHeader()
        dexFile.header  = header

        val mapList = readMapList(header)
        dexFile.mapList = mapList

        readStringIDs(header, dexFile)
        readTypeIDs(header, dexFile)
        readProtoIDs(header, dexFile)
        readFieldIDs(header, dexFile)
        readMethodIDs(header, dexFile)
        readClassDefs(header, dexFile)
        readCallSiteIDs(mapList, dexFile)
        readMethodHandles(mapList, dexFile)
        readLinkedDataItems(dexFile)
        dexFile.linkData = readLinkData(header)
    }

    private fun readHeader(): DexHeader {
        return DexHeader.readHeader(input)
    }

    private fun readMapList(header: DexHeader): MapList {
        input.offset = header.mapOffset
        return MapList.readMapList(input)
    }

    private fun readStringIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.stringIDsOffsets

        dexFile.apply {
            stringIDs.clear()
            stringIDs.ensureCapacity(header.stringIDsSize)
            for (i in 0 until header.stringIDsSize) {
                val stringIDItem = StringID.readContent(input)
                stringIDs.add(i, stringIDItem)
            }
        }
    }

    private fun readTypeIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.typeIDsOffset
        dexFile.apply {
            typeIDs.clear()
            typeIDs.ensureCapacity(header.typeIDsSize)
            for (i in 0 until header.typeIDsSize) {
                val typeIDItem = TypeID.readContent(input)
                typeIDs.add(i, typeIDItem)
            }
        }
    }

    private fun readProtoIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.protoIDsOffset
        dexFile.apply {
            protoIDs.clear()
            protoIDs.ensureCapacity(header.protoIDsSize)
            for (i in 0 until header.protoIDsSize) {
                val protoIDItem = ProtoID.readContent(input)
                protoIDs.add(i, protoIDItem)
            }
        }
    }

        private fun readFieldIDs(header: DexHeader, dexFile: DexFile) {
            input.offset = header.fieldIDsOffset
            dexFile.apply {
                fieldIDs.clear()
                fieldIDs.ensureCapacity(header.fieldIDsSize)
                for (i in 0 until header.fieldIDsSize) {
                    val fieldIDItem = FieldID.readContent(input)
                    fieldIDs.add(i, fieldIDItem)
                }
            }
        }

        private fun readMethodIDs(header: DexHeader, dexFile: DexFile) {
            input.offset = header.methodIDsOffset
            dexFile.apply {
                methodIDs.clear()
                methodIDs.ensureCapacity(header.methodIDsSize)
                for (i in 0 until header.methodIDsSize) {
                    val methodIDItem = MethodID.readContent(input)
                    methodIDs.add(i, methodIDItem)
                }
            }
        }

        private fun readClassDefs(header: DexHeader, dexFile: DexFile) {
            input.offset = header.classDefsOffset
            dexFile.apply {
                classDefs.clear()
                classDefs.ensureCapacity(header.classDefsSize)
                for (i in 0 until header.classDefsSize) {
                    val classDefItem = ClassDef.readContent(input)
                    classDefs.add(i, classDefItem)
                }
            }
        }

        private fun readCallSiteIDs(mapList: MapList, dexFile: DexFile) {
            val mapItem: MapItem? = mapList.getMapItemByType(DexConstants.TYPE_CALL_SITE_ID_ITEM)
            if (mapItem != null) {
                input.offset = mapItem.offset
                dexFile.apply {
                    callSiteIDs.clear()
                    callSiteIDs.ensureCapacity(mapItem.size)
                    for (i in 0 until mapItem.size) {
                        val callSiteIDItem = CallSiteID.readContent(input)
                        callSiteIDs.add(i, callSiteIDItem)
                    }
                }
            }
        }

        private fun readMethodHandles(mapList: MapList, dexFile: DexFile) {
            val mapItem: MapItem? = mapList.getMapItemByType(DexConstants.TYPE_METHOD_HANDLE_ITEM)
            if (mapItem != null) {
                input.offset = mapItem.offset
                dexFile.apply {
                    methodHandles.clear()
                    methodHandles.ensureCapacity(mapItem.size)
                    for (i in 0 until mapItem.size) {
                        val methodHandleItem = MethodHandle.readContent(input)
                        methodHandles.add(i, methodHandleItem)
                    }
                }
            }
        }

        private fun readLinkedDataItems(dexFile: DexFile) {
            dexFile.dataItemsAccept(object : DataItemVisitor {
                override fun visitAnyDataItem(dexFile: DexFile, dataItem: DataItem) {
                    dataItem.readLinkedDataItems(input)
                }
            })
        }

        private fun readLinkData(header: DexHeader): ByteArray {
            val linkData = ByteArray(header.linkSize)
            input.offset = header.linkOffset
            input.readFully(linkData)
            return linkData
        }
}
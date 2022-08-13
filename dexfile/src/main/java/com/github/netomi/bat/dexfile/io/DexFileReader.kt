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
import com.github.netomi.bat.util.contentToHexString
import com.github.netomi.bat.util.toHexStringWithPrefix
import com.google.common.hash.Hashing
import java.io.InputStream

class DexFileReader(`is`: InputStream, verifyChecksum: Boolean = true) : DexFileVisitor {

    private val input:          DexDataInput = DexDataInput(`is`)
    private val verifyChecksum: Boolean      = verifyChecksum

    override fun visitDexFile(dexFile: DexFile) {
        read(dexFile)

        if (verifyChecksum) {
            verifyChecksum(dexFile)
            // signature verification seems to be disabled by libdex of
            // the Android project (used by dexdump), and some dex files
            // have invalid signature data (e.g. invoke-custom.dex taken
            // from the dexdump source repo), so disable the check as it
            // would lead to some false positives.
            // verifySignature(dexFile)
        }
    }

    @Suppress("UnstableApiUsage")
    private fun verifyChecksum(dexFile: DexFile) {
        assert(dexFile.header != null)
        val adlerHasher = Hashing.adler32().newHasher()

        input.offset = 12
        input.update(adlerHasher)

        val checksum = adlerHasher.hash().asInt()
        if (checksum != dexFile.header!!.checksum) {
            throw DexFormatException("Calculated checksum %s does not match %s."
                    .format(toHexStringWithPrefix(checksum),
                            toHexStringWithPrefix(dexFile.header!!.checksum)))
        }
    }

    @Suppress("UnstableApiUsage")
    private fun verifySignature(dexFile: DexFile) {
        assert(dexFile.header != null)
        @Suppress("DEPRECATION")
        val sha1Hasher = Hashing.sha1().newHasher()

        input.offset = 32
        input.update(sha1Hasher)

        val signature = sha1Hasher.hash().asBytes()

        if (!signature.contentEquals(dexFile.header!!.signature)) {
            throw DexFormatException("Calculated signature %s does not match %s.".format(signature.contentToHexString(),
                                                                                         dexFile.header!!.signature.contentToHexString()))
        }
    }

    private fun read(dexFile: DexFile) {
        dexFile.clear()

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

        dexFile.refreshCaches()
    }

    private fun readHeader(): DexHeader {
        return DexHeader.read(input)
    }

    private fun readMapList(header: DexHeader): MapList {
        input.offset = header.mapOffset
        return MapList.read(input)
    }

    private fun readStringIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.stringIDsOffsets

        dexFile.apply {
            stringIDs.ensureCapacity(header.stringIDsSize)
            val stringIDs = mutableListOf<StringID>()
            for (i in 0 until header.stringIDsSize) {
                val stringIDItem = StringID.read(input)
                stringIDs.add(stringIDItem)
            }

            val offset = input.offset

            // immediately read the linked data items, as the string value
            // is used for caching reasons when adding a StringID item.
            for (stringID in stringIDs) {
                stringID.readLinkedDataItems(input)
                dexFile.addStringID(stringID)
            }

            input.offset = offset
        }
    }

    private fun readTypeIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.typeIDsOffset
        dexFile.apply {
            typeIDs.ensureCapacity(header.typeIDsSize)
            for (i in 0 until header.typeIDsSize) {
                val typeIDItem = TypeID.read(input)
                typeIDs.add(i, typeIDItem)
            }
        }
    }

    private fun readProtoIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.protoIDsOffset
        dexFile.apply {
            protoIDs.ensureCapacity(header.protoIDsSize)
            for (i in 0 until header.protoIDsSize) {
                val protoIDItem = ProtoID.read(input)
                protoIDs.add(i, protoIDItem)
            }
        }
    }

    private fun readFieldIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.fieldIDsOffset
        dexFile.apply {
            fieldIDs.ensureCapacity(header.fieldIDsSize)
            for (i in 0 until header.fieldIDsSize) {
                val fieldIDItem = FieldID.read(input)
                fieldIDs.add(i, fieldIDItem)
            }
        }
    }

    private fun readMethodIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.methodIDsOffset
        dexFile.apply {
            methodIDs.ensureCapacity(header.methodIDsSize)
            for (i in 0 until header.methodIDsSize) {
                val methodIDItem = MethodID.read(input)
                methodIDs.add(i, methodIDItem)
            }
        }
    }

    private fun readClassDefs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.classDefsOffset
        dexFile.apply {
            classDefs.ensureCapacity(header.classDefsSize)
            for (i in 0 until header.classDefsSize) {
                val classDefItem = ClassDef.read(input)
                classDefs.add(i, classDefItem)
            }
        }
    }

    private fun readCallSiteIDs(mapList: MapList, dexFile: DexFile) {
        val mapItem: MapItem? = mapList.getMapItemByType(TYPE_CALL_SITE_ID_ITEM)
        if (mapItem != null) {
            input.offset = mapItem.offset
            dexFile.apply {
                callSiteIDs.ensureCapacity(mapItem.size)
                for (i in 0 until mapItem.size) {
                    val callSiteIDItem = CallSiteID.read(input)
                    callSiteIDs.add(i, callSiteIDItem)
                }
            }
        }
    }

    private fun readMethodHandles(mapList: MapList, dexFile: DexFile) {
        val mapItem: MapItem? = mapList.getMapItemByType(TYPE_METHOD_HANDLE_ITEM)
        if (mapItem != null) {
            input.offset = mapItem.offset
            dexFile.apply {
                methodHandles.ensureCapacity(mapItem.size)
                for (i in 0 until mapItem.size) {
                    val methodHandleItem = MethodHandle.read(input)
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

            // no need to read the linked data items for strings again as they have
            // been read during reading of the string ID item itself.
            override fun visitStringID(dexFile: DexFile, stringID: StringID) {}
        })
    }

    private fun readLinkData(header: DexHeader): ByteArray {
        val linkData = ByteArray(header.linkSize)
        input.offset = header.linkOffset
        input.readFully(linkData)
        return linkData
    }
}
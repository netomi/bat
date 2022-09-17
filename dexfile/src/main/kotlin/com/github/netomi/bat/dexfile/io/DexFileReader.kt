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
import com.github.netomi.bat.util.mutableListOfCapacity
import com.github.netomi.bat.util.toHexStringWithPrefix
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.Adler32


class DexFileReader(`is`: InputStream, private val verifyChecksum: Boolean = true) : DexFileVisitor {

    private val input: DexDataInput = DexDataInput(`is`)

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

    private fun verifyChecksum(dexFile: DexFile) {
        assert(dexFile.header != null)
        val adlerHasher = Adler32()

        input.offset = 12
        input.update(adlerHasher)

        val checksum = adlerHasher.value.toInt()
        if (checksum != dexFile.header!!.checksum) {
            throw DexFormatException("Calculated checksum %s does not match %s."
                    .format(toHexStringWithPrefix(checksum),
                            toHexStringWithPrefix(dexFile.header!!.checksum)))
        }
    }

    private fun verifySignature(dexFile: DexFile) {
        assert(dexFile.header != null)
        // every java platform is required to have an SHA-1 digest.
        val sha1Hasher = MessageDigest.getInstance("SHA-1")!!

        input.offset = 32
        input.update(sha1Hasher)

        val signature = sha1Hasher.digest()

        if (!signature.contentEquals(dexFile.header!!.signature)) {
            throw DexFormatException("Calculated signature %s does not match %s.".format(signature.contentToHexString(),
                                                                                         dexFile.header!!.signature.contentToHexString()))
        }
    }

    private fun read(dexFile: DexFile) {
        require(dexFile.isEmpty()) { "trying to read a dex file into a non-empty DexFile instance" }

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
        return DexHeader.read(input)
    }

    private fun readMapList(header: DexHeader): MapList {
        input.offset = header.mapOffset
        return MapList.read(input)
    }

    private fun readStringIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.stringIDsOffsets

        dexFile.apply {
            val readStringIDs = mutableListOf<StringID>()
            for (i in 0 until header.stringIDsSize) {
                val stringIDItem = StringID.read(input)
                readStringIDs.add(stringIDItem)
            }

            val offset = input.offset

            // immediately read the linked data items, as the string value
            // is used for caching reasons when adding a StringID item.
            stringIDs = mutableListOfCapacity(header.stringIDsSize)
            for (stringID in readStringIDs) {
                stringID.readLinkedDataItems(input)
                addStringID(stringID)
            }

            input.offset = offset
        }
    }

    private fun readTypeIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.typeIDsOffset
        dexFile.apply {
            typeIDs = mutableListOfCapacity(header.typeIDsSize)
            for (i in 0 until header.typeIDsSize) {
                val typeIDItem = TypeID.read(input)
                addTypeID(typeIDItem)
            }
        }
    }

    private fun readProtoIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.protoIDsOffset
        dexFile.apply {
            protoIDs = mutableListOfCapacity(header.protoIDsSize)
            for (i in 0 until header.protoIDsSize) {
                val protoIDItem = ProtoID.read(input)
                addProtoID(protoIDItem)
            }
        }
    }

    private fun readFieldIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.fieldIDsOffset
        dexFile.apply {
            fieldIDs = mutableListOfCapacity(header.fieldIDsSize)
            for (i in 0 until header.fieldIDsSize) {
                val fieldIDItem = FieldID.read(input)
                addFieldID(fieldIDItem)
            }
        }
    }

    private fun readMethodIDs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.methodIDsOffset
        dexFile.apply {
            methodIDs = mutableListOfCapacity(header.methodIDsSize)
            for (i in 0 until header.methodIDsSize) {
                val methodIDItem = MethodID.read(input)
                addMethodID(methodIDItem)
            }
        }
    }

    private fun readClassDefs(header: DexHeader, dexFile: DexFile) {
        input.offset = header.classDefsOffset
        dexFile.apply {
            classDefs = mutableListOfCapacity(header.classDefsSize)
            for (i in 0 until header.classDefsSize) {
                val classDefItem = ClassDef.read(input)
                addClassDef(classDefItem)
            }
        }
    }

    private fun readCallSiteIDs(mapList: MapList, dexFile: DexFile) {
        val mapItem: MapItem? = mapList.getMapItemByType(TYPE_CALL_SITE_ID_ITEM)
        if (mapItem != null) {
            input.offset = mapItem.offset
            dexFile.apply {
                callSiteIDs = mutableListOfCapacity(mapItem.size)
                for (i in 0 until mapItem.size) {
                    val callSiteIDItem = CallSiteID.read(input)
                    addCallSiteID(callSiteIDItem)
                }
            }
        }
    }

    private fun readMethodHandles(mapList: MapList, dexFile: DexFile) {
        val mapItem: MapItem? = mapList.getMapItemByType(TYPE_METHOD_HANDLE_ITEM)
        if (mapItem != null) {
            input.offset = mapItem.offset
            dexFile.apply {
                methodHandles = mutableListOfCapacity(mapItem.size)
                for (i in 0 until mapItem.size) {
                    val methodHandleItem = MethodHandle.read(input)
                    addMethodHandle(methodHandleItem)
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
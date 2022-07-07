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
package com.github.netomi.bat.dexfile

object DexConstants {
          val DEX_FILE_MAGIC          = byteArrayOf(0x64, 0x65, 0x78, 0x0a)
    const val ENDIAN_CONSTANT         = 0x12345678L
    const val REVERSE_ENDIAN_CONSTANT = 0x78563412L
    const val NO_INDEX                = -0x1

    // TYPE IDs as used by DataItems.
    const val TYPE_HEADER_ITEM                = 0x0000
    const val TYPE_STRING_ID_ITEM             = 0x0001
    const val TYPE_TYPE_ID_ITEM               = 0x0002
    const val TYPE_PROTO_ID_ITEM              = 0x0003
    const val TYPE_FIELD_ID_ITEM              = 0x0004
    const val TYPE_METHOD_ID_ITEM             = 0x0005
    const val TYPE_CLASS_DEF_ITEM             = 0x0006
    const val TYPE_CALL_SITE_ID_ITEM          = 0x0007
    const val TYPE_METHOD_HANDLE_ITEM         = 0x0008
    const val TYPE_MAP_LIST                   = 0x1000
    const val TYPE_TYPE_LIST                  = 0x1001
    const val TYPE_ANNOTATION_SET_REF_LIST    = 0x1002
    const val TYPE_ANNOTATION_SET_ITEM        = 0x1003
    const val TYPE_CLASS_DATA_ITEM            = 0x2000
    const val TYPE_CODE_ITEM                  = 0x2001
    const val TYPE_STRING_DATA_ITEM           = 0x2002
    const val TYPE_DEBUG_INFO_ITEM            = 0x2003
    const val TYPE_ANNOTATION_ITEM            = 0x2004
    const val TYPE_ENCODED_ARRAY_ITEM         = 0x2005
    const val TYPE_ANNOTATIONS_DIRECTORY_ITEM = 0x2006

    // Access flags
    const val ACC_PUBLIC                = 0x1
    const val ACC_PRIVATE               = 0x2
    const val ACC_PROTECTED             = 0x4
    const val ACC_STATIC                = 0x8
    const val ACC_FINAL                 = 0x10
    const val ACC_SYNCHRONIZED          = 0x20
    const val ACC_VOLATILE              = 0x40
    const val ACC_BRIDGE                = 0x40
    const val ACC_TRANSIENT             = 0x80
    const val ACC_VARARGS               = 0x80
    const val ACC_NATIVE                = 0x100
    const val ACC_INTERFACE             = 0x200
    const val ACC_ABSTRACT              = 0x400
    const val ACC_STRICT                = 0x800
    const val ACC_SYNTHETIC             = 0x1000
    const val ACC_ANNOTATION            = 0x2000
    const val ACC_ENUM                  = 0x4000
    const val ACC_CONSTRUCTOR           = 0x10000
    const val ACC_DECLARED_SYNCHRONIZED = 0x20000

    // Annotation Visibility types.
    const val VISIBILITY_BUILD: Short   = 0x00
    const val VISIBILITY_RUNTIME: Short = 0x01
    const val VISIBILITY_SYSTEM: Short  = 0x02

    // Method Handle Types.
    const val METHOD_HANDLE_TYPE_STATIC_PUT         = 0x00
    const val METHOD_HANDLE_TYPE_STATIC_GET         = 0x01
    const val METHOD_HANDLE_TYPE_INSTANCE_PUT       = 0x02
    const val METHOD_HANDLE_TYPE_INSTANCE_GET       = 0x03
    const val METHOD_HANDLE_TYPE_INVOKE_STATIC      = 0x04
    const val METHOD_HANDLE_TYPE_INVOKE_INSTANCE    = 0x05
    const val METHOD_HANDLE_TYPE_INVOKE_CONSTRUCTOR = 0x06
    const val METHOD_HANDLE_TYPE_INVOKE_DIRECT      = 0x07
    const val METHOD_HANDLE_TYPE_INVOKE_INTERFACE   = 0x08
}
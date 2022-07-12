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

const val NO_INDEX = -0x1

internal       val DEX_FILE_MAGIC          = byteArrayOf(0x64, 0x65, 0x78, 0x0a)
internal const val ENDIAN_CONSTANT         = 0x12345678L
internal const val REVERSE_ENDIAN_CONSTANT = 0x78563412L

// TYPE IDs as used by DataItems.
internal const val TYPE_HEADER_ITEM                = 0x0000
internal const val TYPE_STRING_ID_ITEM             = 0x0001
internal const val TYPE_TYPE_ID_ITEM               = 0x0002
internal const val TYPE_PROTO_ID_ITEM              = 0x0003
internal const val TYPE_FIELD_ID_ITEM              = 0x0004
internal const val TYPE_METHOD_ID_ITEM             = 0x0005
internal const val TYPE_CLASS_DEF_ITEM             = 0x0006
internal const val TYPE_CALL_SITE_ID_ITEM          = 0x0007
internal const val TYPE_METHOD_HANDLE_ITEM         = 0x0008
internal const val TYPE_MAP_LIST                   = 0x1000
internal const val TYPE_TYPE_LIST                  = 0x1001
internal const val TYPE_ANNOTATION_SET_REF_LIST    = 0x1002
internal const val TYPE_ANNOTATION_SET_ITEM        = 0x1003
internal const val TYPE_CLASS_DATA_ITEM            = 0x2000
internal const val TYPE_CODE_ITEM                  = 0x2001
internal const val TYPE_STRING_DATA_ITEM           = 0x2002
internal const val TYPE_DEBUG_INFO_ITEM            = 0x2003
internal const val TYPE_ANNOTATION_ITEM            = 0x2004
internal const val TYPE_ENCODED_ARRAY_ITEM         = 0x2005
internal const val TYPE_ANNOTATIONS_DIRECTORY_ITEM = 0x2006

// Access flags
internal const val ACC_PUBLIC                = 0x1
internal const val ACC_PRIVATE               = 0x2
internal const val ACC_PROTECTED             = 0x4
internal const val ACC_STATIC                = 0x8
internal const val ACC_FINAL                 = 0x10
internal const val ACC_SYNCHRONIZED          = 0x20
internal const val ACC_VOLATILE              = 0x40
internal const val ACC_BRIDGE                = 0x40
internal const val ACC_TRANSIENT             = 0x80
internal const val ACC_VARARGS               = 0x80
internal const val ACC_NATIVE                = 0x100
internal const val ACC_INTERFACE             = 0x200
internal const val ACC_ABSTRACT              = 0x400
internal const val ACC_STRICT                = 0x800
internal const val ACC_SYNTHETIC             = 0x1000
internal const val ACC_ANNOTATION            = 0x2000
internal const val ACC_ENUM                  = 0x4000
internal const val ACC_CONSTRUCTOR           = 0x10000
internal const val ACC_DECLARED_SYNCHRONIZED = 0x20000

// Annotation Visibility types.
internal const val VISIBILITY_BUILD: Short   = 0x00
internal const val VISIBILITY_RUNTIME: Short = 0x01
internal const val VISIBILITY_SYSTEM: Short  = 0x02

// Method Handle Types.
internal const val METHOD_HANDLE_TYPE_STATIC_PUT         = 0x00
internal const val METHOD_HANDLE_TYPE_STATIC_GET         = 0x01
internal const val METHOD_HANDLE_TYPE_INSTANCE_PUT       = 0x02
internal const val METHOD_HANDLE_TYPE_INSTANCE_GET       = 0x03
internal const val METHOD_HANDLE_TYPE_INVOKE_STATIC      = 0x04
internal const val METHOD_HANDLE_TYPE_INVOKE_INSTANCE    = 0x05
internal const val METHOD_HANDLE_TYPE_INVOKE_CONSTRUCTOR = 0x06
internal const val METHOD_HANDLE_TYPE_INVOKE_DIRECT      = 0x07
internal const val METHOD_HANDLE_TYPE_INVOKE_INTERFACE   = 0x08
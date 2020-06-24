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

package com.github.netomi.bat.dexfile;

public final class DexConstants
{
    private DexConstants() {}

    public static final byte[] DEX_FILE_MAGIC = { 0x64, 0x65, 0x78, 0x0a };
    
    public static final long ENDIAN_CONSTANT         = 0x12345678L;
    public static final long REVERSE_ENDIAN_CONSTANT = 0x78563412L;

    public static final int NO_INDEX = 0xffffffff;

    // TYPE IDs as used by DataItems.

    public static final int TYPE_HEADER_ITEM                = 0x0000;
    public static final int TYPE_STRING_ID_ITEM             = 0x0001;
    public static final int TYPE_TYPE_ID_ITEM               = 0x0002;
    public static final int TYPE_PROTO_ID_ITEM              = 0x0003;
    public static final int TYPE_FIELD_ID_ITEM              = 0x0004;
    public static final int TYPE_METHOD_ID_ITEM             = 0x0005;
    public static final int TYPE_CLASS_DEF_ITEM             = 0x0006;
    public static final int TYPE_CALL_SITE_ID_ITEM          = 0x0007;
    public static final int TYPE_METHOD_HANDLE_ITEM         = 0x0008;
    public static final int TYPE_MAP_LIST                   = 0x1000;
    public static final int TYPE_TYPE_LIST                  = 0x1001;
    public static final int TYPE_ANNOTATION_SET_REF_LIST    = 0x1002;
    public static final int TYPE_ANNOTATION_SET_ITEM        = 0x1003;
    public static final int TYPE_CLASS_DATA_ITEM            = 0x2000;
    public static final int TYPE_CODE_ITEM                  = 0x2001;
    public static final int TYPE_STRING_DATA_ITEM           = 0x2002;
    public static final int TYPE_DEBUG_INFO_ITEM            = 0x2003;
    public static final int TYPE_ANNOTATION_ITEM            = 0x2004;
    public static final int TYPE_ENCODED_ARRAY_ITEM         = 0x2005;
    public static final int TYPE_ANNOTATIONS_DIRECTORY_ITEM = 0x2006;

    // Access flags

    public static final int ACC_PUBLIC                = 0x1;
    public static final int ACC_PRIVATE               = 0x2;
    public static final int ACC_PROTECTED             = 0x4;
    public static final int ACC_STATIC                = 0x8;
    public static final int ACC_FINAL                 = 0x10;
    public static final int ACC_SYNCHRONIZED          = 0x20;
    public static final int ACC_VOLATILE              = 0x40;
    public static final int ACC_BRIDGE                = 0x40;
    public static final int ACC_TRANSIENT             = 0x80;
    public static final int ACC_VARARGS               = 0x80;
    public static final int ACC_NATIVE                = 0x100;
    public static final int ACC_INTERFACE             = 0x200;
    public static final int ACC_ABSTRACT              = 0x400;
    public static final int ACC_STRICT                = 0x800;
    public static final int ACC_SYNTHETIC             = 0x1000;
    public static final int ACC_ANNOTATION            = 0x2000;
    public static final int ACC_ENUM                  = 0x4000;
    public static final int ACC_CONSTRUCTOR           = 0x10000;
    public static final int ACC_DECLARED_SYNCHRONIZED = 0x20000;

    // Annotation Visibility types.

    public static final int VISIBILITY_BUILD   = 0x00;
    public static final int VISIBILITY_RUNTIME = 0x01;
    public static final int VISIBILITY_SYSTEM  = 0x02;

    // Method Handle Types.

    public static final int METHOD_HANDLE_TYPE_STATIC_PUT      = 0x00;
    public static final int METHOD_HANDLE_TYPE_STATIC_GET      = 0x01;
    public static final int METHOD_HANDLE_TYPE_INSTANCE_PUT    = 0x02;
    public static final int METHOD_HANDLE_TYPE_INSTANCE_GET    = 0x03;
    public static final int METHOD_HANDLE_TYPE_INVOKE_STATIC   = 0x04;
    public static final int METHOD_HANDLE_TYPE_INVOKE_INSTANCE = 0x05;
}

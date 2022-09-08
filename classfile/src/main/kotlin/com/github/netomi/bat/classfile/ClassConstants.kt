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
package com.github.netomi.bat.classfile

const val MAGIC : Int = 0xCA_FE_BA_BE.toInt() // need to explicitly convert to int due to KT-4749

const val MAJOR_VERSION_1_0  = 45
const val MAJOR_VERSION_1_1  = 45
const val MAJOR_VERSION_1_2  = 46
const val MAJOR_VERSION_1_3  = 47
const val MAJOR_VERSION_1_4  = 48
const val MAJOR_VERSION_5_0  = 49
const val MAJOR_VERSION_6_0  = 50
const val MAJOR_VERSION_7_0  = 51
const val MAJOR_VERSION_8_0  = 52
const val MAJOR_VERSION_9_0  = 53
const val MAJOR_VERSION_10_0 = 54
const val MAJOR_VERSION_11_0 = 55
const val MAJOR_VERSION_12_0 = 56
const val MAJOR_VERSION_13_0 = 57
const val MAJOR_VERSION_14_0 = 58
const val MAJOR_VERSION_15_0 = 59
const val MAJOR_VERSION_16_0 = 60
const val MAJOR_VERSION_17_0 = 61

// General access flags (for class / fields / methods).
const val ACC_PUBLIC       = 0x0001
const val ACC_PRIVATE      = 0x0002
const val ACC_PROTECTED    = 0x0004
const val ACC_STATIC       = 0x0008
const val ACC_FINAL        = 0x0010
const val ACC_SUPER        = 0x0020
const val ACC_SYNCHRONIZED = 0x0020
const val ACC_OPEN         = 0x0020
const val ACC_TRANSITIVE   = 0x0020
const val ACC_VOLATILE     = 0x0040
const val ACC_BRIDGE       = 0x0040
const val ACC_STATIC_PHASE = 0x0040
const val ACC_TRANSIENT    = 0x0080
const val ACC_VARARGS      = 0x0080
const val ACC_NATIVE       = 0x0100
const val ACC_INTERFACE    = 0x0200
const val ACC_ABSTRACT     = 0x0400
const val ACC_STRICT       = 0x0800
const val ACC_SYNTHETIC    = 0x1000
const val ACC_ANNOTATION   = 0x2000
const val ACC_ENUM         = 0x4000
const val ACC_MODULE       = 0x8000
const val ACC_MANDATED     = 0x8000

// Reference kinds:
// https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-5.html#jvms-5.4.3.5-220
const val REF_getField         = 1
const val REF_getStatic        = 2
const val REF_putField         = 3
const val REF_putStatic        = 4
const val REF_invokeVirtual    = 5
const val REF_invokeStatic     = 6
const val REF_invokeSpecial    = 7
const val REF_newInvokeSpecial = 8
const val REF_invokeInterface  = 9

// Item types for union verification_type_info
// https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.4
const val ITEM_Top               = 0
const val ITEM_Integer           = 1
const val ITEM_Float             = 2
const val ITEM_Double            = 3
const val ITEM_Long              = 4
const val ITEM_Null              = 5
const val ITEM_UninitializedThis = 6
const val ITEM_Object            = 7
const val ITEM_Uninitialized     = 8

// Array Types
const val T_BOOLEAN =  4
const val T_CHAR    =  5
const val T_FLOAT   =  6
const val T_DOUBLE  =  7
const val T_BYTE    =  8
const val T_SHORT   =  9
const val T_INT     = 10
const val T_LONG    = 11

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

package com.github.netomi.bat.test;

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.io.DexFileReader;
import com.github.netomi.bat.dexfile.io.DexFileWriter;
import com.github.netomi.bat.dexfile.io.DexFilePrinter;

import java.io.*;

public class Test {

    public static void main(String[] args) {
        DexFile dexFile = new DexFile();

        try (InputStream  is = new FileInputStream("classes.dex");
             OutputStream os = new FileOutputStream("classes2.dex")) {

            DexFileReader reader = new DexFileReader(is);

            reader.visitDexFile(dexFile);

            dexFile.accept(new DexFilePrinter());

            DexFileWriter writer = new DexFileWriter(os);

            dexFile.accept(writer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

//Processing 'classes.dex'...
//Opened 'classes.dex', DEX version '035'
//DEX file header:
//magic               : 'dex\n035\0'
//checksum            : e6638088
//signature           : 3433...4c46
//file_size           : 2432
//header_size         : 112
//link_size           : 0
//link_off            : 0 (0x000000)
//string_ids_size     : 52
//string_ids_off      : 112 (0x000070)
//type_ids_size       : 20
//type_ids_off        : 320 (0x000140)
//proto_ids_size       : 15
//proto_ids_off        : 400 (0x000190)
//field_ids_size      : 3
//field_ids_off       : 580 (0x000244)
//method_ids_size     : 22
//method_ids_off      : 604 (0x00025c)
//class_defs_size     : 1
//class_defs_off      : 780 (0x00030c)
//data_size           : 1620
//data_off            : 812 (0x00032c)
//
//Class #0 header:
//class_idx           : 10
//access_flags        : 1 (0x0001)
//superclass_idx      : 2
//interfaces_off      : 0 (0x000000)
//source_file_idx     : 0
//annotations_off     : 0 (0x000000)
//class_data_off      : 2209 (0x0008a1)
//static_fields_size  : 3
//instance_fields_size: 0
//direct_methods_size : 3
//virtual_methods_size: 1
//
//Class #0            -
//  Class descriptor  : 'Lcom/example/HelloWorldActivity;'
//  Access flags      : 0x0001 (PUBLIC)
//  Superclass        : 'Landroid/app/Activity;'
//  Interfaces        -
//  Static fields     -
//    #0              : (in Lcom/example/HelloWorldActivity;)
//      name          : 'ˊ'
//      type          : 'I'
//      access        : 0x000a (PRIVATE STATIC)
//    #1              : (in Lcom/example/HelloWorldActivity;)
//      name          : 'ˋ'
//      type          : 'B'
//      access        : 0x000a (PRIVATE STATIC)
//    #2              : (in Lcom/example/HelloWorldActivity;)
//      name          : 'ॱ'
//      type          : 'I'
//      access        : 0x000a (PRIVATE STATIC)
//  Instance fields   -
//  Direct methods    -
//    #0              : (in Lcom/example/HelloWorldActivity;)
//      name          : '<clinit>'
//      type          : '()V'
//      access        : 0x10008 (STATIC CONSTRUCTOR)
//      code          -
//      registers     : 0
//      ins           : 0
//      outs          : 0
//      insns size    : 1 16-bit code units
//0003a0:                                        |[0003a0] com.example.HelloWorldActivity.<clinit>:()V
//0003b0: 0e00                                   |0000: return-void
//      catches       : (none)
//      positions     :
//      locals        :
//
//    #1              : (in Lcom/example/HelloWorldActivity;)
//      name          : '<init>'
//      type          : '()V'
//      access        : 0x10001 (PUBLIC CONSTRUCTOR)
//      code          -
//      registers     : 1
//      ins           : 1
//      outs          : 1
//      insns size    : 4 16-bit code units
//0003b4:                                        |[0003b4] com.example.HelloWorldActivity.<init>:()V
//0003c4: 7010 0000 0000                         |0000: invoke-direct {v0}, Landroid/app/Activity;.<init>:()V // method@0000
//0003ca: 0e00                                   |0003: return-void
//      catches       : (none)
//      positions     :
//        0x0000 line=16
//      locals        :
//        0x0000 - 0x0004 reg=0 this Lcom/example/HelloWorldActivity;
//
//    #2              : (in Lcom/example/HelloWorldActivity;)
//      name          : 'ˋ'
//      type          : '(Ljava/lang/String;)Ljava/lang/String;'
//      access        : 0x0002 (PRIVATE)
//      code          -
//      registers     : 6
//      ins           : 2
//      outs          : 3
//      insns size    : 43 16-bit code units
//00032c:                                        |[00032c] com.example.HelloWorldActivity.ˋ:(Ljava/lang/String;)Ljava/lang/String;
//00033c: 1200                                   |0000: const/4 v0, #int 0 // #0
//00033e: 7120 0300 0500                         |0001: invoke-static {v5, v0}, Landroid/util/Base64;.decode:(Ljava/lang/String;I)[B // method@0003
//000344: 0c05                                   |0004: move-result-object v5
//000346: 2151                                   |0005: array-length v1, v5
//000348: 2311 1300                              |0006: new-array v1, v1, [B // type@0013
//00034c: 2152                                   |0008: array-length v2, v5
//00034e: 3520 1100                              |0009: if-ge v0, v2, 001a // +0011
//000352: 2152                                   |000b: array-length v2, v5
//000354: b102                                   |000c: sub-int/2addr v2, v0
//000356: d802 02ff                              |000d: add-int/lit8 v2, v2, #int -1 // #ff
//00035a: 4802 0502                              |000f: aget-byte v2, v5, v2
//00035e: 6403 0100                              |0011: sget-byte v3, Lcom/example/HelloWorldActivity;.ˋ:B // field@0001
//000362: b732                                   |0013: xor-int/2addr v2, v3
//000364: 8d22                                   |0014: int-to-byte v2, v2
//000366: 4f02 0100                              |0015: aput-byte v2, v1, v0
//00036a: d800 0001                              |0017: add-int/lit8 v0, v0, #int 1 // #01
//00036e: 28ef                                   |0019: goto 0008 // -0011
//000370: 2205 0f00                              |001a: new-instance v5, Ljava/lang/String; // type@000f
//000374: 1a00 1c00                              |001c: const-string v0, "UTF-8" // string@001c
//000378: 7030 1200 1500                         |001e: invoke-direct {v5, v1, v0}, Ljava/lang/String;.<init>:([BLjava/lang/String;)V // method@0012
//00037e: 1105                                   |0021: return-object v5
//000380: 0d05                                   |0022: move-exception v5
//000382: 2200 0e00                              |0023: new-instance v0, Ljava/lang/RuntimeException; // type@000e
//000386: 7020 1100 5000                         |0025: invoke-direct {v0, v5}, Ljava/lang/RuntimeException;.<init>:(Ljava/lang/Throwable;)V // method@0011
//00038c: 2802                                   |0028: goto 002a // +0002
//00038e: 2700                                   |0029: throw v0
//000390: 28ff                                   |002a: goto 0029 // -0001
//      catches       : 1
//        0x0001 - 0x0021
//          Ljava/io/UnsupportedEncodingException; -> 0x0022
//      positions     :
//        0x0001 line=1046
//        0x0005 line=1047
//        0x0008 line=1048
//        0x000b line=1050
//        0x001a line=1052
//        0x0023 line=1056
//      locals        :
//        0x0000 - 0x002b reg=4 this Lcom/example/HelloWorldActivity;
//
//  Virtual methods   -
//    #0              : (in Lcom/example/HelloWorldActivity;)
//      name          : 'onCreate'
//      type          : '(Landroid/os/Bundle;)V'
//      access        : 0x0001 (PUBLIC)
//      code          -
//      registers     : 9
//      ins           : 2
//      outs          : 3
//      insns size    : 142 16-bit code units
//0003cc:                                        |[0003cc] com.example.HelloWorldActivity.onCreate:(Landroid/os/Bundle;)V
//0003dc: 6f20 0100 8700                         |0000: invoke-super {v7, v8}, Landroid/app/Activity;.onCreate:(Landroid/os/Bundle;)V // method@0001
//0003e2: 2208 0800                              |0003: new-instance v8, Landroid/widget/TextView; // type@0008
//0003e6: 7020 0700 7800                         |0005: invoke-direct {v8, v7}, Landroid/widget/TextView;.<init>:(Landroid/content/Context;)V // method@0007
//0003ec: 1a00 0600                              |0008: const-string v0, "Hello, world!" // string@0006
//0003f0: 6e20 0900 0800                         |000a: invoke-virtual {v8, v0}, Landroid/widget/TextView;.setText:(Ljava/lang/CharSequence;)V // method@0009
//0003f6: 1300 1100                              |000d: const/16 v0, #int 17 // #11
//0003fa: 6e20 0800 0800                         |000f: invoke-virtual {v8, v0}, Landroid/widget/TextView;.setGravity:(I)V // method@0008
//000400: 6e20 0f00 8700                         |0012: invoke-virtual {v7, v8}, Lcom/example/HelloWorldActivity;.setContentView:(Landroid/view/View;)V // method@000f
//000406: 2208 0700                              |0015: new-instance v8, Landroid/widget/Button; // type@0007
//00040a: 7020 0500 7800                         |0017: invoke-direct {v8, v7}, Landroid/widget/Button;.<init>:(Landroid/content/Context;)V // method@0005
//000410: 1401 0100 037f                         |001a: const v1, #float 174128887730233583002069148843976425472.000000 // #7f030001
//000416: 6e10 0400 0800                         |001d: invoke-virtual {v8}, Landroid/view/View;.getContext:()Landroid/content/Context; // method@0004
//00041c: 0c02                                   |0020: move-result-object v2
//00041e: 6e20 0200 1200                         |0021: invoke-virtual {v2, v1}, Landroid/content/Context;.getString:(I)Ljava/lang/String; // method@0002
//000424: 0c01                                   |0024: move-result-object v1
//000426: 1a02 0100                              |0025: const-string v2, """,��" // string@0001
//00042a: 6e20 1400 2100                         |0027: invoke-virtual {v1, v2}, Ljava/lang/String;.startsWith:(Ljava/lang/String;)Z // method@0014
//000430: 0a02                                   |002a: move-result v2
//000432: 1203                                   |002b: const/4 v3, #int 0 // #0
//000434: 1214                                   |002c: const/4 v4, #int 1 // #1
//000436: 3802 0400                              |002d: if-eqz v2, 0031 // +0004
//00043a: 1202                                   |002f: const/4 v2, #int 0 // #0
//00043c: 2802                                   |0030: goto 0032 // +0002
//00043e: 1212                                   |0031: const/4 v2, #int 1 // #1
//000440: 1205                                   |0032: const/4 v5, #int 0 // #0
//000442: 3242 3300                              |0033: if-eq v2, v4, 0066 // +0033
//000446: 6002 0000                              |0035: sget v2, Lcom/example/HelloWorldActivity;.ˊ:I // field@0000
//00044a: d802 024d                              |0037: add-int/lit8 v2, v2, #int 77 // #4d
//00044e: d426 8000                              |0039: rem-int/lit16 v6, v2, #int 128 // #0080
//000452: 6706 0200                              |003b: sput v6, Lcom/example/HelloWorldActivity;.ॱ:I // field@0002
//000456: dc02 0202                              |003d: rem-int/lit8 v2, v2, #int 2 // #02
//00045a: 3802 0400                              |003f: if-eqz v2, 0043 // +0004
//00045e: 1212                                   |0041: const/4 v2, #int 1 // #1
//000460: 2802                                   |0042: goto 0044 // +0002
//000462: 1202                                   |0043: const/4 v2, #int 0 // #0
//000464: 1246                                   |0044: const/4 v6, #int 4 // #4
//000466: 3242 0f00                              |0045: if-eq v2, v4, 0054 // +000f
//00046a: 6e20 1500 6100                         |0047: invoke-virtual {v1, v6}, Ljava/lang/String;.substring:(I)Ljava/lang/String; // method@0015
//000470: 0c01                                   |004a: move-result-object v1
//000472: 7020 1000 1700                         |004b: invoke-direct {v7, v1}, Lcom/example/HelloWorldActivity;.ˋ:(Ljava/lang/String;)Ljava/lang/String; // method@0010
//000478: 0c01                                   |004e: move-result-object v1
//00047a: 6e10 1300 0100                         |004f: invoke-virtual {v1}, Ljava/lang/String;.intern:()Ljava/lang/String; // method@0013
//000480: 0c01                                   |0052: move-result-object v1
//000482: 2813                                   |0053: goto 0066 // +0013
//000484: 6e20 1500 6100                         |0054: invoke-virtual {v1, v6}, Ljava/lang/String;.substring:(I)Ljava/lang/String; // method@0015
//00048a: 0c01                                   |0057: move-result-object v1
//00048c: 7020 1000 1700                         |0058: invoke-direct {v7, v1}, Lcom/example/HelloWorldActivity;.ˋ:(Ljava/lang/String;)Ljava/lang/String; // method@0010
//000492: 0c01                                   |005b: move-result-object v1
//000494: 6e10 1300 0100                         |005c: invoke-virtual {v1}, Ljava/lang/String;.intern:()Ljava/lang/String; // method@0013
//00049a: 0c01                                   |005f: move-result-object v1
//00049c: 2152                                   |0060: array-length v2, v5
//00049e: 2805                                   |0061: goto 0066 // +0005
//0004a0: 0d08                                   |0062: move-exception v8
//0004a2: 2708                                   |0063: throw v8
//0004a4: 0d08                                   |0064: move-exception v8
//0004a6: 2708                                   |0065: throw v8
//0004a8: 6e20 0900 1800                         |0066: invoke-virtual {v8, v1}, Landroid/widget/TextView;.setText:(Ljava/lang/CharSequence;)V // method@0009
//0004ae: 6e20 0600 0800                         |0069: invoke-virtual {v8, v0}, Landroid/widget/Button;.setGravity:(I)V // method@0006
//0004b4: 6e20 0f00 8700                         |006c: invoke-virtual {v7, v8}, Lcom/example/HelloWorldActivity;.setContentView:(Landroid/view/View;)V // method@000f
//0004ba: 1a08 0500                              |006f: const-string v8, "DexGuard has optimized, converted, signed, and aligned this sample" // string@0005
//0004be: 7130 0a00 8704                         |0071: invoke-static {v7, v8, v4}, Landroid/widget/Toast;.makeText:(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast; // method@000a
//0004c4: 0c08                                   |0074: move-result-object v8
//0004c6: 6e10 0b00 0800                         |0075: invoke-virtual {v8}, Landroid/widget/Toast;.show:()V // method@000b
//0004cc: 6008 0200                              |0078: sget v8, Lcom/example/HelloWorldActivity;.ॱ:I // field@0002
//0004d0: d808 082b                              |007a: add-int/lit8 v8, v8, #int 43 // #2b
//0004d4: d480 8000                              |007c: rem-int/lit16 v0, v8, #int 128 // #0080
//0004d8: 6700 0000                              |007e: sput v0, Lcom/example/HelloWorldActivity;.ˊ:I // field@0000
//0004dc: dc08 0802                              |0080: rem-int/lit8 v8, v8, #int 2 // #02
//0004e0: 3908 0300                              |0082: if-nez v8, 0085 // +0003
//0004e4: 1213                                   |0084: const/4 v3, #int 1 // #1
//0004e6: 3803 0600                              |0085: if-eqz v3, 008b // +0006
//0004ea: 2158                                   |0087: array-length v8, v5
//0004ec: 0e00                                   |0088: return-void
//0004ee: 0d08                                   |0089: move-exception v8
//0004f0: 2708                                   |008a: throw v8
//0004f2: 0e00                                   |008b: return-void
//0004f4: 0d08                                   |008c: move-exception v8
//0004f6: 2708                                   |008d: throw v8
//      catches       : 5
//        0x0054 - 0x0058
//          Ljava/lang/Exception; -> 0x0064
//        0x005c - 0x0060
//          Ljava/lang/Exception; -> 0x008c
//        0x0060 - 0x0061
//          <any> -> 0x0062
//        0x0066 - 0x0078
//          Ljava/lang/Exception; -> 0x008c
//        0x0087 - 0x0088
//          <any> -> 0x0089
//      positions     :
//        0x0000 line=21
//        0x0003 line=24
//        0x000a line=25
//        0x000f line=26
//        0x0012 line=27
//        0x0015 line=29
//        0x001d line=30
//        0x0035 line=35
//        0x0063 line=0
//        0x0065 line=35
//        0x0066 line=0
//        0x0069 line=31
//        0x006c line=32
//        0x0071 line=35
//      locals        :
//        0x0000 - 0x008e reg=7 this Lcom/example/HelloWorldActivity;
//
//  source_file_idx   : 0 ()

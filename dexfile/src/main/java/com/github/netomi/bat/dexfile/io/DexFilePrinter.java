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

package com.github.netomi.bat.dexfile.io;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.instruction.DexInstruction;
import com.github.netomi.bat.dexfile.util.Primitives;
import com.github.netomi.bat.dexfile.visitor.*;

import java.io.PrintStream;

public class DexFilePrinter
extends      DefaultDexVisitor
implements   DexFileVisitor,
             DexHeaderVisitor,
             ClassDefVisitor,
             ClassDataVisitor,
             EncodedFieldVisitor,
             EncodedMethodVisitor,
             TypeListVisitor,
             TypeVisitor,
             CodeVisitor,
             InstructionVisitor
{
    private final PrintStream ps;

    public DexFilePrinter() {
        this(System.out);
    }

    public DexFilePrinter(PrintStream ps) {
        this.ps               = ps;
    }

    @Override
    public void visitDexFile(DexFile dexFile) {
        dexFile.headerAccept(this);
        dexFile.classDefsAccept(this);
    }

    @Override
    public void visitHeader(DexFile dexFile, DexHeader header) {
        ps.println("DEX file header:");
        ps.println("magic               : '" + Primitives.toAsciiString(header.magic) + "'");
        ps.println("checksum            : " + Primitives.toHexString(header.checksum));
        ps.println("signature           : " + Primitives.toHexString(header.signature));
        ps.println("file_size           : " + header.fileSize);
        ps.println("header_size         : " + header.headerSize);
        ps.println("link_size           : " + header.linkSize);
        ps.println("link_off            : " + formatNumber(header.linkOffset));
        ps.println("string_ids_size     : " + header.stringIDsSize);
        ps.println("string_ids_off      : " + formatNumber(header.stringIDsOffsets));
        ps.println("type_ids_size       : " + header.typeIDsSize);
        ps.println("type_ids_off        : " + formatNumber(header.typeIDsOffset));
        ps.println("proto_ids_size      : " + header.protoIDsSize);
        ps.println("proto_ids_off       : " + formatNumber(header.protoIDsOffset));
        ps.println("field_ids_size      : " + header.fieldIDsSize);
        ps.println("field_ids_off       : " + formatNumber(header.fieldIDsOffset));
        ps.println("method_ids_size     : " + header.methodIDsSize);
        ps.println("method_ids_off      : " + formatNumber(header.methodIDsOffset));
        ps.println("class_defs_size     : " + header.classDefsSize);
        ps.println("class_defs_off      : " + formatNumber(header.classDefsOffset));
        ps.println("data_size           : " + header.dataSize);
        ps.println("data_off            : " + formatNumber(header.dataOffset));
        ps.println();
    }

    @Override
    public void visitClassDef(DexFile dexFile, int index, ClassDef classDefItem) {
        ps.println("Class #" + index + " header:");
        ps.println("class_idx           : " + classDefItem.classIndex);
        ps.println("access_flags        : " + formatNumber(classDefItem.accessFlags));
        ps.println("superclass_idx      : " + classDefItem.superClassIndex);
        ps.println("interfaces_off      : " + formatNumber(classDefItem.getInterfacesOffset()));
        ps.println("source_file_idx     : " + classDefItem.sourceFileIndex);
        ps.println("annotations_off     : " + formatNumber(classDefItem.getAnnotationsOffset()));
        ps.println("class_data_off      : " + formatNumber(classDefItem.getClassDataOffset()));
        ps.println("static_fields_size  : " + classDefItem.classData.staticFields.size());
        ps.println("instance_fields_size: " + classDefItem.classData.instanceFields.size());
        ps.println("direct_methods_size : " + classDefItem.classData.directMethods.size());
        ps.println("virtual_methods_size: " + classDefItem.classData.virtualMethods.size());
        ps.println();

        ps.println("Class #" + index);
        ps.println("  Class descriptor  : '" + classDefItem.getType(dexFile) + "'");
        ps.println("  Access flags      : " + formatAccessFlags(classDefItem.accessFlags));
        ps.println("  Superclass        : '" + classDefItem.getSuperClassType(dexFile) + "'");
        ps.println("  Interfaces        -");
        classDefItem.interfacesAccept(dexFile, this);

        classDefItem.classDataAccept(dexFile, this);

        ps.println("  source_file_idx   : " + getSourceFileIndex(dexFile, classDefItem));
        ps.println();
    }

    @Override
    public void visitClassData(DexFile dexFile, ClassDef classDef, ClassData classData) {
        ps.println("  Static fields     -");
        classData.staticFieldsAccept(dexFile, classDef, this);
        ps.println("  Instance fields   -");
        classData.instanceFieldsAccept(dexFile, classDef, this);
        ps.println("  Direct methods    -");
        classData.directMethodsAccept(dexFile, classDef, this);
        ps.println("  Virtual methods   -");
        classData.virtualMethodsAccept(dexFile, classDef, this);
    }

    @Override
    public void visitAnyField(DexFile dexFile, ClassDef classDef, ClassData classData, int index, EncodedField encodedField) {
        ps.println(String.format("    #%-14d : (in %s)", index, classDef.getType(dexFile)));
        ps.println("      name          : '" + encodedField.getName(dexFile) + "'");
        ps.println("      type          : '" + encodedField.getType(dexFile) + "'");
        ps.println("      access        : " + formatAccessFlags(encodedField.accessFlags));
    }

    @Override
    public void visitAnyMethod(DexFile dexFile, ClassDef classDef, ClassData classData, int index, EncodedMethod encodedMethod) {
        ps.println(String.format("    #%-14d : (in %s)", index, classDef.getType(dexFile)));
        ps.println("      name          : '" + encodedMethod.getName(dexFile) + "'");
        ps.println("      type          : '" + encodedMethod.getShortyType(dexFile) + "'");
        ps.println("      access        : " + formatAccessFlags(encodedMethod.accessFlags));
        encodedMethod.codeAccept(dexFile, classDef, classData, this);
    }

    @Override
    public void visitCode(DexFile dexFile, ClassDef classDef, ClassData classData, EncodedMethod method, Code code) {
        ps.println("      code          -");
        ps.println("      registers     : " + code.registersSize);
        ps.println("      ins           : " + code.insSize);
        ps.println("      outs          : " + code.outsSize);
        ps.println("      insns size    : " + code.insnsSize + " 16-bit code units");

        int codeOffset = method.getCodeOffset();

        ps.println(asHexValue(codeOffset) + ":                                        |[" +
                   asHexValue(codeOffset) + "] " +
                   DexUtil.fullExternalMethodSignature(dexFile, classDef, method));

        code.instructionsAccept(dexFile, classDef, classData, method, code, this);
//0003a0:                                        |[0003a0] com.example.HelloWorldActivity.<clinit>:()V
//0003b0: 0e00                                   |0000: return-void
//      catches       : (none)
//      positions     :
//      locals        :

    }

    @Override
    public void visitInstruction(DexFile dexFile, ClassDef classDef, ClassData classData, EncodedMethod method, Code code, int offset, DexInstruction instruction) {
        ps.println(instruction.toString());
    }

    @Override
    public void visitInterfaces(DexFile dexFile, ClassDef classDefItem, TypeList typeList) {
        if (typeList != null) {
            typeList.typesAccept(dexFile, this);
        }
    }

    @Override
    public void visitType(DexFile dexFile, TypeList typeList, int index, String type) {
        ps.println(String.format("    #%-14d : '%s'", index, type));
    }

    // Private utility methods.

    private static String getSourceFileIndex(DexFile dexFile, ClassDef classDefItem) {
        return classDefItem.sourceFileIndex == DexConstants.NO_INDEX ?
                classDefItem.sourceFileIndex + " (unknown)" :
                classDefItem.getSourceFile(dexFile);
    }

    private static String formatNumber(long number) {
        return String.format("%d (0x%04x)", number, number);
    }

    private static String formatAccessFlags(int accessFlags) {
        return String.format("0x%04x (%s)", accessFlags, DexAccessFlags.formatAsHumanReadable(accessFlags));
    }

    private static String asHexValue(int value) {
        return String.format("%06x", value);
    }

}

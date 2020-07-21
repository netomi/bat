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
import com.github.netomi.bat.dexfile.debug.*;
import com.github.netomi.bat.dexfile.instruction.DexInstruction;
import com.github.netomi.bat.dexfile.util.Primitives;
import com.github.netomi.bat.dexfile.visitor.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DexFilePrinter
implements   DexFileVisitor,
             DexHeaderVisitor,
             ClassDefVisitor,
             ClassDataVisitor,
             EncodedFieldVisitor,
             EncodedMethodVisitor,
             TypeListVisitor,
             TypeVisitor,
             CodeVisitor,
             InstructionVisitor,
             TryVisitor,
             DebugSequenceVisitor
{
    private final BufferedWriter out;

    private int fileOffset;
    private int codeOffset;

    public DexFilePrinter() {
        this(System.out);
    }

    public DexFilePrinter(OutputStream outputStream) {
        out = new BufferedWriter(
              new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), 8192);
    }

    @Override
    public void visitDexFile(DexFile dexFile) {
        dexFile.headerAccept(this);
        dexFile.classDefsAccept(this);

        try {
            out.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void visitHeader(DexFile dexFile, DexHeader header) {
        println("DEX file header:");
        println("magic               : '" + Primitives.toAsciiString(header.magic) + "'");
        println("checksum            : " + Primitives.asHexValue(header.checksum, 8));
        println("signature           : " + formatSignatureByteArray(header.signature));
        println("file_size           : " + header.fileSize);
        println("header_size         : " + header.headerSize);
        println("link_size           : " + header.linkSize);
        println("link_off            : " + formatNumber((long) header.linkOffset));
        println("string_ids_size     : " + header.stringIDsSize);
        println("string_ids_off      : " + formatNumber((long) header.stringIDsOffsets));
        println("type_ids_size       : " + header.typeIDsSize);
        println("type_ids_off        : " + formatNumber((long) header.typeIDsOffset));
        println("proto_ids_size      : " + header.protoIDsSize);
        println("proto_ids_off       : " + formatNumber((long) header.protoIDsOffset));
        println("field_ids_size      : " + header.fieldIDsSize);
        println("field_ids_off       : " + formatNumber((long) header.fieldIDsOffset));
        println("method_ids_size     : " + header.methodIDsSize);
        println("method_ids_off      : " + formatNumber((long) header.methodIDsOffset));
        println("class_defs_size     : " + header.classDefsSize);
        println("class_defs_off      : " + formatNumber((long) header.classDefsOffset));
        println("data_size           : " + header.dataSize);
        println("data_off            : " + formatNumber((long) header.dataOffset));
        println();
    }

    @Override
    public void visitClassDef(DexFile dexFile, int index, ClassDef classDefItem) {
        println("Class #" + index + " header:");
        println("class_idx           : " + classDefItem.classIndex);
        println("access_flags        : " + formatNumber(classDefItem.accessFlags));
        println("superclass_idx      : " + classDefItem.superClassIndex);
        println("interfaces_off      : " + formatNumber(classDefItem.getInterfacesOffset()));
        println("source_file_idx     : " + classDefItem.sourceFileIndex);
        println("annotations_off     : " + formatNumber(classDefItem.getAnnotationsOffset()));
        println("class_data_off      : " + formatNumber(classDefItem.getClassDataOffset()));

        if (classDefItem.classData != null) {
            println("static_fields_size  : " + classDefItem.classData.staticFields.size());
            println("instance_fields_size: " + classDefItem.classData.instanceFields.size());
            println("direct_methods_size : " + classDefItem.classData.directMethods.size());
            println("virtual_methods_size: " + classDefItem.classData.virtualMethods.size());
        } else {
            println("static_fields_size  : ");
            println("instance_fields_size: ");
            println("direct_methods_size : ");
            println("virtual_methods_size: ");
        }
        println();

        println("Class #" + index);
        println("  Class descriptor  : '" + classDefItem.getType(dexFile) + "'");
        println("  Access flags      : " + formatAccessFlags(classDefItem.accessFlags));
        println("  Superclass        : '" + classDefItem.getSuperClassType(dexFile) + "'");
        println("  Interfaces        -");
        classDefItem.interfacesAccept(dexFile, this);

        classDefItem.classDataAccept(dexFile, this);

        println();
        println("  source_file_idx   : " + getSourceFileIndex(dexFile, classDefItem));
        println();
    }

    @Override
    public void visitClassData(DexFile dexFile, ClassDef classDef, ClassData classData) {
        println("  Static fields     -");
        classData.staticFieldsAccept(dexFile, classDef, this);
        println("  Instance fields   -");
        classData.instanceFieldsAccept(dexFile, classDef, this);
        println("  Direct methods    -");
        classData.directMethodsAccept(dexFile, classDef, this);
        println("  Virtual methods   -");
        classData.virtualMethodsAccept(dexFile, classDef, this);
    }

    @Override
    public void visitAnyField(DexFile dexFile, ClassDef classDef, int index, EncodedField encodedField) {
        println(String.format("    #%-14d : (in %s)", index, classDef.getType(dexFile)));
        println("      name          : '" + encodedField.getName(dexFile) + "'");
        println("      type          : '" + encodedField.getType(dexFile) + "'");
        println("      access        : " + formatAccessFlags(encodedField.accessFlags));
    }

    @Override
    public void visitAnyMethod(DexFile dexFile, ClassDef classDef, int index, EncodedMethod encodedMethod) {
        println(String.format("    #%-14d : (in %s)", index, classDef.getType(dexFile)));
        println("      name          : '" + encodedMethod.getName(dexFile) + "'");
        println("      type          : '" + encodedMethod.getDescriptor(dexFile) + "'");
        println("      access        : " + formatAccessFlags(encodedMethod.accessFlags));
        encodedMethod.codeAccept(dexFile, classDef, this);
    }

    @Override
    public void visitCode(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code) {
        println("      code          -");
        println("      registers     : " + code.registersSize);
        println("      ins           : " + code.insSize);
        println("      outs          : " + code.outsSize);
        println("      insns size    : " + code.insnsSize + " 16-bit code units");

        fileOffset = method.getCodeOffset();

        println(Primitives.asHexValue(fileOffset, 6) + ":                                        |[" +
                   Primitives.asHexValue(fileOffset, 6) + "] " +
                   DexUtil.fullExternalMethodSignature(dexFile, classDef, method));

        fileOffset = align(fileOffset, 4);
        fileOffset += 16;

        codeOffset = 0;

        code.instructionsAccept(dexFile, classDef, method, code, this);

        String catchCount = code.tries.size() == 0 ? "(none)" : String.valueOf(code.tries.size());
        println(String.format("      catches       : %s", catchCount));

        code.triesAccept(dexFile, classDef, method, code, this);

        if (code.debugInfo != null) {
            println("      positions     :");
            code.debugInfo.debugSequenceAccept(dexFile, new SourceLinePrinter(code.debugInfo.lineStart));

            println("      locals        :");
            code.debugInfo.debugSequenceAccept(dexFile, new LocalVariablePrinter(code.registersSize));
        }

        println();
    }

    @Override
    public void visitAnyInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction) {
        StringBuilder sb = new StringBuilder();

        sb.append(Primitives.asHexValue(fileOffset, 6));
        sb.append(": ");

        int codeUnitOffset = offset;
        for (int i = 0; i < instruction.getLength() && i < 7; i++) {
            short codeUnit = code.insns[codeUnitOffset++];
            // print code units in little endian format.
            sb.append(Primitives.asHexValue(codeUnit        & 0xff, 2));
            sb.append(Primitives.asHexValue((codeUnit >> 8) & 0xff, 2));
            sb.append(' ');
        }

        if (instruction.getLength() >= 7) {
            sb.append("...");
        }

        for (int i = sb.length(); i < 47; i++) {
            sb.append(' ');
        }

        sb.append('|');
        sb.append(Primitives.asHexValue(codeOffset, 4));
        sb.append(": ");

        sb.append(instruction.toString(dexFile, offset));

        println(sb.toString());

        fileOffset += instruction.getLength() * 2;
        codeOffset += instruction.getLength();
    }

    @Override
    public void visitTry(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int index, Try tryObject) {
        String startAddr = Primitives.toHexString((short) tryObject.startAddr);
        String endAddr   = Primitives.toHexString((short) (tryObject.startAddr + tryObject.insnCount));

        println(String.format("        %s - %s", startAddr, endAddr));

        EncodedCatchHandler catchHandler = tryObject.catchHandler;

        for (TypeAddrPair addrPair : catchHandler.handlers) {
            println(String.format("          %s -> %s", addrPair.getType(dexFile), Primitives.toHexString((short) addrPair.addr)));
        }

        if (catchHandler.catchAllAddr != -1) {
            println(String.format("          %s -> %s", "<any>", Primitives.toHexString((short) catchHandler.catchAllAddr)));
        }
    }

    @Override
    public void visitInterfaces(DexFile dexFile, ClassDef classDefItem, TypeList typeList) {
        if (typeList != null) {
            typeList.typesAccept(dexFile, this);
        }
    }

    @Override
    public void visitType(DexFile dexFile, TypeList typeList, int index, String type) {
        println(String.format("    #%-14d : '%s'", index, type));
    }

    // Private utility methods.

    private void println(String s) {
        try {
            out.write(s);
            out.write('\n');
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void println() {
        try {
            out.write('\n');
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getSourceFileIndex(DexFile dexFile, ClassDef classDefItem) {
        return classDefItem.sourceFileIndex + " (" + classDefItem.getSourceFile(dexFile) + ")";
    }

    private static String formatSignatureByteArray(byte[] array) {
        StringBuilder sb = new StringBuilder();

        int len = array.length;

        sb.append(Primitives.asHexValue(array[0]));
        sb.append(Primitives.asHexValue(array[1]));
        sb.append("...");
        sb.append(Primitives.asHexValue(array[len - 2]));
        sb.append(Primitives.asHexValue(array[len - 1]));

        return sb.toString();
    }

    private static String formatNumber(long number) {
        return String.format("%d (0x%06x)", number, number);
    }

    private static String formatNumber(int number) {
        return String.format("%d (0x%04x)", number, number);
    }

    private static String formatAccessFlags(int accessFlags) {
        return String.format("0x%04x (%s)", accessFlags, DexAccessFlags.formatAsHumanReadable(accessFlags));
    }

    private static int align(int offset, int alignment) {
        if (alignment > 1) {
            int currentAligment = offset % alignment;
            int padding = (alignment - currentAligment) % alignment;
            return offset + padding;
        } else {
            return offset;
        }
    }

    // inner helper classes

    private class SourceLinePrinter
    implements    DebugSequenceVisitor
    {
        private int   lineNumber;
        private short codeOffset;

        SourceLinePrinter(int lineStart) {
            this.lineNumber = lineStart;
        }

        @Override
        public void visitAnyDebugInstruction(DexFile dexFile, DebugInfo debugInfo, DebugInstruction instruction) {}

        @Override
        public void visitAdvanceLine(DexFile dexFile, DebugInfo debugInfo, DebugAdvanceLine instruction) {
            lineNumber += instruction.lineDiff;
        }

        @Override
        public void visitAdvanceLineAndPC(DexFile dexFile, DebugInfo debugInfo, DebugAdvanceLineAndPC instruction) {
            lineNumber += instruction.lineDiff;
            codeOffset += instruction.addrDiff;
            printPosition();
        }

        @Override
        public void visitAdvancePC(DexFile dexFile, DebugInfo debugInfo, DebugAdvancePC instruction) {
            codeOffset += instruction.addrDiff;
        }

        private void printPosition() {
            println(String.format("        %s line=%d", Primitives.toHexString(codeOffset), lineNumber));
        }
    }

    private class LocalVariablePrinter
    implements    DebugSequenceVisitor
    {
        private final LocalVariableInfo[] variableInfos;

        private short codeOffset;

        LocalVariablePrinter(int numRegisters) {
            this.variableInfos = new LocalVariableInfo[numRegisters];
        }

        @Override
        public void visitAnyDebugInstruction(DexFile dexFile, DebugInfo debugInfo, DebugInstruction instruction) {}

        @Override
        public void visitAdvanceLineAndPC(DexFile dexFile, DebugInfo debugInfo, DebugAdvanceLineAndPC instruction) {
            codeOffset += instruction.addrDiff;
        }

        @Override
        public void visitAdvancePC(DexFile dexFile, DebugInfo debugInfo, DebugAdvancePC instruction) {
            codeOffset += instruction.addrDiff;
        }

        @Override
        public void visitStartLocal(DexFile dexFile, DebugInfo debugInfo, DebugStartLocal instruction) {
            String name = dexFile.getString(instruction.nameIndex);
            String type = dexFile.getType(instruction.typeIndex);

            LocalVariableInfo variableInfo = new LocalVariableInfo(name, type, null);
            variableInfo.startAddr = codeOffset;

            variableInfos[instruction.registerNum] = variableInfo;
        }

        @Override
        public void visitStartLocalExtended(DexFile dexFile, DebugInfo debugInfo, DebugStartLocalExtended instruction) {
            String name = dexFile.getString(instruction.nameIndex);
            String type = dexFile.getType(instruction.typeIndex);
            String sig  = dexFile.getString(instruction.sigIndex);

            LocalVariableInfo variableInfo = new LocalVariableInfo(name, type, sig);
            variableInfo.startAddr = codeOffset;

            variableInfos[instruction.registerNum] = variableInfo;
        }

        @Override
        public void visitEndLocal(DexFile dexFile, DebugInfo debugInfo, DebugEndLocal instruction) {
            LocalVariableInfo variableInfo = variableInfos[instruction.registerNum];
            variableInfo.endAddr = codeOffset;

            printLocal(instruction.registerNum, variableInfo);
        }

        @Override
        public void visitRestartLocal(DexFile dexFile, DebugInfo debugInfo, DebugRestartLocal instruction) {
        }

        private void printLocal(int registerNum, LocalVariableInfo variableInfo) {
            println(String.format("        %s - %s reg=%d %s %s",
                    Primitives.toHexString((short) variableInfo.startAddr),
                    Primitives.toHexString((short) variableInfo.endAddr),
                    registerNum,
                    variableInfo.name,
                    variableInfo.type));
        }
    }

    private static class LocalVariableInfo {
        private final String name;
        private final String type;
        private final String signature;

        private int startAddr;
        private int endAddr;

        LocalVariableInfo(String name, String type, String signature) {
            this.name      = name;
            this.type      = type;
            this.signature = signature;
        }
    }
}

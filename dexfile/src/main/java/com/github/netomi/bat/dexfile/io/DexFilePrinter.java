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

import java.io.PrintStream;

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
    private final PrintStream ps;

    private int fileOffset;
    private int codeOffset;

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

        if (classDefItem.classData != null) {
            ps.println("static_fields_size  : " + classDefItem.classData.staticFields.size());
            ps.println("instance_fields_size: " + classDefItem.classData.instanceFields.size());
            ps.println("direct_methods_size : " + classDefItem.classData.directMethods.size());
            ps.println("virtual_methods_size: " + classDefItem.classData.virtualMethods.size());
        } else {
            ps.println("static_fields_size  : ");
            ps.println("instance_fields_size: ");
            ps.println("direct_methods_size : ");
            ps.println("virtual_methods_size: ");
        }
        ps.println();

        ps.println("Class #" + index);
        ps.println("  Class descriptor  : '" + classDefItem.getType(dexFile) + "'");
        ps.println("  Access flags      : " + formatAccessFlags(classDefItem.accessFlags));
        ps.println("  Superclass        : '" + classDefItem.getSuperClassType(dexFile) + "'");
        ps.println("  Interfaces        -");
        classDefItem.interfacesAccept(dexFile, this);

        classDefItem.classDataAccept(dexFile, this);

        ps.println();
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
    public void visitAnyField(DexFile dexFile, ClassDef classDef, int index, EncodedField encodedField) {
        ps.println(String.format("    #%-14d : (in %s)", index, classDef.getType(dexFile)));
        ps.println("      name          : '" + encodedField.getName(dexFile) + "'");
        ps.println("      type          : '" + encodedField.getType(dexFile) + "'");
        ps.println("      access        : " + formatAccessFlags(encodedField.accessFlags));
    }

    @Override
    public void visitAnyMethod(DexFile dexFile, ClassDef classDef, int index, EncodedMethod encodedMethod) {
        ps.println(String.format("    #%-14d : (in %s)", index, classDef.getType(dexFile)));
        ps.println("      name          : '" + encodedMethod.getName(dexFile) + "'");
        ps.println("      type          : '" + encodedMethod.getDescriptor(dexFile) + "'");
        ps.println("      access        : " + formatAccessFlags(encodedMethod.accessFlags));
        encodedMethod.codeAccept(dexFile, classDef, this);
    }

    @Override
    public void visitCode(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code) {
        ps.println("      code          -");
        ps.println("      registers     : " + code.registersSize);
        ps.println("      ins           : " + code.insSize);
        ps.println("      outs          : " + code.outsSize);
        ps.println("      insns size    : " + code.insnsSize + " 16-bit code units");

        fileOffset = method.getCodeOffset();

        ps.println(Primitives.asHexValue(fileOffset, 6) + ":                                        |[" +
                   Primitives.asHexValue(fileOffset, 6) + "] " +
                   DexUtil.fullExternalMethodSignature(dexFile, classDef, method));

        fileOffset = align(fileOffset, 4);
        fileOffset += 16;

        codeOffset = 0;

        code.instructionsAccept(dexFile, classDef, method, code, this);

        ps.println(String.format("      catches       : %d", code.tries.size()));

        code.triesAccept(dexFile, classDef, method, code, this);

        if (code.debugInfo != null) {
            ps.println("      positions     :");
            code.debugInfo.debugSequenceAccept(dexFile, new SourceLinePrinter(ps, code.debugInfo.lineStart));

            ps.println("      locals        :");
            code.debugInfo.debugSequenceAccept(dexFile, new LocalVariablePrinter(ps, code.registersSize));
        }

        ps.println();
    }

    @Override
    public void visitInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction) {
        StringBuilder sb = new StringBuilder();

        sb.append(Primitives.asHexValue(fileOffset, 6));
        sb.append(": ");

        for (int i = 0; i < instruction.getLength() && i < 7; i++) {
            sb.append(Primitives.asHexValue(code.insns[offset++], 4));
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

        sb.append(instruction.toString(dexFile));

        ps.println(sb.toString());

        codeOffset += instruction.getLength();
    }

    @Override
    public void visitTry(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int index, Try tryObject) {
        String startAddr = Primitives.toHexString((short) tryObject.startAddr);
        String endAddr   = Primitives.toHexString((short) (tryObject.startAddr + tryObject.insnCount));

        ps.println(String.format("        %s - %s", startAddr, endAddr));

        EncodedCatchHandler catchHandler = tryObject.catchHandler;

        for (TypeAddrPair addrPair : catchHandler.handlers) {
            ps.println(String.format("          %s -> %s", addrPair.getType(dexFile), Primitives.toHexString((short) addrPair.addr)));
        }

        if (catchHandler.catchAllAddr != -1) {
            ps.println(String.format("          %s -> %s", "<any>", Primitives.toHexString((short) catchHandler.catchAllAddr)));
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
        ps.println(String.format("    #%-14d : '%s'", index, type));
    }

    // Private utility methods.

    private static String getSourceFileIndex(DexFile dexFile, ClassDef classDefItem) {
        return classDefItem.sourceFileIndex + " (" + classDefItem.getSourceFile(dexFile) + ")";
    }

    private static String formatNumber(long number) {
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

    private static class SourceLinePrinter
    implements           DebugSequenceVisitor {

        private final PrintStream ps;

        private int   lineNumber;
        private short codeOffset;

        SourceLinePrinter(PrintStream printStream, int lineStart) {
            this.ps         = printStream;
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
            ps.println(String.format("        %s line=%d", Primitives.toHexString(codeOffset), lineNumber));
        }
    }

    private static class LocalVariablePrinter
    implements           DebugSequenceVisitor {

        private final PrintStream         ps;
        private final LocalVariableInfo[] variableInfos;

        private short codeOffset;

        LocalVariablePrinter(PrintStream printStream, int numRegisters) {
            this.ps            = printStream;
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
            ps.println(String.format("        %s - %s reg=%d %s %s",
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

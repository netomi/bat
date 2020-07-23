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
package com.github.netomi.bat.dump;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.annotation.*;
import com.github.netomi.bat.dexfile.debug.*;
import com.github.netomi.bat.dexfile.instruction.DexInstruction;
import com.github.netomi.bat.dexfile.util.Mutf8;
import com.github.netomi.bat.dexfile.util.Primitives;
import com.github.netomi.bat.dexfile.value.*;
import com.github.netomi.bat.dexfile.visitor.*;

import java.io.OutputStream;
import java.util.List;

public class DexDumpPrinter
implements   DexFileVisitor,
             ClassDefVisitor
{
    private final BufferedPrinter printer;
    private final boolean         printFileSummary;
    private final boolean         printHeaders;
    private final boolean         printAnnotations;

    private final VisitorImpl     visitorImpl;

    private int fileOffset;
    private int codeOffset;

    public DexDumpPrinter() {
        this(System.out);
    }

    public DexDumpPrinter(OutputStream outputStream) {
        this(outputStream, true, true, true);
    }

    public DexDumpPrinter(OutputStream outputStream,
                          boolean printFileSummary,
                          boolean printHeaders,
                          boolean printAnnotations) {
        printer = new BufferedPrinter(outputStream);

        this.printFileSummary = printFileSummary;
        this.printHeaders     = printHeaders;
        this.printAnnotations = printAnnotations;

        visitorImpl = new VisitorImpl();
    }

    @Override
    public void visitDexFile(DexFile dexFile) {
        if (printFileSummary) {
            dexFile.headerAccept(visitorImpl);
        }

        dexFile.classDefsAccept(this);

        try {
            printer.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void visitClassDef(DexFile dexFile, int index, ClassDef classDef) {
        if (printHeaders) {
            println("Class #" + index + " header:");
            println("class_idx           : " + classDef.classIndex);
            println("access_flags        : " + formatNumber(classDef.accessFlags));
            println("superclass_idx      : " + classDef.superClassIndex);
            println("interfaces_off      : " + formatNumber((long) classDef.getInterfacesOffset()));
            println("source_file_idx     : " + classDef.sourceFileIndex);
            println("annotations_off     : " + formatNumber((long) classDef.getAnnotationsOffset()));
            println("class_data_off      : " + formatNumber((long) classDef.getClassDataOffset()));

            if (classDef.classData != null) {
                println("static_fields_size  : " + classDef.classData.staticFields.size());
                println("instance_fields_size: " + classDef.classData.instanceFields.size());
                println("direct_methods_size : " + classDef.classData.directMethods.size());
                println("virtual_methods_size: " + classDef.classData.virtualMethods.size());
            }
            else {
                println("static_fields_size  : 0");
                println("instance_fields_size: 0");
                println("direct_methods_size : 0");
                println("virtual_methods_size: 0");
            }
            println();
        }

        if (printAnnotations && classDef.annotationsDirectory != null) {
            println(String.format("Class #%d annotations:", index));
            classDef.annotationSetsAccept(dexFile, visitorImpl);

            println();
        }

        println(String.format("Class #%-5d        -", index));
        println("  Class descriptor  : '" + classDef.getType(dexFile) + "'");
        println("  Access flags      : " + formatAccessFlags(classDef.accessFlags, DexAccessFlags.Target.CLASS));
        println("  Superclass        : '" + classDef.getSuperClassType(dexFile) + "'");
        println("  Interfaces        -");
        classDef.interfacesAccept(dexFile, visitorImpl);

        if (classDef.classData != null) {
            classDef.classDataAccept(dexFile, visitorImpl);
        } else {
            println("  Static fields     -");
            println("  Instance fields   -");
            println("  Direct methods    -");
            println("  Virtual methods   -");
        }

        println("  source_file_idx   : " + getSourceFileIndex(dexFile, classDef));
        println();
    }

    // Private utility methods.

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

    private static String formatAccessFlags(int accessFlags, int target) {
        return String.format("0x%04x (%s)", accessFlags, DexAccessFlags.formatAsHumanReadable(accessFlags, target));
    }

    private static int align(int offset, int alignment) {
        if (alignment > 1) {
            int currentAlignment = offset % alignment;
            int padding = (alignment - currentAlignment) % alignment;
            return offset + padding;
        } else {
            return offset;
        }
    }

    private void print(String value) {
        printer.print(value);
    }

    private void println(String value) {
        printer.println(value);
    }

    private void println() {
        printer.println();
    }

    // inner helper classes

    private class VisitorImpl
    implements    DexHeaderVisitor,
                  ClassDataVisitor,
                  EncodedFieldVisitor,
                  EncodedMethodVisitor,
                  TypeListVisitor,
                  TypeVisitor,
                  CodeVisitor,
                  InstructionVisitor,
                  TryVisitor,
                  DebugSequenceVisitor,
                  AnnotationSetVisitor,
                  AnnotationVisitor,
                  EncodedValueVisitor
    {
        private final InstructionVisitor instructionPrinter = new InstructionPrinter(printer);

        private final PrintfFormat FLOATING_FORMAT = new PrintfFormat("%g");

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
            println("      access        : " + formatAccessFlags(encodedField.accessFlags, DexAccessFlags.Target.FIELD));

            if (encodedField.isStatic()       &&
                classDef.staticValues != null &&
                index < classDef.staticValues.encodedArrayValue.getValueCount())
            {
                print("      value         : ");
                classDef.staticValues.encodedArrayValue.valueAccept(dexFile, index, this);
                println();
            }
        }

        @Override
        public void visitAnyMethod(DexFile dexFile, ClassDef classDef, int index, EncodedMethod encodedMethod) {
            println(String.format("    #%-14d : (in %s)", index, classDef.getType(dexFile)));
            println("      name          : '" + encodedMethod.getName(dexFile) + "'");
            println("      type          : '" + encodedMethod.getDescriptor(dexFile) + "'");
            println("      access        : " + formatAccessFlags(encodedMethod.accessFlags, DexAccessFlags.Target.METHOD));

            if (encodedMethod.code != null) {
                encodedMethod.codeAccept(dexFile, classDef, this);
            }
            else {
                println("      code          : (none)");
            }

            println();
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

            println("      positions     :");
            if (code.debugInfo != null) {
                code.debugInfo.debugSequenceAccept(dexFile, new SourceLinePrinter(code.debugInfo.lineStart));
            }

            println("      locals        :");
            if (code.debugInfo != null) {
                LocalVariablePrinter localVariablePrinter =
                        new LocalVariablePrinter(dexFile, method, code);

                code.debugInfo.debugSequenceAccept(dexFile, localVariablePrinter);

                localVariablePrinter.finish();
            }
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
                sb.append(Primitives.asHexValue(codeUnit & 0xff, 2));
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

            print(sb.toString());
            instruction.accept(dexFile, classDef, method, code, offset, instructionPrinter);
            println();

            fileOffset += instruction.getLength() * 2;
            codeOffset += instruction.getLength();
        }

        @Override
        public void visitTry(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int index, Try tryObject) {
            String startAddr = Primitives.toHexString((short) tryObject.startAddr);
            String endAddr = Primitives.toHexString((short) (tryObject.startAddr + tryObject.insnCount));

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

        @Override
        public void visitClassAnnotationSet(DexFile dexFile, ClassDef classDef, AnnotationSet annotationSet) {
            println("Annotations on class");
            annotationSet.accept(dexFile, classDef, this);
        }

        @Override
        public void visitFieldAnnotationSet(DexFile dexFile, ClassDef classDef, FieldAnnotation fieldAnnotation, AnnotationSet annotationSet) {
            FieldID fieldID = fieldAnnotation.getFieldID(dexFile);
            println("Annotations on field #" + fieldAnnotation.fieldIndex + " '" + fieldID.getName(dexFile) + "'");
            annotationSet.accept(dexFile, classDef, this);
        }

        @Override
        public void visitMethodAnnotationSet(DexFile dexFile, ClassDef classDef, MethodAnnotation methodAnnotation, AnnotationSet annotationSet) {
            MethodID methodID = methodAnnotation.getMethodID(dexFile);
            println("Annotations on method #" + methodAnnotation.methodIndex + " '" + methodID.getName(dexFile) + "'");
            annotationSet.accept(dexFile, classDef, this);
        }

        @Override
        public void visitParameterAnnotationSet(DexFile dexFile, ClassDef classDef, ParameterAnnotation parameterAnnotation, AnnotationSetRefList annotationSetRefList) {
            MethodID methodID = parameterAnnotation.getMethodID(dexFile);
            println("Annotations on method #" + parameterAnnotation.methodIndex + " '" + methodID.getName(dexFile) + "' parameters");

            List<AnnotationSetRef> list = annotationSetRefList.annotationSetRefs;
            for (int i = 0; i < list.size(); i++) {
                println("#" + i);
                AnnotationSetRef annotationSetRef = list.get(i);
                annotationSetRef.annotationSet.accept(dexFile, classDef, this);

                if (annotationSetRef.annotationSet.getAnnotationCount() == 0) {
                    println("   empty-annotation-set");
                }
            }
        }

        @Override
        public void visitAnnotation(DexFile dexFile, ClassDef classDef, AnnotationSet annotationSet, int index, Annotation annotation) {
            print("  " + annotation.getVisibility() + " ");

            EncodedAnnotationValue annotationValue = annotation.getAnnotationValue();
            annotationValue.accept(dexFile, this);

            println();
        }

        @Override
        public void visitAnyValue(DexFile dexFile, EncodedValue value) {
            print(value.toString());
        }

        @Override
        public void visitArrayValue(DexFile dexFile, EncodedArrayValue value) {
            print("{ ");
            value.valuesAccept(dexFile, this, (df, v) -> print(" "));
            print("}");
        }

        @Override
        public void visitEnumValue(DexFile dexFile, EncodedEnumValue value) {
            print(value.getEnumField(dexFile).getName(dexFile));
        }

        @Override
        public void visitMethodValue(DexFile dexFile, EncodedMethodValue value) {
            print(value.getMethod(dexFile).getName(dexFile));
        }

        @Override
        public void visitFieldValue(DexFile dexFile, EncodedFieldValue value) {
            print(value.getField(dexFile).getName(dexFile));
        }

        @Override
        public void visitStringValue(DexFile dexFile, EncodedStringValue value) {
            printer.printAsMutf8(String.format("\"%s\"", value.getString(dexFile)), true);
        }

        @Override
        public void visitCharValue(DexFile dexFile, EncodedCharValue value) {
            print(Integer.toString(value.getValue()));
        }

        @Override
        public void visitByteValue(DexFile dexFile, EncodedByteValue value) {
            print(Integer.toString(value.getValue()));
        }

        @Override
        public void visitShortValue(DexFile dexFile, EncodedShortValue value) {
            print(Short.toString(value.getValue()));
        }

        @Override
        public void visitBooleanValue(DexFile dexFile, EncodedBooleanValue value) {
            print(Boolean.toString(value.getValue()));
        }

        @Override
        public void visitIntValue(DexFile dexFile, EncodedIntValue value) {
            print(Integer.toString(value.getValue()));
        }

        @Override
        public void visitLongValue(DexFile dexFile, EncodedLongValue value) {
            print(Long.toString(value.getValue()));
        }

        @Override
        public void visitDoubleValue(DexFile dexFile, EncodedDoubleValue value) {
            print(FLOATING_FORMAT.sprintf(value.getValue()));
        }

        @Override
        public void visitFloatValue(DexFile dexFile, EncodedFloatValue value) {
            print(FLOATING_FORMAT.sprintf(value.getValue()));
        }

        @Override
        public void visitTypeValue(DexFile dexFile, EncodedTypeValue value) {
            print(value.getType(dexFile));
        }

        @Override
        public void visitAnnotationValue(DexFile dexFile, EncodedAnnotationValue value) {
            print(value.getType(dexFile));

            for (int i = 0; i < value.getAnnotationElementCount(); i++) {
                AnnotationElement element = value.getAnnotationElement(i);

                print(" " + element.getName(dexFile) + "=");
                element.value.accept(dexFile, this);
            }
        }

        @Override
        public void visitNullValue(DexFile dexFile, EncodedNullValue value) {
            print("null");
        }
    }

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
        private final int                 codeSize;
        private final LocalVariableInfo[] variableInfos;

        private short codeOffset;

        LocalVariablePrinter(DexFile dexFile, EncodedMethod method, Code code) {
            this.codeSize      = code.insnsSize;
            this.variableInfos = new LocalVariableInfo[code.registersSize];

            // initialize the local variable info with the method parameters.

            int register = code.registersSize - code.insSize;

            if (!method.isStatic()) {
                String classType = method.getClassType(dexFile);
                variableInfos[register++] = new LocalVariableInfo("this", classType, null);
            }

            DebugInfo debugInfo  = code.debugInfo;
            ProtoID   protoID    = method.getProtoID(dexFile);
            TypeList  parameters = protoID.parameters;

            if (debugInfo != null && parameters != null) {
                for (int i = 0; i < debugInfo.getParameterCount() && register < code.registersSize; i++, register++) {
                    String parameterName = debugInfo.getParameterName(dexFile, i);
                    String parameterType = parameters.getType(dexFile, i);

                    variableInfos[register] = new LocalVariableInfo(parameterName, parameterType, null);

                    // TODO: extract into util class.
                    if (parameterType.equals("J") || parameterType.equals("D")) {
                        register++;
                    }
                }
            }
        }

        public void finish() {
            for (int i = 0; i < variableInfos.length; i++) {
                LocalVariableInfo info = variableInfos[i];

                if (info != null && info.endAddr == -1) {
                    info.endAddr = codeSize;
                    printLocal(i, info);
                }
            }
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
            LocalVariableInfo variableInfo = variableInfos[instruction.registerNum];
            // only for compatibility with dexdump:
            // print the method parameters potentially twice
            if (variableInfo != null &&
                variableInfo.endAddr == -1)
            {
                variableInfo.endAddr = codeOffset;
                printLocal(instruction.registerNum, variableInfo);
            }

            String name = dexFile.getString(instruction.nameIndex);
            String type = dexFile.getType(instruction.typeIndex);

            variableInfo = new LocalVariableInfo(name, type, null);
            variableInfo.startAddr = codeOffset;

            variableInfos[instruction.registerNum] = variableInfo;
        }

        @Override
        public void visitStartLocalExtended(DexFile dexFile, DebugInfo debugInfo, DebugStartLocalExtended instruction) {
            LocalVariableInfo variableInfo = variableInfos[instruction.registerNum];
            // only for compatibility with dexdump:
            // print the method parameters potentially twice
            if (variableInfo != null &&
                variableInfo.endAddr == -1)
            {
                variableInfo.endAddr = codeOffset;
                printLocal(instruction.registerNum, variableInfo);
            }

            String name = dexFile.getString(instruction.nameIndex);
            String type = dexFile.getType(instruction.typeIndex);
            String sig  = dexFile.getString(instruction.sigIndex);

            variableInfo = new LocalVariableInfo(name, type, sig);
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
            LocalVariableInfo variableInfo = variableInfos[instruction.registerNum];
            variableInfo.startAddr = codeOffset;
            variableInfo.endAddr   = -1;
        }

        private void printLocal(int registerNum, LocalVariableInfo variableInfo) {
            println(variableInfo.toString(registerNum));
        }
    }

    private static class LocalVariableInfo {
        private final String name;
        private final String type;
        private final String signature;

        private int startAddr =  0;
        private int endAddr   = -1;

        LocalVariableInfo(String name, String type, String signature) {
            this.name      = name;
            this.type      = type;
            this.signature = signature;
        }

        String toString(int registerNum) {
            StringBuilder sb = new StringBuilder();

            sb.append("        ");
            sb.append(Primitives.toHexString((short) startAddr));
            sb.append(" - ");
            sb.append(Primitives.toHexString((short) endAddr));
            sb.append(" reg=");
            sb.append(registerNum);
            sb.append(' ');
            sb.append(name == null ? "(null)" : name);
            sb.append(' ');
            sb.append(type);

            if (signature != null) {
                sb.append(' ');
                sb.append(signature);
            }

            return sb.toString();
        }
    }
}

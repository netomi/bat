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
package com.github.netomi.bat.smali.disassemble;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.annotation.*;
import com.github.netomi.bat.dexfile.util.DexClasses;
import com.github.netomi.bat.dexfile.value.AnnotationElement;
import com.github.netomi.bat.dexfile.value.EncodedAnnotationValue;
import com.github.netomi.bat.dexfile.visitor.*;
import com.github.netomi.bat.io.IndentingPrinter;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmaliPrinter
implements   ClassDefVisitor,
             ClassDataVisitor,
             AnnotationSetVisitor,
             AnnotationVisitor,
             AnnotationElementVisitor,
             EncodedFieldVisitor,
             EncodedMethodVisitor,
             CodeVisitor
{
    private final IndentingPrinter printer;

    private boolean printParameterInfo;
    private int     currentParameterIndex;
    private int     currentRegisterIndex;
    private String  currentParameterType;

    public SmaliPrinter() {
        this(new OutputStreamWriter(System.out));
    }

    public SmaliPrinter(Writer writer) {
        this.printer = new IndentingPrinter(writer, 4);
    }

    @Override
    public void visitClassDef(DexFile dexFile, int index, ClassDef classDef) {
        printer.print(".class");

        String accessFlags =
            DexAccessFlags.formatAsHumanReadable(classDef.getAccessFlags(), DexAccessFlags.Target.CLASS)
                          .toLowerCase();

        if (!accessFlags.isEmpty()) {
            printer.print(" " + accessFlags);
        }

        printer.print(" " + DexClasses.internalTypeFromClassName(classDef.getClassName(dexFile)));
        printer.println();

        printer.println(".super " + classDef.getSuperClassType(dexFile));

        String sourceFile = classDef.getSourceFile(dexFile);
        if (sourceFile != null) {
            printer.println(".source \"" + sourceFile + "\"");
        }

        classDef.interfaceListAccept(dexFile, (dexFile1, typeList) -> {
            printer.println();
            printer.println("# interfaces");
            classDef.interfacesAccept(dexFile1, (df, tl, idx, type) -> printer.println(".implements " + type));
        });

        classDef.annotationsDirectoryAccept(dexFile, new ClassAnnotationSetVisitor(this));

        classDef.classDataAccept(dexFile, this);

        printer.flush();
    }

    @Override
    public void visitClassData(DexFile dexFile, ClassDef classDef, ClassData classData) {
        if (classData.getStaticFieldCount() > 0) {
            printer.println();
            printer.println();
            printer.println("# static fields");
            classData.staticFieldsAccept(dexFile, classDef, this.joinedByFieldConsumer((df, field) -> printer.println()));
        }

        if (classData.getInstanceFieldCount() > 0) {
            printer.println();
            printer.println();
            printer.println("# instance fields");
            classData.instanceFieldsAccept(dexFile, classDef, this.joinedByFieldConsumer((df, field) -> printer.println()));
        }

        if (classData.getDirectMethodCount() > 0) {
            printer.println();
            printer.println();
            printer.println("# direct methods");
            classData.directMethodsAccept(dexFile, classDef, this.joinedByMethodConsumer((df, method) -> printer.println()));
        }

        if (classData.getVirtualMethodCount() > 0) {
            printer.println();
            printer.println();
            printer.println("# virtual methods");
            classData.virtualMethodsAccept(dexFile, classDef, this.joinedByMethodConsumer((df, method) -> printer.println()));
        }
    }

    @Override
    public void visitAnyField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
        printer.print(".field");

        String accessFlags =
            DexAccessFlags.formatAsHumanReadable(field.getAccessFlags(), DexAccessFlags.Target.FIELD)
                          .toLowerCase();

        if (!accessFlags.isEmpty()) {
            printer.print(" " + accessFlags);
        }

        printer.print(" " + field.getName(dexFile) + ":" + field.getType(dexFile));

        if (field.isStatic()) {
            InitializationDetector detector = new InitializationDetector(field.getName(dexFile), field.getType(dexFile));
            classDef.methodAccept(dexFile, "<clinit>", new AllCodeVisitor(new AllInstructionsVisitor(detector)));

            if (!detector.fieldIsSetInStaticInitializer() || !field.getModifiers().contains(FieldModifier.FINAL)) {
                field.staticValueAccept(dexFile, classDef, index, new EncodedValuePrinter(printer, null, " = "));
            }
        }

        printer.println();
        classDef.annotationsDirectoryAccept(dexFile, new FieldAnnotationSetFilter(field, this));
    }

    @Override
    public void visitAnyMethod(DexFile dexFile, ClassDef classDef, int index, EncodedMethod method) {
        printer.print(".method");

        String accessFlags =
            DexAccessFlags.formatAsHumanReadable(method.getAccessFlags(), DexAccessFlags.Target.METHOD)
                          .replaceAll("_", "-")
                          .toLowerCase();

        if (!accessFlags.isEmpty()) {
            printer.print(" " + accessFlags);
        }

        printer.println(" " + method.getName(dexFile) + method.getDescriptor(dexFile));

        // print code.
        printer.levelUp();
        if (method.code != null) {
            method.codeAccept(dexFile, classDef, this);
        } else if (classDef.getAnnotationsDirectory() != null) {
            int     parameterIndex = 0;
            int     registerIndex  = method.isStatic() ? 0 : 1;

            ProtoID protoID        = method.getProtoID(dexFile);
            TypeList parameters    = protoID.getParameters();

            for (String parameterType : parameters.getTypes(dexFile)) {
                printParameterInfo    = true;
                currentParameterIndex = parameterIndex++;
                currentParameterType  = parameterType;
                currentRegisterIndex  = registerIndex;

                classDef.annotationsDirectoryAccept(dexFile, new ParameterAnnotationSetFilter(method, this));

                registerIndex++;

                // TODO: extract into util class.
                if (parameterType.equals("J") || parameterType.equals("D")) {
                    registerIndex++;
                }
            }

            printParameterInfo = false;
            classDef.annotationsDirectoryAccept(dexFile, new MethodAnnotationSetFilter(method, this));
        }

        printer.levelDown();
        printer.println(".end method");
    }

    @Override
    public void visitCode(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code) {
        printer.println(".registers " + code.registersSize);

        LocalVariableCollector.LocalVariableInfo[] localVariableInfos =
                new LocalVariableCollector.LocalVariableInfo[code.registersSize];

        if (code.debugInfo != null) {
            int localVariables = code.registersSize - code.insSize;

            int parameterIndex = 0;
            int registerIndex = method.isStatic() ? 0 : 1;

            if (!method.isStatic()) {
                String classType = method.getClassType(dexFile);
                localVariableInfos[localVariables] = new LocalVariableCollector.LocalVariableInfo("this", classType, null);
            }

            ProtoID protoID     = method.getProtoID(dexFile);
            TypeList parameters = protoID.getParameters();
            for (String parameterType : parameters.getTypes(dexFile)) {
                currentParameterIndex = parameterIndex;

                String parameterName = code.debugInfo.getParameterName(dexFile, parameterIndex++);
                if (parameterName != null) {
                    printer.println(String.format(".param p%d, \"%s\"    # %s",
                                                  registerIndex,
                                                  parameterName,
                                                  parameterType));
                } else {
                    currentRegisterIndex = registerIndex;
                    currentParameterType = parameterType;
                    printParameterInfo   = true;
                }

                LocalVariableCollector.LocalVariableInfo localVariableInfo =
                    new LocalVariableCollector.LocalVariableInfo(parameterName, parameterType, null);

                localVariableInfos[localVariables + registerIndex] = localVariableInfo;

                classDef.annotationsDirectoryAccept(dexFile, new ParameterAnnotationSetFilter(method, this));

                printParameterInfo = false;

                registerIndex++;

                // TODO: extract into util class.
                if (parameterType.equals("J") || parameterType.equals("D")) {
                    registerIndex++;
                }
            }
        }

        classDef.annotationsDirectoryAccept(dexFile, new MethodAnnotationSetFilter(method, this));

        RegisterPrinter            registerPrinter     = new RegisterPrinter(code);
        BranchTargetPrinter        branchTargetPrinter = new BranchTargetPrinter();
        Map<Integer, List<String>> debugState          = new HashMap<>();

        if (code.debugInfo != null) {
            code.debugInfo.debugSequenceAccept(dexFile,
                new SourceLineCollector(debugState, code.debugInfo.getLineStart()));

            code.debugInfo.debugSequenceAccept(dexFile,
                new LocalVariableCollector(debugState, localVariableInfos, registerPrinter));
        }

        // collect branch target / label infos.
        code.instructionsAccept(dexFile, classDef, method, code, branchTargetPrinter);

        // print the instructions.
        code.instructionsAccept(dexFile, classDef, method, code,
                                new InstructionPrinter(printer, registerPrinter, branchTargetPrinter, debugState));
    }

    @Override
    public void visitAnyAnnotationSet(DexFile dexFile, ClassDef classDef, AnnotationSet annotationSet) {}

    @Override
    public void visitClassAnnotationSet(DexFile dexFile, ClassDef classDef, AnnotationSet annotationSet) {
        int annotationCount = annotationSet.getAnnotationCount();
        if (annotationCount > 0) {
            printer.println();
            printer.println();
            printer.println("# annotations");
            annotationSet.accept(dexFile, classDef, this.joinedByAnnotationConsumer((df, ann) -> printer.println()));
        }
    }

    @Override
    public void visitFieldAnnotationSet(DexFile dexFile, ClassDef classDef, FieldAnnotation fieldAnnotation, AnnotationSet annotationSet) {
        int annotationCount = annotationSet.getAnnotationCount();
        if (annotationCount > 0) {
            printer.levelUp();
            annotationSet.accept(dexFile, classDef, this.joinedByAnnotationConsumer((df, ann) -> printer.println()));
            printer.levelDown();
            printer.println(".end field");
        }
    }

    @Override
    public void visitMethodAnnotationSet(DexFile dexFile, ClassDef classDef, MethodAnnotation methodAnnotation, AnnotationSet annotationSet) {
        annotationSet.accept(dexFile, classDef, this.joinedByAnnotationConsumer((df, ann) -> printer.println()));
    }

    @Override
    public void visitParameterAnnotationSet(DexFile dexFile, ClassDef classDef, ParameterAnnotation parameterAnnotation, AnnotationSetRefList annotationSetRefList) {
        if (currentParameterIndex < annotationSetRefList.getAnnotationSetRefCount()) {
            AnnotationSetRef annotationSetRef = annotationSetRefList.getAnnotationSetRef(currentParameterIndex);
            if (annotationSetRef != null) {
                AnnotationSet annotationSet = annotationSetRef.getAnnotationSet();
                if (annotationSet != null && annotationSet.getAnnotationCount() > 0) {
                    if (printParameterInfo) {
                        printer.println(String.format(".param p%d    # %s",
                                currentRegisterIndex,
                                currentParameterType));
                    }
                    printer.levelUp();
                    annotationSet.accept(dexFile, classDef, this);
                    printer.levelDown();
                    printer.println(".end param");
                }
            }
        }
    }

    @Override
    public void visitAnnotation(DexFile dexFile, ClassDef classDef, AnnotationSet annotationSet, int index, Annotation annotation) {
        printer.print(".annotation " + annotation.getVisibility().getSimpleName() + " ");
        EncodedAnnotationValue annotationValue = annotation.getAnnotationValue();
        printer.println(annotationValue.getType(dexFile));
        printer.levelUp();
        annotationValue.annotationElementsAccept(dexFile, this);
        printer.levelDown();
        printer.println(".end annotation");
    }

    @Override
    public void visitAnnotationElement(DexFile dexFile, AnnotationElement element) {
        printer.print(element.getName(dexFile));
        printer.print(" = ");
        element.getValue().accept(dexFile, new EncodedValuePrinter(printer, this));
        printer.println();
    }
}

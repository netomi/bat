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

package com.github.netomi.bat.smali.assemble;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.smali.parser.SmaliBaseVisitor;
import com.github.netomi.bat.smali.parser.SmaliParser;
import org.antlr.v4.runtime.tree.ParseTree;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

public class ClassDefAssembler
extends      SmaliBaseVisitor<ClassDef>
{
    private final DexFile dexFile;

    private String classType   = null;
    private int    accessFlags = 0;
    private String superType   = null;
    private String sourceFile  = null;

    private ClassDef classDef = null;

    public ClassDefAssembler(DexFile dexFile) {
        this.dexFile = dexFile;
    }

    @Override
    public ClassDef visitSmaliclass(SmaliParser.SmaliclassContext ctx) {
        ctx.classline().accept(this);
        ctx.superclassline().accept(this);

        SmaliParser.SourcelineContext sourcelineContext = ctx.sourceline();
        if (sourcelineContext != null) {
            sourcelineContext.accept(this);
        }

        int classTypeIndex = dexFile.addOrGetTypeIDIndex(classType);
        int superTypeIndex = dexFile.addOrGetTypeIDIndex(superType);
        int sourceFileIndex = sourceFile != null ? dexFile.addOrGetStringIDIndex(sourceFile) : NO_INDEX;

        classDef = ClassDef.of(classTypeIndex,
                               accessFlags,
                               superTypeIndex,
                               sourceFileIndex);

        for (SmaliParser.InterfacelineContext interfacelineContext : ctx.interfaceline()) {
            interfacelineContext.accept(this);
        }

        for (SmaliParser.MemberdefContext memberdefContext : ctx.memberdef()) {
            memberdefContext.accept(this);
        }

        return classDef;
    }

    @Override
    public ClassDef visitClassline(SmaliParser.ClasslineContext ctx) {
        classType = ctx.CLASSTYPE().getText();

        for (SmaliParser.ClassmodifierContext classmodifierContext : ctx.classmodifier()) {
            DexAccessFlags flag = DexAccessFlags.of(classmodifierContext.getText());
            accessFlags |= flag.getValue();
        }

        return classDef;
    }

    @Override
    public ClassDef visitSuperclassline(SmaliParser.SuperclasslineContext ctx) {
        superType = ctx.CLASSTYPE().getText();
        return classDef;
    }

    @Override
    public ClassDef visitSourceline(SmaliParser.SourcelineContext ctx) {
        sourceFile = ctx.FILENAME().getText();
        return classDef;
    }

    @Override
    public ClassDef visitInterfaceline(SmaliParser.InterfacelineContext ctx) {
        String interfaceType = ctx.CLASSTYPE().getText();
        classDef.getInterfaces().addType(dexFile.addOrGetTypeIDIndex(interfaceType));
        return classDef;
    }

    @Override
    public ClassDef visitMemberdef(SmaliParser.MemberdefContext ctx) {
        for (ParseTree child : ctx.children) {
            child.accept(this);
        }
        return classDef;
    }

    @Override
    public ClassDef visitFielddef(SmaliParser.FielddefContext ctx) {
        String name = ctx.MEMBERNAME().getText();
        String type = ctx.fieldtype().getText();

        int accessFlags = 0;
        for (SmaliParser.FieldmodifierContext fieldmodifierContext : ctx.fieldmodifier()) {
            DexAccessFlags flag = DexAccessFlags.of(fieldmodifierContext.getText());
            accessFlags |= flag.getValue();
        }

        int fieldIDIndex   = dexFile.addOrGetFieldID(classType, name, type);
        EncodedField field = EncodedField.of(fieldIDIndex, accessFlags);
        classDef.addField(dexFile, field);

        return super.visitFielddef(ctx);
    }

    @Override
    public ClassDef visitMethoddef(SmaliParser.MethoddefContext ctx) {
        System.out.println("method");
        return super.visitMethoddef(ctx);
    }
}

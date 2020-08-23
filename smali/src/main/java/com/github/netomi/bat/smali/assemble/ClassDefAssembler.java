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

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.DexAccessFlags;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.smali.parser.SmaliBaseVisitor;
import com.github.netomi.bat.smali.parser.SmaliParser;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

public class ClassDefAssembler
extends      SmaliBaseVisitor<ClassDef>
{
    private final DexFile dexFile;

    private String classType   = null;
    private int    accessFlags = 0;
    private String superType   = null;
    private String sourceFile  = null;

    public ClassDefAssembler(DexFile dexFile) {
        this.dexFile = dexFile;
    }

    @Override
    public ClassDef visitSmaliclass(SmaliParser.SmaliclassContext ctx) {
        ctx.classline().accept(this);
        ctx.superclassline().accept(this);

        if (ctx.sourceline() != null) {
            ctx.sourceline().accept(this);
        }

        System.out.println(sourceFile);

        int classTypeIndex = dexFile.addOrGetTypeIDIndex(classType);
        int superTypeIndex = dexFile.addOrGetTypeIDIndex(superType);
        int sourceFileIndex = sourceFile != null ? dexFile.addOrGetStringIDIndex(sourceFile) : NO_INDEX;

        return ClassDef.of(classTypeIndex,
                           accessFlags,
                           superTypeIndex,
                           sourceFileIndex);
    }

    @Override
    public ClassDef visitClassline(SmaliParser.ClasslineContext ctx) {
        classType = ctx.CLASSTYPE().getText();

        for (SmaliParser.ClassmodifierContext classmodifierContext : ctx.classmodifier()) {
            DexAccessFlags flag = DexAccessFlags.of(classmodifierContext.getText());
            accessFlags |= flag.getValue();
        }

        System.out.println(Integer.toHexString(accessFlags));
        return null;
    }

    @Override
    public ClassDef visitSuperclassline(SmaliParser.SuperclasslineContext ctx) {
        superType = ctx.CLASSTYPE().getText();
        return null;
    }

    @Override
    public ClassDef visitSourceline(SmaliParser.SourcelineContext ctx) {
        sourceFile = ctx.FILENAME().getText();
        return null;
    }

    // helper classes.
}

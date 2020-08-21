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
package com.github.netomi.bat.dexfile.visitor;

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.annotation.AnnotationsDirectory;

/**
 * This {@code AnnotationsDirectoryVisitor} will apply the given {@code AnnotationSetVisitor}
 * on the class annotation set of visited annotation directories.
 *
 * @author Thomas Neidhart
 */
public class ClassAnnotationSetVisitor
implements   AnnotationsDirectoryVisitor
{
    private final AnnotationSetVisitor visitor;

    public ClassAnnotationSetVisitor(AnnotationSetVisitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void visitAnnotationsDirectory(DexFile dexFile, ClassDef classDef, AnnotationsDirectory annotationsDirectory) {
        annotationsDirectory.classAnnotationSetAccept(dexFile, classDef, visitor);
    }
}
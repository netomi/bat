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

import com.github.netomi.bat.dexfile.ClassData;
import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.EncodedMethod;

public interface EncodedMethodVisitor
{
    void visitDirectMethod(DexFile dexFile, ClassDef classDef, ClassData classData, int index, EncodedMethod encodedMethod);

    void visitVirtualMethod(DexFile dexFile, ClassDef classDef, ClassData classData, int index, EncodedMethod encodedMethod);

}

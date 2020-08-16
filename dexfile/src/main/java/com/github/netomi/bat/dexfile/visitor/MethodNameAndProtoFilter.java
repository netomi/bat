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
import com.github.netomi.bat.dexfile.EncodedMethod;
import com.github.netomi.bat.dexfile.ProtoID;

public class MethodNameAndProtoFilter
implements   EncodedMethodVisitor
{
    private final String               name;
    private final ProtoID              protoID;
    private final EncodedMethodVisitor visitor;

    public MethodNameAndProtoFilter(String              name,
                                    ProtoID              protoID,
                                    EncodedMethodVisitor visitor) {
        this.name    = name;
        this.protoID = protoID;
        this.visitor = visitor;
    }

    @Override
    public void visitAnyMethod(DexFile dexFile, ClassDef classDef, int index, EncodedMethod method) {}

    @Override
    public void visitDirectMethod(DexFile dexFile, ClassDef classDef, int index, EncodedMethod method) {
        if (accepted(method.getName(dexFile), method.getProtoID(dexFile))) {
            visitor.visitDirectMethod(dexFile, classDef, index, method);
        }
    }

    @Override
    public void visitVirtualMethod(DexFile dexFile, ClassDef classDef, int index, EncodedMethod method) {
        if (accepted(method.getName(dexFile), method.getProtoID(dexFile))) {
            visitor.visitVirtualMethod(dexFile, classDef, index, method);
        }
    }

    // Private utility methods.

    private boolean accepted(String name, ProtoID protoID) {
        return this.name.equals(name) && this.protoID.equals(protoID);
    }
}

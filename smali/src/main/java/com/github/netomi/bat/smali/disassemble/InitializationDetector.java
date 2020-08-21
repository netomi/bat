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
import com.github.netomi.bat.dexfile.instruction.DexInstruction;
import com.github.netomi.bat.dexfile.instruction.FieldInstruction;
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;

class      InitializationDetector
implements InstructionVisitor
{
    private final String name;
    private final String type;

    private boolean fieldIsSetInStaticInitializer;

    public InitializationDetector(String fieldName, String type) {
        this.name = fieldName;
        this.type = type;
    }

    public boolean fieldIsSetInStaticInitializer() {
        return fieldIsSetInStaticInitializer;
    }

    @Override
    public void visitAnyInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, DexInstruction instruction) {}

    @Override
    public void visitFieldInstruction(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, FieldInstruction instruction) {
        String mnemonic = instruction.getMnemonic();


        if (mnemonic.contains("sput")) {
            FieldID fieldID = instruction.getField(dexFile);

            if (fieldID.getName(dexFile).equals(name) &&
                fieldID.getType(dexFile).equals(type)) {
                fieldIsSetInStaticInitializer = true;
            }
        }

    }
}

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
package com.github.netomi.bat.dexfile.util;

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.visitor.ClassDefVisitor;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ClassDefPool
{
    private final SortedMap<String, ClassDefData> classDefMap;

    public ClassDefPool() {
        this.classDefMap = new TreeMap<>();
    }

    public int size() {
        return classDefMap.size();
    }

    public void addClassDef(DexFile dexFile, int index, ClassDef classDef) {
        String className = classDef.getClassName(dexFile);

        classDefMap.computeIfAbsent(className, (clsName) -> new ClassDefData(dexFile, index, classDef));
    }

    public ClassDefData getClassDef(String className) {
        return classDefMap.get(className);
    }

    public void classDefsAccept(ClassDefVisitor visitor) {
        for (Map.Entry<String, ClassDefData> entry : classDefMap.entrySet()) {
            ClassDefData data = entry.getValue();

            visitor.visitClassDef(data.dexFile, data.index, data.classDef);
        }
    }

    // Inner helper classes.

    public static class ClassDefData
    {
        private final DexFile  dexFile;
        private final int      index;
        private final ClassDef classDef;

        ClassDefData(DexFile dexFile, int index, ClassDef classDef) {
            this.dexFile  = dexFile;
            this.index    = index;
            this.classDef = classDef;
        }

        public DexFile getDexFile() {
            return dexFile;
        }

        public int getIndex() {
            return index;
        }

        public ClassDef getClassDef() {
            return classDef;
        }
    }
}

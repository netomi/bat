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

public interface ClassDefVisitor
{
    void visitClassDef(DexFile dexFile, int index, ClassDef classDef);

    class      Multi
    extends    AbstractMultiVisitor<ClassDefVisitor>
    implements ClassDefVisitor
    {
        public static ClassDefVisitor of(ClassDefVisitor visitor, ClassDefVisitor... visitors) {
            return new Multi(visitor, visitors);
        }

        private Multi(ClassDefVisitor visitor, ClassDefVisitor... otherVisitors) {
            super(visitor, otherVisitors);
        }

        @Override
        public void visitClassDef(DexFile dexFile, int index, ClassDef classDef) {
            for (ClassDefVisitor visitor : getVisitors()) {
                visitor.visitClassDef(dexFile, index, classDef);
            }
        }
    }
}

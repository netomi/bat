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
import com.github.netomi.bat.dexfile.util.DexClasses;
import com.github.netomi.bat.util.Matchers;
import com.github.netomi.bat.util.StringMatcher;

public class ClassNameFilter
implements   ClassDefVisitor
{
    private final StringMatcher   matcher;
    private final ClassDefVisitor visitor;

    public ClassNameFilter(String regularExpression,
                           ClassDefVisitor visitor) {
        this.matcher = Matchers.classNameMatcher(regularExpression);
        this.visitor = visitor;
    }

    @Override
    public void visitClassDef(DexFile dexFile, int index, ClassDef classDefItem) {
        String className = classDefItem.getClassName(dexFile);
        if (accepted(DexClasses.externalClassNameFromInternalName(className))) {
            visitor.visitClassDef(dexFile, index, classDefItem);
        }
    }

    // Private utility methods.

    private boolean accepted(String className) {
        return matcher.matches(className);
    }
}

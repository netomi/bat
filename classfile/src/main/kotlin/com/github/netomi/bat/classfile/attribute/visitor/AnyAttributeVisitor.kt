/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
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

package com.github.netomi.bat.classfile.attribute.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeInvisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeVisibleAnnotationsAttribute

fun interface AnyAttributeVisitor {
    fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute)

    fun visitUnknownAttribute(classFile: ClassFile, attribute: UnknownAttribute) {
        visitAnyAttribute(classFile, attribute)
    }
}
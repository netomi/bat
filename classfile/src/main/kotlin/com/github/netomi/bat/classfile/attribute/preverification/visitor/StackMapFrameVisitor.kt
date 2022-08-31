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

package com.github.netomi.bat.classfile.attribute.preverification.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.preverification.*

fun interface StackMapFrameVisitor {
    fun visitAnyFrame(classFile: ClassFile, frame: StackMapFrame)

    fun visitSameFrame(classFile: ClassFile, frame: SameFrame) {
        visitAnyFrame(classFile, frame)
    }

    fun visitSameFrameExtended(classFile: ClassFile, frame: SameFrameExtended) {
        visitAnyFrame(classFile, frame)
    }

    fun visitChopFrame(classFile: ClassFile, frame: ChopFrame) {
        visitAnyFrame(classFile, frame)
    }

    fun visitAppendFrame(classFile: ClassFile, frame: AppendFrame) {
        visitAnyFrame(classFile, frame)
    }

    fun visitFullFrame(classFile: ClassFile, frame: FullFrame) {
        visitAnyFrame(classFile, frame)
    }

    fun visitSameLocalsOneStackItemFrame(classFile: ClassFile, frame: SameLocalsOneStackItemFrame) {
        visitAnyFrame(classFile, frame)
    }

    fun visitSameLocalsOneStackItemFrameExtended(classFile: ClassFile, frame: SameLocalsOneStackItemFrameExtended) {
        visitAnyFrame(classFile, frame)
    }
}
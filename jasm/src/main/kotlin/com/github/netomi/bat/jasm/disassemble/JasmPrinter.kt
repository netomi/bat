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

package com.github.netomi.bat.jasm.disassemble

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.io.IndentingPrinter
import java.io.OutputStreamWriter
import java.io.Writer

class JasmPrinter constructor(writer: Writer = OutputStreamWriter(System.out)) : ClassFileVisitor {

    private val printer: IndentingPrinter = IndentingPrinter(writer, 4)

    override fun visitClassFile(classFile: ClassFile) {
        TODO("Not yet implemented")
    }
}

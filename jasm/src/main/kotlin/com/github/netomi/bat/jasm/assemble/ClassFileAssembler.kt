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

package com.github.netomi.bat.jasm.assemble

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.jasm.parser.JasmBaseVisitor
import com.github.netomi.bat.jasm.parser.JasmParser.*
import java.io.PrintWriter

internal class ClassFileAssembler(private val lenientMode:    Boolean      = false,
                                  private val warningPrinter: PrintWriter? = null): JasmBaseVisitor<List<ClassFile>>() {

    override fun visitCFile(ctx: CFileContext): List<ClassFile> {
        return super.visitCFile(ctx)
    }
}
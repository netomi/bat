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

package com.github.netomi.bat.shrinker.io

import com.github.netomi.bat.classfile.io.ClassFileReader
import com.github.netomi.bat.io.DataEntry
import com.github.netomi.bat.io.DataEntryReader
import com.github.netomi.bat.shrinker.classfile.ProgramClass
import com.github.netomi.bat.shrinker.util.ProgramView

fun readProgramClasses(programView: ProgramView): DataEntryReader {
    return ProgramClassReader(programView)
}

private class ProgramClassReader constructor(private val programView: ProgramView): DataEntryReader {

    override fun read(entry: DataEntry) {
        entry.getInputStream().use { `is` ->
            val classFile = ProgramClass()
            val reader    = ClassFileReader(`is`, false)
            reader.visitClassFile(classFile)

            programView.addProgramClass(classFile)
        }
    }
}
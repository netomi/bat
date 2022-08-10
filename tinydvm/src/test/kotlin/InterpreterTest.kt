import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.util.ClassDefPool
import com.github.netomi.bat.dexfile.util.classDefPoolFiller
import com.github.netomi.bat.smali.Assembler
import com.github.netomi.bat.tinydvm.Dvm
import com.github.netomi.bat.tinydvm.Interpreter
import kotlin.test.Test

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

class InterpreterTest {

    @Test
    fun simpleInstruction() {
        InterpreterTest::class.java.getResourceAsStream("/smali/HelloWorld.smali")!!.use { `is` ->
            val dexFile = DexFile.empty()
            val classDef = Assembler(dexFile).assemble(`is`).first()

            val classDefPool = ClassDefPool.empty()
            dexFile.classDefsAccept(classDefPoolFiller(classDefPool))

            val dvm = Dvm(classDefPool)

            classDef.methodsAccept(dexFile, "main") { _, _, _, method ->
                val interpreter = Interpreter.of(dvm, dexFile, classDef, method)
                interpreter.invoke()
            }
        }
    }
}

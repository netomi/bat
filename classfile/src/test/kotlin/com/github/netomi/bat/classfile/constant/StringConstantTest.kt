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
package com.github.netomi.bat.classfile.constant

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.ConstantPool
import com.github.netomi.bat.classfile.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.visitor.ConstantVisitor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringConstantTest : ConstantBaseTest() {

    override fun createEmptyConstant(): StringConstant {
        return StringConstant.create(constantPool)
    }

    override fun createConstants(): List<StringConstant> {
        return listOf(StringConstant.create(constantPool, 10),
                StringConstant.create(constantPool, 0),
                StringConstant.create(constantPool, 100))
    }

    @Test
    fun accessors() {
        val constant = createConstants()[0]
        assertEquals(10, constant.stringIndex)

        constant.stringIndex = 20
        assertEquals(20, constant.stringIndex)
    }

    @Test
    fun constantVisitor() {
        val constant = createConstants()[0]

        var wrongMethod   = 0
        var correctMethod = 0

        constant.accept(ClassFile(), object : ConstantVisitor {
            override fun visitAnyConstant(classFile: ClassFile, constant: Constant) {
                wrongMethod++
            }

            override fun visitStringConstant(classFile: ClassFile, constant: StringConstant) {
                correctMethod++
            }
        })

        Assertions.assertTrue(wrongMethod == 0)
        Assertions.assertTrue(correctMethod == 1)
    }

    @Test
    fun constantPoolVisitor() {
        val constant = createConstants()[0]

        var wrongMethod   = 0
        var correctMethod = 0

        constant.accept(ClassFile(), 0, object : ConstantPoolVisitor {
            override fun visitAnyConstant(classFile: ClassFile, index: Int, constant: Constant) {
                wrongMethod++
            }

            override fun visitStringConstant(classFile: ClassFile, index: Int, constant: StringConstant) {
                correctMethod++
            }
        })

        Assertions.assertTrue(wrongMethod == 0)
        Assertions.assertTrue(correctMethod == 1)
    }
}
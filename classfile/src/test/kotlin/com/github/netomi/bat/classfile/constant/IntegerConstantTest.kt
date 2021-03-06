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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.ConstantPool
import com.github.netomi.bat.classfile.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.visitor.ConstantVisitor

class IntegerConstantTest : ConstantBaseTest() {

    override fun createEmptyConstant(): IntegerConstant {
        return IntegerConstant.create()
    }

    override fun createConstants(): List<IntegerConstant> {
        return listOf(IntegerConstant.create(0),
                IntegerConstant.create(100),
                IntegerConstant.create(Integer.MIN_VALUE))
    }

    @Test
    fun accessors() {
        val constant = createConstants()[0]
        assertEquals(0, constant.value)

        constant.value = Integer.MAX_VALUE
        assertEquals(Integer.MAX_VALUE, constant.value)
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

            override fun visitIntegerConstant(classFile: ClassFile, constant: IntegerConstant) {
                correctMethod++
            }
        })

        assertTrue(wrongMethod == 0)
        assertTrue(correctMethod == 1)
    }

    @Test
    fun constantPoolVisitor() {
        val constant = createConstants()[0]

        var wrongMethod   = 0
        var correctMethod = 0

        constant.accept(ClassFile(), ConstantPool(), 0, object : ConstantPoolVisitor {
            override fun visitAnyConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: Constant) {
                wrongMethod++
            }

            override fun visitIntegerConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: IntegerConstant) {
                correctMethod++
            }
        })

        assertTrue(wrongMethod == 0)
        assertTrue(correctMethod == 1)
    }
}
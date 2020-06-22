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
package org.netomi.bat.classfile.constant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.netomi.bat.classfile.ClassFile
import org.netomi.bat.classfile.ConstantPool
import org.netomi.bat.classfile.visitor.ConstantPoolVisitor
import org.netomi.bat.classfile.visitor.ConstantVisitor

class LongConstantTest : ConstantBaseTest() {

    override fun createEmptyConstant(): LongConstant {
        return LongConstant.create()
    }

    override fun createConstants(): List<LongConstant> {
        return listOf(LongConstant.create(0),
                      LongConstant.create(100),
                      LongConstant.create(Long.MIN_VALUE))
    }

    @Test
    fun accessors() {
        val constant = createConstants()[0]
        assertEquals(0, constant.value)

        constant.value = Long.MAX_VALUE
        assertEquals(Long.MAX_VALUE, constant.value)
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

            override fun visitLongConstant(classFile: ClassFile, constant: LongConstant) {
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

            override fun visitLongConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: LongConstant) {
                correctMethod++
            }
        })

        assertTrue(wrongMethod == 0)
        assertTrue(correctMethod == 1)
    }
}
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
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LongConstantTest : ConstantBaseTest() {

    override fun createConstants(): List<LongConstant> {
        return listOf(LongConstant.of(0),
                      LongConstant.of(100),
                      LongConstant.of(Long.MIN_VALUE))
    }

    @Test
    fun accessors() {
        val constant = createConstants()[0]
        assertEquals(0, constant.value)
    }

    @Test
    fun constantVisitor() {
        val constant = createConstants()[0]

        var wrongMethod   = 0
        var correctMethod = 0

        constant.accept(ClassFile.empty(), 0, object : ConstantVisitor {
            override fun visitAnyConstant(classFile: ClassFile, index: Int, constant: Constant) {
                wrongMethod++
            }

            override fun visitLongConstant(classFile: ClassFile, index: Int, constant: LongConstant) {
                correctMethod++
            }
        })

        assertTrue(wrongMethod == 0)
        assertTrue(correctMethod == 1)
    }
}
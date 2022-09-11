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

package com.github.netomi.bat.classfile.attribute

import com.github.netomi.bat.classfile.AbstractClassFileTest
import com.github.netomi.bat.classfile.visitor.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExceptionsAttributeTest: AbstractClassFileTest() {

    @Test
    fun checkAttributeExistence() {
        val classFile = loadClassFile("ExceptionClass")

        assertEquals(getPackageName() + "/ExceptionClass", classFile.className.toString(), "classnames do not match")

        val methodCollector = methodCollector()
        classFile.methodsAccept(filterMethodsByName("method", methodCollector))

        val exceptionsAttribute =
            methodCollector.items().singleOrNull()?.attributes?.filterIsInstance<ExceptionsAttribute>()?.singleOrNull()
        assertNotNull(exceptionsAttribute)

        assertEquals(2, exceptionsAttribute.size)
        assertEquals("java/lang/RuntimeException", classFile.getClassName(exceptionsAttribute[0]).toString())
        assertEquals("java/io/IOException", classFile.getClassName(exceptionsAttribute[1]).toString())
    }
}
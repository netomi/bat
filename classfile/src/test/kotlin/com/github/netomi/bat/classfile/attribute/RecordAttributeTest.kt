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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RecordAttributeTest: AbstractClassFileTest() {

    @Test
    fun checkAttributeExistence() {
        val classFile = loadClassFile("RecordClass")

        assertEquals(getPackageName() + "/RecordClass", classFile.className.toString(), "classnames do not match")
        val recordAttribute = classFile.attributes.filterIsInstance<RecordAttribute>().singleOrNull()
        assertNotNull(recordAttribute)

        // 2 components
        assertEquals(2, recordAttribute.size)

        assertEquals("name", recordAttribute[0].getName(classFile))
        assertEquals("Ljava/lang/String;", recordAttribute[0].getDescriptor(classFile))

        assertEquals("value", recordAttribute[1].getName(classFile))
        assertEquals("Ljava/lang/Integer;", recordAttribute[1].getDescriptor(classFile))
    }
}
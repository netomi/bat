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

package com.github.netomi.bat.dexfile.editor

import com.github.netomi.bat.dexfile.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClassDefEditorTest {

    private fun editor(): ClassDefEditor {
        val dexFile   = DexFile.of(DexFormat.FORMAT_035)
        val dexEditor = DexEditor.of(dexFile)

        return dexEditor.addClassDef(TEST_CLASS_TYPE, Visibility.PUBLIC)
    }

    @Test
    fun setVisibility() {
        val classDefEditor = editor()
        val classDef = classDefEditor.classDef

        assertEquals(Visibility.PUBLIC, classDef.visibility)
        assertTrue(classDef.accessFlags and ACC_PUBLIC != 0)

        classDefEditor.setVisibility(Visibility.PRIVATE)
        assertEquals(Visibility.PRIVATE, classDef.visibility)
        assertTrue(classDef.accessFlags and ACC_PRIVATE != 0)

        classDefEditor.setVisibility(Visibility.PACKAGE_PRIVATE)
        assertEquals(Visibility.PACKAGE_PRIVATE, classDef.visibility)
        assertTrue(classDef.accessFlags and 0xf == 0)
    }

    @Test
    fun addModifier() {
        val classDefEditor = editor()
        val classDef = classDefEditor.classDef

        assertFalse(classDef.modifiers.contains(ClassModifier.FINAL))

        classDefEditor.addModifier(ClassModifier.FINAL)
        assertTrue(classDef.modifiers.contains(ClassModifier.FINAL))

        val before = classDef.accessFlags
        classDefEditor.addModifier(ClassModifier.FINAL)
        val after = classDef.accessFlags
        assertEquals(before, after)
        assertTrue(classDef.modifiers.contains(ClassModifier.FINAL))
    }

    @Test
    fun removeModifier() {
        val classDefEditor = editor()
        val classDef = classDefEditor.classDef

        assertFalse(classDef.modifiers.contains(ClassModifier.FINAL))

        classDefEditor.addModifier(ClassModifier.FINAL)
        assertTrue(classDef.modifiers.contains(ClassModifier.FINAL))

        classDefEditor.removeModifier(ClassModifier.FINAL)
        assertFalse(classDef.modifiers.contains(ClassModifier.FINAL))

        val before = classDef.accessFlags
        classDefEditor.removeModifier(ClassModifier.ABSTRACT)
        val after = classDef.accessFlags
        assertEquals(before, after)
        assertFalse(classDef.modifiers.contains(ClassModifier.ABSTRACT))

    }

    companion object {
        const val TEST_CLASS_TYPE = "LTestClass;"
    }
}
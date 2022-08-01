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

package com.github.netomi.bat.smali

import com.github.netomi.bat.io.OutputStreamFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

internal class TestOutputStreamFactory : OutputStreamFactory {
    private val outputStreamMap: MutableMap<String, ByteArrayOutputStream> = TreeMap()

    val classNames: Iterable<String>
        get() = outputStreamMap.keys

    fun getOutputStream(className: String): ByteArrayOutputStream? {
        return outputStreamMap[className]
    }

    @Throws(IOException::class)
    override fun createOutputStream(element: String): OutputStream {
        val baos = ByteArrayOutputStream()
        outputStreamMap[element] = baos
        return baos
    }
}

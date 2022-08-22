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
package com.github.netomi.bat.classfile

import com.github.netomi.bat.classfile.constant.ConstantPool
import java.io.DataInput
import java.io.IOException

/**
 * https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.6
 */
class Method internal constructor(): Member() {

    override val accessFlagTarget: AccessFlagTarget
        get() = AccessFlagTarget.METHOD

    companion object {
        @Throws(IOException::class)
        fun readMethod(input: DataInput, constantPool: ConstantPool): Method {
            val method = Method()
            method.read(input, constantPool)
            return method
        }
    }
}
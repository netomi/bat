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

package com.github.netomi.bat.tinydvm.data.jvm

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.ProtoID
import com.github.netomi.bat.tinydvm.data.DvmClass
import com.github.netomi.bat.tinydvm.data.DvmField
import com.github.netomi.bat.tinydvm.data.DvmMethod
import com.github.netomi.bat.tinydvm.overrides.Override
import com.github.netomi.bat.util.*

class DvmNativeClass private constructor(private val clazz: Class<Any>): DvmClass() {

    override val type: JvmType
        get() = clazz.name.asExternalClassName().toInternalType().asJvmType()

    override val className: String
        get() = clazz.name.asExternalClassName().toInternalClassName()

    private val fieldCache = mutableMapOf<String, DvmNativeField?>()

    override fun getField(name: String, type: JvmType): DvmField? {
        return fieldCache.computeIfAbsent(name) { fieldName ->
            try {
                DvmNativeField.of(clazz.getField(fieldName), type)
            } catch (exception: NoSuchFieldException) {
                null
            }
        }
    }

    override fun getDirectMethod(dexFile: DexFile, name: String, protoID: ProtoID): DvmMethod? {
        val parameterClasses = protoID.getParameterTypes(dexFile).map { it.toJvmClass() }.toMutableList()

        return try {
            var methodName = name

            val ann = clazz.getDeclaredAnnotation(Override::class.java)
            if (ann != null) {
                for (mapping in ann.names) {
                    if (mapping.name == name) {
                        methodName = mapping.overrideName

                        parameterClasses.add(0, mapping.overrideDescriptor.asJvmType().toJvmClass())
                    }
                }
            }

            val method = clazz.getDeclaredMethod(methodName, *parameterClasses.toTypedArray())
            DvmNativeMethod.of(method, dexFile, protoID)
        } catch (exception: NoSuchMethodException) {
            null
        }
    }

    override fun toString(): String {
        return "DvmNativeClass[type=$type]"
    }

    companion object {
        internal fun of(clazz: Class<out Any>): DvmNativeClass {
            return DvmNativeClass(clazz as Class<Any>)
        }

        fun of(type: JvmType): DvmNativeClass {
            val externalClassName = type.toExternalClassName()
            return DvmNativeClass(Class.forName(externalClassName) as Class<Any>)
        }
    }
}
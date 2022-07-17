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
package com.github.netomi.bat.dexfile

/**
 * Represents a data item contained in a dex file.
 */
abstract class DataItem : DexContent() {
    /**
     * Returns the type of this `DataItem` instance.
     *
     * @return the type of this DataItem.
     */
    val itemType: Int
        get() = javaClass.getAnnotation(DataItemAnn::class.java).type

    val dataAlignment: Int
        get() = javaClass.getAnnotation(DataItemAnn::class.java).dataAlignment

    val containedInDataSection: Boolean
        get() = javaClass.getAnnotation(DataItemAnn::class.java).dataSection

    abstract val isEmpty: Boolean

    interface Map {
        fun getOffset(item: DataItem?): Int
    }
}

@Retention(AnnotationRetention.RUNTIME)
annotation class DataItemAnn(val type: Int, val dataAlignment: Int, val dataSection: Boolean)
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
 * A base class for encoded fields and methods in a dex file
 */
abstract class EncodedMember protected constructor(accessFlags: Int = 0) : DexContent() {

    var accessFlags: Int = accessFlags
        protected set(value) {
            field = value
            visibility = Visibility.of(value)
            updateModifiers(value)
        }

    var visibility: Visibility = Visibility.of(accessFlags)
        private set

    abstract fun getName(dexFile: DexFile): String

    protected abstract fun updateModifiers(accessFlags: Int)
}

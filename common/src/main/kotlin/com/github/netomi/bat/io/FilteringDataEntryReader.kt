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

package com.github.netomi.bat.io

import com.github.netomi.bat.util.StringMatcher

fun filterDataEntriesBy(matcher:        StringMatcher,
                        acceptedReader: DataEntryReader,
                        rejectedReader: DataEntryReader? = null): DataEntryReader {
    return FilteringDataEntryReader(matcher, acceptedReader, rejectedReader)
}

private class FilteringDataEntryReader
    constructor(private val matcher:        StringMatcher,
                private val acceptedReader: DataEntryReader,
                private val rejectedReader: DataEntryReader?): DataEntryReader {

    override fun read(entry: DataEntry) {
        if (matcher.matches(entry.name)) {
            acceptedReader.read(entry)
        } else {
            rejectedReader?.read(entry)
        }
    }
}
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

package com.github.netomi.bat.shrinker

import com.github.netomi.bat.io.PathInputSource
import com.github.netomi.bat.io.filterDataEntriesBy
import com.github.netomi.bat.io.unwrapArchives
import com.github.netomi.bat.shrinker.marker.ClassUsageMarker
import com.github.netomi.bat.shrinker.marker.UsageMarker
import com.github.netomi.bat.shrinker.wpo.WPOContext
import com.github.netomi.bat.shrinker.wpo.io.readLibraryClasses
import com.github.netomi.bat.shrinker.wpo.io.readProgramClasses
import com.github.netomi.bat.util.fileNameMatcher
import java.nio.file.Path

fun main(args: Array<String>) {
    val jmodPath = Path.of("/home/tn/.sdkman/candidates/java/current/jmods")

    val context = WPOContext()

    val inputSource = PathInputSource.of(jmodPath)
    inputSource.pumpDataEntries(
        unwrapArchives(
        filterDataEntriesBy(fileNameMatcher("java/**.class"),
        readLibraryClasses(context))))

    println("loaded ${context.libraryClassCount} library classes")

    val programInputSource = PathInputSource.of("../tmp/shrinking/input.jar")
    programInputSource.pumpDataEntries(
        unwrapArchives(
        filterDataEntriesBy(fileNameMatcher("**.class"),
        readProgramClasses(context))))

    println("loaded ${context.programClassCount} program classes")

    context.init()

    val usageMarker = UsageMarker()
    context.libraryClassesAccept(ClassUsageMarker(usageMarker))

    val clazz = context.getClass("test0007/Testa")

    context.programClassesAccept(ClassShrinker(usageMarker))

    for (method in clazz!!.methods) {
        println("${method.getName(clazz)} = ${usageMarker.isUsed(method)}")
    }

    println("done")
}
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

package com.github.netomi.bat.classfile

import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.module.ModuleHashesAttribute
import com.github.netomi.bat.classfile.constant.visitor.IDAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.constant.editor.ConstantPoolShrinker
import com.github.netomi.bat.classfile.io.ClassFileWriter
import java.nio.file.Paths
import java.util.TreeSet
import java.util.zip.ZipInputStream
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

fun main(args: Array<String>) {
    //val path = Paths.get("/home/tn/workspace/android_sdk/platforms/android-33/android.jar")
//    val path = Paths.get("/home/tn/.sdkman/candidates/java/current/jmods/java.base.jmod")
//
//    val `is` = path.inputStream()
//    `is`.skip(4)
//
//    val pool = mutableListOf<ClassFile>()
//
//    val start = System.nanoTime()
//    ZipInputStream(`is`).use { zis ->
//        generateSequence { zis.nextEntry }
//            .filterNot { it.isDirectory }
//            .filter { it.name.endsWith(".class") }
//            .map {
//                println(it.name)
//
//                val classFile = ClassFile.read(zis, false)
//                pool.add(classFile)
//                zis.closeEntry()
//            }.first()
//    }
//    val end = System.nanoTime()
//    println("read ${pool.size} class files, took ${(end - start)/1e6} ms")
//    val classfile = pool[0]

    val classfile = ClassFile.read(Paths.get("ProtoID.class").inputStream())

    val visitedIndices = TreeSet<Int>()


//    val attr = classfile._attributes.get<ModuleHashesAttribute>(AttributeType.MODULE_HASHES)
//    classfile._attributes.removeAttribute(attr!!)

    classfile.accept(ConstantPoolShrinker())

    classfile.referencedConstantsAccept(false, MyReferencedConstantVisitor(visitedIndices))

    println("${visitedIndices.size} == ${classfile.constantPool.size}")

    val writer = ClassFileWriter(Paths.get("shrunk.class").outputStream())
    writer.visitClassFile(classfile)
}

class MyConstantUsageVisitor constructor(val usageMarker: MutableMap<Any, Boolean>): ReferencedConstantVisitor {
    override fun visitAnyConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        val constantIndex = accessor.get()
        val constant = classFile.getConstant(constantIndex)
        usageMarker[constant] = true
        constant.referencedConstantsAccept(classFile, this)
    }
}

class MyReferencedConstantVisitor constructor(val visitedIndices: MutableSet<Int>): ReferencedConstantVisitor {
    override fun visitAnyConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        val constantIndex = accessor.get()
        visitedIndices.add(constantIndex)
        val constant = classFile.getConstant(constantIndex)
        constant.referencedConstantsAccept(classFile, this)
    }
}
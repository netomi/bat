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

import com.github.netomi.bat.classfile.io.ClassFilePrinter
import java.io.DataInput
import java.io.DataInputStream
import java.io.FileInputStream
import java.io.IOException

interface TypedVisitor<out T> {
    fun visitAnyElement(): T? {
        return null
    }

    fun visitElement(element: Element): T? {
        return visitAnyElement()
    }
}

typealias Visitor = TypedVisitor<Any>

data class Element (val a: Int) {
    fun <T> accept(visitor: TypedVisitor<T>): T? {
        return visitor.visitElement(this)
    }
}

class Test {
   val elements: MutableList<Element> = mutableListOf()

    fun acceptElements(visitor: Visitor) {
        elements.forEach { it.accept(visitor) }
    }

    fun <T> collectElements(visitor: TypedVisitor<T>): Collection<T> {
        val result = mutableListOf<T>()
        elements.forEach {
            when(val x = it.accept(visitor)) {
                Unit, null -> Unit
                else -> result.add(x)
            }
        }
        return result
    }
}

object Main {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val input: DataInput = DataInputStream(FileInputStream("Member.class"))
        val classFile = ClassFile.readClassFile(input)
        classFile.accept(ClassFilePrinter())

//        val t = Test()
//        t.elements.add(Element(1))
//        t.elements.add(Element(2))
//
//        val visitor = object: Visitor {
//            override fun visitElement(element: Element) {
//                println("element ${element.a}")
//                //return element
//            }
//        }
//
//        t.acceptElements(visitor)
//
//        val result = t.collectElements(visitor)
//        println(result)
    }
}
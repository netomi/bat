package org.netomi.bat.classfile

import org.netomi.bat.classfile.io.ClassFilePrinter
import java.io.DataInput
import java.io.DataInputStream
import java.io.FileInputStream
import java.io.IOException

object Main {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val input: DataInput = DataInputStream(FileInputStream("MyTest.class"))
        val classFile = ClassFile.readClassFile(input)
        classFile.accept(ClassFilePrinter())
    }
}
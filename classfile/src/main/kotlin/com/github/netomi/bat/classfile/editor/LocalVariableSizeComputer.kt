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

package com.github.netomi.bat.classfile.editor

import com.github.netomi.bat.classfile.instruction.JvmInstruction
import com.github.netomi.bat.classfile.verifier.Frame
import com.github.netomi.bat.classfile.verifier.FrameProcessor

internal class LocalVariableSizeComputer: FrameProcessor {
    var localVariableSize: Int = 0
        private set

    override fun handleInstruction(offset: Int, instruction: JvmInstruction, frameBefore: Frame, frameAfter: Frame) {
        localVariableSize = localVariableSize.coerceAtLeast(frameAfter.variableSize)
    }
}
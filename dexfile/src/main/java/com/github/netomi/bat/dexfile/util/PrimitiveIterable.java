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
package com.github.netomi.bat.dexfile.util;

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.util.IntArray;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.BiFunction;

public class PrimitiveIterable<E>
implements   Iterable<E>
{
    private final DexFile                         dexFile;
    private final BiFunction<DexFile, Integer, E> accessorFunction;
    private final IntArray                        array;

    public static <E> Iterable<E> of(DexFile                         dexFile,
                                     BiFunction<DexFile, Integer, E> accessorFunction,
                                     IntArray                        array) {
        return new PrimitiveIterable<E>(dexFile, accessorFunction, array);
    }

    private PrimitiveIterable(DexFile                         dexFile,
                              BiFunction<DexFile, Integer, E> accessorFunction,
                              IntArray                        array) {
        this.dexFile          = dexFile;
        this.accessorFunction = accessorFunction;
        this.array            = array;
    }
    
    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < array.size();
            }

            @Override
            public E next() {
                return accessorFunction.apply(dexFile, array.get(index++));
            }
        };
    }
}

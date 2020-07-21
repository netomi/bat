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
package com.github.netomi.bat.smali.io;

import com.github.netomi.bat.dexfile.DexUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileOutputStreamFactory
implements   OutputStreamFactory
{
    private final Path baseDir;

    public FileOutputStreamFactory(Path baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public OutputStream createOutputStream(String className) throws IOException {
        String[] packageComponents = DexUtil.internalPackageNameFromInternalName(className).split("/");

        Path currentPath = baseDir;
        for (String component : packageComponents) {
            currentPath = currentPath.resolve(component);
        }

        Files.createDirectories(currentPath);

        currentPath = currentPath.resolve(DexUtil.simpleClassNameFromInternalName(className) + ".smali");
        return Files.newOutputStream(currentPath);
    }
}

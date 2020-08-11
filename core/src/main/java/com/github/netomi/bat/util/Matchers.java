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
package com.github.netomi.bat.util;

/**
 * @author Thomas Neidhart
 */
public final class Matchers
{
    // prevent intialization.
    private Matchers() {}

    public static StringMatcher classNameMatcher(String regularExpression) {
        return new ClassNameMatcher(regularExpression);
    }

    public static StringMatcher fileNameMatcher(String regularExpression) {
        return new FileNameMatcher(regularExpression);
    }

    // helper classes.

    private static class ClassNameMatcher extends RegexStringMatcher
    {
        public ClassNameMatcher(String regularExpression) {
            super("\\.\\/", regularExpression);
        }
    }

    private static class FileNameMatcher extends RegexStringMatcher
    {
        public FileNameMatcher(String regularExpression) {
            super("\\/", regularExpression);
        }
    }
}

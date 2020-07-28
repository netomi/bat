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
public interface StringMatcher
{
    boolean matches(String input);

    default StringMatcher or(StringMatcher other) {
        return (input) -> StringMatcher.this.matches(input) || other.matches(input);
    }

    default StringMatcher and(StringMatcher other) {
        return (input) -> StringMatcher.this.matches(input) && other.matches(input);
    }
}

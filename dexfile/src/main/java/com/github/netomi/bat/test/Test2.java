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
package com.github.netomi.bat.test;

public class Test2 {

    public static void main(String[] args) {
        short value1 = (short) 0xf000;
        short value2 = 0x0001;

        System.out.println(Integer.toHexString(value1));
        System.out.println(Integer.toHexString(value2));

        int intValue = (value1 & 0xffff) | (value2 << 16);

        System.out.println(intValue);

    }
}

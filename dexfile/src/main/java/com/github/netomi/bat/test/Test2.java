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

    public static short get(byte[] arr, int bytes) {
        short result = 0;
        for (int i = 0; i < bytes; i++) {
            int b = arr[i];
            result |= b << (8*i);
        }

        // sign-extend
        int shift = 32 - 8 * bytes;
        return shift == 0 ?
            result :
            (short) (result << shift >> shift);
    }

    public static void main(String[] args) {
        byte[] arr = new byte[] { (byte) 0x10, (byte) 0x10 };

        short value = get(arr, 2);
        System.out.println(value);
    }
}

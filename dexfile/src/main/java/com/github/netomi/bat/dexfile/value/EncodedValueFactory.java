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

package com.github.netomi.bat.dexfile.value;

import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexFormatException;
import com.github.netomi.bat.dexfile.util.Primitives;

import static com.github.netomi.bat.dexfile.value.EncodedValue.*;

public class EncodedValueFactory
{
    private EncodedValueFactory() {}

    public static EncodedValue readValue(DexDataInput input) {
        int typeAndArg = input.readUnsignedByte();

        int valueArg = typeAndArg >>> 5;
        int valueType = typeAndArg & 0x1f;

        EncodedValue encodedValue = create(valueType);

        encodedValue.read(input, valueArg);
        return encodedValue;
    }

    private static EncodedValue create(int valueType) {
        switch (valueType) {
            case VALUE_BYTE:          return new EncodedByteValue();
            case VALUE_SHORT:         return new EncodedShortValue();
            case VALUE_CHAR:          return new EncodedCharValue();
            case VALUE_INT:           return new EncodedIntValue();
            case VALUE_LONG:          return new EncodedLongValue();
            case VALUE_FLOAT:         return new EncodedFloatValue();
            case VALUE_DOUBLE:        return new EncodedDoubleValue();
            case VALUE_METHOD_TYPE:   return new EncodedMethodTypeValue();
            case VALUE_METHOD_HANDLE: return new EncodedMethodHandleValue();
            case VALUE_STRING:        return new EncodedStringValue();
            case VALUE_TYPE:          return new EncodedTypeValue();
            case VALUE_FIELD:         return new EncodedFieldValue();
            case VALUE_METHOD:        return new EncodedMethodValue();
            case VALUE_ENUM:          return new EncodedEnumValue();
            case VALUE_ARRAY:         return new EncodedArrayValue();
            case VALUE_ANNOTATION:    return new EncodedAnnotationValue();
            case VALUE_NULL:          return new EncodedNullValue();
            case VALUE_BOOLEAN:       return new EncodedBooleanValue();
            default: throw new DexFormatException("Unexpected encoded value type: " + Primitives.toHexString((short) valueType));
        }
    }
}

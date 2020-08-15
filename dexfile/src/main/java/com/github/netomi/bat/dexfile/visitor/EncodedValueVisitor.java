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
package com.github.netomi.bat.dexfile.visitor;

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.value.*;

import java.util.function.BiConsumer;

public interface EncodedValueVisitor
{
    void visitAnyValue(DexFile dexFile, EncodedValue value);

    default void visitAnnotationValue(DexFile dexFile, EncodedAnnotationValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitArrayValue(DexFile dexFile, EncodedArrayValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitBooleanValue(DexFile dexFile, EncodedBooleanValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitByteValue(DexFile dexFile, EncodedByteValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitCharValue(DexFile dexFile, EncodedCharValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitDoubleValue(DexFile dexFile, EncodedDoubleValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitEnumValue(DexFile dexFile, EncodedEnumValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitFieldValue(DexFile dexFile, EncodedFieldValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitFloatValue(DexFile dexFile, EncodedFloatValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitIntValue(DexFile dexFile, EncodedIntValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitLongValue(DexFile dexFile, EncodedLongValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitMethodHandleValue(DexFile dexFile, EncodedMethodHandleValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitMethodTypeValue(DexFile dexFile, EncodedMethodTypeValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitMethodValue(DexFile dexFile, EncodedMethodValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitNullValue(DexFile dexFile, EncodedNullValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitShortValue(DexFile dexFile, EncodedShortValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitStringValue(DexFile dexFile, EncodedStringValue value) {
        visitAnyValue(dexFile, value);
    }

    default void visitTypeValue(DexFile dexFile, EncodedTypeValue value) {
        visitAnyValue(dexFile, value);
    }

    static EncodedValueVisitor concatenate(EncodedValueVisitor... visitors) {
        return new EncodedValueVisitor() {
            @Override
            public void visitAnyValue(DexFile dexFile, EncodedValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitAnyValue(dexFile, value);
                }
            }

            @Override
            public void visitAnnotationValue(DexFile dexFile, EncodedAnnotationValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitAnnotationValue(dexFile, value);
                }
            }

            @Override
            public void visitArrayValue(DexFile dexFile, EncodedArrayValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitArrayValue(dexFile, value);
                }
            }

            @Override
            public void visitBooleanValue(DexFile dexFile, EncodedBooleanValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitBooleanValue(dexFile, value);
                }
            }

            @Override
            public void visitByteValue(DexFile dexFile, EncodedByteValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitByteValue(dexFile, value);
                }
            }

            @Override
            public void visitCharValue(DexFile dexFile, EncodedCharValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitCharValue(dexFile, value);
                }
            }

            @Override
            public void visitDoubleValue(DexFile dexFile, EncodedDoubleValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitDoubleValue(dexFile, value);
                }
            }

            @Override
            public void visitEnumValue(DexFile dexFile, EncodedEnumValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitEnumValue(dexFile, value);
                }
            }

            @Override
            public void visitFieldValue(DexFile dexFile, EncodedFieldValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitFieldValue(dexFile, value);
                }
            }

            @Override
            public void visitFloatValue(DexFile dexFile, EncodedFloatValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitFloatValue(dexFile, value);
                }
            }

            @Override
            public void visitIntValue(DexFile dexFile, EncodedIntValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitIntValue(dexFile, value);
                }
            }

            @Override
            public void visitLongValue(DexFile dexFile, EncodedLongValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitLongValue(dexFile, value);
                }
            }

            @Override
            public void visitMethodHandleValue(DexFile dexFile, EncodedMethodHandleValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitMethodHandleValue(dexFile, value);
                }
            }

            @Override
            public void visitMethodTypeValue(DexFile dexFile, EncodedMethodTypeValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitMethodTypeValue(dexFile, value);
                }
            }

            @Override
            public void visitMethodValue(DexFile dexFile, EncodedMethodValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitMethodValue(dexFile, value);
                }
            }

            @Override
            public void visitNullValue(DexFile dexFile, EncodedNullValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitNullValue(dexFile, value);
                }
            }

            @Override
            public void visitShortValue(DexFile dexFile, EncodedShortValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitShortValue(dexFile, value);
                }
            }

            @Override
            public void visitStringValue(DexFile dexFile, EncodedStringValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitStringValue(dexFile, value);
                }
            }

            @Override
            public void visitTypeValue(DexFile dexFile, EncodedTypeValue value) {
                for (EncodedValueVisitor visitor : visitors) {
                    visitor.visitTypeValue(dexFile, value);
                }
            }
        };
    }
}

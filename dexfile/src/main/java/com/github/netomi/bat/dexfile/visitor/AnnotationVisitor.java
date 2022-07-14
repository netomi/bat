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

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.annotation.Annotation;
import com.github.netomi.bat.dexfile.annotation.AnnotationSet;

import java.util.function.BiConsumer;

public interface AnnotationVisitor
{
    void visitAnnotation(DexFile dexFile, ClassDef classDef, AnnotationSet annotationSet, int index, Annotation annotation);

    default AnnotationVisitor andThen(AnnotationVisitor... visitors) {
        return Multi.of(this, visitors);
    }

    default AnnotationVisitor joinedByAnnotationConsumer(BiConsumer<DexFile, Annotation> consumer) {
        AnnotationVisitor joiner = new AnnotationVisitor() {
            private boolean firstVisited = false;
            @Override
            public void visitAnnotation(DexFile dexFile, ClassDef classDef, AnnotationSet annotationSet, int index, Annotation annotation) {
                if (firstVisited) {
                    consumer.accept(dexFile, annotation);
                } else {
                    firstVisited = true;
                }
            }
        };

        return Multi.of(joiner, this);
    }

    class      Multi
    extends    AbstractMultiVisitor<AnnotationVisitor>
    implements AnnotationVisitor
    {
        public static AnnotationVisitor of(AnnotationVisitor visitor, AnnotationVisitor... visitors) {
            return new Multi(visitor, visitors);
        }

        private Multi(AnnotationVisitor visitor, AnnotationVisitor... otherVisitors) {
            super(visitor, otherVisitors);
        }

        @Override
        public void visitAnnotation(DexFile dexFile, ClassDef classDef, AnnotationSet annotationSet, int index, Annotation annotation) {
            for (AnnotationVisitor visitor : getVisitors()) {
                visitor.visitAnnotation(dexFile, classDef, annotationSet, index, annotation);
            }
        }
    }
}

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

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultiVisitor<V>
{
    private final List<V> visitors;

    @SafeVarargs
    protected AbstractMultiVisitor(V visitor, V... otherVisitors) {
        ArrayList<V> visitorList = new ArrayList<>();

        addVisitors(visitor, visitorList);
        for (V nextVisitor : otherVisitors) {
            addVisitors(nextVisitor, visitorList);
        }

        visitorList.trimToSize();
        visitors = visitorList;
    }

    @SuppressWarnings("unchecked")
    private static <V> void addVisitors(V visitor, List<V> visitorList) {
        if (visitor instanceof AbstractMultiVisitor<?>) {
            List<V> visitors = ((AbstractMultiVisitor<V>) visitor).visitors;
            visitorList.addAll(visitors);
        } else {
            visitorList.add(visitor);
        }
    }

    public Iterable<V> visitors() {
        return visitors;
    }
}

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
package com.github.netomi.bat.dexfile.visitor

abstract class AbstractMultiVisitor<V> @SafeVarargs protected constructor(visitor: V, vararg otherVisitors: V) {

    private val _visitors: List<V>

    init {
        val visitorList = ArrayList<V>()
        addVisitor(visitor, visitorList)
        for (nextVisitor in otherVisitors) {
            addVisitor(nextVisitor, visitorList)
        }
        visitorList.trimToSize()
        _visitors = visitorList
    }

    val visitors: Iterable<V>
        get() = _visitors

    companion object {
        private fun <V> addVisitor(visitor: V, visitorList: MutableList<V>) {
            if (visitor is AbstractMultiVisitor<*>) {
                val visitors = (visitor as AbstractMultiVisitor<V>).visitors
                visitorList.addAll(visitors)
            } else {
                visitorList.add(visitor)
            }
        }
    }
}
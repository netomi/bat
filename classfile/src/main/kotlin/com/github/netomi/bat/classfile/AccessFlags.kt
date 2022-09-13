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
package com.github.netomi.bat.classfile

import com.github.netomi.bat.classfile.AccessFlagTarget.*
import java.util.*

internal fun accessFlagsToSet(accessFlags: Int, target: AccessFlagTarget): EnumSet<AccessFlag> {
    val result = EnumSet.noneOf(AccessFlag::class.java)
    val flagsOfTarget = AccessFlag.flagsByTarget(target)
    result.addAll(flagsOfTarget.filter { accessFlags and it.value != 0 })
    return result
}

enum class AccessFlagTarget(val value: Int) {
    CLASS           (0x01),
    FIELD           (0x02),
    METHOD          (0x04),
    INNER_CLASS     (0x08),
    MODULE          (0x10),
    REQUIRED_MODULE (0x20),
    EXPORTED_PACKAGE(0x40),
    OPENED_MODULE   (0x80);
}

enum class AccessFlag(val value: Int, val synthetic: Boolean, private val target: Int) {

    PUBLIC                          (ACC_PUBLIC,       false, arrayOf(CLASS, FIELD, METHOD, INNER_CLASS)),
    PRIVATE                         (ACC_PRIVATE,      false, arrayOf(FIELD, METHOD, INNER_CLASS)),
    PROTECTED                       (ACC_PROTECTED,    false, arrayOf(FIELD, METHOD, INNER_CLASS)),
    STATIC                          (ACC_STATIC,       false, arrayOf(FIELD, METHOD, INNER_CLASS)),
    FINAL                           (ACC_FINAL,        false, arrayOf(CLASS, FIELD, METHOD, INNER_CLASS)),
    SUPER                           (ACC_SUPER,        true,  arrayOf(CLASS)),
    SYNCHRONIZED                    (ACC_SYNCHRONIZED, false, arrayOf(METHOD)),
    OPEN                            (ACC_OPEN,         false, arrayOf(AccessFlagTarget.MODULE)),
    TRANSITIVE                      (ACC_TRANSITIVE,   false, arrayOf(REQUIRED_MODULE)),
    VOLATILE                        (ACC_VOLATILE,     false, arrayOf(FIELD)),
    BRIDGE                          (ACC_BRIDGE,       true,  arrayOf(METHOD)),
    STATIC_PHASE                    (ACC_STATIC_PHASE, false, arrayOf(REQUIRED_MODULE)),
    VARARGS                         (ACC_VARARGS,      true,  arrayOf(METHOD)),
    NATIVE                          (ACC_NATIVE,       false, arrayOf(METHOD)),
    TRANSIENT                       (ACC_TRANSIENT,    false, arrayOf(FIELD)),
    INTERFACE                       (ACC_INTERFACE,    false, arrayOf(CLASS, INNER_CLASS)),
    ABSTRACT                        (ACC_ABSTRACT,     false, arrayOf(CLASS, METHOD, INNER_CLASS)),
    STRICT                          (ACC_STRICT,       false, arrayOf(METHOD)),
    SYNTHETIC                       (ACC_SYNTHETIC,    true,  arrayOf(CLASS, FIELD, METHOD, INNER_CLASS, AccessFlagTarget.MODULE,
                                                                      REQUIRED_MODULE, EXPORTED_PACKAGE, OPENED_MODULE)),
    ANNOTATION                      (ACC_ANNOTATION,   true,  arrayOf(CLASS, INNER_CLASS)),
    ENUM                            (ACC_ENUM,         true,  arrayOf(CLASS, FIELD, INNER_CLASS)),
    MODULE                          (ACC_MODULE,       true,  arrayOf(CLASS)),
    MANDATED                        (ACC_MANDATED,     false, arrayOf(AccessFlagTarget.MODULE, REQUIRED_MODULE, EXPORTED_PACKAGE, OPENED_MODULE));

    fun matchesTarget(target: AccessFlagTarget): Boolean {
        return this.target and target.value != 0
    }

    constructor(value: Int, synthetic: Boolean, targets: Array<AccessFlagTarget>)
            : this(value, synthetic, targets.fold(0) { acc, t -> acc or t.value })

    companion object {
        fun flagsByTarget(target: AccessFlagTarget): Collection<AccessFlag> {
            return values().filter { it.matchesTarget(target) }
        }
    }
}

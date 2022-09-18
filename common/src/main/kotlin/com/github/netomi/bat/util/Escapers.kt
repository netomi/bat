/*
 * Copyright (C) 2006 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.netomi.bat.util

import java.util.*

/**
 * An object that converts literal text into a format safe for inclusion in a particular context
 * (such as an XML document). Typically (but not always), the inverse process of "unescaping" the
 * text is performed automatically by the relevant parser.
 *
 * For example, an XML escaper would convert the literal string `"Foo<Bar>"` into `"Foo&lt;Bar&gt;"` to prevent `"<Bar>"` from being confused with an XML tag. When the
 * resulting XML document is parsed, the parser API will return this text as the original literal
 * string `"Foo<Bar>"`.
 *
 * A `CharEscaper` instance is required to be stateless, and safe when used concurrently by
 * multiple threads.
 *
 * Popular escapers are defined as constants in classes like [ ] and [com.google.common.xml.XmlEscapers]. To create
 * your own escapers extend this class and implement the [.escape] method.
 *
 * @author Sven Mawson
 */
internal abstract class CharEscaper protected constructor() {
    /**
     * Returns the escaped form of a given literal string.
     *
     * @param string the literal string to be escaped
     * @return the escaped form of `string`
     * @throws NullPointerException if `string` is null
     */
    open fun escape(string: String): String {
        // Inlineable fast-path loop which hands off to escapeSlow() only if needed
        val length = string.length
        for (index in 0 until length) {
            if (escape(string[index]) != null) {
                return escapeSlow(string, index)
            }
        }
        return string
    }

    /**
     * Returns the escaped form of the given character, or `null` if this character does not
     * need to be escaped. If an empty array is returned, this effectively strips the input character
     * from the resulting text.
     *
     * If the character does not need to be escaped, this method should return `null`, rather
     * than a one-character array containing the character itself. This enables the escaping algorithm
     * to perform more efficiently.
     *
     * An escaper is expected to be able to deal with any `char` value, so this method should
     * not throw any exceptions.
     *
     * @param c the character to escape if necessary
     * @return the replacement characters, or `null` if no escaping was needed
     */
    protected abstract fun escape(c: Char): CharArray?

    /**
     * Returns the escaped form of a given literal string, starting at the given index. This method is
     * called by the [.escape] method when it discovers that escaping is required. It is
     * protected to allow subclasses to override the fastpath escaping function to inline their
     * escaping test.
     *
     * @param s the literal string to be escaped
     * @param index the index to start escaping from
     * @return the escaped form of `string`
     * @throws NullPointerException if `string` is null
     */
    protected fun escapeSlow(s: String, index: Int): String {
        var index = index
        val slen = s.length

        // Get a destination buffer and setup some loop variables.
        var dest = charBufferFromThreadLocal()
        var destSize = dest.size
        var destIndex = 0
        var lastEscape = 0

        // Loop through the rest of the string, replacing when needed into the
        // destination buffer, which gets grown as needed as well.
        while (index < slen) {
            // Get a replacement for the current character.
            val r = escape(s[index])

            // If no replacement is needed, just continue.
            if (r == null) {
                index++
                continue
            }
            val rlen = r.size
            val charsSkipped = index - lastEscape

            // This is the size needed to add the replacement, not the full size
            // needed by the string. We only regrow when we absolutely must, and
            // when we do grow, grow enough to avoid excessive growing. Grow.
            val sizeNeeded = destIndex + charsSkipped + rlen
            if (destSize < sizeNeeded) {
                destSize = sizeNeeded + DEST_PAD_MULTIPLIER * (slen - index)
                dest = growBuffer(dest, destIndex, destSize)
            }

            // If we have skipped any characters, we need to copy them now.
            if (charsSkipped > 0) {
                s.toCharArray(dest, destIndex, lastEscape, index)
                destIndex += charsSkipped
            }

            // Copy the replacement string into the dest buffer as needed.
            if (rlen > 0) {
                System.arraycopy(r, 0, dest, destIndex, rlen)
                destIndex += rlen
            }
            lastEscape = index + 1
            index++
        }

        // Copy leftover characters if there are any.
        val charsLeft = slen - lastEscape
        if (charsLeft > 0) {
            val sizeNeeded = destIndex + charsLeft
            if (destSize < sizeNeeded) {
                // Regrow and copy, expensive! No padding as this is the final copy.
                dest = growBuffer(dest, destIndex, sizeNeeded)
            }
            s.toCharArray(dest, destIndex, lastEscape, slen)
            destIndex = sizeNeeded
        }
        return String(dest, 0, destIndex)
    }

    companion object {
        /**
         * Helper method to grow the character buffer as needed, this only happens once in a while so it's
         * ok if it's in a method call. If the index passed in is 0 then no copying will be done.
         */
        private fun growBuffer(dest: CharArray, index: Int, size: Int): CharArray {
            if (size < 0) { // overflow - should be OutOfMemoryError but GWT/j2cl don't support it
                throw AssertionError("Cannot increase internal buffer any further")
            }
            val copy = CharArray(size)
            if (index > 0) {
                System.arraycopy(dest, 0, copy, 0, index)
            }
            return copy
        }

        /** The multiplier for padding to use when growing the escape buffer.  */
        private const val DEST_PAD_MULTIPLIER = 2

        private fun charBufferFromThreadLocal(): CharArray {
            return DEST_TL.get()
        }

        /**
         * A thread-local destination buffer to keep us from creating new buffers. The starting size is
         * 1024 characters. If we grow past this we don't put it back in the threadlocal, we just keep
         * going and grow as needed.
         */
        private val DEST_TL: ThreadLocal<CharArray> = object : ThreadLocal<CharArray>() {
            override fun initialValue(): CharArray {
                return CharArray(1024)
            }
        }
    }
}


/**
 * An implementation-specific parameter class suitable for initializing [ ] or [ArrayBasedUnicodeEscaper] instances.
 * This class should be used when more than one escaper is created using the same character replacement mapping
 * to allow the underlying (implementation specific) data structures to be shared.
 *
 * The size of the data structure used by ArrayBasedCharEscaper and ArrayBasedUnicodeEscaper is
 * proportional to the highest valued character that has a replacement. For example a replacement
 * map containing the single character '\u1000' will require approximately 16K of memory.
 * As such sharing this data structure between escaper instances is the primary goal of this class.
 *
 * @author David Beaumont
 */
internal class ArrayBasedEscaperMap private constructor(val replacementArray: Array<CharArray?>) {

    companion object {
        /**
         * Returns a new ArrayBasedEscaperMap for creating ArrayBasedCharEscaper or
         * ArrayBasedUnicodeEscaper instances.
         *
         * @param replacements a map of characters to their escaped representations
         */
        fun create(replacements: Map<Char, String>): ArrayBasedEscaperMap {
            return ArrayBasedEscaperMap(createReplacementArray(replacements))
        }

        // Creates a replacement array from the given map. The returned array is a
        // linear lookup table of replacement character sequences indexed by the
        // original character value.
        fun createReplacementArray(map: Map<Char, String>): Array<CharArray?> {
            if (map.isEmpty()) {
                return EMPTY_REPLACEMENT_ARRAY
            }
            val max = Collections.max(map.keys)
            val replacements = arrayOfNulls<CharArray>(max.code + 1)
            for (entry in map) {
                val c = entry.key
                replacements[c.code] = entry.value.toCharArray()
            }
            return replacements
        }

        // Immutable empty array for when there are no replacements.
        private val EMPTY_REPLACEMENT_ARRAY = Array<CharArray?>(0) { CharArray(0) }
    }
}


/**
 * A [CharEscaper] that uses an array to quickly look up replacement characters for a given
 * `char` value. An additional safe range is provided that determines whether `char`
 * values without specific replacements are to be considered safe and left unescaped or should be
 * escaped in a general way.
 *
 *
 * A good example of usage of this class is for Java source code escaping where the replacement
 * array contains information about special ASCII characters such as `\\t` and `\\n`
 * while [.escapeUnsafe] is overridden to handle general escaping of the form `\\uxxxx`.
 *
 *
 * The size of the data structure used by [ArrayBasedCharEscaper] is proportional to the
 * highest valued character that requires escaping. For example a replacement map containing the
 * single character '`\``u1000`' will require approximately 16K of memory. If you need
 * to create multiple escaper instances that have the same character replacement mapping consider
 * using [ArrayBasedEscaperMap].
 *
 * @author Sven Mawson
 * @author David Beaumont
 */
internal abstract class ArrayBasedCharEscaper protected constructor(escaperMap: ArrayBasedEscaperMap, safeMin: Char, safeMax: Char) : CharEscaper() {
    // The replacement array (see ArrayBasedEscaperMap).
    private val replacements: Array<CharArray?>

    // The number of elements in the replacement array.
    private val replacementsLength: Int

    // The first character in the safe range.
    private val safeMin: Char

    // The last character in the safe range.
    private val safeMax: Char

    /**
     * Creates a new ArrayBasedCharEscaper instance with the given replacement map and specified safe
     * range. If `safeMax < safeMin` then no characters are considered safe.
     *
     * If a character has no mapped replacement then it is checked against the safe range. If it
     * lies outside that, then [.escapeUnsafe] is called, otherwise no escaping is performed.
     *
     * @param replacementMap a map of characters to their escaped representations
     * @param safeMin the lowest character value in the safe range
     * @param safeMax the highest character value in the safe range
     */
    protected constructor(replacementMap: Map<Char, String>, safeMin: Char, safeMax: Char)
            : this(ArrayBasedEscaperMap.create(replacementMap), safeMin, safeMax)

    /**
     * Creates a new ArrayBasedCharEscaper instance with the given replacement map and specified safe
     * range. If `safeMax < safeMin` then no characters are considered safe. This initializer is
     * useful when explicit instances of ArrayBasedEscaperMap are used to allow the sharing of large
     * replacement mappings.
     *
     *
     * If a character has no mapped replacement then it is checked against the safe range. If it
     * lies outside that, then [.escapeUnsafe] is called, otherwise no escaping is performed.
     *
     * @param escaperMap the mapping of characters to be escaped
     * @param safeMin the lowest character value in the safe range
     * @param safeMax the highest character value in the safe range
     */
    init {
        var safeMinVar = safeMin
        var safeMaxVar = safeMax
        replacements = escaperMap.replacementArray
        replacementsLength = replacements.size
        if (safeMaxVar < safeMinVar) {
            // If the safe range is empty, set the range limits to opposite extremes
            // to ensure the first test of either value will (almost certainly) fail.
            safeMaxVar = Character.MIN_VALUE
            safeMinVar = Character.MAX_VALUE
        }
        this.safeMin = safeMinVar
        this.safeMax = safeMaxVar
    }

    /**
     * This is overridden to improve performance. Rough benchmarking shows that this almost doubles
     * the speed when processing strings that do not require any escaping.
     */
    override fun escape(s: String): String {
        for (i in s.indices) {
            val c = s[i]
            if (c.code < replacementsLength && replacements[c.code] != null || c > safeMax || c < safeMin) {
                return escapeSlow(s, i)
            }
        }
        return s
    }

    /**
     * Escapes a single character using the replacement array and safe range values. If the given
     * character does not have an explicit replacement and lies outside the safe range then [ ][.escapeUnsafe] is called.
     *
     * @return the replacement characters, or `null` if no escaping was required
     */
    override fun escape(c: Char): CharArray? {
        if (c.code < replacementsLength) {
            val chars = replacements[c.code]
            if (chars != null) {
                return chars
            }
        }
        return if (c in safeMin..safeMax) {
            null
        } else escapeUnsafe(c)
    }

    /**
     * Escapes a `char` value that has no direct explicit value in the replacement array and
     * lies outside the stated safe range. Subclasses should override this method to provide
     * generalized escaping for characters.
     *
     * Note that arrays returned by this method must not be modified once they have been returned.
     * However, it is acceptable to return the same array multiple times (even for different input
     * characters).
     *
     * @param c the character to escape
     * @return the replacement characters, or `null` if no escaping was required
     */
    // TODO(dbeaumont,cpovirk): Rename this something better once refactoring done
    protected abstract fun escapeUnsafe(c: Char): CharArray?
}

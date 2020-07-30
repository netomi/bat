/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.netomi.bat.util;

import java.util.Arrays;

/**
 * Implements a growing array of int primitives.
 */
public class IntArray
implements   Cloneable
{
    private static final int MIN_CAPACITY_INCREMENT = 12;

    private int[] values;
    private int   size;

    private  IntArray(int[] array, int size) {
        this.values = array;
        this.size   = Preconditions.checkArgumentInRange(size, 0, array.length, "size");
    }

    /**
     * Creates an empty IntArray with the default initial capacity.
     */
    public IntArray() {
        this(10);
    }

    /**
     * Creates an empty IntArray with the specified initial capacity.
     */
    public IntArray(int initialCapacity) {
        if (initialCapacity == 0) {
            values = EmptyArray.INT;
        } else {
            values = new int[initialCapacity];
        }
        size = 0;
    }

    /**
     * Creates an IntArray wrapping the given primitive int array.
     */
    public static IntArray wrap(int[] array) {
        return new IntArray(array, array.length);
    }

    /**
     * Creates an IntArray from the given primitive int array, copying it.
     */
    public static IntArray fromArray(int[] array, int size) {
        return wrap(Arrays.copyOf(array, size));
    }

    /**
     * Changes the size of this IntArray. If this IntArray is shrinked, the backing array capacity
     * is unchanged. If the new size is larger than backing array capacity, a new backing array is
     * created from the current content of this IntArray padded with 0s.
     */
    public void resize(int newSize) {
        Preconditions.checkArgumentNonnegative(newSize);
        if (newSize <= values.length) {
            Arrays.fill(values, newSize, values.length, 0);
        } else {
            ensureCapacity(newSize - size);
        }
        size = newSize;
    }

    /**
     * Appends the specified value to the end of this array.
     */
    public void add(int value) {
        add(size, value);
    }

    /**
     * Inserts a value at the specified position in this array. If the specified index is equal to
     * the length of the array, the value is added at the end.
     *
     * @throws IndexOutOfBoundsException when index &lt; 0 || index &gt; size()
     */
    public void add(int index, int value) {
        ensureCapacity(1);
        int rightSegment = size - index;
        size++;
        ArrayUtil.checkBounds(size, index);
        if (rightSegment != 0) {
            // Move by 1 all values from the right of 'index'
            System.arraycopy(values, index, values, index + 1, rightSegment);
        }
        values[index] = value;
    }

    /**
     * Adds the values in the specified array to this array.
     */
    public void addAll(IntArray values) {
        final int count = values.size;
        ensureCapacity(count);
        System.arraycopy(values.values, 0, values, size, count);
        size += count;
    }

    /**
     * Ensures capacity to append at least <code>count</code> values.
     */
    private void ensureCapacity(int count) {
        final int currentSize = size;
        final int minCapacity = currentSize + count;
        if (minCapacity >= values.length) {
            final int targetCap = currentSize + (currentSize < (MIN_CAPACITY_INCREMENT / 2) ?
                    MIN_CAPACITY_INCREMENT : currentSize >> 1);
            final int newCapacity = targetCap > minCapacity ? targetCap : minCapacity;
            final int[] newValues = new int[newCapacity];
            System.arraycopy(values, 0, newValues, 0, currentSize);
            values = newValues;
        }
    }

    /**
     * Removes all values from this array.
     */
    public void clear() {
        size = 0;
    }

    @Override
    public IntArray clone() throws CloneNotSupportedException {
        final IntArray clone = (IntArray) super.clone();
        clone.values = values.clone();
        return clone;
    }

    /**
     * Returns the value at the specified position in this array.
     */
    public int get(int index) {
        ArrayUtil.checkBounds(size, index);
        return values[index];
    }

    /**
     * Sets the value at the specified position in this array.
     */
    public void set(int index, int value) {
        ArrayUtil.checkBounds(size, index);
        values[index] = value;
    }

    /**
     * Returns the index of the first occurrence of the specified value in this
     * array, or -1 if this array does not contain the value.
     */
    public int indexOf(int value) {
        final int n = size;
        for (int i = 0; i < n; i++) {
            if (values[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes the value at the specified index from this array.
     */
    public void remove(int index) {
        ArrayUtil.checkBounds(size, index);
        System.arraycopy(values, index + 1, values, index, size - index - 1);
        size--;
    }

    /**
     * Returns the number of values in this array.
     */
    public int size() {
        return size;
    }

    /**
     * Returns a new array with the contents of this IntArray.
     */
    public int[] toArray() {
        return Arrays.copyOf(values, size);
    }

    @Override
    public String toString() {
        return Arrays.toString(toArray());
    }
}

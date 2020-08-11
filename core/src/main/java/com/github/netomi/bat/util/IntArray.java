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

import java.util.Objects;

/**
 * Implements a growing array of int primitives.
 */
public class IntArray
implements   Cloneable
{
    private static final int MIN_CAPACITY_INCREMENT = 12;

    private int   size;
    private int[] values;

    private  IntArray(int[] array, int size) {
        this.size   = Preconditions.checkArgumentInRange(size, 0, array.length, "size");
        this.values = array;
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
        size = 0;
        if (initialCapacity == 0) {
            values = EmptyArray.INT;
        } else {
            values = new int[initialCapacity];
        }
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
        return wrap(java.util.Arrays.copyOf(array, size));
    }

    /**
     * Changes the size of this IntArray. If this IntArray is shrunk, the backing array capacity
     * is unchanged. If the new size is larger than backing array capacity, a new backing array is
     * created from the current content of this IntArray padded with 0s.
     */
    public void resize(int newSize) {
        Preconditions.checkArgumentNonnegative(newSize);
        if (newSize <= values.length) {
            java.util.Arrays.fill(values, newSize, values.length, 0);
        } else {
            ensureCapacity(newSize - size);
        }
        size = newSize;
    }

    /**
     * Inserts the given value into this array in sorted order.
     * If the value is already present, it is not added.
     * <p>
     * If the array is not sorted, the behaviour is undefined.
     *
     * @param value  the value to be added
     */
    public void insert(int value) {
        int index = java.util.Arrays.binarySearch(values, 0, size, value);
        if (index < 0) {
            int insertionPoint = -(index + 1);
            add(insertionPoint, value);
        }
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
        Arrays.checkBounds(size, index);
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
        Arrays.checkBounds(size, index);
        return values[index];
    }

    /**
     * Sets the value at the specified position in this array.
     */
    public void set(int index, int value) {
        Arrays.checkBounds(size, index);
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
        Arrays.checkBounds(size, index);
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
        return java.util.Arrays.copyOf(values, size);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntArray other = (IntArray) o;

        if (size != other.size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (values[i] != other.values[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * Objects.hash(size);

        for (int i = 0; i < size; i++) {
            result = 31 * result + values[i];
        }

        return result;
    }

    @Override
    public String toString() {
        return java.util.Arrays.toString(toArray());
    }
}

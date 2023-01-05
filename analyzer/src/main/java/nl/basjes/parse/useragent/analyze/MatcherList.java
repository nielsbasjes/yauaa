/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basjes.parse.useragent.analyze;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

@DefaultSerializer(MatcherList.KryoSerializer.class)
public final class MatcherList implements Collection<Matcher>, Serializable {

    private int size;
    private int maxSize;

    private Matcher[] allElements;

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private MatcherList() {
    }

    public static class KryoSerializer extends FieldSerializer<MatcherList> {
        public KryoSerializer(Kryo kryo, Class<?> type) {
            super(kryo, type);
        }

        @Override
        public void write(Kryo kryo, Output output, MatcherList object) {
            if (object.size > 0) {
                throw new IllegalStateException("Cannot serialize MatcherList with a non-zero size.");
            }

            output.write(object.maxSize);
        }

        @Override
        public MatcherList read(Kryo kryo, Input input, Class<? extends MatcherList> type) {
            MatcherList matcherList = new MatcherList();
            matcherList.maxSize = input.read();

            // Note: The matches list is always empty after deserialization.
            matcherList.initialize();

            return matcherList;
        }

    }

    public MatcherList(int newMaxSize) {
        maxSize = newMaxSize;
        initialize();
    }

    private void initialize() {
        size = 0;
        allElements = new Matcher[maxSize];
        for (int i = 0; i < maxSize; i++) {
            allElements[i] = null;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        size = 0;
    }

    public boolean add(Matcher matcher) {
        if (size >= maxSize) {
            increaseCapacity();
        }

        allElements[size] = matcher;
        size++;
        return true;
    }

    @Nonnull
    @Override
    public Iterator<Matcher> iterator() {
        return new Iterator<Matcher>() {
            int offset = 0;

            @Override
            public boolean hasNext() {
                return offset < size;
            }

            @Override
            public Matcher next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("Array index out of bounds");
                }
                return allElements[offset++];
            }
        };
    }

    private static final int CAPACITY_INCREASE = 3;

    private void increaseCapacity() {
        int newMaxSize = maxSize+ CAPACITY_INCREASE;
        Matcher[] newAllElements = new Matcher[newMaxSize];
        System.arraycopy(allElements, 0, newAllElements, 0, maxSize);
        for (int i = maxSize; i < newMaxSize; i++) {
            newAllElements[i] = null;
        }
        allElements = newAllElements;
        maxSize = newMaxSize;
    }

    // Needed for debugging
    @Nonnull
    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.allElements, this.size);
    }

// ============================================================
// Everything else is NOT supported
// ============================================================

    @Override
    public boolean addAll(@Nonnull Collection<? extends Matcher> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(@Nonnull T[] ts) {
        throw new UnsupportedOperationException();
    }
}

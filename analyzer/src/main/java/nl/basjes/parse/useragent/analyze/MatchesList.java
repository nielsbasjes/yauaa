/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2025 Niels Basjes
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
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@DefaultSerializer(MatchesList.KryoSerializer.class)
public final class MatchesList implements Collection<MatchesList.Match>, Serializable {

    public static final class Match implements Serializable {
        private String key;
        private String value;
        private transient ParseTree result;

        public Match(String key, String value, ParseTree result) {
            fill(key, value, result);
        }

        public void fill(String nKey, String nValue, ParseTree nResult) {
            this.key = nKey;
            this.value = nValue;
            this.result = nResult;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public ParseTree getResult() {
            return result;
        }
    }

    private int maxSize;

    private transient int size;
    private transient Match[] allElements;

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private MatchesList() {
    }

    public static class KryoSerializer extends FieldSerializer<MatchesList> {
        public KryoSerializer(Kryo kryo, Class<?> type) {
            super(kryo, type);
        }

        @Override
        public void write(Kryo kryo, Output output, MatchesList object) {
            output.write(object.maxSize);
        }

        @Override
        public MatchesList read(Kryo kryo, Input input, Class<? extends MatchesList> type) {
            MatchesList matchesList = new MatchesList();
            matchesList.maxSize = input.read();

            // Note: The matches list is always empty after deserialization.
            matchesList.initialize();

            return matchesList;
        }

    }

    private void readObject(java.io.ObjectInputStream stream)
        throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        initialize();
    }

    public MatchesList(int newMaxSize) {
        maxSize = newMaxSize;
        initialize();
    }

    private void initialize() {
        size = 0;
        allElements = new Match[maxSize];
        for (int i = 0; i < maxSize; i++) {
            allElements[i] = new Match(null, null, null);
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

    public void add(String key, String value, ParseTree result) {
        if (size >= maxSize) {
            increaseCapacity();
        }

        allElements[size].fill(key, value, result);
        size++;
    }

    @Nonnull
    @Override
    public Iterator<Match> iterator() {
        //noinspection Convert2Diamond
        return new Iterator<Match>() {
            int offset = 0;

            @Override
            public boolean hasNext() {
                return offset < size;
            }

            @Override
            public Match next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("Array index out of bounds");
                }
                return allElements[offset++];
            }
        };
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.allElements, this.size);
    }

    private static final int CAPACITY_INCREASE = 3;

    private void increaseCapacity() {
        int newMaxSize = maxSize+ CAPACITY_INCREASE;
        Match[] newAllElements = new Match[newMaxSize];
        System.arraycopy(allElements, 0, newAllElements, 0, maxSize);
        for (int i = maxSize; i < newMaxSize; i++) {
            newAllElements[i] = new Match(null, null, null);
        }
        allElements = newAllElements;
        maxSize = newMaxSize;
    }

    public List<String> toStrings() {
        List<String> result = new ArrayList<>(size);
        for (Match match: this) {
            result.add("{ \"" + match.key + "\"=\"" + match.value + "\" }");
        }
        return result;
    }

    public String toString() {
        return "MatchesList("+size+") " + toStrings();
    }

// ============================================================
// Everything else is NOT supported
// ============================================================

    @Override
    public boolean add(Match match) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends Match> collection) {
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

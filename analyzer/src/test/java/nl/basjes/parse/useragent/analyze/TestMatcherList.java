/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMatcherList {

    @Test
    public void testNormalUse() {
        MatcherList list = new MatcherList(1);
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        list.add(new Matcher(null));
        assertEquals(1, list.size());
        assertFalse(list.isEmpty());
        list.add(new Matcher(null));
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());
        Iterator<Matcher> iterator = list.iterator();
        assertTrue(iterator.hasNext());
        Matcher match1 = iterator.next();
        assertNotNull(match1);
        assertTrue(iterator.hasNext());
        Matcher match2 = iterator.next();
        assertNotNull(match2);
        assertFalse(iterator.hasNext());

        list.clear();

        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        list.add(new Matcher(null));
        assertEquals(1, list.size());
        assertFalse(list.isEmpty());
        list.add(new Matcher(null));
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());
        iterator = list.iterator();
        assertTrue(iterator.hasNext());
        match1 = iterator.next();
        assertNotNull(match1);
        assertTrue(iterator.hasNext());
        match2 = iterator.next();
        assertNotNull(match2);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testTooMany() {
        MatcherList list = new MatcherList(5);
        list.add(new Matcher(null));
        list.add(new Matcher(null));
        Iterator<Matcher> iterator = list.iterator();

        iterator.next(); // Ok
        iterator.next(); // Ok
        assertThrows(NoSuchElementException.class, iterator::next); // Should throw
    }

    @Test
    public void testToArray() {
        MatcherList list = new MatcherList(5);
        list.add(new Matcher(null));
        list.add(new Matcher(null));

        final Object[] array = list.toArray();
        assertNotNull(array);
        assertEquals(2, array.length);
        assertTrue(array[0] instanceof Matcher);
        assertTrue(array[1] instanceof Matcher);
    }

    // The MatcherList is currently only used as a transient member.
    // To ensure the serialization works we test that here.
    byte[] serialize(MatcherList list) {
        Kryo             kryo             = new Kryo();
        ByteBufferOutput byteBufferOutput = new ByteBufferOutput(1_000_000, -1);
        kryo.writeClassAndObject(byteBufferOutput, list);

        ByteBuffer buf = byteBufferOutput.getByteBuffer();
        byte[]     arr = new byte[buf.position()];
        buf.rewind();
        buf.get(arr);

        return arr;
    }

    MatcherList deserialize(byte[] bytes) {
        Kryo            kryo            = new Kryo();
        ByteBufferInput byteBufferInput = new ByteBufferInput(bytes);
        return (MatcherList) kryo.readClassAndObject(byteBufferInput);
    }

    @Test
    public void serializeAndDeserializeMatcherListNonEmpty() {
        MatcherList list = new MatcherList(5);
        list.add(new Matcher(null));

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            serialize(list);
        });

        assertEquals("Cannot serialize MatcherList with a non-zero size.", exception.getMessage());
    }

    @Test
    public void serializeAndDeserializeMatcherList() {
        MatcherList list = new MatcherList(5);
        list.add(new Matcher(null));
        list.add(new Matcher(null));

        final Object[] array = list.toArray();
        assertNotNull(array);
        assertEquals(2, array.length);
        assertTrue(array[0] instanceof Matcher);
        assertTrue(array[1] instanceof Matcher);

        list.clear();

        final byte[] bytes = serialize(list);

        MatcherList list2 = deserialize(bytes);
        final Object[] array2 = list2.toArray();
        assertNotNull(array2);
        assertEquals(0, array2.length);
    }

    @Test
    public void testUnsupportedAddAll() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatcherList(1).addAll(null));
    }

    @Test
    public void testUnsupportedRemove() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatcherList(1).remove(null));
    }

    @Test
    public void testUnsupportedRemoveAll() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatcherList(1).removeAll(null));
    }

    @Test
    public void testUnsupportedRetainAll() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatcherList(1).retainAll(null));
    }

    @Test
    public void testUnsupportedContains() {
        assertThrows(UnsupportedOperationException.class, () ->
            assertFalse(new MatcherList(1).contains(null)));
    }

    @Test
    public void testUnsupportedContainsAll() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatcherList(1).containsAll(null));
    }

    @Test
    public void testUnsupportedToArray() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatcherList(1).toArray(null));
    }
}

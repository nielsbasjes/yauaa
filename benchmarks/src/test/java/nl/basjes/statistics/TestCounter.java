/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

package nl.basjes.statistics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCounter {

    private static final String  BAD_COUNT         = "Bad count";
    private static final String  BAD_MIN           = "Bad value for Min";
    private static final String  BAD_MEAN          = "Bad value for Mean";
    private static final String  BAD_MAX           = "Bad value for Max";
    private static final String  BAD_SUM           = "Bad value for Sum";
    private static final String  BAD_VARIANCE      = "Bad value for Variance";
    private static final String  BAD_STDDEV        = "Bad value for stdDev";
    private static final String  BAD               = "Counter is bad";

    private static final double  MAX_ERROR         = 0.0001;

    private static final Counter REFERENCE_COUNTER = new Counter();

    static {
        REFERENCE_COUNTER.increment(1);
        REFERENCE_COUNTER.increment(2);
        REFERENCE_COUNTER.increment(3);
        REFERENCE_COUNTER.increment(0);
        REFERENCE_COUNTER.increment(10);
        REFERENCE_COUNTER.increment(20);
    }

    // ------------------------------------------

    @Test
    public void testEmptyCounter() {
        final Counter c1 = new Counter();

        assertEquals(BAD_COUNT,    0,          c1.getN());
        assertEquals(BAD_MIN,      Double.NaN, c1.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,     Double.NaN, c1.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,      Double.NaN, c1.getMax(),      MAX_ERROR);
        assertEquals(BAD_SUM,      Double.NaN, c1.getSum(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,   Double.NaN, c1.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE, Double.NaN, c1.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,   Double.NaN, c1.getStdDev(),   MAX_ERROR);

        final Counter nullCounter = null;
        c1.increment(nullCounter);
        assertEquals(BAD_COUNT,    0,          c1.getN());
        assertEquals(BAD_MIN,      Double.NaN, c1.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,     Double.NaN, c1.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,      Double.NaN, c1.getMax(),      MAX_ERROR);
        assertEquals(BAD_SUM,      Double.NaN, c1.getSum(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,   Double.NaN, c1.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE, Double.NaN, c1.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,   Double.NaN, c1.getStdDev(),   MAX_ERROR);

    }

    // ------------------------------------------

    @Test
    public void testCounterBasics() {
        final Counter c1 = new Counter();
        assertEquals(BAD_COUNT,      0,        c1.getN());

        c1.increment(10);

        assertEquals(BAD_COUNT,       1,        c1.getN());
        assertEquals(BAD_MIN,        10,        c1.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,       10,        c1.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,        10,        c1.getMax(),      MAX_ERROR);
        assertEquals(BAD_SUM,        10,        c1.getSum(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,      0,        c1.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE,    0,        c1.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,      0,        c1.getStdDev(),   MAX_ERROR);

        c1.increment(20);

        assertEquals(BAD_COUNT,       2,        c1.getN());
        assertEquals(BAD_MIN,        10,        c1.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,       15,        c1.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,        20,        c1.getMax(),      MAX_ERROR);
        assertEquals(BAD_SUM,        30,        c1.getSum(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,      7.07107,  c1.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE,   50,        c1.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,      7.07107,  c1.getStdDev(),   MAX_ERROR);

        c1.increment(70);

        assertEquals(BAD_COUNT,       3,        c1.getN());
        assertEquals(BAD_MIN,        10,        c1.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,       33.33333,  c1.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,        70,        c1.getMax(),      MAX_ERROR);
        assertEquals(BAD_SUM,       100,        c1.getSum(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,     32.14550,  c1.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE, 1033.33333,  c1.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,     32.14550,  c1.getStdDev(),   MAX_ERROR);
    }

    // ------------------------------------------

    @Test
    public void testCounterToString() {
        final Counter c1 = new Counter();
        c1.increment(1);
        c1.increment(2);
        c1.increment(3);

        assertEquals("{\"n\":3,\"m2\":2.0,\"sum\":6.0,\"min\":1.0,\"max\":3.0}", c1.toString());
    }

    // ------------------------------------------

    @Test
    public void testCounterMerging() {
        final Counter c1 = new Counter();
        c1.increment(1);
        c1.increment(2);
        c1.increment(3);

        assertEquals(BAD_COUNT,       3,      c1.getN());
        assertEquals(BAD_MIN,         1,      c1.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,        2,      c1.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,         3,      c1.getMax(),      MAX_ERROR);
        assertEquals(BAD_SUM,         6,      c1.getSum(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,      1,      c1.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE,    1,      c1.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,      1,      c1.getStdDev(),   MAX_ERROR);

        final Counter c2 = new Counter();
        c2.increment(0);
        assertEquals(BAD_COUNT,       1,      c2.getN());
        assertEquals(BAD_MIN,         0,      c2.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,        0,      c2.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,         0,      c2.getMax(),      MAX_ERROR);
        assertEquals(BAD_SUM,         0,      c2.getSum(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,      0,      c2.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE,    0,      c2.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,      0,      c2.getStdDev(),   MAX_ERROR);

        c2.increment(10);
        assertEquals(BAD_COUNT,       2,      c2.getN());
        assertEquals(BAD_MIN,         0,      c2.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,        5,      c2.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,        10,      c2.getMax(),      MAX_ERROR);
        assertEquals(BAD_SUM,        10,      c2.getSum(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,      7.0711, c2.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE,   50,      c2.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,      7.0711, c2.getStdDev(),   MAX_ERROR);

        c2.increment(20);
        assertEquals(BAD_COUNT,       3,      c2.getN());
        assertEquals(BAD_MIN,         0,      c2.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,       10,      c2.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,        20,      c2.getMax(),      MAX_ERROR);
        assertEquals(BAD_SUM,        30,      c2.getSum(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,     10,      c2.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE,  100,      c2.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,     10,      c2.getStdDev(),   MAX_ERROR);

        c1.increment(c2);
        assertEquals(BAD_COUNT,       6,      c1.getN());
        assertEquals(BAD_MIN,         0,      c1.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,        6,      c1.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,        20,      c1.getMax(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,      7.7201, c1.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE,   59.6,    c1.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,      7.7201, c1.getStdDev(),   MAX_ERROR);

        // SELF MERGE MUST WORK!!
        // (Note that the variance of 1,2,3,0,10,20
        //          is different from 1,2,3,0,10,20,1,2,3,0,10,20)
        c1.increment(c1);
        assertEquals(BAD_COUNT,      12,      c1.getN());
        assertEquals(BAD_MIN,         0,      c1.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,        6,      c1.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,        20,      c1.getMax(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,      7.3608, c1.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE,   54.1818, c1.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,      7.3608, c1.getStdDev(),   MAX_ERROR);

        // DOUBLE SELF MERGE MUST WORK!!
        // (Note that the variance of
        //  1,2,3,0,10,20,1,2,3,0,10,20
        //  is different from
        //  1,2,3,0,10,20,1,2,3,0,10,20,1,2,3,0,10,20,1,2,3,0,10,20)
        c1.increment(c1);
        assertEquals(BAD_COUNT,      24,      c1.getN());
        assertEquals(BAD_MIN,         0,      c1.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,        6,      c1.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,        20,      c1.getMax(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,      7.1990, c1.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE,   51.8261, c1.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,      7.1990, c1.getStdDev(),   MAX_ERROR);
    }

    // ------------------------------------------

    @Test
    public void testCounterMergingLeft() {
        final Counter c1 = new Counter();
        c1.increment(1);
        c1.increment(2);
        c1.increment(3);

        final Counter c2 = new Counter();
        c2.increment(0);
        c2.increment(10);
        c2.increment(20);

        c1.increment(c2);

        assertTrue(BAD, counterIsSameAsReferenceCounter(c1));
    }

    // ------------------------------------------

    @Test
    public void testCounterMergingRight() {
        final Counter c1 = new Counter();
        c1.increment(0);
        c1.increment(10);
        c1.increment(20);

        final Counter c2 = new Counter();
        c2.increment(1);
        c2.increment(2);
        c2.increment(3);

        c1.increment(c2);

        assertTrue(BAD, counterIsSameAsReferenceCounter(c1));
    }

    // ------------------------------------------

    @Test
    public void testCounterMergeAddAdd() {
        final Counter c1 = new Counter();
        c1.increment(1);
        c1.increment(2);

        final Counter c2 = new Counter();
        c2.increment(0);
        c2.increment(10);

        c1.increment(c2);
        c1.increment(3);
        c1.increment(20);

        assertTrue(BAD, counterIsSameAsReferenceCounter(c1));
    }

    // ------------------------------------------

    @Test
    public void testCounterMergeMerge() {
        final Counter c1 = new Counter();
        c1.increment(0);
        c1.increment(10);

        final Counter c2 = new Counter();
        c2.increment(20);
        c2.increment(1);

        final Counter c3 = new Counter();
        c3.increment(2);
        c3.increment(3);

        c1.increment(c2);
        c1.increment(c3);

        assertTrue(BAD, counterIsSameAsReferenceCounter(c1));
    }

    // ------------------------------------------

    @Test
    public void testCounterMergeAddMergeAdd() {
        final Counter c1 = new Counter();

        final Counter c2 = new Counter();
        c2.increment(20);
        c2.increment(1);

        final Counter c3 = new Counter();
        c3.increment(2);
        c3.increment(3);

        c1.increment(c2);
        c1.increment(0);
        c1.increment(c3);
        c1.increment(10);

        assertTrue(BAD, counterIsSameAsReferenceCounter(c1));
    }

    // ------------------------------------------

    @Test
    public void testCounterMergeAddMergeAddMergeAdd() {
        final Counter c1 = new Counter();

        final Counter c2 = new Counter();
        c2.increment(20);

        final Counter c3 = new Counter();
        c3.increment(1);

        final Counter c4 = new Counter();
        c4.increment(2);

        final Counter c5 = new Counter();
        c5.increment(3);

        c1.increment(c2);
        c1.increment(c3);
        c1.increment(0);
        c1.increment(c4);
        c1.increment(10);
        c1.increment(c5);

        assertTrue(BAD, counterIsSameAsReferenceCounter(c1));
    }

    // ------------------------------------------

    @Test
    public void testCounterMergeAddMergeAddMergeAddAsBytes() {
        final Counter c1 = new Counter();

        final Counter c2 = new Counter();
        c2.increment(20);

        final Counter c3 = new Counter();
        c3.increment(1);

        final Counter c4 = new Counter();
        c4.increment(2);

        final Counter c5 = new Counter();
        c5.increment(3);

        c1.increment(c2.toBytes());
        c1.increment(c3.toBytes());
        c1.increment(0);
        c1.increment(c4.toBytes());
        c1.increment(10);
        c1.increment(c5.toBytes());

        assertTrue(BAD, counterIsSameAsReferenceCounter(c1));
    }

    // ------------------------------------------

    @Test
    public void testCounterMergingEmptyRight() {
        final Counter c1 = new Counter();
        c1.increment(1);
        c1.increment(2);
        c1.increment(3);
        c1.increment(0);
        c1.increment(10);
        c1.increment(20);

        final Counter c2 = new Counter();

        c1.increment(c2);

        assertTrue(BAD, counterIsSameAsReferenceCounter(c1));
    }

    // ------------------------------------------

    @Test
    public void testCounterMergingEmptyLeft() {
        final Counter c1 = new Counter();

        final Counter c2 = new Counter();
        c2.increment(1);
        c2.increment(2);
        c2.increment(3);
        c2.increment(0);
        c2.increment(10);
        c2.increment(20);

        c1.increment(c2);

        assertTrue(BAD, counterIsSameAsReferenceCounter(c1));
    }

    // ------------------------------------------

    @Test
    public void testCounterMergingUp() {
        final Counter c = new Counter();

        c.increment(0);
        c.increment(1);
        c.increment(2);
        c.increment(3);
        c.increment(10);
        c.increment(20);

        assertTrue(BAD, counterIsSameAsReferenceCounter(c));
    }

    // ------------------------------------------

    @Test
    public void testCounterMergingDown() {
        final Counter c = new Counter();

        c.increment(20);
        c.increment(10);
        c.increment(3);
        c.increment(2);
        c.increment(1);
        c.increment(0);

        assertTrue(BAD, counterIsSameAsReferenceCounter(c));
    }

    // ------------------------------------------

    @Test
    public void testCounterSerialization() {
        byte[] serializedBytes = REFERENCE_COUNTER.toBytes();
        Counter deserialized = new Counter(serializedBytes);
        assertTrue(BAD, counterIsSameAsReferenceCounter(deserialized));
    }

    // ------------------------------------------
//
//    @Test
//    public void testCounterWritability() throws IOException {
//        byte[] bytes = TestWritableInterface.serialize(REFERENCE_COUNTER);
//        Counter deserialized = TestWritableInterface.asWritable(bytes, Counter.class);
//        assertTrue(BAD, counterIsSameAsReferenceCounter(deserialized));
//    }

    // ------------------------------------------

    private boolean counterIsSameAsReferenceCounter(final Counter c) {
        assertEquals(BAD_COUNT,    REFERENCE_COUNTER.getN(),         c.getN());
        assertEquals(BAD_MIN,      REFERENCE_COUNTER.getMin(),       c.getMin(),      MAX_ERROR);
        assertEquals(BAD_MEAN,     REFERENCE_COUNTER.getMean(),      c.getMean(),     MAX_ERROR);
        assertEquals(BAD_MAX,      REFERENCE_COUNTER.getMax(),       c.getMax(),      MAX_ERROR);
        assertEquals(BAD_SUM,      REFERENCE_COUNTER.getSum(),       c.getSum(),      MAX_ERROR);
        assertEquals(BAD_STDDEV,   REFERENCE_COUNTER.getStdDev(),    c.getStdDev(),   MAX_ERROR);
        assertEquals(BAD_VARIANCE, REFERENCE_COUNTER.getVariance(),  c.getVariance(), MAX_ERROR);
        assertEquals(BAD_STDDEV,   REFERENCE_COUNTER.getStdDev(),    c.getStdDev(),   MAX_ERROR);
        return true;
    }

}

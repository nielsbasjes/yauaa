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

package nl.basjes.statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// CHECKSTYLE.OFF: ParenPad
class TestCounter {

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
    void testEmptyCounter() {
        final Counter c1 = new Counter();

        assertEquals(0,          c1.getN(),                   BAD_COUNT    );
        assertEquals(Double.NaN, c1.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(Double.NaN, c1.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(Double.NaN, c1.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals(Double.NaN, c1.getSum(),      MAX_ERROR, BAD_SUM      );
        assertEquals(Double.NaN, c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(Double.NaN, c1.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(Double.NaN, c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );

        c1.increment((Counter)null);
        assertEquals(0,          c1.getN(),                   BAD_COUNT    );
        assertEquals(Double.NaN, c1.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(Double.NaN, c1.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(Double.NaN, c1.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals(Double.NaN, c1.getSum(),      MAX_ERROR, BAD_SUM      );
        assertEquals(Double.NaN, c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(Double.NaN, c1.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(Double.NaN, c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );

    }

    // ------------------------------------------

    @Test
    void testCounterBasics() {
        final Counter c1 = new Counter();
        assertEquals(   0,        c1.getN(),                   BAD_COUNT    );

        c1.increment(10);

        assertEquals(   1,        c1.getN(),                   BAD_COUNT    );
        assertEquals(  10,        c1.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(  10,        c1.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(  10,        c1.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals(  10,        c1.getSum(),      MAX_ERROR, BAD_SUM      );
        assertEquals(   0,        c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(   0,        c1.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(   0,        c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );

        c1.increment(20);

        assertEquals(   2,        c1.getN(),                   BAD_COUNT    );
        assertEquals(  10,        c1.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(  15,        c1.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(  20,        c1.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals(  30,        c1.getSum(),      MAX_ERROR, BAD_SUM      );
        assertEquals(   7.07107,  c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(  50,        c1.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(   7.07107,  c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );

        c1.increment(70);

        assertEquals(   3,        c1.getN(),                   BAD_COUNT    );
        assertEquals(  10,        c1.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(  33.33333,  c1.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(  70,        c1.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals( 100,        c1.getSum(),      MAX_ERROR, BAD_SUM      );
        assertEquals(  32.14550,  c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(1033.33333,  c1.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(  32.14550,  c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
    }

    // ------------------------------------------

    @Test
    void testCounterToString() {
        final Counter c1 = new Counter();
        c1.increment(1);
        c1.increment(2);
        c1.increment(3);

        assertEquals("{\"n\":3,\"m2\":2.0,\"sum\":6.0,\"min\":1.0,\"max\":3.0}", c1.toString());
    }

    // ------------------------------------------

    @Test
    void testCounterMerging() {
        final Counter c1 = new Counter();
        c1.increment(1);
        c1.increment(2);
        c1.increment(3);

        assertEquals(   3,      c1.getN(),                   BAD_COUNT    );
        assertEquals(   1,      c1.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(   2,      c1.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(   3,      c1.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals(   6,      c1.getSum(),      MAX_ERROR, BAD_SUM      );
        assertEquals(   1,      c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(   1,      c1.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(   1,      c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );

        final Counter c2 = new Counter();
        c2.increment(0);
        assertEquals(   1,      c2.getN(),                   BAD_COUNT    );
        assertEquals(   0,      c2.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(   0,      c2.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(   0,      c2.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals(   0,      c2.getSum(),      MAX_ERROR, BAD_SUM      );
        assertEquals(   0,      c2.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(   0,      c2.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(   0,      c2.getStdDev(),   MAX_ERROR, BAD_STDDEV   );

        c2.increment(10);
        assertEquals(   2,      c2.getN(),                   BAD_COUNT    );
        assertEquals(   0,      c2.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(   5,      c2.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(  10,      c2.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals(  10,      c2.getSum(),      MAX_ERROR, BAD_SUM      );
        assertEquals(   7.0711, c2.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(  50,      c2.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(   7.0711, c2.getStdDev(),   MAX_ERROR, BAD_STDDEV   );

        c2.increment(20);
        assertEquals(   3,      c2.getN(),                   BAD_COUNT    );
        assertEquals(   0,      c2.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(   10,     c2.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(   20,     c2.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals(   30,     c2.getSum(),      MAX_ERROR, BAD_SUM      );
        assertEquals(   10,     c2.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(   100,    c2.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(   10,     c2.getStdDev(),   MAX_ERROR, BAD_STDDEV   );

        c1.increment(c2);
        assertEquals(   6,      c1.getN(),                   BAD_COUNT    );
        assertEquals(   0,      c1.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(   6,      c1.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(  20,      c1.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals(   7.7201, c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(  59.6,    c1.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(   7.7201, c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );

        // SELF MERGE MUST WORK!!
        // (Note that the variance of 1,2,3,0,10,20
        //          is different from 1,2,3,0,10,20,1,2,3,0,10,20)
        c1.increment(c1);
        assertEquals(  12,      c1.getN(),                   BAD_COUNT    );
        assertEquals(   0,      c1.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(   6,      c1.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(  20,      c1.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals(   7.3608, c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(  54.1818, c1.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(   7.3608, c1.getStdDev(),   MAX_ERROR, BAD_STDDEV   );

        // DOUBLE SELF MERGE MUST WORK!!
        // (Note that the variance of
        //  1,2,3,0,10,20,1,2,3,0,10,20
        //  is different from
        //  1,2,3,0,10,20,1,2,3,0,10,20,1,2,3,0,10,20,1,2,3,0,10,20)
        c1.increment(c1);
        assertEquals(  24,      c1.getN(), BAD_COUNT      );
        assertEquals(   0,      c1.getMin(),      MAX_ERROR, BAD_MIN         );
        assertEquals(   6,      c1.getMean(),     MAX_ERROR, BAD_MEAN        );
        assertEquals(  20,      c1.getMax(),      MAX_ERROR, BAD_MAX        );
        assertEquals(   7.1990, c1.getStdDev(),   MAX_ERROR, BAD_STDDEV      );
        assertEquals(  51.8261, c1.getVariance(), MAX_ERROR, BAD_VARIANCE   );
        assertEquals(   7.1990, c1.getStdDev(),   MAX_ERROR, BAD_STDDEV      );
    }

    // ------------------------------------------

    @Test
    void testCounterMergingLeft() {
        final Counter c1 = new Counter();
        c1.increment(1);
        c1.increment(2);
        c1.increment(3);

        final Counter c2 = new Counter();
        c2.increment(0);
        c2.increment(10);
        c2.increment(20);

        c1.increment(c2);

        assertCounterIsSameAsReferenceCounter(c1);
    }

    // ------------------------------------------

    @Test
    void testCounterMergingRight() {
        final Counter c1 = new Counter();
        c1.increment(0);
        c1.increment(10);
        c1.increment(20);

        final Counter c2 = new Counter();
        c2.increment(1);
        c2.increment(2);
        c2.increment(3);

        c1.increment(c2);

        assertCounterIsSameAsReferenceCounter(c1);
    }

    // ------------------------------------------

    @Test
    void testCounterMergeAddAdd() {
        final Counter c1 = new Counter();
        c1.increment(1);
        c1.increment(2);

        final Counter c2 = new Counter();
        c2.increment(0);
        c2.increment(10);

        c1.increment(c2);
        c1.increment(3);
        c1.increment(20);

        assertCounterIsSameAsReferenceCounter(c1);
    }

    // ------------------------------------------

    @Test
    void testCounterMergeMerge() {
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

        assertCounterIsSameAsReferenceCounter(c1);
    }

    // ------------------------------------------

    @Test
    void testCounterMergeAddMergeAdd() {
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

        assertCounterIsSameAsReferenceCounter(c1);
    }

    // ------------------------------------------

    @Test
    void testCounterMergeAddMergeAddMergeAdd() {
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

        assertCounterIsSameAsReferenceCounter(c1);
    }

    // ------------------------------------------

    @Test
    void testCounterMergeAddMergeAddMergeAddAsBytes() {
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

        assertCounterIsSameAsReferenceCounter(c1);
    }

    // ------------------------------------------

    @Test
    void testCounterMergingEmptyRight() {
        final Counter c1 = new Counter();
        c1.increment(1);
        c1.increment(2);
        c1.increment(3);
        c1.increment(0);
        c1.increment(10);
        c1.increment(20);

        final Counter c2 = new Counter();

        c1.increment(c2);

        assertCounterIsSameAsReferenceCounter(c1);
    }

    // ------------------------------------------

    @Test
    void testCounterMergingEmptyLeft() {
        final Counter c1 = new Counter();

        final Counter c2 = new Counter();
        c2.increment(1);
        c2.increment(2);
        c2.increment(3);
        c2.increment(0);
        c2.increment(10);
        c2.increment(20);

        c1.increment(c2);

        assertCounterIsSameAsReferenceCounter(c1);
    }

    // ------------------------------------------

    @Test
    void testCounterMergingUp() {
        final Counter c = new Counter();

        c.increment(0);
        c.increment(1);
        c.increment(2);
        c.increment(3);
        c.increment(10);
        c.increment(20);

        assertCounterIsSameAsReferenceCounter(c);
    }

    // ------------------------------------------

    @Test
    void testCounterMergingDown() {
        final Counter c = new Counter();

        c.increment(20);
        c.increment(10);
        c.increment(3);
        c.increment(2);
        c.increment(1);
        c.increment(0);

        assertCounterIsSameAsReferenceCounter(c);
    }

    // ------------------------------------------

    @Test
    void testCounterSerialization() {
        byte[] serializedBytes = REFERENCE_COUNTER.toBytes();
        Counter deserialized = new Counter(serializedBytes);
        assertCounterIsSameAsReferenceCounter(deserialized);
    }

    // ------------------------------------------
//
//    @Test
//    void testCounterWritability() throws IOException {
//        byte[] bytes = TestWritableInterface.serialize(REFERENCE_COUNTER);
//        Counter deserialized = TestWritableInterface.asWritable(bytes, Counter.class);
//        assertTrue(BAD, counterIsSameAsReferenceCounter(deserialized));
//    }

    // ------------------------------------------

    private void assertCounterIsSameAsReferenceCounter(final Counter c) {
        assertEquals(REFERENCE_COUNTER.getN(),         c.getN(),                   BAD_COUNT    );
        assertEquals(REFERENCE_COUNTER.getMin(),       c.getMin(),      MAX_ERROR, BAD_MIN      );
        assertEquals(REFERENCE_COUNTER.getMean(),      c.getMean(),     MAX_ERROR, BAD_MEAN     );
        assertEquals(REFERENCE_COUNTER.getMax(),       c.getMax(),      MAX_ERROR, BAD_MAX      );
        assertEquals(REFERENCE_COUNTER.getSum(),       c.getSum(),      MAX_ERROR, BAD_SUM      );
        assertEquals(REFERENCE_COUNTER.getStdDev(),    c.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
        assertEquals(REFERENCE_COUNTER.getVariance(),  c.getVariance(), MAX_ERROR, BAD_VARIANCE );
        assertEquals(REFERENCE_COUNTER.getStdDev(),    c.getStdDev(),   MAX_ERROR, BAD_STDDEV   );
    }

}

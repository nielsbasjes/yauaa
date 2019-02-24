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

/**
 * An implementation of a counter that allows adding values and returns some summary statistics similar
 * to what http://commons.apache.org/math/apidocs/org/apache/commons/math3/stat/descriptive/SummaryStatistics.html does.
 * The advantages of this implementation:
 * 1) It allows serializing the underlying data into only 40 bytes (fixed size!)
 * 2) Actually implements the Associative and Commutativity properties of the underlying operations.
 * Thus efficiently allows for doing distributed aggregation of petabyte size datasets.
 */
public class Counter  {

    private long   n;   // Count of values
    private double m2;  // Second moment of values that have been added
    private double sum; // Total sum
    private double min; // Lowest value
    private double max; // Highest value

    // ------------------------------------------

    // These are ONLY local caches
    private double  mean;
    private double  variance;
    private double  stddev;
    private boolean mustRecalcVariance;

    // ------------------------------------------

    public Counter() {
        wipe();
    }

    // ------------------------------------------

    public Counter(final byte[] bytes) {
        wipe();
        setBytes(bytes);
    }

    // ------------------------------------------

    public void wipe() {
        n    = 0;
        m2   = Double.NaN;
        sum  = Double.NaN;
        min  = Double.NaN;
        max  = Double.NaN;

        // Cached values
        mean     = Double.NaN;
        variance = Double.NaN;
        stddev   = Double.NaN;
        mustRecalcVariance = true;
    }

    // ------------------------------------------

    /**
     * Creates the combined statistics by merging the current counter with
     * the specified counter that is given in the serialized form.
     * @param newValue The value that must be included in this counter
     */
    public void increment(final double newValue) {
        increment(1, 0.0, newValue, newValue, newValue);
    }

    // ------------------------------------------

    private static final int LONG_BYTES         = Long.SIZE / 8;
    private static final int DOUBLE_BYTES       = Double.SIZE / 8;
    private static final int COUNTER_BYTES_SIZE = LONG_BYTES + 4 * DOUBLE_BYTES;
    private static final int N_OFFSET           = 0;
    private static final int M2_OFFSET          = LONG_BYTES;
    private static final int SUM_OFFSET         = LONG_BYTES + DOUBLE_BYTES;
    private static final int MIN_OFFSET         = LONG_BYTES + DOUBLE_BYTES * 2;
    private static final int MAX_OFFSET         = LONG_BYTES + DOUBLE_BYTES * 3;

    public void setBytes(final byte[] bytes) {
        wipe();
        increment(bytes);
    }

    // ------------------------------------------

    /**
     * Creates the combined statistics by merging the current counter with
     * the specified counter that is given in the serialized form.
     * @param bytes The counter that must be included in this counter as represented in bytes
     */
    public void increment(final byte[] bytes) {
        increment(
            getLong(bytes,   N_OFFSET),    // Count of values
            getDouble(bytes, M2_OFFSET),   // Second moment of values that have been added
            getDouble(bytes, SUM_OFFSET),  // Total sum
            getDouble(bytes, MIN_OFFSET),  // Lowest value
            getDouble(bytes, MAX_OFFSET)); // Highest value
    }

    // ------------------------------------------

    public byte[] toBytes() {
        final byte[] bytes = new byte[COUNTER_BYTES_SIZE];
        putLong(n,     bytes, N_OFFSET);   // Count of values
        putDouble(m2,  bytes, M2_OFFSET);  // Second moment of values that have been added
        putDouble(sum, bytes, SUM_OFFSET); // Total sum
        putDouble(min, bytes, MIN_OFFSET); // Lowest value
        putDouble(max, bytes, MAX_OFFSET); // Highest value
        return bytes;
    }

    // ------------------------------------------

    /**
     * Creates the combined statistics by merging the current counter with the specified counter.
     * @param counter The counter that must be included in this counter
     */
    public void increment(final Counter counter) {
        if (counter == null) {
            return;
        }
        increment(counter.n, counter.m2, counter.sum, counter.min, counter.max);
    }

    // ------------------------------------------

    /**
     * Creates the combined statistics by merging the current counter with the specified counter.
     * Formulas were taken from
     *   http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm
     * and the code belonging to
     *   http://commons.apache.org/math/apidocs/org/apache/commons/math3/stat/descriptive/SummaryStatistics.html
     * and
     *   http://commons.apache.org/math/apidocs/org/apache/commons/math3/stat/descriptive/AggregateSummaryStatistics.html
     * @param cN   Count of values
     * @param cM2  Second moment of values that have been added
     * @param cSum Total sum
     * @param cMin Lowest value
     * @param cMax Highest value
     */
    private void increment(final long cN, final double cM2, final double cSum, final double cMin, final double cMax) {
        if (cN == 0) {
            return;
        }

        if (n == 0) {
            n    = cN;
            m2   = cM2;
            sum  = cSum;
            min  = cMin;
            max  = cMax;
            mean = sum/n;
            return;
        }

        min = Math.min(min, cMin);
        max = Math.max(max, cMax);

        final double oldN = n;
        final double meanDiff = (cSum/cN) - mean;

        sum += cSum;
        n   += cN;
        mean = sum/n;

        m2 = m2 + cM2 + meanDiff * meanDiff * oldN * cN / n;

        mustRecalcVariance = true;
    }

    //  ------------------------------------------

    /**
     * Get the number of increments that have been added
     * @return the number of increments that have been added
     */
    public long getN() {
        return n;
    }

    // ------------------------------------------

    /**
     * Get the sum of all increments that have been added
     * @return the sum of all increments that have been added
     */
    public double getSum() {
        return sum;
    }

    // ------------------------------------------

    /**
     * Get the mean (average) of all increments that have been added
     * @return the mean (average) of all increments that have been added
     */
    public double getMean() {
        return mean;
    }

    // ------------------------------------------

    /**
     * Get the variance of all increments that have been added
     * @return the variance of all increments that have been added
     */
    public double getVariance() {
        if (mustRecalcVariance) {
            mustRecalcVariance = false;
            if (n == 0) {
                variance = Double.NaN;
            } else if (n == 1) {
                variance = 0d;
            } else {
                variance = m2 / (n - 1);
            }
        }
        stddev = Double.NaN;
        return variance;
    }

    // ------------------------------------------

    /**
     * Get the standard deviation of all increments that have been added
     * @return the standard deviation of all increments that have been added
     */
    public double getStdDev() {
        if (mustRecalcVariance || Double.isNaN(stddev)) {
            stddev = Math.sqrt(getVariance());
        }
        return stddev;
    }

    // ------------------------------------------


    /**
     * Get the lowest value that has been added
     * @return the lowest value that has been added
     */
    public double getMin() {
        return min;
    }

    // ------------------------------------------

    /**
     * Get the highest value that has been added
     * @return the highest value that has been added
     */
    public double getMax() {
        return max;
    }

    // ------------------------------------------

//    /** {@inheritDoc} */
//    public void readFields(final DataInput in) throws IOException {
//        final byte[] bytes = new byte[COUNTER_BYTES_SIZE];
//        in.readFully(bytes, 0, COUNTER_BYTES_SIZE);
//        setBytes(bytes);
//    }
//
//    // ------------------------------------------
//
//    /** {@inheritDoc} */
//    public void write(final DataOutput out) throws IOException {
//        out.write(toBytes());
//    }

    // ------------------------------------------

    @Override
    public String toString() {
        return "{"
            + "\"n\":"    + n   // Count of values
            + ",\"m2\":"  + m2  // Second moment of values that have been added
            + ",\"sum\":" + sum // Total sum
            + ",\"min\":" + min // Lowest value
            + ",\"max\":" + max // Highest value
            + "}";
    }

    // ------------------------------------------

    // Some helper functions to convert the values to and from byte[]
    // These were copied from java.io.DataInputStream and java.io.DataOutputStream
    // This was done because instantiating these classes again and again is a massive needless overhead.

    private long getLong(final byte[] bytes, final int offset) {
        return (
            ((long)(bytes[offset  ] & 255) << 56) +
            ((long)(bytes[offset+1] & 255) << 48) +
            ((long)(bytes[offset+2] & 255) << 40) +
            ((long)(bytes[offset+3] & 255) << 32) +
            ((long)(bytes[offset+4] & 255) << 24) +
            ((long)(bytes[offset+5] & 255) << 16) +
            ((long)(bytes[offset+6] & 255) <<  8) +
            ((long)(bytes[offset+7] & 255)));
    }

    private double getDouble(final byte[] bytes, final int offset) {
        return Double.longBitsToDouble(getLong(bytes, offset));
    }

    private void putLong(final long value, final byte[] bytes, final int offset) {
        bytes[offset  ] = (byte)(value >>> 56);
        bytes[offset+1] = (byte)(value >>> 48);
        bytes[offset+2] = (byte)(value >>> 40);
        bytes[offset+3] = (byte)(value >>> 32);
        bytes[offset+4] = (byte)(value >>> 24);
        bytes[offset+5] = (byte)(value >>> 16);
        bytes[offset+6] = (byte)(value >>>  8);
        bytes[offset+7] = (byte)(value);
    }

    private void putDouble(final double value, final byte[] bytes, final int offset) {
        putLong(Double.doubleToLongBits(value), bytes, offset);
    }

    // ------------------------------------------

}

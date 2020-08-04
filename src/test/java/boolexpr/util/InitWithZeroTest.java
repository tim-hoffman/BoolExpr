package boolexpr.util;

/*-
 * #%L
 * BoolExpr
 * %%
 * Copyright (C) 2020 Timothy Hoffman
 * %%
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
 * #L%
 */
/* Adapted from https://github.com/brettwooldridge/SparseBitSet (available under
 *  the Apache-2.0 License)
 */

import boolexpr.util.SparseBitSet.Statistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 */
public class InitWithZeroTest {

    private static final int BITSET_MAX = Integer.MAX_VALUE - 1;

    private static void assertSizeZeroStatistics(SparseBitSet bs) {
        String[] stats = new String[Statistics.values().length];
        bs.statistics(stats);
        Assert.assertEquals("0", stats[Statistics.Size.ordinal()]);
        Assert.assertEquals("0", stats[Statistics.Length.ordinal()]);
        Assert.assertEquals("0", stats[Statistics.Cardinality.ordinal()]);
        Assert.assertEquals("0", stats[Statistics.Total_words.ordinal()]);
        //NOTE: no need to check Statistics.Set_array_length, it can vary
        Assert.assertEquals("0", stats[Statistics.Level2_areas.ordinal()]);
        Assert.assertEquals("0", stats[Statistics.Level3_blocks.ordinal()]);
        Assert.assertEquals(-1, bs.minSetBit());
        Assert.assertEquals(-1, bs.maxSetBit());
    }

    private SparseBitSet bitset;

    @Before
    public void setup() {
        bitset = new SparseBitSet(0);
    }

    @Test
    public void testPreviousSetBit() {
        Assert.assertEquals(-1, bitset.previousSetBit(0));
    }

    @Test
    public void testPreviousClearBit() {
        Assert.assertEquals(0, bitset.previousClearBit(0));
    }

    @Test
    public void testNextSetBit() {
        Assert.assertEquals(-1, bitset.nextSetBit(0));
    }

    @Test
    public void testNextClearBit() {
        Assert.assertEquals(0, bitset.nextClearBit(0));
    }

    @Test
    public void testClone() {
        assertSizeZeroStatistics(bitset);

        SparseBitSet clone = bitset.clone();

        Assert.assertEquals(-1, clone.nextSetBit(0));
        Assert.assertEquals(-1, clone.previousSetBit(0));
        Assert.assertEquals(-1, clone.previousSetBit(BITSET_MAX));
        Assert.assertEquals(0, clone.nextClearBit(0));
        Assert.assertEquals(0, clone.previousClearBit(0));
        Assert.assertEquals(BITSET_MAX, clone.previousClearBit(BITSET_MAX));
        assertSizeZeroStatistics(clone);
    }

    @Test
    public void testCloneNonEmpty() {
        //First modify the bitset and then clone
        bitset.set(10);
        bitset.set(15);
        {
            Assert.assertEquals(10, bitset.nextSetBit(0));
            Assert.assertEquals(-1, bitset.previousSetBit(0));
            Assert.assertEquals(15, bitset.previousSetBit(BITSET_MAX));
            Assert.assertEquals(0, bitset.nextClearBit(0));
            Assert.assertEquals(0, bitset.previousClearBit(0));
            Assert.assertEquals(BITSET_MAX, bitset.previousClearBit(BITSET_MAX));
            Assert.assertEquals(10, bitset.minSetBit());
            Assert.assertEquals(15, bitset.maxSetBit());
        }

        //Clone and make some checks
        SparseBitSet clone = bitset.clone();
        {
            Assert.assertEquals(10, clone.nextSetBit(0));
            Assert.assertEquals(-1, clone.previousSetBit(0));
            Assert.assertEquals(15, clone.previousSetBit(BITSET_MAX));
            Assert.assertEquals(0, clone.nextClearBit(0));
            Assert.assertEquals(0, clone.previousClearBit(0));
            Assert.assertEquals(BITSET_MAX, clone.previousClearBit(BITSET_MAX));
            Assert.assertEquals(10, clone.minSetBit());
            Assert.assertEquals(15, clone.maxSetBit());
        }

        //Now, change the initial bitset and check that it has changed but the
        // clone has not changed.
        bitset.set(20);
        bitset.set(5);
        {
            Assert.assertEquals(5, bitset.nextSetBit(0));
            Assert.assertEquals(-1, bitset.previousSetBit(0));
            Assert.assertEquals(20, bitset.previousSetBit(BITSET_MAX));
            Assert.assertEquals(0, bitset.nextClearBit(0));
            Assert.assertEquals(0, bitset.previousClearBit(0));
            Assert.assertEquals(BITSET_MAX, bitset.previousClearBit(BITSET_MAX));
            Assert.assertEquals(5, bitset.minSetBit());
            Assert.assertEquals(20, bitset.maxSetBit());
        }
        {
            Assert.assertEquals(10, clone.nextSetBit(0));
            Assert.assertEquals(-1, clone.previousSetBit(0));
            Assert.assertEquals(15, clone.previousSetBit(BITSET_MAX));
            Assert.assertEquals(0, clone.nextClearBit(0));
            Assert.assertEquals(0, clone.previousClearBit(0));
            Assert.assertEquals(BITSET_MAX, clone.previousClearBit(BITSET_MAX));
            Assert.assertEquals(10, clone.minSetBit());
            Assert.assertEquals(15, clone.maxSetBit());
        }

        //Finally, change the initial bitset and ensure that it has changed but
        //  the original bitset has not changed.
        clone.resize(-1);
        {
            assertSizeZeroStatistics(clone);
        }
        {
            Assert.assertEquals(5, bitset.nextSetBit(0));
            Assert.assertEquals(-1, bitset.previousSetBit(0));
            Assert.assertEquals(20, bitset.previousSetBit(BITSET_MAX));
            Assert.assertEquals(0, bitset.nextClearBit(0));
            Assert.assertEquals(0, bitset.previousClearBit(0));
            Assert.assertEquals(BITSET_MAX, bitset.previousClearBit(BITSET_MAX));
            Assert.assertEquals(5, bitset.minSetBit());
            Assert.assertEquals(20, bitset.maxSetBit());
        }
    }

    @Test
    public void testSize() {
        Assert.assertEquals(0, bitset.size());
    }

    @Test
    public void testMin() {
        Assert.assertEquals(-1, bitset.minSetBit());
    }

    @Test
    public void testMax() {
        Assert.assertEquals(-1, bitset.maxSetBit());
    }

    @Test
    public void testAnd() {
        bitset.and(new SparseBitSet(0));
    }

    @Test
    public void testResizeTruncateCacheBehavior() {
        //This test makes sure that all cached data is updated correctly when
        //  a non-empty bitset is truncated via resize(-1) which is not actually
        //  usable outside the SparseBitSet class but none-the-less, should
        //  result in correct behavior.
        assertSizeZeroStatistics(bitset);

        {
            bitset.set(100);
            String[] stats = new String[Statistics.values().length];
            bitset.statistics(stats);
            Assert.assertEquals("1", stats[Statistics.Size.ordinal()]);
            Assert.assertEquals("101", stats[Statistics.Length.ordinal()]);//i.e. highest set bit + 1
            Assert.assertEquals("1", stats[Statistics.Cardinality.ordinal()]);
            Assert.assertEquals("1", stats[Statistics.Total_words.ordinal()]);
            Assert.assertEquals("1", stats[Statistics.Set_array_length.ordinal()]);
            Assert.assertEquals("1", stats[Statistics.Level2_areas.ordinal()]);
            Assert.assertEquals("1", stats[Statistics.Level3_blocks.ordinal()]);
            Assert.assertEquals(100, bitset.minSetBit());
            Assert.assertEquals(100, bitset.maxSetBit());
        }

        //NOTE: resize does not always cause the cache to be cleared but
        //  resizing to -1 does so the cached values should be reset to
        //  their original valuse.
        bitset.resize(-1);
        assertSizeZeroStatistics(bitset);
    }
}

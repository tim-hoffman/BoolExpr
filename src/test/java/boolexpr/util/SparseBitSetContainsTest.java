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

import java.util.Random;
import junit.framework.Assert;
import org.junit.*;

/**
 *
 * @author Timothy Hoffman
 */
public class SparseBitSetContainsTest {

    public SparseBitSetContainsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private void assertContainsAll(SparseBitSet a, SparseBitSet b, boolean expected) {
//        assertContainsAll_multi_strat(a, b, expected);
        assertContainsAll_both(a, b, expected);
    }

    private void assertContainsAll_multi_strat(SparseBitSet a, SparseBitSet b, boolean expected) {
        final int NUM_ITER = 500;//1000 is reasonable total runtime, 24 min
        long start;

        //Always ensure identity 
        long elap_0 = 0;
        for (int i = 0; i < NUM_ITER; i++) {
            start = System.nanoTime();
            Assert.assertTrue(a.containsAll_Strat(a));
            elap_0 += System.nanoTime() - start;
            start = System.nanoTime();
            Assert.assertTrue(b.containsAll_Strat(b));
            elap_0 += System.nanoTime() - start;
        }
        //Check the actual question
        long elap_1 = 0;
        for (int i = 0; i < NUM_ITER; i++) {
            start = System.nanoTime();
            Assert.assertEquals(expected, a.containsAll_Strat(b));
            elap_1 += System.nanoTime() - start;
        }
        //NOTE: cardinality calls must appear AFTER the checks b/c they
        //  force caching and that could affec the results above (but in the
        //  case of Strategy approach, I don't think it does actually.
        String size = "(a=" + a.cardinality() + ",b=" + b.cardinality() + ")";
        System.out.println("Identity checks " + size + ": strat=" + (elap_0 / NUM_ITER) + "ns/iter");
        System.out.println("Actual checks " + size + ": strat=" + (elap_1 / NUM_ITER) + "ns/iter");
    }

    private void assertContainsAll_both(SparseBitSet a, SparseBitSet b, boolean expected) {
        //Always ensure identity on both ways of doing it
        long start = System.nanoTime();
        Assert.assertTrue(a.containsAll_Strat(a));
        Assert.assertTrue(b.containsAll_Strat(b));
        long elap_0_strat = System.nanoTime() - start;
        start = System.nanoTime();
        Assert.assertTrue(a.containsAll_Loop(a));
        Assert.assertTrue(b.containsAll_Loop(b));
        long elap_0_loop = System.nanoTime() - start;
        System.out.println("Identity checks: strat=" + elap_0_strat + "ns, loop=" + elap_0_loop + "ns, strat/loop=" + (1d * elap_0_strat / elap_0_loop));

        //Check the actual question
        start = System.nanoTime();
        Assert.assertEquals(expected, a.containsAll_Strat(b));
        long elap_1_strat = System.nanoTime() - start;
        start = System.nanoTime();
        Assert.assertEquals(expected, a.containsAll_Loop(b));
        long elap_1_loop = System.nanoTime() - start;
        System.out.println("Actual checks: strat=" + elap_1_strat + "ns, loop=" + elap_1_loop + "ns, strat/loop=" + (1d * elap_1_strat / elap_1_loop));
    }

    @Test
    public void test0() {
        System.out.println("\nRunning Test " + new Object() {/**/ }.getClass().getEnclosingMethod().getName() + "...");
        //Contains empty -> false
        SparseBitSet a = SparseBitSet.make(0, 1, 2, 3);
        SparseBitSet b = new SparseBitSet();
        assertContainsAll(a, b, true);
        assertContainsAll(b, a, false);
    }

    @Test
    public void test1() {
        System.out.println("\nRunning Test " + new Object() {/**/ }.getClass().getEnclosingMethod().getName() + "...");
        //Equal (but not same reference)
        SparseBitSet a = SparseBitSet.make(0, 1, 2, 3);
        SparseBitSet b = SparseBitSet.make(3, 2, 1, 0);
        assertContainsAll(a, b, true);
        assertContainsAll(b, a, true);
    }

    @Test
    public void test2() {
        System.out.println("\nRunning Test " + new Object() {/**/ }.getClass().getEnclosingMethod().getName() + "...");
        //Simple small value case
        SparseBitSet a = SparseBitSet.make(0, 1, 2, 3);
        SparseBitSet b = SparseBitSet.make(1, 2);
        assertContainsAll(a, b, true);
        assertContainsAll(b, a, false);
    }

    @Test
    public void test3() {
        System.out.println("\nRunning Test " + new Object() {/**/ }.getClass().getEnclosingMethod().getName() + "...");
        //Simple large value case
        SparseBitSet a = SparseBitSet.make(1_000_000_000, 1_005_000_000, 2_100_000_000, 2_000_300_000);
        SparseBitSet b = SparseBitSet.make(1_000_000_000, 2_000_300_000);
        assertContainsAll(a, b, true);
        assertContainsAll(b, a, false);
    }

//    @Test
    public void test4() {
        System.out.println("\nRunning Test " + new Object() {/**/ }.getClass().getEnclosingMethod().getName() + "...");
        //Simple large value case
        SparseBitSet a = new SparseBitSet();
        a.set(0, Integer.MAX_VALUE);//full set

        {
            SparseBitSet b = new SparseBitSet();
            b.set(0, 100);
            b.set(1000, 2000);
            b.set(5_000_000, 7_000_000);
            b.set(2_000_000_000, Integer.MAX_VALUE);
            assertContainsAll(a, b, true);
            assertContainsAll(b, a, false);
        }
        if (false) {
            Random rand = new Random(Long.MAX_VALUE / 2);
            for (int rep = 0; rep < 50; rep++) {
                final int count = rand.nextInt(49) + 13;
                System.out.println("Building iteration " + rep + " with " + count + " ranges...");
                SparseBitSet c = new SparseBitSet();
                for (int i = 0; i < count; i++) {
                    int range = rand.nextInt(Integer.MAX_VALUE / count);
                    int min = rand.nextInt(Integer.MAX_VALUE - range);
                    c.set(min, min + range);
                }
                assertContainsAll(a, c, true);
                assertContainsAll(c, a, false);
            }
        }
    }

    @Test
    public void test5() {
        System.out.println("\nRunning Test " + new Object() {/**/ }.getClass().getEnclosingMethod().getName() + "...");
        //Simple large value case
        SparseBitSet a = new SparseBitSet();
        a.set(0, Integer.MAX_VALUE);//full set
        assert a.cardinality() == Integer.MAX_VALUE;

        //Remove some random ranges from 'a' and make sure containsAll 
        //  is false.
        Random rand = new Random(Long.MAX_VALUE / 2);
        final int count = rand.nextInt(49) + 13;
        final int chunkSize = Integer.MAX_VALUE / count;
//        System.out.println("count = " + count + ", chunkSize = " + chunkSize);
        for (int i = 0; i < count; i++) {
            int range = rand.nextInt(chunkSize);//from 0 to full chunk less 1
            int offset = chunkSize * i;
            int min = rand.nextInt(chunkSize - range) + offset;
            a.clear(min, min + range);
            SparseBitSet c = new SparseBitSet();
            c.set(min, min + range);
//            System.out.println("  Cleared " + min + " to " + (min + range - 1) + " (" + range + ")");
            assertContainsAll(a, c, false);
            Assert.assertFalse(a.intersects(c));
            assert c.cardinality() == range;
        }
        int numRemoved = Integer.MAX_VALUE - a.cardinality();
        System.out.println("    Actual removed count = " + numRemoved);
        System.out.println("    Remaining count = " + a.cardinality());
        System.out.println("    Percent removed = " + ((1d * numRemoved / Integer.MAX_VALUE) * 100));

        //Clone what remains of 'a' and make sure it matches.
        //The cloning process can drop unused blocks/areas from the bitset.
        SparseBitSet b = a.clone();
        assertContainsAll(a, b, true);
        assertContainsAll(b, a, true);
    }
}

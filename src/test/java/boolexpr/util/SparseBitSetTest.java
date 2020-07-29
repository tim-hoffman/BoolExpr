package boolexpr.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import org.junit.*;

/**
 *
 * @author Timothy Hoffman
 */
public class SparseBitSetTest {

    private static final boolean RUN_PERFORMANCE_TESTS = false;

    public SparseBitSetTest() {
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

    @Test
    public void testMinMax() {
        SparseBitSet sb = new SparseBitSet();
        Assert.assertEquals(-1, sb.minSetBit());//in empty set, the minSetBit is -1
        Assert.assertEquals(-1, sb.maxSetBit());//in empty set, the max is -1

        sb.set(0);
        Assert.assertEquals(0, sb.minSetBit());//any single element is the minSetBit
        Assert.assertEquals(0, sb.maxSetBit());//any single element is the max

        sb.clear();
        Assert.assertEquals(-1, sb.minSetBit());//in empty set, the minSetBit is -1
        Assert.assertEquals(-1, sb.maxSetBit());//in empty set, the max is -1
        sb.set(500);
        Assert.assertEquals(500, sb.minSetBit());//any single element is the minSetBit
        Assert.assertEquals(500, sb.maxSetBit());//any single element is the max

        sb.set(70, 81);
        Assert.assertEquals(70, sb.minSetBit());//70 is the new minSetBit
        Assert.assertEquals(500, sb.maxSetBit());//500 is still the max

        sb.clear(500);
        Assert.assertEquals(70, sb.minSetBit());//70 is still the minSetBit
        Assert.assertEquals(80, sb.maxSetBit());//80 is the new max

        sb.clear(75, 80);//leaves {70..74, 80}
        Assert.assertEquals(70, sb.minSetBit());//70 is still the minSetBit
        Assert.assertEquals(80, sb.maxSetBit());//80 is still the max

        sb.clear(80);//leaves {70..74}
        Assert.assertEquals(70, sb.minSetBit());//70 is still the minSetBit
        Assert.assertEquals(74, sb.maxSetBit());//74 is the new max

        sb.set(100, 106);//equals {70..74,100..105}
        Assert.assertEquals(70, sb.minSetBit());//70 is still the minSetBit
        Assert.assertEquals(105, sb.maxSetBit());//105 is the new max

        sb.clear(100, 106);//back to {70..74}
        Assert.assertEquals(70, sb.minSetBit());//70 is still the minSetBit
        Assert.assertEquals(74, sb.maxSetBit());//74 is the new max

        //largest allowed value is Integer.MAX_VALUE - 1
        sb.set(Integer.MAX_VALUE - 1);//equals {70..74,IntMax-1}
        Assert.assertEquals(70, sb.minSetBit());//70 is still the minSetBit
        Assert.assertEquals(Integer.MAX_VALUE - 1, sb.maxSetBit());//IntMax - 1 is the new max

        //smallest allowed value is 0
        sb.set(0);//equals {0,70..74,IntMax-1}
        Assert.assertEquals(0, sb.minSetBit());//0 is the new minSetBit
        Assert.assertEquals(Integer.MAX_VALUE - 1, sb.maxSetBit());//IntMax - 1 is still the max

        sb.clear(70, 75);//equals {0,IntMax-1}
        Assert.assertEquals(0, sb.minSetBit());//0 is still the minSetBit
        Assert.assertEquals(Integer.MAX_VALUE - 1, sb.maxSetBit());//IntMax - 1 is still the max

        sb.clear(0);//equals {IntMax-1}
        Assert.assertEquals(Integer.MAX_VALUE - 1, sb.minSetBit());//IntMax - 1 is the new minSetBit
        Assert.assertEquals(Integer.MAX_VALUE - 1, sb.maxSetBit());//IntMax - 1 is still the max

        sb.clear(Integer.MAX_VALUE - 1);
        Assert.assertEquals(-1, sb.minSetBit());//in empty set, the minSetBit is -1
        Assert.assertEquals(-1, sb.maxSetBit());//in empty set, the max is -1
    }

    private static void initialize(int numValues, SparseBitSet sb_lc, SparseBitSet sb_lf, SparseBitSet sb_hc, SparseBitSet sb_hf) {
        sb_lc.clear();
        sb_lf.clear();
        sb_hc.clear();
        sb_hf.clear();
        for (int i = 0; i < numValues; i++) {
            sb_lc.set(i);                       //low and close numbers
            sb_lf.set(i * 2_000_000);           //low and far numbers
            sb_hc.set(i + 2_000_000_000);       //high and close numbers
            sb_hf.set((i + 1) * 2_000_000);     //high and far numbers
        }
    }

    private static void initialize(int numValues, HashMap<Integer, Integer> m_lc, HashMap<Integer, Integer> m_lf, HashMap<Integer, Integer> m_hc, HashMap<Integer, Integer> m_hf) {
        m_lc.clear();
        m_lf.clear();
        m_hc.clear();
        m_hf.clear();
        for (int i = 0; i < numValues; i++) {
            m_lc.put(i, i);                     //low and close numbers
            m_lf.put(i * 2_000_000, i);         //low and far numbers
            m_hc.put(i + 2_000_000_000, i);     //high and close numbers
            m_hf.put((i + 1) * 2_000_000, i);   //high and far numbers
        }
    }

    @Test
    public void testBitsetIteratorPerformance() {
        if (RUN_PERFORMANCE_TESTS) {
            SparseBitSet sb1 = new SparseBitSet();
            SparseBitSet sb2 = new SparseBitSet();
            SparseBitSet sb3 = new SparseBitSet();
            SparseBitSet sb4 = new SparseBitSet();
            initialize(1000, sb1, sb2, sb3, sb4);

            bitsetIteratorPerformanceHelper(sb1, "low/close");
            bitsetIteratorPerformanceHelper(sb2, "low/far");
            bitsetIteratorPerformanceHelper(sb3, "high/close");
            bitsetIteratorPerformanceHelper(sb4, "high/far");
            bitsetIteratorPerformanceHelper(sb1, "low/close");//repeat

            //Sample output from 50K iterations w/in bitsetIteratorPerformanceHelper(..)
            //    Running bitsetIteratorPerformanceHelper low/close with 50000 iterations...
            //    [testBitsetIteratorPerformance] low/close +next = 392.322069ms (127446.30993470826 keys per ms)
            //    [testBitsetIteratorPerformance] low/close +min = 394.868144ms (126624.54735776306 keys per ms)
            //    [testBitsetIteratorPerformance] low/close +iter = 551.543228ms (90654.72561653862 keys per ms)
            //    [testBitsetIteratorPerformance] low/close +stat+next = 410.305706ms (121860.357457471 keys per ms)
            //    [testBitsetIteratorPerformance] low/close +stat+min = 411.236104ms (121584.65541731715 keys per ms)
            //    [testBitsetIteratorPerformance] low/close +stat+iter = 564.609707ms (88556.74881268026 keys per ms)
            //
            //    Running bitsetIteratorPerformanceHelper low/far with 50000 iterations...
            //    [testBitsetIteratorPerformance] low/far +next = 5135.004124ms (9737.090524681338 keys per ms)
            //    [testBitsetIteratorPerformance] low/far +min = 4748.492415ms (10529.65775875626 keys per ms)
            //    [testBitsetIteratorPerformance] low/far +iter = 5050.402941ms (9900.200159098553 keys per ms)
            //    [testBitsetIteratorPerformance] low/far +stat+next = 4689.796337ms (10661.443782862505 keys per ms)
            //    [testBitsetIteratorPerformance] low/far +stat+min = 18903.269076ms (2645.0451400218963 keys per ms)
            //    [testBitsetIteratorPerformance] low/far +stat+iter = 19129.600805ms (2613.750308209843 keys per ms)
            //
            //    Running bitsetIteratorPerformanceHelper high/close with 50000 iterations...
            //    [testBitsetIteratorPerformance] high/close +next = 1297.443135ms (38537.33443200191 keys per ms)
            //    [testBitsetIteratorPerformance] high/close +min = 462.628593ms (108078.05820164685 keys per ms)
            //    [testBitsetIteratorPerformance] high/close +iter = 587.43307ms (85116.07969227881 keys per ms)
            //    [testBitsetIteratorPerformance] high/close +stat+next = 1374.437376ms (36378.52176685858 keys per ms)
            //    [testBitsetIteratorPerformance] high/close +stat+min = 5516.856041ms (9063.132992489118 keys per ms)
            //    [testBitsetIteratorPerformance] high/close +stat+iter = 5637.266978ms (8869.546217188226 keys per ms)
            //
            //    Running bitsetIteratorPerformanceHelper high/far with 50000 iterations...
            //    [testBitsetIteratorPerformance] high/far +next = 4875.119453ms (10256.158947906706 keys per ms)
            //    [testBitsetIteratorPerformance] high/far +min = 4907.241219ms (10189.024294629851 keys per ms)
            //    [testBitsetIteratorPerformance] high/far +iter = 5035.115364ms (9930.259067645067 keys per ms)
            //    [testBitsetIteratorPerformance] high/far +stat+next = 4928.21557ms (10145.66008523852 keys per ms)
            //    [testBitsetIteratorPerformance] high/far +stat+min = 18970.506274ms (2635.670301984899 keys per ms)
            //    [testBitsetIteratorPerformance] high/far +stat+iter = 19128.646734ms (2613.8806730707224 keys per ms)
            //
            //  Average *+iter keys per ms = 37281.898818338762675
            //  Average +stat+iter keys per ms = 25663.48150278726285
            //
            //OBSERVATION 1: Within a given configuration, the "+next" cases are
            //  about the same with or without "+stat". This is because they do not
            //  require statistics to be computed.
            //OBSERVATION 2: Within a given configuration, the "+min" and "+iter"
            //  cases always perform better when stats are cached vs when stats
            //  are computed each time. So they have a real advantage when the
            //  bitsets are not modified.
            //OBSERVATION 3: Performance of "+min" and "+iter" (with pre-computed
            //  stats) is similar regardless of high or low start. Makes sense
            //  because they rely on the cached min value. What's interesting though
            //  is that it's only significantly faster than "+next" in the
            //  "high/close" case and in the other indistinguishable, or even slower.
            //OBSERVATION 4: computing stats becomes expensive with the high start
            //  value and even more so when there's a far spread of values (but the
            //  far spread does overshadow the high start).
            //OBSERVATION 5:  the Iterator version almost always performs slower due
            //  to unboxing and other overhead, except when the benefit of usin the
            //  cached min vs next(0) is extreme (i.e. high/close w/ pre-computed).
            //
            //
            //
            //TODO: based on these results I need to decide what condition to use
            //  in NormalForm#resolveAll(..) to decide whether to iterate on phrase
            //  or on the replacement set. Perhaps always replacement set? The
            //  phrase may not have its statistics computed already and there really
            //  isn't a need to according to what's in resolveAll. Now, computing
            //  the size would but that's not needed unless I'm using it as a test
            //  in the condition. Also, removal of a value from the phrase would
            //  cause the cache to be reset!
            //TODO: I need a test w/ a BitSet.remove(..) call w/in the iteration
            //  because that causes the statistics to be reset. But that really
            //  shouldn't affect anything b/c the statistics should only be used
            //  for the initial 'min' value initializer in the loop.
            //NOTE: BitSet.iterator is 3.1x slower than *+iter and nearly 4.5x slower than +stat+iter
        }
    }

    private void bitsetIteratorPerformanceHelper(SparseBitSet sb, String testName) {
        if (RUN_PERFORMANCE_TESTS) {
            final int NUM_ITER = 50_000;
            System.out.println("\nRunning bitsetIteratorPerformanceHelper " + testName + " with " + NUM_ITER + " iterations...");

            double elapsed1a = 0, elapsed2a = 0, elapsed3a = 0;
            double elapsed1c = 0, elapsed2c = 0, elapsed3c = 0;
            int sum1a = 0, sum2a = 0, sum3a = 0, sum1c = 0, sum2c = 0, sum3c = 0;
            for (int r = 0; r < NUM_ITER; r++) {
                {//CASE 1a: nextSetBit(0)
                    sb.statistics();//compute statistics prior to timing
                    long start = System.nanoTime();
                    for (int i = sb.nextSetBit(0); i >= 0; i = sb.nextSetBit(i + 1)) {
                        sum1a++;
                    }
                    elapsed1a += System.nanoTime() - start;
                }
                {//CASE 2a: minSetBit()
                    sb.statistics();//compute statistics prior to timing
                    long start = System.nanoTime();
                    for (int i = sb.minSetBit(); i >= 0; i = sb.nextSetBit(i + 1)) {
                        sum2a++;
                    }
                    elapsed2a += System.nanoTime() - start;
                }
                {//CASE 3a: iterator()
                    sb.statistics();//compute statistics prior to timing
                    long start = System.nanoTime();
                    for (Integer i : sb) {
                        sum3a++;
                    }
                    elapsed3a += System.nanoTime() - start;
                }
                {//CASE 1c: statistics + nextSetBit(0)
                    sb.cache.hash = 0;//clear cache to force statistics to recompute
                    long start = System.nanoTime();
                    for (int i = sb.nextSetBit(0); i >= 0; i = sb.nextSetBit(i + 1)) {
                        sum1c++;
                    }
                    elapsed1c += System.nanoTime() - start;
                }
                {//CASE 2c: statistics + minSetBit()
                    sb.cache.hash = 0;//clear cache to force statistics to recompute
                    long start = System.nanoTime();
                    for (int i = sb.minSetBit(); i >= 0; i = sb.nextSetBit(i + 1)) {
                        sum2c++;
                    }
                    elapsed2c += System.nanoTime() - start;
                }
                {//CASE 3c: statistics + iterator()
                    sb.cache.hash = 0;//clear cache to force statistics to recompute
                    long start = System.nanoTime();
                    for (Integer i : sb) {
                        sum3c++;
                    }
                    elapsed3c += System.nanoTime() - start;
                }
            }

            double elapsedMS1a = elapsed1a / 1_000_000;
            double elapsedMS2a = elapsed2a / 1_000_000;
            double elapsedMS3a = elapsed3a / 1_000_000;
            double elapsedMS1c = elapsed1c / 1_000_000;
            double elapsedMS2c = elapsed2c / 1_000_000;
            double elapsedMS3c = elapsed3c / 1_000_000;
            System.out.println("[testBitsetIteratorPerformance] " + testName + " +next = " + elapsedMS1a + "ms (" + (sum1a / elapsedMS1a) + " keys per ms)");
            System.out.println("[testBitsetIteratorPerformance] " + testName + " +min = " + elapsedMS2a + "ms (" + (sum2a / elapsedMS2a) + " keys per ms)");
            System.out.println("[testBitsetIteratorPerformance] " + testName + " +iter = " + elapsedMS3a + "ms (" + (sum3a / elapsedMS3a) + " keys per ms)");
            System.out.println("[testBitsetIteratorPerformance] " + testName + " +stat+next = " + elapsedMS1c + "ms (" + (sum1c / elapsedMS1c) + " keys per ms)");
            System.out.println("[testBitsetIteratorPerformance] " + testName + " +stat+min = " + elapsedMS2c + "ms (" + (sum2c / elapsedMS2c) + " keys per ms)");
            System.out.println("[testBitsetIteratorPerformance] " + testName + " +stat+iter = " + elapsedMS3c + "ms (" + (sum3c / elapsedMS3c) + " keys per ms)");
            System.out.println("sum1a = " + sum1a + "; sum2a = " + sum2a + "; sum3a = " + sum3a + "; sum1c = " + sum1c + "; sum2c = " + sum2c + "; sum3c = " + sum3c);
        }
    }

    @Test
    public void testHashMapIteratorPerformance() {
        if (RUN_PERFORMANCE_TESTS) {
            {
                HashMap<Integer, Integer> m1 = new HashMap<>();
                HashMap<Integer, Integer> m2 = new HashMap<>();
                HashMap<Integer, Integer> m3 = new HashMap<>();
                HashMap<Integer, Integer> m4 = new HashMap<>();
                initialize(100, m1, m2, m3, m4);

                hashmapIteratorPerformanceHelper(10_000_000, m1, "low/close");
                hashmapIteratorPerformanceHelper(10_000_000, m2, "low/far");
                hashmapIteratorPerformanceHelper(10_000_000, m3, "high/close");
                hashmapIteratorPerformanceHelper(10_000_000, m4, "high/far");
                hashmapIteratorPerformanceHelper(10_000_000, m1, "low/close");//repeat
                //
                //Sample output for 100 hashmap values
                //    Running hashmapIteratorPerformanceHelper low/close with 10000000 iterations...
                //    [testHashMapIteratorPerformance] low/close +entrySet = 8110.603824ms (123295.38240308457 keys per ms)
                //    [testHashMapIteratorPerformance] low/close +keySet = 14284.798014ms (70004.49001938544 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper low/far with 10000000 iterations...
                //    [testHashMapIteratorPerformance] low/far +entrySet = 9378.795012ms (106623.50533522887 keys per ms)
                //    [testHashMapIteratorPerformance] low/far +keySet = 13720.754435ms (72882.28972665817 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper high/close with 10000000 iterations...
                //    [testHashMapIteratorPerformance] high/close +entrySet = 8371.63542ms (119450.97341565789 keys per ms)
                //    [testHashMapIteratorPerformance] high/close +keySet = 13263.679378ms (75393.86104723437 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper high/far with 10000000 iterations...
                //    [testHashMapIteratorPerformance] high/far +entrySet = 9417.794055ms (106181.9778771962 keys per ms)
                //    [testHashMapIteratorPerformance] high/far +keySet = 14642.415136ms (68294.74446065862 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper low/close with 10000000 iterations...
                //    [testHashMapIteratorPerformance] low/close +entrySet = 8424.787655ms (118697.35368422173 keys per ms)
                //    [testHashMapIteratorPerformance] low/close +keySet = 12959.786815ms (77161.76309648655 keys per ms)
                //
                //  Average +entrySet keys per ms = 114849.838543077
                //
            }
            {
                HashMap<Integer, Integer> m1 = new HashMap<>();
                HashMap<Integer, Integer> m2 = new HashMap<>();
                HashMap<Integer, Integer> m3 = new HashMap<>();
                HashMap<Integer, Integer> m4 = new HashMap<>();
                initialize(1000, m1, m2, m3, m4);

                hashmapIteratorPerformanceHelper(1_000_000, m1, "low/close");
                hashmapIteratorPerformanceHelper(1_000_000, m2, "low/far");
                hashmapIteratorPerformanceHelper(1_000_000, m3, "high/close");
                hashmapIteratorPerformanceHelper(1_000_000, m4, "high/far");
                hashmapIteratorPerformanceHelper(1_000_000, m1, "low/close");//repeat
                //
                //Sample output for 1000 hashmap values
                //    Running hashmapIteratorPerformanceHelper low/close with 1000000 iterations...
                //    [testHashMapIteratorPerformance] low/close +entrySet = 7716.378505ms (129594.47224524143 keys per ms)
                //    [testHashMapIteratorPerformance] low/close +keySet = 12417.698791ms (80530.218749127 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper low/far with 1000000 iterations...
                //    [testHashMapIteratorPerformance] low/far +entrySet = 10400.51215ms (96149.11127237133 keys per ms)
                //    [testHashMapIteratorPerformance] low/far +keySet = 15818.143563ms (63218.54369428573 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper high/close with 1000000 iterations...
                //    [testHashMapIteratorPerformance] high/close +entrySet = 7695.5502ms (129945.22470920923 keys per ms)
                //    [testHashMapIteratorPerformance] high/close +keySet = 12347.199717ms (80990.02388559161 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper high/far with 1000000 iterations...
                //    [testHashMapIteratorPerformance] high/far +entrySet = 10400.075817ms (96153.14518817223 keys per ms)
                //    [testHashMapIteratorPerformance] high/far +keySet = 15741.92138ms (63524.64707837335 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper low/close with 1000000 iterations...
                //    [testHashMapIteratorPerformance] low/close +entrySet = 7713.799024ms (129637.80841174272 keys per ms)
                //    [testHashMapIteratorPerformance] low/close +keySet = 12416.625691ms (80537.17852869115 keys per ms)
                //
                //  Average +entrySet keys per ms = 116295.952365347
                //
                //OBSERVATION 1: keySet+get is always slower than entrySet (as expected)
                //OBSERVATION 2: far spread keys seem to be a little slower than close keys
                //OBSERVATION 3: interestingly, high keys seem to be faster than low keys
            }

            {
                HashMap<Integer, Integer> m1 = new HashMap<>();
                HashMap<Integer, Integer> m2 = new HashMap<>();
                HashMap<Integer, Integer> m3 = new HashMap<>();
                HashMap<Integer, Integer> m4 = new HashMap<>();
                initialize(10000, m1, m2, m3, m4);

                hashmapIteratorPerformanceHelper(100_000, m1, "low/close");
                hashmapIteratorPerformanceHelper(100_000, m2, "low/far");
                hashmapIteratorPerformanceHelper(100_000, m3, "high/close");
                hashmapIteratorPerformanceHelper(100_000, m4, "high/far");
                hashmapIteratorPerformanceHelper(100_000, m1, "low/close");//repeat
                //
                //Sample output for 10,000 hashmap values
                //    Running hashmapIteratorPerformanceHelper low/close with 100000 iterations...
                //    [testHashMapIteratorPerformance] low/close +entrySet = 8242.87705ms (121316.86472261528 keys per ms)
                //    [testHashMapIteratorPerformance] low/close +keySet = 13110.046608ms (76277.3794709304 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper low/far with 100000 iterations...
                //    [testHashMapIteratorPerformance] low/far +entrySet = 17068.826356ms (58586.33623327487 keys per ms)
                //    [testHashMapIteratorPerformance] low/far +keySet = 21960.93695ms (45535.39779640413 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper high/close with 100000 iterations...
                //    [testHashMapIteratorPerformance] high/close +entrySet = 8215.944081ms (121714.55771133797 keys per ms)
                //    [testHashMapIteratorPerformance] high/close +keySet = 13023.557741ms (76783.93415125413 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper high/far with 100000 iterations...
                //    [testHashMapIteratorPerformance] high/far +entrySet = 17049.99942ms (58651.02838812883 keys per ms)
                //    [testHashMapIteratorPerformance] high/far +keySet = 21984.705246ms (45486.16817057143 keys per ms)
                //
                //    Running hashmapIteratorPerformanceHelper low/close with 100000 iterations...
                //    [testHashMapIteratorPerformance] low/close +entrySet = 8182.201777ms (122216.49224185344 keys per ms)
                //    [testHashMapIteratorPerformance] low/close +keySet = 13019.093542ms (76810.2630781451 keys per ms)
                //
                //  Average +entrySet keys per ms = 96497.055859442
                //
            }
        }
    }

    private void hashmapIteratorPerformanceHelper(final int NUM_ITER, HashMap<Integer, Integer> m, String testName) {
        System.out.println("\nRunning hashmapIteratorPerformanceHelper " + testName + " with " + NUM_ITER + " iterations...");

        final int numKeys = m.size();
        double elapsed1 = 0, elapsed2 = 0;
        int sum1 = 0, sum2 = 0;
        int vals1 = 0, vals2 = 0;
        for (int r = 0; r < NUM_ITER; r++) {
            {//CASE 1: entrySet()
                ArrayList<Integer> values = new ArrayList<>(numKeys);
                long start = System.nanoTime();
                for (Map.Entry<Integer, Integer> e : m.entrySet()) {
                    sum1++;
                    values.add(e.getValue());
                }
                elapsed1 += System.nanoTime() - start;
                vals1 += values.size();
            }
            {//CASE 2: keySet()
                ArrayList<Integer> values = new ArrayList<>(numKeys);
                long start = System.nanoTime();
                for (Integer e : m.keySet()) {
                    sum2++;
                    values.add(m.get(e));
                }
                elapsed2 += System.nanoTime() - start;
                vals2 += values.size();
            }
        }

        double elapsedMS1 = elapsed1 / 1_000_000;
        double elapsedMS2 = elapsed2 / 1_000_000;
        System.out.println("[testHashMapIteratorPerformance] " + testName + " +entrySet = " + elapsedMS1 + "ms (" + (sum1 / elapsedMS1) + " keys per ms)");
        System.out.println("[testHashMapIteratorPerformance] " + testName + " +keySet = " + elapsedMS2 + "ms (" + (sum2 / elapsedMS2) + " keys per ms)");
        System.out.println("sum1 = " + sum1 + "; sum2 = " + sum2 + "; vals1 = " + vals1 + "; vals2 = " + vals2);
        assert sum1 == NUM_ITER * numKeys;
        assert sum2 == NUM_ITER * numKeys;
        assert vals1 == NUM_ITER * numKeys;
        assert vals2 == NUM_ITER * numKeys;
    }

    private static int randomNonNegativeInteger() {
        return (int) (Math.random() * Integer.MAX_VALUE);
    }

    @Test
    public void testContainsPerformance() {
        if (RUN_PERFORMANCE_TESTS) {
            SparseBitSet sb1 = new SparseBitSet();
            SparseBitSet sb2 = new SparseBitSet();
            SparseBitSet sb3 = new SparseBitSet();
            SparseBitSet sb4 = new SparseBitSet();
            initialize(1000, sb1, sb2, sb3, sb4);

            containsPerformanceHelper(sb1, "low/close");
            containsPerformanceHelper(sb2, "low/far");
            containsPerformanceHelper(sb3, "high/close");
            containsPerformanceHelper(sb4, "high/far");

            //Sample output from 100M iterations w/in containsPerformanceHelper(..)
            //    [testContainsPerformance] low/close = 2623.384238ms (2.623384238E-5 per iter)
            //    valuesFound = 32
            //    [testContainsPerformance] low/far = 2817.354983ms (2.817354983E-5 per iter)
            //    valuesFound = 62
            //    [testContainsPerformance] high/close = 2721.5951ms (2.7215951E-5 per iter)
            //    valuesFound = 59
            //    [testContainsPerformance] high/far = 2768.630877ms (2.768630877E-5 per iter)
            //    valuesFound = 54
            //
            //OBSERVATION 1: the time in all cases is very similar
            //OBSERVATION 2: the operation is reliably fast, does not require statistics
        }
    }

    private void containsPerformanceHelper(SparseBitSet sb, String testName) {
        final int NUM_ITER = 100_000_000;
        System.out.println("\nRunning containsPerformanceHelper " + testName + " with " + NUM_ITER + " iterations...");

        double elapsed = 0;
        int valuesFound = 0;
        for (int r = 0; r < NUM_ITER; r++) {
            int rand = randomNonNegativeInteger();
            long start = System.nanoTime();
            boolean x = sb.get(rand);
            elapsed += System.nanoTime() - start;
            if (x) {
                valuesFound++;
            }
        }

        double elapsedMS = elapsed / 1_000_000;
        System.out.println("[testContainsPerformance] " + testName + " = " + elapsedMS + "ms (" + (elapsedMS / NUM_ITER) + " per iter)");
        System.out.println("valuesFound = " + valuesFound);
    }

    private void removeAllExcept(SparseBitSet sb, int preserve) {
        sb.clear(0, preserve);
        sb.clear(preserve + 1, Integer.MAX_VALUE);
        Assert.assertEquals(1, sb.size());
        Assert.assertTrue(sb.get(preserve));
        Assert.assertEquals(preserve, sb.minSetBit());
        Assert.assertEquals(preserve, sb.maxSetBit());
    }

    @Test
    public void testNextSetBitEndPerformance() {
        if (RUN_PERFORMANCE_TESTS) {
            {
                System.out.println("=============================================");
                //large bit sets
                int size = 1000;
                SparseBitSet sb1 = new SparseBitSet();
                SparseBitSet sb2 = new SparseBitSet();
                SparseBitSet sb3 = new SparseBitSet();
                SparseBitSet sb4 = new SparseBitSet();
                initialize(size, sb1, sb2, sb3, sb4);

                nextSetBitEndPerformanceHelper(sb1, size + " low/close new");
                nextSetBitEndPerformanceHelper(sb2, size + " low/far new");
                nextSetBitEndPerformanceHelper(sb3, size + " high/close new");
                nextSetBitEndPerformanceHelper(sb4, size + " high/far new");
                nextSetBitEndPerformanceHelper(sb1, size + " low/close new (repeat)");//repeat

                System.out.println("=============================================");
                //single element bit set reduced as max from large bitset
                int max1 = sb1.maxSetBit(), min1 = sb1.minSetBit();
                int max2 = sb2.maxSetBit(), min2 = sb2.minSetBit();
                int max3 = sb3.maxSetBit(), min3 = sb3.minSetBit();
                int max4 = sb4.maxSetBit(), min4 = sb4.minSetBit();
                System.out.println("low/close: min = " + min1 + ", max = " + max1);
                System.out.println("low/far: min = " + min2 + ", max = " + max2);
                System.out.println("high/close: min = " + min3 + ", max = " + max3);
                System.out.println("high/far: min = " + min4 + ", max = " + max4);
                System.out.println("=============================================");

                removeAllExcept(sb1, min1);
                removeAllExcept(sb2, min2);
                removeAllExcept(sb3, min3);
                removeAllExcept(sb4, min4);

                nextSetBitEndPerformanceHelper(sb1, "1(min) low/close reduced");
                nextSetBitEndPerformanceHelper(sb2, "1(min) low/far reduced");
                nextSetBitEndPerformanceHelper(sb3, "1(min) high/close reduced");
                nextSetBitEndPerformanceHelper(sb4, "1(min) high/far reduced");
                nextSetBitEndPerformanceHelper(sb1, "1(min) low/close reduced (repeat)");//repeat

                System.out.println("=============================================");
                //single element bit set newly created
                SparseBitSet sb1_max = new SparseBitSet();
                sb1_max.set(max1);
                SparseBitSet sb2_max = new SparseBitSet();
                sb2_max.set(max2);
                SparseBitSet sb3_max = new SparseBitSet();
                sb3_max.set(max3);
                SparseBitSet sb4_max = new SparseBitSet();
                sb4_max.set(max4);

                nextSetBitEndPerformanceHelper(sb1_max, "1(max) low/close new");
                nextSetBitEndPerformanceHelper(sb2_max, "1(max) low/far new");
                nextSetBitEndPerformanceHelper(sb3_max, "1(max) high/close new");
                nextSetBitEndPerformanceHelper(sb4_max, "1(max) high/far new");
                nextSetBitEndPerformanceHelper(sb1_max, "1(max) low/close new (repeat)");//repeat

                System.out.println("=============================================");
                //single element bit set newly created
                SparseBitSet sb1_min = new SparseBitSet();
                sb1_min.set(min1);
                SparseBitSet sb2_min = new SparseBitSet();
                sb2_min.set(min2);
                SparseBitSet sb3_min = new SparseBitSet();
                sb3_min.set(min3);
                SparseBitSet sb4_min = new SparseBitSet();
                sb4_min.set(min4);

                nextSetBitEndPerformanceHelper(sb1_min, "1(min) low/close new");
                nextSetBitEndPerformanceHelper(sb2_min, "1(min) low/far new");
                nextSetBitEndPerformanceHelper(sb3_min, "1(min) high/close new");
                nextSetBitEndPerformanceHelper(sb4_min, "1(min) high/far new");
                nextSetBitEndPerformanceHelper(sb1_min, "1(min) low/close new (repeat)");//repeat
            }
            {
                System.out.println("=============================================");
                //small bit sets
                int size = 10;
                SparseBitSet sb1 = new SparseBitSet();
                SparseBitSet sb2 = new SparseBitSet();
                SparseBitSet sb3 = new SparseBitSet();
                SparseBitSet sb4 = new SparseBitSet();
                initialize(size, sb1, sb2, sb3, sb4);

                nextSetBitEndPerformanceHelper(sb1, size + " low/close new");
                nextSetBitEndPerformanceHelper(sb2, size + " low/far new");
                nextSetBitEndPerformanceHelper(sb3, size + " high/close new");
                nextSetBitEndPerformanceHelper(sb4, size + " high/far new");
                nextSetBitEndPerformanceHelper(sb1, size + " low/close new (repeat)");//repeat
            }
        }
        //
        //OBSERVATION: the speed of nextSetBit() does depend on the content of 
        //  the BitSet w/ large values causing it to be slower than smaller.
        //OBSERVATION: newly created BitSets perform better than BitSets had
        //  contained larger values that were removed leaving only the minimum.
        //
    }

    private void nextSetBitEndPerformanceHelper(SparseBitSet sb, String testName) {
        if (RUN_PERFORMANCE_TESTS) {
            final int NUM_ITER = 50_000;
            System.out.println("\nRunning nextSetBitEndPerformanceHelper " + testName + " with " + NUM_ITER + " iterations...");

            //precompute max+1 b/c we only want to time nextSetBit
            int maxPlus1 = sb.maxSetBit() + 1;
            int res;

            double elapsed = 0;
            for (int r = 0; r < NUM_ITER; r++) {
                long start = System.nanoTime();
                res = sb.nextSetBit(maxPlus1);
                elapsed += System.nanoTime() - start;
                Assert.assertEquals(-1, res);
            }

//            System.out.println("[nextSetBitEndPerformanceHelper] " + testName + " = " + (NUM_ITER / (elapsed / 1_000_000)) + "iter/ms");
            System.out.println("[nextSetBitEndPerformanceHelper] " + testName + " = " + (elapsed / NUM_ITER) + "ns/iter");
        }
    }
}

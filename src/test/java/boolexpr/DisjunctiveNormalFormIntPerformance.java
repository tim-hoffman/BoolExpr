package boolexpr;

import boolexpr.util.SparseBitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.*;

/**
 *
 * @author Timothy
 */
public class DisjunctiveNormalFormIntPerformance {

    public DisjunctiveNormalFormIntPerformance() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    //called before each test method
    @Before
    public void setUp() {
    }

    //called after each test method
    @After
    public void tearDown() {
    }

//    @Test
    public void test_resolveAll() {
        final int MAX_VAR = 14;//14
        Construction<SparseBitSet, Integer, DisjunctiveNormalFormInt> cons = Construction.DNF_INT;
        //TODO: I want to test both approaches to resolveAll (by phrase or by resolution map key)
        //      Need some cases where the resolution map keys aren't even there.
        //      Need some cases where the resolutions are huge and/or original DNF is huge.

        DisjunctiveNormalFormInt large = TestHelpers.buildLargestInstance(cons, MAX_VAR + 1);//variables are 0 to MAX_VAR (inclusive)
        final int numProps = large.getNumProps();
        final int numPhrases = large.getNumPhrases();
        System.out.println("#props = " + numProps + "; #phrases = " + numPhrases + "; props/phrase = " + ((double) numProps / numPhrases));
        System.out.println(large.toString(true));

        {//TEST: resMap is very large but no replacements happen
            final int NUM_ITER = 100;
            HashMap<Integer, DisjunctiveNormalFormInt> resMap = new HashMap<>();
            final DisjunctiveNormalFormInt T = DisjunctiveNormalFormInt.getTrue();
            for (int i = MAX_VAR + 1; i <= MAX_VAR + 2000; i++) {
                resMap.put(i, T);
            }
            long start = System.nanoTime();
            for (int i = 0; i < NUM_ITER; i++) {
                //NOTE: don't really need the clone, but it makes results more
                //  comparable to the ones that do need it.
                large.clone(false).resolveAll(resMap);
            }
            double elapsedMS = (System.nanoTime() - start) / 1_000_000;
            System.out.println("CASE 1: " + (elapsedMS / NUM_ITER) + "ms");
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse phrases: 11.9ms
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse the map: 116.6ms
        }
        {//TEST: resMap is smaller than phrase size but no replacements happen
            final int NUM_ITER = 1000;
            HashMap<Integer, DisjunctiveNormalFormInt> resMap = new HashMap<>();
            final DisjunctiveNormalFormInt T = DisjunctiveNormalFormInt.getTrue();
            for (int i = MAX_VAR + 1; i <= MAX_VAR + 5; i++) {
                resMap.put(i, T);
            }
            long start = System.nanoTime();
            for (int i = 0; i < NUM_ITER; i++) {
                //NOTE: don't really need the clone, but it makes results more
                //  comparable to the ones that do need it. 
                large.clone(false).resolveAll(resMap);
            }
            double elapsedMS = (System.nanoTime() - start) / 1_000_000;
            System.out.println("CASE 2: " + (elapsedMS / NUM_ITER) + "ms");
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse phrases: 9.458ms
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse the map: 9.04ms
        }
        {//TEST: resMap is just enough to renumber each variable (so not that large)
            final int NUM_ITER = 100;
            HashMap<Integer, DisjunctiveNormalFormInt> resMap = new HashMap<>();
            for (int i = 0; i <= MAX_VAR; i++) {
                resMap.put(i, new DisjunctiveNormalFormInt(i + MAX_VAR + 1));
            }
            long start = System.nanoTime();
            for (int i = 0; i < NUM_ITER; i++) {
                large.clone(false).resolveAll(resMap);
            }
            double elapsedMS = (System.nanoTime() - start) / 1_000_000;
            System.out.println("CASE 3: " + (elapsedMS / NUM_ITER) + "ms");
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse phrases: 2752.84ms (dominated by finalAdd, cross ~40ms, opt1 total ~40ms)
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse the map: 2592.44ms (again, dominated by finalAdd, cross ~40ms, opt2 total ~40ms)
        }
        {//TEST: resMap is very large and still just renumbers each variable
            final int NUM_ITER = 100;
            HashMap<Integer, DisjunctiveNormalFormInt> resMap = new HashMap<>();
            for (int i = 0; i < 2000; i++) {
                resMap.put(i, new DisjunctiveNormalFormInt(i + MAX_VAR + 1));
            }
            long start = System.nanoTime();
            for (int i = 0; i < NUM_ITER; i++) {
                large.clone(false).resolveAll(resMap);
            }
            double elapsedMS = (System.nanoTime() - start) / 1_000_000;
            System.out.println("CASE 4: " + (elapsedMS / NUM_ITER) + "ms");
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse phrases: 2752.13ms (dominated by finalAdd, cross only ~45ms, opt1 total ~40ms)
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse the map: 2831.92ms (dominated by finalAdd, cross ~45ms, opt2 total ~200ms)
        }
        {//TEST: resolutions are large (i.e. lots of OR)
            //NOTE: total time >50min when 'NUM_ITER=100' and 'j<=25' below
            final int NUM_ITER = 100;
            HashMap<Integer, DisjunctiveNormalFormInt> resMap = new HashMap<>();
            DisjunctiveNormalFormInt rep = cons.newFromElem(0);
            rep.merge(cons.newFromElem(1));
            rep.merge(cons.newFromElem(2));
            //NOTE: with 25, the phrases get quite large but still aren't taking
            //  as long as the ones in h2 take. I wonder if it's just strange
            //  DNF combinations or if the CPU is being throttled for some reason.
            //BEGIN: NormalForm#resolveAll(..) this.size=51480:6435; resMap.size=1
            //END: NormalForm#resolveAll(..) this.size=187176:23727; resMap.size=1
            for (int j = 1; j <= 25; j++) {
                rep.merge(cons.newFromElem(MAX_VAR + j));
            }
            resMap.put(0, rep);

            //First, test applying the resolutions when it does cause modification
            long start = System.nanoTime();
            for (int i = 0; i < NUM_ITER; i++) {
                large.clone(false).resolveAll(resMap);
            }
            double elapsedMS = (System.nanoTime() - start) / 1_000_000;
            System.out.println("CASE 5a: " + (elapsedMS / NUM_ITER) + "ms");
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse phrases: 13808.49ms (dominated by finalAdd, cross ~70, op1 total ~72, phraseAdd ~85)
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse the map: 12859.91ms (same)

            //Now, test applying the resolutions when it does NOT cause modification
            DisjunctiveNormalFormInt alreadyMod = large.clone(false);
            alreadyMod.resolveAll(resMap);
            start = System.nanoTime();
            for (int i = 0; i < NUM_ITER; i++) {
                alreadyMod.clone(false).resolveAll(resMap);
            }
            elapsedMS = (System.nanoTime() - start) / 1_000_000;
            System.out.println("CASE 5b: " + (elapsedMS / NUM_ITER) + "ms");
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse phrases: 18284.49ms (dominated by finalAdd, cross ~23, op1 total ~25, phraseAdd ~58)
            //OBSERVATION: forcing NormalForm#resolveAll(..) to traverse the map: 17848.12ms (same)
        }
    }

    private static void compareAppendPhrase(final int numIter, DisjunctiveNormalFormInt dnf, SparseBitSet newPhrase, String label) {
        {//sample output
            DisjunctiveNormalFormInt clone = dnf.clone(false);
            clone.appendPhraseToEachPhrase_2(newPhrase);
            System.out.println("appended = " + clone.toString(true));
        }
        final List<Integer> ORDER = TestHelpers.list(1, 2);
        long start;
        double elapsedMS1 = 0, elapsedMS2 = 0;
        DisjunctiveNormalFormInt clone;
        for (int i = 0; i < numIter; i++) {
            Collections.shuffle(ORDER);//do the tasks in random order
            for (Integer o : ORDER) {
                switch (o) {
                    case 1:
                        clone = dnf.clone(false);
                        start = System.nanoTime();
                        clone.appendPhraseToEachPhrase_1(newPhrase);
                        elapsedMS1 += ((double) (System.nanoTime() - start)) / 1_000_000;
                        break;
                    case 2:
                        clone = dnf.clone(false);
                        start = System.nanoTime();
                        clone.appendPhraseToEachPhrase_2(newPhrase);
                        elapsedMS2 += ((double) (System.nanoTime() - start)) / 1_000_000;
                        break;
                }
            }
        }

        System.out.println(label + ": (ms)(" + numIter + ") v1=" + (elapsedMS1) + ", v2=" + (elapsedMS2));
        System.out.println(label + ": (ms/iter) v1=" + (elapsedMS1 / numIter) + ", v2=" + (elapsedMS2 / numIter));
    }

    public void appendPhraseToEachPhrase_Helper(final int MAX_VAR, final int ITERATION_FACTOR) {
        assert ITERATION_FACTOR > 0;
        assert MAX_VAR >= 0;
        Construction<SparseBitSet, Integer, DisjunctiveNormalFormInt> cons = Construction.DNF_INT;

        //Build large DNF instances with low, mid, and high values
        DisjunctiveNormalFormInt lowDNF = TestHelpers.buildLargestInstance(cons, MAX_VAR + 1);//variables: 0 to MAX_VAR (inclusive)
        {
            System.out.println("\nlowDNF(" + MAX_VAR + ") = " + lowDNF.toString(true));
        }
        DisjunctiveNormalFormInt midDNF = lowDNF.clone(false);//variables: 200_000 to MAX_VAR+200_000 (inclusive)
        {
            HashMap<Integer, Integer> renum = new HashMap<>();
            for (int i = 0; i <= MAX_VAR; i++) {
                renum.put(i, 200_000 + i);
            }
            midDNF.replaceAll(renum);
            System.out.println("\nmidDNF(" + MAX_VAR + ") = " + midDNF.toString(true));
        }
        DisjunctiveNormalFormInt highDNF = lowDNF.clone(false);//variables: 2B to MAX_VAR+2B (inclusive)
        {
            HashMap<Integer, Integer> renum = new HashMap<>();
            for (int i = 0; i <= MAX_VAR; i++) {
                renum.put(i, 2_000_000_000 + i);
            }
            highDNF.replaceAll(renum);
            System.out.println("\nhighDNF(" + MAX_VAR + ") = " + highDNF.toString(true));
        }

        ////////////////////////////////////////////////////////////////////////
        // No overlap cases
        ////////////////////////////////////////////////////////////////////////
        {//TEST: single-elem phrase, small value, no overlap
            final int NUM_ITER = ITERATION_FACTOR * 1000;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 1; i++) {
                newPhrase.set(MAX_VAR + 1 + i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 1v" + MAX_VAR);
        }
        {//TEST: small phrase, small close values, no overlap
            final int NUM_ITER = ITERATION_FACTOR * 100;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 5; i++) {
                newPhrase.set(MAX_VAR + 1 + i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 4v" + MAX_VAR);
        }
        {//TEST: large phrase, small close values, no overlap
            final int NUM_ITER = ITERATION_FACTOR * 100;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 100; i++) {
                newPhrase.set(MAX_VAR + 1 + i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 7v" + MAX_VAR);
        }
        {//TEST: single-elem phrase, large value, no overlap
            final int NUM_ITER = ITERATION_FACTOR * 100;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 1; i++) {
                newPhrase.set(MAX_VAR + 200_000 + i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 2v" + MAX_VAR);
        }
        {//TEST: small phrase, large close values, no overlap
            final int NUM_ITER = ITERATION_FACTOR * 100;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 5; i++) {
                newPhrase.set(MAX_VAR + 200_000 + i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 5v" + MAX_VAR);
        }
        {//TEST: large phrase, large close values, no overlap
            final int NUM_ITER = ITERATION_FACTOR * 100;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 100; i++) {
                newPhrase.set(MAX_VAR + 200_000 + i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 8v" + MAX_VAR);
        }
        {//TEST: single-elem phrase, very large value, no overlap
            final int NUM_ITER = ITERATION_FACTOR * 10;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 1; i++) {
                newPhrase.set(MAX_VAR + 2_000_000_000 + i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 3v" + MAX_VAR);
        }
        {//TEST: small phrase, very large close values, no overlap
            final int NUM_ITER = ITERATION_FACTOR * 10;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 5; i++) {
                newPhrase.set(MAX_VAR + 2_000_000_000 + i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 6v" + MAX_VAR);
        }
        {//TEST: large phrase, very large close values, no overlap
            final int NUM_ITER = ITERATION_FACTOR * 1;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 100; i++) {
                newPhrase.set(MAX_VAR + 2_000_000_000 + i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 9v" + MAX_VAR);
        }

        ////////////////////////////////////////////////////////////////////////
        // With overlap cases
        ////////////////////////////////////////////////////////////////////////
        {//TEST: single-elem phrase, small value, with overlap
            final int NUM_ITER = ITERATION_FACTOR * 1000;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 1; i++) {
                newPhrase.set(i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 11v" + MAX_VAR);
        }
        {//TEST: small phrase, small close values, with overlap
            final int NUM_ITER = ITERATION_FACTOR * 1000;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 5; i++) {
                newPhrase.set(i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 14v" + MAX_VAR);
        }
        {//TEST: large phrase, small close values, with overlap
            final int NUM_ITER = ITERATION_FACTOR * 1000;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 100; i++) {
                newPhrase.set(i);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 17v" + MAX_VAR);
        }
        {//TEST: single-elem phrase, large value, with overlap
            final int NUM_ITER = ITERATION_FACTOR * 1000;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 1; i++) {
                newPhrase.set(200_000 + i);
            }
            compareAppendPhrase(NUM_ITER, midDNF, newPhrase, "CASE 12v" + MAX_VAR);
        }
        {//TEST: small phrase, large close values, with overlap
            final int NUM_ITER = ITERATION_FACTOR * 1000;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 5; i++) {
                newPhrase.set(200_000 + i);
            }
            compareAppendPhrase(NUM_ITER, midDNF, newPhrase, "CASE 15v" + MAX_VAR);
        }
        {//TEST: large phrase, large close values, with overlap
            final int NUM_ITER = ITERATION_FACTOR * 1000;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 100; i++) {
                newPhrase.set(200_000 + i);
            }
            compareAppendPhrase(NUM_ITER, midDNF, newPhrase, "CASE 18v" + MAX_VAR);
        }
        {//TEST: single-elem phrase, very large value, with overlap
            final int NUM_ITER = ITERATION_FACTOR * 10;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 1; i++) {
                newPhrase.set(2_000_000_000 + i);
            }
            compareAppendPhrase(NUM_ITER, highDNF, newPhrase, "CASE 13v" + MAX_VAR);
        }
        {//TEST: small phrase, very large close values, with overlap
            final int NUM_ITER = ITERATION_FACTOR * 10;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 5; i++) {
                newPhrase.set(2_000_000_000 + i);
            }
            compareAppendPhrase(NUM_ITER, highDNF, newPhrase, "CASE 16v" + MAX_VAR);
        }
        {//TEST: large phrase, very large close values, with overlap
            final int NUM_ITER = ITERATION_FACTOR * 1;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 100; i++) {
                newPhrase.set(2_000_000_000 + i);
            }
            compareAppendPhrase(NUM_ITER, highDNF, newPhrase, "CASE 19v" + MAX_VAR);
        }

        ////////////////////////////////////////////////////////////////////////
        // Sparse value cases
        ////////////////////////////////////////////////////////////////////////
        {//TEST: small phrase, small sparse values
            final int NUM_ITER = ITERATION_FACTOR * 1000;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 5; i++) {
                newPhrase.set(i * 2_000);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 24Lv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER, midDNF, newPhrase, "CASE 24Mv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER / 100, highDNF, newPhrase, "CASE 24Hv" + MAX_VAR);
        }
        {//TEST: large phrase, small sparse values
            final int NUM_ITER = ITERATION_FACTOR * 10;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 100; i++) {
                newPhrase.set(i * 2_000);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 27Lv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER, midDNF, newPhrase, "CASE 27Mv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER / 10, highDNF, newPhrase, "CASE 27Hv" + MAX_VAR);
        }
        {//TEST: small phrase, large sparse values
            final int NUM_ITER = ITERATION_FACTOR * 1000;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 5; i++) {
                newPhrase.set((i + 1) * 200_000);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 25Lv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER, midDNF, newPhrase, "CASE 25Mv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER / 1000, highDNF, newPhrase, "CASE 25Hv" + MAX_VAR);
        }
        {//TEST: large phrase, large sparse values
            final int NUM_ITER = ITERATION_FACTOR * 10;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 100; i++) {
                newPhrase.set((i + 1) * 200_000);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 28Lv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER, midDNF, newPhrase, "CASE 28Mv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER / 10, highDNF, newPhrase, "CASE 28Hv" + MAX_VAR);
        }
        {//TEST: small phrase, very large sparse values
            final int NUM_ITER = ITERATION_FACTOR * 100;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 5; i++) {
                newPhrase.set((i + 1) * 20_000_000);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 26Lv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER, midDNF, newPhrase, "CASE 26Mv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER / 10, highDNF, newPhrase, "CASE 26Hv" + MAX_VAR);
        }
        {//TEST: large phrase, very large sparse values
            final int NUM_ITER = ITERATION_FACTOR * 1;
            SparseBitSet newPhrase = new SparseBitSet();
            for (int i = 0; i < 100; i++) {
                newPhrase.set((i + 1) * 20_000_000);
            }
            compareAppendPhrase(NUM_ITER, lowDNF, newPhrase, "CASE 29Lv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER, midDNF, newPhrase, "CASE 29Mv" + MAX_VAR);
            compareAppendPhrase(NUM_ITER, highDNF, newPhrase, "CASE 29Hv" + MAX_VAR);
        }
    }

    @Test
    public void test_appendPhraseToEachPhrase_V5() {
        appendPhraseToEachPhrase_Helper(5, 100);
        //    CASE 1v5: (ms)(100000) v1=515.8811890004358, v2=514.890410000445
        //    CASE 1v5: (ms/iter) v1=0.0051588118900043585, v2=0.005148904100004449
        //      OBSERVATION: v2 is slightly faster
        //    CASE 4v5: (ms)(10000) v1=254.6452099999921, v2=101.13572000001875
        //    CASE 4v5: (ms/iter) v1=0.02546452099999921, v2=0.010113572000001875
        //      OBSERVATION: v2 is 2.5x faster
        //    CASE 7v5: (ms)(10000) v1=5141.429631999884, v2=1044.005683000035
        //    CASE 7v5: (ms/iter) v1=0.5141429631999884, v2=0.10440056830000351
        //      OBSERVATION: v2 is 4.9x faster
        //    CASE 2v5: (ms)(10000) v1=89.7956380000099, v2=89.70847100001006
        //    CASE 2v5: (ms/iter) v1=0.00897956380000099, v2=0.008970847100001006
        //      OBSERVATION: v2 is slightly faster
        //    CASE 5v5: (ms)(10000) v1=408.1971000000468, v2=140.6786589999963
        //    CASE 5v5: (ms/iter) v1=0.04081971000000468, v2=0.01406786589999963
        //      OBSERVATION: v2 is 2.9x faster
        //    CASE 8v5: (ms)(10000) v1=8077.044369999833, v2=1097.4551179999937
        //    CASE 8v5: (ms/iter) v1=0.8077044369999833, v2=0.10974551179999938
        //      OBSERVATION: v2 is 7.4x faster
        //    CASE 3v5: (ms)(1000) v1=2725.946587000002, v2=2723.1154620000025
        //    CASE 3v5: (ms/iter) v1=2.7259465870000024, v2=2.7231154620000027
        //      OBSERVATION: v2 is slightly faster
        //    CASE 6v5: (ms)(1000) v1=13011.843437000012, v2=2732.4882360000056
        //    CASE 6v5: (ms/iter) v1=13.011843437000012, v2=2.732488236000006
        //      OBSERVATION: v2 is 4.8x faster
        //    CASE 9v5: (ms)(100) v1=25691.335448999995, v2=284.21690599999994
        //    CASE 9v5: (ms/iter) v1=256.91335448999996, v2=2.8421690599999994
        //      OBSERVATION: v2 is 90x faster!
        //    CASE 11v5: (ms)(100000) v1=438.88653100003415, v2=438.40601100002266
        //    CASE 11v5: (ms/iter) v1=0.004388865310000342, v2=0.004384060110000226
        //      OBSERVATION: v2 is slightly faster
        //    CASE 14v5: (ms)(100000) v1=812.6056969985071, v2=785.2723899986369
        //    CASE 14v5: (ms/iter) v1=0.008126056969985071, v2=0.007852723899986369
        //      OBSERVATION: v2 is slightly faster
        //    CASE 17v5: (ms)(100000) v1=4377.464716996918, v2=1923.3067379998881
        //    CASE 17v5: (ms/iter) v1=0.04377464716996918, v2=0.019233067379998883
        //      OBSERVATION: v2 is 2.3x faster
        //    CASE 12v5: (ms)(100000) v1=436.95740999990227, v2=437.1309919999051
        //    CASE 12v5: (ms/iter) v1=0.004369574099999022, v2=0.004371309919999051
        //      OBSERVATION: v2 is slightly SLOWER (1.74E-6 ms/iteration)
        //    CASE 15v5: (ms)(100000) v1=804.3777479984733, v2=749.4450159992147
        //    CASE 15v5: (ms/iter) v1=0.008043777479984733, v2=0.007494450159992147
        //      OBSERVATION: v2 is slightly faster
        //    CASE 18v5: (ms)(100000) v1=4555.301311001888, v2=1932.838567999941
        //    CASE 18v5: (ms/iter) v1=0.045553013110018886, v2=0.01932838567999941
        //      OBSERVATION: v2 is 2.3x faster
        //    CASE 13v5: (ms)(1000) v1=1332.7303740000002, v2=1334.6968929999991
        //    CASE 13v5: (ms/iter) v1=1.3327303740000003, v2=1.3346968929999992
        //      OBSERVATION: v2 is slightly SLOWER (1.96E-3 ms/iteration)
        //    CASE 16v5: (ms)(1000) v1=2283.317797000001, v2=1635.9822669999985
        //    CASE 16v5: (ms/iter) v1=2.283317797000001, v2=1.6359822669999986
        //      OBSERVATION: v2 is 1.4x faster
        //    CASE 19v5: (ms)(100) v1=1449.0538390000008, v2=165.52418300000008
        //    CASE 19v5: (ms/iter) v1=14.490538390000008, v2=1.6552418300000007
        //      OBSERVATION: v2 is 8.8x faster
        //    CASE 24Lv10: (ms)(10000) v1=9424.897278999937, v2=6276.627106999983
        //    CASE 24Lv10: (ms/iter) v1=0.9424897278999937, v2=0.6276627106999983
        //      OBSERVATION: v2 is 1.5x faster
        //    CASE 24Mv10: (ms)(10000) v1=11782.037944000034, v2=3789.764839000002
        //    CASE 24Mv10: (ms/iter) v1=1.1782037944000034, v2=0.3789764839000002
        //      OBSERVATION: v2 is 3.1x faster
        //    CASE 24Hv10: (ms)(100) v1=32389.861630000003, v2=6453.6088500000005
        //    CASE 24Hv10: (ms/iter) v1=323.8986163, v2=64.5360885
        //      OBSERVATION: v2 is 5x faster
        //    CASE 27Lv10: (ms)(100) v1=9588.428252999996, v2=448.67900199999985
        //    CASE 27Lv10: (ms/iter) v1=95.88428252999996, v2=4.486790019999998
        //      OBSERVATION: v2 is 21x faster
        //    CASE 27Mv10: (ms)(100) v1=24948.914861000012, v2=921.6640050000001
        //    CASE 27Mv10: (ms/iter) v1=249.48914861000011, v2=9.21664005
        //      OBSERVATION: v2 is 27x faster
        //    CASE 27Hv10: (ms)(10) v1=66604.887066, v2=749.136312
        //    CASE 27Hv10: (ms/iter) v1=6660.488706599999, v2=74.9136312
        //      OBSERVATION: v2 is 89x faster
        //    CASE 25Lv10: (ms)(10000) v1=19460.752765999954, v2=6528.103722000045
        //    CASE 25Lv10: (ms/iter) v1=1.9460752765999954, v2=0.6528103722000045
        //      OBSERVATION: v2 is 3x faster
        //    CASE 25Mv10: (ms)(10000) v1=13037.86308400002, v2=6890.0038359999835
        //    CASE 25Mv10: (ms/iter) v1=1.303786308400002, v2=0.6890003835999984
        //      OBSERVATION: v2 is 1.9x faster
        //    CASE 25Hv10: (ms)(10) v1=3114.2272059999996, v2=627.318509
        //    CASE 25Hv10: (ms/iter) v1=311.42272059999993, v2=62.7318509
        //      OBSERVATION: v2 is 5x faster
        //    CASE 28Lv10: (ms)(100) v1=67484.112053, v2=1865.9725429999996
        //    CASE 28Lv10: (ms/iter) v1=674.84112053, v2=18.659725429999995
        //      OBSERVATION: v2 is 36x faster
        //    CASE 28Mv10: (ms)(100) v1=32159.504344000008, v2=1046.2997980000002
        //    CASE 28Mv10: (ms/iter) v1=321.5950434400001, v2=10.462997980000003
        //      OBSERVATION: v2 is 31x faster
        //    CASE 28Hv10: (ms)(10) v1=68242.809173, v2=928.2302949999998
        //    CASE 28Hv10: (ms/iter) v1=6824.2809173000005, v2=92.82302949999999
        //      OBSERVATION: v2 is 74x faster
        //    CASE 26Lv10: (ms)(1000) v1=16416.453967000005, v2=6039.559457999998
        //    CASE 26Lv10: (ms/iter) v1=16.416453967000006, v2=6.039559457999998
        //      OBSERVATION: v2 is 2.7x faster
        //    CASE 26Mv10: (ms)(1000) v1=16485.686475000002, v2=5994.700238999993
        //    CASE 26Mv10: (ms/iter) v1=16.485686475, v2=5.994700238999993
        //      OBSERVATION: v2 is 2.8x faster
        //    CASE 26Hv10: (ms)(100) v1=31203.499746, v2=6286.869442
        //    CASE 26Hv10: (ms/iter) v1=312.03499746, v2=62.868694420000004
        //      OBSERVATION: v2 is 5x faster
        //    CASE 29Lv10: (ms)(10) v1=45683.76579700001, v2=1042.8885719999998
        //    CASE 29Lv10: (ms/iter) v1=4568.376579700001, v2=104.28885719999998
        //      OBSERVATION: v2 is 44x faster
        //    CASE 29Mv10: (ms)(10) v1=45908.477253000005, v2=1073.6922960000002
        //    CASE 29Mv10: (ms/iter) v1=4590.8477253, v2=107.36922960000001
        //      OBSERVATION: v2 is 43x faster
        //    CASE 29Hv10: (ms)(10) v1=70780.06114800001, v2=3880.1507690000003
        //    CASE 29Hv10: (ms/iter) v1=7078.0061148, v2=388.01507690000005
        //      OBSERVATION: v2 is 18x faster
        //
        //OBSERVATION: on average, v2 is ~15x faster
    }

    @Test
    public void test_appendPhraseToEachPhrase_V10() {
        appendPhraseToEachPhrase_Helper(10, 10);
        //    CASE 1v10: (ms)(10000) v1=1307.6245840000208, v2=1306.2804900000126
        //    CASE 1v10: (ms/iter) v1=0.13076245840000209, v2=0.13062804900000127
        //      OBSERVATION: v2 is slightly faster
        //    CASE 4v10: (ms)(1000) v1=664.3969969999994, v2=269.8711780000009
        //    CASE 4v10: (ms/iter) v1=0.6643969969999993, v2=0.2698711780000009
        //      OBSERVATION: v2 is 2.5x faster
        //    CASE 7v10: (ms)(1000) v1=12911.979623999989, v2=2479.4551989999995
        //    CASE 7v10: (ms/iter) v1=12.911979623999988, v2=2.4794551989999993
        //      OBSERVATION: v2 is 5.2x faster
        //    CASE 2v10: (ms)(1000) v1=227.45923600000035, v2=227.53807000000032
        //    CASE 2v10: (ms/iter) v1=0.22745923600000034, v2=0.22753807000000031
        //      OBSERVATION: v2 is slightly SLOWER (7.88E-5 ms/iteration)
        //    CASE 5v10: (ms)(1000) v1=983.622854000001, v2=328.04807900000014
        //    CASE 5v10: (ms/iter) v1=0.9836228540000009, v2=0.32804807900000016
        //      OBSERVATION: v2 is 3x faster
        //    CASE 8v10: (ms)(1000) v1=19769.232551000005, v2=2750.3995630000013
        //    CASE 8v10: (ms/iter) v1=19.769232551000005, v2=2.750399563000001
        //      OBSERVATION: v2 is 7.2x faster
        //    CASE 3v10: (ms)(100) v1=6832.843483000001, v2=6800.782253000001
        //    CASE 3v10: (ms/iter) v1=68.32843483, v2=68.00782253000001
        //      OBSERVATION: v2 is slightly faster
        //    CASE 6v10: (ms)(100) v1=30742.840986999996, v2=6531.717395000001
        //    CASE 6v10: (ms/iter) v1=307.42840986999994, v2=65.31717395000001
        //      OBSERVATION: v2 is 4.7x faster
        //    CASE 9v10: (ms)(10) v1=59914.46097, v2=733.6693799999998
        //    CASE 9v10: (ms/iter) v1=5991.446097, v2=73.36693799999998
        //      OBSERVATION: v2 is 81x faster
        //    CASE 11v10: (ms)(10000) v1=5672.319764000043, v2=5663.115367000066
        //    CASE 11v10: (ms/iter) v1=0.5672319764000042, v2=0.5663115367000066
        //      OBSERVATION: v2 is slightly faster
        //    CASE 14v10: (ms)(10000) v1=9887.761905000014, v2=12198.165579000071
        //    CASE 14v10: (ms/iter) v1=0.9887761905000014, v2=1.219816557900007
        //      OBSERVATION: v2 is a bit SLOWER (2.31E-1 ms/iteration)
        //    CASE 17v10: (ms)(10000) v1=11476.973050999952, v2=8167.801877999947
        //    CASE 17v10: (ms/iter) v1=1.1476973050999952, v2=0.8167801877999946
        //      OBSERVATION: v2 is 1.4x faster
        //    CASE 12v10: (ms)(10000) v1=4924.644568999993, v2=4928.468408000007
        //    CASE 12v10: (ms/iter) v1=0.4924644568999993, v2=0.4928468408000007
        //      OBSERVATION: v2 is slightly SLOWER (3.82E-4 ms/iteration)
        //    CASE 15v10: (ms)(10000) v1=8779.559460999984, v2=11574.608468000017
        //    CASE 15v10: (ms/iter) v1=0.8779559460999984, v2=1.1574608468000016
        //      OBSERVATION: v2 is a bit SLOWER (2.80E-1 ms/iteration)
        //    CASE 18v10: (ms)(10000) v1=10360.055960000062, v2=7255.6219499999825
        //    CASE 18v10: (ms/iter) v1=1.036005596000006, v2=0.7255621949999983
        //      OBSERVATION: v2 is 1.4x faster
        //    CASE 13v10: (ms)(100) v1=3403.8128329999995, v2=3401.269486999999
        //    CASE 13v10: (ms/iter) v1=34.03812832999999, v2=34.01269486999999
        //      OBSERVATION: v2 is slightly faster
        //    CASE 16v10: (ms)(100) v1=6244.019469, v2=3628.841640999999
        //    CASE 16v10: (ms/iter) v1=62.44019469, v2=36.28841640999999
        //      OBSERVATION: v2 is 1.7x faster
        //    CASE 19v10: (ms)(10) v1=748.9720930000001, v2=374.987939
        //    CASE 19v10: (ms/iter) v1=74.89720930000001, v2=37.498793899999995
        //      OBSERVATION: v2 is 2x faster
        //    CASE 24Lv5: (ms)(100000) v1=1750.5415989996193, v2=920.8950189990386
        //    CASE 24Lv5: (ms/iter) v1=0.017505415989996193, v2=0.009208950189990387
        //      OBSERVATION: v2 is 1.9x faster
        //    CASE 24Mv5: (ms)(100000) v1=4627.89659900088, v2=1465.8300159997289
        //    CASE 24Mv5: (ms/iter) v1=0.0462789659900088, v2=0.014658300159997289
        //      OBSERVATION: v2 is 3.2x faster
        //    CASE 24Hv5: (ms)(1000) v1=13559.192401999979, v2=2740.6712610000022
        //    CASE 24Hv5: (ms/iter) v1=13.55919240199998, v2=2.7406712610000024
        //      OBSERVATION: v2 is 4.9x faster
        //    CASE 27Lv5: (ms)(1000) v1=2313.171957, v2=134.8120179999999
        //    CASE 27Lv5: (ms/iter) v1=2.3131719570000002, v2=0.13481201799999992
        //      OBSERVATION: v2 is 17x faster
        //    CASE 27Mv5: (ms)(1000) v1=4948.810579999993, v2=258.9218920000002
        //    CASE 27Mv5: (ms/iter) v1=4.948810579999993, v2=0.2589218920000002
        //      OBSERVATION: v2 is 19x faster
        //    CASE 27Hv5: (ms)(100) v1=27138.971668, v2=299.3073640000001
        //    CASE 27Hv5: (ms/iter) v1=271.38971668, v2=2.9930736400000013
        //      OBSERVATION: v2 is 91x faster
        //    CASE 25Lv5: (ms)(100000) v1=8136.994034998371, v2=2740.913935002307
        //    CASE 25Lv5: (ms/iter) v1=0.08136994034998371, v2=0.02740913935002307
        //      OBSERVATION: v2 is 3x faster
        //    CASE 25Mv5: (ms)(100000) v1=3522.7413319979446, v2=1541.9629699996838
        //    CASE 25Mv5: (ms/iter) v1=0.035227413319979445, v2=0.015419629699996837
        //      OBSERVATION: v2 is 2.3x faster
        //    CASE 25Hv5: (ms)(100) v1=1352.2584429999997, v2=274.00497200000007
        //    CASE 25Hv5: (ms/iter) v1=13.522584429999997, v2=2.7400497200000005
        //      OBSERVATION: v2 is 4.9x faster
        //    CASE 28Lv5: (ms)(1000) v1=18462.463117000007, v2=616.8097580000009
        //    CASE 28Lv5: (ms/iter) v1=18.462463117000006, v2=0.6168097580000009
        //      OBSERVATION: v2 is 30x faster
        //    CASE 28Mv5: (ms)(1000) v1=8935.772047999995, v2=294.72115199999973
        //    CASE 28Mv5: (ms/iter) v1=8.935772047999995, v2=0.2947211519999997
        //      OBSERVATION: v2 is 30x faster
        //    CASE 28Hv5: (ms)(100) v1=28416.931847000007, v2=323.919225
        //    CASE 28Hv5: (ms/iter) v1=284.16931847000006, v2=3.23919225
        //      OBSERVATION: v2 is 88x faster
        //    CASE 26Lv5: (ms)(10000) v1=6679.047468000059, v2=2372.344471999934
        //    CASE 26Lv5: (ms/iter) v1=0.6679047468000059, v2=0.23723444719999337
        //      OBSERVATION: v2 is 2.8x faster
        //    CASE 26Mv5: (ms)(10000) v1=6688.728078000036, v2=2405.4254059999275
        //    CASE 26Mv5: (ms/iter) v1=0.6688728078000036, v2=0.24054254059999275
        //      OBSERVATION: v2 is 2.8x faster
        //    CASE 26Hv5: (ms)(1000) v1=13343.329031000007, v2=2693.138173
        //    CASE 26Hv5: (ms/iter) v1=13.343329031000007, v2=2.693138173
        //      OBSERVATION: v2 is 5x faster
        //    CASE 29Lv5: (ms)(100) v1=19247.25129600001, v2=482.834843
        //    CASE 29Lv5: (ms/iter) v1=192.4725129600001, v2=4.82834843
        //      OBSERVATION: v2 is 40x faster
        //    CASE 29Mv5: (ms)(100) v1=19246.307032999997, v2=472.22056600000025
        //    CASE 29Mv5: (ms/iter) v1=192.46307032999997, v2=4.722205660000003
        //      OBSERVATION: v2 is 41x faster
        //    CASE 29Hv5: (ms)(100) v1=28307.351204999995, v2=299.93262799999985
        //    CASE 29Hv5: (ms/iter) v1=283.07351205, v2=2.9993262799999987
        //      OBSERVATION: v2 is 94x faster
        //
        //OBSERVATION: on average, v2 is ~17x faster
    }
}

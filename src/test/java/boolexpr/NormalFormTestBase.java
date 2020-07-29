package boolexpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Timothy Hoffman
 *
 * @param <P> phrase type
 * @param <E> element type
 * @param <D> normal form type
 */
public abstract class NormalFormTestBase<P, E, D extends NormalForm<P, E, D>> {

    protected static final boolean RUN_PERFORMANCE_TESTS = false;

    protected abstract Construction<P, E, D> getCons();

    @Test
    public void testEmptyConstructor() {
        System.out.println("test_EmptyConstructor");
        Construction<P, E, D> cons = getCons();

        D instance = cons.newFromEmpty();

        Assert.assertTrue(instance.isEmpty());
        Assert.assertTrue(cons.isDisjunctive() ? instance.isFalse() : instance.isTrue());
        Assert.assertFalse(cons.isDisjunctive() ? instance.isTrue() : instance.isFalse());
        Assert.assertEquals(0, instance.getNumPhrases());
        Assert.assertEquals(0, instance.getNumProps());
        Assert.assertEquals("", instance.toString(TestHelpers.FORMAT, true));
    }

    @Test
    public void testPropConstructor() {
        System.out.println("test_PropConstructor");
        Construction<P, E, D> cons = getCons();
        {
            D instance = cons.newFromElem(cons.getElemFor(5));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertFalse(instance.isFalse());
            Assert.assertFalse(instance.isTrue());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(1, instance.getNumProps());
            Assert.assertEquals("(5)", instance.toString(TestHelpers.FORMAT, true));
        }
        try {
            D instance = cons.newFromElem(null);

            Assert.assertFalse(instance.isEmpty());
            Assert.assertFalse(cons.isDisjunctive() ? instance.isFalse() : instance.isTrue());
            Assert.assertTrue(cons.isDisjunctive() ? instance.isTrue() : instance.isFalse());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(0, instance.getNumProps());
            Assert.assertEquals("()", instance.toString(TestHelpers.FORMAT, true));
        } catch (NullPointerException ex) {
            //<init>(FormRules,PropType) is allowed to throw
            //  NullPointerException if the proposition is null
            System.out.println("single-proposition constructor threw NPE");
        }
    }

    @Test
    public void testPhraseConstructor() {
        System.out.println("test_PhraseConstructor");
        Construction<P, E, D> cons = getCons();
        {
            D instance = cons.newFromPhrase(cons.buildPhrase(5, 6, 7, 8, 63));//fully-formed phrase

            Assert.assertFalse(instance.isEmpty());
            Assert.assertFalse(instance.isFalse());
            Assert.assertFalse(instance.isTrue());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(5, instance.getNumProps());
            Assert.assertEquals("(5,6,7,8,63)", instance.toString(TestHelpers.FORMAT, true));
        }
        {
            D instance = cons.newFromPhrase(cons.buildPhrase());//empty phrase

            Assert.assertFalse(instance.isEmpty());
            Assert.assertFalse(cons.isDisjunctive() ? instance.isFalse() : instance.isTrue());
            Assert.assertTrue(cons.isDisjunctive() ? instance.isTrue() : instance.isFalse());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(0, instance.getNumProps());
            Assert.assertEquals("()", instance.toString(TestHelpers.FORMAT, true));
        }
        try {
            D instance = cons.newFromPhrase(null);//null phrase

            Assert.assertTrue(instance.isEmpty());
            Assert.assertTrue(cons.isDisjunctive() ? instance.isFalse() : instance.isTrue());
            Assert.assertFalse(cons.isDisjunctive() ? instance.isTrue() : instance.isFalse());
            Assert.assertEquals(0, instance.getNumPhrases());
            Assert.assertEquals(0, instance.getNumProps());
            Assert.assertEquals("", instance.toString(TestHelpers.FORMAT, true));
        } catch (NullPointerException ex) {
            //<init>(FormRules,PhraseType) is allowed to throw
            //  NullPointerException if the phrase is null
            System.out.println("single-phrase constructor threw NPE");
        }
    }

    /**
     * Tests copy constructors.
     *
     * Ensures the objects match on equals() but not referential equality
     */
    @Test
    public void testCopyConstructor() {
        System.out.println("test_CopyConstructor");
        Construction<P, E, D> cons = getCons();

        D instance1 = cons.buildSentence(Arrays.asList(cons.buildPhrase(2, 4), cons.buildPhrase(2, 5, 7)));
        D instance2 = cons.newFromClone(instance1, false);

        //ensure outer level references are not the same object
        Assert.assertNotSame("clone object is the same as original", instance1, instance2);

        //ensure sizes are equal
        Assert.assertEquals("data are not the same size", instance1.getNumPhrases(), instance2.getNumPhrases());
        Assert.assertEquals("data are not the same size", instance1.getNumProps(), instance2.getNumProps());

        //for each phrase in instance1...
        for (P phr1 : instance1.data) {
            //find a phrase in instance2 which matches on equals()
            P phr2 = null;
            for (P temp : instance2.data) {
                if (phr1.equals(temp)) {
                    if (phr2 == null) {
                        phr2 = temp;    //if not yet found, save it
                    } else {
                        //if already found, error.
                        Assert.fail("multiple phrases in clone match original");
                    }
                }
            }

            //ensure such a phrase was found
            Assert.assertNotNull("no matching phrase found in clone", phr2);

            //ensure the phrases are not the same object
            Assert.assertNotSame("phrase reference matches", phr1, phr2);

            //ensure the items phr1 and phr2 are the same
            //NOTE: this is gauranteed becasue phr2 was found
        }
    }

    @Test
    public void test_tryAddWithAbsorption() {
        System.out.println("test_tryAddWithAbsorption");
        Construction<P, E, D> cons = getCons();

        D instance = cons.newFromPhrase(cons.buildPhrase(1, 4, 7));

        //Add a non-overlapping phrase
        instance.tryAddWithAbsorption(cons.buildPhrase(2, 3));

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(2, instance.getNumPhrases());
        Assert.assertEquals(5, instance.getNumProps());
        Assert.assertEquals("(2,3)(1,4,7)", instance.toString(TestHelpers.FORMAT, true));

        //Add a partially overlapping phrase that does not cause absorption
        instance.tryAddWithAbsorption(cons.buildPhrase(1, 4, 9));

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(3, instance.getNumPhrases());
        Assert.assertEquals(8, instance.getNumProps());
        Assert.assertEquals("(2,3)(1,4,7)(1,4,9)", instance.toString(TestHelpers.FORMAT, true));

        //Add a phrase that is absorbed
        instance.tryAddWithAbsorption(cons.buildPhrase(1, 4, 9, 27));

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(3, instance.getNumPhrases());
        Assert.assertEquals(8, instance.getNumProps());
        Assert.assertEquals("(2,3)(1,4,7)(1,4,9)", instance.toString(TestHelpers.FORMAT, true));

        //Add a phrase that absorbs 1 phrase
        instance.tryAddWithAbsorption(cons.buildPhrase(2));

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(3, instance.getNumPhrases());
        Assert.assertEquals(7, instance.getNumProps());
        Assert.assertEquals("(2)(1,4,7)(1,4,9)", instance.toString(TestHelpers.FORMAT, true));

        //Add a phrase that absorbs 2 phrases
        instance.tryAddWithAbsorption(cons.buildPhrase(1, 4));

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(2, instance.getNumPhrases());
        Assert.assertEquals(3, instance.getNumProps());
        Assert.assertEquals("(2)(1,4)", instance.toString(TestHelpers.FORMAT, true));

        //Add the empty phrase that absorbs everything
        instance.tryAddWithAbsorption(cons.buildPhrase());

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(1, instance.getNumPhrases());
        Assert.assertEquals(0, instance.getNumProps());
        Assert.assertEquals("()", instance.toString(TestHelpers.FORMAT, true));

        //Add an arbitrary phrase, will be absorbed by the empty
        instance.tryAddWithAbsorption(cons.buildPhrase(4, 5));

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(1, instance.getNumPhrases());
        Assert.assertEquals(0, instance.getNumProps());
        Assert.assertEquals("()", instance.toString(TestHelpers.FORMAT, true));
    }

    @Test
    public void test_addSingletonPhrase() {
        System.out.println("test_addSingletonPhrase");
        Construction<P, E, D> cons = getCons();

        D instance = cons.newFromPhrase(cons.buildPhrase(1, 4, 7));

        //Add a non-overlapping phrase
        instance.addSingletonPhrase(cons.getElemFor(6));

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(2, instance.getNumPhrases());
        Assert.assertEquals(4, instance.getNumProps());
        Assert.assertEquals("(6)(1,4,7)", instance.toString(TestHelpers.FORMAT, true));

        //Add a singleton phrase that already exists (i.e. is absorbed/absorbs)
        instance.addSingletonPhrase(cons.getElemFor(6));

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(2, instance.getNumPhrases());
        Assert.assertEquals(4, instance.getNumProps());
        Assert.assertEquals("(6)(1,4,7)", instance.toString(TestHelpers.FORMAT, true));

        //Add a phrase that absorbs 1 phrase
        instance.addSingletonPhrase(cons.getElemFor(7));

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(2, instance.getNumPhrases());
        Assert.assertEquals(2, instance.getNumProps());
        Assert.assertEquals("(6)(7)", instance.toString(TestHelpers.FORMAT, true));

        //Add the empty phrase that absorbs everything
        //NOTE: this does depend on the definition of createSingleton(ElemType)
        //  to NOT add a null entry to the set
        instance.addSingletonPhrase(null);

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(1, instance.getNumPhrases());
        Assert.assertEquals(0, instance.getNumProps());
        Assert.assertEquals("()", instance.toString(TestHelpers.FORMAT, true));

        //Add an arbitrary phrase, will be absorbed by the empty
        instance.addSingletonPhrase(cons.getElemFor(63));

        Assert.assertFalse(instance.isEmpty());
        Assert.assertEquals(1, instance.getNumPhrases());
        Assert.assertEquals(0, instance.getNumProps());
        Assert.assertEquals("()", instance.toString(TestHelpers.FORMAT, true));
    }

    @Test
    public void test_appendAllPhrases() {
        System.out.println("test_appendAllPhrases");
        Construction<P, E, D> cons = getCons();

        {
            D instance = cons.newFromPhrase(cons.buildPhrase(1, 4, 7));

            //Append to a single phrase (new value)
            instance.appendElemToEachPhrase(cons.getElemFor(6));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(4, instance.getNumProps());
            Assert.assertEquals("(1,4,6,7)", instance.toString(TestHelpers.FORMAT, true));

            //Append to a single phrase (existing value)
            instance.appendElemToEachPhrase(cons.getElemFor(1));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(4, instance.getNumProps());
            Assert.assertEquals("(1,4,6,7)", instance.toString(TestHelpers.FORMAT, true));

            //Append to more than one phrase
            instance.tryAddWithAbsorption(cons.buildPhrase(2, 3, 5));
            instance.appendElemToEachPhrase(cons.getElemFor(8));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(2, instance.getNumPhrases());
            Assert.assertEquals(9, instance.getNumProps());
            Assert.assertEquals("(2,3,5,8)(1,4,6,7,8)", instance.toString(TestHelpers.FORMAT, true));

            //Append where some (but not all) phrases already contain the value
            instance.appendElemToEachPhrase(cons.getElemFor(2));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(2, instance.getNumPhrases());
            Assert.assertEquals(10, instance.getNumProps());
            Assert.assertEquals("(2,3,5,8)(1,2,4,6,7,8)", instance.toString(TestHelpers.FORMAT, true));

            //Append 'null' value (i.e. no effect)
            instance.appendElemToEachPhrase(null);

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(2, instance.getNumPhrases());
            Assert.assertEquals(10, instance.getNumProps());
            Assert.assertEquals("(2,3,5,8)(1,2,4,6,7,8)", instance.toString(TestHelpers.FORMAT, true));
        }

        {
            //Append something to the totally empty set
            D instanceF = cons.newFromEmpty();

            Assert.assertTrue(instanceF.isEmpty());
            Assert.assertEquals(0, instanceF.getNumPhrases());
            Assert.assertEquals(0, instanceF.getNumProps());
            Assert.assertEquals("", instanceF.toString(TestHelpers.FORMAT, true));

            instanceF.appendElemToEachPhrase(cons.getElemFor(2));

            Assert.assertTrue(instanceF.isEmpty());
            Assert.assertEquals(0, instanceF.getNumPhrases());
            Assert.assertEquals(0, instanceF.getNumProps());
            Assert.assertEquals("", instanceF.toString(TestHelpers.FORMAT, true));

            //Append something to the set containing only the empty phrase
            D instanceT = cons.newFromPhrase(cons.buildPhrase());

            Assert.assertFalse(instanceT.isEmpty());
            Assert.assertEquals(1, instanceT.getNumPhrases());
            Assert.assertEquals(0, instanceT.getNumProps());
            Assert.assertEquals("()", instanceT.toString(TestHelpers.FORMAT, true));

            instanceT.appendElemToEachPhrase(cons.getElemFor(2));

            Assert.assertFalse(instanceT.isEmpty());
            Assert.assertEquals(1, instanceT.getNumPhrases());
            Assert.assertEquals(1, instanceT.getNumProps());
            Assert.assertEquals("(2)", instanceT.toString(TestHelpers.FORMAT, true));
        }
    }

    @Test
    public void test_merge() {
        System.out.println("test_merge");
        Construction<P, E, D> cons = getCons();
        {
            D instance = cons.newFromPhrase(cons.buildPhrase(1, 4, 7));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(3, instance.getNumProps());
            Assert.assertEquals("(1,4,7)", instance.toString(TestHelpers.FORMAT, true));

            //Merge with self (no effect)
            instance.merge(instance);

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(3, instance.getNumProps());
            Assert.assertEquals("(1,4,7)", instance.toString(TestHelpers.FORMAT, true));

            //Merge with identical (but not self) NormalForm (no effect)
            instance.merge(cons.newFromPhrase(cons.buildPhrase(1, 4, 7)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(3, instance.getNumProps());
            Assert.assertEquals("(1,4,7)", instance.toString(TestHelpers.FORMAT, true));

            //Merge with non-overlapping NormalForm
            instance.merge(cons.newFromPhrase(cons.buildPhrase(2, 5, 9)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(2, instance.getNumPhrases());
            Assert.assertEquals(6, instance.getNumProps());
            Assert.assertEquals("(1,4,7)(2,5,9)", instance.toString(TestHelpers.FORMAT, true));

            //Merge with partially overlapping NormalForm that does not cause absorption
            instance.merge(cons.newFromPhrase(cons.buildPhrase(1, 4, 9)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(3, instance.getNumPhrases());
            Assert.assertEquals(9, instance.getNumProps());
            Assert.assertEquals("(1,4,7)(1,4,9)(2,5,9)", instance.toString(TestHelpers.FORMAT, true));

            //Merge with a NormalForm that is absorbed
            instance.merge(cons.newFromPhrase(cons.buildPhrase(1, 4, 9, 29)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(3, instance.getNumPhrases());
            Assert.assertEquals(9, instance.getNumProps());
            Assert.assertEquals("(1,4,7)(1,4,9)(2,5,9)", instance.toString(TestHelpers.FORMAT, true));

            //Merge with a NormalForm that absorbs 1 phrase
            instance.merge(cons.newFromPhrase(cons.buildPhrase(2, 5)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(3, instance.getNumPhrases());
            Assert.assertEquals(8, instance.getNumProps());
            Assert.assertEquals("(2,5)(1,4,7)(1,4,9)", instance.toString(TestHelpers.FORMAT, true));

            //Merge with a NormalForm that absorbs 2 phrases
            instance.merge(cons.newFromPhrase(cons.buildPhrase(1, 4)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(2, instance.getNumPhrases());
            Assert.assertEquals(4, instance.getNumProps());
            Assert.assertEquals("(1,4)(2,5)", instance.toString(TestHelpers.FORMAT, true));

            //Merge with the empty NormalForm (no phrases, no effect)
            instance.merge(cons.newFromEmpty());

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(2, instance.getNumPhrases());
            Assert.assertEquals(4, instance.getNumProps());
            Assert.assertEquals("(1,4)(2,5)", instance.toString(TestHelpers.FORMAT, true));
            {
                //Perform the previous case in the reverse order (same result)
                D instance2 = cons.newFromEmpty();
                instance2.merge(instance);
                Assert.assertFalse(instance2.isEmpty());
                Assert.assertEquals(2, instance2.getNumPhrases());
                Assert.assertEquals(4, instance2.getNumProps());
                Assert.assertEquals("(1,4)(2,5)", instance2.toString(TestHelpers.FORMAT, true));
            }

            //Merge with the single empty phrase NormalForm (absorbs all others)
            instance.merge(cons.newFromPhrase(cons.buildPhrase()));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(0, instance.getNumProps());
            Assert.assertEquals("()", instance.toString(TestHelpers.FORMAT, true));
            {
                //Perform the previous case in the reverse order (same result)
                D instance2 = cons.newFromPhrase(cons.buildPhrase());
                instance2.merge(instance);
                Assert.assertFalse(instance2.isEmpty());
                Assert.assertEquals(1, instance2.getNumPhrases());
                Assert.assertEquals(0, instance2.getNumProps());
                Assert.assertEquals("()", instance2.toString(TestHelpers.FORMAT, true));
            }

            //Merge with an arbitrary NormalForm, will be absorbed by the empty
            instance.merge(cons.newFromPhrase(cons.buildPhrase(1, 4, 34, 12)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(0, instance.getNumProps());
            Assert.assertEquals("()", instance.toString(TestHelpers.FORMAT, true));
        }
        {
            //Build 2 more complex NormalForms and merge them
            D instanceA = cons.newFromPhrase(cons.buildPhrase(1, 4));
            instanceA.merge(cons.newFromPhrase(cons.buildPhrase(1, 5, 9)));
            instanceA.merge(cons.newFromPhrase(cons.buildPhrase(5, 7, 9)));
            Assert.assertEquals("(1,4)(1,5,9)(5,7,9)", instanceA.toString(TestHelpers.FORMAT, true));
            D instanceB = cons.newFromPhrase(cons.buildPhrase(1, 4, 7));
            instanceB.merge(cons.newFromPhrase(cons.buildPhrase(1, 9)));
            instanceB.merge(cons.newFromPhrase(cons.buildPhrase(4, 9)));
            Assert.assertEquals("(1,9)(4,9)(1,4,7)", instanceB.toString(TestHelpers.FORMAT, true));

            instanceA.merge(instanceB);

            Assert.assertFalse(instanceA.isEmpty());
            Assert.assertEquals(4, instanceA.getNumPhrases());
            Assert.assertEquals(9, instanceA.getNumProps());
            Assert.assertEquals("(1,4)(1,9)(4,9)(5,7,9)", instanceA.toString(TestHelpers.FORMAT, true));
        }
        {
            //Build 2 even more complex NormalForms and merge them.
            //
            //This test is meant to test the multithreading case. To do that, we
            //  use the formula for USE_THREADS from NormalForm#merge(..) and
            //  find a value for A and B (let them be equal for simplicity) such
            //  that the formula is true and then build 2 sentences, each with
            //  that number of phrases.

            D instanceA = cons.newFromEmpty();
            D instanceB = cons.newFromEmpty();
            D instanceC = cons.newFromEmpty();
            int[] tempPhrase = new int[5];
            int A, N = Runtime.getRuntime().availableProcessors();
            for (A = 0; !(A * (A / N) + (N * N) + 3000 - (A * A) < 0); A++) {
                //Append a random phrase to A and C
                for (int i = 0; i < tempPhrase.length; i++) {
                    tempPhrase[i] = TestHelpers.getRandomInRange(0, 64);//63 is max for {@lnk test.BLOCK}
                }
                instanceA.merge(cons.newFromPhrase(cons.buildPhrase(tempPhrase)));
                instanceC.merge(cons.newFromPhrase(cons.buildPhrase(tempPhrase)));

                //Append a random phrase to B and C
                for (int i = 0; i < tempPhrase.length; i++) {
                    tempPhrase[i] = TestHelpers.getRandomInRange(0, 64);//max for Enum
                }
                instanceB.merge(cons.newFromPhrase(cons.buildPhrase(tempPhrase)));
                instanceC.merge(cons.newFromPhrase(cons.buildPhrase(tempPhrase)));
            }
            //It's possible that one of the randomly generated phrases absorbed
            //  another and might result in the multithreaded version not running.
            Assert.assertEquals("Just run the test again.", A, instanceA.getNumPhrases());
            Assert.assertEquals("Just run the test again.", A, instanceB.getNumPhrases());

            //Keep string representation of B prior to merging it into A
            String toStringB = instanceB.toString(TestHelpers.FORMAT, true);

            //Run the merge that will hopefully exercise the multithreaded approach.
            instanceA.merge(instanceB);

            //Since they are randomly generated, the only thing I can really
            //  check is that merging each sequentially is equivalent to merging
            //  the two sentences.
            Assert.assertEquals(instanceC, instanceA);

            //We can also ensure that the phrases in the two sentences are
            //  independent (i.e. they were cloned properly). To do this, we
            //  update each phrase in A and ensure that the phrases in B have
            //  not changed.
            instanceA.appendElemToEachPhrase(cons.getElemFor(0));
            instanceA.appendElemToEachPhrase(cons.getElemFor(63));
            Assert.assertEquals("Phrase in B was modified via merge with A", toStringB, instanceB.toString(TestHelpers.FORMAT, true));
        }
    }

    @Test
    public void test_cross() {
        System.out.println("test_cross");
        Construction<P, E, D> cons = getCons();
        {
            D instance = cons.newFromPhrase(cons.buildPhrase(1, 4, 7));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(3, instance.getNumProps());
            Assert.assertEquals("(1,4,7)", instance.toString(TestHelpers.FORMAT, true));

            //Cross with self (no effect)
            instance.cross(instance);

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(3, instance.getNumProps());
            Assert.assertEquals("(1,4,7)", instance.toString(TestHelpers.FORMAT, true));

            //Cross with identical (but not self) NormalForm (no effect)
            instance.cross(cons.newFromPhrase(cons.buildPhrase(1, 4, 7)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(3, instance.getNumProps());
            Assert.assertEquals("(1,4,7)", instance.toString(TestHelpers.FORMAT, true));

            //Cross with non-overlapping NormalForm
            instance.cross(cons.newFromPhrase(cons.buildPhrase(2, 5, 9)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(6, instance.getNumProps());
            Assert.assertEquals("(1,2,4,5,7,9)", instance.toString(TestHelpers.FORMAT, true));

            //Cross with partially overlapping NormalForm
            instance.cross(cons.newFromPhrase(cons.buildPhrase(1, 4, 3)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(7, instance.getNumProps());
            Assert.assertEquals("(1,2,3,4,5,7,9)", instance.toString(TestHelpers.FORMAT, true));

            //Cross with the single empty phrase NormalForm (no effect)
            instance.cross(cons.newFromPhrase(cons.buildPhrase()));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(7, instance.getNumProps());
            Assert.assertEquals("(1,2,3,4,5,7,9)", instance.toString(TestHelpers.FORMAT, true));

            {
                //Perform the previous case in the reverse order (same result)
                D instance2 = cons.newFromPhrase(cons.buildPhrase());
                instance2.cross(instance);
                Assert.assertFalse(instance.isEmpty());
                Assert.assertEquals(1, instance.getNumPhrases());
                Assert.assertEquals(7, instance.getNumProps());
                Assert.assertEquals("(1,2,3,4,5,7,9)", instance.toString(TestHelpers.FORMAT, true));
            }

            //Cross with the empty NormalForm (no phrases; removes all)
            instance.cross(cons.newFromEmpty());

            Assert.assertTrue(instance.isEmpty());
            Assert.assertEquals(0, instance.getNumPhrases());
            Assert.assertEquals(0, instance.getNumProps());
            Assert.assertEquals("", instance.toString(TestHelpers.FORMAT, true));

            {
                //Perform the previous case in the reverse order (same result)
                D instance2 = cons.newFromEmpty();
                instance2.cross(instance);
                Assert.assertTrue(instance.isEmpty());
                Assert.assertEquals(0, instance.getNumPhrases());
                Assert.assertEquals(0, instance.getNumProps());
                Assert.assertEquals("", instance.toString(TestHelpers.FORMAT, true));
            }

            //Cross with an arbitrary NormalForm, will be absorbed by the empty
            instance.cross(cons.newFromPhrase(cons.buildPhrase(1, 4, 34, 12)));

            Assert.assertTrue(instance.isEmpty());
            Assert.assertEquals(0, instance.getNumPhrases());
            Assert.assertEquals(0, instance.getNumProps());
            Assert.assertEquals("", instance.toString(TestHelpers.FORMAT, true));
        }
        {
            D instance = cons.newFromPhrase(cons.buildPhrase(1, 4, 5, 7));
            instance.merge(cons.newFromPhrase(cons.buildPhrase(3, 4, 7, 9)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(2, instance.getNumPhrases());
            Assert.assertEquals(8, instance.getNumProps());
            Assert.assertEquals("(1,4,5,7)(3,4,7,9)", instance.toString(TestHelpers.FORMAT, true));

            //Cross with a NormalForm that is absorbed
            instance.cross(cons.newFromPhrase(cons.buildPhrase(4)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(2, instance.getNumPhrases());
            Assert.assertEquals(8, instance.getNumProps());
            Assert.assertEquals("(1,4,5,7)(3,4,7,9)", instance.toString(TestHelpers.FORMAT, true));

            //Cross with a NormalForm that is absorbed (more complex)
            instance.cross(cons.newFromPhrase(cons.buildPhrase(4, 7)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(2, instance.getNumPhrases());
            Assert.assertEquals(8, instance.getNumProps());
            Assert.assertEquals("(1,4,5,7)(3,4,7,9)", instance.toString(TestHelpers.FORMAT, true));

            //Cross with a NormalForm that absorbs 1 phrase
            instance.cross(cons.newFromPhrase(cons.buildPhrase(1, 5, 3, 9)));

            Assert.assertFalse(instance.isEmpty());
            Assert.assertEquals(1, instance.getNumPhrases());
            Assert.assertEquals(6, instance.getNumProps());
            Assert.assertEquals("(1,3,4,5,7,9)", instance.toString(TestHelpers.FORMAT, true));
        }
        {
            //Build 2 more complex NormalForms and cross them
            D instanceA = cons.newFromPhrase(cons.buildPhrase(1, 4));
            instanceA.merge(cons.newFromPhrase(cons.buildPhrase(1, 5, 9)));
            instanceA.merge(cons.newFromPhrase(cons.buildPhrase(5, 7, 9)));
            Assert.assertEquals("(1,4)(1,5,9)(5,7,9)", instanceA.toString(TestHelpers.FORMAT, true));
            D instanceB = cons.newFromPhrase(cons.buildPhrase(1, 4, 7));
            instanceB.merge(cons.newFromPhrase(cons.buildPhrase(1, 9)));
            instanceB.merge(cons.newFromPhrase(cons.buildPhrase(4, 9)));
            Assert.assertEquals("(1,9)(4,9)(1,4,7)", instanceB.toString(TestHelpers.FORMAT, true));

            instanceA.cross(instanceB);

            Assert.assertFalse(instanceA.isEmpty());
            Assert.assertEquals(4, instanceA.getNumPhrases());
            Assert.assertEquals(13, instanceA.getNumProps());
            Assert.assertEquals("(1,4,7)(1,4,9)(1,5,9)(4,5,7,9)", instanceA.toString(TestHelpers.FORMAT, true));
        }
    }

    /**
     *
     */
    @Test
    public void testEquals() {
        System.out.println("test_equals");
        Construction<P, E, D> cons = getCons();

        D instance1 = cons.newFromElem(cons.getElemFor(6));
        instance1.and(cons.getElemFor(7));
        instance1.or(cons.getElemFor(8));
        instance1.or(cons.getElemFor(9));
        instance1.and(cons.getElemFor(10));

        D instance2 = cons.newFromElem(cons.getElemFor(6));
        instance2.and(cons.getElemFor(7));
        instance2.or(cons.getElemFor(8));
        instance2.or(cons.getElemFor(9));
        instance2.and(cons.getElemFor(10));

        Assert.assertTrue(instance1.equals(instance2));
        Assert.assertTrue(instance2.equals(instance1));
    }

    private D resolved(D orig, int var, D rep) {
        D retVal = orig.clone(false);
        retVal.resolve(getCons().getElemFor(var), rep);
        return retVal;
    }

    @Test
    public void test_resolve_TF() {
        System.out.println("test_resolve_TF");
        Construction<P, E, D> cons = getCons();

        D TRUE = cons.staticGetTrue();
        Assert.assertFalse(TRUE.isFalse());
        Assert.assertTrue(TRUE.isTrue());
        Assert.assertEquals(!cons.isDisjunctive(), TRUE.isEmpty());
        Assert.assertEquals(cons.isDisjunctive() ? 1 : 0, TRUE.getNumPhrases());
        Assert.assertEquals(0, TRUE.getNumProps());
        Assert.assertEquals(cons.isDisjunctive() ? "()" : "", TRUE.toString(TestHelpers.FORMAT, true));
        D FALSE = cons.staticGetFalse();
        Assert.assertTrue(FALSE.isFalse());
        Assert.assertFalse(FALSE.isTrue());
        Assert.assertEquals(cons.isDisjunctive(), FALSE.isEmpty());
        Assert.assertEquals(cons.isDisjunctive() ? 0 : 1, FALSE.getNumPhrases());
        Assert.assertEquals(0, FALSE.getNumProps());
        Assert.assertEquals(cons.isDisjunctive() ? "" : "()", FALSE.toString(TestHelpers.FORMAT, true));

        D a = cons.buildSentence(Arrays.asList(cons.buildPhrase(1, 2, 3), cons.buildPhrase(1, 2, 4)));
        Assert.assertEquals("(1,2,3)(1,2,4)", a.toString(TestHelpers.FORMAT, true));

        D a_1_TRUE = resolved(a, 1, TRUE);
        Assert.assertEquals(cons.isDisjunctive() ? "(2,3)(2,4)" : "", a_1_TRUE.toString(TestHelpers.FORMAT, true));
        if (cons.isDisjunctive()) {
            Assert.assertFalse(a_1_TRUE.isFalse());
            Assert.assertFalse(a_1_TRUE.isTrue());
        } else {
            Assert.assertFalse(a_1_TRUE.isFalse());
            Assert.assertTrue(a_1_TRUE.isTrue());
        }

        D a_1_FALSE = resolved(a, 1, FALSE);
        Assert.assertEquals(cons.isDisjunctive() ? "" : "(2,3)(2,4)", a_1_FALSE.toString(TestHelpers.FORMAT, true));
        if (cons.isDisjunctive()) {
            Assert.assertTrue(a_1_FALSE.isFalse());
            Assert.assertFalse(a_1_FALSE.isTrue());
        } else {
            Assert.assertFalse(a_1_FALSE.isFalse());
            Assert.assertFalse(a_1_FALSE.isTrue());
        }

        D a_3_TRUE = resolved(a, 3, TRUE);
        Assert.assertEquals(cons.isDisjunctive() ? "(1,2)" : "(1,2,4)", a_3_TRUE.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(a_3_TRUE.isFalse());
        Assert.assertFalse(a_3_TRUE.isTrue());

        D a_3_FALSE = resolved(a, 3, FALSE);
        Assert.assertEquals(cons.isDisjunctive() ? "(1,2,4)" : "(1,2)", a_3_FALSE.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(a_3_FALSE.isFalse());
        Assert.assertFalse(a_3_FALSE.isTrue());

        D a_0_TRUE = resolved(a, 0, TRUE);
        Assert.assertEquals("(1,2,3)(1,2,4)", a_0_TRUE.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(a_0_TRUE.isFalse());
        Assert.assertFalse(a_0_TRUE.isTrue());

        D a_0_FALSE = resolved(a, 0, FALSE);
        Assert.assertEquals("(1,2,3)(1,2,4)", a_0_FALSE.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(a_0_FALSE.isFalse());
        Assert.assertFalse(a_0_FALSE.isTrue());

        D b = cons.buildSentence(Arrays.asList(cons.buildPhrase(0), cons.buildPhrase(1, 2, 3), cons.buildPhrase(1, 2, 4)));
        Assert.assertEquals("(0)(1,2,3)(1,2,4)", b.toString(TestHelpers.FORMAT, true));

        D b_1_TRUE = resolved(b, 1, TRUE);
        Assert.assertEquals(cons.isDisjunctive() ? "(0)(2,3)(2,4)" : "(0)", b_1_TRUE.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(b_1_TRUE.isFalse());
        Assert.assertFalse(b_1_TRUE.isTrue());

        D b_1_FALSE = resolved(b, 1, FALSE);
        Assert.assertEquals(cons.isDisjunctive() ? "(0)" : "(0)(2,3)(2,4)", b_1_FALSE.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(b_1_FALSE.isFalse());
        Assert.assertFalse(b_1_FALSE.isTrue());

        D b_3_TRUE = resolved(b, 3, TRUE);
        Assert.assertEquals(cons.isDisjunctive() ? "(0)(1,2)" : "(0)(1,2,4)", b_3_TRUE.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(b_3_TRUE.isFalse());
        Assert.assertFalse(b_3_TRUE.isTrue());

        D b_3_FALSE = resolved(b, 3, FALSE);
        Assert.assertEquals(cons.isDisjunctive() ? "(0)(1,2,4)" : "(0)(1,2)", b_3_FALSE.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(b_3_FALSE.isFalse());
        Assert.assertFalse(b_3_FALSE.isTrue());

        D b_0_TRUE = resolved(b, 0, TRUE);
        Assert.assertEquals(cons.isDisjunctive() ? "()" : "(1,2,3)(1,2,4)", b_0_TRUE.toString(TestHelpers.FORMAT, true));
        if (cons.isDisjunctive()) {
            Assert.assertFalse(b_0_TRUE.isFalse());
            Assert.assertTrue(b_0_TRUE.isTrue());
        } else {
            Assert.assertFalse(b_0_TRUE.isFalse());
            Assert.assertFalse(b_0_TRUE.isTrue());
        }

        D b_0_FALSE = resolved(b, 0, FALSE);
        Assert.assertEquals(cons.isDisjunctive() ? "(1,2,3)(1,2,4)" : "()", b_0_FALSE.toString(TestHelpers.FORMAT, true));
        if (cons.isDisjunctive()) {
            Assert.assertFalse(b_0_FALSE.isFalse());
            Assert.assertFalse(b_0_FALSE.isTrue());
        } else {
            Assert.assertTrue(b_0_FALSE.isFalse());
            Assert.assertFalse(b_0_FALSE.isTrue());
        }
    }

    @Test
    public void test_resolve() {
        System.out.println("test_resolve");
        Construction<P, E, D> cons = getCons();

        D a = cons.buildSentence(Arrays.asList(cons.buildPhrase(1, 2, 3), cons.buildPhrase(1, 2, 4)));
        Assert.assertEquals("(1,2,3)(1,2,4)", a.toString(TestHelpers.FORMAT, true));

        D b = cons.buildSentence(Arrays.asList(cons.buildPhrase(0), cons.buildPhrase(1, 2, 3), cons.buildPhrase(1, 2, 4)));
        Assert.assertEquals("(0)(1,2,3)(1,2,4)", b.toString(TestHelpers.FORMAT, true));

        //Replace var w/ self
        D b_0_0 = resolved(b, 0, cons.newFromElem(cons.getElemFor(0)));
        Assert.assertEquals("(0)(1,2,3)(1,2,4)", b_0_0.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(b_0_0.isFalse());
        Assert.assertFalse(b_0_0.isTrue());
        Assert.assertEquals(b, b_0_0);

        //Replace single var phrase w/ phrases that already exist
        D b_0_a = resolved(b, 0, a);
        Assert.assertEquals("(1,2,3)(1,2,4)", b_0_a.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(b_0_a.isFalse());
        Assert.assertFalse(b_0_a.isTrue());
        Assert.assertEquals(a, b_0_a);

        //Replace single var phrase w/ phrases that are absorbed
        D b_0_1240 = resolved(b, 0, cons.newFromPhrase(cons.buildPhrase(1, 2, 4, 0)));
        Assert.assertEquals("(1,2,3)(1,2,4)", b_0_1240.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(b_0_1240.isFalse());
        Assert.assertFalse(b_0_1240.isTrue());
        Assert.assertEquals(a, b_0_1240);

        //Replace single var phrase w/ phrases that absorb existing
        D b_0_12 = resolved(b, 0, cons.newFromPhrase(cons.buildPhrase(1, 2)));
        Assert.assertEquals("(1,2)", b_0_12.toString(TestHelpers.FORMAT, true));
        Assert.assertFalse(b_0_12.isFalse());
        Assert.assertFalse(b_0_12.isTrue());

        //Replacing each part, abosorption takes over
        D b_1_a = resolved(b, 1, a);
        Assert.assertEquals(b, b_1_a);
        D b_3_a = resolved(b, 3, a);
        Assert.assertEquals(b, b_3_a);
        D b_4_a = resolved(b, 4, a);
        Assert.assertEquals(b, b_4_a);

        //Replace var w/in complex phrase w/ another complex DNF
        D b_4_67o89 = resolved(b, 4, cons.buildSentence(Arrays.asList(cons.buildPhrase(8, 9), cons.buildPhrase(6, 7))));
        Assert.assertEquals("(0)(1,2,3)(1,2,6,7)(1,2,8,9)", b_4_67o89.toString(TestHelpers.FORMAT, true));

        //Replace var w/in multiple complex phrases w/ another complex DNF
        D b_2_67o89 = resolved(b, 2, cons.buildSentence(Arrays.asList(cons.buildPhrase(8, 9), cons.buildPhrase(6, 7))));
        Assert.assertEquals("(0)(1,3,6,7)(1,3,8,9)(1,4,6,7)(1,4,8,9)", b_2_67o89.toString(TestHelpers.FORMAT, true));
        D b_2_67o89o0 = resolved(b, 2, cons.buildSentence(Arrays.asList(cons.buildPhrase(0), cons.buildPhrase(8, 9), cons.buildPhrase(6, 7))));
        Assert.assertEquals("(0)(1,3,6,7)(1,3,8,9)(1,4,6,7)(1,4,8,9)", b_2_67o89o0.toString(TestHelpers.FORMAT, true));
    }

    private D resolved(D orig, Map<E, D> rep) {
        D retVal = orig.clone(false);
        retVal.resolveAll(rep);
        return retVal;
    }

    @Test
    public void test_resolveAll() {
        System.out.println("test_resolveAll");
        Construction<P, E, D> cons = getCons();

        final int VAR_LIMIT = 5;//all variables used should be less than this number

        D a = cons.buildSentence(Arrays.asList(cons.buildPhrase(1, 2, 3), cons.buildPhrase(1, 2, 4)));
        Assert.assertEquals("(1,2,3)(1,2,4)", a.toString(TestHelpers.FORMAT, true));

        D b = cons.buildSentence(Arrays.asList(cons.buildPhrase(0), cons.buildPhrase(1, 2, 3), cons.buildPhrase(1, 2, 4)));
        Assert.assertEquals("(0)(1,2,3)(1,2,4)", b.toString(TestHelpers.FORMAT, true));

        {//empty replacement map
            LinkedHashMap<E, D> replacements = new LinkedHashMap<>();

            D a_0 = resolved(a, replacements);
            Assert.assertEquals(a, a_0);
            D b_0 = resolved(b, replacements);
            Assert.assertEquals(b, b_0);
        }

        {//useless replacement map (i.e. keys not appearing)
            LinkedHashMap<E, D> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(23), cons.buildSentence(Arrays.asList(cons.buildPhrase(63))));
            replacements.put(cons.getElemFor(11), cons.buildSentence(Arrays.asList(cons.buildPhrase(63))));
            replacements.put(cons.getElemFor(52), cons.buildSentence(Arrays.asList(cons.buildPhrase(63))));
            replacements.put(cons.getElemFor(45), cons.buildSentence(Arrays.asList(cons.buildPhrase(63))));

            D a_0 = resolved(a, replacements);
            Assert.assertEquals(a, a_0);
            D b_0 = resolved(b, replacements);
            Assert.assertEquals(b, b_0);
        }

        {//replace with self
            LinkedHashMap<E, D> replacements = new LinkedHashMap<>();
            for (int i = 0; i < VAR_LIMIT; i++) {
                replacements.put(cons.getElemFor(i), cons.buildSentence(Arrays.asList(cons.buildPhrase(i))));
            }

            D a_0 = resolved(a, replacements);
            Assert.assertEquals(a, a_0);
            D b_0 = resolved(b, replacements);
            Assert.assertEquals(b, b_0);
        }

        {//replace with empty phrase (i.e. TRUE in DNF, FALSE in CNF)
            P phrase = cons.buildPhrase();
            LinkedHashMap<E, D> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(0), cons.buildSentence(Arrays.asList(phrase)));
            replacements.put(cons.getElemFor(4), cons.buildSentence(Arrays.asList(phrase)));

            D a_0 = resolved(a, replacements);
            Assert.assertEquals("(1,2)", a_0.toString(TestHelpers.FORMAT, true));
            D b_0 = resolved(b, replacements);
            Assert.assertEquals("()", b_0.toString(TestHelpers.FORMAT, true));
        }

        try {//replace with 'null' phrase (i.e. FALSE in DNF, TRUE in CNF)
            P phrase = null;
            LinkedHashMap<E, D> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(0), cons.buildSentence(Arrays.asList(phrase)));
            replacements.put(cons.getElemFor(4), cons.buildSentence(Arrays.asList(phrase)));

            D a_0 = resolved(a, replacements);
            Assert.assertEquals("(1,2,3)", a_0.toString(TestHelpers.FORMAT, true));
            D b_0 = resolved(b, replacements);
            Assert.assertEquals("(1,2,3)", b_0.toString(TestHelpers.FORMAT, true));
        } catch (NullPointerException ex) {
            //<init>(FormRules,PhraseType) is allowed to throw
            //  NullPointerException if the proposition is null
            System.out.println("single-phrase constructor threw NPE");
        }

        {//replace w/ values not appearing previously
            /// simple values
            LinkedHashMap<E, D> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(0), cons.buildSentence(Arrays.asList(cons.buildPhrase(10))));
            replacements.put(cons.getElemFor(1), cons.buildSentence(Arrays.asList(cons.buildPhrase(11))));
            replacements.put(cons.getElemFor(3), cons.buildSentence(Arrays.asList(cons.buildPhrase(13))));

            D a_0 = resolved(a, replacements);
            Assert.assertEquals("(2,4,11)(2,11,13)", a_0.toString(TestHelpers.FORMAT, true));

            D b_0 = resolved(b, replacements);
            Assert.assertEquals("(10)(2,4,11)(2,11,13)", b_0.toString(TestHelpers.FORMAT, true));

            /// complex values
            replacements.clear();
            replacements.put(cons.getElemFor(0), cons.buildSentence(Arrays.asList(cons.buildPhrase(10, 11, 12), cons.buildPhrase(20, 21))));
            replacements.put(cons.getElemFor(1), cons.buildSentence(Arrays.asList(cons.buildPhrase(30))));
            replacements.put(cons.getElemFor(3), cons.buildSentence(Arrays.asList(cons.buildPhrase(40, 41, 44), cons.buildPhrase(52, 55))));

            D a_1 = resolved(a, replacements);
            Assert.assertEquals("(2,4,30)(2,30,52,55)(2,30,40,41,44)", a_1.toString(TestHelpers.FORMAT, true));

            D b_1 = resolved(b, replacements);
            Assert.assertEquals("(20,21)(2,4,30)(10,11,12)(2,30,52,55)(2,30,40,41,44)", b_1.toString(TestHelpers.FORMAT, true));
        }

        {//replace w/ values that do appear previously but does not cause absorption
            /// simple values
            LinkedHashMap<E, D> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(1), cons.buildSentence(Arrays.asList(cons.buildPhrase(2))));

            D a_0 = resolved(a, replacements);
            Assert.assertEquals("(2,3)(2,4)", a_0.toString(TestHelpers.FORMAT, true));

            D b_0 = resolved(b, replacements);
            Assert.assertEquals("(0)(2,3)(2,4)", b_0.toString(TestHelpers.FORMAT, true));

            /// complex values (1)
            replacements.clear();
            replacements.put(cons.getElemFor(1), cons.buildSentence(Arrays.asList(cons.buildPhrase(1, 2))));

            D a_1 = resolved(a, replacements);
            Assert.assertEquals("(1,2,3)(1,2,4)", a_1.toString(TestHelpers.FORMAT, true));

            D b_1 = resolved(b, replacements);
            Assert.assertEquals("(0)(1,2,3)(1,2,4)", b_1.toString(TestHelpers.FORMAT, true));

            /// complex values (2)
            replacements.clear();
            replacements.put(cons.getElemFor(1), cons.buildSentence(Arrays.asList(cons.buildPhrase(1, 2, 3), cons.buildPhrase(1, 2, 4))));

            D a_2 = resolved(a, replacements);
            Assert.assertEquals("(1,2,3)(1,2,4)", a_2.toString(TestHelpers.FORMAT, true));

            D b_2 = resolved(b, replacements);
            Assert.assertEquals("(0)(1,2,3)(1,2,4)", b_2.toString(TestHelpers.FORMAT, true));

            /// complex values (3)
            replacements.clear();
            replacements.put(cons.getElemFor(1), cons.buildSentence(Arrays.asList(cons.buildPhrase(3), cons.buildPhrase(4))));

            D a_3 = resolved(a, replacements);
            Assert.assertEquals("(2,3)(2,4)", a_3.toString(TestHelpers.FORMAT, true));

            D b_3 = resolved(b, replacements);
            Assert.assertEquals("(0)(2,3)(2,4)", b_3.toString(TestHelpers.FORMAT, true));

            /// complex values (4)
            replacements.clear();
            replacements.put(cons.getElemFor(4), cons.buildSentence(Arrays.asList(cons.buildPhrase(2, 4), cons.buildPhrase(1, 4))));

            D a_4 = resolved(a, replacements);
            Assert.assertEquals("(1,2,3)(1,2,4)", a_4.toString(TestHelpers.FORMAT, true));

            D b_4 = resolved(b, replacements);
            Assert.assertEquals("(0)(1,2,3)(1,2,4)", b_4.toString(TestHelpers.FORMAT, true));
        }

        {//replace w/ values that do appear previously and cause absorption
            LinkedHashMap<E, D> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(0), cons.buildSentence(Arrays.asList(cons.buildPhrase(1))));

            D b_0 = resolved(b, replacements);
            Assert.assertEquals("(1)", b_0.toString(TestHelpers.FORMAT, true));
        }

        {//replace w/ values that do appear previously and cause absorption
            /// simple values
            LinkedHashMap<E, D> replacements1 = new LinkedHashMap<>();
            replacements1.put(cons.getElemFor(3), cons.buildSentence(Arrays.asList(cons.buildPhrase(4))));

            D a_0 = resolved(a, replacements1);
            Assert.assertEquals("(1,2,4)", a_0.toString(TestHelpers.FORMAT, true));

            D b_0 = resolved(b, replacements1);
            Assert.assertEquals("(0)(1,2,4)", b_0.toString(TestHelpers.FORMAT, true));

            /// simple values
            LinkedHashMap<E, D> replacements2 = new LinkedHashMap<>();
            replacements2.put(cons.getElemFor(3), cons.buildSentence(Arrays.asList(cons.buildPhrase(2))));

            D a_1 = resolved(a, replacements2);
            Assert.assertEquals("(1,2)", a_1.toString(TestHelpers.FORMAT, true));

            D b_1 = resolved(b, replacements2);
            Assert.assertEquals("(0)(1,2)", b_1.toString(TestHelpers.FORMAT, true));

            /// complex values (1)
            LinkedHashMap<E, D> replacements3 = new LinkedHashMap<>();
            replacements3.put(cons.getElemFor(3), cons.buildSentence(Arrays.asList(cons.buildPhrase(2, 3), cons.buildPhrase(1, 2))));

            D a_2 = resolved(a, replacements3);
            Assert.assertEquals("(1,2)", a_2.toString(TestHelpers.FORMAT, true));

            D b_2 = resolved(b, replacements3);
            Assert.assertEquals("(0)(1,2)", b_2.toString(TestHelpers.FORMAT, true));

            /// complex values (2)
            LinkedHashMap<E, D> replacements4 = new LinkedHashMap<>();
            replacements4.put(cons.getElemFor(0), cons.buildSentence(Arrays.asList(cons.buildPhrase(2, 4), cons.buildPhrase(2, 3))));

            D a_4 = resolved(a, replacements4);
            Assert.assertEquals("(1,2,3)(1,2,4)", a_4.toString(TestHelpers.FORMAT, true));

            D b_4 = resolved(b, replacements4);
            Assert.assertEquals("(2,3)(2,4)", b_4.toString(TestHelpers.FORMAT, true));
        }

        {//replace w/ values that do appear previously and would cause replacement
            //  of an already replaced value if implementation is incorrect

            /// simple values
            LinkedHashMap<E, D> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(1), cons.buildSentence(Arrays.asList(cons.buildPhrase(11))));
            replacements.put(cons.getElemFor(11), cons.buildSentence(Arrays.asList(cons.buildPhrase(15))));
            replacements.put(cons.getElemFor(0), cons.buildSentence(Arrays.asList(cons.buildPhrase(21))));
            replacements.put(cons.getElemFor(21), cons.buildSentence(Arrays.asList(cons.buildPhrase(1))));

            D a_0 = resolved(a, replacements);
            Assert.assertEquals("(2,3,11)(2,4,11)", a_0.toString(TestHelpers.FORMAT, true));

            D b_0 = resolved(b, replacements);
            Assert.assertEquals("(21)(2,3,11)(2,4,11)", b_0.toString(TestHelpers.FORMAT, true));

            /// complex values
            replacements.clear();
            replacements.put(cons.getElemFor(1), cons.buildSentence(Arrays.asList(cons.buildPhrase(11, 12))));
            replacements.put(cons.getElemFor(11), cons.buildSentence(Arrays.asList(cons.buildPhrase(1, 2))));
            replacements.put(cons.getElemFor(0), cons.buildSentence(Arrays.asList(cons.buildPhrase(2, 3), cons.buildPhrase(2, 4))));
            replacements.put(cons.getElemFor(4), cons.buildSentence(Arrays.asList(cons.buildPhrase(5))));

            D a_1 = resolved(a, replacements);
            Assert.assertEquals("(2,3,11,12)(2,5,11,12)", a_1.toString(TestHelpers.FORMAT, true));

            D b_1 = resolved(b, replacements);
            Assert.assertEquals("(2,3)(2,4)(2,5,11,12)", b_1.toString(TestHelpers.FORMAT, true));
        }
    }

    private D replaced(D orig, Map<E, E> rep) {
        D retVal = orig.clone(false);
        retVal.replaceAll(rep);
        return retVal;
    }

    @Test
    public void test_replaceAll() {
        System.out.println("test_replaceAll");
        Construction<P, E, D> cons = getCons();

        final int VAR_LIMIT = 5;//all variables used should be less than this number

        D a = cons.buildSentence(Arrays.asList(cons.buildPhrase(1, 2, 3), cons.buildPhrase(1, 2, 4)));
        Assert.assertEquals("(1,2,3)(1,2,4)", a.toString(TestHelpers.FORMAT, true));

        D b = cons.buildSentence(Arrays.asList(cons.buildPhrase(0), cons.buildPhrase(1, 2, 3), cons.buildPhrase(1, 2, 4)));
        Assert.assertEquals("(0)(1,2,3)(1,2,4)", b.toString(TestHelpers.FORMAT, true));

        {//empty replacement map
            LinkedHashMap<E, E> replacements = new LinkedHashMap<>();

            D a_0 = replaced(a, replacements);
            Assert.assertEquals(a, a_0);
            D b_0 = replaced(b, replacements);
            Assert.assertEquals(b, b_0);
        }

        {//useless replacement map (i.e. keys not appearing)
            LinkedHashMap<E, E> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(23), cons.getElemFor(63));
            replacements.put(cons.getElemFor(11), cons.getElemFor(63));
            replacements.put(cons.getElemFor(52), cons.getElemFor(63));
            replacements.put(cons.getElemFor(45), cons.getElemFor(63));

            D a_0 = replaced(a, replacements);
            Assert.assertEquals(a, a_0);
            D b_0 = replaced(b, replacements);
            Assert.assertEquals(b, b_0);
        }

        {//replace with self
            LinkedHashMap<E, E> replacements = new LinkedHashMap<>();
            for (int i = 0; i < VAR_LIMIT; i++) {
                replacements.put(cons.getElemFor(i), cons.getElemFor(i));
            }

            D a_0 = replaced(a, replacements);
            Assert.assertEquals(a, a_0);
            D b_0 = replaced(b, replacements);
            Assert.assertEquals(b, b_0);
        }

        {//replace w/ values not appearing previously
            LinkedHashMap<E, E> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(0), cons.getElemFor(10));
            replacements.put(cons.getElemFor(1), cons.getElemFor(11));
            replacements.put(cons.getElemFor(3), cons.getElemFor(13));

            D a_0 = replaced(a, replacements);
            Assert.assertEquals("(2,4,11)(2,11,13)", a_0.toString(TestHelpers.FORMAT, true));

            D b_0 = replaced(b, replacements);
            Assert.assertEquals("(10)(2,4,11)(2,11,13)", b_0.toString(TestHelpers.FORMAT, true));
        }

        {//replace w/ values that do appear previously but does not cause absorption
            LinkedHashMap<E, E> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(1), cons.getElemFor(2));

            D a_0 = replaced(a, replacements);
            Assert.assertEquals("(2,3)(2,4)", a_0.toString(TestHelpers.FORMAT, true));

            D b_0 = replaced(b, replacements);
            Assert.assertEquals("(0)(2,3)(2,4)", b_0.toString(TestHelpers.FORMAT, true));
        }

        {//replace w/ values that do appear previously and cause absorption
            LinkedHashMap<E, E> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(0), cons.getElemFor(1));

            D b_0 = replaced(b, replacements);
            Assert.assertEquals("(1)", b_0.toString(TestHelpers.FORMAT, true));
        }

        {//replace w/ values that do appear previously and cause absorption
            /// simple values
            LinkedHashMap<E, E> replacements1 = new LinkedHashMap<>();
            replacements1.put(cons.getElemFor(3), cons.getElemFor(4));

            D a_0 = replaced(a, replacements1);
            Assert.assertEquals("(1,2,4)", a_0.toString(TestHelpers.FORMAT, true));

            D b_0 = replaced(b, replacements1);
            Assert.assertEquals("(0)(1,2,4)", b_0.toString(TestHelpers.FORMAT, true));

            /// simple values
            LinkedHashMap<E, E> replacements2 = new LinkedHashMap<>();
            replacements2.put(cons.getElemFor(3), cons.getElemFor(2));

            D a_1 = replaced(a, replacements2);
            Assert.assertEquals("(1,2)", a_1.toString(TestHelpers.FORMAT, true));

            D b_2 = replaced(b, replacements2);
            Assert.assertEquals("(0)(1,2)", b_2.toString(TestHelpers.FORMAT, true));
        }

        {//replace w/ values that do appear previously and would cause replacement
            //  of an already replaced value if implementation is incorrect 
            LinkedHashMap<E, E> replacements = new LinkedHashMap<>();
            replacements.put(cons.getElemFor(1), cons.getElemFor(11));
            replacements.put(cons.getElemFor(11), cons.getElemFor(15));
            replacements.put(cons.getElemFor(0), cons.getElemFor(21));
            replacements.put(cons.getElemFor(21), cons.getElemFor(1));

            D a_0 = replaced(a, replacements);
            Assert.assertEquals("(2,3,11)(2,4,11)", a_0.toString(TestHelpers.FORMAT, true));

            D b_0 = replaced(b, replacements);
            Assert.assertEquals("(21)(2,3,11)(2,4,11)", b_0.toString(TestHelpers.FORMAT, true));
        }
    }

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testUnmodifiable() {
        System.out.println("test_Unmodifiable");
        Construction<P, E, D> cons = getCons();

        D instance = cons.newFromElem(cons.getElemFor(5));
        D umodInstance = instance.asUnmodifiable();

        Assert.assertNotSame(instance, umodInstance);
        Assert.assertTrue(instance.equals(umodInstance));

        instance.and(cons.getElemFor(7));

        Assert.assertFalse(instance.equals(umodInstance));

        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("Modification is not allowed");
        umodInstance.and(cons.getElemFor(7));
    }

    /**
     *
     */
    @Test
    public void testAndProp() {
        System.out.println("test_and(Prop)");
        Construction<P, E, D> cons = getCons();

        D instance = cons.newFromElem(cons.getElemFor(3));
        Assert.assertEquals("(3)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(1));
        Assert.assertEquals(cons.isDisjunctive() ? "(1,3)" : "(1)(3)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(3));
        Assert.assertEquals(cons.isDisjunctive() ? "(1,3)" : "(1)(3)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(4));
        Assert.assertEquals(cons.isDisjunctive() ? "(1,3,4)" : "(1)(3)(4)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(2));
        Assert.assertEquals(cons.isDisjunctive() ? "(1,2,3,4)" : "(1)(2)(3)(4)", instance.toString(TestHelpers.FORMAT, true));
    }

    @Test
    public void testAndFirst() {
        System.out.println("test_AndFirst");
        Construction<P, E, D> cons = getCons();

        D instance = cons.newFromElem(cons.getElemFor(7));
        Assert.assertEquals("(7)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(5));
        Assert.assertEquals(cons.isDisjunctive() ? "(5,7)" : "(5)(7)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(63));
        Assert.assertEquals(cons.isDisjunctive() ? "(63)(5,7)" : "(5,63)(7,63)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(43));
        Assert.assertEquals(cons.isDisjunctive() ? "(43,63)(5,7,43)" : "(43)(5,63)(7,63)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(6));
        Assert.assertEquals(cons.isDisjunctive() ? "(6,43,63)(5,6,7,43)" : "(6)(43)(5,63)(7,63)", instance.toString(TestHelpers.FORMAT, true));
    }

    @Test
    public void testAndFirstFromTrue() {
        System.out.println("test_AndFirst(from TRUE)");
        Construction<P, E, D> cons = getCons();

        D instance = cons.staticGetTrue();
        Assert.assertEquals(cons.isDisjunctive() ? "()" : "", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(7));
        Assert.assertEquals("(7)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(5));
        Assert.assertEquals(cons.isDisjunctive() ? "(5,7)" : "(5)(7)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(63));
        Assert.assertEquals(cons.isDisjunctive() ? "(63)(5,7)" : "(5,63)(7,63)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(43));
        Assert.assertEquals(cons.isDisjunctive() ? "(43,63)(5,7,43)" : "(43)(5,63)(7,63)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(6));
        Assert.assertEquals(cons.isDisjunctive() ? "(6,43,63)(5,6,7,43)" : "(6)(43)(5,63)(7,63)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(1));
        Assert.assertEquals(cons.isDisjunctive() ? "(1)(6,43,63)(5,6,7,43)" : "(1,6)(1,43)(1,5,63)(1,7,63)", instance.toString(TestHelpers.FORMAT, true));
    }

    @Test
    public void testAndFirstFromFalse() {
        System.out.println("test_AndFirst(from FALSE)");
        Construction<P, E, D> cons = getCons();

        D instance = cons.staticGetFalse();
        Assert.assertEquals(cons.isDisjunctive() ? "" : "()", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(7));
        Assert.assertEquals(cons.isDisjunctive() ? "" : "()", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(5));
        Assert.assertEquals(cons.isDisjunctive() ? "" : "()", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(63));
        Assert.assertEquals("(63)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(43));
        Assert.assertEquals(cons.isDisjunctive() ? "(43,63)" : "(43)(63)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(6));
        Assert.assertEquals(cons.isDisjunctive() ? "(6,43,63)" : "(6)(43)(63)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(1));
        Assert.assertEquals(cons.isDisjunctive() ? "(1)(6,43,63)" : "(1,6)(1,43)(1,63)", instance.toString(TestHelpers.FORMAT, true));
    }

    /**
     *
     */
    @Test
    public void testAndNF() {
        System.out.println("test_and(NF)");
        Construction<P, E, D> cons = getCons();

        D test = cons.newFromElem(cons.getElemFor(1));
        {
            test.and(cons.getElemFor(2));
            test.or(cons.getElemFor(3));
            test.or(cons.getElemFor(4));
            test.and(cons.getElemFor(5));

            D instance2 = cons.newFromElem(cons.getElemFor(6));
            instance2.and(cons.getElemFor(7));
            instance2.or(cons.getElemFor(8));
            instance2.or(cons.getElemFor(9));
            instance2.and(cons.getElemFor(10));

            test.and(instance2);
        }

        D expectation = cons.newFromElem(cons.getElemFor(1));
        {
            expectation.and(cons.getElemFor(2));
            expectation.and(cons.getElemFor(5));
            expectation.and(cons.getElemFor(6));
            expectation.and(cons.getElemFor(7));
            expectation.and(cons.getElemFor(10));

            {
                D temp = cons.newFromElem(cons.getElemFor(1));
                temp.and(cons.getElemFor(2));
                temp.and(cons.getElemFor(5));
                temp.and(cons.getElemFor(8));
                temp.and(cons.getElemFor(10));
                expectation.or(temp);
            }
            {
                D temp = cons.newFromElem(cons.getElemFor(1));
                temp.and(cons.getElemFor(2));
                temp.and(cons.getElemFor(5));
                temp.and(cons.getElemFor(9));
                temp.and(cons.getElemFor(10));
                expectation.or(temp);
            }
            {
                D temp = cons.newFromElem(cons.getElemFor(3));
                temp.and(cons.getElemFor(5));
                temp.and(cons.getElemFor(6));
                temp.and(cons.getElemFor(7));
                temp.and(cons.getElemFor(10));
                expectation.or(temp);
            }
            {
                D temp = cons.newFromElem(cons.getElemFor(3));
                temp.and(cons.getElemFor(5));
                temp.and(cons.getElemFor(8));
                temp.and(cons.getElemFor(10));
                expectation.or(temp);
            }
            {
                D temp = cons.newFromElem(cons.getElemFor(3));
                temp.and(cons.getElemFor(5));
                temp.and(cons.getElemFor(9));
                temp.and(cons.getElemFor(10));
                expectation.or(temp);
            }
            {
                D temp = cons.newFromElem(cons.getElemFor(4));
                temp.and(cons.getElemFor(5));
                temp.and(cons.getElemFor(6));
                temp.and(cons.getElemFor(7));
                temp.and(cons.getElemFor(10));
                expectation.or(temp);
            }
            {
                D temp = cons.newFromElem(cons.getElemFor(4));
                temp.and(cons.getElemFor(5));
                temp.and(cons.getElemFor(8));
                temp.and(cons.getElemFor(10));
                expectation.or(temp);
            }
            {
                D temp = cons.newFromElem(cons.getElemFor(4));
                temp.and(cons.getElemFor(5));
                temp.and(cons.getElemFor(9));
                temp.and(cons.getElemFor(10));
                expectation.or(temp);
            }
        }
        Assert.assertEquals(expectation, test);
        if (cons.isDisjunctive()) {
            Assert.assertEquals("(3,5,8,10)(3,5,9,10)(4,5,8,10)(4,5,9,10)(1,2,5,8,10)(1,2,5,9,10)(3,5,6,7,10)(4,5,6,7,10)(1,2,5,6,7,10)", test.toString(TestHelpers.FORMAT, true));
        } else {
            Assert.assertEquals("(5)(10)(1,3,4)(2,3,4)(6,8,9)(7,8,9)", test.toString(TestHelpers.FORMAT, true));
        }
    }

    /**
     * Identical formulas to {@link #testAndDNF()} but using the chaining
     * methods.
     */
    @Test
    public void testAndNFChaining() {
        System.out.println("test_and(NF)+chaining");
        Construction<P, E, D> cons = getCons();

        D test = cons.staticAnd(1, 2).or(cons.getElemFor(3)).or(cons.getElemFor(4)).and(cons.getElemFor(5));
        test.and(cons.staticAnd(6, 7).or(cons.getElemFor(8)).or(cons.getElemFor(9)).and(cons.getElemFor(10)));

        D expectation = cons.staticAnd(1, 2).and(cons.getElemFor(5)).and(cons.getElemFor(6)).and(cons.getElemFor(7)).and(cons.getElemFor(10));
        expectation.or(cons.staticAnd(1, 2).and(cons.getElemFor(5)).and(cons.getElemFor(8)).and(cons.getElemFor(10)));
        expectation.or(cons.staticAnd(1, 2).and(cons.getElemFor(5)).and(cons.getElemFor(9)).and(cons.getElemFor(10)));
        expectation.or(cons.staticAnd(3, 5).and(cons.getElemFor(6)).and(cons.getElemFor(7)).and(cons.getElemFor(10)));
        expectation.or(cons.staticAnd(3, 5).and(cons.getElemFor(8)).and(cons.getElemFor(10)));
        expectation.or(cons.staticAnd(3, 5).and(cons.getElemFor(9)).and(cons.getElemFor(10)));
        expectation.or(cons.staticAnd(4, 5).and(cons.getElemFor(6)).and(cons.getElemFor(7)).and(cons.getElemFor(10)));
        expectation.or(cons.staticAnd(4, 5).and(cons.getElemFor(8)).and(cons.getElemFor(10)));
        expectation.or(cons.staticAnd(4, 5).and(cons.getElemFor(9)).and(cons.getElemFor(10)));

//        //TODO: the below is true for disjunctive form
//        D expectation = cons.buildSentence(Arrays.asList(
//                cons.buildPhrase(1, 2, 5, 6, 7, 10),
//                cons.buildPhrase(1, 2, 5, 8, 10),
//                cons.buildPhrase(1, 2, 5, 9, 10),
//                cons.buildPhrase(3, 5, 6, 7, 10),
//                cons.buildPhrase(3, 5, 8, 10),
//                cons.buildPhrase(3, 5, 9, 10),
//                cons.buildPhrase(4, 5, 6, 7, 10),
//                cons.buildPhrase(4, 5, 8, 10),
//                cons.buildPhrase(4, 5, 9, 10)
//        ));
        Assert.assertEquals(expectation, test);
        if (cons.isDisjunctive()) {
            Assert.assertEquals("(3,5,8,10)(3,5,9,10)(4,5,8,10)(4,5,9,10)(1,2,5,8,10)(1,2,5,9,10)(3,5,6,7,10)(4,5,6,7,10)(1,2,5,6,7,10)", test.toString(TestHelpers.FORMAT, true));
        } else {
            Assert.assertEquals("(5)(10)(1,3,4)(2,3,4)(6,8,9)(7,8,9)", test.toString(TestHelpers.FORMAT, true));
        }
    }

    /**
     *
     */
    @Test
    public void testAndNF_thisFalse() {
        System.out.println("test_and(NF)+this is false");
        Construction<P, E, D> cons = getCons();

        D instance_empty = cons.staticGetFalse();
        D instance_nonEmpty = cons.staticOr(5, 7);

        D retVal = instance_empty.and(instance_nonEmpty);

        Assert.assertSame(retVal, instance_empty);
        Assert.assertNotSame(retVal, instance_nonEmpty);

        Assert.assertFalse(retVal.isTrue());
        Assert.assertTrue(retVal.isFalse());
        Assert.assertEquals(cons.isDisjunctive() ? "" : "()", retVal.toString(TestHelpers.FORMAT, true));
    }

    /**
     *
     */
    @Test
    public void testAndNF_thisTrue() {
        System.out.println("test_and(NF)+this is true");
        Construction<P, E, D> cons = getCons();

        D instance_empty = cons.staticGetTrue();
        D instance_nonEmpty = cons.staticOr(5, 7);

        D retVal = instance_empty.and(instance_nonEmpty);

        Assert.assertSame(retVal, instance_empty);
        Assert.assertNotSame(retVal, instance_nonEmpty);

        Assert.assertFalse(retVal.isTrue());
        Assert.assertFalse(retVal.isFalse());
        Assert.assertEquals(cons.isDisjunctive() ? "(5)(7)" : "(5,7)", retVal.toString(TestHelpers.FORMAT, true));
    }

    /**
     *
     */
    @Test
    public void testAndNF_otherFalse() {
        System.out.println("test_and(NF)+other is false");
        Construction<P, E, D> cons = getCons();

        D instance_empty = cons.staticGetFalse();
        D instance_nonEmpty = cons.staticOr(5, 7);

        D retVal = instance_nonEmpty.and(instance_empty);

        Assert.assertSame(retVal, instance_nonEmpty);
        Assert.assertNotSame(retVal, instance_empty);

        Assert.assertFalse(retVal.isTrue());
        Assert.assertTrue(retVal.isFalse());
        Assert.assertEquals(cons.isDisjunctive() ? "" : "()", retVal.toString(TestHelpers.FORMAT, true));
    }

    /**
     *
     */
    @Test
    public void testAndDNF_otherTrue() {
        System.out.println("test_and(NF)+other is true");
        Construction<P, E, D> cons = getCons();

        D instance_empty = cons.staticGetTrue();
        D instance_nonEmpty = cons.staticOr(5, 7);

        D retVal = instance_nonEmpty.and(instance_empty);

        Assert.assertSame(retVal, instance_nonEmpty);
        Assert.assertNotSame(retVal, instance_empty);

        Assert.assertFalse(retVal.isTrue());
        Assert.assertFalse(retVal.isFalse());
        Assert.assertEquals(cons.isDisjunctive() ? "(5)(7)" : "(5,7)", retVal.toString(TestHelpers.FORMAT, true));
    }

    /**
     *
     */
    @Test
    public void testOrProp() {
        System.out.println("test_or(Prop)");
        Construction<P, E, D> cons = getCons();

        D instance = cons.newFromElem(cons.getElemFor(3));
        Assert.assertEquals("(3)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(1));
        Assert.assertEquals(cons.isDisjunctive() ? "(1)(3)" : "(1,3)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(3));
        Assert.assertEquals(cons.isDisjunctive() ? "(1)(3)" : "(1,3)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(4));
        Assert.assertEquals(cons.isDisjunctive() ? "(1)(3)(4)" : "(1,3,4)", instance.toString(TestHelpers.FORMAT, true));
    }

    @Test
    public void testOrFirst() {
        System.out.println("test_OrFirst");
        Construction<P, E, D> cons = getCons();

        D instance = cons.newFromElem(cons.getElemFor(7));
        Assert.assertEquals("(7)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(5));
        Assert.assertEquals(cons.isDisjunctive() ? "(5,7)" : "(5)(7)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(63));
        Assert.assertEquals(cons.isDisjunctive() ? "(63)(5,7)" : "(5,63)(7,63)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(12));
        Assert.assertEquals(cons.isDisjunctive() ? "(12)(63)(5,7)" : "(5,12,63)(7,12,63)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(43));
        Assert.assertEquals(cons.isDisjunctive() ? "(12,43)(43,63)(5,7,43)" : "(43)(5,12,63)(7,12,63)", instance.toString(TestHelpers.FORMAT, true));
    }

    @Test
    public void testOrFirstFromTrue() {
        System.out.println("test_OrFirst(from TRUE)");
        Construction<P, E, D> cons = getCons();

        D instance = cons.staticGetTrue();
        Assert.assertEquals(cons.isDisjunctive() ? "()" : "", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(7));
        Assert.assertEquals(cons.isDisjunctive() ? "()" : "", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(5));
        Assert.assertEquals("(5)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(63));
        Assert.assertEquals(cons.isDisjunctive() ? "(5)(63)" : "(5,63)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(12));
        Assert.assertEquals(cons.isDisjunctive() ? "(5)(12)(63)" : "(5,12,63)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(43));
        Assert.assertEquals(cons.isDisjunctive() ? "(5,43)(12,43)(43,63)" : "(43)(5,12,63)", instance.toString(TestHelpers.FORMAT, true));
    }

    @Test
    public void testOrFirstFromFalse() {
        System.out.println("test_OrFirst(from FALSE)");
        Construction<P, E, D> cons = getCons();

        D instance = cons.staticGetFalse();
        Assert.assertEquals(cons.isDisjunctive() ? "" : "()", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(7));
        Assert.assertEquals("(7)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(5));
        Assert.assertEquals(cons.isDisjunctive() ? "(5,7)" : "(5)(7)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(63));
        Assert.assertEquals(cons.isDisjunctive() ? "(63)(5,7)" : "(5,63)(7,63)", instance.toString(TestHelpers.FORMAT, true));

        instance.or(cons.getElemFor(12));
        Assert.assertEquals(cons.isDisjunctive() ? "(12)(63)(5,7)" : "(5,12,63)(7,12,63)", instance.toString(TestHelpers.FORMAT, true));

        instance.and(cons.getElemFor(43));
        Assert.assertEquals(cons.isDisjunctive() ? "(12,43)(43,63)(5,7,43)" : "(43)(5,12,63)(7,12,63)", instance.toString(TestHelpers.FORMAT, true));
    }

    /**
     *
     */
    @Test
    public void testOrNF() {
        System.out.println("test_or(NF)");
        Construction<P, E, D> cons = getCons();

        D test = cons.newFromElem(cons.getElemFor(1));
        {
            test.and(cons.getElemFor(2));
            test.or(cons.getElemFor(3));
            test.or(cons.getElemFor(4));
            test.and(cons.getElemFor(5));

            D instance2 = cons.newFromElem(cons.getElemFor(6));
            instance2.and(cons.getElemFor(7));
            instance2.or(cons.getElemFor(8));
            instance2.or(cons.getElemFor(9));
            instance2.and(cons.getElemFor(10));

            test.or(instance2);
        }

        D expectation = cons.newFromElem(cons.getElemFor(1));
        {
            expectation.and(cons.getElemFor(2));
            expectation.and(cons.getElemFor(5));

            {
                D temp = cons.newFromElem(cons.getElemFor(3));
                temp.and(cons.getElemFor(5));
                expectation.or(temp);
            }
            {
                D temp = cons.newFromElem(cons.getElemFor(4));
                temp.and(cons.getElemFor(5));
                expectation.or(temp);
            }
            {
                D temp = cons.newFromElem(cons.getElemFor(6));
                temp.and(cons.getElemFor(7));
                temp.and(cons.getElemFor(10));
                expectation.or(temp);
            }
            {
                D temp = cons.newFromElem(cons.getElemFor(8));
                temp.and(cons.getElemFor(10));
                expectation.or(temp);
            }
            {
                D temp = cons.newFromElem(cons.getElemFor(9));
                temp.and(cons.getElemFor(10));
                expectation.or(temp);
            }
        }

        Assert.assertEquals(expectation, test);
        if (cons.isDisjunctive()) {
            Assert.assertEquals("(3,5)(4,5)(8,10)(9,10)(1,2,5)(6,7,10)", test.toString(TestHelpers.FORMAT, true));
        } else {
            Assert.assertEquals("(5,10)(1,3,4,10)(2,3,4,10)(5,6,8,9)(5,7,8,9)(1,3,4,6,8,9)(1,3,4,7,8,9)(2,3,4,6,8,9)(2,3,4,7,8,9)", test.toString(TestHelpers.FORMAT, true));
        }
    }

    /**
     * Identical formulas to {@link #testOrDNF()} but using the chaining
     * methods.
     */
    @Test
    public void testOrNFChaining() {
        System.out.println("test_or(NF)+chaining");
        Construction<P, E, D> cons = getCons();

        D test = cons.staticAnd(1, 2).or(cons.getElemFor(3)).or(cons.getElemFor(4)).and(cons.getElemFor(5));
        test.or(cons.staticAnd(6, 7).or(cons.getElemFor(8)).or(cons.getElemFor(9)).and(cons.getElemFor(10)));

        D expectation = cons.staticAnd(1, 2).and(cons.getElemFor(5));
        expectation.or(cons.staticAnd(3, 5));
        expectation.or(cons.staticAnd(4, 5));
        expectation.or(cons.staticAnd(6, 7).and(cons.getElemFor(10)));
        expectation.or(cons.staticAnd(8, 10));
        expectation.or(cons.staticAnd(9, 10));

        Assert.assertEquals(expectation, test);
        if (cons.isDisjunctive()) {
            Assert.assertEquals("(3,5)(4,5)(8,10)(9,10)(1,2,5)(6,7,10)", test.toString(TestHelpers.FORMAT, true));
        } else {
            Assert.assertEquals("(5,10)(1,3,4,10)(2,3,4,10)(5,6,8,9)(5,7,8,9)(1,3,4,6,8,9)(1,3,4,7,8,9)(2,3,4,6,8,9)(2,3,4,7,8,9)", test.toString(TestHelpers.FORMAT, true));
        }
    }

    /**
     *
     */
    @Test
    public void testOrNF_thisFalse() {
        System.out.println("test_or(NF)+this is false");
        Construction<P, E, D> cons = getCons();

        D instance_empty = cons.staticGetFalse();
        D instance_nonEmpty = cons.staticAnd(5, 7);

        D retVal = instance_empty.or(instance_nonEmpty);

        Assert.assertSame(retVal, instance_empty);
        Assert.assertNotSame(retVal, instance_nonEmpty);

        Assert.assertFalse(retVal.isTrue());
        Assert.assertFalse(retVal.isFalse());
        Assert.assertEquals(cons.isDisjunctive() ? "(5,7)" : "(5)(7)", retVal.toString(TestHelpers.FORMAT, true));
    }

    /**
     *
     */
    @Test
    public void testOrNF_thisTrue() {
        System.out.println("test_or(NF)+this is true");
        Construction<P, E, D> cons = getCons();

        D instance_empty = cons.staticGetTrue();
        D instance_nonEmpty = cons.staticAnd(5, 7);

        D retVal = instance_empty.or(instance_nonEmpty);

        Assert.assertSame(retVal, instance_empty);
        Assert.assertNotSame(retVal, instance_nonEmpty);

        Assert.assertTrue(retVal.isTrue());
        Assert.assertFalse(retVal.isFalse());
        Assert.assertEquals(cons.isDisjunctive() ? "()" : "", retVal.toString(TestHelpers.FORMAT, true));
    }

    /**
     *
     */
    @Test
    public void testOrNF_otherFalse() {
        System.out.println("test_or(NF)+other is false");
        Construction<P, E, D> cons = getCons();

        D instance_empty = cons.staticGetFalse();
        D instance_nonEmpty = cons.staticAnd(5, 7);

        D retVal = instance_nonEmpty.or(instance_empty);

        Assert.assertSame(retVal, instance_nonEmpty);
        Assert.assertNotSame(retVal, instance_empty);

        Assert.assertFalse(retVal.isTrue());
        Assert.assertFalse(retVal.isFalse());
        Assert.assertEquals(cons.isDisjunctive() ? "(5,7)" : "(5)(7)", retVal.toString(TestHelpers.FORMAT, true));
    }

    /**
     *
     */
    @Test
    public void testOrNF_otherTrue() {
        System.out.println("test_or(NF)+other is true");
        Construction<P, E, D> cons = getCons();

        D instance_empty = cons.staticGetTrue();
        D instance_nonEmpty = cons.staticAnd(5, 7);

        D retVal = instance_nonEmpty.or(instance_empty);

        Assert.assertSame(retVal, instance_nonEmpty);
        Assert.assertNotSame(retVal, instance_empty);

        Assert.assertTrue(retVal.isTrue());
        Assert.assertFalse(retVal.isFalse());
        Assert.assertEquals(cons.isDisjunctive() ? "()" : "", retVal.toString(TestHelpers.FORMAT, true));
    }

    /**
     * This test case is intended to explore the performance of a common
     * occurrence of AND with a very large instance and a very small instance.
     */
    @Test
    public void testPerformanceAndLarge() {
        if (RUN_PERFORMANCE_TESTS) {
            System.out.println("testPerformanceAndLarge");
            Construction<P, E, D> cons = getCons();
            //NOTE: the results seem to become pretty consistent after 10 iterations
            final int NUM_ITER = 10;

            D instLarge = TestHelpers.buildLargestInstance(cons, 15);//TODO: at least 15 but 20 is extremely slow to build
            System.out.println("instLarge = " + instLarge.toString(TestHelpers.FORMAT, true));

            //Phrases of different configurations where the elements used are contained in largeInst
            D inst_P1_N1_C = cons.newFromPhrase(cons.buildPhrase(5));
//        D inst_P1_N10_C = cons.newFromPhrase(cons.buildPhrase(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
            D inst_P1_N10_C = cons.newFromPhrase(cons.buildPhrase(14, 13, 12, 11, 10, 9, 8, 7, 6, 5));
            D inst_P2_N2_C = cons.buildSentence(TestHelpers.list(cons.buildPhrase(5), cons.buildPhrase(6)));
            System.out.println("inst_P1_N1_C = " + inst_P1_N1_C.toString(TestHelpers.FORMAT, true));
            System.out.println("inst_P1_N10_C = " + inst_P1_N10_C.toString(TestHelpers.FORMAT, true));
            System.out.println("inst_P2_N2_C = " + inst_P2_N2_C.toString(TestHelpers.FORMAT, true));

            //Phrases of different configurations where the elements used are NOT contained in largeInst
            D inst_P1_N1_NC = cons.newFromPhrase(cons.buildPhrase(25));
            D inst_P1_N10_NC = cons.newFromPhrase(cons.buildPhrase(54, 55, 56, 57, 58, 59, 60, 61, 62, 63));
            D inst_P2_N2_NC = cons.buildSentence(TestHelpers.list(cons.buildPhrase(25), cons.buildPhrase(26)));
            System.out.println("inst_P1_N1_NC = " + inst_P1_N1_NC.toString(TestHelpers.FORMAT, true));
            System.out.println("inst_P1_N10_NC = " + inst_P1_N10_NC.toString(TestHelpers.FORMAT, true));
            System.out.println("inst_P2_N2_NC = " + inst_P2_N2_NC.toString(TestHelpers.FORMAT, true));

            //Phrases where some elements are contained in largeInst and some are not
            D inst_P1_N30_B = cons.newFromPhrase(cons.buildPhrase(TestHelpers.getRandomInRange(10, 0, 15, false)))
                    .and(cons.newFromPhrase(cons.buildPhrase(TestHelpers.getRandomInRange(20, 20, 64, false))));
            System.out.println("inst_P1_N30_B = " + inst_P1_N30_B.toString(TestHelpers.FORMAT, true));

            long totalElap_P1_N1_C = 0, totalElap_P1_N10_C = 0, totalElap_P2_N2_C = 0;
            long totalElap_P1_N1_NC = 0, totalElap_P1_N10_NC = 0, totalElap_P2_N2_NC = 0;
            long totalElap_P1_N30_B = 0;
            for (int i = 0; i < NUM_ITER; i++) {
                System.out.println("Iteration: " + i);
                long start, elapsed;

                start = System.nanoTime();
                D result_P1_N1_C = cons.staticAnd(instLarge, inst_P1_N1_C);
                totalElap_P1_N1_C += elapsed = System.nanoTime() - start;
//            System.out.println("result(P=1,N=1,C)(" + (((double) elapsed) / 1_000_000) + "ms) = " + result_P1_N1_C.toString(TestHelpers.FORMAT, true));

                start = System.nanoTime();
                D result_P1_N1_NC = cons.staticAnd(instLarge, inst_P1_N1_NC);
                totalElap_P1_N1_NC += elapsed = System.nanoTime() - start;
//            System.out.println("result(P=1,N=1,NC)(" + (((double) elapsed) / 1_000_000) + "ms) = " + result_P1_N1_NC.toString(TestHelpers.FORMAT, true));

                start = System.nanoTime();
                D result_P1_N10_C = cons.staticAnd(instLarge, inst_P1_N10_C);
                totalElap_P1_N10_C += elapsed = System.nanoTime() - start;
//            System.out.println("result(P=1,N=10,C)(" + (((double) elapsed) / 1_000_000) + "ms) = " + result_P1_N10_C.toString(TestHelpers.FORMAT, true));

                start = System.nanoTime();
                D result_P1_N10_NC = cons.staticAnd(instLarge, inst_P1_N10_NC);
                totalElap_P1_N10_NC += elapsed = System.nanoTime() - start;
//            System.out.println("result(P=1,N=10,NC)(" + (((double) elapsed) / 1_000_000) + "ms) = " + result_P1_N10_NC.toString(TestHelpers.FORMAT, true));

                start = System.nanoTime();
                D result_P2_N2_C = cons.staticAnd(instLarge, inst_P2_N2_C);
                totalElap_P2_N2_C += elapsed = System.nanoTime() - start;
//            System.out.println("result(P=2,N=2,C)(" + (((double) elapsed) / 1_000_000) + "ms) = " + result_P2_N2_C.toString(TestHelpers.FORMAT, true));

                start = System.nanoTime();
                D result_P2_N2_NC = cons.staticAnd(instLarge, inst_P2_N2_NC);
                totalElap_P2_N2_NC += elapsed = System.nanoTime() - start;
//            System.out.println("result(P=2,N=2,NC)(" + (((double) elapsed) / 1_000_000) + "ms) = " + result_P2_N2_NC.toString(TestHelpers.FORMAT, true));

                start = System.nanoTime();
                D result_P1_N30_B = cons.staticAnd(instLarge, inst_P1_N30_B);
                totalElap_P1_N30_B += elapsed = System.nanoTime() - start;
//            System.out.println("result(P=2,N=30,B)(" + (((double) elapsed) / 1_000_000) + "ms) = " + result_P1_N30_B.toString(TestHelpers.FORMAT, true));
            }
            System.out.println("Average AND time (P=1,N=1,C) = " + (((double) totalElap_P1_N1_C) / 1_000_000 / NUM_ITER) + "ms");
            System.out.println("Average AND time (P=1,N=1,NC) = " + (((double) totalElap_P1_N1_NC) / 1_000_000 / NUM_ITER) + "ms");
            System.out.println("Average AND time (P=1,N=10,C) = " + (((double) totalElap_P1_N10_C) / 1_000_000 / NUM_ITER) + "ms");
            System.out.println("Average AND time (P=1,N=10,NC) = " + (((double) totalElap_P1_N10_NC) / 1_000_000 / NUM_ITER) + "ms");
            System.out.println("Average AND time (P=1,N=30,B) = " + (((double) totalElap_P1_N30_B) / 1_000_000 / NUM_ITER) + "ms");
            System.out.println("Average AND time (P=2,N=2,C) = " + (((double) totalElap_P2_N2_C) / 1_000_000 / NUM_ITER) + "ms");
            System.out.println("Average AND time (P=2,N=2,NC) = " + (((double) totalElap_P2_N2_NC) / 1_000_000 / NUM_ITER) + "ms");

            ////CASE: naive approach for cross(PhraseType) and cross(ConcreteType)
            //NUM_ITER = 50, largest = 14   (129sec)
            //    Average AND time (P=1,N=1,C) = 33.98771482ms
            //    Average AND time (P=1,N=1,NC) = 4.54804546ms
            //    Average AND time (P=2,N=2,C) = 543.9456256ms
            //    Average AND time (P=2,N=2,NC) = 1999.85577494ms
            //
            //NUM_ITER = 10, largest = 15
            //    Average AND time (P=1,N=1,C) = 105.82212790000001ms
            //    Average AND time (P=1,N=1,NC) = 9.483055799999999ms
            //    Average AND time (P=2,N=2,C) = 1663.7992985ms
            //    Average AND time (P=2,N=2,NC) = 7498.425758400001ms
            //
            //NUM_ITER = 50, largest = 15   (482sec)
            //    Average AND time (P=1,N=1,C) = 100.38007594000001ms
            //    Average AND time (P=1,N=1,NC) = 9.3905351ms
            //    Average AND time (P=2,N=2,C) = 1669.55087826ms
            //    Average AND time (P=2,N=2,NC) = 7813.13311248ms
            //
            //OBSERVATION: it makes sense that C(i.e. large contains values from small)
            //  is slower on P=1,N=1 b/c it actually has to do absorption checks, etc.
            //
            //NUM_ITER = 20, largest = 15   (275sec)
            //    Average AND time (P=1,N=1,C) = 114.26654185ms
            //    Average AND time (P=1,N=1,NC) = 12.6458057ms
            //    Average AND time (P=1,N=10,C) = 144.1022491ms
            //    Average AND time (P=1,N=10,NC) = 1757.9982725000002ms
            //    Average AND time (P=2,N=2,C) = 1794.80604945ms
            //    Average AND time (P=2,N=2,NC) = 8965.28044855ms
            //
            //NUM_ITER = 10, largest = 15   (130sec)
            //    Average AND time (P=1,N=1,C) = 107.422686ms
            //    Average AND time (P=1,N=1,NC) = 11.0696142ms
            //    Average AND time (P=1,N=10,C) = 123.074946ms
            //    Average AND time (P=1,N=10,NC) = 1693.7241766ms
            //    Average AND time (P=2,N=2,C) = 1768.4647933ms
            //    Average AND time (P=2,N=2,NC) = 9082.9227728ms
            //
            ////CASE: changed  cross(PhraseType) to use loop of appendAllPhrases instead of union+tryAdd
            //NUM_ITER = 10, largest = 15   (104sec)
            //    Average AND time (P=1,N=1,C) = 98.3462763ms
            //    Average AND time (P=1,N=1,NC) = 10.6524985ms
            //    Average AND time (P=1,N=10,C) = 178.7676289ms
            //    Average AND time (P=1,N=10,NC) = 41.6200009ms
            //    Average AND time (P=2,N=2,C) = 1682.0928935ms
            //    Average AND time (P=2,N=2,NC) = 8159.278070099999ms
            //
            //NUM_ITER = 10, largest = 15   (107sec)
            //    Average AND time (P=1,N=1,C) = 100.8573449ms
            //    Average AND time (P=1,N=1,NC) = 10.857935999999999ms
            //    Average AND time (P=1,N=10,C) = 125.5174941ms
            //    Average AND time (P=1,N=10,NC) = 42.3925269ms
            //    Average AND time (P=2,N=2,C) = 1725.9515651999998ms
            //    Average AND time (P=2,N=2,NC) = 8525.5168784ms
            //
            //NUM_ITER = 10, largest = 15   (105sec)
            //    Average AND time (P=1,N=1,C) = 101.12309189999999ms
            //    Average AND time (P=1,N=1,NC) = 9.6072463ms
            //    Average AND time (P=1,N=10,C) = 128.80355179999998ms
            //    Average AND time (P=1,N=10,NC) = 37.1244317ms
            //    Average AND time (P=1,N=30,B) = 186.1532835ms
            //    Average AND time (P=2,N=2,C) = 1684.3360803ms
            //    Average AND time (P=2,N=2,NC) = 8089.954330999999ms
            //
            ////CASE: changed cross(ConcreteType) to use cross(P)+merge instead of union+tryAdd
            //NUM_ITER = 10, largest = 15   (??sec)
            //    Average AND time (P=1,N=1,C) = 101.9419272ms
            //    Average AND time (P=1,N=1,NC) = 9.9762457ms
            //    Average AND time (P=1,N=10,C) = 128.5751644ms
            //    Average AND time (P=1,N=10,NC) = 39.4631526ms
            //    Average AND time (P=1,N=30,B) = 152.9905049ms
            //    Average AND time (P=2,N=2,C) = 351.8056669ms
            //    Average AND time (P=2,N=2,NC) = 682.7529299ms
            //
            //NUM_ITER = 10, largest = 15   (17sec)
            //    Average AND time (P=1,N=1,C) = 101.09722719999999ms
            //    Average AND time (P=1,N=1,NC) = 9.8439266ms
            //    Average AND time (P=1,N=10,C) = 128.5180166ms
            //    Average AND time (P=1,N=10,NC) = 40.939232399999995ms
            //    Average AND time (P=1,N=30,B) = 180.3836642ms
            //    Average AND time (P=2,N=2,C) = 352.2061144ms
            //    Average AND time (P=2,N=2,NC) = 642.281351ms
            //
            //NUM_ITER = 10, largest = 17 (24K phrases)   (337sec)
            //    Average AND time (P=1,N=1,C) = 1487.2661936ms
            //    Average AND time (P=1,N=1,NC) = 36.578486600000005ms
            //    Average AND time (P=1,N=10,C) = 1907.7493170000002ms
            //    Average AND time (P=1,N=10,NC) = 148.83995910000002ms
            //    Average AND time (P=1,N=30,B) = 2899.644179ms
            //    Average AND time (P=2,N=2,C) = 6111.5398541ms
            //    Average AND time (P=2,N=2,NC) = 15180.892495799999ms
            //
            //NUM_ITER = 100, largest = 15   (140sec)
            //    Average AND time (P=1,N=1,C) = 95.95352774999999ms
            //    Average AND time (P=1,N=1,NC) = 8.39856783ms
            //    Average AND time (P=1,N=10,C) = 122.93084008ms
            //    Average AND time (P=1,N=10,NC) = 34.59751223ms
            //    Average AND time (P=1,N=30,B) = 161.34563272ms
            //    Average AND time (P=2,N=2,C) = 334.79008331ms
            //    Average AND time (P=2,N=2,NC) = 621.68556961ms
            //
        }
    }

    @Test
    public void testAbsorbsNF() {
        System.out.println("test_absorbs(NF)");
        Construction<P, E, D> cons = getCons();

        //Create all NormalForm as unmodifiable (i.e. clone(true))
        D nf_emptyPhrase = cons.newFromPhrase(cons.buildPhrase()).clone(true);
        D nf_nullPhrase = cons.newFromEmpty().clone(true);

        ArrayList<D> nfs = new ArrayList<>();
        nfs.add(cons.newFromPhrase(cons.buildPhrase(1, 5, 7)).clone(true));
        nfs.add(cons.newFromPhrase(cons.buildPhrase(5, 7)).clone(true));
        nfs.add(cons.newFromPhrase(cons.buildPhrase(1, 5, 7, 8)).clone(true));
        nfs.add(cons.newFromPhrase(cons.buildPhrase(2, 9)).clone(true));
        nfs.add(cons.buildSentence(TestHelpers.list(cons.buildPhrase(2, 9), cons.buildPhrase(1, 5, 7))).clone(true));
        nfs.add(cons.buildSentence(TestHelpers.list(cons.buildPhrase(2, 9), cons.buildPhrase(1, 5, 7, 12))).clone(true));
        nfs.add(cons.buildSentence(TestHelpers.list(cons.buildPhrase(2, 9), cons.buildPhrase(1, 5, 7), cons.buildPhrase(3, 6))).clone(true));
        nfs.add(cons.buildSentence(TestHelpers.list(cons.buildPhrase(7, 8), cons.buildPhrase(10, 11))).clone(true));

        //Any NormalForm always absorbs itself
        Assert.assertTrue(nf_emptyPhrase.absorbs(nf_emptyPhrase));
        Assert.assertTrue(nf_nullPhrase.absorbs(nf_nullPhrase));
        for (D i : nfs) {
            Assert.assertTrue(i.absorbs(i));
        }

        //TRUE absorbs everything
        Assert.assertTrue(nf_emptyPhrase.absorbs(nf_nullPhrase));
        for (D i : nfs) {
            Assert.assertTrue(nf_emptyPhrase.absorbs(i));
        }

        //FALSE absorbs nothing (other than itself)
        Assert.assertFalse(nf_nullPhrase.absorbs(nf_emptyPhrase));
        for (D i : nfs) {
            Assert.assertFalse(nf_nullPhrase.absorbs(i));
        }

        //Nothing absorbs TRUE (other than itself)
        Assert.assertFalse(nf_nullPhrase.absorbs(nf_emptyPhrase));
        for (D i : nfs) {
            Assert.assertFalse(i.absorbs(nf_emptyPhrase));
        }

        //Everything absorbs FALSE
        Assert.assertTrue(nf_emptyPhrase.absorbs(nf_nullPhrase));
        for (D i : nfs) {
            Assert.assertTrue(i.absorbs(nf_nullPhrase));
        }

        //"A absorbs B" is equivalent to "(A merge B) == A"
        for (D i : nfs) {
            for (D j : nfs) {
//                System.out.println(i + " absorbs " + j + " = " + i.absorbs(j));
                D clone = i.clone(false);
                clone.merge(j);
                Assert.assertEquals(clone.equals(i), i.absorbs(j));
            }
        }
    }
}

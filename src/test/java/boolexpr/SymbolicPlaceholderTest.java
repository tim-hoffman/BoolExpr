package boolexpr;

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

import static org.junit.Assert.assertEquals;
import org.junit.*;

/**
 *
 * @author Timothy Hoffman
 */
public class SymbolicPlaceholderTest {

    public SymbolicPlaceholderTest() {

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

    @Test
    public void testSymbolicPlaceholders() {
        System.out.println("test_EmptyConstructor");

        //Assumption: sensors [0,99] are the actual temp sensor numbers
        //          above that, they are symbolic.
        //Question: will that work or will absorption ever remove an entire
        //  symbolic phrase when it shouldn't?
        //  I think that is more likely if a symbolic phrase contains ORs.
        //  But it seems the absorption law works at the meta level.
        DisjunctiveNormalFormInt phrase1 = new DisjunctiveNormalFormInt(1);
        phrase1.and(2);
        phrase1.and(3);
        phrase1.and(4);
        phrase1 = phrase1.asUnmodifiable();

        DisjunctiveNormalFormInt phrase2 = new DisjunctiveNormalFormInt(5);
        phrase2.and(6);
        phrase2.and(7);
        phrase2.and(8);
        phrase2 = phrase2.asUnmodifiable();

        DisjunctiveNormalFormInt phrase3 = new DisjunctiveNormalFormInt(9);
        phrase3.and(10);
        phrase3.and(11);
        phrase3.and(12);
        phrase3 = phrase3.asUnmodifiable();

        DisjunctiveNormalFormInt phrase4 = new DisjunctiveNormalFormInt(13);
        phrase4.and(14);
        phrase4.and(15);
        phrase4.and(16);
        phrase4 = phrase4.asUnmodifiable();

        //// Build the base normal forms
        DisjunctiveNormalFormInt symbolic_100 = new DisjunctiveNormalFormInt(phrase2);
        symbolic_100.or(phrase3);
        symbolic_100.or(phrase4);
        symbolic_100 = symbolic_100.asUnmodifiable();
        System.out.println("sybolic_100: " + symbolic_100.toString(true));

        DisjunctiveNormalFormInt symbolic_101 = new DisjunctiveNormalFormInt(phrase1);
        symbolic_101.or(phrase2);
        symbolic_101.or(phrase3);
        symbolic_101 = symbolic_101.asUnmodifiable();
        System.out.println("sybolic_101: " + symbolic_101.toString(true));

        DisjunctiveNormalFormInt withoutSymbolic = new DisjunctiveNormalFormInt(phrase1);
        withoutSymbolic.or(phrase2);
        withoutSymbolic.or(phrase3);
        withoutSymbolic.or(phrase4);
        withoutSymbolic = withoutSymbolic.asUnmodifiable();
        System.out.println("withoutSymbolic: " + withoutSymbolic.toString(true));

        DisjunctiveNormalFormInt withSymbolic = new DisjunctiveNormalFormInt(phrase1);
        withSymbolic.or(100);
        withSymbolic = withSymbolic.asUnmodifiable();
        System.out.println("withSymbolic: " + withSymbolic.toString(true));

        //// Try some operations on them and compare the results
        {
            // OR with a phrase that will absorb something in the symbolic reference
            DisjunctiveNormalFormInt absorbingPhrase = new DisjunctiveNormalFormInt(10);
            DisjunctiveNormalFormInt i1 = DisjunctiveNormalFormInt.or(absorbingPhrase, withoutSymbolic);
            System.out.println("absorbing OR (without): " + i1.toString(true));
            DisjunctiveNormalFormInt i2 = DisjunctiveNormalFormInt.or(absorbingPhrase, withSymbolic);
            System.out.println("absorbing OR (with-be): " + i2.toString(true));
            //Resolve symbolic references
            i2.resolve(100, symbolic_100);
            System.out.println("absorbing OR (with-af): " + i2.toString(true));
            assertEquals(i1, i2);
        }
        {
            // AND multiple symbolic references
            DisjunctiveNormalFormInt withoutSymbolic2 = new DisjunctiveNormalFormInt(withoutSymbolic);
            DisjunctiveNormalFormInt withSymbolic2 = new DisjunctiveNormalFormInt(withSymbolic);
            //AND both versions (symbolic and non-symbolic)
            withSymbolic2.and(101);
            withoutSymbolic2.and(symbolic_101);
            System.out.println("withSymbolic2: " + withSymbolic2.toString(true));
            System.out.println("withoutSymbolic2: " + withoutSymbolic2.toString(true));
            //Resolve symbolic references
            withSymbolic2.resolve(100, symbolic_100);
            withSymbolic2.resolve(101, symbolic_101);
            System.out.println("withSymbolic2 (after): " + withSymbolic2.toString(true));
            assertEquals(withSymbolic2, withoutSymbolic2);
        }
    }

    @Test
    public void testMoreSimilarToRealUsage() {
        //We create an indicator set for each flow in a method.
        //We then generate a symbolic reference for each flow's indicator
        //  set and resolve the entire indicator set with the symbolic ref.
        //When those flows are used by other methods, they're just using the
        //  symbolic references combined with their own local sensor numbers.
        //Then, at the very end, we resolve all symbolic references.
        //NOTE: this approach will create unnecessary symbolic references for
        //  recursive methods and other methods processed more than once.

        //"Process" m1 to generate the following indicator sets
        //SENSORS: 1..10
        //SYMBOLIC: 101..105
        DisjunctiveNormalFormInt m1_101 = new DisjunctiveNormalFormInt(1).and(2).and(3).or(4).and(5).asUnmodifiable();
        DisjunctiveNormalFormInt m1_102 = new DisjunctiveNormalFormInt(6).and(2).or(DisjunctiveNormalFormInt.and(3, 7)).asUnmodifiable();
        DisjunctiveNormalFormInt m1_103 = new DisjunctiveNormalFormInt(8).asUnmodifiable();
        DisjunctiveNormalFormInt m1_104 = new DisjunctiveNormalFormInt(2).asUnmodifiable();
        DisjunctiveNormalFormInt m1_105 = new DisjunctiveNormalFormInt(9).or(2).or(3).or(10).or(8).asUnmodifiable();
        System.out.println("101 = " + m1_101.toString(true));
        System.out.println("102 = " + m1_102.toString(true));
        System.out.println("103 = " + m1_103.toString(true));
        System.out.println("104 = " + m1_104.toString(true));
        System.out.println("105 = " + m1_105.toString(true));

        //"Process" m2 to generate its indicator sets using symbolic names
        //  for the indicator sets of m1.
        //SENSORS: 11..18
        //SYMBOLIC: 111..114
        DisjunctiveNormalFormInt m2_111 = new DisjunctiveNormalFormInt(11).or(104).and(12).or(103).and(13);
        DisjunctiveNormalFormInt m2_112 = new DisjunctiveNormalFormInt(14).and(101).or(new DisjunctiveNormalFormInt(101).and(15));
        DisjunctiveNormalFormInt m2_113 = new DisjunctiveNormalFormInt(101);
        DisjunctiveNormalFormInt m2_114 = new DisjunctiveNormalFormInt(16).or(17).or(18).or(101).or(104);
        System.out.println("111 = " + m2_111.toString(true));
        System.out.println("112 = " + m2_112.toString(true));
        System.out.println("113 = " + m2_113.toString(true));
        System.out.println("114 = " + m2_114.toString(true));

        //"Process" m3 to generate its indicator sets using symbolic names
        //  for the indicator sets of m1 and m2.
        //SENSORS: 19..23
        //SYMBOLIC: 121..122
        DisjunctiveNormalFormInt m3_121 = new DisjunctiveNormalFormInt(19).or(20).or(111).and(101).and(21).and(105);
        DisjunctiveNormalFormInt m3_122 = new DisjunctiveNormalFormInt(22).or(23).or(113).and(DisjunctiveNormalFormInt.or(23, 20).or(114));
        System.out.println("121 = " + m3_121.toString(true));
        System.out.println("122 = " + m3_122.toString(true));

        //"Process" m3 a second time (i.e. it's self-recursive)
        //SENSORS: 19..24
        //SYMBOLIC: 123..124
        DisjunctiveNormalFormInt m3_123 = new DisjunctiveNormalFormInt(121).or(20).or(111).and(23).and(24);
        DisjunctiveNormalFormInt m3_124 = new DisjunctiveNormalFormInt(22).or(122).or(113).and(DisjunctiveNormalFormInt.or(19, 121).or(24));
        System.out.println("123 = " + m3_123.toString(true));
        System.out.println("124 = " + m3_124.toString(true));

        System.out.println("RESOLVING...");
        //Finally, resolve all symbolic references
        //If we resolve in the same order, we should't have to repeat
        //10_
        //  nothing to resolve 
        //11_
        //  only symbolic references are from 10_
        m2_111.resolve(101, m1_101);
        m2_111.resolve(102, m1_102);
        m2_111.resolve(103, m1_103);
        m2_111.resolve(104, m1_104);
        m2_111.resolve(105, m1_105);
        System.out.println("111 (resolved) = " + m2_111.toString(true));

        m2_112.resolve(101, m1_101);
        m2_112.resolve(102, m1_102);
        m2_112.resolve(103, m1_103);
        m2_112.resolve(104, m1_104);
        m2_112.resolve(105, m1_105);
        System.out.println("112 (resolved) = " + m2_112.toString(true));

        m2_113.resolve(101, m1_101);
        m2_113.resolve(102, m1_102);
        m2_113.resolve(103, m1_103);
        m2_113.resolve(104, m1_104);
        m2_113.resolve(105, m1_105);
        System.out.println("113 (resolved) = " + m2_113.toString(true));

        m2_114.resolve(101, m1_101);
        m2_114.resolve(102, m1_102);
        m2_114.resolve(103, m1_103);
        m2_114.resolve(104, m1_104);
        m2_114.resolve(105, m1_105);
        System.out.println("114 (resolved) = " + m2_114.toString(true));

        //12_
        //  could contain symbolic references from 10_ or 11_ (but applied in any order since 11_ references were already resolved)
        m3_121.resolve(101, m1_101);
        m3_121.resolve(102, m1_102);
        m3_121.resolve(103, m1_103);
        m3_121.resolve(104, m1_104);
        m3_121.resolve(105, m1_105);
        m3_121.resolve(111, m2_111);
        m3_121.resolve(112, m2_112);
        m3_121.resolve(113, m2_113);
        m3_121.resolve(114, m2_114);
        System.out.println("121 (resolved) = " + m3_121.toString(true));

        m3_122.resolve(101, m1_101);
        m3_122.resolve(102, m1_102);
        m3_122.resolve(103, m1_103);
        m3_122.resolve(104, m1_104);
        m3_122.resolve(105, m1_105);
        m3_122.resolve(111, m2_111);
        m3_122.resolve(112, m2_112);
        m3_122.resolve(113, m2_113);
        m3_122.resolve(114, m2_114);
        System.out.println("122 (resolved) = " + m3_122.toString(true));

        m3_123.resolve(101, m1_101);
        m3_123.resolve(102, m1_102);
        m3_123.resolve(103, m1_103);
        m3_123.resolve(104, m1_104);
        m3_123.resolve(105, m1_105);
        m3_123.resolve(111, m2_111);
        m3_123.resolve(112, m2_112);
        m3_123.resolve(113, m2_113);
        m3_123.resolve(114, m2_114);
        System.out.println("123 (resolved) = " + m3_123.toString(true));

        m3_124.resolve(101, m1_101);
        m3_124.resolve(102, m1_102);
        m3_124.resolve(103, m1_103);
        m3_124.resolve(104, m1_104);
        m3_124.resolve(105, m1_105);
        m3_124.resolve(111, m2_111);
        m3_124.resolve(112, m2_112);
        m3_124.resolve(113, m2_113);
        m3_124.resolve(114, m2_114);
        System.out.println("124 (resolved) = " + m3_124.toString(true));

        //Compute the same ones as above without using symbolic references
        //m1 : skip because there were no symbolics
        //m2 : 
        DisjunctiveNormalFormInt m2_111_b = new DisjunctiveNormalFormInt(11).or(m1_104).and(12).or(m1_103).and(13);
        DisjunctiveNormalFormInt m2_112_b = new DisjunctiveNormalFormInt(14).and(m1_101).or(new DisjunctiveNormalFormInt(m1_101).and(15));
        DisjunctiveNormalFormInt m2_113_b = new DisjunctiveNormalFormInt(m1_101);
        DisjunctiveNormalFormInt m2_114_b = new DisjunctiveNormalFormInt(16).or(17).or(18).or(m1_101).or(m1_104);
        System.out.println("111(b) = " + m2_111_b.toString(true));
        System.out.println("112(b) = " + m2_112_b.toString(true));
        System.out.println("113(b) = " + m2_113_b.toString(true));
        System.out.println("114(b) = " + m2_114_b.toString(true));

        //m3 : first iteration
        DisjunctiveNormalFormInt m3_121_b = new DisjunctiveNormalFormInt(19).or(20).or(m2_111_b).and(m1_101).and(21).and(m1_105);
        DisjunctiveNormalFormInt m3_122_b = new DisjunctiveNormalFormInt(22).or(23).or(m2_113_b).and(DisjunctiveNormalFormInt.or(23, 20).or(m2_114));
        System.out.println("121(b) = " + m3_121_b.toString(true));
        System.out.println("122(b) = " + m3_122_b.toString(true));

        // m3 : second iteration
        DisjunctiveNormalFormInt m3_123_b = new DisjunctiveNormalFormInt(121).or(20).or(m2_111_b).and(23).and(24);
        DisjunctiveNormalFormInt m3_124_b = new DisjunctiveNormalFormInt(22).or(122).or(m2_113_b).and(DisjunctiveNormalFormInt.or(19, 121).or(24));
        System.out.println("123(b) = " + m3_123_b.toString(true));
        System.out.println("124(b) = " + m3_124_b.toString(true));

        //Compare
        assertEquals(m2_111, m2_111_b);
        assertEquals(m2_112, m2_112_b);
        assertEquals(m2_113, m2_113_b);
        assertEquals(m2_114, m2_114_b);
        assertEquals(m3_121, m3_121_b);
        assertEquals(m3_122, m3_122_b);
        assertEquals(m3_123, m3_123_b);
        assertEquals(m3_124, m3_124_b);

        //ALL GOOD!
        //Now, the question is, how do we store all the replacements and do the resolutions?
        //Basically, a method could use ANY previous method's symbolic references.
        //If ALL symbolic references have been replaced in the sybolic replacement DNFs,
        //  then there is no need to perform the replacements in any particular order.
        //Otherwise, we have to peform them in order (just like the mutex paths thing).
        //Furthermore, I can't apply replacements within a certain method until I 
        //  know that method won't be processed again. So better just wait until the
        //  end and then apply them in the same order the methods were originally processed.
        //
        //Q: Should I just create them on-demand (i.e. as a method's flows are used) rather
        //  than always summarizing for every method? Could end up with more duplicates then.
        //
        //Q: How do I handle the ones that recurse? How can I still reach a fixed point
        //  when the same method is visited again?
        //  A: Maybe need to lookup if I already have symbolics for a method and use them.
        //  Ex: First pass gives A_1=1&2&3, B_1=C&5; second pass gives A_2=1&2&3, B_2=C&5
        //      Need to recognize that A_1==A_2 and B_1==B_2 so fixed point has been reached.
        //  The 'dst' set after 'analyseMethod' needs to have the same value twice. So, I guess
        //      if we had the symbolic results from the first time, we compare what we new have
        //      to that to check for a match. But will that require any amount of resolution?
        //  TODO: see if, on the second time processing, I end up with symbolics that 
        //      were generated on the first time processing. Those should be the only
        //      ones requiring resolution.
    }
}

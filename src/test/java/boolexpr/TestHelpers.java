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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Timothy Hoffman
 */
public class TestHelpers {

    public static final NormalForm.PrintingConnectives FORMAT = new NormalForm.PrintingConnectives("(", ")", ",", "");

    /**
     *
     * @param <E>
     * @param cons
     * @param startElem
     * @param length
     *
     * @return a new array of {@code length} sequential elements from the given
     *         {@link Construction} starting at element {@code startElem}.
     */
    public static <E> E[] buildElemList(Construction<?, E, ?> cons, int startElem, int length) {
        @SuppressWarnings("unchecked")
        E[] retVal = (E[]) Array.newInstance(cons.getElemFor(startElem).getClass(), length);
        for (int i = 0; i < length; i++) {
            retVal[i] = cons.getElemFor(i + startElem);
        }
        return retVal;
    }

    /**
     * Gets all combinations of values (w/o repetition) of the given
     * {@code length} from the given {@code universe}.
     *
     * NOTE: The number of results in the returned {@link List} will be
     * {@code A! / ((A-B)! * B!)} where {@code A = universe.length} and
     * {@code B = length}. Thus, the maximum number of results occurs when
     * {@code length = universe.length / 2}.
     *
     * @param universe
     * @param length
     *
     * @return
     */
    public static <T> List<T[]> combinationsNoRepetition(T[] universe, int length) {
        List<T[]> finalResult = new ArrayList<>();
        combinNoRep_Rec(universe, 0, length, new Object[length], finalResult);
        return finalResult;
    }

    /**
     * Gets all combinations of values (w/o repetition) from the given
     * {@code universe} where length of the combinations is determined by the
     * size of {@code tempResult} and the results are stored in
     * {@code finalResult}.
     *
     * @param universe
     * @param startPosition
     * @param len
     * @param tempResult
     * @param finalResult
     */
    @SuppressWarnings("unchecked")
    private static <T> void combinNoRep_Rec(T[] universe, int startPosition, int len, Object[] tempResult, List<T[]> finalResult) {
        if (len == 0) {
            finalResult.add((T[]) Arrays.copyOf(tempResult, tempResult.length, universe.getClass()));
        } else {
            for (int i = startPosition; i <= universe.length - len; i++) {
                tempResult[tempResult.length - len] = universe[i];
                combinNoRep_Rec(universe, i + 1, len - 1, tempResult, finalResult);
            }
        }
    }

    /**
     * Return uniformly distributed value in the range {@code [min-max)}.
     *
     * @param min inclusive
     * @param max exclusive
     *
     * @return
     */
    public static int getRandomInRange(int min, int max) {
        return (int) (Math.random() * (max - min)) + min;
    }

    /**
     * Return {@code count} uniformly distributed values in the range
     * {@code [min-max)}.
     *
     * @param count
     * @param min             inclusive
     * @param max             exclusive
     * @param allowDuplicates
     *
     * @return
     */
    public static int[] getRandomInRange(int count, int min, int max, boolean allowDuplicates) {
        if (!allowDuplicates && max - min < count) {
            throw new IllegalArgumentException("Unable to generate " + count + " values without repetition in the range [" + min + "," + max + ")");
        }
        HashSet<Integer> selected = new HashSet<>();
        int[] retVal = new int[count];
        for (int i = 0; i < count; i++) {
            retVal[i] = getRandomInRange(min, max);
            if (!allowDuplicates) {
                if (selected.contains(retVal[i])) {
                    i--;//select index i again
                } else {
                    selected.add(retVal[i]);
                }
            }
        }
        return retVal;
    }

    /**
     *
     * @param <N>
     * @param elems
     *
     * @return
     */
    @SafeVarargs
    public static <N> List<N> list(N... elems) {
        List<N> retVal = new ArrayList<>();
        retVal.addAll(Arrays.asList(elems));
        return retVal;
    }

    /**
     * Constructs a new sentence with as many equal-sized phrases as possible
     * over the given number of variables (in the range
     * {@code 0 <= i < numVars}).
     *
     * The number of phrases grows very quickly relative to the number of
     * variables according to the function
     * {@code X!/((floor(X/2)!)^2*(floor(X/2)*(X%2)+1)}. Alternatively,
     * {@code f(X) = f(X-1)*(1 + floor(X/2)/ceil(X/2))}. Essentially, it nearly
     * doubles with every increment of {@code numVars}.
     *
     * The size of each phrase grows more slowly as {@code ceil(X/2)}.
     *
     * Finally, the number of occurances of variable {@code i} follows the same
     * formula as the number of phrases but with {@code X-1}.
     *
     * @param <P>
     * @param <E>
     * @param <D>
     * @param cons
     * @param numVars
     *
     * @return
     */
    public static <P, E, D extends NormalForm<P, E, D>> D buildLargestInstance(Construction<P, E, D> cons, int numVars) {
        E[] universe = TestHelpers.buildElemList(cons, 0, numVars);
        D instance = cons.newFromEmpty();
        for (E[] s : TestHelpers.combinationsNoRepetition(universe, (int) Math.ceil(universe.length / 2.0))) {
            instance.merge(cons.newFromPhrase(cons.buildPhrase(s)));
        }
        return instance;
    }

    private TestHelpers() {
    }
}

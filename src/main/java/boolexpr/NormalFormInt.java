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

import boolexpr.util.Ordering;
import boolexpr.util.SparseBitSet;
import java.util.*;
import boolexpr.util.Numberer;

/**
 * Implementation of {@link NormalForm} where propositions have type
 * {@link Integer} and are stored in a {@link SparseBitSet} for efficient
 * storage. Useful when values have a wide range between min and max or when the
 * max is not known ahead of time (i.e. cannot use enum).
 *
 * @author Timothy Hoffman
 * 
 * @param <P>
 */
/*package*/ abstract class NormalFormInt<ConcreteType extends NormalFormInt<ConcreteType>> extends NormalForm<SparseBitSet, Integer, ConcreteType> {

    /**
     * Creates a {@link NormalFormInt} with a single phrase, unless the given
     * {@link SparseBitSet} is {@code null}, in which case the created
     * {@link NormalFormInt} will be empty (i.e. contains 0 phrases).
     *
     * @param firstPhrase
     */
    protected NormalFormInt(FormRules formRules, SparseBitSet firstPhrase) {
        super(formRules, firstPhrase);
    }

    /**
     * Creates a {@link NormalFormInt} with a single phrase containing a single
     * proposition (or containing no propositions if {@code firstProp} is null).
     *
     * @param firstProp
     */
    protected NormalFormInt(FormRules formRules, Integer firstProp) {
        super(formRules, createSingletonInternal(firstProp));
    }

    /**
     * Creates a {@link NormalFormEnum} with no phrases.
     */
    protected NormalFormInt(FormRules formRules) {
        super(formRules);
    }

    /**
     * Copy constructor, performs a deep copy of the given
     * {@link NormalFormInt}.
     *
     * @param original     the {@link NormalFormInt} to duplicate
     * @param unmodifiable whether or not the new instance should be marked as
     *                     unmodifiable/immutable
     */
    protected NormalFormInt(ConcreteType original, boolean unmodifiable) {
        super(original, unmodifiable);
    }

    /**
     * Static implementation of {@link #createSingleton(Integer)}.
     *
     * @param singleProp
     *
     * @return a new {@link SparseBitSet} containing only the given element, or
     *         no elements if the given element is null.
     */
    private static SparseBitSet createSingletonInternal(Integer singleProp) {
        SparseBitSet retVal = new SparseBitSet();
        if (singleProp != null) {
            retVal.set(singleProp);
        }
        return retVal;
    }

    /**
     * @return a new {@link SparseBitSet} containing all unique propositions
     *         contained in any phrase of {@code this}
     */
    public final SparseBitSet getAllPropsBitSet() {
        SparseBitSet retVal = new SparseBitSet();
        for (SparseBitSet s : data) {
            retVal.or(s);
        }
        return retVal;
    }

    @Override
    protected final SparseBitSet clone(SparseBitSet orig) {
        return orig.clone();
    }

    @Override
    protected int size(SparseBitSet set) {
        return set.cardinality();
    }

    @Override
    protected boolean isEmpty(SparseBitSet set) {
        return set.isEmpty();
    }

    /**
     * Utility method to check if s1 contains all elements in s2.
     *
     * @param s1
     * @param s2
     *
     * @return {@code true} iff s1 contains all elements in s2
     */
    @Override
    protected boolean containsAll(SparseBitSet s1, SparseBitSet s2) {
        return s1.containsAll(s2);
    }

    /**
     * Utility method to check if s1 contains any element in s2.
     *
     * @param s1
     * @param s2
     *
     * @return {@code true} iff s1 contains any element in s2
     */
    @Override
    protected boolean containsAny(SparseBitSet s1, SparseBitSet s2) {
        //Traverse s2 to see if any value is in s1.
        //NOTE: If the size were computed then I could traverse the smaller
        //  of 's1' and 's2' but the size/stats can also be expensive.
        for (int i = s2.minSetBit(); i >= 0; i = s2.nextSetBit(i + 1)) {
            if (s1.get(i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean contains(SparseBitSet s, Integer e) {
        return s.get(e);
    }

    @Override
    protected SparseBitSet createSingleton(Integer singleProp) {
        return createSingletonInternal(singleProp);
    }

    @Override
    protected void add(SparseBitSet set, Integer newItem) {
        if (newItem != null) {
            set.set(newItem);
        }
    }

    @Override
    protected void remove(SparseBitSet set, Integer item) {
        set.clear(item);
    }

    @Override
    protected void addAll(SparseBitSet base, SparseBitSet toAdd) {
        base.or(toAdd);
    }

    @Override
    protected void collectToSet(SparseBitSet src, HashSet<Integer> dst) {
        for (int i = src.minSetBit(); i >= 0; i = src.nextSetBit(i + 1)) {
            dst.add(i);
        }
    }

    @Override
    protected Iterable<SparseBitSet> order(Collection<SparseBitSet> phrases) {
        return Ordering.orderBitSets(phrases);
    }

    @Override
    protected Iterator<Integer> iterator(SparseBitSet phrase, boolean ordered) {
        return phrase.iterator();//NOTE: SparseBitSet iterator is always ordered
    }

    @Override
    protected Integer minElem(SparseBitSet phrase) {
        int min = phrase.minSetBit();
        return min < 0 ? null : min;
    }

    @Override
    public Integer parseElement(String s) {
        return s.isEmpty() ? null : Integer.parseInt(s);
    }

    /**
     * Explores all phrases in {@code this} to check for elements in the given
     * {@link Set}. All phrases not containing an element in the {@link Set} are
     * removed and numbered (as a whole) and replaced with a singleton phrase
     * containing that number. All phrases that do contain an element in the
     * {@link Set} are preserved but all elements in a phrase that are not in
     * the {@code preserve} {@link Set} are extracted out, numbered, and
     * replaced with only that number. If {@code skipTrivialExtractions==true},
     * then phrases and sub-phrases containing just a single proposition (i.e.
     * the replacement would not change the overall size of {@code this}) are
     * not replaced.
     *
     * NOTE: I'm not sure how to implement this method for the NormalFormEnum
     * because there is no way to generate new elements in an Enum if they are
     * all exhausted! Hence, why it is not a method on NormalForm.
     *
     * @param preserve
     * @param extractions
     * @param skipTrivialExtractions
     */
    public void extractIrrelevantSubpaths(Set<Integer> preserve, Numberer<ConcreteType> extractions, boolean skipTrivialExtractions) {
        checkModifiability();

        //create a copy of data and clear data
        ArrayList<SparseBitSet> dataCopy = new ArrayList<>(this.data);
        this.data.clear();

        //create another empty ConcreteType that will contain all complete
        //  phrases that are irrelevant and in the end should be replaced
        //  by a new singleton phrase.
        ConcreteType removedPhrases = this.clone(false);

        //iterate the copy, determining if the phrase should go into
        //  'removedPhrases' or instead be updated and re-added to data
        //for those that should be re-added to data, instead of just numbering
        //  the irrelevant part and updating only the current phrase, we group
        //  these phrases by the relevant part that remains, creating a disjunction
        //  for each that can be entirely removed leaving only the relevant part
        //  and a single inserted number to replace an entire set of phrases.
        //  ex: A&B | A&C |... becomes A&Z where Z=B|C|... 
        Map<SparseBitSet, ConcreteType> relevantPhrToIrrelevantChunks = new HashMap<>();
        for (SparseBitSet phrase : dataCopy) {
            //will contain all elements that are not in 'preserve'; initially empty
            SparseBitSet irrelevantElems = createSingletonInternal(null);

            //traverse the elements in 'phrase' checking if they should be preserved
            for (Iterator<Integer> itr = iterator(phrase, false); itr.hasNext();) {
                Integer e = itr.next();
                //if the element is not in the 'preserve' set, then remove it
                //  from 'phrase' and add it to 'irrelevantElems'
                if (!preserve.contains(e)) {
                    add(irrelevantElems, e);
                    itr.remove();
                }
            }
            if (size(phrase) == 0) {
                //if 'phrase' becomes empty, then the entire phrase should be
                //  removed so just add 'irrelevantElems' to 'removedPhrases'.
                removedPhrases.tryAddWithAbsorption(irrelevantElems);
            } else if (size(irrelevantElems) > 0) {
                //otherwise, if 'irrelevantElems' is not empty, then store it in
                //  the 'relevantPhrToIrrelevantChunks' Map keyed on the relevant
                //  part of the phrase.
                ConcreteType partialPhraseRemoved = create(irrelevantElems);
                ConcreteType existing = relevantPhrToIrrelevantChunks.get(phrase);
                if (existing != null) {
                    partialPhraseRemoved.merge(existing);
                }
                relevantPhrToIrrelevantChunks.put(phrase, partialPhraseRemoved);
            }
        }
        //for each entry in the 'relevantPhrToIrrelevantChunks', add a phrase to
        //  'this' containing the key phrase with an additional number to
        //  represent the mapped ConcreteType
        for (Map.Entry<SparseBitSet, ConcreteType> e : relevantPhrToIrrelevantChunks.entrySet()) {
            SparseBitSet relevantSubPhrase = e.getKey();
            ConcreteType irrelevantSubPhrase = e.getValue();
            //if the 'irrelevantSubPhrase' contains more than one proposition,
            //  then generate a new symbolic replacement for it and add that to
            //  the 'relevantSubPhrase'. Otherwise, if 'skipTrivialExtractions'
            //  is true and it is only a single proposition, there is no need to
            //  create a replacement so just add it back to 'relevantSubPhrase'. 
            int num;
            if (skipTrivialExtractions && irrelevantSubPhrase.numPropsEquals(1)) {
                num = irrelevantSubPhrase.getAllProps().iterator().next();
            } else {
                num = extractions.getOrCreateNumber(irrelevantSubPhrase);
            }
            //Finally, add 'num' to the phrase and add the phrase back into 'this'
            add(relevantSubPhrase, num);
            this.tryAddWithAbsorption(relevantSubPhrase);
        }

        //finally, if 'removedPhrases' is not empty, then number it and add
        //  a singleton phrase to 'this' to represent all removed phrases.
        if (!removedPhrases.isEmpty()) {
            //similar to above, if 'skipTrivialExtractions' is true and the only
            //  thing removed was a single phrase containing a single
            //  proposition, then don't number and replace it.
            int num;
            if (skipTrivialExtractions && removedPhrases.numPropsEquals(1)) {
                num = removedPhrases.getAllProps().iterator().next();
            } else {
                num = extractions.getOrCreateNumber(removedPhrases);
            }
            this.tryAddWithAbsorption(createSingletonInternal(num));
        }
    }
}

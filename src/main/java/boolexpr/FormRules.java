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

/**
 *
 * @author Timothy Hoffman
 */
/*package*/ interface FormRules {

    /**
     * @param <P>
     * @param <E>
     * @param <D>
     * @param base
     *
     * @return {@code true} iff the given {@link NormalForm} represents the
     *         literal {@code false}.
     */
    public <P, E, D extends NormalForm<P, E, D>> boolean isFalse(NormalForm<P, E, D> base);

    /**
     * @param <P>
     * @param <E>
     * @param <D>
     * @param base
     *
     * @return {@code true} iff the given {@link NormalForm} represents the
     *         literal {@code true}.
     */
    public <P, E, D extends NormalForm<P, E, D>> boolean isTrue(NormalForm<P, E, D> base);

    /**
     * Clears the given {@link NormalForm} and reconstructs it to represent the
     * literal {@code false}.
     *
     * @param <P>
     * @param <E>
     * @param <D>
     * @param base
     *
     * @return {@code base}, modified to represent the literal {@code false}
     */
    public <P, E, D extends NormalForm<P, E, D>> D getFalse(D base);

    /**
     * Clears the given {@link NormalForm} and reconstructs it to represent the
     * literal {@code true}.
     *
     * @param <P>
     * @param <E>
     * @param <D>
     * @param base
     *
     * @return {@code base}, modified to represent the literal {@code true}
     */
    public <P, E, D extends NormalForm<P, E, D>> D getTrue(D base);

    /**
     *
     * @param <P>
     * @param <E>
     * @param <D>
     * @param base
     * @param newProp
     *
     * @return {@code base}, modified to represent {@code base & newProp}
     */
    public <P, E, D extends NormalForm<P, E, D>> D and(D base, E newProp);

    /**
     *
     * @param <P>
     * @param <E>
     * @param <D>
     * @param base
     * @param newSentence
     *
     * @return {@code base}, modified to represent {@code base & newSentence}
     */
    public <P, E, D extends NormalForm<P, E, D>> D and(D base, D newSentence);

    /**
     *
     * @param <P>
     * @param <E>
     * @param <D>
     * @param base
     * @param newProp
     *
     * @return {@code base}, modified to represent {@code base | newProp}
     */
    public <P, E, D extends NormalForm<P, E, D>> D or(D base, E newProp);

    /**
     *
     * @param <P>
     * @param <E>
     * @param <D>
     * @param base
     * @param newSentence
     *
     * @return {@code base}, modified to represent {@code base | newSentence}
     */
    public <P, E, D extends NormalForm<P, E, D>> D or(D base, D newSentence);

    /**
     * Implementation of {@link FormRules} for disjunctive normal form.
     */
    public static FormRules DISJUNCTIVE = new FormRules() {

        @Override
        public <P, E, D extends NormalForm<P, E, D>> boolean isFalse(NormalForm<P, E, D> base) {
            //In DNF, FALSE contains no phrases
            return base.isEmpty();
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> boolean isTrue(NormalForm<P, E, D> base) {
            //In DNF, TRUE contains only a single empty phrase
            return base.getNumPhrases() == 1 && base.numPropsEquals(0);
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D getFalse(D base) {
            base.checkModifiability();
            //In DNF, FALSE contains no phrases
            base.data.clear();
            return base;
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D getTrue(D base) {
            base.checkModifiability();
            //In DNF, TRUE contains only a single empty phrase
            base.data.clear();
            base.data.add(base.createSingleton(null));
            return base;
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D and(D base, E newProp) {
            base.checkModifiability();
            //RULE: ( A | B | ... ) & ( X ) -> ( A&X | B&X | ... )
            //outer operator is OR so append newProp to each phrase
            base.appendElemToEachPhrase(newProp);
            return base;
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D and(D base, D newSentence) {
            base.checkModifiability();
            //outer operator is OR so each phrase of this must be joined
            //  with each phrase of newSentence
            base.cross(newSentence);
            return base;
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D or(D base, E newProp) {
            base.checkModifiability();
            //RULE: ( A | ... ) | ( X ) -> ( A | ... | X )
            //outer operator is OR so just create a new phrase and add it
            base.addSingletonPhrase(newProp);
            return base;
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D or(D base, D newSentence) {
            base.checkModifiability();
            //outer operator is OR so just add all phrases in the other to this
            base.merge(newSentence);
            return base;
        }
    };

    /**
     * Implementation of {@link FormRules} for conjunctive normal form.
     */
    public static FormRules CONJUNCTIVE = new FormRules() {

        @Override
        public <P, E, D extends NormalForm<P, E, D>> boolean isFalse(NormalForm<P, E, D> base) {
            //In CNF, FALSE contains only a single empty phrase
            return base.getNumPhrases() == 1 && base.numPropsEquals(0);
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> boolean isTrue(NormalForm<P, E, D> base) {
            //In CNF, TRUE contains no phrases
            return base.isEmpty();
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D getFalse(D base) {
            base.checkModifiability();
            //In CNF, FALSE contains only a single empty phrase
            base.data.clear();
            base.data.add(base.createSingleton(null));
            return base;
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D getTrue(D base) {
            base.checkModifiability();
            //In CNF, TRUE contains no phrases
            base.data.clear();
            return base;
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D and(D base, E newProp) {
            base.checkModifiability();
            //RULE: ( A & ... ) & ( X ) -> ( A & ... & X )
            //outer operator is AND so just create a new phrase and add it
            base.addSingletonPhrase(newProp);
            return base;
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D and(D base, D newSentence) {
            base.checkModifiability();
            //outer operator is AND so just add all phrases in the other to this
            base.merge(newSentence);
            return base;
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D or(D base, E newProp) {
            base.checkModifiability();
            //RULE: ( A & B & ... ) | ( X ) -> ( A|X & B|X & ... ) 
            //outer operator is AND so append newProp to each phrase
            base.appendElemToEachPhrase(newProp);
            return base;
        }

        @Override
        public <P, E, D extends NormalForm<P, E, D>> D or(D base, D newSentence) {
            base.checkModifiability();
            //outer operator is AND so each phrase of this must be joined
            //  with each phrase of newSentence
            base.cross(newSentence);
            return base;
        }
    };
}

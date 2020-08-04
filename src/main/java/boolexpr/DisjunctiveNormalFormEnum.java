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

import java.util.EnumSet;

/**
 * Disjunctive Normal Form is a boolean formula which is an OR of ANDs.
 *
 * @author Timothy Hoffman
 * 
 * @param <P> type of the propositions
 */
public class DisjunctiveNormalFormEnum<P extends Enum<P>> extends NormalFormEnum<P, DisjunctiveNormalFormEnum<P>> {

    private static final FormRules RULES = FormRules.DISJUNCTIVE;

    /**
     * Creates a {@link DisjunctiveNormalFormEnum} with a single phrase.
     *
     * NOTE: If the given {@link EnumSet} is empty, this is equivalent to
     * {@link #getTrue()}. If {@code null}, then it's equivalent to
     * {@link #getFalse()}.
     *
     * @param firstPhrase
     */
    protected DisjunctiveNormalFormEnum(EnumSet<P> firstPhrase) {
        super(RULES, firstPhrase);
    }

    /**
     * Creates a {@link DisjunctiveNormalFormEnum} with a single phrase
     * containing a single proposition.
     *
     * @param firstProp
     *
     * @throws NullPointerException if {@code firstProp} is {@code null}
     */
    public DisjunctiveNormalFormEnum(P firstProp) {
        super(RULES, firstProp);
    }

    /**
     * Creates a {@link DisjunctiveNormalFormEnum} with no phrases. This is
     * equivalent to {@link #getFalse()}.
     *
     * @param elementType
     */
    public DisjunctiveNormalFormEnum(Class<P> elementType) {
        super(RULES, elementType);
    }

    /**
     * Create a new {@link DisjunctiveNormalFormEnum} by performing a deep copy
     * of an existing {@link DisjunctiveNormalFormEnum}.
     *
     * @param original     the {@link DisjunctiveNormalFormEnum} to duplicate
     * @param unmodifiable whether or not the new instance should be marked as
     *                     unmodifiable/immutable
     */
    public DisjunctiveNormalFormEnum(DisjunctiveNormalFormEnum<P> original, boolean unmodifiable) {
        super(original, unmodifiable);
    }

    /**
     * Copy constructor, performs a deep copy of the given
     * {@link DisjunctiveNormalFormEnum}.
     *
     * NOTE: the resulting {@link DisjunctiveNormalFormEnum} is modifiable.
     *
     * @param original the {@link DisjunctiveNormalFormEnum} to duplicate
     */
    protected DisjunctiveNormalFormEnum(DisjunctiveNormalFormEnum<P> original) {
        this(original, false);
    }

    @Override
    public DisjunctiveNormalFormEnum<P> clone(boolean unmodifiable) {
        return new DisjunctiveNormalFormEnum<>(this, unmodifiable);
    }

    @Override
    protected DisjunctiveNormalFormEnum<P> create(EnumSet<P> firstPhrase) {
        if (firstPhrase == null) {
            //The phrase is 'null' when attempting to create an empty set.
            //However, that will cause an NPE in the NormalFormEnum constructor.
            //Instead, use the Class constructor with the enum type from @this.
            return new DisjunctiveNormalFormEnum<>(this.enumType);
        } else {
            return new DisjunctiveNormalFormEnum<>(firstPhrase);
        }
    }

    public static <P extends Enum<P>> DisjunctiveNormalFormEnum<P> getFalse(Class<P> elementType) {
        return new DisjunctiveNormalFormEnum<>(elementType);
    }

    public static <P extends Enum<P>> DisjunctiveNormalFormEnum<P> getTrue(Class<P> elementType) {
        return new DisjunctiveNormalFormEnum<>(EnumSet.noneOf(elementType));
    }

    /**
     * Returns a new {@link DisjunctiveNormalFormEnum} that is the result of
     * performing the "and" operation on the two inputs.
     *
     * @param <P> type of the propositions
     * @param in1
     * @param in2
     *
     * @return
     */
    public static <P extends Enum<P>> DisjunctiveNormalFormEnum<P> and(DisjunctiveNormalFormEnum<P> in1, DisjunctiveNormalFormEnum<P> in2) {
        return new DisjunctiveNormalFormEnum<>(in1).and(in2);
    }

    /**
     * Returns a new {@link DisjunctiveNormalFormEnum} that is the result of
     * performing the "and" operation on the two inputs.
     *
     * @param <P> type of the propositions
     * @param in1
     * @param in2
     *
     * @return
     */
    public static <P extends Enum<P>> DisjunctiveNormalFormEnum<P> and(P in1, P in2) {
        return new DisjunctiveNormalFormEnum<>(in1).and(in2);
    }

    /**
     * Returns a new {@link DisjunctiveNormalFormEnum} that is the result of
     * performing the OR operation on the two inputs.
     *
     * @param <P> type of the propositions
     * @param in1
     * @param in2
     *
     * @return
     */
    public static <P extends Enum<P>> DisjunctiveNormalFormEnum<P> or(DisjunctiveNormalFormEnum<P> in1, DisjunctiveNormalFormEnum<P> in2) {
        return new DisjunctiveNormalFormEnum<>(in1).or(in2);
    }

    /**
     * Returns a new {@link DisjunctiveNormalFormEnum} that is the result of
     * performing the OR operation on the two inputs.
     *
     * @param <P> type of the propositions
     * @param in1
     * @param in2
     *
     * @return
     */
    public static <P extends Enum<P>> DisjunctiveNormalFormEnum<P> or(P in1, P in2) {
        return new DisjunctiveNormalFormEnum<>(in1).or(in2);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Builds a formatted {@link String} representation of {@code this} with the
     * option to sort the items in the {@link String} returned.
     *
     * @param sorted
     *
     * @return
     */
    public String toString(boolean sorted) {
        return "<" + super.toString(PrintingConnectives.DISJUNCTIVE_STD, sorted) + ">";
    }

    /**
     * Parses a {@link DisjunctiveNormalFormEnum} from the given string (in the
     * format generated by {@link #toString(boolean)}.
     *
     * @param <P>
     * @param elementType
     * @param inputStr
     *
     * @return
     */
    public static <P extends Enum<P>> DisjunctiveNormalFormEnum<P> fromString(Class<P> elementType, String inputStr) {
        //Peel off < and > from the beginning and end (resp.)
        if (inputStr.startsWith("<")) {
            inputStr = inputStr.substring(1);
        }
        if (inputStr.endsWith(">")) {
            inputStr = inputStr.substring(0, inputStr.length() - 1);
        }
        //Use the superclass implementation
        DisjunctiveNormalFormEnum<P> retVal = new DisjunctiveNormalFormEnum<>(elementType);
        retVal.fromString(inputStr, PrintingConnectives.DISJUNCTIVE_STD);
        return retVal;
    }

    /**
     * Builds a CSV formatted {@link String} representation of {@code this} with
     * the option to sort the items in the {@link String} returned.
     *
     * @param sorted
     *
     * @return
     */
    public String toCSVstring(boolean sorted) {
        return super.toString(PrintingConnectives.DISJUNCTIVE_CSV, sorted);
    }

    /**
     * Parses a {@link DisjunctiveNormalFormEnum} from the given string (in the
     * format generated by {@link #toCSVstring(boolean)}.
     *
     * @param <P>
     * @param elementType
     * @param inputStr
     *
     * @return
     */
    public static <P extends Enum<P>> DisjunctiveNormalFormEnum<P> fromCSVString(Class<P> elementType, String inputStr) {
        //Use the superclass implementation
        DisjunctiveNormalFormEnum<P> retVal = new DisjunctiveNormalFormEnum<>(elementType);
        retVal.fromString(inputStr, PrintingConnectives.DISJUNCTIVE_CSV);
        return retVal;
    }
}

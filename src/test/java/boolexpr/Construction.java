package boolexpr;

import boolexpr.util.SparseBitSet;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import boolexpr.test.BLOCK;

/**
 *
 * @author Timothy Hoffman
 * @param <P>
 * @param <E>
 * @param <D>
 */
public interface Construction<P, E, D extends NormalForm<P, E, D>> {

    public D newFromEmpty();

    public D newFromElem(E firstProp);

    public D newFromPhrase(P firstPhrase);

    public D newFromClone(D existing, boolean unmodifiable);

    public D staticGetFalse();

    public D staticGetTrue();

    public boolean isDisjunctive();

    public E getElemFor(int value);

    //Note: we use int to shortcut the need for repeated use of getElemFor()
    public P buildPhrase(int... values);

    public P buildPhrase(E[] values);

    public D buildSentence(List<P> phrases);

    //Note: we use int to shortcut the need for repeated use of getElemFor()
    public D staticOr(int src1, int src2);

    public D staticOr(D src1, D src2);

    //Note: we use int to shortcut the need for repeated use of getElemFor()
    public D staticAnd(int src1, int src2);

    public D staticAnd(D src1, D src2);

    /**
     *
     */
    public final Construction<SparseBitSet, Integer, DisjunctiveNormalFormInt> DNF_INT
            = new Construction<SparseBitSet, Integer, DisjunctiveNormalFormInt>() {

        @Override
        public DisjunctiveNormalFormInt newFromEmpty() {
            return new DisjunctiveNormalFormInt();
        }

        @Override
        public DisjunctiveNormalFormInt newFromElem(Integer firstProp) {
            return new DisjunctiveNormalFormInt(firstProp);
        }

        @Override
        public DisjunctiveNormalFormInt newFromPhrase(SparseBitSet firstPhrase) {
            return new DisjunctiveNormalFormInt(firstPhrase);
        }

        @Override
        public DisjunctiveNormalFormInt newFromClone(DisjunctiveNormalFormInt existing, boolean unmodifiable) {
            return new DisjunctiveNormalFormInt(existing, unmodifiable);
        }

        @Override
        public DisjunctiveNormalFormInt staticGetFalse() {
            return DisjunctiveNormalFormInt.getFalse();
        }

        @Override
        public DisjunctiveNormalFormInt staticGetTrue() {
            return DisjunctiveNormalFormInt.getTrue();
        }

        @Override
        public boolean isDisjunctive() {
            return true;
        }

        @Override
        public Integer getElemFor(int value) {
            return value;
        }

        @Override
        public SparseBitSet buildPhrase(int... values) {
            SparseBitSet phrase = new SparseBitSet();
            for (int x : values) {
                phrase.set(x);
            }
            return phrase;
        }

        @Override
        public SparseBitSet buildPhrase(Integer[] values) {
            SparseBitSet phrase = new SparseBitSet();
            for (int x : values) {
                phrase.set(x);
            }
            return phrase;
        }

        @Override
        public DisjunctiveNormalFormInt buildSentence(List<SparseBitSet> phrases) {
            DisjunctiveNormalFormInt retVal = new DisjunctiveNormalFormInt();
            for (SparseBitSet x : phrases) {
                retVal.merge(new DisjunctiveNormalFormInt(x));
            }
            return retVal;
        }

        @Override
        public DisjunctiveNormalFormInt staticOr(int src1, int src2) {
            return DisjunctiveNormalFormInt.or(src1, src2);
        }

        @Override
        public DisjunctiveNormalFormInt staticOr(DisjunctiveNormalFormInt src1, DisjunctiveNormalFormInt src2) {
            return DisjunctiveNormalFormInt.or(src1, src2);
        }

        @Override
        public DisjunctiveNormalFormInt staticAnd(int src1, int src2) {
            return DisjunctiveNormalFormInt.and(src1, src2);
        }

        @Override
        public DisjunctiveNormalFormInt staticAnd(DisjunctiveNormalFormInt src1, DisjunctiveNormalFormInt src2) {
            return DisjunctiveNormalFormInt.and(src1, src2);
        }
    };

    /**
     *
     */
    public final Construction<EnumSet<BLOCK>, BLOCK, DisjunctiveNormalFormEnum<BLOCK>> DNF_ENUM
            = new Construction<EnumSet<BLOCK>, BLOCK, DisjunctiveNormalFormEnum<BLOCK>>() {

        @Override
        public DisjunctiveNormalFormEnum<BLOCK> newFromEmpty() {
            return new DisjunctiveNormalFormEnum<>(BLOCK.class);
        }

        @Override
        public DisjunctiveNormalFormEnum<BLOCK> newFromElem(BLOCK firstProp) {
            return new DisjunctiveNormalFormEnum<>(firstProp);
        }

        @Override
        public DisjunctiveNormalFormEnum<BLOCK> newFromPhrase(EnumSet<BLOCK> firstPhrase) {
            return new DisjunctiveNormalFormEnum<>(firstPhrase);
        }

        @Override
        public DisjunctiveNormalFormEnum<BLOCK> newFromClone(DisjunctiveNormalFormEnum<BLOCK> existing, boolean unmodifiable) {
            return new DisjunctiveNormalFormEnum<>(existing, unmodifiable);
        }

        @Override
        public DisjunctiveNormalFormEnum<BLOCK> staticGetFalse() {
            return DisjunctiveNormalFormEnum.getFalse(BLOCK.class);
        }

        @Override
        public DisjunctiveNormalFormEnum<BLOCK> staticGetTrue() {
            return DisjunctiveNormalFormEnum.getTrue(BLOCK.class);
        }

        @Override
        public boolean isDisjunctive() {
            return true;
        }

        @Override
        public BLOCK getElemFor(int value) {
            return BLOCK.values()[value];
        }

        @Override
        public EnumSet<BLOCK> buildPhrase(int... values) {
            EnumSet<BLOCK> phrase = EnumSet.noneOf(BLOCK.class);
            for (int x : values) {
                phrase.add(getElemFor(x));
            }
            return phrase;
        }

        @Override
        public EnumSet<BLOCK> buildPhrase(BLOCK[] values) {
            EnumSet<BLOCK> phrase = EnumSet.noneOf(BLOCK.class);
            phrase.addAll(Arrays.asList(values));
            return phrase;
        }

        @Override
        public DisjunctiveNormalFormEnum<BLOCK> buildSentence(List<EnumSet<BLOCK>> phrases) {
            DisjunctiveNormalFormEnum<BLOCK> retVal = new DisjunctiveNormalFormEnum<>(BLOCK.class);
            for (EnumSet<BLOCK> x : phrases) {
                retVal.merge(new DisjunctiveNormalFormEnum<>(x));
            }
            return retVal;
        }

        @Override
        public DisjunctiveNormalFormEnum<BLOCK> staticOr(int src1, int src2) {
            return DisjunctiveNormalFormEnum.or(getElemFor(src1), getElemFor(src2));
        }

        @Override
        public DisjunctiveNormalFormEnum<BLOCK> staticOr(DisjunctiveNormalFormEnum<BLOCK> src1, DisjunctiveNormalFormEnum<BLOCK> src2) {
            return DisjunctiveNormalFormEnum.or(src1, src2);
        }

        @Override
        public DisjunctiveNormalFormEnum<BLOCK> staticAnd(int src1, int src2) {
            return DisjunctiveNormalFormEnum.and(getElemFor(src1), getElemFor(src2));
        }

        @Override
        public DisjunctiveNormalFormEnum<BLOCK> staticAnd(DisjunctiveNormalFormEnum<BLOCK> src1, DisjunctiveNormalFormEnum<BLOCK> src2) {
            return DisjunctiveNormalFormEnum.and(src1, src2);
        }
    };

    /**
     *
     */
    public final Construction<SparseBitSet, Integer, ConjunctiveNormalFormInt> CNF_INT
            = new Construction<SparseBitSet, Integer, ConjunctiveNormalFormInt>() {

        @Override
        public ConjunctiveNormalFormInt newFromEmpty() {
            return new ConjunctiveNormalFormInt();
        }

        @Override
        public ConjunctiveNormalFormInt newFromElem(Integer firstProp) {
            return new ConjunctiveNormalFormInt(firstProp);
        }

        @Override
        public ConjunctiveNormalFormInt newFromPhrase(SparseBitSet firstPhrase) {
            return new ConjunctiveNormalFormInt(firstPhrase);
        }

        @Override
        public ConjunctiveNormalFormInt newFromClone(ConjunctiveNormalFormInt existing, boolean unmodifiable) {
            return new ConjunctiveNormalFormInt(existing, unmodifiable);
        }

        @Override
        public ConjunctiveNormalFormInt staticGetFalse() {
            return ConjunctiveNormalFormInt.getFalse();
        }

        @Override
        public ConjunctiveNormalFormInt staticGetTrue() {
            return ConjunctiveNormalFormInt.getTrue();
        }

        @Override
        public boolean isDisjunctive() {
            return false;
        }

        @Override
        public Integer getElemFor(int value) {
            return value;
        }

        @Override
        public SparseBitSet buildPhrase(int... values) {
            SparseBitSet phrase = new SparseBitSet();
            for (int x : values) {
                phrase.set(x);
            }
            return phrase;
        }

        @Override
        public SparseBitSet buildPhrase(Integer[] values) {
            SparseBitSet phrase = new SparseBitSet();
            for (int x : values) {
                phrase.set(x);
            }
            return phrase;
        }

        @Override
        public ConjunctiveNormalFormInt buildSentence(List<SparseBitSet> phrases) {
            ConjunctiveNormalFormInt retVal = new ConjunctiveNormalFormInt();
            for (SparseBitSet x : phrases) {
                retVal.merge(new ConjunctiveNormalFormInt(x));
            }
            return retVal;
        }

        @Override
        public ConjunctiveNormalFormInt staticOr(int src1, int src2) {
            return ConjunctiveNormalFormInt.or(src1, src2);
        }

        @Override
        public ConjunctiveNormalFormInt staticOr(ConjunctiveNormalFormInt src1, ConjunctiveNormalFormInt src2) {
            return ConjunctiveNormalFormInt.or(src1, src2);
        }

        @Override
        public ConjunctiveNormalFormInt staticAnd(int src1, int src2) {
            return ConjunctiveNormalFormInt.and(src1, src2);
        }

        @Override
        public ConjunctiveNormalFormInt staticAnd(ConjunctiveNormalFormInt src1, ConjunctiveNormalFormInt src2) {
            return ConjunctiveNormalFormInt.and(src1, src2);
        }
    };
}

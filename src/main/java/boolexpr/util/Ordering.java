package boolexpr.util;

import java.util.*;

/**
 * Provides ordering utility methods.
 *
 * @author Timothy Hoffman
 */
public class Ordering {

    /**
     * Comparator for {@link Set Sets} based first on size then on contents.
     *
     */
    public static final Comparator<Set<? extends Comparable>> SET_COMPARATOR = new Comparator<Set<? extends Comparable>>() {

        @Override
        @SuppressWarnings("unchecked")
        public int compare(Set<? extends Comparable> o1, Set<? extends Comparable> o2) {
            //first, check for equality
            if (Objects.equals(o1, o2)) {
                return 0;
            }

            //next, compare the size of the sets
            int cmp = Integer.compare(o1.size(), o2.size());
            if (cmp != 0) {
                return cmp;
            }

            assert o1.size() == o2.size();

            //if the size is the same (and non-empty), compare the lists after
            //  ordering and return the first non-zero comparison between elements.
            //TODO: there may be a more efficient way to get a total ordering
            List<? extends Comparable> l1 = Ordering.order(o1);
            List<? extends Comparable> l2 = Ordering.order(o2);
            for (int i = 0; i < l1.size(); i++) {
                cmp = l1.get(i).compareTo(l2.get(i));
                if (cmp != 0) {
                    return cmp;
                }
            }

            //if everything was the same, return 0
            return 0;
        }
    };

    /**
     * Comparator for {@link SparseBitSet SparseBitSets} based first on size
     * then on contents.
     */
    @SuppressWarnings("unchecked")
    public static final Comparator<SparseBitSet> BITSET_COMPARATOR = new Comparator<SparseBitSet>() {

        @Override
        public int compare(SparseBitSet o1, SparseBitSet o2) {
            //first, check for equality
            if (Objects.equals(o1, o2)) {
                return 0;
            }

            //next, compare the number of true bits
            int cmp = Integer.compare(o1.cardinality(), o2.cardinality());
            if (cmp != 0) {
                return cmp;
            }

            //if the number of true bits is the same (and non-empty), compare the
            //  set bits, returning result for the first pair that is not equivalent.
            for (int i1 = o1.minSetBit(), i2 = o2.minSetBit(); i1 >= 0 && i2 >= 0; i1 = o1.nextSetBit(i1 + 1), i2 = o2.nextSetBit(i2 + 1)) {
                cmp = Integer.compare(i1, i2);
                if (cmp != 0) {
                    return cmp;
                }
            }

            //if everything was the same, return 0
            return 0;
        }
    };

    /**
     * Order a {@link Collection} of {@link Comparable Comparables}.
     *
     * @param <T>
     * @param input
     *
     * @return
     */
    public static <T extends Comparable<? super T>> List<T> order(Collection<T> input) {
        List<T> retVal = new ArrayList<>(input);
        Collections.sort(retVal);
        return retVal;
    }

    /**
     * Order a {@link Collection} of objects according to the given
     * {@link Comparator}.
     *
     * @param <T>
     * @param input
     * @param comparator
     *
     * @return
     */
    public static <T> List<T> order(Collection<T> input, Comparator<? super T> comparator) {
        List<T> retVal = new ArrayList<>(input);
        Collections.sort(retVal, comparator);
        return retVal;
    }

    /**
     * Order a {@link Collection} of {@link EnumSet EnumSets} using
     * {@link #SET_COMPARATOR}.
     *
     * @param <T>
     * @param input
     *
     * @return
     */
    public static <T extends Enum<T>> List<EnumSet<T>> orderEnums(Collection<EnumSet<T>> input) {
        return order(input, SET_COMPARATOR);
    }

    /**
     * Order a {@link Collection} of {@link SparseBitSet SparseBitSets} using
     * {@link #BITSET_COMPARATOR}.
     *
     * @param input
     *
     * @return
     */
    public static List<SparseBitSet> orderBitSets(Collection<SparseBitSet> input) {
        List<SparseBitSet> retVal = order(input, BITSET_COMPARATOR);
        return retVal;
    }

    private Ordering() {
    }
}

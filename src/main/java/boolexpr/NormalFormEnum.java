package boolexpr;

import boolexpr.util.Ordering;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Implementation of {@link NormalForm} where propositions have type
 * {@link Enum} and are stored in an {@link EnumSet} for efficient storage.
 *
 * @author Timothy Hoffman
 * @param <P>
 */
/*package*/ abstract class NormalFormEnum<P extends Enum<P>, ConcreteType extends NormalFormEnum<P, ConcreteType>> extends NormalForm<EnumSet<P>, P, ConcreteType> {

    /**
     * The declaring class type of the {@link Enum} {@code P}.
     */
    protected final Class<P> enumType;

    /**
     * Creates a {@link NormalFormEnum} with a single phrase.
     *
     * @param firstPhrase
     *
     * @throws NullPointerException if {@code firstPhrase} is {@code null}
     */
    protected NormalFormEnum(FormRules formRules, EnumSet<P> firstPhrase) {
        super(formRules, firstPhrase);
        this.enumType = getElementType(firstPhrase);
    }

    /**
     * Creates a {@link NormalFormEnum} with a single phrase containing a single
     * proposition.
     *
     * @param firstProp
     *
     * @throws NullPointerException if {@code firstProp} is {@code null}
     */
    protected NormalFormEnum(FormRules formRules, P firstProp) {
        super(formRules, EnumSet.of(firstProp));
        this.enumType = getElementType(firstProp);
    }

    /**
     * Creates a {@link NormalFormEnum} with no phrases.
     */
    protected NormalFormEnum(FormRules formRules, Class<P> elementType) {
        super(formRules);
        this.enumType = elementType;
    }

    /**
     * Copy constructor, performs a deep copy of the given
     * {@link NormalFormEnum}.
     *
     * @param original     the {@link NormalFormEnum} to duplicate
     * @param unmodifiable whether or not the new instance should be marked as
     *                     unmodifiable/immutable
     */
    protected NormalFormEnum(ConcreteType original, boolean unmodifiable) {
        super(original, unmodifiable);
        this.enumType = original.enumType;
    }

    @Override
    protected final EnumSet<P> clone(EnumSet<P> orig) {
        return orig.clone();
    }

    @Override
    protected int size(EnumSet<P> set) {
        return set.size();
    }

    @Override
    protected boolean isEmpty(EnumSet<P> set) {
        return set.isEmpty();
    }

    @Override
    protected boolean containsAll(EnumSet<P> s1, EnumSet<P> s2) {
        return s1.containsAll(s2);
    }

    @Override
    protected boolean containsAny(EnumSet<P> s1, EnumSet<P> s2) {
        for (P e : s2) {
            if (s1.contains(e)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean contains(EnumSet<P> s, P e) {
        return s.contains(e);
    }

    /**
     *
     * @param singleProp
     *
     * @return a new {@link EnumSet} containing only the given element, or no
     *         elements if the given element is null.
     */
    @Override
    protected EnumSet<P> createSingleton(P singleProp) {
        if (singleProp == null) {
            return EnumSet.noneOf(enumType);
        } else {
            return EnumSet.of(singleProp);
        }
    }

    @Override
    protected void add(EnumSet<P> set, P newItem) {
        if (newItem != null) {
            set.add(newItem);
        }
    }

    @Override
    protected void remove(EnumSet<P> set, P item) {
        if (item == null) {
            //NOTE: the null item should never be added to the set
            assert !set.contains(null);
        } else {
            set.remove(item);
        }
    }

    @Override
    protected void addAll(EnumSet<P> base, EnumSet<P> toAdd) {
        base.addAll(toAdd);
    }

    @Override
    protected void collectToSet(EnumSet<P> src, HashSet<P> dst) {
        dst.addAll(src);
    }

    @Override
    protected Iterable<EnumSet<P>> order(Collection<EnumSet<P>> phrases) {
        return Ordering.orderEnums(phrases);
    }

    @Override
    protected Iterator<P> iterator(EnumSet<P> phrase, boolean ordered) {
        return (ordered ? Ordering.order(phrase) : phrase).iterator();
    }

    @Override
    protected P minElem(EnumSet<P> phrase) {
        //NOTE: EnumSet iterator returns elements in "natural ordering"
        //  (i.e. order they are declared).
        Iterator<P> itr = phrase.iterator();
        return itr.hasNext() ? itr.next() : null;
    }

    @Override
    public P parseElement(String s) {
        return Enum.valueOf(enumType, s);
    }

    protected static <P extends Enum<P>> Class<P> getElementType(EnumSet<P> enumSet) {
        if (enumSet.isEmpty()) {
            enumSet = EnumSet.complementOf(enumSet);
        }
        return enumSet.iterator().next().getDeclaringClass();
    }

    protected static <P extends Enum<P>> Class<P> getElementType(P enumValue) {
        return enumValue.getDeclaringClass();
    }
}

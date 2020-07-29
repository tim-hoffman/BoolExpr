package boolexpr;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Conjunctive or disjunctive normal form of a boolean expression with methods
 * to modify and simply the expression.
 *
 * Method to add a phrase is implemented with set absorption law (see:
 * http://en.wikipedia.org/wiki/Absorption_law)
 *
 * @author Timothy Hoffman
 *
 * @param <PhraseType>   assumed to be mutable so clones are made as necessary
 * @param <ElemType>     assumed to be immutable
 * @param <ConcreteType> type of concrete implementation of {@link NormalForm}
 */
public abstract class NormalForm<PhraseType, ElemType, ConcreteType extends NormalForm<PhraseType, ElemType, ConcreteType>> {

    /**
     * Flag to enable debugging.
     *
     * NOTE: it can cause a significant slowdown.
     */
    private static final boolean ENABLE_DEBUG_ASSERTS = false;

    /**
     * Marks if {@code this} is modifiable. If {@code false}, any attempt to
     * modify will throw an {@link UnsupportedOperationException}.
     *
     * NOTE: default to {@code false}
     */
    protected final boolean preventModification;

    /**
     *
     */
    protected final FormRules formRules;

    /**
     * The inner data is stored in an {@link EnumSet} for efficient storage. The
     * outer data is stored in a {@link HashSet} for efficient lookup but
     * whenever the hashed {@link EnumSet} changes, it must be rehashed.
     */
    protected final HashSet<PhraseType> data;

    /**
     * Base constructor. Creates an empty {@link NormalForm}.
     *
     * @param formRules
     * @param unmodifiable whether or not the new instance should be marked as
     *                     unmodifiable/immutable
     */
    private NormalForm(FormRules formRules, boolean unmodifiable) {
        this.preventModification = unmodifiable;
        this.formRules = formRules;
        this.data = new HashSet<>();
    }

    /**
     * Creates a {@link NormalForm} with a single phrase, unless the given
     * {@code firstPhrase} is {@code null}, in which case the created
     * {@link NormalForm} will be empty (i.e. contains 0 phrases).
     *
     * @param formRules
     * @param firstPhrase
     */
    protected NormalForm(FormRules formRules, PhraseType firstPhrase) {
        this(formRules, false);
        //Since {@code this} is empty, just add directly (if non-null)
        if (firstPhrase != null) {
            data.add(firstPhrase);
        }
    }

    /**
     * Creates an empty and modifiable {@link NormalForm}.
     *
     * @param formRules
     */
    protected NormalForm(FormRules formRules) {
        this(formRules, false);
    }

    /**
     * Copy constructor, performs a deep copy of the given {@link NormalForm}.
     *
     * @param original     the {@link NormalForm} to duplicate
     * @param unmodifiable whether or not the new instance should be marked as
     *                     unmodifiable/immutable
     *
     * @throws NullPointerException if {@code original} is {@code null}
     */
    protected NormalForm(ConcreteType original, boolean unmodifiable) {
        this(original.formRules, unmodifiable);

        //NOTE: assert that absorption holds in {@code original}, so there is
        //  no need to use tryAddWithAbsorption
        if (ENABLE_DEBUG_ASSERTS) {
            assert original.satisfiesAbsorptionLaw();
        }

        //Peform a deep copy by cloning the underlying phrases
        for (PhraseType s : original.data) {
            this.data.add(clone(s));
        }
    }

    @SuppressWarnings("unchecked")
    protected final ConcreteType getConcreteThis() {
        return (ConcreteType) this;
    }

    protected final void checkModifiability() {
        if (preventModification) {
            throw new UnsupportedOperationException("Modification is not allowed");
        }
    }

    public final boolean isUnmodifiable() {
        return preventModification;
    }

    /**
     * Returns an unmodifiable copy of {@code this}.
     *
     * @return
     */
    public ConcreteType asUnmodifiable() {
        return clone(true);
    }

    /**
     * @return {@code true} iff {@code this} {@link NormalForm} represents the
     *         literal {@code false}.
     */
    public boolean isFalse() {
        return formRules.isFalse(this);
    }

    /**
     * @return {@code true} iff {@code this} {@link NormalForm} represents the
     *         literal {@code true}.
     */
    public boolean isTrue() {
        return formRules.isTrue(this);
    }

    /**
     *
     * @return true iff this NormalForm contains no phrases (and consequently,
     *         no propositions).
     */
    public final boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * @return a new {@link Set} of {@code PhraseType} containing cloned phrases
     *         from {@code this}
     */
    public Set<PhraseType> getPhrases() {
        //TODO: the best would be if I could just create unmodifiable views of
        //  the phrases and they I wouldn't even need these 2 methods!
        Set<PhraseType> retVal = new HashSet<>();
        for (PhraseType e : this.data) {
            retVal.add(clone(e));
        }
        return retVal;
    }

    /**
     * WARNING: using this method is potentially unsafe because the phrases
     * returned are the exact phrases used in {@code this} thus modification of
     * any of phrase would directly modify {@code this}!
     *
     * NOTE: the {@link Iterator} returned does not support removal
     *
     * @return an {@link Iterator} over the phrases in {@code this}
     */
    public Iterator<PhraseType> getUnsafePhraseIterator() {
        return new Iterator<PhraseType>() {
            final Iterator<PhraseType> i = NormalForm.this.data.iterator();

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public PhraseType next() {
                return i.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removal is not supported.");
            }
        };
    }

    /**
     * Phrase {@code x} absorbs phrase {@code y} iff {@code y} contains all
     * elements in {@code x} (i.e. {@code x} is a subset of {@code y}). A
     * {@link NormalForm} sentence should not contain both {@code x} and
     * {@code y} if {@code x} absorbs {@code y}, it should only have {@code x}.
     *
     * Generalized set absorption law: a + (a * b) == a * (a + b) == a
     *
     *
     * @param x
     * @param y
     *
     * @return true iff x absorbs y
     */
    //NOTE: not private so that MergeTask does not require synth bridge to call
    boolean absorbs(PhraseType x, PhraseType y) {
        //OBSERVATION: I printed out all calls to this method to discover if
        //  there was opportunity for caching the results. It turns out that
        //  only a very small percent of calls are duplicates.
        //
        //NOTE: I wonder if the size check is actually much slower than just
        //  doing the contains check...
        return /*size(y) < size(x) ? false :*/ containsAll(y, x);
    }

    /**
     * {@code this} absorbs {@code phrase} iff some phrase in {@code this}
     * absorbs {@code phrase}.
     *
     * @param phrase
     *
     * @return true iff {@code this} absorbs {@code phrase}
     */
    protected boolean thisAbsorbsPhrase(PhraseType phrase) {
        for (PhraseType p : this.data) {
            if (absorbs(p, phrase)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code this} absorbs {@code other} iff every phrase in {@code other} is
     * absorbed by some phrase in {@code this}.
     *
     * @param other
     *
     * @return true iff {@code this} absorbs {@code other}
     */
    public boolean absorbs(ConcreteType other) {
        //First try the very fast referential equality test
        //
        //TODO: I could maybe try equals() instead but I wonder if the amount
        //  of times that it's true would justify its cost (because it could
        //  still be somewhat costly on large NormalForm instances but I wonder
        //  how its cost compares to the full cost of absorbs?).
        //
        if (this == other) {
            return true;
        }
        for (PhraseType p : other.data) {
            if (!thisAbsorbsPhrase(p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test method for checking if the absorption law is satisfied.
     *
     * @return
     */
    protected boolean satisfiesAbsorptionLaw() {
        final HashSet<PhraseType> thisDataRef = this.data;
        for (PhraseType a : thisDataRef) {
            for (PhraseType b : thisDataRef) {
                if (a != b) {
                    if (absorbs(a, b) || absorbs(b, a)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Add the new phrase to {@code this} unless there exists a phrase which is
     * a subset of the new phrase. Furthermore, if the new phrase is a subset of
     * an existing phrase, then remove the existing phrase.
     *
     * NOTE: assumes {@code this} can take ownership of {@code newPhrase} (i.e.
     * it is not cloned within this method).
     *
     * @param newPhrase
     */
    protected final void tryAddWithAbsorption(PhraseType newPhrase) {
        checkModifiability();
        if (ENABLE_DEBUG_ASSERTS) {
            assert this.satisfiesAbsorptionLaw();
        }
        for (Iterator<PhraseType> itr = data.iterator(); itr.hasNext();) {
            PhraseType oldPhrase = itr.next();
            if (absorbs(oldPhrase, newPhrase)) {
                //old absorbs new, so the new one should not be added
                //it is enough that just one old phrase absorbs the new phrase
                return;
            } else if (absorbs(newPhrase, oldPhrase)) {
                //new absorbs old so remove the old one
                //new phrase may absorb multiple old phrases so keep searching
                itr.remove();
            }
        }
        //finally, add the new phrase
        data.add(newPhrase);
    }

    /**
     * Adds a new phrase to the outer level containing a single proposition.
     *
     * @param newProp
     */
    protected final void addSingletonPhrase(ElemType newProp) {
        //TODO: maybe for consistancy, have this always throw NPE if newProp is null. Why does adding 'null' make sense to clear it all out anyway?
        tryAddWithAbsorption(createSingleton(newProp));
    }

    /**
     * Adds the given proposition to each phrase in {@code this}. If
     * {@code this} is empty or if {@code newProp} is {@code null}, nothing
     * happens.
     *
     * @param newProp
     */
    protected final void appendElemToEachPhrase(ElemType newProp) {
        checkModifiability();
        if (newProp != null && !this.data.isEmpty()) {//short-circuit
            if (ENABLE_DEBUG_ASSERTS) {
                assert this.satisfiesAbsorptionLaw();
            }
            appendElemToEachPhrase_internal(newProp);
            //Make sure it still satisfies absorption after (i.e. the CLAIM holds)
            if (ENABLE_DEBUG_ASSERTS) {
                assert this.satisfiesAbsorptionLaw();
            }
        }
    }

    /**
     * Implementation of {@link #appendElemToEachPhrase(java.lang.Object)}
     * without all of the wrapping checks.
     *
     * NOTE: This method should only be used when {@code this} is modifiable and
     * non-empty and {@code newProp != null}.
     *
     * @param newProp
     */
    private void appendElemToEachPhrase_internal(ElemType newProp) {
        //NOTE: EACH PHRASE MUST BE REHASHED WHEN MODIFIED
        //NOTE: We can assume that absorption holding before implies that
        //      it will hold after but only in certain cases.
        //      Ex (w/ absorption): data = (a | b)  newProp = a
        //          -> (a | b) & a == (a & a) | (a & b) == a | (a & b) == a
        //
        //CLAIM: the only time absorption could happen is when phrase A
        //  already contains 'newProp' and phrase B does not. Then it is
        //  possible, but not gauranteed, that A' absorbs B'.
        //PROOF: Consider two arbitrary phrases, A and B, from 'this'.
        //  Since they are both in 'this', it follows that neither
        //  absorbs (or, is a subset of) the other. Formally, there
        //  exists some x in A but not in B and some y in B but not A.
        //  We add z to both A and B (giving A' and B' resp.). It is
        //  obvious that if both A and B already contain z, then they
        //  will still not absorb each other. Likewise, if neither A nor
        //  B contain z, then neither A' nor B' will absorb each other.
        //  The final case (w.l.o.g.), is when A contains z and B does
        //  not contain z. Thus A'=A and B'=B/union/z. It follows from 
        //  the definition of y that y cannot be z. Thus, there will
        //  remain y in B' but not in A', hence it is NOT possible for
        //  B' to absorb A'. However, from the definition of x, it is
        //  possible for x to be z and if z is the only element in A
        //  that could be x, then A' will absorb B'.
        //  Example A={2}, B={4}, and z=2. Then A'={2} and B'={2,4}.
        //
        //Approach: keep a list of phrases that might be absorbed but
        //  directly add all phrases that cannot be absorbed. Then scan
        //  the list and remove any that are absorbed. Then add what
        //  remains to 'data'.
        //NOTE: using 2 ArrayLists to avoid removing which is slow
        ArrayList<PhraseType> maybeAbsorbed = new ArrayList<>();
        //create a copy of data and clear data
        ArrayList<PhraseType> thisCopy = new ArrayList<>(this.data);
        this.data.clear();
        //NOTE: it is safe to modify/consume the phrases in 'thisCopy' 
        //  since they have been removed from 'this'.
        for (PhraseType phraseA : thisCopy) {
            if (contains(phraseA, newProp)) {
                //cannot be absorbed so directly add it
                add(phraseA, newProp);
                this.data.add(phraseA);
            } else {
                //might be absorbed so postpone adding
                add(phraseA, newProp);
                maybeAbsorbed.add(phraseA);
            }
        }
        for (Iterator<PhraseType> itr = maybeAbsorbed.iterator(); itr.hasNext();) {
            PhraseType absorbee = itr.next();
            for (PhraseType absorber : this.data) {
                if (absorbs(absorber, absorbee)) {
                    itr.remove();
                    break;//break inner loop
                }
            }
        }
        this.data.addAll(maybeAbsorbed);
    }

    /**
     * Appends the given phrase to each phrase in {@code this}. If {@code this}
     * is empty or if {@code newPhrase} is {@code null}, nothing happens.
     *
     * @param newPhrase
     *
     * @deprecated use {@link #appendPhraseToEachPhrase_2(java.lang.Object)}
     */
    @Deprecated
    protected final void appendPhraseToEachPhrase_1(PhraseType newPhrase) {
        checkModifiability();
        if (newPhrase != null && !this.data.isEmpty()) {//short-circuit
            if (ENABLE_DEBUG_ASSERTS) {
                assert this.satisfiesAbsorptionLaw();
            }
            //The obvious approach is to clone and clear 'this', loop over the
            //  clone, and union each phrase with 'other' and finally add back
            //  to 'this' via tryAddWithAbsorption(..). However, that actually
            //  ends up being much slower than the approach below, mainly due to
            //  excess absorption checks which appendAllPhrases(..) optimizes.
            //This approach instead just appends each element of 'other' in turn.
            for (Iterator<ElemType> itr = iterator(newPhrase, false); itr.hasNext();) {
                appendElemToEachPhrase_internal(itr.next());
            }
            //Make sure it still satisfies absorption after (i.e. the CLAIM holds)
            if (ENABLE_DEBUG_ASSERTS) {
                assert this.satisfiesAbsorptionLaw();
            }
        }
    }

    /**
     * Appends the given phrase to each phrase in {@code this}. If {@code this}
     * is empty or if {@code newPhrase} is {@code null}, nothing happens.
     *
     * @param newPhrase
     */
    protected final void appendPhraseToEachPhrase_2(PhraseType newPhrase) {
        checkModifiability();
        if (newPhrase != null && !this.data.isEmpty()) {//short-circuit
            if (ENABLE_DEBUG_ASSERTS) {
                assert this.satisfiesAbsorptionLaw();
            }
            //NOTE: when phrase is SparseBitSet, size may be costly but that
            //  same cost would be incurred by the iterator below anyway.
            int size = size(newPhrase);
            if (size == 1) {
                appendElemToEachPhrase_internal(minElem(newPhrase));
            } else if (size > 0) {
                //NOTE: EACH PHRASE MUST BE REHASHED WHEN MODIFIED
                //
                //CLAIM: A phrase that fully contains 'newPhrase' cannot be absorbed
                //PROOF: Consider two arbitrary phrases, A and B, from 'this'.
                //  Since they are both in 'this', it follows that neither absorbs
                //  (or, is a subset of) the other. Formally, there exists some x in
                //  A but not in B and some y in B but not A. Assume (w.l.o.g.) that
                //  phrase A fully contains 'newPhrase'. Since A does not contain y,
                //  then 'newPhrase' cannot contain y, thus the union of A and
                //  'newPhrase' will also not contain y. Therefore, A cannot be
                //  absorbed by B (which was arbitrary) since it does not contain y.
                //
                //Approach: Merge and add all phrases that cannot be absorbed.
                // Then, for the remaining phrases, iterate 'newPhrase' and 
                // do something similar to appendElemToEachPhrase_internal(..)
                //NOTE: fully mirroring that method is actually really slow
                // because it requires additional absorption checks between
                // all pairs in 'couldBeAbsorbed'.
                //NOTE: using 2 ArrayLists to avoid removing which is slow
                ArrayList<PhraseType> couldBeAbsorbed = new ArrayList<>();
                //create a copy of data and clear data
                ArrayList<PhraseType> thisCopy = new ArrayList<>(this.data);
                this.data.clear();
                //NOTE: it is safe to modify/consume the phrases in 'thisCopy' 
                //  and 'maybeAbsorbed' since they have been removed from 'this'.
                for (PhraseType phraseA : thisCopy) {
                    if (containsAll(phraseA, newPhrase)) {
                        //cannot be absorbed so directly add it
                        addAll(phraseA, newPhrase);
                        this.data.add(phraseA);
                    } else {
                        //might be absorbed so postpone adding
                        couldBeAbsorbed.add(phraseA);
                    }
                }
                //Now, iterate the new phrase and add each elem in turn similar
                //  to appendElemToEachPhrase_internal(..)
                for (Iterator<ElemType> npItr = iterator(newPhrase, false); npItr.hasNext();) {
                    ElemType newProp = npItr.next();
                    ArrayList<PhraseType> maybeAbsorbed = new ArrayList<>();
                    //create a copy of data and clear data
                    ArrayList<PhraseType> couldBeAbsorbedCopy = new ArrayList<>(couldBeAbsorbed);
                    couldBeAbsorbed.clear();
                    //NOTE: it is safe to modify/consume the phrases in 'thisCopy' 
                    //  since they have been removed from 'this'.
                    for (PhraseType phraseA : couldBeAbsorbedCopy) {
                        if (contains(phraseA, newProp)) {
                            //cannot be absorbed so directly add it
                            add(phraseA, newProp);
                            couldBeAbsorbed.add(phraseA);
                        } else {
                            //might be absorbed so postpone adding
                            add(phraseA, newProp);
                            maybeAbsorbed.add(phraseA);
                        }
                    }
                    OUTER:
                    for (Iterator<PhraseType> itr = maybeAbsorbed.iterator(); itr.hasNext();) {
                        PhraseType absorbee = itr.next();
                        for (PhraseType absorber : this.data) {
                            if (absorbs(absorber, absorbee)) {
                                itr.remove();
                                continue OUTER;//continue outer loop
                            }
                        }
                        for (PhraseType absorber : couldBeAbsorbed) {
                            if (absorbs(absorber, absorbee)) {
                                itr.remove();
                                continue OUTER;//continue outer loop
                            }
                        }
                    }
                    couldBeAbsorbed.addAll(maybeAbsorbed);
                }
                this.data.addAll(couldBeAbsorbed);
            }
            //Make sure it still satisfies absorption after (i.e. the CLAIM holds)
            if (ENABLE_DEBUG_ASSERTS) {
                assert this.satisfiesAbsorptionLaw();
            }
        }
    }

    public static boolean PRINT_MERGE_STATS = false;//TODO: TEMP: DEBUG

    public String stats(boolean csv) {
        if (csv) {
            return "" + getNumProps() + "," + getAllProps().size() + "," + getNumPhrases();
        } else {
            return "{n=" + getNumProps() + "; u=" + getAllProps().size() + "; p=" + getNumPhrases() + "}";
        }
    }

    //Global static thread pool for use by any method, creates only Daemon
    //  threads and allows 
    protected static final ExecutorService POOL;

    static {
        ThreadFactory threadFactory = new ThreadFactory() {
            //Simplified version of Executors.defaultThreadFactory() that always
            //  creates daemon threads with a more descriptive name.
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            private final ThreadGroup group;

            {
                SecurityManager s = System.getSecurityManager();
                group = (s != null) ? s.getThreadGroup()
                        : Thread.currentThread().getThreadGroup();
            }

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(group, r, "NormalForm-" + threadNumber.getAndIncrement());
                if (!t.isDaemon()) {
                    t.setDaemon(true);
                }
                if (t.getPriority() != Thread.NORM_PRIORITY) {
                    t.setPriority(Thread.NORM_PRIORITY);
                }
                return t;
            }
        };
        int numThreads = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor tempPool
                = new ThreadPoolExecutor(numThreads, numThreads,
                        30L, TimeUnit.SECONDS,//NOTE: if changing this, also consider chaning Helpers
                        new LinkedBlockingQueue<Runnable>(), threadFactory);
        tempPool.allowCoreThreadTimeOut(true);
        POOL = tempPool;
    }

    private class MergeTask implements Runnable {

        final ArrayList<PhraseType> toRemove = new ArrayList<>();
        final int currThreadNum;
        final CountDownLatch latch;
        final ArrayList<PhraseType> otherDataListRef;
        final int totalNumThreads;

        /**
         * @param otherDataListRef
         * @param rangeBegin       inclusive
         * @param rangeEnd         exclusive
         */
        MergeTask(int currThreadNum, CountDownLatch latch, ArrayList<PhraseType> otherDataListRef, int totalNumThreads) {
            this.currThreadNum = currThreadNum;
            this.latch = latch;
            this.otherDataListRef = otherDataListRef;
            this.totalNumThreads = totalNumThreads;
        }

        private int startIdx(int threadNum) {
            //This formula (w/ intentional trucaction in the division) provides
            //  the most equal distribution of tasks among threads.
            return threadNum * otherDataListRef.size() / totalNumThreads;
        }

        @Override
        public void run() {
//            System.out.println("MergeTask starting: " + Thread.currentThread().getName());
            try {
                //Just like the single-threaded version in merge(ConcreteType), iterate
                //  'this.data' and check for absorption but only among the given range.
                //  And instead of directly removing from 'this.data', store in the
                //  'toRemove' list to be removed synchronously at a later time.
                final int startIncl = startIdx(currThreadNum);
                final int endExcl = startIdx(currThreadNum + 1);
                //Maybe make things faster by keeping local refs to this.*
                final NormalForm<PhraseType, ElemType, ConcreteType> _this = NormalForm.this;
                final HashSet<PhraseType> _thisData = _this.data;

                //
                //
                //TODO: is this approach actually faster? I'm not convinced it is.
                //The prior approach had the 'this' loop on the outside, however, 
                //  there was an issue w/ making sure the clone(phrB) happened.
                //UPDATE: actually, I think it's fine. The runtime of callGraphProc
                //  was still around 14min for h2 so I don't think it's worse.
                //
                //
                NEXT_PHRASE:
                for (int i = startIncl; i < endExcl; i++) {
//                System.out.println("Thread " + currThreadNum + " checking " + i);
                    PhraseType phrB = otherDataListRef.get(i);
                    //NOTE: 'phrB' cannot yet be null since it's only traversed once
                    for (PhraseType phrA : _thisData) {
                        //If A is already marked for removal, no need to check again
                        //UPDATE: this check can actually be very slow!
//                    if (!toRemove.contains(phrA)) {
                        if (absorbs(phrB, phrA)) {
                            //Phrase B absorbs phrase A, so mark A for removal
                            toRemove.add(phrA);
                            //It's possible for B to absorb more than one phrase
                            //  from A so continue to loop over the all A.
                            //In fact, this case appears first since absorbs(..)
                            //  check can be time-consuming and it is more
                            //  likely for this case to be hit.
                        } else if (absorbs(phrA, phrB)) {
                            //Phrase A absorbs phrase B, so B should not be added
                            otherDataListRef.set(i, null);
                            //break inner loop to proceed to next B w/o cloning
                            continue NEXT_PHRASE;
                        }
//                    }
                    }
                    //Finally, if B is to be preserved/added, create a clone to
                    //  ensure that two NormalForm instances do not contain the
                    //  same phrase object.
                    otherDataListRef.set(i, _this.clone(phrB));
                }
            } finally {
                //Notify of completion
                //NOTE: in finally block so that the latch will countdown and 
                //  allows the executor to shutdown even when there is an error.
//                System.out.println("MergeTask completed: " + Thread.currentThread().getName());
                latch.countDown();
            }
        }
    }

    /**
     * Add all phrases from {@code other} to {@code this} (applying absorption
     * law as necessary).
     *
     * @param other
     */
    protected final void merge(ConcreteType other) {
        checkModifiability();
        //If {@code this} satisfies absorption, then the final result will
        //  satisify absorption after all data from other is added
        if (ENABLE_DEBUG_ASSERTS) {
            assert this.satisfiesAbsorptionLaw();
            assert other.satisfiesAbsorptionLaw();
        }

        long start = 0;//TODO: TEMP: DEBUG
        String thisStats = null;//TODO: TEMP: DEBUG
        boolean useThreads = false;
        if (PRINT_MERGE_STATS) {//TODO: TEMP: DEBUG
            thisStats = this.stats(false);//TODO: TEMP: DEBUG
            start = System.nanoTime();//TODO: TEMP: DEBUG
        }//TODO: TEMP: DEBUG
        try {//TODO: TEMP: DEBUG
            //NOTE: short-circuit cases arranged in order of (approximate) speed
            //  with the final block being the normal case.
            if (other.isEmpty()) {
                // {A} + {} = {A}        (i.e. Identity law)
                // ACTION: 'this' remains unchanged
            } else if (this.isEmpty()) {
                // {} + {B} = {B}        (i.e. Identity law)
                // ACTION: just add a clone of every phrase in 'other'
                for (PhraseType ph : other.data) {
                    this.data.add(clone(ph));
                }
            } else if (this.getNumPhrases() == 1 && this.numPropsEquals(0)) {
                // {()} + {B} = {()}     (i.e. Annulment law)
                // ACTION: 'this' remains unchanged
            } else if (other.getNumPhrases() == 1 && other.numPropsEquals(0)) {
                // {A} + {()} = {()}     (i.e. Annulment law)
                // ACTION: clear 'this' and add a clone of phrase from 'other'
                this.data.clear();
                this.data.add(clone(other.data.iterator().next()));
            } else if (this.equals(other)) {
                // {A} x {A} = {A}      (i.e. Idempotent law)
                // ACTION: 'this' remains unchanged
            } else {
                //NOTE: the obvious approach for merge is to loop over 'other.data', 
                //  clone each phrase, and add to this via tryAddWithAbsorption(..).
                //  Unfortunately, that is very inefficient b/c it ends up checking for
                //  absorption among all pairs of phrases in 'other' which is unecessary
                //  since it must already satisfy the absorption law. So instead of
                //  that, we manually perform the absorption check here, not checking
                //  pairs of phrases from 'other' and not cloning them unless they are
                //  really added. Furthermore, at a certain threshold, we split the
                //  traversal of 'other' among several threads to speed up processing.
                //  The criteria for when to use multiple threads is based on comparison
                //  of the approximate complexity of the two approaches. Multi-threading
                //  should be used when A*(B/N) + N*N + C - A*B < 0 where A is the count
                //  of phrases is this, resp. B in other, N is the number of threads
                //  and C is a constant. Based on a comparison of the runtime for the
                //  two approaches in several of the DaCapo benchmarks, we let C=3000
                //  and only 0.41% of the datapoints tested are categorized incorrectly.
                //NOTE: The original formula above can have integer overflow so convert:
                //  ==  A * (B / N) + R - (A * B) < 0   let R=(N * N) + 3000
                //  ==  (A * ((B/N) - B)) + R < 0       factor A from the B expressions
                //  ==  R < -(A * ((B/N) - B))          subtract AB expression on both sides
                //  ==  R < A * -((B/N) - B)            move the negative
                //  ==  R < A * (B - (B/N))             distribute the negative
                //  ** This may still overflow. However, since A, B, and N are
                //      positive we can easily handle by cases:
                //      1) A > R : this implies the expression above, check it first
                //      2) A <= R : can divide both sides by A to avoid overflow
                //          i.e. R/A < (B - (B/N))
                //      ** However, we must ensure that this implies the earlier
                //          expression despite the truncation in int division!
                //      Proof:
                //          let X=(B-(B/N)) which must be an integer
                //          floor(R/A) < X implies R/A < X
                //          mult A on both sides: A(R/A) < A(X) 
                //          simplify: R < A * X     QED (original expr above) 
                //  
                //NOTE: ArrayList ends up being more efficient than HashSet
                //  in memory consumption and iterator performance. The only
                //  downside is that removal in ArrayList is not constant time
                //  so instead of removing, we just replace with 'null'.
                ArrayList<PhraseType> otherDataList = new ArrayList<>(other.data);

                final int A = this.getNumPhrases();
                final int B = other.getNumPhrases();//i.e. other.data.size()
                final int N = Runtime.getRuntime().availableProcessors();
                final int R = (N * N) + 3000;
                final boolean USE_THREADS = (A > R) || ((B - (B / N)) > (R / A));
                if (USE_THREADS) {
                    useThreads = true;//TODO: TEMP: DEBUG
                    //MULTI-THREADED APPROACH
                    CountDownLatch latch = new CountDownLatch(N);
                    ArrayList<MergeTask> taskList = new ArrayList<>(N);
                    for (int i = 0; i < N; i++) {
                        MergeTask task = new MergeTask(i, latch, otherDataList, N);
                        taskList.add(task);
                        POOL.execute(task);
                    }
                    // wait for the threads to be done
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.err);
                        throw new RuntimeException(e);
                    }
                    //Perform removals from this.data
                    for (MergeTask task : taskList) {
                        this.data.removeAll(task.toRemove);
                    }
                    //Perform additions to this.data from the pruned 'otherDataList'
                    //  Cloning has already been done w/in the thread.
                    for (PhraseType ph : otherDataList) {
                        if (ph != null) {
                            this.data.add(ph);
                        }
                    }
                } else {
                    //SINGLE-THREADED APPROACH
                    //If phrase A from 'this' absorbs phrase B from 'other', then remove B from 'otherDataList'
                    //If phrase B absorbs phrase A, then remove A
                    //In the end, add a clone of each phrase remaining in B to A
                    final int otherSize = otherDataList.size();//size doesn't change b/c we don't remove
                    for (Iterator<PhraseType> thisItr = this.data.iterator(); thisItr.hasNext();) {
                        PhraseType phrA = thisItr.next();
                        for (int i = 0; i < otherSize; i++) {
                            PhraseType phrB = otherDataList.get(i);
                            if (phrB != null) {
                                if (absorbs(phrA, phrB)) {
                                    //Phrase A absorbs phrase B, so B should not be added
                                    otherDataList.set(i, null);
                                } else if (absorbs(phrB, phrA)) {
                                    //Phrase B absorbs phrase A, so remove A from this
                                    thisItr.remove();
                                    break;//break inner loop to proceed to next A from 'this'
                                }
                            }
                        }
                    }
                    //Perform additions to this.data from the pruned 'otherDataList'
                    //  being sure to clone each phrase before adding so that 'this'
                    //  and 'other' do not contain the same phrase objects.
                    for (PhraseType ph : otherDataList) {
                        if (ph != null) {
                            this.data.add(clone(ph));
                        }
                    }
                }
            }
        } finally {//TODO: TEMP: DEBUG
            if (PRINT_MERGE_STATS) {//TODO: TEMP: DEBUG
                double elapsed = System.nanoTime() - start;//TODO: TEMP: DEBUG
//                Throwable t = new Throwable();
//                System.out.println("[NormalForm#merge] Callee: " + t.getStackTrace()[5]);
//                t.printStackTrace(System.out);
                System.out.println("[NormalForm#merge] " + (elapsed / 1_000_000) + "ms (" + (useThreads ? "multi" : "single")
                        + "-threaded) (t_in=" + thisStats + " o_in=" + other.stats(false) + " out=" + this.stats(false) + ") from " + /*t.getStackTrace()[5]*/ "?");//TODO: TEMP: DEBUG
            }//TODO: TEMP: DEBUG
        }//TODO: TEMP: DEBUG
    }

    /**
     * Performs a cross product between {@code this} and {@code other}.
     *
     * @param other
     */
    protected final void cross(ConcreteType other) {
        checkModifiability();
        //safety check
        if (ENABLE_DEBUG_ASSERTS) {
            assert this.satisfiesAbsorptionLaw();
            assert other.satisfiesAbsorptionLaw();
        }

        //NOTE: short-circuit cases arranged in order of (approximate) speed
        //  with the final block being the normal case.
        if (this.isEmpty()) {
            // {} x {B} = {}        (i.e. Annulment law)
            // ACTION: 'this' remains unchanged
        } else if (other.isEmpty()) {
            // {A} x {} = {}        (i.e. Annulment law)
            // ACTION: just clear 'this'
            this.data.clear();
        } else if (other.getNumPhrases() == 1) {
            // If there is a single phrase in 'other', just let
            //  cross(Phrase) handle it.
            // NOTE: this includes the {A} x {()} = {A} identity case.
            this.cross(other.data.iterator().next());
        } else if (this.getNumPhrases() == 1 && this.numPropsEquals(0)) {
//            String thisStats = this.stats(true);
//            long start = System.nanoTime();//TODO: TEMP: DEBUG

            // {()} x {B} = {B}     (i.e. Identity law)
            // ACTION: just clear 'this' and add a clone of every phrase in 'other'
            // NOTE: this should be faster than the loop below
            this.data.clear();
            for (PhraseType newPhrase : other.data) {
                this.data.add(clone(newPhrase));
            }
//            double elapsed = System.nanoTime() - start;//TODO: TEMP: DEBUG
//            System.out.println("[NormalForm#cross_S1] " + (elapsed / 1_000_000) + ";" + thisStats + ";" + other.stats(true) + ";" + this.stats(true));//TODO: TEMP: DEBUG
        } else if (this.equals(other)) {
            // {A} x {A} = {A}      (i.e. Idempotent law)
            // ACTION: 'this' remains unchanged
        } else {
//            String thisStats = this.stats(true);
//            long start = System.nanoTime();//TODO: TEMP: DEBUG

            //The obvious approach is to clone and clear 'this', loop over the
            //  clone, and loop over 'other' and union each phrase pair of
            //  phrases and finally use tryAddWithAbsorption(..) to add back to
            //  'this'. However, that actually ends up being significantly
            //  slower than the approach below, mainly due to excess absorption
            //  checks which cross(PhraseType) optimizes.
            //
            //NOTE: although it would be fast to just add 'newThis' to data,
            //  the merge with absorption check is necessary. Consider the
            //  example: (7)(3,4) x (2)(5) = (3,4)(3,7)(5,7)
            //
            //TODO: it's possible there could be more room to optimize but this
            //  is significantly better than it was.
            //The runtime of the merge(..) call significantly outweighs
            //  everything else in the tests I ran (13ms vs <0.3ms)
            ConcreteType origThis = this.clone(false);
            this.data.clear();
            for (PhraseType phraseB : other.data) {
                ConcreteType newThis = origThis.clone(false);
                newThis.cross(phraseB);
                this.merge(newThis);
            }
//            double elapsed = System.nanoTime() - start;//TODO: TEMP: DEBUG
//            System.out.println("[NormalForm#cross_S2] " + (elapsed / 1_000_000) + ";" + thisStats + ";" + other.stats(true) + ";" + this.stats(true));//TODO: TEMP: DEBUG
        }
    }

    /**
     * Performs a cross product between {@code this} and {@code other}.
     *
     * @param other
     */
    protected final void cross(PhraseType other) {
        checkModifiability();
        //safety check
        if (ENABLE_DEBUG_ASSERTS) {
            assert this.satisfiesAbsorptionLaw();
        }

        //NOTE: short-circuit cases arranged in order of (approximate) speed
        //  with the final block being the normal case.
        if (this.isEmpty()) {
            // {} x {B} = {}        (i.e. Annulment law)
            // ACTION: 'this' remains unchanged
        } else if (other == null) {
            //NOTE: null phrase is used to create empty NormalForm
            // {A} x {} = {}        (i.e. Annulment law)
            // ACTION: just clear 'this'
            this.data.clear();
        } else if (size(other) == 0) {
            // {A} x {()} = {A}     (i.e. Identity law)
            // ACTION: 'this' remains unchanged
        } else if (this.getNumPhrases() == 1 && this.numPropsEquals(0)) {
            // {()} x {B} = {B}     (i.e. Identity law)
            // ACTION: just clear 'this' and add a clone of 'other'
            // NOTE: this should be faster than the loop below
            this.data.clear();
            this.data.add(clone(other));
        } else if (this.getNumPhrases() == 1 && other.equals(this.data.iterator().next())) {
            // {A} x {A} = {A}      (i.e. Idempotent law)
            // ACTION: 'this' remains unchanged
        } else {
//            String thisStats = this.stats(true);
//            long start = System.nanoTime();//TODO: TEMP: DEBUG

            //NOTE: performance tests show that _2 is ~15 times faster than _1!
            appendPhraseToEachPhrase_2(other);

//            double elapsed = System.nanoTime() - start;//TODO: TEMP: DEBUG
//            System.out.println("[NormalForm#cross_P2] " + (elapsed / 1_000_000) + ";" + thisStats + ";" + size(other) + ";" + this.stats(true));//TODO: TEMP: DEBUG
        }
    }

    /**
     * For every element appearing in {@code this}, if the given {@link Map}
     * contains that element as a key, then replace it with the associated value
     * from the {@link Map}.
     *
     * NOTE: although the same could be achieved via
     * {@link #resolveAll(java.util.Map)}, this method is slightly faster.
     *
     * @param oldToNewValue
     *
     * @return {@code true} iff any modification was made to {@code this}
     */
    public boolean replaceAll(Map<ElemType, ElemType> oldToNewValue) {
        checkModifiability();
        //Handle trivial cases where no replacement can happen
        if (oldToNewValue.isEmpty() || this.data.isEmpty()) {
            return false;
        }
        //Handle some other trivial cases
        if (this.data.size() == 1) {
            PhraseType onlyPhrase = this.data.iterator().next();
            //If 'this' contains just an empty phrase, there's nothing to replace.
            if (isEmpty(onlyPhrase)) {
                return false;
            }
            //If 'this' contains a single proposition, replacement is trivial.
            if (size(onlyPhrase) == 1) {
                ElemType onlyElem = minElem(onlyPhrase);
                ElemType replacement = oldToNewValue.get(onlyElem);
                if (replacement == null) {
                    //In this case, there is no replacement
                    return false;
                } else {
                    //In this case, just remove 'onlyElem' and add 'replacement'
                    this.remove(onlyPhrase, onlyElem);
                    this.add(onlyPhrase, replacement);
                    return true;
                }
            }
        }

//        String original = this.getNumProps() + "," + this.getNumPhrases() + "," + oldToNewValue.size();
//        System.out.println("BEGIN: NormalForm#replaceAll(..) this.size=" + this.getNumProps() + ":" + this.getNumPhrases()
//                + "; repMap.size=" + oldToNewValue.size());
//        long start = System.nanoTime();
        //Update each phrase, replacing all occurrences of a key from the map
        //  with the value. Modified phrases are removed from 'this.data' via
        //  the iterator and their replaced version placed in 'updatedPhrases'
        //  and then added back into 'this' at the end.
        //NOTE: since we're only doing element-for-element replacements, the
        //  number of phrases nor the size of any phrase will ever increase.
        //  Thus, it is safe to store all updated phrases in a list rather than
        //  creating a new ConcreteType to store them which would result in 
        //  many unnecessary/repeated absorption checks.
        ArrayList<PhraseType> updatedPhrases = new ArrayList<>(this.data.size());
        for (Iterator<PhraseType> it = this.data.iterator(); it.hasNext();) {
            PhraseType phrase = it.next();
            //NOTE: In general, we should expect iteration over the 'phrase' to
            //  be slow compared to iteration over the Map (ex: the SparseBitSet
            //  phrase iterator is slow when values are large and/or sparse).
            //  Hence, preference should be given to iteration over the Map when
            //  possible. However, if that map is very large, its iteration
            //  could be the bottleneck so avoid iterating it whenever possible.
            //
            //NOTE: To avoid computing the new phrase size in every iteration
            //  (used to determine when the phrase has only a single element
            //  and the iteration over the Map need not continue), just compute
            //  it once up front and then subtract removals until the element
            //  count reaches 1. Hence, the overhead w/in the loop is only a
            //  decrement and int equality check which is likely faster than any
            //  method call for a size re-computation.
            int elemsInPhrase = size(phrase);
            assert elemsInPhrase > 0;//0 is handled earlier and cannot be <0
            //With this size, we can additionally handle the easy case of size-1
            //  phrases to avoid iterating the Map since we can quickly lookup
            //  the single element.
            if (elemsInPhrase == 1) {
                ElemType onlyElem = minElem(phrase);
                assert onlyElem != null;//sanity check
                ElemType replacement = oldToNewValue.get(onlyElem);
                //When there is no replacement for the single element in the
                //  phrase, nothing happens. However, if there is a replacement,
                //  then remove the phrase, update it, and add to the List.
                if (replacement != null) {
                    it.remove();
                    remove(phrase, onlyElem);
                    add(phrase, replacement);
                    updatedPhrases.add(phrase);
                }
            } else {
                boolean removedPhrase = false;
                //We cannot directly set the replacement values in the existing
                //  phrase because it could cause double replacement if the Map
                //  contains some X appearing as both a key and a value, with
                //  the key occurance being encountered later during traversal.
                //  Hence, we create a new phrase and temporarily add all
                //  replacement values to it. In the end, we add them back into
                //  the reduced original phrase.
                PhraseType newPhrase = createSingleton(null);//use 'null' to create empty set
                //Iterate the Map and, if a key is found in the phrase,
                //  then remove it and add the mapped value to 'newPhrase'.
                assert elemsInPhrase > 1;//sanity check: inner break condition doesn't need to capture 0
                for (Map.Entry<ElemType, ElemType> e : oldToNewValue.entrySet()) {
                    final ElemType key = e.getKey();
                    if (contains(phrase, key)) {
                        //If any key from the map is found in the phrase, we
                        //  remove the phrase from 'data' so that it's union
                        //  with 'newPhrase' can be re-added with absorption.
                        //NOTE: Phrase must be removed before modifying it or
                        //  else HashSet.iterator().remove() will not work!
                        if (!removedPhrase) {
                            removedPhrase = true;
                            it.remove();
                        }
                        //Remove the 'key' from the phrase and decrement the 
                        //  count of elements remaining in the phrase.
                        remove(phrase, key);
                        elemsInPhrase--;

                        //Add the replacement value to the 'newPhrase'
                        add(newPhrase, e.getValue());

                        //After removal of the current key, if the phrase has
                        //  only a single element remaining, handle it trivally
                        //  (similar to before) to avoid any further iteration.
                        if (elemsInPhrase == 1) {
                            ElemType onlyElem = minElem(phrase);
                            assert onlyElem != null;//sanity check
                            ElemType replacement = oldToNewValue.get(onlyElem);
                            if (replacement != null) {
                                assert removedPhrase;//must have been removed above

                                //Remove the final element from the phrase and
                                //  decrement the count of remaining elements.
                                remove(phrase, onlyElem);
                                elemsInPhrase--;

                                //Add the replacement value to the 'newPhrase'
                                add(newPhrase, replacement);
                            }
                            //Either way, break the loop b/c there can be no
                            //  further matches.
                            break;
                        }
                    }
                }
                assert size(phrase) == elemsInPhrase;//sanity check

                //Finally, if there were any replacements, then create and
                //  cache the updated phrase
                if (removedPhrase) {
                    addAll(phrase, newPhrase);
                    updatedPhrases.add(phrase);
                }
            }
        }

        //Finally, add all updated phrases back into 'this' w/ absorption
        for (PhraseType p : updatedPhrases) {
            tryAddWithAbsorption(p);
        }

//        double elapsedMS = ((double) (System.nanoTime() - start) / 1_000_000);
//        System.out.println("END: NormalForm#replaceAll(..) this.size=" + this.getNumProps() + ":" + this.getNumPhrases()
//                + "; repMap.size=" + oldToNewValue.size() + " in " + elapsedMS + "ms");
//        System.out.println("END: NormalForm#replaceAll," + original + "," + this.getNumProps() + "," + this.getNumPhrases() + "," + elapsedMS + "ms");
//        //END: NormalForm#replaceAll, this.props(in), this.phrases(in), map.size, this.props(out), this.phrases(out), time
        return !updatedPhrases.isEmpty();
    }

    /**
     * For every element appearing in {@code this}, if the given {@link Map}
     * contains that element as a key, then replace it with the associated value
     * from the {@link Map}.
     *
     * @param resolutionMap
     *
     * @return {@code true} iff any modification was made to {@code this}
     */
    public boolean resolveAll(Map<ElemType, ConcreteType> resolutionMap) {
        checkModifiability();
        //Handle trivial cases where no resolution can happen
        if (resolutionMap.isEmpty() || this.data.isEmpty()) {
            return false;
        }
        //Handle some other trivial cases
        if (this.data.size() == 1) {
            PhraseType onlyPhrase = this.data.iterator().next();
            //If 'this' contains just an empty phrase, there's nothing to resolve.
            if (isEmpty(onlyPhrase)) {
                return false;
            }
            //If 'this' contains a single proposition, resolution is trivial.
            if (size(onlyPhrase) == 1) {
                ConcreteType resolution = resolutionMap.get(minElem(onlyPhrase));
                if (resolution == null) {
                    //In this case, there is no replacement
                    return false;
                } else {
                    //In this case, just clear 'this' and merge in 'resolution'
                    this.data.clear();
                    this.merge(resolution);
                    return true;
                }
            }
        }

        //
        //TODO: I may want to optimize the case where data contains a single
        //  phrase with a single element and it is replaced. If not here than
        //  at least in Constraint b/c it's really simple there, just return
        //  map.get(x).
        //
        //
        long elapInnerCross = 0, elapPhraseCross = 0, elapPhraseMerge = 0;
        int count = 0;//in the end, will always be original #phrases
//        System.out.println("BEGIN: NormalForm#resolveAll(..) this.size=" + this.getNumProps() + ":" + this.getNumPhrases() + "; resMap.size=" + resolutionMap.size());
        final long overallStart = System.nanoTime();

        //Update each phrase, replacing all occurrences of a key from the map
        //  with the value. Modified phrases are removed from 'this.data' via
        //  the iterator and their expansion merged with 'toAdd' which is then
        //  merged back into 'this' at the end.
        //
        ConcreteType toAdd = create(null);//use 'null' to create empty sentence
        for (Iterator<PhraseType> it = data.iterator(); it.hasNext();) {
            count++;
            PhraseType phrase = it.next();
            //NOTE: In general, we should expect iteration over the 'phrase' to
            //  be slow compared to iteration over the Map (ex: the SparseBitSet
            //  phrase iterator is slow when values are large and/or sparse).
            //  Hence, preference should be given to iteration over the Map when
            //  possible. However, if that map is very large, its iteration
            //  could be the bottleneck so avoid iterating it whenever possible.
            //
            //NOTE: To avoid computing the new phrase size in every iteration
            //  (used to determine when the phrase has only a single element
            //  and the iteration over the Map need not continue), just compute
            //  it once up front and then subtract removals until the element
            //  count reaches 1. Hence, the overhead w/in the loop is only a
            //  decrement and int equality check which is likely faster than any
            //  method call for a size re-computation.
            int elemsInPhrase = size(phrase);
            assert elemsInPhrase > 0;//0 is handled earlier and cannot be <0
            //With this size, we can additionally handle the easy case of size-1
            //  phrases to avoid iterating the Map since we can quickly lookup
            //  the single element.
            if (elemsInPhrase == 1) {
                ElemType onlyElem = minElem(phrase);
                assert onlyElem != null;//sanity check
                ConcreteType resolution = resolutionMap.get(onlyElem);
                //When there is no resolution for the single element in the
                //  phrase, nothing happens. However, if there is a resolution,
                //  then remove the phrase and merge the resolution with 'toAdd'
                if (resolution != null) {
                    it.remove();
                    long start = System.nanoTime();
                    toAdd.merge(resolution);
                    elapPhraseMerge += System.nanoTime() - start;
                }
            } else if (elemsInPhrase == 2 && resolutionMap.size() > 1500) {
                //NOTE: minElem(..) cannot be used to get the second value
                //  unless the first is removed, thus we would always end up 
                //  removing it and thus always removing the phrase from 'data'
                //  and merging with 'toAdd' so it seems the better approach is
                //  to just use the iterator to retrieve the 2 elements.
                Iterator<ElemType> pIt = iterator(phrase, false);
                assert pIt.hasNext();
                ElemType e1 = pIt.next();
                assert pIt.hasNext();
                ElemType e2 = pIt.next();
                assert !pIt.hasNext();

                ConcreteType res1 = resolutionMap.get(e1);
                ConcreteType res2 = resolutionMap.get(e2);
                if (res1 != null || res2 != null) {
                    //If either element has a replacement, remove the phrase from
                    //  'data' and create an aggregate for the replacement(s).
                    it.remove();
                    ConcreteType aggregate = create(createSingleton(null));//use 'null' to create empty phrase
                    //For both elements, cross the replacement or (if null) the
                    //  original element into the aggregate.
                    long startCross = System.nanoTime();
                    if (res1 == null) {
                        ((NormalForm<?, ElemType, ?>) aggregate).appendElemToEachPhrase_internal(e1);
                    } else {
                        aggregate.cross(res1);
                    }
                    if (res2 == null) {
                        ((NormalForm<?, ElemType, ?>) aggregate).appendElemToEachPhrase_internal(e2);
                    } else {
                        aggregate.cross(res2);
                    }
                    elapInnerCross += System.nanoTime() - startCross;
                    //Finally, merge the aggregate into 'toAdd'
                    long start = System.nanoTime();
                    toAdd.merge(aggregate);
                    elapPhraseMerge += System.nanoTime() - start;
                } //else nothing to do when no replacements
            } else {
                //When there is more than one element in the phrase, compute the
                //  cross product over all replacement values and the remainder
                //  of the phrase after removing the replaced elements. Merge
                //  this cross product with 'toAdd' and remove the phrase.
                boolean removedPhrase = false;
                ConcreteType aggregate = create(createSingleton(null));//use 'null' to create empty phrase
                //Iterate the resolution map and, if a key is found in the phrase,
                //  then remove it and add the mapped value to 'aggregate'.
                assert elemsInPhrase > 1;//sanity check: inner break condition doesn't need to capture 0
                for (Map.Entry<ElemType, ConcreteType> e : resolutionMap.entrySet()) {
                    final ElemType key = e.getKey();
                    if (contains(phrase, key)) {
                        //If any key from the map is found in the phrase, remove
                        //  the phrase from 'data' so that it's cross product
                        //  with 'aggregate' can be re-added with absorption.
                        //NOTE: Phrase must be removed before modifying it or
                        //  else HashSet.iterator().remove() will not work!
                        if (!removedPhrase) {
                            removedPhrase = true;
                            it.remove();
                        }
                        //Remove the 'key' from the phrase and decrement the 
                        //  count of elements remaining in the phrase.
                        remove(phrase, key);
                        elemsInPhrase--;

                        //Accumulate the value via cross product
                        long startCross = System.nanoTime();
                        aggregate.cross(e.getValue());
                        elapInnerCross += System.nanoTime() - startCross;

                        //After removal of the current key, if the phrase has
                        //  only a single element remaining, handle it trivally
                        //  (similar to before) to avoid any further iteration.
                        if (elemsInPhrase == 1) {
                            ElemType onlyElem = minElem(phrase);
                            assert onlyElem != null;//sanity check
                            ConcreteType resolution = resolutionMap.get(onlyElem);
                            if (resolution != null) {
                                assert removedPhrase;//must have been removed above

                                //Remove the final element from the phrase and
                                //  decrement the count of remaining elements.
                                remove(phrase, onlyElem);
                                elemsInPhrase--;

                                //Accumulate the 'resolution' via cross product
                                startCross = System.nanoTime();
                                aggregate.cross(resolution);
                                elapInnerCross += System.nanoTime() - startCross;
                            }
                            //Either way, break the loop b/c there can be no
                            //  further matches.
                            break;
                        }
                    }
                }
                assert size(phrase) == elemsInPhrase;//sanity check

                //Finally, if there were any replacements, then cross the
                //  aggregated replacements into what remains of the phrase
                //  and merge it with 'toAdd' for later addition to 'this'.
                if (removedPhrase) {
                    long start = System.nanoTime();
                    aggregate.cross(phrase);
                    elapPhraseCross += System.nanoTime() - start;

                    start = System.nanoTime();
                    toAdd.merge(aggregate);
                    elapPhraseMerge += System.nanoTime() - start;
                }
            }
        }

        long startFinal = System.nanoTime();
        this.merge(toAdd);
        long elapFinalAdd = System.nanoTime() - startFinal;

//        final double overallElapsedMS = ((double) (System.nanoTime() - overallStart) / 1_000_000);
//        System.out.println("END: NormalForm#resolveAll(..) this.size=" + this.getNumProps() + ":" + this.getNumPhrases()
//                + "; resMap.size=" + resolutionMap.size() + " in " + overallElapsedMS + "ms"
//                + "; count=" + count
//                + "; innerCross(ms)=" + (((double) elapInnerCross) / 1_000_000)
//                + "; phraseCross(ms)=" + (((double) elapPhraseCross) / 1_000_000)
//                + "; phraseMerge(ms)=" + (((double) elapPhraseMerge) / 1_000_000)
//                + "; finalMerge(ms)=" + (((double) elapFinalAdd) / 1_000_000)
//        );
//APPROACH: Iterating over the phrase (slow)
//                countOpt1++;
//                long startOpt = System.nanoTime();
//                //Iterate the phrase and check for a replacement for each value.
//                //  When an element from the phrase is found in the map, add the
//                //  mapped value to 'newPhrase' and removing the old value.
//                for (Iterator<ElemType> phrItr = iterator(phrase, false); phrItr.hasNext();) {
//                    ElemType elem = phrItr.next();
//                    ConcreteType replacement = resolutionMap.get(elem);
//                    if (replacement != null) {
//                        //If any key from the map is found in the phrase, we need
//                        //  to remove the phrase from 'data' so that it's union with
//                        //  'newPhrase' can be re-added with absorption.
//                        //Phrase must be removed before modifying it or else
//                        //  HashSet.iterator().remove() will not work!
//                        if (!removedPhrase) {
//                            removedPhrase = true;
//                            it.remove();
//                        }
//                        //Remove the 'elem' from the phrase
//                        remove(phrase, elem);
//                        long startCross = System.nanoTime();
//                        //Accumulate the value via cross product
//                        aggregate.cross(replacement);
//                        elapInnerCross1 += System.nanoTime() - startCross;
//                    }
//                }
//                elapOpt1 += System.nanoTime() - startOpt;
        return !toAdd.isEmpty();
    }

    /**
     * Find all references to {@code symbolicValue} and replace them with the
     * given {@link NormalForm}.
     *
     * @param symbolicValue
     * @param expansion
     *
     * @return {@code true} iff the given {@code symbolicValue} was present and
     *         replaced
     */
    public boolean resolve(ElemType symbolicValue, ConcreteType expansion) {
        //NOTE: resolveAll(..) calls checkModifiability()
        HashMap<ElemType, ConcreteType> resolutionMap = new HashMap<>();
        resolutionMap.put(symbolicValue, expansion);
        return resolveAll(resolutionMap);
    }

    /**
     * Updates {@code this} {@link NormalForm} via the "and" operation with
     * {@code newProp} and returns a reference to {@code this}.
     *
     * @param newProp
     *
     * @return {@code this}
     */
    public ConcreteType and(ElemType newProp) {
        return formRules.and(getConcreteThis(), newProp);
    }

    /**
     * Updates {@code this} {@link NormalForm} via the "and" operation with
     * {@code newSentence} and returns a reference to {@code this}.
     *
     * @param newSentence
     *
     * @return {@code this}
     */
    public ConcreteType and(ConcreteType newSentence) {
        return formRules.and(getConcreteThis(), newSentence);
    }

    /**
     * Updates {@code this} {@link NormalForm} via the "or" operation with
     * {@code newProp} and returns a reference to {@code this}.
     *
     * @param newProp
     *
     * @return {@code this}
     */
    public ConcreteType or(ElemType newProp) {
        return formRules.or(getConcreteThis(), newProp);
    }

    /**
     * Updates {@code this} {@link NormalForm} via the "or" operation with
     * {@code newSentence} and returns a reference to {@code this}.
     *
     * @param newSentence
     *
     * @return {@code this}
     */
    public ConcreteType or(ConcreteType newSentence) {
        return formRules.or(getConcreteThis(), newSentence);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.data);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final NormalForm other = (NormalForm) obj;
        return this.data.equals(other.data);
    }

    /**
     *
     * @return the number of phrases in this NormalForm
     */
    public final int getNumPhrases() {
        return data.size();
    }

    /**
     *
     * @return the total number of propositions contained in all phrases of this
     *         (counting occurrences of the same proposition in different
     *         phrases for each time it occurs).
     */
    public final int getNumProps() {
        int numProps = 0;
        for (PhraseType phrase : data) {
            numProps += size(phrase);
        }
        return numProps;
    }

    /**
     *
     * @param expected
     *
     * @return {@code true} iff the given {@code int} equals the total number of
     *         propositions contained in all phrases of this (counting
     *         occurrences of the same proposition in different phrases for each
     *         time it occurs).
     */
    public final boolean numPropsEquals(int expected) {
        int numProps = 0;
        for (PhraseType phrase : data) {
            numProps += size(phrase);
            //Since we are only adding to 'numProps' short circuit
            //  if it's already greater than 'expected'
            if (numProps > expected) {
                return false;
            }
        }
        return numProps == expected;
    }

    /**
     *
     * @return new {@link Set} containing all unique propositions contained in
     *         any phrase of {@code this}
     */
    public final Set<ElemType> getAllProps() {
        HashSet<ElemType> retVal = new HashSet<>();
        for (PhraseType s : data) {
            collectToSet(s, retVal);
        }
        return retVal;
    }

    /**
     *
     * @param elem
     *
     * @return {@code true} iff {@code this} contains the given proposition in
     *         some phrase
     */
    public boolean containsProp(ElemType elem) {
        for (PhraseType s : data) {
            if (contains(s, elem)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param elem
     *
     * @return the number of phrases containing the given proposition
     */
    public int countProp(ElemType elem) {
        int retVal = 0;
        for (PhraseType s : data) {
            if (contains(s, elem)) {
                retVal++;
            }
        }
        return retVal;
    }

    @Override
    public String toString() {
        return toString(PrintingConnectives.DEFAULT, false);
    }

    /**
     * Builds a {@link String} representation of {@code this} with the option to
     * sort the items in {@link String} returned.
     *
     * @param conn
     * @param sorted
     *
     * @return
     */
    public String toString(PrintingConnectives conn, boolean sorted) {
        StringBuilder b = new StringBuilder();
        for (Iterator<PhraseType> itP = (sorted ? order(data) : data).iterator(); itP.hasNext();) {
            PhraseType phrase = itP.next();
            b.append(conn.phraseWrapBegin);
            for (Iterator<ElemType> itE = iterator(phrase, sorted); itE.hasNext();) {
                b.append(itE.next());
                if (itE.hasNext()) {
                    b.append(conn.innerConnective);
                }
            }
            b.append(conn.phraseWrapEnd);
            if (itP.hasNext()) {
                b.append(conn.outerConnective);
            }
        }
        return b.toString();
    }

    /**
     * Clears {@code this} and rebuilds it from the given {@link String} using
     * the given {@link PrintingConnectives} to distinguish elements and phrases
     * in the {@link String}.
     *
     * @param input
     * @param conn
     *
     */
    public void fromString(String input, PrintingConnectives conn) {
//        System.out.println("  input  = '" + input + "'");
        //Ensure 'this' is empty
        this.data.clear();
        //Short-circuit when 'input' is empty or else T/F will be
        //  indistinguishable due to the nature of String.split(..)
        if (input.isEmpty()) {
            return;
        }
        //Split the input string into phrases and parse each
        for (String phraseStr : input.split(quoteForRegex(conn.outerConnective))) {
            if (phraseStr.isEmpty()) {
                //If there are outer connectives w/ no phrase in between, just
                //  skip adding the empty phrase (to distinguish T/F)
                continue;
            }
//            System.out.println("  phraseStr  = '" + phraseStr + "'");
            if (phraseStr.startsWith(conn.phraseWrapBegin)) {
                phraseStr = phraseStr.substring(1);
            } else if (!conn.phraseWrapBegin.isEmpty()) {
                throw new IllegalArgumentException("Phrase does not begin with \"" + conn.phraseWrapBegin + "\": " + phraseStr);
            }
            if (phraseStr.endsWith(conn.phraseWrapEnd)) {
                phraseStr = phraseStr.substring(0, phraseStr.length() - 1);
            } else if (!conn.phraseWrapEnd.isEmpty()) {
                throw new IllegalArgumentException("Phrase does not end with \"" + conn.phraseWrapEnd + "\": " + phraseStr);
            }
            PhraseType phrase = null;
            //Split the phrase into elements and parse each
            for (String itemStr : phraseStr.split(quoteForRegex(conn.innerConnective))) {
//                System.out.println("  itemStr  = '" + itemStr + "'");
                ElemType elem = parseElement(itemStr);
                if (phrase == null) {
                    phrase = createSingleton(elem);
                } else {
                    add(phrase, elem);
                }
            }
            tryAddWithAbsorption(phrase);
        }
    }

    private static String quoteForRegex(String s) {
        return "\\Q" + s + "\\E";

    }

    /**
     *
     */
    public static class PrintingConnectives {

        /**
         * {@link String} used to denote the start of a phrase in the string
         * representation. If both this and {@link #phraseWrapEnd} are empty,
         * then the literals TRUE and FALSE are indistinguishable in String
         * format.
         */
        public final String phraseWrapBegin;

        /**
         * {@link String} used to denote the start of a phrase in the string
         * representation. If both this and {@link #phraseWrapBegin} are empty,
         * then the literals TRUE and FALSE are indistinguishable in String
         * format.
         */
        public final String phraseWrapEnd;
        /**
         * {@link String} used to separate items within a phrase in the string
         * representation.
         */
        public final String innerConnective;

        /**
         * {@link String} used to separate phrases in the string representation.
         */
        public final String outerConnective;

        /**
         * General constructor.
         *
         * @param phBegin
         * @param phEnd
         * @param inner
         * @param outer
         */
        public PrintingConnectives(String phBegin, String phEnd, String inner, String outer) {
            phraseWrapBegin = phBegin;
            phraseWrapEnd = phEnd;
            innerConnective = inner;
            outerConnective = outer;
        }

        public static final PrintingConnectives DEFAULT = new PrintingConnectives("(", ")", "+", "-");
        public static final PrintingConnectives DISJUNCTIVE_STD = new PrintingConnectives("(", ")", "&", "|");
        public static final PrintingConnectives DISJUNCTIVE_CSV = new PrintingConnectives("", "", "&", ",");
        public static final PrintingConnectives CONJUNCTIVE_STD = new PrintingConnectives("(", ")", "|", "&");
        public static final PrintingConnectives CONJUNCTIVE_CSV = new PrintingConnectives("", "", "|", ",");

    }

    /**
     * Creates a shallow copy of the given set.
     *
     * NOTE: implementation MUST NOT access, modify, etc. the instance data of
     * any subclass because it may be called during initialization of
     * {@code this} base object.
     *
     * @param orig
     *
     * @return
     */
    protected abstract PhraseType clone(PhraseType orig);

    /**
     *
     * @param phrase
     *
     * @return the number of elements contained in the phrase
     */
    protected abstract int size(PhraseType phrase);

    /**
     *
     * @param phrase
     *
     * @return {@code true} iff there are no elements in the phrase
     */
    protected abstract boolean isEmpty(PhraseType phrase);

    /**
     * Utility method to check if {@code s} contains element {@code e}.
     *
     * @param s
     * @param e
     *
     * @return {@code true} iff {@code s} contains element {@code e}
     */
    protected abstract boolean contains(PhraseType s, ElemType e);

    /**
     * Utility method to check if {@code s1} contains all elements in
     * {@code s2}. In other words, {@code s2} is a subset of {@code s1}.
     *
     * @param s1
     * @param s2
     *
     * @return {@code true} iff {@code s1} contains all elements in {@code s2}
     */
    protected abstract boolean containsAll(PhraseType s1, PhraseType s2);

    /**
     * Utility method to check if {@code s1} contains any element in {@code s2}.
     * In other words, {@code s1} intersects {@code s2}.
     *
     * @param s1
     * @param s2
     *
     * @return {@code true} iff {@code s1} contains any element in {@code s2}
     */
    protected abstract boolean containsAny(PhraseType s1, PhraseType s2);

    /**
     * Creates a new set containing only the given element (if non-null). If the
     * given element is null, the implementation should return an empty set.
     *
     * @param singleProp
     *
     * @return
     */
    protected abstract PhraseType createSingleton(ElemType singleProp);

    /**
     * Adds the given element to the set (if non-null). If the given element is
     * null, the implementation should simply do nothing.
     *
     * @param set
     * @param newItem
     */
    protected abstract void add(PhraseType set, ElemType newItem);

    /**
     * Removes the given element from the set (if non-null). If the given
     * element is null, the implementation should simply do nothing.
     *
     * @param set
     * @param item
     */
    protected abstract void remove(PhraseType set, ElemType item);

    /**
     * Adds all elements in {@code src} to the given {@link HashSet}.
     *
     * @param src
     * @param dst
     */
    protected abstract void collectToSet(PhraseType src, HashSet<ElemType> dst);

    /**
     * Adds all elements in {@code toAdd} to {@code base}.
     *
     * @param base
     * @param toAdd
     */
    protected abstract void addAll(PhraseType base, PhraseType toAdd);

    /**
     *
     * @param phrases
     *
     * @return an ordering of the elements in {@code phrases}
     */
    protected abstract Iterable<PhraseType> order(Collection<PhraseType> phrases);

    /**
     *
     * @param phrase
     * @param ordered
     *
     * @return an {@link Iterator} over the elements in the given
     *         {@code SetType}. Ordered if 'ordered' is true.
     */
    protected abstract Iterator<ElemType> iterator(PhraseType phrase, boolean ordered);

    /**
     * Returns the smallest element in the phrase (or {@code null} if empty).
     * Equivalent to {@code iterator(phrase, true).next()} when the
     * {@code phrase} is not empty but may have better performance.
     *
     * @param phrase
     *
     * @return
     */
    protected abstract ElemType minElem(PhraseType phrase);

    /**
     * Converts the given {@link String} representation of an element to the
     * element type. If the given {@link String} is empty, can return some
     * default value or {@code null}. If the given {@link String} is
     * {@code null}, may throw {@link NullPointerException}.
     *
     * @param s
     *
     * @return
     */
    public abstract ElemType parseElement(String s);

    /**
     * Creates a deep copy of {@code this}.
     *
     * @param unmodifiable whether or not the new instance should be marked as
     *                     unmodifiable/immutable
     *
     * @return
     */
    protected abstract ConcreteType clone(boolean unmodifiable);

    /**
     * Creates a new {@link NormalForm} with the single phrase given, unless the
     * given phrase is {@code null}, in which case the created
     * {@link NormalForm} will be empty (i.e. contains 0 phrases).
     *
     * @param firstPhrase
     *
     * @return
     */
    protected abstract ConcreteType create(PhraseType firstPhrase);
}

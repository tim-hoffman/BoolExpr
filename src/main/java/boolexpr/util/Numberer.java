package boolexpr.util;

/**
 * @author Timothy Hoffman
 * @param <N>
 */
public interface Numberer<N> {

    /**
     * Returns the number assigned to the given node, creating a new number if
     * the node has not already been assigned a number.
     *
     * @param node
     *
     * @return
     */
    int getOrCreateNumber(N node);

    /**
     * @return the {@code int} that will be used to number the next Object used
     *         as parameter to {@link #getOrCreateNumber(java.lang.Object)}
     */
    int getNextNumberAvailable();

    /**
     *
     * @param o
     *
     * @return
     */
    public Integer getOrNull(N o);
}

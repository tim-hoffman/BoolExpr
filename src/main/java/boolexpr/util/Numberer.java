package boolexpr.util;

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
 * @author Timothy Hoffman
 * 
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

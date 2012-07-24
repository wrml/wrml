/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wrml.runtime;

/**
 * The runtime graph's associated error type.
 */
public class ModelGraphException extends RuntimeException {

    private final ModelGraph _ModelGraph;

    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception to communicate a problem about the specified
     * {@link ModelGraph}.
     * 
     * @param message
     *            The message to explain what is going on.
     * 
     * @param modelGraph
     *            The impacted graph.
     */
    ModelGraphException(String message, ModelGraph modelGraph) {
        super(message);
        _ModelGraph = modelGraph;
    }

    /**
     * Get the graph associated with this error.
     * 
     * @return
     *         The graph that raised this exception.
     */
    public ModelGraph getModelGraph() {
        return _ModelGraph;
    }

}

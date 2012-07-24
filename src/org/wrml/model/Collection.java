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

package org.wrml.model;

import java.util.Set;

import org.wrml.runtime.Dimensions;
import org.wrml.util.observable.ObservableList;

/**
 * A Collection is a document representing a multitude of documents.
 * 
 * As a transferable state representation, a Collection model's content is
 * necessarily finite although its underlying set of documents may be infinite
 * (at least conceptually). For this reason, Collections are paginated, with
 * each model instance (dimensionally) holding a page's worth of the Collection.
 */
public interface Collection<D extends Document> extends Named, Document {

    /**
     * Creates the specified document in this collection.
     * 
     * @param document
     * @param dimensions
     * @return
     */
    public D create(D document);

    /**
     * The elements in this page of the collection
     * 
     * @return The page elements.
     */
    public ObservableList<D> getPageElements();

    /**
     * Search this Collection for a set of documents.
     * 
     * @param criteria
     * @param dimensions
     * @return
     */
    public Set<D> search(DocumentSearchCriteria criteria, Dimensions dimensions);
}

/**
 * WRML - Web Resource Modeling Language
 *  __     __   ______   __    __   __
 * /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \
 * \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____
 *  \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\
 *   \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/
 *
 * http://www.wrml.org
 *
 * Copyright (C) 2011 - 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wrml.model.schema;


import java.net.URI;
import java.util.List;

/**
 * <p>
 * A specialized {@link ListValue} that is "dynamically" filled with elements that result from a search.
 * The search is executed upon request a {@link org.wrml.model.Model} instance associated with the {@link Schema} that
 * contains a {@link Slot} with a {@link CollectionValue}.
 * </p>
 * <p>
 * The search criteria is modeled using {@link CollectionValueSearchCriterion} placed the <b>and</b> ({@link #getAnd()})
 * and/or the <b>or</b> ({@link #getOr()}) slots.
 * </p>
 *
 * @see CollectionValueSearchCriterion
 * @see org.wrml.runtime.schema.CollectionSlot
 * @see org.wrml.runtime.schema.CollectionSlotCriterion
 * @see org.wrml.runtime.search.SearchCriteria
 * @see org.wrml.runtime.service.Service#search(org.wrml.runtime.search.SearchCriteria)
 */
public interface CollectionValue extends Primitive, Inextensible, ListValue {

    /**
     * <p>
     * The {@link java.net.URI} id of the {@link org.wrml.model.rest.LinkRelation} associated with this {@link CollectionValue}.
     * </p>
     *
     * @see org.wrml.model.rest.LinkRelation#getUri()
     * @see org.wrml.model.rest.Document
     * @see org.wrml.model.rest.Link
     * @see org.wrml.model.rest.LinkTemplate
     */
    URI getLinkRelationUri();

    /**
     * <p>
     * The optional limit to set on the number of elements that may be contained within this {@link CollectionValue}.
     * </p>
     * <p>
     * The default value is <code>null</code> to indicate that there is no limit.
     * </p>
     */
    Integer getLimit();

    /**
     * <p>
     * The optional list of {@link CollectionValueSearchCriterion} that is logically "ANDed" together to qualify/filter the contents of this {@link CollectionValue}.
     * </p>
     *
     * @return The optional list of {@link CollectionValueSearchCriterion} that is logically "ANDed" together to qualify/filter the contents of this {@link CollectionValue}.
     */
    List<CollectionValueSearchCriterion> getAnd();

    /**
     * <p>
     * The optional list of {@link org.wrml.runtime.schema.CollectionSlotCriterion} that is logically "ORed" together to qualify/filter the contents of this {@link CollectionValue}.
     * </p>
     *
     * @return The optional list of {@link org.wrml.runtime.schema.CollectionSlotCriterion} that is logically "ORed" together to qualify/filter the contents of this {@link CollectionValue}.
     */
    List<CollectionValueSearchCriterion> getOr();

    /**
     * @see #getLinkRelationUri()
     */
    URI setLinkRelationUri(URI linkRelationUri);

    /**
     * @see #getLimit()
     */
    Integer setLimit(Integer limit);

}

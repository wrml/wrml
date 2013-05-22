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
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
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
package org.wrml.runtime.search;

import org.wrml.model.Model;
import org.wrml.runtime.Dimensions;

import java.util.List;
import java.util.Set;

/**
 * Container for model search criteria.
 *
 * @see org.wrml.runtime.schema.ProtoSearchCriteria#buildSearchCriteria(org.wrml.model.Model)
 * @see org.wrml.runtime.service.Service#search(SearchCriteria)
 */
public interface SearchCriteria
{

    /**
     * TODO: Javadoc
     */
    Dimensions getResultDimensions();

    /**
     * TODO: Javadoc
     */
    List<SearchCriterion> getAnd();

    /**
     * TODO: Javadoc
     */
    List<SearchCriterion> getOr();

    /**
     * TODO: Javadoc
     */
    Set<String> getProjectionSlotNames();

    /**
     * TODO: Javadoc
     */
    Integer getResultLimit();

    /**
     * TODO: Javadoc
     */
    Model getReferrer();

    /**
     * TODO: Javadoc
     */
    String getReferrerCollectionSlotName();


}

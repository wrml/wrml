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
package org.wrml.runtime.schema;

import java.lang.annotation.*;

/**
 * <p>
 * {@link CollectionSlot} annotates a Java {@link java.lang.reflect.Method} ({@link java.lang.annotation.ElementType#METHOD})
 * as a {@link org.wrml.model.schema.Slot} containing a {@link java.util.List} of {@link org.wrml.model.Model}s
 * that is <i>filled</i> dynamically with the results of a search.
 * </p>
 *
 * @see org.wrml.runtime.service.Service#search(org.wrml.runtime.search.SearchCriteria)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CollectionSlot
{

    /**
     * <p>
     * The optional {@link org.wrml.model.rest.LinkRelation} {@link java.net.URI} designating the relationship between this {@link CollectionSlot} and the {@link org.wrml.model.rest.Document}s that it contains.
     * </p>
     * <p>
     * This value is optional for slots that do not contain {@link org.wrml.model.rest.Document} models (and thus do not require the framework to determine their {@link java.net.URI} slot values).
     * </p>
     */
    String linkRelationUri() default "";

    /**
     * <p>
     * The optional limit to set on the number of elements that may be contained within this {@link CollectionSlot}.
     * </p>
     * <p>
     * The default value is <code>-1</code> to indicate that there is no limit.
     * </p>
     */
    int limit() default -1;

    /**
     * <p>
     * The optional array of {@link CollectionSlotCriterion} that is logically "ANDed" together to qualify/filter the contents of this {@link CollectionSlot}.
     * </p>
     *
     * @return The optional array of {@link CollectionSlotCriterion} that is logically "ANDed" together to qualify/filter the contents of this {@link CollectionSlot}.
     */
    CollectionSlotCriterion[] and() default {};

    /**
     * <p>
     * The optional array of {@link CollectionSlotCriterion} that is logically "ORed" together to qualify/filter the contents of this {@link CollectionSlot}.
     * </p>
     *
     * @return The optional array of {@link CollectionSlotCriterion} that is logically "ORed" together to qualify/filter the contents of this {@link CollectionSlot}.
     */
    CollectionSlotCriterion[] or() default {};
}

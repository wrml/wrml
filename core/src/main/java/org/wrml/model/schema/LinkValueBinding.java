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

import org.wrml.model.Model;
import org.wrml.runtime.schema.DefaultValue;

/**
 * <p>
 * One or more instances of this model may optionally be added to a {@link LinkValue} to establish the <i>binding</i> between
 * the link's <i>referrer</i> and its <i>referenced</i> endpoint (both are typically {@link org.wrml.model.rest.Document}s).
 * </p>
 * <p>
 * The reference slot is expected to be a key slot that is used as a {@link org.wrml.runtime.rest.UriTemplate} parameter when linking.
 * Note that a {@link LinkValueBinding} is not necessary in cases where the referenced document slot's name is the same name as the referrer's slot that holds the link "binding" value.
 * This <i>same slot name</i> case is considered a <i>natural</i> link binding and is enabled by default.
 * </p>
 */
public interface LinkValueBinding extends Model {

    /**
     * <p>
     * The slot, within the link's referenced document, that is <i>bound</i> by this {@link LinkValueBinding}.
     * </p>
     * <p>
     * Note that the reference slot is expected to be a key slot that is used as a {@link org.wrml.runtime.rest.UriTemplate} parameter when linking.
     * </p>
     *
     * @return The slot, within the link's referenced document, that is <i>bound</i> by this {@link LinkValueBinding}.
     */
    String getReferenceSlot();

    /**
     * The source of the value that will be used to "fill in" the reference slot upon linking.
     *
     * @return The source of the value that will be used to "fill in" the reference slot upon linking.
     */
    String getValueSource();

    /**
     * The source type for the binding value.
     *
     * @return The source type for the binding value.
     */
    @DefaultValue("ReferrerSlot")
    ValueSourceType getValueSourceType();

    /**
     * @see #getReferenceSlot()
     */
    String setReferenceSlot(String referenceSlot);

    /**
     * @see #getValueSource()
     */
    String setValueSource(String valueSource);

    /**
     * @see #getValueSourceType()
     */
    ValueSourceType setValueSourceType(ValueSourceType valueSourceType);

}

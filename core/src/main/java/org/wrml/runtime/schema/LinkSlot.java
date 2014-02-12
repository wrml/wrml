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

import org.wrml.model.rest.Api;
import org.wrml.model.rest.Method;
import org.wrml.model.schema.LinkValue;
import org.wrml.model.schema.Slot;

import java.lang.annotation.*;

/**
 * <p>
 * A {@link LinkSlot} identifies a Java {@link java.lang.reflect.Method} ({@link ElementType#METHOD}) as a {@link Slot} containing a
 * {@link LinkValue}. In the context of the WRML runtime, this annotation enables the automation of {@link Api}-metadata
 * powered interconntect automation (aka HATEOAS).
 * </p>
 * <p>
 * From the runtime's perspective {@link LinkSlot} annotations describe hypermedia-oriented <i><b>functions</b></i>.
 * </p>
 * <p>
 * <pre>
 *  LinkSlot: <resonseSchema> <method><rel>(<requestSchema>)
 *  Example: org/science/rocket/RocketLaunchResult POST rel="http://.../makeRocketGoNow" (org/science/rocket/RocketLaunchInstructions)
 * </pre>
 * </p>
 * <p>
 * Compared to...
 * </p>
 * <p>
 * <pre>
 *  Function/Method: <return type> <name>(parameters)
 *  Example: org.science.rocket.RocketLaunchResult makeRocketGoNow(org.science.rocket.RocketLaunchInstructions instructions)
 * </pre>
 * </p>
 *
 * @see Slot
 * @see LinkValue
 * @see org.wrml.model.rest.Link
 * @see org.wrml.model.rest.LinkRelation
 * @see LinkProtoSlot
 * @see LinkSlotBinding
 * @see java.net.URI
 * @see org.wrml.runtime.rest.UriTemplate
 * @see Method
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface LinkSlot {

    /**
     * The (String representation) of the {@link java.net.URI} that identifies the {@link org.wrml.model.rest.LinkRelation}
     * associated with this {@link LinkSlot}.
     *
     * @return The (String representation) of the {@link java.net.URI} that identifies the {@link org.wrml.model.rest.LinkRelation}
     * associated with this {@link LinkSlot}.
     */
    String linkRelationUri();

    /**
     * <p>
     * The interaction {@link Method} associated with this {@link LinkSlot}, which is effectively copied here (from the
     * {@link org.wrml.model.rest.LinkRelation#getMethod()}) so that the {@link org.wrml.model.rest.LinkRelation} document
     * does not need to be retrieved/loaded in order to "prototype" and load the schema associated with this slot.
     * </p>
     * <p>
     * The default value is {@link Method#Get}.
     * </p>
     *
     * @return The interaction {@link Method} associated with this {@link LinkSlot}
     */
    Method method() default Method.Get;

    /**
     * The optional array of {@link LinkSlotBinding} that enable a degree control/logic to influence the formation of the "href" values with
     * the {@link org.wrml.model.rest.Link} instances that occupy this link slot within model instances.
     *
     * @return The optional array of {@link LinkSlotBinding}.
     */
    LinkSlotBinding[] bindings() default {};

    /**
     * Flag that determines the default behavior of this {@link LinkSlot} with respect to the embedding of the reference document. The default value is <code>false</code>.
     *
     * @return <code>true</code> if this {@link LinkSlot} should always embed the linked document, <code>false</code> if it should be left up to the request.
     * @see org.wrml.runtime.Dimensions#getEmbeddedLinkSlotNames()
     */
    boolean embedded() default false;

}

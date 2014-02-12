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

import org.wrml.model.MaybeReadOnly;
import org.wrml.model.MaybeRequired;
import org.wrml.model.Model;

import java.net.URI;

/**
 * <p>
 * {@link ModelValue}s enable {@link Model}s to be embedded within other {@link Model}s.
 * </p>
 *
 * @see Model
 * @see Schema
 * @see Slot
 * @see <a href="http://www.json.org">JSON object</a>
 */
public interface ModelValue extends MaybeReadOnly, MaybeRequired, Primitive, Inextensible, Value {

    /**
     * <p>
     * The {@link URI} id of the {@link Schema} associated with this {@link ModelValue}'s {@link Model} value. The WRML
     * runtime ensures that only {@link Model}s with the identified {@link Schema} are permitted as {@link Slot} values
     * associated with this {@link ModelValue}.
     * </p>
     *
     * @see Schema#getUri()
     * @see Model#getSchemaUri()
     */
    URI getModelSchemaUri();

    /**
     * @see #getModelSchemaUri()
     */
    URI setModelSchemaUri(URI modelSchemaUri);
}

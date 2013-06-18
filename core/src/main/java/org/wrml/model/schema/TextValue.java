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
import java.util.UUID;

import org.wrml.model.MaybeReadOnly;
import org.wrml.model.MaybeRequired;
import org.wrml.model.Model;

import org.wrml.runtime.schema.DefaultValue;
import java.util.List;

/**
 * <p>
 * A text-based ({@link String}) value, which may conform to some defined {@link Syntax}.
 * </p>
 * 
 * @see Syntax
 */
public interface TextValue extends MaybeReadOnly, MaybeRequired, Primitive, Inextensible, Value, Model
{

    /**
     * The WRML constant name for a TextValue's <i>maximumLength</i> slot.
     */
    public static final String SLOT_NAME_MAXIMUM_LENGTH = "maximumLength";

    /**
     * The WRML constant name for a TextValue's <i>minimumLength</i> slot.
     */
    public static final String SLOT_NAME_MINIMUM_LENGTH = "minimumLength";


    /**
     * The WRML constant name for a TextValue's <i>multiline</i> slot.
     */
    public static final String SLOT_NAME_MULTILINE = "multiline";

    /**
     * <p>
     * The <i>optional</i> default associated with this value.
     * </p>
     * 
     * @see DefaultValue
     */
    String getDefault();

    List<String> getDisallowedValues();

    Integer getMaximumLength();

    Integer getMinimumLength();

    Boolean isMultiline();

    /**
     * <p>
     * The <i>optional</i> {@link org.wrml.model.schema.Syntax#getUri()} associated with this {@link TextValue}. The WRML runtime uses a
     * {@link TextValue}'s {@link Syntax} association to automatically <i>coerce</i> {@link String} values into their
     * more useful runtime counterparts (e.g. {@link URI}, {@link UUID} etc.).
     * </p>
     * 
     * @see Syntax
     */
    URI getSyntaxUri();

    /**
     * @see #getDefault()
     */
    String setDefault(String defaultValue);

    Integer setMaximumLength(final Integer maxLength);

    Integer setMinimumLength(final Integer minLength);

    Boolean setMultiline(final boolean isMultiline);

    /**
     * @see #getSyntaxUri()
     */
    URI setSyntaxUri(URI syntaxUri);
}

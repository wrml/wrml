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
package org.wrml.model.rest;

import org.wrml.model.Abstract;

import java.net.URI;

/**
 * <p>
 * An {@link Embedded} model is *always* part of a {@link Document} (as a slot value).
 * </p>
 */
public interface Embedded extends Abstract
{

    /**
     * The WRML constant name for a Embedded's <i>documentUri</i> slot.
     */
    public static final String SLOT_NAME_DOCUMENT_URI = "documentUri";

    /**
     * <p>
     * The {@link URI} (see {@link Document#getUri()}) of the {@link Document} that this {@link Embedded} is nested
     * within.
     * </p>
     */
    URI getDocumentUri();

    URI setDocumentUri(final URI containingDocumentUri);
}

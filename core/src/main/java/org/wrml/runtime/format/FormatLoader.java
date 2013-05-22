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
 * Copyright 2012 Mark Masse (OSS project WRML.org)
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
package org.wrml.runtime.format;

import org.wrml.model.format.Format;
import org.wrml.model.rest.Document;
import org.wrml.runtime.Keys;
import org.wrml.runtime.Loader;
import org.wrml.runtime.rest.MediaType;

import java.net.URI;
import java.util.Set;
import java.util.SortedSet;

/**
 * A {@link Loader} of {@link Format} models. Allows the WRML runtime to vary its supported {@link Format}s and
 * dynamically <i>learn</i> to communicate with new {@link Format}s.
 */
public interface FormatLoader extends Loader
{

    FormatLoaderConfiguration getConfig();

    /**
     * Get the {@link URI} that identifies the runtime's current default {@link Format}.
     */
    URI getDefaultFormatUri();

    /**
     * Get the runtime's current default {@link Format}.
     */
    Format getDefaultFormat();

    /**
     * Set the URI that identifies the runtime's current default {@link Format}.
     */
    void setDefaultFormatUri(final URI formatUri);

    /**
     * Returns the {@link Formatter} associated with the specified {@link Format} {@link URI}.
     *
     * @param formatUri The {@link URI} of the {@link Format} associated with the {@link Formatter}.
     * @return The {@link Formatter} associated with the specified {@link Format} {@link URI}.
     */
    Formatter getFormatter(final URI formatUri);

    /**
     * Returns the already loaded {@link Format} associated with the specified {@link Keys}.
     */
    Format getLoadedFormat(final Keys keys);

    /**
     * Returns the already loaded {@link Format} associated with the specified {@link MediaType}.
     */
    Format getLoadedFormat(final MediaType mediaType);


    /**
     * Returns the {@link Set} of already loaded {@link Format}s.
     */
    Set<Format> getLoadedFormats();

    /**
     * Returns the {@link Set} of already loaded {@link Format} {@link URI}s.
     */
    SortedSet<URI> getLoadedFormatUris();

    void loadConfiguredFormat(final FormatterConfiguration formatterConfiguration) throws FormatLoaderException;

    /**
     * Loads/reloads the specified {@link Format}.
     */
    void loadFormat(final Format format) throws FormatLoaderException;

    /**
     * Loads/reloads the specified {@link Format}, by it's {@link Document} key ({@link URI}) value.
     */
    Format loadFormat(final URI formatUri) throws FormatLoaderException;

    /**
     * Loads/reloads the specified {@link Formatter}.
     */
    void loadFormatter(final Formatter formatter) throws FormatLoaderException;

}

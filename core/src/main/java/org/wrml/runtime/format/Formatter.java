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
package org.wrml.runtime.format;

import org.wrml.model.Model;
import org.wrml.model.format.Format;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * Reads and writes {@link Model} data that is <i>serialized</i> using an associated {@link Format}.
 */
public interface Formatter
{

    /**
     * <p>
     * Configures the {@link Formatter} for the given context with the given settings.
     * </p>
     *
     * @param context The runtime {@link Context} that owns the {@link Formatter}.
     * @param config  The {@link Formatter}'s "custom" configuration.
     */
    void init(final Context context, FormatterConfiguration config);

    /**
     * The runtime {@link Context} that owns the {@link Formatter}.
     */
    Context getContext();

    /**
     * The {@link FormatterConfiguration} associated with this {@link Formatter}.
     *
     * @return The {@link FormatterConfiguration} associated with this {@link Formatter}.
     */
    FormatterConfiguration getConfig();

    /**
     * The id of the {@link Format} that's associated with this {@link Formatter}.
     */
    URI getFormatUri();

    /**
     * @return <code>true</code> iff this {@link Formatter} can be applied to format (read/write) {@link Model}
     *         instances of the the identified {@link Schema}.
     */
    boolean isApplicableTo(final URI schemaUri);

    /**
     * @return a {@link Model} with the indicated {@link Keys} and {@link Dimensions} from the specified{@link InputStream}.
     * @throws {@link ModelReadingException} if {@code in} cannot be deserialized to a {@code Model}.
     * @throws {@link UnsupportedOperationException} if the read operation is either never supported or is not supported for the <i>dimensioned</i> {@link Schema}.
     */
    <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException, UnsupportedOperationException;

    /**
     * Writes a {@link Model} with the indicated {@link ModelWriteOptions} to the specified {@link OutputStream}.
     *
     * @throws {@link ModelWritingException} if {@code model} cannot be serialized to the {@code out} {@link OutputStream}.
     * @throws {@link UnsupportedOperationException} if the write operation is either never supported or is not supported for the {@code model}'s {@link Schema}.
     */
    void writeModel(final OutputStream out, Model model, final ModelWriteOptions writeOptions) throws ModelWritingException, UnsupportedOperationException;

}

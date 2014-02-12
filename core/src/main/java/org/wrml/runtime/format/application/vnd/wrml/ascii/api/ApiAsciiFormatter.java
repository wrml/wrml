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
package org.wrml.runtime.format.application.vnd.wrml.ascii.api;

import org.wrml.model.Model;
import org.wrml.model.rest.Api;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.AbstractFormatter;
import org.wrml.runtime.format.ModelReadingException;
import org.wrml.runtime.format.ModelWriteOptions;
import org.wrml.runtime.format.ModelWritingException;
import org.wrml.runtime.rest.ApiBuilder;
import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.util.AsciiArt;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * Ascii art format for WRML REST APIs.
 */
public class ApiAsciiFormatter extends AbstractFormatter {

    public ApiAsciiFormatter() {

    }

    @Override
    public boolean isApplicableTo(final URI schemaUri) {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Prototype apiPrototype = schemaLoader.getPrototype(schemaLoader.getApiSchemaUri());
        return (apiPrototype.isAssignableFrom(schemaUri));
    }

    @Override
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException {

        throw new UnsupportedOperationException("The \"readModel\" operation is not supported by the \"" + getFormatUri() + "\" format.");
    }

    @Override
    public void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        if (!(model instanceof Api)) {
            throw new ModelWritingException("The \"" + getFormatUri() + "\" format supports "
                    + schemaLoader.getApiSchemaUri() + ", it cannot write the model.", null, this);
        }

        final Api api = (Api) model;
        final ApiBuilder apiBuilder = new ApiBuilder(api);
        final ApiNavigator apiNavigator = apiBuilder.navigate();

        final String asciiArtText = AsciiArt.express(apiNavigator);

        try {
            out.write(asciiArtText.getBytes(Charset.forName("UTF-8")));
        }
        catch (final Exception e) {
            throw new ModelWritingException(getClass().getSimpleName()
                    + " encounter an error while attempting to write an API (" + model + ").  Message: "
                    + e.getMessage(), null, this);

        }

    }

}

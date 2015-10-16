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
package org.wrml.runtime.format.application.vnd.wrml.wrmldoc;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.wrml.model.Model;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.*;
import org.wrml.runtime.format.application.vnd.wrml.complete.api.CompleteApiBuilder;
import org.wrml.runtime.format.application.vnd.wrml.complete.schema.CompleteSchemaBuilder;
import org.wrml.runtime.schema.SchemaLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

/**
 * Format for Wrmldoc JSON data.
 */
public class WrmldocDataFormatter extends AbstractFormatter {

    public static final String DOCROOT_SETTING_NAME = "docroot";

    private WrmldocDataBuilder _WrmldocDataBuilder;

    @Override
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException {
        throw new UnsupportedOperationException("The \"readModel\" operation is not supported by the \"" + getFormatUri() + "\" format.");
    }

    @Override
    public void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException {

        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            final ObjectNode wrmldocData = _WrmldocDataBuilder.buildWrmldocData(objectMapper, model);
            final ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());
            objectWriter.writeValue(out, wrmldocData);
        }
        catch (final Exception e) {
            throw new ModelWritingException(getClass().getSimpleName()
                    + " encounter an error while attempting to write wrmldoc data.  Message: " + e.getMessage(), null, this);

        }

    }

    @Override
    protected void initFromConfiguration(FormatterConfiguration config) {

        final Map<String, String> settings = config.getSettings();
        if (settings == null) {
            throw new NullPointerException("The settings cannot be null.");
        }

        String docroot = null;
        if (settings.containsKey(DOCROOT_SETTING_NAME)) {
            docroot = settings.get(DOCROOT_SETTING_NAME);
        }

        final CompleteSchemaBuilder completeSchemaBuilder = new CompleteSchemaBuilder();
        final CompleteApiBuilder completeApiBuilder = new CompleteApiBuilder(completeSchemaBuilder);
        _WrmldocDataBuilder = new WrmldocDataBuilder(completeApiBuilder, docroot);

    }
}

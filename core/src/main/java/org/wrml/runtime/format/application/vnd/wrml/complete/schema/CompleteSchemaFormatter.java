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
package org.wrml.runtime.format.application.vnd.wrml.complete.schema;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.wrml.model.Model;
import org.wrml.model.Named;
import org.wrml.model.Titled;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.rest.Method;
import org.wrml.model.schema.Schema;
import org.wrml.model.schema.ValueType;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.AbstractFormatter;
import org.wrml.runtime.format.ModelReadingException;
import org.wrml.runtime.format.ModelWriteOptions;
import org.wrml.runtime.format.ModelWritingException;
import org.wrml.runtime.rest.SystemLinkRelation;
import org.wrml.runtime.schema.*;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.util.UniqueName;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

/**
 * Format for complete WRML Schemas for use in tools/apps.
 */
public class CompleteSchemaFormatter extends AbstractFormatter {


    public CompleteSchemaFormatter() {

    }


    @Override
    public boolean isApplicableTo(final URI schemaUri) {

        final SchemaLoader schemaLoader = getContext().getSchemaLoader();
        return (schemaLoader.getSchemaSchemaUri().equals(schemaUri));
    }

    @Override
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException {

        throw new UnsupportedOperationException("The \"readModel\" operation is not supported by the \"" + getFormatUri() + "\" format.");
    }

    @Override
    public void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException {

        if (!(model instanceof Schema)) {
            throw new ModelWritingException("The \"" + getFormatUri() + "\" format cannot write the model.", null, this);
        }

        final Schema schema = (Schema) model;
        final CompleteSchemaBuilder completeSchemaBuilder = new CompleteSchemaBuilder();
        final ObjectMapper objectMapper = new ObjectMapper();

        try {
            final ObjectNode rootNode = completeSchemaBuilder.buildCompleteSchema(objectMapper, schema);
            final ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());
            objectWriter.writeValue(out, rootNode);
        }
        catch (final Exception e) {
            throw new ModelWritingException(getClass().getSimpleName()
                    + " encounter an error while attempting to write a complete Schema.  Message: " + e.getMessage(), null, this);

        }

    }


}

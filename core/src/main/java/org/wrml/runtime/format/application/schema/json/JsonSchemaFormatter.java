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
package org.wrml.runtime.format.application.schema.json;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.wrml.model.Model;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.AbstractFormatter;
import org.wrml.runtime.format.ModelReadingException;
import org.wrml.runtime.format.ModelWriteOptions;
import org.wrml.runtime.format.ModelWritingException;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;

import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.net.URI;

public class JsonSchemaFormatter extends AbstractFormatter
{


    public JsonSchemaFormatter()
    {

    }

    @Override
    public boolean isApplicableTo(final URI schemaUri)
    {

        if (schemaUri == null)
        {
            return false;
        }
        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Prototype schemaPrototype = schemaLoader.getPrototype(schemaLoader.getSchemaSchemaUri());
        return (schemaPrototype.isAssignableFrom(schemaUri));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException, UnsupportedOperationException
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final URI uri = rootModelKeys.getValue(schemaLoader.getDocumentSchemaUri());
        final URI schemaUri = rootModelDimensions.getSchemaUri();

        if (!isApplicableTo(schemaUri))
        {
            throw new UnsupportedOperationException(getClass().getSimpleName() + " can be used to read and write schemas only (" + schemaLoader.getSchemaSchemaUri() + ")");
        }

        final JsonSchemaLoader jsonSchemaLoader = schemaLoader.getJsonSchemaLoader();
        JsonSchema jsonSchema;
        Schema wrmlSchema = null;
        try
        {
            jsonSchema = jsonSchemaLoader.load(in, uri);
            wrmlSchema = schemaLoader.load(jsonSchema, schemaLoader.getDocumentSchemaUri());
            if (wrmlSchema == null)
            {
                throw new InvalidObjectException("Unable to deserialize InputStream to Model");
            }
        }
        catch (Exception e)
        {
            throw new ModelReadingException(getClass().getSimpleName()
                    + " encounter an error while attempting to read a JSON Schema (" + uri + ").  Message: "
                    + e.getMessage(), null, this);
        }

        return (M) wrmlSchema;
    }

    @Override
    public void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException, UnsupportedOperationException
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        if (!(model instanceof Schema))
        {
            throw new UnsupportedOperationException(getClass().getSimpleName() + " can be used to read and write schemas only (" + schemaLoader.getSchemaSchemaUri() + ")");
        }

        final Schema wrmlSchema = (Schema) model;
        final ObjectWriter objectWriter = new ObjectMapper().writer(new DefaultPrettyPrinter());
        final JsonSchema jsonSchema = schemaLoader.getJsonSchemaLoader().load(wrmlSchema);

        try
        {
            objectWriter.writeValue(out, jsonSchema.getRootNode());
        }
        catch (final Exception e)
        {
            throw new ModelWritingException(getClass().getSimpleName()
                    + " encounter an error while attempting to write a JSON Schema (" + model + ").  Message: "
                    + e.getMessage(), null, this);

        }

    }
}

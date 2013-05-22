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

package org.wrml.runtime.schema;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.format.Format;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.schema.Choices;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.ContextTest;
import org.wrml.runtime.DimensionsBuilder;
import org.wrml.runtime.format.application.schema.json.JsonSchema;
import org.wrml.util.UniqueName;

import java.io.IOException;
import java.net.URI;
import java.util.SortedSet;

public class SchemaLoaderTest
{

    private static final Logger logger = LoggerFactory.getLogger(SchemaLoaderTest.class);

    private SchemaLoader _SchemaLoader;

    @Before
    public void setUp() throws Exception
    {
        _SchemaLoader = ContextTest.createTestContext().getSchemaLoader();
    }

    @After
    public void tearDown() throws Exception
    {

        _SchemaLoader = null;
    }

    @Test(expected = SchemaLoaderException.class)
    public void initParamContextNullFailure()
    {

        final SchemaLoader schemaLoader = new DefaultSchemaLoader();
        schemaLoader.init(null);
    }

    @Test
    public void schemaLoaderNotNull()
    {

        Assert.assertNotNull(_SchemaLoader);
    }

    @Test
    public void contextNotNull()
    {

        Assert.assertNotNull(_SchemaLoader.getContext());
    }

    @Test
    public void jsonSchemaLoaderNotNull()
    {

        Assert.assertNotNull(_SchemaLoader.getJsonSchemaLoader());
    }

    @Test
    public void getLoadedSchemaUrisNotNull()
    {

        Assert.assertNotNull(_SchemaLoader.getLoadedSchemaUris());
    }

    @Test
    public void getLoadedSchemaUrisIsEmpty()
    {

        Assert.assertTrue(_SchemaLoader.getLoadedSchemaUris().isEmpty());
    }

    @Test
    public void getPrototypedSchemaUrisNotNull()
    {

        Assert.assertNotNull(_SchemaLoader.getPrototypedSchemaUris());
    }

    @Test
    public void getPrototypedSchemaUrisNotEmpty()
    {

        Assert.assertTrue(!_SchemaLoader.getPrototypedSchemaUris().isEmpty());
    }

    @Test
    public void getPrototypedSchemaUrisNotLying()
    {

        final SortedSet<URI> prototypedSchemaUris = _SchemaLoader.getPrototypedSchemaUris();
        for (final URI schemaUri : prototypedSchemaUris)
        {
            final Prototype prototype = _SchemaLoader.getPrototype(schemaUri);
            Assert.assertNotNull(prototype);
        }
    }

    @Test
    public void loadPrototypedSchemas()
    {

        final SortedSet<URI> prototypedSchemaUris = _SchemaLoader.getPrototypedSchemaUris();
        for (final URI schemaUri : prototypedSchemaUris)
        {
            final Schema schema = _SchemaLoader.load(schemaUri);
            Assert.assertNotNull(schema);
        }

        Assert.assertEquals(prototypedSchemaUris, _SchemaLoader.getLoadedSchemaUris());
    }

    @Test
    public void getDocumentSchemaUri()
    {

        Assert.assertEquals(_SchemaLoader.getDocumentSchemaUri(), _SchemaLoader.getTypeUri(Document.class));
        Assert.assertTrue(_SchemaLoader.getDocumentSchemaUri() == _SchemaLoader.getDocumentSchemaUri());
    }

    @Test
    public void getSchemaSchemaUri()
    {

        Assert.assertEquals(_SchemaLoader.getSchemaSchemaUri(), _SchemaLoader.getTypeUri(Schema.class));
        Assert.assertTrue(_SchemaLoader.getSchemaSchemaUri() == _SchemaLoader.getSchemaSchemaUri());
    }

    @Test
    public void getSchemaDimensions()
    {

        Assert.assertEquals(_SchemaLoader.getSchemaDimensions(), new DimensionsBuilder(_SchemaLoader.getTypeUri(Schema.class)).toDimensions());
        Assert.assertTrue(_SchemaLoader.getSchemaDimensions().getSchemaUri() == _SchemaLoader.getSchemaSchemaUri());
        Assert.assertTrue(_SchemaLoader.getSchemaDimensions() == _SchemaLoader.getSchemaDimensions());
    }

    @Test
    public void getLinkRelationSchemaUri()
    {

        Assert.assertEquals(_SchemaLoader.getLinkRelationSchemaUri(), _SchemaLoader.getTypeUri(LinkRelation.class));
        Assert.assertTrue(_SchemaLoader.getLinkRelationSchemaUri() == _SchemaLoader.getLinkRelationSchemaUri());
    }

    @Test
    public void getLinkRelationDimensions()
    {
        Assert.assertEquals(_SchemaLoader.getLinkRelationDimensions(), new DimensionsBuilder(_SchemaLoader.getTypeUri(LinkRelation.class)).toDimensions());
        Assert.assertTrue(_SchemaLoader.getLinkRelationDimensions().getSchemaUri() == _SchemaLoader.getLinkRelationSchemaUri());
        Assert.assertTrue(_SchemaLoader.getLinkRelationDimensions() == _SchemaLoader.getLinkRelationDimensions());
    }

    @Test
    public void getApiSchemaUri()
    {

        Assert.assertEquals(_SchemaLoader.getApiSchemaUri(), _SchemaLoader.getTypeUri(Api.class));
        Assert.assertTrue(_SchemaLoader.getApiSchemaUri() == _SchemaLoader.getApiSchemaUri());
    }

    @Test
    public void getApiDimensions()
    {

        Assert.assertEquals(_SchemaLoader.getApiDimensions(), new DimensionsBuilder(_SchemaLoader.getTypeUri(Api.class)).toDimensions());
        Assert.assertTrue(_SchemaLoader.getApiDimensions().getSchemaUri() == _SchemaLoader.getApiSchemaUri());
        Assert.assertTrue(_SchemaLoader.getApiDimensions() == _SchemaLoader.getApiDimensions());
    }

    @Test
    public void getFormatSchemaUri()
    {

        Assert.assertEquals(_SchemaLoader.getFormatSchemaUri(), _SchemaLoader.getTypeUri(Format.class));
        Assert.assertTrue(_SchemaLoader.getFormatSchemaUri() == _SchemaLoader.getFormatSchemaUri());
    }

    @Test
    public void getFormatDimensions()
    {

        Assert.assertEquals(_SchemaLoader.getFormatDimensions(), new DimensionsBuilder(_SchemaLoader.getTypeUri(Format.class)).toDimensions());
        Assert.assertTrue(_SchemaLoader.getFormatDimensions().getSchemaUri() == _SchemaLoader.getFormatSchemaUri());
        Assert.assertTrue(_SchemaLoader.getFormatDimensions() == _SchemaLoader.getFormatDimensions());
    }

    @Test
    public void loadWrmlSchemaA() throws IOException
    {

        final Schema schema = ContextTest.getModelResource(_SchemaLoader.getContext(), ContextTest.WRML_SCHEMA_A_URI, _SchemaLoader.getSchemaDimensions());
        Assert.assertNotNull(schema);
    }

    @Test
    public void convertWrmlSchemaToJsonSchemaA() throws IOException
    {

        final Schema schema = ContextTest.getModelResource(_SchemaLoader.getContext(), ContextTest.WRML_SCHEMA_A_URI, _SchemaLoader.getSchemaDimensions());
        final JsonSchema jsonSchema = _SchemaLoader.getJsonSchemaLoader().load(schema);
        Assert.assertNotNull(jsonSchema);

    }

    @Test
    public void loadWrmlSchemaB() throws IOException
    {

        final Schema schema = ContextTest.getModelResource(_SchemaLoader.getContext(), ContextTest.WRML_SCHEMA_B_URI, _SchemaLoader.getSchemaDimensions());
        Assert.assertNotNull(schema);
    }

    @Test
    public void convertWrmlSchemaToJsonSchemaB() throws IOException
    {

        final Schema schema = ContextTest.getModelResource(_SchemaLoader.getContext(), ContextTest.WRML_SCHEMA_B_URI, _SchemaLoader.getSchemaDimensions());
        final JsonSchema jsonSchema = _SchemaLoader.getJsonSchemaLoader().load(schema);
        Assert.assertNotNull(jsonSchema);

    }

    @Test
    public void loadWrmlSchemaC() throws IOException
    {

        final Schema schema = ContextTest.getModelResource(_SchemaLoader.getContext(), ContextTest.WRML_SCHEMA_C_URI, _SchemaLoader.getSchemaDimensions());
        Assert.assertNotNull(schema);
    }

    @Test
    public void convertWrmlSchemaToJsonSchemaC() throws IOException
    {

        final Schema schema = ContextTest.getModelResource(_SchemaLoader.getContext(), ContextTest.WRML_SCHEMA_C_URI, _SchemaLoader.getSchemaDimensions());
        final JsonSchema jsonSchema = _SchemaLoader.getJsonSchemaLoader().load(schema);
        Assert.assertNotNull(jsonSchema);

    }

    @Test
    public void loadWrmlChoicesWrmlLetters() throws IOException
    {

        final Choices choices = ContextTest.getModelResource(_SchemaLoader.getContext(), ContextTest.WRML_CHOICES_WRML_LETTERS_URI, _SchemaLoader.getChoicesDimensions());
        Assert.assertNotNull(choices);
    }

    @Test
    public void loadWrmlChoicesAlphabet() throws IOException
    {

        final Choices choices = ContextTest.getModelResource(_SchemaLoader.getContext(), ContextTest.WRML_CHOICES_ALPHABET_URI, _SchemaLoader.getChoicesDimensions());
        Assert.assertNotNull(choices);
    }

    @Ignore
    @Test
    public void getSchemaSubsystemSchemaNames() throws IOException
    {

        final String packageName = Schema.class.getPackage().getName();
        final UniqueName namespace = new UniqueName(packageName.replace('.', UniqueName.NAME_SEPARATOR_CHAR));
        logger.debug("Listing schema names within: {}", namespace);
        final SortedSet<UniqueName> schemaNames = _SchemaLoader.getSchemaNames(namespace);

        Assert.assertTrue(!schemaNames.isEmpty());

        for (final UniqueName schemaName : schemaNames)
        {
            logger.debug("Schema subsystem model: {}", schemaName);
        }
    }

    @Ignore
    @Test
    public void getRestSubsystemSchemaNames() throws IOException
    {

        final String packageName = Api.class.getPackage().getName();
        final UniqueName namespace = new UniqueName(packageName.replace('.', UniqueName.NAME_SEPARATOR_CHAR));
        logger.debug("Listing schema names within: {}", namespace);
        final SortedSet<UniqueName> schemaNames = _SchemaLoader.getSchemaNames(namespace);

        // TODO: Need to re-enable listing schemas from memory

        // Assert.assertTrue(!schemaNames.isEmpty());

        for (final UniqueName schemaName : schemaNames)
        {
            logger.debug("REST subsystem model: " + schemaName);
        }
    }

    @Ignore
    @Test
    public void getNoSchemaNames() throws IOException
    {

        final UniqueName namespace = new UniqueName("org/wrml/noschemas");
        logger.debug("Listing schema names within: {}", namespace);
        final SortedSet<UniqueName> schemaNames = _SchemaLoader.getSchemaNames(namespace);

        Assert.assertTrue(schemaNames.isEmpty());
    }

    @Ignore
    @Test
    public void getTestNamespaceSchemaNames() throws IOException, ClassNotFoundException
    {

        final Context context = _SchemaLoader.getContext();
        final Schema schema = ContextTest.getModelResource(context, ContextTest.WRML_SCHEMA_SCREEN_URI, _SchemaLoader.getSchemaDimensions());
        context.newModel(schema.getUri());
        final UniqueName namespace = new UniqueName(schema.getUniqueName().getNamespace());
        logger.debug("Listing schema names within: {}", namespace);
        final SortedSet<UniqueName> schemaNames = _SchemaLoader.getSchemaNames(namespace);

        // TODO: Need to re-enable listing schemas from memory

        // Assert.assertTrue(!schemaNames.isEmpty());

        for (final UniqueName schemaName : schemaNames)
        {
            logger.debug("Test models: {}", schemaName);
        }
    }

    @Ignore
    @Test
    public void getWrmlNamespaceSchemaSubnamespaces() throws IOException, ClassNotFoundException
    {

        final Context context = _SchemaLoader.getContext();
        context.newModel(ContextTest.WRML_SCHEMA_SCREEN_URI);

        final UniqueName namespace = new UniqueName("org/wrml");
        logger.debug("Listing schema subnamespaces within: {}", namespace);
        final SortedSet<UniqueName> subnamespaces = _SchemaLoader.getSchemaSubnamespaces(namespace);

        Assert.assertTrue(!subnamespaces.isEmpty());

        for (final UniqueName subnamespace : subnamespaces)
        {
            logger.debug("org/wrml subnamespace: {}", subnamespace);
        }
    }

}

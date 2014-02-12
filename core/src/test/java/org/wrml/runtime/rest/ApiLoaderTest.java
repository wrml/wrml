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
package org.wrml.runtime.rest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.rest.*;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.*;
import org.wrml.runtime.format.application.schema.json.JsonSchema;
import org.wrml.runtime.schema.SchemaLoader;

import java.io.IOException;
import java.net.URI;

public class ApiLoaderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiLoaderTest.class);

    public static final String TEST_WIZARD_RESOURCE_PATH = "/org/wrml/test/wizard/";

    public static final URI WRML_SCHEMA_WIZARD_URI = SystemApi.Schema.getUri().resolve(TEST_WIZARD_RESOURCE_PATH + "Wizard");

    public static final URI WRML_API_WIZARD_EXAMPLE_URI = URI.create("http://wizard.example.api.wrml.org");

    public static final URI WRML_MERLIN_URI = WRML_API_WIZARD_EXAMPLE_URI.resolve("/wizards/merlin?rg=42");

    private ApiLoader _ApiLoader;

    @Before
    public void setUp() throws Exception {

        final Engine engine = EngineTest.createTestEngine();
        _ApiLoader = engine.getContext().getApiLoader();

    }

    @After
    public void tearDown() throws Exception {

        _ApiLoader = null;
    }

    @Test(expected = ApiLoaderException.class)
    public void initParamContextNullFailure() {

        final ApiLoader apiLoader = new DefaultApiLoader();
        apiLoader.init(null);
    }

    @Test
    public void apiLoaderNotNull() {

        Assert.assertNotNull(_ApiLoader);
    }

    @Test
    public void contextNotNull() {

        Assert.assertNotNull(_ApiLoader.getContext());
    }

    @Test
    public void loadWrmlSchemaScreen() throws IOException {

        final Context context = _ApiLoader.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Schema schema = ContextTest.getModelResource(context, ContextTest.WRML_SCHEMA_SCREEN_URI, schemaLoader.getSchemaDimensions());
        Assert.assertNotNull(schema);
    }

    @Test
    public void loadScreenApiAndGetAggregate() throws IOException {

        // TODO: Break this up into several tests

        final Context context = _ApiLoader.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final Schema aSchema = ContextTest.getModelResource(context, ContextTest.WRML_SCHEMA_A_URI, schemaLoader.getSchemaDimensions());
        final Schema bSchema = ContextTest.getModelResource(context, ContextTest.WRML_SCHEMA_B_URI, schemaLoader.getSchemaDimensions());
        final Schema cSchema = ContextTest.getModelResource(context, ContextTest.WRML_SCHEMA_C_URI, schemaLoader.getSchemaDimensions());

        final LinkRelation a = ContextTest.getModelResource(context, ContextTest.WRML_RELATION_A_URI, schemaLoader.getLinkRelationDimensions());
        Assert.assertNotNull(a);
        final LinkRelation b = ContextTest.getModelResource(context, ContextTest.WRML_RELATION_B_URI, schemaLoader.getLinkRelationDimensions());
        Assert.assertNotNull(b);
        final LinkRelation c = ContextTest.getModelResource(context, ContextTest.WRML_RELATION_C_URI, schemaLoader.getLinkRelationDimensions());
        Assert.assertNotNull(c);

        final Schema aggregateSchema = ContextTest.getModelResource(context, ContextTest.WRML_SCHEMA_SCREEN_URI, schemaLoader.getSchemaDimensions());
        Assert.assertNotNull(aggregateSchema);
        final JsonSchema jsonSchema = schemaLoader.getJsonSchemaLoader().load(aggregateSchema);
        Assert.assertNotNull(jsonSchema);

        final Api api = ContextTest.getModelResource(context, ContextTest.WRML_API_SCREEN_EXAMPLE_URI, schemaLoader.getApiDimensions());
        final ApiNavigator apiNavigator = _ApiLoader.loadApi(api);
        Assert.assertNotNull(apiNavigator);
        Assert.assertNotNull(apiNavigator.toString());

        Assert.assertEquals(api, _ApiLoader.loadApi(ContextTest.WRML_API_SCREEN_EXAMPLE_URI).getApi());

        final Keys aggregateKeys = _ApiLoader.buildDocumentKeys(ContextTest.WRML_SCREEN_AGGREGATE_URI, aggregateSchema.getUri());
        final Dimensions aggregateDimensions = new DimensionsBuilder().setSchemaUri(aggregateSchema.getUri()).toDimensions();
        final AggregateDocument aggregate = context.getModel(aggregateKeys, aggregateDimensions);

        final Link aLink = (Link) aggregate.getSlotValue("a");
        Assert.assertNotNull(aLink.getDoc());

        final Link bLink = (Link) aggregate.getSlotValue("b");
        Assert.assertNotNull(bLink.getDoc());

        final Link cLink = (Link) aggregate.getSlotValue("c");
        Assert.assertNotNull(cLink.getDoc());

        Assert.assertNotNull(aggregate.reference("a"));
        Assert.assertNotNull(aggregate.reference("b"));
        Assert.assertNotNull(aggregate.reference("c"));
    }

    @Test
    public void loadWizardApiAndGetMerlin() throws IOException {

        final Context context = _ApiLoader.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final Api api = ContextTest.getModelResource(context, WRML_API_WIZARD_EXAMPLE_URI, schemaLoader.getApiDimensions());
        final ApiNavigator apiNavigator = _ApiLoader.loadApi(api);
        Assert.assertNotNull(apiNavigator);

        final Keys merlinKeys = _ApiLoader.buildDocumentKeys(WRML_MERLIN_URI, WRML_SCHEMA_WIZARD_URI);
        final Dimensions wizardDimensions = _ApiLoader.buildDocumentDimensions(Method.Get, WRML_MERLIN_URI, new DimensionsBuilder().setSchemaUri(WRML_SCHEMA_WIZARD_URI));
        final Document merlin = context.getModel(merlinKeys, wizardDimensions);

        LOGGER.debug("Merlin, the Wizard: {}", merlin);

        final Document primarySpell = merlin.reference("primarySpell");
        final Document secondarySpell = merlin.reference("secondarySpell");

        Assert.assertNotNull(primarySpell);
        Assert.assertNotNull(secondarySpell);

        LOGGER.debug("Merlin's primary Spell: {}", primarySpell);
        LOGGER.debug("Merlin's secondary Spell: {}", secondarySpell);

        final Document guild = merlin.reference("guild");
        LOGGER.debug("Merlin's Guild: {}", guild);

    }

    // TODO: Add more tests

}

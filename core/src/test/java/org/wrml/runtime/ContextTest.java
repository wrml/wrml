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
package org.wrml.runtime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.wrml.model.Model;
import org.wrml.model.format.Format;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Link;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.rest.LinkTemplate;
import org.wrml.model.rest.ResourceTemplate;
import org.wrml.model.schema.BooleanValue;
import org.wrml.model.schema.Choices;
import org.wrml.model.schema.DateValue;
import org.wrml.model.schema.DoubleValue;
import org.wrml.model.schema.IntegerValue;
import org.wrml.model.schema.ListValue;
import org.wrml.model.schema.LongValue;
import org.wrml.model.schema.ModelValue;
import org.wrml.model.schema.Schema;
import org.wrml.model.schema.SingleSelectValue;
import org.wrml.model.schema.Slot;
import org.wrml.model.schema.Syntax;
import org.wrml.model.schema.TextValue;
import org.wrml.model.schema.Value;
import org.wrml.runtime.format.SystemFormat;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.runtime.rest.Resource;
import org.wrml.runtime.rest.ResourceTest;
import org.wrml.runtime.rest.SystemApi;
import org.wrml.runtime.rest.SystemLinkRelation;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.syntax.SystemSyntax;

import java.io.IOException;
import java.net.URI;

import junit.framework.TestCase;

@RunWith(BlockJUnit4ClassRunner.class)
public class ContextTest extends TestCase
{

    public static final String TEST_RESOURCE_PATH = "/org/wrml/test/";

    public static final URI WRML_SCHEMA_SCREEN_URI = SystemApi.Schema.getUri().resolve(TEST_RESOURCE_PATH + "Screen");

    public static final URI WRML_RELATION_A_URI = SystemApi.LinkRelation.getUri().resolve(TEST_RESOURCE_PATH + "a");

    public static final URI WRML_RELATION_B_URI = SystemApi.LinkRelation.getUri().resolve(TEST_RESOURCE_PATH + "b");

    public static final URI WRML_RELATION_C_URI = SystemApi.LinkRelation.getUri().resolve(TEST_RESOURCE_PATH + "c");

    public static final URI WRML_API_SCREEN_EXAMPLE_URI = URI.create("http://screen.example.api.wrml.org");

    public static final URI WRML_SCREEN_AGGREGATE_URI = WRML_API_SCREEN_EXAMPLE_URI.resolve("/screens/1/2/3");

    public static final URI WRML_SCHEMA_A_URI = SystemApi.Schema.getUri().resolve(TEST_RESOURCE_PATH + "A");

    public static final URI WRML_SCHEMA_B_URI = SystemApi.Schema.getUri().resolve(TEST_RESOURCE_PATH + "B");

    public static final URI WRML_SCHEMA_C_URI = SystemApi.Schema.getUri().resolve(TEST_RESOURCE_PATH + "C");

    public static final URI WRML_CHOICES_WRML_LETTERS_URI = SystemApi.Choices.getUri().resolve(TEST_RESOURCE_PATH + "WrmlLetters");

    public static final URI WRML_CHOICES_ALPHABET_URI = SystemApi.Choices.getUri().resolve(TEST_RESOURCE_PATH + "Alphabet");

    private Context _Context;

    public static <M extends Model> M getModelResource(final Context context, final URI uri, final Dimensions dimensions) throws IOException
    {
        final ApiLoader apiLoader = context.getApiLoader();
        final Keys keys = apiLoader.buildDocumentKeys(uri, dimensions.getSchemaUri());
        return context.getModel(keys, dimensions);
    }

    /**
     * A factory method intended to provide a test {@link Context} to other {@code *Test} classes for integration tests.
     * 
     * @return a {@link Context} (via {@link EngineTest#createTestEngine()}) for integration tests.
     * @throws IOException
     */
    public static Context createTestContext() throws IOException
    {
        return EngineTest.createTestEngine().getContext();
    }

    @Before
    @Override
    public void setUp() throws Exception
    {
        _Context = createTestContext();
    }

    @After
    @Override
    public void tearDown() throws Exception
    {
        _Context = null;
    }

    @Test(expected = ContextException.class)
    public void initParamConfigNullFailure()
    {

        final Context context = new DefaultContext();
        context.init(null);
    }

    @Test
    public void contextNotNull()
    {

        assertNotNull(_Context);
    }

    @Test
    public void configNotNull()
    {

        assertNotNull(_Context.getConfig());
    }

    @Test
    public void apiLoaderNotNull()
    {

        assertNotNull(_Context.getApiLoader());
    }

    @Test
    public void formatLoaderNotNull()
    {

        assertNotNull(_Context.getFormatLoader());
    }

    @Test
    public void modelBuilderNotNull()
    {

        assertNotNull(_Context.getModelBuilder());
    }

    @Test
    public void schemaLoaderNotNull()
    {

        assertNotNull(_Context.getSchemaLoader());
    }

    @Test
    public void serviceLoaderNotNull()
    {

        assertNotNull(_Context.getServiceLoader());
    }

    @Test
    public void syntaxLoaderNotNull()
    {

        assertNotNull(_Context.getSyntaxLoader());
    }

    @Test
    public void cacheIsNull()
    {

        assertNull(_Context.getModelCache());
    }

    @Test
    public void newModelSyntaxNotNull()
    {

        assertNotNull(_Context.newModel(Syntax.class));
    }

    @Test
    public void newModelSchemaNotNull()
    {

        assertNotNull(_Context.newModel(Schema.class));
    }

    @Test
    public void newModelSlotNotNull()
    {

        assertNotNull(_Context.newModel(Slot.class));
    }

    @Test
    public void newModelTextValueNotNull()
    {

        assertNotNull(_Context.newModel(TextValue.class));
    }

    @Test
    public void newModelBooleanValueNotNull()
    {

        assertNotNull(_Context.newModel(BooleanValue.class));
    }

    @Test
    public void newModelIntegerValueNotNull()
    {

        assertNotNull(_Context.newModel(IntegerValue.class));
    }

    @Test
    public void newModelModelValueNotNull()
    {

        assertNotNull(_Context.newModel(ModelValue.class));
    }

    @Test
    public void newModelListValueNotNull()
    {

        assertNotNull(_Context.newModel(ListValue.class));
    }

    @Test
    public void newModelDateValueNotNull()
    {

        assertNotNull(_Context.newModel(DateValue.class));
    }

    @Test
    public void newModelDoubleValueNotNull()
    {

        assertNotNull(_Context.newModel(DoubleValue.class));
    }

    @Test
    public void newModelLongValueNotNull()
    {

        assertNotNull(_Context.newModel(LongValue.class));
    }

    @Test
    public void newModelSingleSelectValueNotNull()
    {

        assertNotNull(_Context.newModel(SingleSelectValue.class));
    }

    @Test
    public void newModelChoicesNotNull()
    {

        assertNotNull(_Context.newModel(Choices.class));
    }

    @Test(expected = ModelBuilderException.class)
    public void newModelValueAbstractFailure()
    {
        // Value directly extends Abstract (marking it as an "abstract" interface), which means that it is illegal to create new instances.
        _Context.newModel(Value.class);
    }

    @Test
    public void newModelApiNotNull()
    {

        assertNotNull(_Context.newModel(Api.class));
    }

    @Test
    public void newModelLinkRelationNotNull()
    {

        assertNotNull(_Context.newModel(LinkRelation.class));
    }

    @Test
    public void newModelLinkTemplateNotNull()
    {

        assertNotNull(_Context.newModel(LinkTemplate.class));
    }

    @Test
    public void newModelResourceTemplateNotNull()
    {

        assertNotNull(_Context.newModel(ResourceTemplate.class));
    }

    @Test
    public void newModelLinkNotNull()
    {

        assertNotNull(_Context.newModel(Link.class));
    }

    @Test
    public void newModelFormatNotNull()
    {

        assertNotNull(_Context.newModel(Format.class));
    }

    @Test
    public void getModelSystemSchemaSchema()
    {

        final SchemaLoader schemaLoader = _Context.getSchemaLoader();
        final URI uri = schemaLoader.getSchemaSchemaUri();
        final Keys keys = new KeysBuilder().addKey(schemaLoader.getDocumentSchemaUri(), uri).toKeys();
        final Dimensions dimensions = schemaLoader.getSchemaDimensions();

        assertNotNull(_Context.getModel(keys, dimensions));
        assertTrue(_Context.getModel(keys, dimensions) instanceof Schema);
        assertEquals(_Context.getModel(keys, dimensions), _Context.getModel(keys, dimensions));
        assertTrue(_Context.getModel(keys, dimensions) == _Context.getModel(keys, dimensions));
        assertEquals(_Context.getModel(keys, dimensions).getSchemaUri(), dimensions.getSchemaUri());
        assertEquals(((Document) _Context.getModel(keys, dimensions)).getUri(), uri);

        assertNotNull(_Context.getModel(keys, dimensions).toString());
        assertNotNull(_Context.getModel(keys, dimensions).getKeys());
    }

    @Test
    public void getModelSystemLinkRelationApi()
    {

        final SchemaLoader schemaLoader = _Context.getSchemaLoader();
        final URI uri = SystemApi.LinkRelation.getUri();
        final Keys keys = new KeysBuilder().addKey(schemaLoader.getDocumentSchemaUri(), uri).toKeys();
        final Dimensions dimensions = schemaLoader.getApiDimensions();

        assertNotNull(_Context.getModel(keys, dimensions));
        assertTrue(_Context.getModel(keys, dimensions) instanceof Api);
        assertEquals(_Context.getModel(keys, dimensions), _Context.getModel(keys, dimensions));
        assertTrue(_Context.getModel(keys, dimensions) == _Context.getModel(keys, dimensions));
        assertEquals(_Context.getModel(keys, dimensions).getSchemaUri(), dimensions.getSchemaUri());
        assertEquals(((Document) _Context.getModel(keys, dimensions)).getUri(), uri);

        assertNotNull(_Context.getModel(keys, dimensions).toString());
        assertNotNull(_Context.getModel(keys, dimensions).getKeys());

    }

    @Test
    public void getModelSystemFormatJson()
    {

        final SchemaLoader schemaLoader = _Context.getSchemaLoader();
        final URI uri = SystemFormat.json.getFormatUri();
        final Keys keys = new KeysBuilder().addKey(schemaLoader.getDocumentSchemaUri(), uri).toKeys();
        final Dimensions dimensions = schemaLoader.getFormatDimensions();

        assertNotNull(_Context.getModel(keys, dimensions));
        assertTrue(_Context.getModel(keys, dimensions) instanceof Format);
        assertEquals(_Context.getModel(keys, dimensions), _Context.getModel(keys, dimensions));
        assertTrue(_Context.getModel(keys, dimensions) == _Context.getModel(keys, dimensions));
        assertEquals(_Context.getModel(keys, dimensions).getSchemaUri(), dimensions.getSchemaUri());
        assertEquals(((Document) _Context.getModel(keys, dimensions)).getUri(), uri);

        assertNotNull(_Context.getModel(keys, dimensions).toString());
        assertNotNull(_Context.getModel(keys, dimensions).getKeys());

    }

    // TODO: Make this work
    @Test
    public void getModelSystemSyntaxUuid()
    {

        final SchemaLoader schemaLoader = _Context.getSchemaLoader();
        final URI uri = SystemSyntax.UUID.getSyntaxUri();
        final Keys keys = new KeysBuilder().addKey(schemaLoader.getDocumentSchemaUri(), uri).toKeys();
        final Dimensions dimensions = new DimensionsBuilder().setSchemaUri(schemaLoader.getTypeUri(Syntax.class)).toDimensions();

        assertNotNull(_Context.getModel(keys, dimensions));
        assertTrue(_Context.getModel(keys, dimensions) instanceof Syntax);
        assertEquals(_Context.getModel(keys, dimensions), _Context.getModel(keys, dimensions));
        assertTrue(_Context.getModel(keys, dimensions) == _Context.getModel(keys, dimensions));
        assertEquals(_Context.getModel(keys, dimensions).getSchemaUri(), dimensions.getSchemaUri());
        assertEquals(((Document) _Context.getModel(keys, dimensions)).getUri(), uri);

        assertNotNull(_Context.getModel(keys, dimensions).toString());
        assertNotNull(_Context.getModel(keys, dimensions).getKeys());

    }

    @Test
    public void getModelSystemLinkRelationSelf()
    {

        final SchemaLoader schemaLoader = _Context.getSchemaLoader();
        final URI uri = SystemLinkRelation.self.getUri();
        final Keys keys = new KeysBuilder().addKey(schemaLoader.getDocumentSchemaUri(), uri).toKeys();
        final Dimensions dimensions = schemaLoader.getLinkRelationDimensions();

        assertNotNull(_Context.getModel(keys, dimensions));
        assertTrue(_Context.getModel(keys, dimensions) instanceof LinkRelation);
        assertEquals(_Context.getModel(keys, dimensions), _Context.getModel(keys, dimensions));
        assertTrue(_Context.getModel(keys, dimensions) == _Context.getModel(keys, dimensions));
        assertEquals(_Context.getModel(keys, dimensions).getSchemaUri(), dimensions.getSchemaUri());
        assertEquals(((Document) _Context.getModel(keys, dimensions)).getUri(), uri);

        assertNotNull(_Context.getModel(keys, dimensions).toString());
        assertNotNull(_Context.getModel(keys, dimensions).getKeys());

    }

    @Test(expected = ContextException.class)
    public void testOptionsModelNull()
    {
        _Context.optionsModel(null);
        fail("expected ContextException");
    }

    /**
     * Test for {@link Context#optionsModel(Model)}.
     * <p>
     * Without the call to {@link #createTestContext()}, this could be a self-contained unit test.
     */
    @Test
    public void testOptionsModel() throws Exception
    {
        DefaultContext context = (DefaultContext) createTestContext();
        ApiLoader mockApiLoader = mock(ApiLoader.class);
        ApiNavigator mockApiNavigator = mock(ApiNavigator.class);
        Model mockModel = mock(Model.class);
        Resource mockResource = ResourceTest.getMock(mockApiNavigator, null, null);
        URI mockUri = URI.create("mock");

        when(mockApiLoader.getParentApiNavigator(mockUri)).thenReturn(mockApiNavigator);
        when(mockApiNavigator.getResource(mockUri)).thenReturn(mockResource);
        when(mockModel.getSchemaUri()).thenReturn(mockUri);

        context._ApiLoader = mockApiLoader;

        Model result = context.optionsModel(mockModel);
        assertNotNull(result);
    }

    @Test
    @Ignore
    public void testOptionsNodeLevel()
    {
        fail("TODO: WRML-484"); // TODO: WRML-484
    }

    @Test
    @Ignore
    public void testOptionsRootLevel()
    {
        fail("TODO: WRML-483"); // TODO: WRML-483
    }

}

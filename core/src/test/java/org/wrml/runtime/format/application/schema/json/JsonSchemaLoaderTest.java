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

package org.wrml.runtime.format.application.schema.json;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Engine;
import org.wrml.runtime.EngineTest;
import org.wrml.runtime.rest.SystemApi;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import org.wrml.model.schema.Slot;
import org.wrml.model.schema.Value;
import org.wrml.runtime.format.application.schema.json.JsonSchema.Definitions.JsonType;
import org.wrml.runtime.format.application.schema.json.JsonSchema.Definitions.PropertyType;
import org.wrml.runtime.format.application.schema.json.JsonSchema.Property;

//TODO: move to integration test module
public class JsonSchemaLoaderTest
{

    public static final String TEST_RESOURCE_PACKAGE = "resources/test/";

    public static final String TEST_RESOURCE_NAMESPACE = "org/wrml/test/";

    public static final String JSON_SCHEMA_PERSON_UNIQUE_NAME = TEST_RESOURCE_NAMESPACE + "Person";

    public static final String JSON_SCHEMA_PERSON_RESOURCE_NAME = TEST_RESOURCE_PACKAGE + JSON_SCHEMA_PERSON_UNIQUE_NAME;

    public static final URI JSON_SCHEMA_PERSON_URI = SystemApi.Schema.getUri().resolve("/" + JSON_SCHEMA_PERSON_UNIQUE_NAME);

    public static final String JSON_SCHEMA_IMPORTANT_PERSON_UNIQUE_NAME = TEST_RESOURCE_NAMESPACE + "ImportantPerson";

    public static final String JSON_SCHEMA_IMPORTANT_PERSON_RESOURCE_NAME = TEST_RESOURCE_PACKAGE + JSON_SCHEMA_IMPORTANT_PERSON_UNIQUE_NAME;

    public static final URI JSON_SCHEMA_IMPORTANT_PERSON_URI = SystemApi.Schema.getUri().resolve("/" + JSON_SCHEMA_IMPORTANT_PERSON_UNIQUE_NAME);

    public static final String JSON_SCHEMA_ADDRESSABLE_PERSON_UNIQUE_NAME = TEST_RESOURCE_NAMESPACE + "AddressablePerson";

    public static final String JSON_SCHEMA_ADDRESSABLE_PERSON_RESOURCE_NAME = TEST_RESOURCE_PACKAGE + JSON_SCHEMA_ADDRESSABLE_PERSON_UNIQUE_NAME;

    public static final URI JSON_SCHEMA_ADDRESSABLE_PERSON_URI = SystemApi.Schema.getUri().resolve("/" + JSON_SCHEMA_ADDRESSABLE_PERSON_UNIQUE_NAME);
    
    public static final String JSON_SCHEMA_ADDRESSABLE_PERSON_PROPERTY_NAME = "mailable";

    public static final String JSON_SCHEMA_BUSINESS_ADDRESS_UNIQUE_NAME = TEST_RESOURCE_NAMESPACE + "BusinessAddress";

    public static final String JSON_SCHEMA_BUSINESS_ADDRESS_RESOURCE_NAME = TEST_RESOURCE_PACKAGE + JSON_SCHEMA_BUSINESS_ADDRESS_UNIQUE_NAME;

    public static final URI JSON_SCHEMA_BUSINESS_ADDRESS_URI = SystemApi.Schema.getUri().resolve("/" + JSON_SCHEMA_BUSINESS_ADDRESS_UNIQUE_NAME);
    
    public static final String JSON_SCHEMA_BUSINESS_ADDRESS_PROPERTY_NAME = "businessName";

    public static final String JSON_SCHEMA_POSTAL_ADDRESS_UNIQUE_NAME = TEST_RESOURCE_NAMESPACE + "PostalAddress";

    public static final String JSON_SCHEMA_POSTAL_ADDRESS_RESOURCE_NAME = TEST_RESOURCE_PACKAGE + JSON_SCHEMA_POSTAL_ADDRESS_UNIQUE_NAME;

    public static final URI JSON_SCHEMA_POSTAL_ADDRESS_URI = SystemApi.Schema.getUri().resolve("/" + JSON_SCHEMA_POSTAL_ADDRESS_UNIQUE_NAME);

    public static final String JSON_SCHEMA_CONTACT_INFORMATION_UNIQUE_NAME = TEST_RESOURCE_NAMESPACE + "ContactInformation";

    public static final String JSON_SCHEMA_CONTACT_INFORMATION_RESOURCE_NAME = TEST_RESOURCE_PACKAGE + JSON_SCHEMA_CONTACT_INFORMATION_UNIQUE_NAME;

    public static final URI JSON_SCHEMA_CONTACT_INFORMATION_URI = SystemApi.Schema.getUri().resolve("/" + JSON_SCHEMA_CONTACT_INFORMATION_UNIQUE_NAME);

    public static final String JSON_SCHEMA_MULTIPLE5_UNIQUE_NAME = TEST_RESOURCE_NAMESPACE + "MultipleOf5";

    public static final String JSON_SCHEMA_MULTIPLE5_RESOURCE_NAME = TEST_RESOURCE_PACKAGE + JSON_SCHEMA_MULTIPLE5_UNIQUE_NAME;

    public static final URI JSON_SCHEMA_MULTIPLE5_URI = SystemApi.Schema.getUri().resolve("/" + JSON_SCHEMA_MULTIPLE5_UNIQUE_NAME);
    
    public static final Integer MULTIPLE5_VALUE = 5;
    
    public static final String JSON_SCHEMA_MULTIPLE4_UNIQUE_NAME = TEST_RESOURCE_NAMESPACE + "MultipleOf4";

    public static final String JSON_SCHEMA_MULTIPLE4_RESOURCE_NAME = TEST_RESOURCE_PACKAGE + JSON_SCHEMA_MULTIPLE4_UNIQUE_NAME;

    public static final URI JSON_SCHEMA_MULTIPLE4_URI = SystemApi.Schema.getUri().resolve("/" + JSON_SCHEMA_MULTIPLE4_UNIQUE_NAME);
    
    public static final String JSON_SCHEMA_MULTIPLE_PROPERTY_NAME = "value";
    
    public static final String JSON_SCHEMA_DEPENDENCY_UNIQUE_NAME = TEST_RESOURCE_NAMESPACE + "DependencyCheck";

    public static final String JSON_SCHEMA_DEPENDENCY_RESOURCE_NAME = TEST_RESOURCE_PACKAGE + JSON_SCHEMA_DEPENDENCY_UNIQUE_NAME;

    public static final URI JSON_SCHEMA_DEPENDENCY_URI = SystemApi.Schema.getUri().resolve("/" + JSON_SCHEMA_DEPENDENCY_UNIQUE_NAME);
    
    public static final String DEPENDENT_1 = "a";
    
    public static final String DEPENDENCY_1 = "b";

    private JsonSchemaLoader _JsonSchemaLoader;

    @Before
    public void setUp() throws Exception
    {

        final Engine engine = EngineTest.createTestEngine();
        _JsonSchemaLoader = engine.getContext().getSchemaLoader().getJsonSchemaLoader();

    }

    @After
    public void tearDown() throws Exception
    {

        _JsonSchemaLoader = null;
    }

    @Test(expected = JsonSchemaLoaderException.class)
    public void initParamNullFailure()
    {

        final JsonSchemaLoader jsonSchemaLoader = new JsonSchemaLoader();
        jsonSchemaLoader.init(null);
    }

    @Test
    public void jsonSchemaLoaderNotNull()
    {

        Assert.assertNotNull(_JsonSchemaLoader);
    }

    @Test
    public void loadJsonSchemaPerson() throws IOException
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_PERSON_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_PERSON_URI);
        Assert.assertNotNull(jsonSchema);
    }

    @Test
    public void convertJsonSchemaPerson() throws IOException
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_PERSON_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_PERSON_URI);
        final Schema schema = _JsonSchemaLoader.getContext().getSchemaLoader().load(jsonSchema);
        Assert.assertNotNull(schema);
    }
    
    // EXTENDS AND ALLOF TESTS

    @Test
    public void loadJsonSchemaImportantPerson() throws Exception
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_IMPORTANT_PERSON_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_IMPORTANT_PERSON_URI);
        Assert.assertNotNull(jsonSchema);
        
        Assert.assertTrue(!jsonSchema.getExtendedSchemaUris().isEmpty());
        Set<URI> extensions = jsonSchema.getExtendedSchemaUris();
        Assert.assertTrue(extensions.contains(JSON_SCHEMA_PERSON_URI));
    }

    @Test
    public void loadJsonSchemaAddressablePerson() throws Exception
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_ADDRESSABLE_PERSON_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_ADDRESSABLE_PERSON_URI);
        Assert.assertNotNull(jsonSchema);
        
        Assert.assertTrue(!jsonSchema.getProperties().isEmpty());
        ConcurrentMap<String,Property> props = jsonSchema.getProperties();
        Assert.assertTrue(props.containsKey(JSON_SCHEMA_ADDRESSABLE_PERSON_PROPERTY_NAME));
        Property prop = props.get(JSON_SCHEMA_ADDRESSABLE_PERSON_PROPERTY_NAME);
        Assert.assertTrue(prop.getJsonType().equals(JsonType.Boolean));
        
        // Make sure multiple values get picked up
        Assert.assertTrue(!jsonSchema.getExtendedSchemaUris().isEmpty());
        Set<URI> extensions = jsonSchema.getExtendedSchemaUris();
        Assert.assertTrue(extensions.contains(JSON_SCHEMA_PERSON_URI));
        Assert.assertTrue(extensions.contains(JSON_SCHEMA_POSTAL_ADDRESS_URI));
    }

    @Test
    public void loadJsonSchemaBusinessAddress() throws Exception
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_BUSINESS_ADDRESS_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_BUSINESS_ADDRESS_URI);
        Assert.assertNotNull(jsonSchema);
        
        Assert.assertTrue(!jsonSchema.getProperties().isEmpty());
        ConcurrentMap<String,Property> props = jsonSchema.getProperties();
        Assert.assertTrue(props.containsKey(JSON_SCHEMA_BUSINESS_ADDRESS_PROPERTY_NAME));
        Property prop = props.get(JSON_SCHEMA_BUSINESS_ADDRESS_PROPERTY_NAME);
        Assert.assertTrue(prop.getJsonType().equals(JsonType.String));
        
        Assert.assertTrue(!jsonSchema.getExtendedSchemaUris().isEmpty());
        Set<URI> extensions = jsonSchema.getExtendedSchemaUris();
        Assert.assertTrue(extensions.contains(JSON_SCHEMA_POSTAL_ADDRESS_URI));
    }
    
    // Multiple of Test
    
    @Test
    public void loadJsonSchemaMultipleOf5() throws Exception
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_MULTIPLE5_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_MULTIPLE5_URI);
        Assert.assertNotNull(jsonSchema);
        
        Assert.assertTrue(!jsonSchema.getProperties().isEmpty());
        ConcurrentMap<String,Property> props = jsonSchema.getProperties();
        Assert.assertTrue(props.containsKey(JSON_SCHEMA_MULTIPLE_PROPERTY_NAME));
        Property prop = props.get(JSON_SCHEMA_MULTIPLE_PROPERTY_NAME);
        Assert.assertTrue(prop.getJsonType().equals(JsonType.Integer));
        Object obj = prop.getValue(PropertyType.MultipleOf);
        Assert.assertTrue(obj != null);
        Assert.assertTrue(obj instanceof Integer);
        Integer val = (Integer)obj;
        Assert.assertTrue(val.equals(MULTIPLE5_VALUE));
    }
    
    @Test
    public void convertJsonSchemaMultipleOf5() throws Exception
    {
        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_MULTIPLE5_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_MULTIPLE5_URI);
        final Schema schema = _JsonSchemaLoader.getContext().getSchemaLoader().load(jsonSchema);
        Assert.assertNotNull(schema);
        List<Slot> slots = schema.getSlots();
        Assert.assertTrue(slots != null);
        Assert.assertTrue(slots.size() == 1);
        Slot slot = slots.get(0);
        Value val = slot.getValue();
        Object obj = val.getSlotValue(PropertyType.DivisibleBy.getName());
        Assert.assertTrue(obj != null);
        Assert.assertTrue(obj.equals(MULTIPLE5_VALUE));
    }
    
    // Required Keyword Tests
    
    // This test ensures v3 schema support for Required keyword.
    @Test
    public void loadJsonSchemaMultipleOf4Required() throws Exception
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_MULTIPLE4_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_MULTIPLE4_URI);
        Assert.assertNotNull(jsonSchema);
        
        Assert.assertTrue(!jsonSchema.getProperties().isEmpty());
        ConcurrentMap<String,Property> props = jsonSchema.getProperties();
        List<Property> requiredFields = jsonSchema.getRequired();
        Assert.assertTrue(requiredFields != null);
        Assert.assertTrue(requiredFields.size() == 1);
        Property prop = requiredFields.get(0);
        Assert.assertTrue(prop != null);
        Object obj = prop.getValue(PropertyType.Required);
        Assert.assertTrue((Boolean)obj);
        Assert.assertTrue(prop.getName().equals(JSON_SCHEMA_MULTIPLE_PROPERTY_NAME));
    }
    
    // Verifying support for v4 required, and backward with v3
    @Test
    public void loadJsonSchemaMultipleOf5Required() throws Exception
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_MULTIPLE5_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_MULTIPLE5_URI);
        Assert.assertNotNull(jsonSchema);
        
        Assert.assertTrue(!jsonSchema.getProperties().isEmpty());
        ConcurrentMap<String,Property> props = jsonSchema.getProperties();
        List<Property> requiredFields = jsonSchema.getRequired();
        Assert.assertTrue(requiredFields != null);
        Assert.assertTrue(requiredFields.size() == 1);
        Property prop = requiredFields.get(0);
        Assert.assertTrue(prop != null);
        Object obj = prop.getValue(PropertyType.Required);
        Assert.assertTrue((Boolean)obj);
        Assert.assertTrue(prop.getName().equals(JSON_SCHEMA_MULTIPLE_PROPERTY_NAME));
    }
    
    // DEPENDENCIES KEYWORD TESTS
    
    @Test
    public void loadJsonSchemaDependencyCheck() throws Exception
    {
        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_DEPENDENCY_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_DEPENDENCY_URI);
        Assert.assertNotNull(jsonSchema);
        
        Map<String,List<Property>> deps = jsonSchema.getDependencies();
        Assert.assertTrue(deps.size() == 1);
        List<Property> props = deps.get(DEPENDENT_1);
        Assert.assertTrue(props != null);
        Assert.assertTrue(props.size() == 1);
        Property prop = props.get(0);
        Assert.assertTrue(prop.getName().equals(DEPENDENCY_1));
    }
    
    // CONTINUE NORMAL TESTS
    
    @Test
    public void loadJsonSchemaPostalAddress() throws IOException
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_POSTAL_ADDRESS_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_POSTAL_ADDRESS_URI);
        Assert.assertNotNull(jsonSchema);
    }

    @Test
    public void convertJsonSchemaPostalAddress() throws IOException
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_POSTAL_ADDRESS_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_POSTAL_ADDRESS_URI);
        final Schema schema = _JsonSchemaLoader.getContext().getSchemaLoader().load(jsonSchema);
        Assert.assertNotNull(schema);
    }

    @Test
    public void loadJsonSchemaContactInformation() throws IOException
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_CONTACT_INFORMATION_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_CONTACT_INFORMATION_URI);
        Assert.assertNotNull(jsonSchema);
    }

    @Test
    public void convertJsonSchemaContactInformation() throws IOException
    {

        final Class<?> thisClass = getClass();

        final InputStream in = thisClass.getResourceAsStream(JSON_SCHEMA_CONTACT_INFORMATION_RESOURCE_NAME);
        final JsonSchema jsonSchema = _JsonSchemaLoader.load(in, JSON_SCHEMA_CONTACT_INFORMATION_URI);
        final Schema schema = _JsonSchemaLoader.getContext().getSchemaLoader().load(jsonSchema);
        Assert.assertNotNull(schema);
    }
}

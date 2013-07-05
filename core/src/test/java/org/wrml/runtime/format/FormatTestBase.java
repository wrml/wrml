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

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.DefaultContext;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.schema.SchemaLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base {@link TestCase} for testing {@link Formatter} implementations using {@link Mockito} mocks.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public abstract class FormatTestBase extends TestCase
{

    protected final String _BadInputData = ">}]<[{this is badly formed data. fo reals.";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected Formatter _Formatter;

    protected Context _MockContext;

    protected SchemaLoader _MockSchemaLoader;

    protected URI _MockSchemaSchemaUri;

    protected Keys _MockKeys;

    protected Dimensions _MockDimensions;

    protected abstract Formatter getFormatter();

    /**
     * {@code @Override} to set your own {@link Model}
     *
     * @return a {@code Model} used for round-trip serialization/deserialization
     */
    protected Model getMockSchema()
    {

        Model model = Mockito.mock(Schema.class);

        when(model.getContext()).thenReturn(_MockContext);
        return model;
    }

    @Before
    public void setUp() throws Exception
    {

        super.setUp();
        _MockContext = mock(DefaultContext.class);
        _MockSchemaLoader = mock(SchemaLoader.class);
        _MockSchemaSchemaUri = URI.create("http://mock.schema.schema/uri");
        _MockKeys = mock(Keys.class);
        _MockDimensions = mock(Dimensions.class);

        when(_MockSchemaLoader.getSchemaSchemaUri()).thenReturn(_MockSchemaSchemaUri);
        when(_MockSchemaLoader.getApiSchemaUri()).thenReturn(_MockSchemaSchemaUri);
        when(_MockDimensions.getSchemaUri()).thenReturn(_MockSchemaSchemaUri);
        when(_MockContext.getSchemaLoader()).thenReturn(_MockSchemaLoader);

        _Formatter = getFormatter();
        assertNotNull("required: initialize _Formatter in getFormatter()", _Formatter);

    }

    @Test
    public void testFormatterConstructor()
    {

        assertNotNull(_Formatter);
    }

    @Test
    public void testGetContext()
    {

        assertNotNull(_Formatter.getContext());
        assertEquals(_MockContext, _Formatter.getContext());
    }

    @Test
    public void testGetFormatUri()
    {

        assertNotNull(_Formatter.getFormatUri());
    }

    @Test
    public void testIsApplicableTo()
    {
        // happy path
        assertTrue(_Formatter.isApplicableTo(_MockSchemaSchemaUri));
        // null check
        assertFalse("a null URI should evaluate to 'false'", _Formatter.isApplicableTo(null));
        // just invalid
        assertFalse("an invalid URI should evaluate to 'false'", _Formatter.isApplicableTo(URI.create("http://a.b.c")));
    }

    @Ignore // The sub classes throw different errors
    @Test(expected = UnsupportedOperationException.class)
    public void testReadModelException() throws ModelReadingException
    {

        _MockDimensions = mock(Dimensions.class);
        when(_MockDimensions.getSchemaUri()).thenReturn(null);
        _Formatter.readModel(null, _MockKeys, _MockDimensions);
        fail("Should have thrown UnsupportedOperationException (see the Formatter API)");
    }

    /**
     * This test should not actually invoke the underlying {@link Formatter}'s marshaller. Mock the Formatter to inject
     * required contexts.
     *
     * @throws ModelReadingException - optional upon @Override
     */
    @Test
    public void testReadModel() throws ModelReadingException
    {

        try
        {
            Model mockModel = getMockSchema();
            Model result = getFormatter().readModel(mock(InputStream.class), _MockKeys, _MockDimensions);
            assertNotNull(result);
            assertEquals(mockModel, result);
        }
        catch (Exception e)
        {
            fail("This test should not actually invoke the underlying Fomatter's marshaller.  Might need to @Override this test with a mock impl that is a pure unit test.  Error: "
                    + e.getMessage());
        }
    }

    /**
     * Test for {@code _BadInputData} and {@code readModel()}.
     *
     * @throws ModelReadingException - expected
     */
    @Ignore // The sub classes throw different errors
    @Test(expected = ModelReadingException.class)
    public void testReadModelBadData() throws ModelReadingException
    {

        InputStream in = new ByteArrayInputStream(_BadInputData.getBytes());
        _Formatter.readModel(in, _MockKeys, _MockDimensions);
        fail("Should have thrown UnsupportedOperationException (see the Formatter API)");
    }

    @Test
    public void testWriteModel() throws ModelWritingException
    {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Model obj = getMockSchema();
        _Formatter.writeModel(out, obj, null);
        assertNotNull(out);
        logger.debug("testWriteModel: {}", out.toString());
        assertTrue(out.toString().length() > 0);
    }

    @Test(expected = ModelWritingException.class)
    public void testWriteModelException() throws ModelWritingException
    {

        Model obj = getMockSchema();
        _Formatter.writeModel(null, obj, null);
        fail("Should have thrown ModelWritingException (see the Formatter API)");
    }

    @Ignore // The sub classes throw different errors
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteModelNotSchemaException() throws ModelWritingException
    {

        Model notSchema = mock(Model.class);
        _Formatter.writeModel(null, notSchema, null);
    }

    @Test
    public void testRoundTrip() throws ModelWritingException, ModelReadingException
    {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        InputStream in = null;

        // 1. create object
        Model obj = getMockSchema();

        // 2. marshal Model to xml using writeModel()
        _Formatter.writeModel(out, obj, null);
        assertNotNull(out);
        String marshalledString1 = new String(out.toByteArray());
        in = new ByteArrayInputStream(out.toByteArray());

        // 3. unmarshal xml to Model using readModel()
        Model unmarshalled = _Formatter.readModel(in, _MockKeys, _MockDimensions);
        assertNotNull(unmarshalled);
        assertNotSame("should not be the same object", unmarshalled, obj);

        // 4. marshal model2 for String comparison
        _Formatter.writeModel(out2, unmarshalled, null);
        assertNotNull(out2);
        String marshalledString2 = new String(out2.toByteArray());
        assertNotSame("should not be the same String", marshalledString1, marshalledString2);

    }

}

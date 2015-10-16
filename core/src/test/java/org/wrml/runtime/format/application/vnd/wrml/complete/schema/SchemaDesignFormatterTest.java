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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.wrml.runtime.format.*;
import org.wrml.runtime.schema.LinkProtoSlot;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.syntax.SyntaxLoader;

import java.io.InputStream;
import java.net.URI;
import java.util.TreeMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link CompleteSchemaFormatter} using {@link Mockito} mocks.
 *
 * @see {@link FormatTestBase} for base {@link Test}s common to all {@link Formatter}s.
 */

@Ignore
public class SchemaDesignFormatterTest extends FormatTestBase {

    @Before
    @Override
    public void setUp() throws Exception {

        super.setUp();
        SyntaxLoader mockSyntaxLoader = mock(SyntaxLoader.class);
        String mockUri = "mockUri";
        Prototype mockPrototype = mock(Prototype.class);
        when(this._MockContext.getSyntaxLoader()).thenReturn(mockSyntaxLoader);
        when(mockSyntaxLoader.formatSyntaxValue(any(URI.class))).thenReturn(mockUri);
        when(this._MockSchemaLoader.getPrototype(any(URI.class))).thenReturn(mockPrototype);
        when(mockPrototype.getLinkProtoSlots()).thenReturn(new TreeMap<URI, LinkProtoSlot>());
    }

    @Override
    protected Formatter getFormatter() {

        final CompleteSchemaFormatter formatter = new CompleteSchemaFormatter();
        final FormatterConfiguration formatterConfiguration = new FormatterConfiguration();
        formatterConfiguration.setFormatUri(SystemFormat.vnd_wrml_complete_schema.getFormatUri());
        formatter.init(this._MockContext, formatterConfiguration);
        return formatter;
    }

    /**
     * {@link Override} since this impl doesn't {@code readModel()}. (throws {@link ModelReadingException}.
     */
    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testReadModel() throws ModelReadingException {

        this._Formatter.readModel(mock(InputStream.class), _MockKeys, _MockDimensions);
        fail("Expected an UnsupportedOperationException");
    }

    /**
     * {@link Override} since this impl doesn't {@code readModel()}. (throws {@link UnsupportedOperationException}.
     */
    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testReadModelException() throws ModelReadingException {

        super.testReadModelException();
        fail("Expected an UnsupportedOperationException");
    }

    /**
     * {@link Override} since this impl doesn't {@code readModel()}. (throws {@link UnsupportedOperationException}.
     */
    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testReadModelBadData() throws ModelReadingException {

        super.testReadModelBadData();
        fail("Expected an UnsupportedOperationException");
    }


    /**
     * {@link Override} since this impl doesn't {@code readModel()}. (throws {@link UnsupportedOperationException}.
     */
    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRoundTrip() throws ModelWritingException, ModelReadingException {

        super.testRoundTrip();
        fail("Expected an UnsupportedOperationException");
    }

}

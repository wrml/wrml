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

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.wrml.model.Model;
import org.wrml.model.rest.Api;
import org.wrml.runtime.format.FormatterConfiguration;
import org.wrml.runtime.format.FormatTestBase;
import org.wrml.runtime.format.Formatter;
import org.wrml.runtime.format.SystemFormat;
import org.wrml.runtime.schema.Prototype;

import java.net.URI;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link ApiAsciiFormatter} using {@link Mockito} mocks.
 *
 * @see {@link FormatTestBase} for base {@link Test}s common to all {@link Formatter}s.
 */
public class ApiAsciiFormatterTest extends FormatTestBase
{

    @Override
    protected Formatter getFormatter()
    {
        final ApiAsciiFormatter formatter = new ApiAsciiFormatter();
        final FormatterConfiguration formatterConfiguration = new FormatterConfiguration();
        formatterConfiguration.setFormatUri(SystemFormat.vnd_wrml_ascii_api.getFormatUri());
        formatter.init(this._MockContext, formatterConfiguration);
        return formatter;
    }

    @Override
    protected Model getMockSchema()
    {

        return mock(Api.class);
    }

    /**
     * Overriding since Prototype gets mocked.
     */
    @Override
    @Test
    public void testIsApplicableTo()
    {

        Prototype mockPrototype1 = mock(Prototype.class);
        Prototype mockPrototype2 = mock(Prototype.class);
        when(mockPrototype1.isAssignableFrom(any(URI.class))).thenReturn(true);
        when(mockPrototype2.isAssignableFrom(any(URI.class))).thenReturn(false);

        // happy path
        when(this._MockSchemaLoader.getPrototype(any(URI.class))).thenReturn(mockPrototype1);
        assertTrue(_Formatter.isApplicableTo(_MockSchemaSchemaUri));

        // still happy (means our method is getting invoked)
        when(this._MockSchemaLoader.getPrototype(any(URI.class))).thenReturn(mockPrototype2);
        assertFalse(_Formatter.isApplicableTo(_MockSchemaSchemaUri));

    }

    @Override
    @Ignore
    @Test
    public void testReadModel()
    {
        // TODO: fix this test
    }

    @Test
    @Ignore
    @Override
    public void testRoundTrip()
    {
        // TODO: fix this test
    }

    @Override
    @Ignore
    @Test
    public void testWriteModel()
    {
        // TODO: fix this test
    }
}

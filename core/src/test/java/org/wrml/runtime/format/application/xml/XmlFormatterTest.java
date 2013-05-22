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

package org.wrml.runtime.format.application.xml;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.wrml.runtime.format.FormatterConfiguration;
import org.wrml.runtime.format.FormatTestBase;
import org.wrml.runtime.format.Formatter;
import org.wrml.runtime.format.SystemFormat;

import java.net.URI;

/**
 * Test for {@link XmlFormatter} using {@link Mockito} mocks.
 *
 * @author JJ Zabkar
 * @see {@link FormatTestBase} for base {@link Test}s common to all {@link Formatter}s.
 */
public class XmlFormatterTest extends FormatTestBase
{

    @Override
    protected Formatter getFormatter()
    {

        final XmlFormatter formatter = new XmlFormatter();
        final FormatterConfiguration formatterConfiguration = new FormatterConfiguration();
        formatterConfiguration.setFormatUri(SystemFormat.xml.getFormatUri());
        formatter.init(this._MockContext, formatterConfiguration);
        return formatter;
    }



    @Override
    @Ignore
    @Test
    public void testReadModel()
    {
        // TODO: fix this test
    }


    @Test
    @Override
    public void testIsApplicableTo()
    {
        // happy path
        assertTrue(_Formatter.isApplicableTo(_MockSchemaSchemaUri));
        // null check
        assertTrue("a null URI should evaluate to 'false'", _Formatter.isApplicableTo(null));
        // just invalid
        assertTrue("an invalid URI should evaluate to 'false'", _Formatter.isApplicableTo(URI.create("http://a.b.c")));

    }
}

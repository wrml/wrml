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
package org.wrml.runtime.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.runtime.syntax.UriSyntaxHandler;

public class UriTemplateTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger(UriTemplateTest.class);

    private SyntaxLoader _SyntaxLoader;

    final Map<String, Object> _ParameterMap = new HashMap<String, Object>()
    {
        private static final long serialVersionUID = 1L;

        {
            put("moose", "crazy");
            put("squirrel", "nuts");
        }
    };

    @Before
    public void init()
    {
        _SyntaxLoader = mock(SyntaxLoader.class);
        when(_SyntaxLoader.formatSyntaxValue("crazy")).thenReturn("crazy");
        when(_SyntaxLoader.formatSyntaxValue("nuts")).thenReturn("nuts");
        when(_SyntaxLoader.getSyntaxHandler(String.class)).thenReturn(null);
        UriSyntaxHandler optimus = new UriSyntaxHandler();
        when(_SyntaxLoader.getSyntaxHandler(URI.class)).thenReturn(optimus);
    }

    @Test
    public void evaluate() throws URISyntaxException
    {
        final String inputPattern = "/{moose}/{squirrel}";

        LOGGER.debug("Input pattern is " + inputPattern);

        final UriTemplate t = new UriTemplate(_SyntaxLoader, inputPattern);

        final URI result = t.evaluate(_ParameterMap);
        final URI expected = new URI("/crazy/nuts");

        Assert.assertTrue("Expected " + expected + ", but got " + result, result.equals(expected));
    }

    @Test
    public void pullParameters() throws URISyntaxException
    {
        final String inputPattern = "/{moose}/{squirrel}";

        final UriTemplate t = new UriTemplate(_SyntaxLoader, inputPattern);

        final URI uri = new URI("/crazy/nuts");

        final SortedSet<Parameter> params = t.getParameters(uri);

        Assert.assertTrue("Params should not be null...", params != null);

        final Iterator<Parameter> iter = params.iterator();
        Parameter p = iter.next();
        // LOGGER.debug(p);
        Assert.assertTrue(p.getName().equals("moose"));
        Assert.assertTrue(p.getValue().equals("crazy"));
        p = iter.next();
        // LOGGER.debug(p);
        Assert.assertTrue(p.getName().equals("squirrel"));
        Assert.assertTrue(p.getValue().equals("nuts"));
    }

    @Test
    public void testCreate()
    {
        @SuppressWarnings("unused")
        final UriTemplate t = new UriTemplate(_SyntaxLoader, "/{moose}/{squirrel}");
    }

    @Test
    public void testParams()
    {
        final UriTemplate t = new UriTemplate(_SyntaxLoader, "/{moose}/{squirrel}");

        final String[] expected = new String[] { "moose", "squirrel" };
        final List<String> expectedList = Arrays.asList(expected);

        final String[] names = t.getParameterNames();
        final List<String> realList = Arrays.asList(names);

        Assert.assertTrue("The expected param names " + expectedList.toString() + " do not match the actual " + realList.toString(), realList.equals(expectedList));
    }

    @Test
    public void testLongParams()
    {
        final UriTemplate t = new UriTemplate(_SyntaxLoader, "/cylons/galaxy/{galaxy}/{quadrant}/aliens/{alienId}");

        final String[] names = t.getParameterNames();
        final List<String> realList = Arrays.asList(names);

        final String[] expected = new String[] { "galaxy", "quadrant", "alienId" };
        final List<String> expectedList = Arrays.asList(expected);

        Assert.assertTrue("The expected param names " + expectedList.toString() + " do not match the actual " + realList.toString(), realList.equals(expectedList));
    }
}

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

package org.wrml.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wrml.model.Model;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.LinkTemplate;
import org.wrml.model.rest.ResourceTemplate;
import org.wrml.runtime.Context;
import org.wrml.runtime.DefaultContext;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Engine;
import org.wrml.runtime.EngineTest;
import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.runtime.rest.Resource;
import org.wrml.runtime.schema.DefaultSchemaLoader;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Resource.class})
public class AsciiArtTest
{

    Api _Api;

    ApiNavigator _ApiNavigator;

    Resource docRoot;

    ResourceTemplate _ResourceTemplate;

    List<LinkTemplate> linkTemplates;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception
    {
        _Api = mock(Api.class);
        _ApiNavigator = mock(ApiNavigator.class);
        docRoot = mock(Resource.class);
        _ResourceTemplate = mock(ResourceTemplate.class);
        linkTemplates = mock(List.class);

        when(docRoot.getPathSegment()).thenReturn("root");
        when(_ApiNavigator.getApi()).thenReturn(_Api);
        when(_ApiNavigator.getDocroot()).thenReturn(docRoot);
        when(_Api.getLinkTemplates()).thenReturn(linkTemplates);

    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testExpressApiNavigator()
    {
        String result = AsciiArt.express(_ApiNavigator);
        assertNotNull(result);
    }

    /**
     * <ul>
     * <li>A
     * <ul>
     * <li>B
     * <ul>
     * <li>D
     * </ul>
     * <li>C
     * </ul>
     * </ul>
     */
    @Test
    public void testPrintAsciiTree() throws Exception
    {
        ConcurrentHashMap<String, Resource> docRootPathResources = new ConcurrentHashMap<String, Resource>();
        ConcurrentHashMap<String, Resource> nodeAPathResources = new ConcurrentHashMap<String, Resource>();
        ConcurrentHashMap<String, Resource> nodeBPathResources = new ConcurrentHashMap<String, Resource>();
        final Resource a = mock(Resource.class);
        final Resource b = mock(Resource.class);
        final Resource c = mock(Resource.class);
        final Resource d = mock(Resource.class);
        when(a.getPathSegment()).thenReturn("node-a");
        when(b.getPathSegment()).thenReturn("node-b");
        when(c.getPathSegment()).thenReturn("node-c");
        when(d.getPathSegment()).thenReturn("node-d");

        docRootPathResources.put("A", a);
        nodeAPathResources.put("B", b);
        nodeAPathResources.put("C", c);
        nodeBPathResources.put("D", d);

        when(docRoot.getLiteralPathSubresources()).thenReturn(docRootPathResources);
        when(a.getLiteralPathSubresources()).thenReturn(nodeAPathResources);
        when(b.getLiteralPathSubresources()).thenReturn(nodeBPathResources);

        String result = AsciiArt.expressAsciiTree(new AsciiArt.ResourceAsciiTreeNode(docRoot));

        assertNotNull(result);

        assertTrue(result.contains("\n+--/node-a"));
        assertTrue(result.contains("\n   +--/node-b"));
        assertTrue(result.contains("\n   |  +--/node-d"));
        assertTrue(result.contains("\n   +--/node-c"));

    }

    @Test
    public void testExpressModel() throws Exception
    {
        final Model model = mock(Model.class);
        final Dimensions dimensions = mock(Dimensions.class);
        final Engine engine = EngineTest.createTestEngine();
        final Context _Context = engine.getContext();
        when(model.getContext()).thenReturn(_Context);
        when(model.getDimensions()).thenReturn(dimensions);

        String result = AsciiArt.express(model);
        assertNotNull(result);
    }

    @Test
    public void testExpressObject()
    {
        String result = AsciiArt.express((Object) "abc");
        assertNotNull(result);
    }

    /**
     * @see {@link DefaultContext#toString()} (overrides {@link Object#toString() toString()})
     * @see {@link DefaultSchemaLoader#toString()} (does <b>NOT</b> override {@link Object#toString() toString()})
     */
    @Test
    public void testExpressRecursiveObject()
    {
        assertNotNull(AsciiArt.express(new DefaultContext()));
        assertNull(AsciiArt.express(new DefaultSchemaLoader()));
    }

    @Test
    public void testExpressObjectNull()
    {
        assertNull(null);
    }

    /**
     * Test to resolve issue WRML-213.
     * 
     * @see https://wrmlorg.jira.com/browse/WRML-213
     */
    @Test
    public void testNoAsciiPrintDeadEnds() throws Exception
    {
        ConcurrentHashMap<String, Resource> docRootPathResources = new ConcurrentHashMap<String, Resource>();
        ConcurrentHashMap<String, Resource> nodeAPathResources = new ConcurrentHashMap<String, Resource>();
        final Resource a = mock(Resource.class);
        final Resource b = mock(Resource.class);
        when(a.getPathSegment()).thenReturn("{uniqueName}");
        when(b.getPathSegment()).thenReturn("foo");
        docRootPathResources.put("A", a);
        nodeAPathResources.put("B", b);
        when(docRoot.getLiteralPathSubresources()).thenReturn(docRootPathResources);
        when(a.getLiteralPathSubresources()).thenReturn(nodeAPathResources);

        String result = AsciiArt.expressAsciiTree(new AsciiArt.ResourceAsciiTreeNode(docRoot));

        assertNotNull(result);
        assertTrue(result.contains("\n+--/{uniqueName}\n   |  \n   +--/foo"));
    }

}
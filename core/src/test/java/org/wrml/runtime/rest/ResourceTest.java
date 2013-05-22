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

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.Mockito;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Method;
import org.wrml.model.rest.ResourceTemplate;
import org.wrml.model.rest.ResourceTemplateTest;
import org.wrml.runtime.Context;
import org.wrml.runtime.DefaultContext;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

/**
 * Test for {@link Resource}.
 * <p>
 * Static method(s) are helpers for other test classes.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ResourceTest extends TestCase
{

    // private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTest.class);

    /** class under test */
    Resource _Resource;

    // mocks
    protected ApiNavigator mockApiNavigator;
    protected ResourceTemplate mockResourceTemplate;

    @Before
    public void setUp() throws Exception
    {
        this.mockApiNavigator = mock(ApiNavigator.class);
        this.mockResourceTemplate = ResourceTemplateTest.getMock();
        this._Resource = ResourceTest.getMock(this.mockApiNavigator, this.mockResourceTemplate, null);
    }

    @After
    public void tearDown() throws Exception
    {
        this._Resource = null;
        this.mockApiNavigator = null;
        this.mockResourceTemplate = null;
    }

    /**
     * Factory method to get a test mock {@link Resource} with mocked dependencies for unit tests.
     * 
     * @see {@link #getMock(ApiNavigator, ResourceTemplate, Resource)}
     * @return a {@link Resource} for unit tests.
     */
    public static Resource getMock()
    {
        return getMock(null, null, null);
    }

    /**
     * Factory method to get a test mock {@link Resource} with mocked dependencies for unit tests.
     * 
     * @param apiNavigator
     *            [optional] the {@link ApiNavigator} to use. Defaults to a {@link Mockito#mock}.
     * @param resourceTemplate
     *            [optional] the {@link ResourceTemplate} to use. Defaults to a {@link Mockito#mock}.
     * @param parentResource
     * @return a {@link Resource} for unit tests.
     */
    public static Resource getMock(ApiNavigator apiNavigator, ResourceTemplate resourceTemplate, Resource parentResource)
    {
        ApiNavigator mockApiNavigator = ObjectUtils.defaultIfNull(apiNavigator, mock(ApiNavigator.class));
        ResourceTemplate mockResourceTemplate = ObjectUtils.defaultIfNull(resourceTemplate, mock(ResourceTemplate.class));
        Context mockContext = mock(DefaultContext.class);
        Api mockApi = mock(Api.class);
        URI mockApiUri = URI.create("http://mock.api.api/uri");

        when(mockApiNavigator.getApi()).thenReturn(mockApi);
        when(mockApi.getContext()).thenReturn(mockContext);
        when(mockApi.getUri()).thenReturn(mockApiUri);

        return new Resource(mockApiNavigator, mockResourceTemplate, parentResource);
    }

    @Test
    public void testResource()
    {
        assertNotNull(this._Resource);
    }

    @Test(expected = ResourceException.class)
    public void testConstructorNullApiNavigator()
    {
        this._Resource = new Resource(null, this.mockResourceTemplate, null);
        fail("expeced ResourceException");
    }

    @Test(expected = ResourceException.class)
    public void testConstructorNullResourceTemplate()
    {
        this._Resource = new Resource(this.mockApiNavigator, null, null);
        fail("expeced ResourceException");
    }

    @Test
    public void testGetAllChildResourcesEmpty()
    {
        List<Resource> children = this._Resource.getAllChildResources();
        assertNotNull(children);
        assertEquals(0, children.size());
    }

    @Test
    public void testGetAllChildResources()
    {
        Resource parentResource = getMock(mockApiNavigator, mockResourceTemplate, null);
        Resource resource = getMock(mockApiNavigator, mockResourceTemplate, parentResource);
        // return non-null on call 1 and null on (recursive) call 2
        when(mockApiNavigator.getResource(any(UUID.class))).thenReturn(resource).thenReturn(null);

        List<Resource> children = parentResource.getAllChildResources();

        assertTrue("Expected at least one child; see ResourceTemplateTest.", children.size() > 0);
    }

    @Test
    public void testGetApiNavigator()
    {
        assertNotNull(this._Resource.getApiNavigator());
        assertSame(this.mockApiNavigator, this._Resource.getApiNavigator());
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetRequestSchemaUris()
    {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetDefaultSchemaUri()
    {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetLinkTemplates()
    {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetLiteralPathSubresources()
    {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetParentResource()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testGetPath()
    {
        Resource parentResource = getMock(mockApiNavigator, mockResourceTemplate, null);
        Resource resource = getMock(mockApiNavigator, mockResourceTemplate, parentResource);

        assertNotNull(resource.getPath(false));
        assertNotNull(resource.getPath(true));
        assertNotNull(parentResource.getPath(false));
        assertNotNull(parentResource.getPath(true));

    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetPathSegment()
    {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetPathText()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testGetReferenceLinkRelationUris()
    {
        assertNull(this._Resource.getReferenceLinkRelationUris(Method.Get));
    }

    @Test
    public void testGetReferenceTemplates()
    {
        assertNotNull(this._Resource.getReferenceTemplates());
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetResourceTemplate()
    {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetResourceTemplateId()
    {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetResponseSchemaUris()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testGetUri()
    {
        assertNull(this._Resource.getUri(null, null));
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetUriTemplate()
    {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testGetVariablePathSubresources()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testIsDocroot()
    {
        final Resource parentResource = getMock(mockApiNavigator, mockResourceTemplate, null);
        final Resource resource = getMock(mockApiNavigator, mockResourceTemplate, parentResource);
        assertTrue(parentResource.isDocroot());
        assertFalse(resource.isDocroot());
    }

    @Test
    @Ignore
    // Test coverage provided by integration tests
    public void testAddSubresource()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testToString()
    {
        assertNotNull(this._Resource.toString());
    }

    @Test
    public void testCompareTo()
    {
        final ResourceTemplate mockResourceTemplate1 = mock(ResourceTemplate.class);
        final ResourceTemplate mockResourceTemplate2 = mock(ResourceTemplate.class);
        when(mockResourceTemplate1.getPathSegment()).thenReturn("abc");
        when(mockResourceTemplate2.getPathSegment()).thenReturn("def");
        final Resource parentResource = getMock(mockApiNavigator, mockResourceTemplate1, null);
        final Resource resource = getMock(mockApiNavigator, mockResourceTemplate2, parentResource);

        final int result1 = resource.compareTo(parentResource);
        final int result2 = parentResource.compareTo(resource);

        assertNotEquals(0, result1);
        assertTrue(result1 != result2);
        assertEquals(0, resource.compareTo(resource));
        assertEquals(0, parentResource.compareTo(parentResource));
    }

}

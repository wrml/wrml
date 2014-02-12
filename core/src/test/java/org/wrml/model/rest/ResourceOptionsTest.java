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
package org.wrml.model.rest;

import junit.framework.TestCase;
import org.apache.commons.lang3.EnumUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.Mockito;
import org.wrml.model.rest.status.Status;
import org.wrml.runtime.Context;
import org.wrml.runtime.Engine;
import org.wrml.runtime.EngineTest;
import org.wrml.runtime.rest.UriTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link ResourceOptions}
 * <p/>
 * //TODO: mark as @IntegrationTest
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ResourceOptionsTest extends TestCase {

    /**
     * class under test
     */
    private ResourceOptions resourceOptions;

    private static Context context;

    @BeforeClass
    public static void beforeClass() throws Exception {

        final Engine engine = EngineTest.createTestEngine();
        context = engine.getContext();
    }

    @AfterClass
    public static void afterClass() {

        context = null;
    }

    @Before
    public void setUp() throws Exception {

        resourceOptions = context.newModel(ResourceOptions.class);
    }

    @After
    public void tearDown() throws Exception {

        resourceOptions = null;
    }

    @Test
    public void testContextPrerequisite() {

        assertNotNull(context);
    }

    @Test
    public void testGetSetStatus() {

        Status mockStatus = Status.ACCEPTED;
        resourceOptions.setStatus(mockStatus);
        Status otherStatus = resourceOptions.getStatus();
        assertEquals(mockStatus, otherStatus);
        assertSame(mockStatus, otherStatus);
    }

    @Test
    public void testGetSetAllow() {

        List<Method> mock = new ArrayList<Method>();
        mock.addAll(EnumUtils.getEnumList(Method.class));
        resourceOptions.setAllow(mock);
        List<Method> other = resourceOptions.getAllow();
        assertEquals(mock.size(), other.size());
        assertEquals(mock, other);
    }

    @Test
    public void testGetSetContentType() {

        String mock = "abc";
        resourceOptions.setContentType(mock);
        String other = resourceOptions.getContentType();
        assertTrue(mock.equals(other));
        assertSame(mock, other);
    }

    @Test
    public void testGetSetUriTemplate() {

        UriTemplate mock = Mockito.mock(UriTemplate.class);
        resourceOptions.setUriTemplate(mock);
        UriTemplate other = resourceOptions.getUriTemplate();
        assertTrue(mock.equals(other));
        assertSame(mock, other);
    }

}

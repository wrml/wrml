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

package org.wrml.model.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.runtime.rest.Resource;
import org.wrml.runtime.rest.ResourceTest;

/**
 * Test for {@link ResourceTemplate}.
 * <p>
 * Static method(s) are helpers for other test classes.
 */
public class ResourceTemplateTest
{

    /** @return a {@link Mock} {@link ResourceTemplate} with a {@link List} of {@link Resource} children. */
    public static ResourceTemplate getMock()
    {
        final ResourceTemplate mockResourceTemplate = mock(ResourceTemplate.class);
        final UUID mockUUID = UUID.randomUUID();
        final ApiNavigator mockApiNavigator = mock(ApiNavigator.class);
        final Resource mockResource = ResourceTest.getMock(mockApiNavigator, mockResourceTemplate, null);
        final List<ResourceTemplate> mockResourceTemplateChildren = new ArrayList<ResourceTemplate>();

        mockResourceTemplateChildren.add(mock(ResourceTemplate.class));

        when(mockApiNavigator.getResource(any(UUID.class))).thenReturn(mockResource);
        when(mockResourceTemplate.getChildren()).thenReturn(mockResourceTemplateChildren);
        when(mockResourceTemplate.getUniqueId()).thenReturn(mockUUID);

        return mockResourceTemplate;
    }

}

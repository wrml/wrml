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
package org.wrml.contrib.runtime.service.groovy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.service.DefaultServiceConfiguration;
import org.wrml.runtime.service.ServiceConfiguration;
import org.wrml.runtime.service.ServiceException;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

// TODO:  This import isn't working out-of-the box with our current maven setup
//import org.wrml.service.ServiceTest;

// TODO:  extends ServiceTest
public class GroovyTemplateServiceTest //extends ServiceTest
{
    private GroovyTemplateService _Service;

    private Context _Context;

    private static final String RESOURCES = "org/wrml/contrib/runtime/service/groovy/groovyTemplates";

    private static final String BASIC_SCRIPT_NAME = "TTL.groovy";

    private static final String NO_SCRIPT_NAME = "bulhunky.groovy";

    private static final String JARGON_SCRIPT_NAME = "bad/jargon.groovy";

    private static final String DEEP_SET_SCRIPT_NAME = "TTLDeep.groovy";

    private static final String SCRIPT_INPUT_KEY = "ttl";

    private static final Long TTL_VALUE = 8L;

    @Before
    public void setUp() {

        _Service = new GroovyTemplateService();

        // initialize
        SortedMap<String, String> settings = new TreeMap<>();
        settings.put(GroovyTemplateService.TEMPLATE_ROOT_LOCATION, getClass().getClassLoader().getResource(RESOURCES).getFile());

        _Context = mock(Context.class);
        _Service.init(_Context, createServiceConfiguration(settings));
    }

    @After
    public void tearDown() {

        _Service = null;
        _Context = null;
    }

    @Test(expected = ServiceException.class)
    public void testInitNull() {

        _Service = null;
        _Service = new GroovyTemplateService();
        _Service.init(null, null);
    }

    @Test(expected = ServiceException.class)
    public void testInitEmpty() {

        _Service = null;
        _Service = new GroovyTemplateService();

        _Service.init(null, createServiceConfiguration());
    }

    @Test(expected = ServiceException.class)
    public void testInitBadName() {

        _Service = null;
        _Service = new GroovyTemplateService();

        SortedMap<String, String> settings = new TreeMap<>();
        settings.put("random", "moose");
        _Service.init(null, createServiceConfiguration(settings));
    }

    @Test(expected = ServiceException.class)
    public void testInitBadLocation() {

        _Service = null;
        _Service = new GroovyTemplateService();

        SortedMap<String, String> settings = new TreeMap<>();
        settings.put(GroovyTemplateService.TEMPLATE_ROOT_LOCATION, "moose");
        _Service.init(null, createServiceConfiguration(settings));
    }

    @Test
    public void testInitGood() {

        _Service = null;
        _Service = new GroovyTemplateService();

        SortedMap<String, String> settings = new TreeMap<>();
        settings.put(GroovyTemplateService.TEMPLATE_ROOT_LOCATION, getClass().getClassLoader().getResource(RESOURCES).getFile());
        _Service.init(_Context, createServiceConfiguration(settings));

        assertTrue(_Service.getRoots().size() == 1);
        assertTrue(_Service.getTemplates().isEmpty());
    }

    @Test(expected = ServiceException.class)
    public void testLoadNoTemplate() {

        Context context = mock(Context.class);
        Keys keys = mock(Keys.class);
        Set<URI> args = new HashSet<>();
        args.add(URI.create(SCRIPT_INPUT_KEY));
        when(keys.getKeyedSchemaUris()).thenReturn(args);
        when(keys.getValue(URI.create(SCRIPT_INPUT_KEY))).thenReturn("Bulwinkle");
        Dimensions dimensions = mock(Dimensions.class);
        Document doc = mock(Document.class);
        when(context.newModel(dimensions)).thenReturn(doc);
        URI scriptSchemaName = URI.create(NO_SCRIPT_NAME);
        when(dimensions.getSchemaUri()).thenReturn(scriptSchemaName);
        Model model = _Service.get(keys, dimensions);
    }

    @Test(expected = ServiceException.class)
    public void testLoadJargonTemplate() {

        Context context = mock(Context.class);
        Keys keys = mock(Keys.class);
        Set<URI> args = new HashSet<>();
        args.add(URI.create(SCRIPT_INPUT_KEY));
        when(keys.getKeyedSchemaUris()).thenReturn(args);
        when(keys.getValue(URI.create(SCRIPT_INPUT_KEY))).thenReturn("Bulwinkle");
        Dimensions dimensions = mock(Dimensions.class);
        Document doc = mock(Document.class);
        when(context.newModel(dimensions)).thenReturn(doc);
        URI scriptSchemaName = URI.create(JARGON_SCRIPT_NAME);
        when(dimensions.getSchemaUri()).thenReturn(scriptSchemaName);
        Model model = _Service.get(keys, dimensions);
    }

    @Test
    public void testGetBasic() {

        Keys keys = mock(Keys.class);
        Set<URI> args = new HashSet<>();
        args.add(URI.create(SCRIPT_INPUT_KEY));
        when(keys.getKeyedSchemaUris()).thenReturn(args);
        when(keys.getValue(URI.create(SCRIPT_INPUT_KEY))).thenReturn(TTL_VALUE);
        Dimensions dimensions = mock(Dimensions.class);
        Document doc = mock(Document.class);
        when(_Context.newModel(dimensions)).thenReturn(doc);
        URI scriptSchemaName = URI.create(BASIC_SCRIPT_NAME);
        when(dimensions.getSchemaUri()).thenReturn(scriptSchemaName);
        Model model = _Service.get(keys, dimensions);

        // Assert the model has what we hope
        assertTrue(model.equals(doc));
        assertTrue(_Service.getRoots().size() == 1);
        assertTrue(_Service.getTemplates().size() == 1);
    }

    @Test
    public void testGetSet() {

        Keys keys = mock(Keys.class);
        Set<URI> args = new HashSet<>();
        args.add(URI.create(SCRIPT_INPUT_KEY));
        when(keys.getKeyedSchemaUris()).thenReturn(args);
        when(keys.getValue(URI.create(SCRIPT_INPUT_KEY))).thenReturn(TTL_VALUE);
        Dimensions dimensions = mock(Dimensions.class);
        Document doc = mock(Document.class);
        when(_Context.newModel(dimensions)).thenReturn(doc);
        URI scriptSchemaName = URI.create(BASIC_SCRIPT_NAME);
        when(dimensions.getSchemaUri()).thenReturn(scriptSchemaName);
        Model model = _Service.get(keys, dimensions);

        // Assert the model has what we hope
        assertTrue(model.equals(doc));
        assertTrue(_Service.getRoots().size() == 1);
        assertTrue(_Service.getTemplates().size() == 1);
        verify(doc, times(1)).setSecondsToLive(TTL_VALUE);
    }

    @Test
    public void testMultipleGets() {

        Keys keys = mock(Keys.class);
        Set<URI> args = new HashSet<>();
        args.add(URI.create(SCRIPT_INPUT_KEY));
        when(keys.getKeyedSchemaUris()).thenReturn(args);
        when(keys.getValue(URI.create(SCRIPT_INPUT_KEY))).thenReturn(TTL_VALUE);
        Dimensions dimensions = mock(Dimensions.class);
        Document doc = mock(Document.class);
        when(_Context.newModel(dimensions)).thenReturn(doc);
        URI scriptSchemaName = URI.create(BASIC_SCRIPT_NAME);
        when(dimensions.getSchemaUri()).thenReturn(scriptSchemaName);
        Model model = _Service.get(keys, dimensions);

        // Assert the model has what we hope
        assertTrue(model.equals(doc));
        assertTrue(_Service.getRoots().size() == 1);
        assertTrue(_Service.getTemplates().size() == 1);
        verify(doc, times(1)).setSecondsToLive(TTL_VALUE);

        model = _Service.get(keys, dimensions);
        model = _Service.get(keys, dimensions);
        model = _Service.get(keys, dimensions);
        verify(doc, times(4)).setSecondsToLive(TTL_VALUE);
        assertTrue(_Service.getRoots().size() == 1);
        assertTrue(_Service.getTemplates().size() == 1);

    }

    @Test
    public void testDeepTemplates() {

        Keys keys = mock(Keys.class);
        Set<URI> args = new HashSet<>();
        args.add(URI.create(SCRIPT_INPUT_KEY));
        when(keys.getKeyedSchemaUris()).thenReturn(args);
        when(keys.getValue(URI.create(SCRIPT_INPUT_KEY))).thenReturn("Bulwinkle");
        Dimensions dimensions = mock(Dimensions.class);
        Document doc = mock(Document.class);
        when(_Context.newModel(dimensions)).thenReturn(doc);
        URI scriptSchemaName = URI.create(DEEP_SET_SCRIPT_NAME);
        when(dimensions.getSchemaUri()).thenReturn(scriptSchemaName);
        Model model = _Service.get(keys, dimensions);

        // Assert the model has what we hope
        assertTrue(model.equals(doc));
        verify(doc, times(1)).setSecondsToLive(0L);
        model = _Service.get(keys, dimensions);

        // Assert the model has what we hope
        assertTrue(model.equals(doc));
        verify(doc, times(1)).setSecondsToLive(1L);
        model = _Service.get(keys, dimensions);

        // Assert the model has what we hope
        assertTrue(model.equals(doc));
        verify(doc, times(1)).setSecondsToLive(2L);
        model = _Service.get(keys, dimensions);

        // Assert the model has what we hope
        assertTrue(model.equals(doc));
        verify(doc, times(1)).setSecondsToLive(3L);
    }

    @Test
    public void testClearTemplates() {

        Map<URI, GroovyTemplate> templates = _Service.getTemplates();
        assertTrue(templates.isEmpty());

        testGetSet();

        assertTrue(templates.size() == 1);

        _Service.clearTemplates();

        assertTrue(templates.size() == 0);
    }


    // TODO: Reuse this from ServletTest base class
    protected ServiceConfiguration createServiceConfiguration() {

        return createServiceConfiguration(null);
    }

    // TODO: Reuse this from ServletTest base class
    protected ServiceConfiguration createServiceConfiguration(final Map<String, String> props) {

        final DefaultServiceConfiguration defaultServiceConfiguration = new DefaultServiceConfiguration();
        final SortedMap<String, String> settings = new TreeMap<>();

        if (props != null) {
            settings.putAll(props);
        }

        defaultServiceConfiguration.setSettings(settings);
        return defaultServiceConfiguration;
    }

}

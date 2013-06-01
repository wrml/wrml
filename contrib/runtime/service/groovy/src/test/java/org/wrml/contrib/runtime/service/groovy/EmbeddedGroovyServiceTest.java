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

import groovy.lang.GroovyObject;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.wrml.runtime.Context;
import org.wrml.runtime.service.ServiceException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.service.DefaultServiceConfiguration;

public class EmbeddedGroovyServiceTest 
{
    private static final String GROOVY_SERVICE_RES_LOCATION = "groovyTemplates/Server.groovy";
    private static final String GROOVY_SERVICE_REL_LOCATION = "src/test/resources/org/wrml/contrib/runtime/service/groovy/groovyTemplates/Server.groovy";
    
    private static final String SERVER_CACHE_METHOD = "getCache";
    private static final String SERVER_CALL_COUNT_METHOD = "getCallCount";
    
    private static final URI DOC_SCHEMA_ID = URI.create("moose/squirrel");
    private static final String DOC_KEY_VALUE = "Bulwinkle";
    
    private EmbeddedGroovyService _Service;
    private Context _Context;
    
    
    @Before
    public void setUp() 
    {
        _Service = new EmbeddedGroovyService();
        
        // Init
        SortedMap<String, String> config = new TreeMap<>();
        config.put(EmbeddedGroovyService.SERVICE_RES_LOCATION_KEY, GROOVY_SERVICE_RES_LOCATION);
        final DefaultServiceConfiguration defaultServiceConfiguration = new DefaultServiceConfiguration();
        defaultServiceConfiguration.setSettings(config);
        
        _Context = mock(Context.class);
        _Service.init(_Context, defaultServiceConfiguration);
    }
    
    @After
    public void tearDown() 
    {
        _Service = null;
        _Context = null;
    }
    
    @Test(expected=ServiceException.class)
    public void testInitNullConfig()
    {
        _Service.init(null, null);
    }
    
    @Test(expected=ServiceException.class)
    public void testInitBadAbsoluteLocation()
    {
        SortedMap<String, String> config = new TreeMap<>();
        config.put(EmbeddedGroovyService.SERVICE_ABS_LOCATION_KEY, "xkzy");
        final DefaultServiceConfiguration defaultServiceConfiguration = new DefaultServiceConfiguration();
        defaultServiceConfiguration.setSettings(config);
        
        _Service.init(null, defaultServiceConfiguration);
    }
    
    @Test(expected=ServiceException.class)
    public void testInitBadResourceLocation()
    {
        SortedMap<String, String> config = new TreeMap<>();
        config.put(EmbeddedGroovyService.SERVICE_RES_LOCATION_KEY, "xkzy");
        final DefaultServiceConfiguration defaultServiceConfiguration = new DefaultServiceConfiguration();
        defaultServiceConfiguration.setSettings(config);
        
        _Service.init(null, defaultServiceConfiguration);
    }
    
    @Test
    public void testInitRes()
    {
        SortedMap<String, String> config = new TreeMap<>();
        config.put(EmbeddedGroovyService.SERVICE_RES_LOCATION_KEY, GROOVY_SERVICE_RES_LOCATION);
        final DefaultServiceConfiguration defaultServiceConfiguration = new DefaultServiceConfiguration();
        defaultServiceConfiguration.setSettings(config);
        
        _Context = mock(Context.class);
        _Service.init(_Context, defaultServiceConfiguration);
        
        GroovyServiceInterface serv = _Service.getGroovyService();
        assertTrue(serv != null);
    }
    
    @Test
    public void testInitRel()
    {
        SortedMap<String, String> config = new TreeMap<>();
        config.put(EmbeddedGroovyService.SERVICE_ABS_LOCATION_KEY, GROOVY_SERVICE_REL_LOCATION);
        final DefaultServiceConfiguration defaultServiceConfiguration = new DefaultServiceConfiguration();
        defaultServiceConfiguration.setSettings(config);
        
        _Context = mock(Context.class);
        _Service.init(_Context, defaultServiceConfiguration);
        
        GroovyServiceInterface serv = _Service.getGroovyService();
        assertTrue(serv != null);
    }
    
    @Test
    public void testGetBasic()
    {
        Dimensions dims = mock(Dimensions.class);
        Document doc = mock(Document.class);
        when(doc.getSchemaUri()).thenReturn(DOC_SCHEMA_ID);
        when(_Context.newModel(dims)).thenReturn(doc);
        Keys keys = mock(Keys.class);
        when(keys.getValue(DOC_SCHEMA_ID)).thenReturn(DOC_KEY_VALUE);
        
        Model value = _Service.get(keys, dims);
        
        assertTrue(value.equals(doc));
    }
    
    @Test
    public void testPutGetBasic()
    {
        Dimensions dims = mock(Dimensions.class);
        Document doc = mock(Document.class);
        when(doc.getSchemaUri()).thenReturn(DOC_SCHEMA_ID);
        when(_Context.newModel(dims)).thenReturn(doc);
        Keys keys = mock(Keys.class);
        when(keys.getValue(DOC_SCHEMA_ID)).thenReturn(DOC_KEY_VALUE);
        when(doc.getKeys()).thenReturn(keys);
        
        Model value = _Service.save(doc);
        
        assertTrue(value != null);
        assertTrue(doc.equals(value));
        
        Model value2 = _Service.get(keys, dims);
        
        assertTrue(value != null);
        assertTrue(doc.equals(value2));
        
        GroovyServiceInterface serv = _Service.getGroovyService();
        GroovyObject obj = (GroovyObject)serv;
        
        Map cache = (Map)obj.invokeMethod(SERVER_CACHE_METHOD, null);
        
        assertTrue(cache.size() == 1);
        
        int callCount = Integer.valueOf(obj.invokeMethod(SERVER_CALL_COUNT_METHOD, null).toString());
        
        assertTrue(callCount == 2);
    }
    
    @Test
    public <M extends Model> void testPutDeleteGet()
    {
        GroovyServiceInterface serv = _Service.getGroovyService();
        GroovyObject obj = (GroovyObject)serv;
        
        Map cache = (Map)obj.invokeMethod(SERVER_CACHE_METHOD, null);
        
        int callCount = Integer.valueOf(obj.invokeMethod(SERVER_CALL_COUNT_METHOD, null).toString());
        
        assertTrue(cache.size() == 0);
        assertTrue(callCount == 0);
        
        Dimensions dims = mock(Dimensions.class);
        Document doc = mock(Document.class);
        when(doc.getSchemaUri()).thenReturn(DOC_SCHEMA_ID);
        when(_Context.newModel(dims)).thenReturn(doc);
        Keys keys = mock(Keys.class);
        Set<URI> schemaUris = new HashSet<>();
        schemaUris.add(DOC_SCHEMA_ID);
        when(keys.getKeyedSchemaUris()).thenReturn(schemaUris);
        when(keys.getValue(DOC_SCHEMA_ID)).thenReturn(DOC_KEY_VALUE);
        when(doc.getKeys()).thenReturn(keys);
        
        Model value = _Service.save(doc);
        
        callCount = Integer.valueOf(obj.invokeMethod(SERVER_CALL_COUNT_METHOD, null).toString());
        
        assertTrue(value != null);
        assertTrue(doc.equals(value));
        assertTrue(cache.size() == 1);
        assertTrue(callCount == 1);
        
        _Service.delete(keys, value.getDimensions());
        
        callCount = Integer.valueOf(obj.invokeMethod(SERVER_CALL_COUNT_METHOD, null).toString());
        
        assertTrue(callCount == 2);
        assertTrue(cache.size() == 0);
    }
}

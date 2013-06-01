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
package org.wrml.runtime;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class EngineTest
{

    /**
     * NOTE: This is a JVM-based (e.g jar-contained) resource path.
     */
    public static final String WRML_CONFIG_RESOURCE = "resources/test/wrml.json";
    
    public static final String WRML_CONFIG_API_RESOURCE = "resources/test/wrml-api.json";
    
    

    public static final Engine createTestEngine() throws IOException
    {
        return createTestEngine(EngineTest.class, WRML_CONFIG_RESOURCE);
    }

    public static final Engine createTestEngine(final Class<?> forClass, final String wrmlConfigResource) throws IOException
    {

        final Engine engine = new DefaultEngine();
        final EngineConfiguration config = EngineConfiguration.load(forClass, wrmlConfigResource);
        engine.init(config);
        return engine;
    }
    
    public static final Engine createTestEngine(final EngineConfiguration config)
    {
        final Engine engine = new DefaultEngine();
        engine.init(config);
        return engine;
    }

    private Engine _Engine;

    @Before
    public void setUp() throws Exception
    {
        _Engine = EngineTest.createTestEngine();
    }

    @After
    public void tearDown() throws Exception
    {

        _Engine = null;
    }

    @Test(expected = EngineException.class)
    public void initParamConfigNullFailure()
    {

        final Engine engine = new DefaultEngine();
        engine.init(null);
    }

    @Test
    public void engineNotNull()
    {
        Assert.assertNotNull(_Engine);
    }


    @Test
    public void contextNotNull()
    {

        final Context context = _Engine.getContext();
        Assert.assertNotNull(context);

    }

    @Test
    public void configNotNull()
    {

        final EngineConfiguration config = _Engine.getConfig();
        Assert.assertNotNull(config);
    }



    @Test
    public void reloadContextResultNotNull()
    {

        final Context reloadedContext = _Engine.reloadContext();
        Assert.assertNotNull(reloadedContext);
    }

    @Test
    public void reloadContextResultNotEqual()
    {

        final Context originalContext = _Engine.getContext();
        final Context reloadedContext = _Engine.reloadContext();

        Assert.assertNotEquals(originalContext, reloadedContext);
    }

    @Test
    public void testWithApi() throws IOException
    {
        _Engine = createTestEngine(EngineTest.class, WRML_CONFIG_API_RESOURCE);
        Assert.assertNotNull(_Engine);
    }
}

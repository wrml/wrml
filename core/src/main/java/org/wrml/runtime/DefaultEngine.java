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
package org.wrml.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.util.AsciiArt;

/**
 * <p>
 * The WRML runtime's default {@link Engine} implementation.
 * </p>
 * 
 * @see EngineConfiguration
 * @see Context
 * @see org.wrml.runtime.syntax.SyntaxLoader
 */
public class DefaultEngine implements Engine
{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEngine.class);

    private static final Class<?> DEFAULT_CONTEXT_FACTORY = DefaultContextFactory.class;

    private EngineConfiguration _Config;

    private Context _Context;

    private Factory<Context> _ContextFactory;

    public DefaultEngine()
    {

        LOG.info(AsciiArt.LOGO);
        LOG.info("Greetings Program!");
    }

    @Override
    public void init(final EngineConfiguration config) throws EngineException
    {

        if (config == null)
        {
            throw new EngineException("The WRML engine configuration cannot be null.", null, this);
        }

        LOG.info("Creating Engine with config:\n" + config);

        _Config = config;
        try
        {
            _ContextFactory = createContextFactory();
        }
        catch (Exception t)
        {
            throw new EngineException(t.getMessage(), t, this);
        }

        reloadContext();
    }

    @Override
    public final EngineConfiguration getConfig()
    {

        return _Config;
    }

    @Override
    public final Context getContext()
    {

        return _Context;
    }

    @Override
    public final Context reloadContext() throws EngineException
    {

        try
        {
            _Context = createContext();
            _Context.init(getConfig().getContext());
        }
        catch (Exception t)
        {
            throw new EngineException(t.getMessage(), t, this);
        }

        return _Context;
    }

    @Override
    public String toString()
    {

        return getClass().getSimpleName() + " { config : " + _Config + ", context : " + _Context + "}";
    }

    protected Context createContext()
    {

        return _ContextFactory.create();
    }

    protected Factory<Context> createContextFactory()
    {

        return DefaultFactoryConfiguration.createFactory(getConfig().getContext(), DEFAULT_CONTEXT_FACTORY);
    }

}

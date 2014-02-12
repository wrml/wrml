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
package org.wrml.runtime.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.runtime.Context;
import org.wrml.runtime.ContextConfiguration;
import org.wrml.runtime.DefaultConfiguration;
import org.wrml.util.WildCardPrefixTree;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * The WRML runtime's default {@link ServiceLoader}; a registry of {@link Service}s.
 * </p>
 *
 * @see Service
 */
public final class DefaultServiceLoader implements ServiceLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultServiceLoader.class);

    private Context _Context;

    private Map<String, Service> _Services;

    private WildCardPrefixTree<Service> _SchemaToServiceTrie;


    public DefaultServiceLoader() {

    }

    @Override
    public Set<String> getServiceNames() {

        return _Services.keySet();
    }

    @Override
    public Service getService(final String serviceName) {

        if (!_Services.containsKey(serviceName)) {
            return null;
        }

        return _Services.get(serviceName);
    }

    @Override
    public final Service getServiceForSchema(final URI schemaUri) {

        final String schemaPath = schemaUri.toString();
        return _SchemaToServiceTrie.getPathValue(schemaPath);
    }

    @Override
    public Collection<Service> getServices() {

        return _Services.values();
    }

    @Override
    public final void loadConfiguredService(final ServiceConfiguration serviceConfiguration) {

        if (serviceConfiguration == null) {
            return;
        }

        final Context context = getContext();
        LOG.info("Creating and configuring service {} from configuration.",
                new Object[]{serviceConfiguration.getName()});
        final String serviceClassName = serviceConfiguration.getImplementation();
        final Service service = DefaultConfiguration.newInstance(serviceClassName);
        service.init(context, serviceConfiguration);
        loadService(service, serviceConfiguration.getName());
    }

    @Override
    public final void loadService(final Service service, final String serviceName) {

        final String mappedServiceName = (serviceName == null) ? service.getClass().getSimpleName() : serviceName;
        _Services.put(mappedServiceName, service);
    }

    @Override
    public final void mapSchemaPatternToService(final String schemaUriPattern, final String serviceName) {

        final Service service = getService(serviceName);
        if (service != null) {
            _SchemaToServiceTrie.setPathValue(schemaUriPattern, service);
        }
    }

    @Override
    public Context getContext() {

        return _Context;
    }

    @Override
    public void init(final Context context) {

        _Context = context;

        _Services = new ConcurrentHashMap<String, Service>();
        _SchemaToServiceTrie = new WildCardPrefixTree<>();

    }

    @Override
    public void loadInitialState() {

        loadConfiguredServices();
    }

    protected void loadConfiguredServices() {

        final Context context = getContext();
        final ContextConfiguration contextConfig = context.getConfig();
        final ServiceLoaderConfiguration config = contextConfig.getServiceLoader();
        final ServiceConfiguration[] serviceConfigs = config.getServices();
        if ((serviceConfigs == null) || (serviceConfigs.length == 0)) {
            LOG.info("No services to configure.");
            return;
        }


        for (final ServiceConfiguration serviceConfiguration : serviceConfigs) {
            loadConfiguredService(serviceConfiguration);
        }

        final Map<String, String> serviceMapping = config.getServiceMapping();
        if (serviceMapping != null && !serviceMapping.isEmpty()) {
            for (final String schemaUriPattern : serviceMapping.keySet()) {
                final String serviceName = serviceMapping.get(schemaUriPattern);
                mapSchemaPatternToService(schemaUriPattern, serviceName);
            }
        }

    }


}

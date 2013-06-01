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
import org.wrml.runtime.rest.ApiNavigator;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * The WRML runtime's default {@link ServiceLoader}; a registry of {@link Service}s.
 * </p>
 *
 * @see Service
 */
public final class DefaultServiceLoader implements ServiceLoader
{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultServiceLoader.class);

    private Context _Context;

    private Map<String, Service> _Services;

    private SchemaToServiceTrie _SchemaToServiceTrie;


    public DefaultServiceLoader()
    {

    }

    @Override
    public Set<String> getServiceNames()
    {

        return _Services.keySet();
    }

    @Override
    public Service getService(final String serviceName)
    {

        if (!_Services.containsKey(serviceName))
        {
            return null;
        }

        return _Services.get(serviceName);
    }

    @Override
    public final Service getServiceForSchema(final URI schemaUri)
    {

        final String schemaPath = schemaUri.toString();
        return _SchemaToServiceTrie.matchPathExtraWilds(schemaPath, null);
    }

    @Override
    public Collection<Service> getServices()
    {

        return _Services.values();
    }

    @Override
    public final void loadConfiguredService(final ServiceConfiguration serviceConfiguration)
    {

        if (serviceConfiguration == null)
        {
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
    public final void loadService(final Service service, final String serviceName)
    {

        final String mappedServiceName = (serviceName == null) ? service.getClass().getSimpleName() : serviceName;
        _Services.put(mappedServiceName, service);
    }

    @Override
    public final void mapSchemaPatternToService(final String schemaUriPattern, final String serviceName)
    {

        final Service service = getService(serviceName);
        if (service != null)
        {
            _SchemaToServiceTrie.addPath(schemaUriPattern, service);
        }
    }

    @Override
    public Context getContext()
    {

        return _Context;
    }

    @Override
    public void init(final Context context)
    {

        _Context = context;

        _Services = new ConcurrentHashMap<String, Service>();
        _SchemaToServiceTrie = new SchemaToServiceTrie(false);

    }

    @Override
    public void loadInitialState()
    {

        loadConfiguredServices();
    }

    protected void loadConfiguredServices()
    {

        final Context context = getContext();
        final ContextConfiguration contextConfig = context.getConfig();
        final ServiceLoaderConfiguration config = contextConfig.getServiceLoader();
        final ServiceConfiguration[] serviceConfigs = config.getServices();
        if ((serviceConfigs == null) || (serviceConfigs.length == 0))
        {
            LOG.info("No services to configure.");
            return;
        }


        for (final ServiceConfiguration serviceConfiguration : serviceConfigs)
        {
            loadConfiguredService(serviceConfiguration);
        }

        final Map<String, String> serviceMapping = config.getServiceMapping();
        if (serviceMapping != null && !serviceMapping.isEmpty())
        {
            for (final String schemaUriPattern : serviceMapping.keySet())
            {
                final String serviceName = serviceMapping.get(schemaUriPattern);
                mapSchemaPatternToService(schemaUriPattern, serviceName);
            }
        }

    }

    /**
     * Uses the UriTrieNode class to perform service matching based on incoming
     * URI's with capture groups.
     *
     * @see UriTrieNode
     */
    static class SchemaToServiceTrie
    {

        private final UriTrieNode _Head;

        private final boolean _AllowNonterminalWildcards;

        public SchemaToServiceTrie(final boolean allowNonterminalWildcards)
        {

            _Head = new UriTrieNode();
            _AllowNonterminalWildcards = allowNonterminalWildcards;
        }

        public void addPath(final String path, final Service service)
        {

            if (service == null)
            {
                throw new IllegalArgumentException("Cannot add a path to the UriServiceMap with a null service.");
            }

            UriTrieNode node = _Head;
            final List<String> segments = segmentPath(path);

            for (int i = 0; i < segments.size(); i++)
            {
                final String segment = segments.get(i);

                // is this the right place for this?
                if (!_AllowNonterminalWildcards && segment != null && segment.equals(UriTrieNode.WILDCARD)
                        && (i + 1) < segments.size())
                {
                    throw new RuntimeException(
                            "Bad path provided to construct a mapping with only terminal wildcards.\n" + path);
                }

                if (node.hasLink(segment))
                {
                    node = node.getLink(segment);
                }
                else
                {
                    node = node.addLink(segment, null);
                }
            }

            node.setService(service);
        }

        public String deepPrint()
        {

            final Set<String> paths = _Head.deepPrint('/');
            final StringBuilder sb = new StringBuilder();
            for (final String p : paths)
            {
                sb.append(p).append('\n');
            }
            return sb.toString();
        }

        public Service matchPath(final String path)
        {

            return matchPath(path, null);
        }

        public Service matchPath(final String path, final List<String> captures)
        {

            final List<String> segments = segmentPath(path);
            UriTrieNode node = _Head;

            for (int i = 0; i < segments.size(); i++)
            {
                final String segment = segments.get(i);
                if (node.hasLink(segment))
                {
                    node = node.getLink(segment);
                }
                else if (node.hasWildcard())
                {
                    // May remove this if full responsibility is placed on addPath
                    if (!_AllowNonterminalWildcards && (i + 1) < segments.size())
                    {
                        throw new RuntimeException("Bad path, " + path + " provided to " + this.getClass().getName()
                                + " with setting _AllowNonterminalWildcards false.");
                    }

                    if (captures != null)
                    {
                        captures.add(segment);
                    }
                    node = node.getWildcardLink();
                }
                else
                {
                    return null;
                }
            }

            return node.getService();
        }

        /**
         * This method will allow a capture group to be used for more than one segment,
         * but only at the end of the match, and only as a last resort.
         *
         * @param path     the uri path, complete with /, to match
         * @param captures a List of Strings in which to record wildcard matches
         * @return the List of Services at the final node which matched the path
         */
        public Service matchPathExtraWilds(final String path, final List<String> captures)
        {
            /*
             * if (_AllowNonterminalWildcards)
             * {
             * throw new RuntimeException("UriServiceMap not designed (presently) to match " +
             * "multiple segments per wild with non-terminal wildcards.");
             * }
             */

            final List<String> segments = segmentPath(path);

            return matchPathExtraWilds(_Head, segments, captures);
        }

        private Service matchPathExtraWilds(final UriTrieNode node, final List<String> segments,
                                            final List<String> captures)
        {

            if (segments.isEmpty())
            {
                return node.getService();
            }

            Service service = null;
            final String segment = segments.remove(0);

            if (node.hasLink(segment))
            {
                service = matchPathExtraWilds(node.getLink(segment), segments, captures);
            }

            if (null == service)
            {
                if (node.hasWildcard())
                {
                    if (captures != null)
                    {
                        captures.add(segment);
                        captures.addAll(segments);
                    }
                    service = node.getWildcardLink().getService();
                }
            }

            segments.add(0, segment);

            return service;
        }

        private List<String> segmentPath(final String path)
        {

            String tPath = path.trim();
            if (path.endsWith(ApiNavigator.PATH_SEPARATOR))
            {
                tPath = tPath.substring(0, path.length() - 1);
            }
            if (path.startsWith(ApiNavigator.PATH_SEPARATOR))
            {
                tPath = tPath.substring(1);
            }

            final List<String> segments = new LinkedList<>(Arrays.asList(tPath.split(ApiNavigator.PATH_SEPARATOR)));
            return segments;
        }
    }

    /**
     * This is the node class for a uri-segment-mapping trie, a form of non-binary tree used
     * for fast lookup of progressively-searchable items
     *
     * @see <a href="https://en.wikipedia.org/wiki/Trie">Wikipedia's Trie entry</a>
     */
    private static class UriTrieNode
    {


        public static final UriTrieNode EMPTY_NODE = new UriTrieNode();

        public static final String WILDCARD = "*";

        private final Map<String, UriTrieNode> _SegmentNodeMap;

        private Service _Service;

        public UriTrieNode()
        {

            _SegmentNodeMap = new HashMap<>();
            _Service = null;
        }

        public UriTrieNode(final Service service)
        {

            _SegmentNodeMap = new HashMap<>();
            _Service = service;
        }

        public UriTrieNode addLink(final String segment, final Service service)
        {

            if (hasLink(segment))
            {
                throw new RuntimeException("There is already a service at this path, "
                        + "could not add more to incremental path " + segment);
            }

            final UriTrieNode newNode = new UriTrieNode(service);
            _SegmentNodeMap.put(segment, newNode);

            return newNode;
        }

        public Set<String> deepPrint(final char separator)
        {

            final Set<String> paths = new TreeSet<>();

            if (_Service != null)
            {
                // TODO Flesh this out
                paths.add("");
            }

            for (final String s : _SegmentNodeMap.keySet())
            {
                for (final String subp : _SegmentNodeMap.get(s).deepPrint(separator))
                {
                    if (subp.isEmpty())
                    {
                        paths.add(s);
                    }
                    else
                    {
                        paths.add(s + separator + subp);
                    }
                }
            }

            return paths;
        }

        public UriTrieNode getLink(final String segment)
        {

            if (hasLink(segment))
            {
                return _SegmentNodeMap.get(segment);
            }
            else
            {
                return EMPTY_NODE;
            }
        }

        public Service getService()
        {

            return _Service;
        }

        public void setService(final Service service)
        {

            _Service = service;
        }

        public UriTrieNode getWildcardLink()
        {

            return _SegmentNodeMap.get(WILDCARD);
        }

        public boolean hasLink(final String segment)
        {

            if (_SegmentNodeMap.containsKey(segment))
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean hasWildcard()
        {

            return hasLink(WILDCARD);
        }

        @Override
        public String toString()
        {

            final StringBuilder sb = new StringBuilder("Services: [");
            if (_Service != null)
            {
                sb.append(_Service.toString());
                sb.append(", ");
            }

            sb.append("]\nPaths: [");
            for (final String link : _SegmentNodeMap.keySet())
            {
                sb.append(link).append(", ");
            }
            sb.append("]");

            return sb.toString();
        }
    }

}

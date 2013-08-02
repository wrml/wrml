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
package org.wrml.util;

import java.util.*;

public abstract class PrefixTreeBase<T> implements PrefixTree<T>
{
    public static final String DEFAULT_PATH_SEPARATOR = "/";

    public static final String PROTOCOL_PREFIX = "://";

    private final PrefixTreeNode<T> _RootNode;

    private final String _PathSeparator;

    public PrefixTreeBase()
    {
        this(DEFAULT_PATH_SEPARATOR);
    }

    public PrefixTreeBase(final String pathSeparator)
    {
        _PathSeparator = pathSeparator;
        _RootNode = new PrefixTreeNode<>();
    }


    public String getPathSeparator()
    {
        return _PathSeparator;
    }

    public PrefixTreeNode<T> getRoot()
    {

        return _RootNode;
    }

    @Override
    public void setPathValue(final String path, final T value)
    {

        PrefixTreeNode node = getRoot();
        final List<String> segments = segmentPath(path);

        for (final String segment : segments)
        {
            if (node.hasChild(segment))
            {
                node = node.getChild(segment);
            }
            else
            {
                node = node.addChild(segment, null);
            }
        }

        node.setValue(value);
    }

    public String toString()
    {

        final String pathSeparator = getPathSeparator();
        final Set<String> paths = getRoot().deepPrint(pathSeparator);
        final StringBuilder sb = new StringBuilder();
        for (final String p : paths)
        {
            sb.append(p).append('\n');
        }
        return sb.toString();
    }

    protected List<String> segmentPath(final String path)
    {

        final String pathSeparator = getPathSeparator();

        String trimmedPath = path.trim();

        final int protocolPrefixIndex = trimmedPath.indexOf(PROTOCOL_PREFIX);
        if (protocolPrefixIndex >= 0)
        {
            trimmedPath = trimmedPath.substring(protocolPrefixIndex + PROTOCOL_PREFIX.length());
        }

        if (trimmedPath.endsWith(pathSeparator))
        {
            trimmedPath = trimmedPath.substring(0, trimmedPath.length() - 1);
        }

        if (trimmedPath.startsWith(pathSeparator))
        {
            trimmedPath = trimmedPath.substring(1);
        }

        String[] segmentArray = trimmedPath.split(pathSeparator);
        if (segmentArray.length == 0)
        {
            return Collections.EMPTY_LIST;
        }

        final List<String> segments = new LinkedList<>(Arrays.asList(segmentArray));
        return segments;
    }
}

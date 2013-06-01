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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PrefixTreeNode<T>
{
    protected final Map<String, PrefixTreeNode<T>> _Links;

    private T _Value;

    public static final PrefixTreeNode EMPTY_NODE = new PrefixTreeNode();

    public PrefixTreeNode()
    {
        _Links = new HashMap<>();
        _Value = null;
    }

    public PrefixTreeNode(final T value)
    {
        _Links = new HashMap<String, PrefixTreeNode<T>>();
        _Value = value;
    }

    public PrefixTreeNode addLink(final String segment, final T value)
    {
        final PrefixTreeNode<T> newNode = new PrefixTreeNode<>(value);
        _Links.put(segment, newNode);

        return newNode;
    }

    public Set<String> deepPrint(final char separator)
    {
        final Set<String> paths = new TreeSet<String>();

        if (_Value != null)
        {
            paths.add(_Value.toString());
        }

        for (final String s : _Links.keySet())
        {
            for (final String subp : _Links.get(s).deepPrint(separator))
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

    public PrefixTreeNode<T> getLink(final String segment)
    {
        if (hasLink(segment))
        {
            return _Links.get(segment);
        }
        else
        {
            return EMPTY_NODE;
        }
    }

    public T getValue()
    {
        return _Value;
    }

    public boolean hasLink(final String segment)
    {
        if (_Links.containsKey(segment))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void setValue(final T value)
    {
        _Value = value;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Value: [");
        if (_Value != null)
        {
            sb.append(_Value.toString());
        }
        sb.append("]\nPaths: [");
        for (final String link : _Links.keySet())
        {
            sb.append(link).append(", ");
        }
        sb.append("]");

        return sb.toString();
    }
}

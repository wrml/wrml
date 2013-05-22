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
 * Copyright 2012 Mark Masse (OSS project WRML.org)
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

import java.util.List;
import java.util.Set;

public class SimplePrefixTree<T> extends PrefixTreeBase<T>
{
    private final PrefixTreeNode<T> head;

    public SimplePrefixTree()
    {
        head = new PrefixTreeNode<>();
    }

    @Override
    public void setPath(final String path, final T value)
    {
        PrefixTreeNode node = head;
        final List<String> segments = segmentPath(path);

        for (int i = 0; i < segments.size(); i++)
        {
            final String segment = segments.get(i);

            if (node.hasLink(segment))
            {
                node = node.getLink(segment);
            }
            else
            {
                node = node.addLink(segment, null);
            }
        }

        node.setValue(value);
    }

    public String deepPrint()
    {
        final Set<String> paths = head.deepPrint('/');
        final StringBuilder sb = new StringBuilder();
        for (final String p : paths)
        {
            sb.append(p).append('\n');
        }
        return sb.toString();
    }

    @Override
    public T matchPath(final String path)
    {
        return matchPathIter(path);
    }

    private T matchPathIter(final String path)
    {
        final List<String> segments = segmentPath(path);
        PrefixTreeNode<T> node = head;

        for (int i = 0; i < segments.size(); i++)
        {
            final String segment = segments.get(i);
            if (node.hasLink(segment))
            {
                node = node.getLink(segment);
            }
            else
            {
                return null;
            }
        }

        return node.getValue();
    }
}

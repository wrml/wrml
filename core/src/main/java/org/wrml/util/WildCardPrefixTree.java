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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class WildCardPrefixTree<T> extends PrefixTreeBase<T>
{
    public static final String WILDCARD = "*";
    
    private final PrefixTreeNode<T> head;

    private final boolean allowNonterminalWildcards;

    public WildCardPrefixTree(final boolean allowNonterminalWildcards)
    {
        head = new PrefixTreeNode<>();
        this.allowNonterminalWildcards = allowNonterminalWildcards;
    }

    public void setPath(final String path, final T value)
    {
        PrefixTreeNode node = head;
        final List<String> segments = segmentPath(path);

        for (int i = 0; i < segments.size(); i++)
        {
            final String segment = segments.get(i);

            // is this the right place for this?
            if (!allowNonterminalWildcards && segment.equals(WildCardPrefixTree.WILDCARD)
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
        final List<String> segments = segmentPath(path);
        final List<String> captures = new LinkedList<String>();
        return matchPathExtraWilds(head, segments, captures);
    }

    /**
     * This method will allow a capture group to be used for more than one segment,
     * but only at the end of the match, and only as a last resort.
     * 
     * @param path
     *            the uri path, complete with /, to match
     * @param captures
     *            a List of Strings in which to record wildcard matches
     * @return the List of Services at the final node which matched the path
     */
    private T matchPathExtraWilds(final PrefixTreeNode<T> node, final List<String> segments,
            final List<String> captures)
    {
        if (segments.isEmpty())
        {
            return node.getValue();
        }

        T value = null;
        final String segment = segments.remove(0);

        if (node.hasLink(segment))
        {
            value = matchPathExtraWilds(node.getLink(segment), segments, captures);
        }

        if (null == value)
        {
            if (node.hasLink(WildCardPrefixTree.WILDCARD))
            {
                if (captures != null)
                {
                    captures.add(segment);
                    captures.addAll(segments);
                }
                
                value = node.getLink(WildCardPrefixTree.WILDCARD).getValue();
            }
        }

        segments.add(0, segment);

        return value;
    }
}

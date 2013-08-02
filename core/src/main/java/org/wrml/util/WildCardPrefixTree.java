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

import java.util.List;

/**
 * A {@link PrefixTree} that allows a wildcard (*) segment at the end of the path to allow multiple paths to "match" the same value.
 *
 * @param <T> The value type of the tree's {@link PrefixTreeNode}s.
 */
public class WildCardPrefixTree<T> extends PrefixTreeBase<T>
{
    public static final String WILDCARD_SEGMENT = "*";

    @Override
    public T getPathValue(final String path)
    {

        // TODO: Replace with regex?

        final List<String> segments = segmentPath(path);
        PrefixTreeNode<T> node = getRoot();

        PrefixTreeNode<T> wildCardNode = null;

        final int segmentCount = segments.size();
        for (int i = 0; i < segmentCount; i++)
        {

            if (node.hasChild(WildCardPrefixTree.WILDCARD_SEGMENT))
            {
                wildCardNode = node.getChild(WildCardPrefixTree.WILDCARD_SEGMENT);
            }

            final String segment = segments.get(i);

            if (node.hasChild(segment))
            {
                node = node.getChild(segment);
            }
            else if (i == segmentCount - 1)
            {
                // Back up to wildcard node (or null if none existed)
                node = wildCardNode;
            }
        }

        if (node == null)
        {
            // No matches
            return null;
        }

        // So path values that exactly match a prefix will also match the "empty" wildcard (*)
        if (node != wildCardNode && node.hasChild(WildCardPrefixTree.WILDCARD_SEGMENT))
        {
            node = node.getChild(WildCardPrefixTree.WILDCARD_SEGMENT);
        }

        return node.getValue();
    }

}

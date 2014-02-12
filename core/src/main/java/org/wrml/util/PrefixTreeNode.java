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

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class PrefixTreeNode<T> {
    protected final ConcurrentHashMap<String, PrefixTreeNode<T>> _Children;

    private T _Value;

    public PrefixTreeNode() {

        this(null);
    }

    public PrefixTreeNode(final T value) {

        _Children = new ConcurrentHashMap<>();
        _Value = value;
    }

    public PrefixTreeNode addChild(final String segment, final T value) {

        final PrefixTreeNode<T> childNode = new PrefixTreeNode<>(value);
        _Children.put(segment, childNode);

        return childNode;
    }

    public Set<String> deepPrint(final String separator) {

        final Set<String> paths = new TreeSet<String>();

        if (_Value != null) {
            paths.add(_Value.toString());
        }

        for (final String s : _Children.keySet()) {
            for (final String segment : _Children.get(s).deepPrint(separator)) {
                if (segment.isEmpty()) {
                    paths.add(s);
                }
                else {
                    paths.add(s + separator + segment);
                }
            }
        }

        return paths;
    }

    public PrefixTreeNode<T> getChild(final String segment) {

        if (hasChild(segment)) {
            return _Children.get(segment);
        }

        return null;
    }

    public T getValue() {

        return _Value;
    }

    public boolean hasChild(final String segment) {

        return _Children.containsKey(segment);
    }

    public void setValue(final T value) {

        _Value = value;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("Value: [");
        if (_Value != null) {
            sb.append(_Value.toString());
        }
        sb.append("]\nChildren: [");
        for (final String link : _Children.keySet()) {
            sb.append(link).append(", ");
        }
        sb.append("]");

        return sb.toString();
    }
}

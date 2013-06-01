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

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.net.URI;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Similar to a {@link URI}'s path component, a {@link UniqueName} is a forward slash delimited {@link String} consisting of a <i>namespace</i> part and a <i>local name</i> part.
 * </p>
 * <p>
 * The namespace part of the {@link UniqueName} may contain any number of string "segments" separated by forward slashes.
 * </p>
 * <p>
 * The local name part follows the final forward slash and may not contain any forward slashes.
 * </p>
 * <p>
 * Examples:
 * </p>
 * <p>
 * <ul>
 * <li>org/wrml/model/rest/Document</li>
 * <li>org/wrml/model/schema/Schema</li>
 * <li>org/wrml/relation/self</li>
 * </ul>
 * </p>
 *
 * @see java.net.URI#getPath()
 * @see java.nio.file.Path
 */
public final class UniqueName implements Comparable<UniqueName>, Serializable
{

    public static final char NAME_SEPARATOR_CHAR = '/';

    public static final String NAME_SEPARATOR = String.valueOf(UniqueName.NAME_SEPARATOR_CHAR);

    private static final long serialVersionUID = 1L;

    // private static final Logger LOGGER = LoggerFactory.getLogger(UniqueName.class);

    public static Comparator<UniqueName> ALPHA_ORDER = new Comparator<UniqueName>()
    {

        @Override
        public int compare(final UniqueName uniqueName1, final UniqueName uniqueName2)
        {

            return ComparisonChain.start().compare(uniqueName1.getFullName(), uniqueName2.getFullName()).result();
        }

    };

    private static AtomicInteger __TemporaryLocalNameCounter = new AtomicInteger(1);

    private final String _Namespace;

    private final String _LocalName;

    private String _FullName;

    private Integer _HashCode;

    public UniqueName(final String uniqueNameString)
    {

        if (StringUtils.isEmpty(uniqueNameString) || uniqueNameString.equals(UniqueName.NAME_SEPARATOR))
        {
            _Namespace = "";
            _LocalName = "";
        }
        else if (uniqueNameString.endsWith(UniqueName.NAME_SEPARATOR))
        {
            _Namespace = uniqueNameString.substring(0, uniqueNameString.length() - 1);
            _LocalName = "";
        }
        else
        {
            final int lastSeparator = uniqueNameString.lastIndexOf(UniqueName.NAME_SEPARATOR_CHAR);

            if (lastSeparator < 1)
            {
                _Namespace = uniqueNameString;
                _LocalName = "";
            }
            else
            {
                _Namespace = uniqueNameString.substring(0, lastSeparator);
                _LocalName = uniqueNameString.substring(lastSeparator + 1);
            }
        }
    }

    public UniqueName(final String namespace, final String localName)
    {

        _Namespace = (namespace != null) ? namespace : "";
        _LocalName = (localName != null) ? localName : "";
    }

    public UniqueName(final String part1, final String part2, final String... otherParts)
    {

        this(part1 + NAME_SEPARATOR + part2 + NAME_SEPARATOR + StringUtils.join(otherParts, NAME_SEPARATOR_CHAR));
    }

    public UniqueName(final UniqueName namespace, final String localName)
    {

        this((namespace != null) ? namespace.toString() : null, localName);
    }

    public UniqueName(final URI uri)
    {

        this(StringUtils.stripStart(uri.getPath(), "/"));
    }

    public static UniqueName createTemporaryUniqueName()
    {

        return new UniqueName("temp" + UniqueName.NAME_SEPARATOR + "Temp" + __TemporaryLocalNameCounter.getAndIncrement());
    }

    @Override
    public final int compareTo(final UniqueName other)
    {

        return UniqueName.ALPHA_ORDER.compare(this, other);
    }

    public boolean equalNamespaces(final UniqueName otherUniqueName)
    {

        if (this == otherUniqueName)
        {
            return true;
        }
        if (otherUniqueName == null)
        {
            return false;
        }
        if (_Namespace == null)
        {
            if (otherUniqueName._Namespace != null)
            {
                return false;
            }
        }
        else if (!_Namespace.equals(otherUniqueName._Namespace))
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(final Object other)
    {

        if (this == other)
        {
            return true;
        }
        if (other == null)
        {
            return false;
        }
        if (getClass() != other.getClass())
        {
            return false;
        }
        final UniqueName otherUniqueName = (UniqueName) other;
        if (_LocalName == null)
        {
            if (otherUniqueName._LocalName != null)
            {
                return false;
            }
        }
        else if (!_LocalName.equals(otherUniqueName._LocalName))
        {
            return false;
        }
        if (_Namespace == null)
        {
            if (otherUniqueName._Namespace != null)
            {
                return false;
            }
        }
        else if (!_Namespace.equals(otherUniqueName._Namespace))
        {
            return false;
        }
        return true;
    }

    public String getLocalName()
    {

        return _LocalName;
    }

    /**
     * Returns the forward-slash separated namespace (path), which along with the schema's (local) name, uniquely identifies the schema within the WRML runtime.
     *
     * @return the forward slash-separated (/) namespace (path) string without a leading or a trailing forward slash.
     */
    public String getNamespace()
    {

        return _Namespace;
    }

    /**
     * <p>
     * Returns the full name of this {@link UniqueName}, which is the namespace part prepended (separated with a forward slash) to the local name part.
     * </p>
     *
     * @return <code>namespace + / + localName</code>
     */
    public String getFullName()
    {

        if (_FullName == null)
        {
            final String namespace = getNamespace();
            final String localName = getLocalName();

            if (namespace != null && localName != null)
            {
                String suffix = localName.trim();
                if (!suffix.isEmpty())
                {
                    suffix = UniqueName.NAME_SEPARATOR + suffix;
                }

                _FullName = namespace + suffix;
            }
            else if (namespace == null && localName == null)
            {
                _FullName = "";
            }
            else if (namespace == null && localName != null)
            {
                _FullName = localName;
            }
            else
            {
                _FullName = namespace;
            }

        }
        return _FullName;

    }

    @Override
    public int hashCode()
    {

        if (_HashCode == null)
        {
            _HashCode = Objects.hashCode(this._LocalName, this._Namespace, this._FullName);
        }

        return _HashCode;
    }

    @Override
    public String toString()
    {

        return getFullName();
    }

}

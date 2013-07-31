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
package org.wrml.runtime.rest;

import org.apache.commons.lang3.StringUtils;
import org.wrml.model.rest.CommonHeader;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaType implements Comparable<MediaType>
{

    public static final String NAME_SEPARATOR = "/";

    public static final String WILDCARD = "*";

    public static final String PRECEDENCE_PARAM = "q";

    public static final Pattern CONTENT_TYPE_HEADER_PATTERN = Pattern.compile(CommonHeader.CONTENT_TYPE.getName() + ": (.*)");

    public static final Pattern PARAM_SEPARATOR_PATTERN = Pattern.compile("( +)?([^;]*)((;( +)?(.*))+)?");

    public static final Pattern NAME_SEPARATOR_PATTERN = Pattern.compile("(.*)/(.*)");

    public static final Pattern EQUAL_SEPARATOR_PATTERN = Pattern.compile(";( *)([^=]+)=?(\"([^\"]+)(\"))?([^;]*)?");

    private final String _Type;

    private final String _SubType;

    private final Map<String, String> _Parameters;

    public MediaType(final String mediaTypeString) throws MediaTypeException
    {

        final Matcher contentTypePrefixMatcher = CONTENT_TYPE_HEADER_PATTERN.matcher(mediaTypeString);

        // Strip off Content-Type string if present
        String shortString = mediaTypeString;
        if (contentTypePrefixMatcher.find())
        {
            shortString = contentTypePrefixMatcher.group(1);
        }

        final Matcher content = PARAM_SEPARATOR_PATTERN.matcher(shortString);
        if (content.find())
        {
            final String combinedType = content.group(2);
            final String combinedParams = content.group(3);
            final Matcher typeNameMatcher = NAME_SEPARATOR_PATTERN.matcher(combinedType);
            if (typeNameMatcher.find())
            {
                _Type = typeNameMatcher.group(1);
                _SubType = typeNameMatcher.group(2);
            }
            else
            {
                throw new MediaTypeException("Unable to extract major/minor type information.");
            }

            _Parameters = new HashMap<String, String>();
            if (combinedParams != null && !combinedParams.equals(""))
            {
                final Matcher paramSeparator = EQUAL_SEPARATOR_PATTERN.matcher(combinedParams);
                while (paramSeparator.find())
                {
                    if (paramSeparator.group(4) != null)
                    {
                        _Parameters.put(paramSeparator.group(2), paramSeparator.group(4));
                    }
                    else
                    {
                        _Parameters.put(paramSeparator.group(2), paramSeparator.group(6));
                    }
                }
            }
        }
        else
        {
            throw new MediaTypeException("Unable to construct MediaType from string " + mediaTypeString);
        }
    }

    public MediaType(final String type, final String subtype)
    {

        this(type, subtype, null);
    }

    public MediaType(final String type, final String subtype, final Map<String, String> parameters)
    {

        if (StringUtils.isEmpty(type))
        {
            throw new IllegalArgumentException("The type portion of the media type is required.");
        }

        _Type = type;

        if (StringUtils.isEmpty(subtype))
        {
            throw new IllegalArgumentException("The subtype portion of the media type is required.");
        }

        _SubType = subtype;


        if (parameters != null)
        {
            _Parameters = parameters;
        }
        else
        {
            _Parameters = new HashMap<>();
        }
    }

    @Override
    public int compareTo(final MediaType other)
    {

        final String ourQValue = getParameter(PRECEDENCE_PARAM);
        final String theirQValue = other.getParameter(PRECEDENCE_PARAM);

        if (ourQValue == null)
        {
            if (theirQValue != null)
            {
                // our value is greater in precedence
                return -1;
            }
            else
            {
                // We're equal;
                return 0;
            }
        }

        if (theirQValue == null)
        {
            return 1;
        }

        final Double ourQ = Double.valueOf(ourQValue);
        final Double theirQ = Double.valueOf(theirQValue);

        if (ourQ == theirQ)
        {
            return 0;
        }
        else if (ourQ > theirQ)
        {
            return -1;
        }
        else
        {
            return 1;
        }

    }

    public String getFullType()
    {

        return _Type + NAME_SEPARATOR + _SubType;
    }

    public String getParameter(final String name)
    {

        return _Parameters.get(name);
    }

    public String setParameter(final String name, final String value)
    {

        return _Parameters.put(name, value);
    }

    public Map<String, String> getParameters()
    {

        return _Parameters;
    }

    public String getSubType()
    {

        return _SubType;
    }

    public String getType()
    {

        return _Type;
    }

    public String toContentType()
    {

        final StringBuffer buffer = new StringBuffer();
        buffer.append(getFullType());
        if (_Parameters.containsKey(SystemMediaType.PARAMETER_NAME_SCHEMA))
        {
            buffer.append("; ").append(SystemMediaType.PARAMETER_NAME_SCHEMA).append("=\"")
                    .append(_Parameters.get(SystemMediaType.PARAMETER_NAME_SCHEMA)).append("\"");
        }

        if (_Parameters.containsKey(SystemMediaType.PARAMETER_NAME_FORMAT))
        {
            buffer.append("; ").append(SystemMediaType.PARAMETER_NAME_FORMAT).append("=\"")
                    .append(_Parameters.get(SystemMediaType.PARAMETER_NAME_FORMAT)).append("\"");
        }

        return buffer.toString();
    }

    @Override
    public String toString()
    {

        return getFullType();
    }

    @Override
    public boolean equals(final Object o)
    {

        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final MediaType mediaType = (MediaType) o;

        if (!_Parameters.equals(mediaType._Parameters))
        {
            return false;
        }
        if (!_SubType.equals(mediaType._SubType))
        {
            return false;
        }
        if (!_Type.equals(mediaType._Type))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {

        int result = _Type.hashCode();
        result = 31 * result + _SubType.hashCode();
        result = 31 * result + _Parameters.hashCode();
        return result;
    }

    public class MediaTypeException extends Exception
    {

        private static final long serialVersionUID = 1L;

        MediaTypeException(final String message)
        {

            super(message);
        }
    }
}

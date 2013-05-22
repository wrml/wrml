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
package org.wrml.runtime.rest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicHeader;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.DimensionsBuilder;
import org.wrml.runtime.syntax.SyntaxLoader;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Set of reusable utility functions related to REST.
 */
public final class RestUtils
{

    public static final String DEFAULT_ENCODING = "UTF-8";

    public static final Pattern ACCEPT = Pattern.compile(CommonHeader.ACCEPT.getName() + ": (.*)");

    public static final Pattern ACCEPT_MAJOR = Pattern.compile("([^,]+)");

    public static final Pattern ACCEPT_TYPE_PARAM_SEP = Pattern.compile("( +)?([^;]*)((;( +)?(.*))+)?");

    public static final Pattern ACCEPT_TYPE_SEP = Pattern.compile("(.*)/(.*)");

    public static final Pattern ACCEPT_PARAM = Pattern.compile(";( )*([^;]+)?");

    public static final Pattern ACCEPT_PARAM_SEP = Pattern.compile("([^=]+)=?(\"([^\"]+)(\"))?(.*)?");

    public static final Pattern CONTENT_TYPE = Pattern.compile(CommonHeader.CONTENT_TYPE.getName() + ": (.*)");

    public static final Pattern CONTENT_TYPE_TYPE_PARAM_SEP = Pattern.compile("([^;/]*)/([^;/]*)((;( +)?(.*))+)?");

    public static final Pattern CONTENT_TYPE_PARAM_SEP = Pattern.compile(";( )*([^;=]+)=(\"([^\"]+)(\"))?([^;]+)?");

    public static final URI encodeUri(final URI uri)
    {

        String uriString;
        try
        {
            uriString = URLEncoder.encode(uri.toString(), DEFAULT_ENCODING);
        }
        catch (final UnsupportedEncodingException e)
        {
            return null;
        }

        return URI.create(uriString);
    }

    public static MediaType extractMediaTypeFromContentTypeHeaderValue(String contentTypeHeaderValue)
    {

        MediaType mediaType = null;

        if (contentTypeHeaderValue == null)
        {
            return mediaType;
        }

        final Matcher contentWrap = CONTENT_TYPE.matcher(contentTypeHeaderValue);

        if (contentWrap.find())
        {
            contentTypeHeaderValue = contentWrap.group(1);
            final Matcher content = CONTENT_TYPE_TYPE_PARAM_SEP.matcher(contentTypeHeaderValue);
            if (content.find())
            {
                final String majorType = content.group(1);
                final String subType = content.group(2);

                final Map<String, String> parameters = new HashMap<>();
                final String params = content.group(4);
                if (params != null && !params.equals(""))
                {
                    final Matcher paramM = CONTENT_TYPE_PARAM_SEP.matcher(params);
                    while (paramM.find())
                    {
                        if (paramM.group(4) != null)
                        {
                            parameters.put(paramM.group(2), paramM.group(4));
                        }
                        else
                        {
                            parameters.put(paramM.group(2), paramM.group(6));
                        }
                    }
                }
                mediaType = new MediaType(majorType, subType, parameters);
            }
        }

        return mediaType;
    }

    public static final MediaType extractMediaTypeFromDimensions(final Context context, final Dimensions dimensions)
    {

        final URI defaultFormatUri = context.getFormatLoader().getDefaultFormatUri();
        final Map<String, String> mediaTypeParameters = new LinkedHashMap<>(6);

        mediaTypeParameters.put(SystemMediaType.PARAMETER_NAME_SCHEMA, dimensions.getSchemaUri().toString());
        mediaTypeParameters.put(SystemMediaType.PARAMETER_NAME_FORMAT, defaultFormatUri.toString());

        final List<String> embeddedLinkSlotNames = dimensions.getEmbeddedLinkSlotNames();
        if (embeddedLinkSlotNames != null && !embeddedLinkSlotNames.isEmpty())
        {
            mediaTypeParameters.put(SystemMediaType.PARAMETER_NAME_EMBED,
                    RestUtils.formatListString(embeddedLinkSlotNames));
        }

        final List<String> excludedSlotNames = dimensions.getExcludedSlotNames();
        if (excludedSlotNames != null && !excludedSlotNames.isEmpty())
        {
            mediaTypeParameters.put(SystemMediaType.PARAMETER_NAME_EXCLUDE,
                    RestUtils.formatListString(excludedSlotNames));
        }
        else
        {
            // INCLUDE and EXCLUDE are mutually exclusive paramaters, with precedence given to exclude (if both are
            // present).
            final List<String> includedSlotNames = dimensions.getIncludedSlotNames();
            if (includedSlotNames != null && !includedSlotNames.isEmpty())
            {
                mediaTypeParameters.put(SystemMediaType.PARAMETER_NAME_INCLUDE,
                        RestUtils.formatListString(includedSlotNames));
            }
        }

        final MediaType mediaType = new MediaType(SystemMediaType.TYPE_APPLICATION, SystemMediaType.SUBTYPE_WRML,
                mediaTypeParameters);

        return mediaType;
    }

    public static List<MediaType> extractMediaTypesFromAcceptHeaderValue(String acceptsHeaderValue)
    {

        final List<MediaType> mediaTypes = new ArrayList<>();

        if (acceptsHeaderValue == null)
        {
            return mediaTypes;
        }

        Matcher accept = ACCEPT.matcher(acceptsHeaderValue);

        // Remove front if present
        if (accept.find())
        {
            acceptsHeaderValue = accept.group(1);
        }

        accept = ACCEPT_MAJOR.matcher(acceptsHeaderValue);

        while (accept.find())
        {
            final String group = accept.group();
            final Matcher typeParam = ACCEPT_TYPE_PARAM_SEP.matcher(group);

            if (typeParam.find())
            {
                final String uberType = typeParam.group(2);
                final String uberParams = typeParam.group(3);

                final Matcher typeM = ACCEPT_TYPE_SEP.matcher(uberType);

                String majorType = "";
                String subType = "";
                if (typeM.find())
                {
                    majorType = typeM.group(1);
                    subType = typeM.group(2);
                }

                final Map<String, String> parameters = new HashMap<>();
                if (uberParams != null && !uberParams.equals(""))
                {
                    final Matcher params = ACCEPT_PARAM.matcher(uberParams);

                    while (params.find())
                    {
                        final String param = params.group(2);
                        final Matcher paramSep = ACCEPT_PARAM_SEP.matcher(param);

                        if (paramSep.find())
                        {
                            if (paramSep.group(3) != null)
                            {
                                parameters.put(paramSep.group(1), paramSep.group(3));
                            }
                            else
                            {
                                parameters.put(paramSep.group(1), paramSep.group(5));
                            }
                        }
                    }
                }

                mediaTypes.add(new MediaType(majorType, subType, parameters));
            }
        }

        Collections.sort(mediaTypes);

        return mediaTypes;
    }

    public static final Set<Header> extractRequestHeaders(final Context context, final Dimensions requestedDimensions)
    {

        final Set<Header> headers = new LinkedHashSet<>();
        final String acceptHeaderValue = RestUtils.extractMediaTypeFromDimensions(context, requestedDimensions)
                .toString();
        final Header acceptHeader = new BasicHeader(CommonHeader.ACCEPT.getName(), acceptHeaderValue);
        headers.add(acceptHeader);

        final URI referrerUri = requestedDimensions.getReferrerUri();
        if (referrerUri != null)
        {
            headers.add(new BasicHeader(CommonHeader.REFERER.getName(), referrerUri.toString()));
        }

        final Locale locale = requestedDimensions.getLocale();
        if (locale != null)
        {
            final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
            final String languageHeaderValue = syntaxLoader.formatSyntaxValue(locale);
            headers.add(new BasicHeader(CommonHeader.ACCEPT_LANGUAGE.getName(), languageHeaderValue));
        }

        final Map<String, String> metadataMap = requestedDimensions.getMetadata();
        if (metadataMap != null && !metadataMap.isEmpty())
        {
            for (final String metadataName : metadataMap.keySet())
            {
                final String metadataValue = metadataMap.get(metadataName);
                final Header additionalHeader = new BasicHeader(metadataName, metadataValue);
                headers.add(additionalHeader);
            }
        }

        return headers;
    }

    public static final Dimensions extractResponseDimensions(final Context context, final HttpResponse response,
                                                             final Dimensions requestedDimensions)
    {

        final DimensionsBuilder dimensionsBuilder = new DimensionsBuilder();
        URI schemaURI = null;
        final Header contentTypeHeader = response.getFirstHeader(CommonHeader.CONTENT_TYPE.getName());
        if (contentTypeHeader != null)
        {
            final String contentTypeHeaderValue = contentTypeHeader.getValue();
            final MediaType mediaType = RestUtils.extractMediaTypeFromContentTypeHeaderValue(contentTypeHeaderValue);
            if (mediaType != null)
            {
                final String schemaUriString = mediaType.getParameter(SystemMediaType.PARAMETER_NAME_SCHEMA);
                if (schemaUriString != null)
                {
                    schemaURI = URI.create(schemaUriString);
                }
            }
        }

        if (schemaURI == null)
        {
            schemaURI = requestedDimensions.getSchemaUri();
        }

        dimensionsBuilder.setSchemaUri(schemaURI);

        final Header contentLanguageheader = response.getFirstHeader(CommonHeader.CONTENT_LANGUAGE.getName());
        if (contentLanguageheader != null)
        {
            String languageTag = contentLanguageheader.getValue();
            if (languageTag != null)
            {
                if (languageTag.contains(","))
                {
                    languageTag = StringUtils.split(languageTag)[0];
                }

                final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
                final Locale locale = syntaxLoader.parseSyntacticText(languageTag, Locale.class);
                if (locale != null)
                {
                    dimensionsBuilder.setLocale(locale);
                }
            }
        }

        return dimensionsBuilder.toDimensions();
    }

    public static final SortedSet<Parameter> extractUriQueryParameters(final URI uri)
    {

        if (uri == null)
        {
            return null;
        }

        final String queryPart = uri.getQuery();
        if (queryPart == null || queryPart.isEmpty())
        {
            return null;
        }

        final List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(uri, DEFAULT_ENCODING);
        if (nameValuePairs == null)
        {
            return null;
        }

        final SortedSet<Parameter> queryParameters = new TreeSet<Parameter>();
        for (final NameValuePair nameValuePair : nameValuePairs)
        {
            final Parameter parameter = new Parameter(nameValuePair.getName(), nameValuePair.getValue());
            queryParameters.add(parameter);
        }

        return queryParameters;
    }

    public static final String getLastPathElement(final URI uri)
    {

        final String path = uri.getPath();
        if (StringUtils.isEmpty(path))
        {
            return path;
        }

        String lastPathElement = StringUtils.substringAfterLast(path, "/");
        lastPathElement = StringUtils.substringBefore(lastPathElement, "?");
        return lastPathElement;
    }

    private static final String formatListString(final List<String> list)
    {

        return new StringBuilder("[").append(StringUtils.join(list, ", ")).append("]").toString();
    }

}

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
import org.wrml.runtime.syntax.SyntaxHandler;
import org.wrml.runtime.syntax.SyntaxLoader;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @see <a href="http://www.ietf.org/rfc/rfc6570.txt">RFC 6570 - URI Template</a>
 */
public class UriTemplate {

    public static final char PATH_SEPARATOR_CHAR = '/';

    public static final String PATH_SEPARATOR = String.valueOf(PATH_SEPARATOR_CHAR);

    private static final Pattern PARAM_NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

    private final SyntaxLoader _SyntaxLoader;

    private final String[] _ParamNames;

    private final Pattern _MatchPattern;

    private final String _UriTemplateString;

    private URI _StaticUri;

    public UriTemplate(final SyntaxLoader syntaxLoader, final String uriTemplateString) {

        _SyntaxLoader = syntaxLoader;
        _UriTemplateString = uriTemplateString;

        final int length = uriTemplateString.length();
        final Matcher matcher = UriTemplate.PARAM_NAMES_PATTERN.matcher(uriTemplateString);
        final StringBuilder pattern = new StringBuilder();
        final List<String> paramNames = new ArrayList<String>();
        int end = 0;

        while (matcher.find()) {
            final int start = matcher.start();

            if (start != end) {
                // Copy the none-match to the pattern
                pattern.append(uriTemplateString.substring(end, start));

                end = matcher.end();
                final String matcherGroup = matcher.group(1);

                // Append the fixed group name
                pattern.append("(?<" + matcherGroup + ">\\S+)");
            }
            paramNames.add(matcher.group(1));
            end = matcher.end();
        }

        if (end != length) {
            pattern.append(uriTemplateString.substring(end, length));
        }

        String patternString = pattern.toString();
        final int lastIndex = patternString.length() - 1;
        if ((lastIndex >= 0) && (patternString.charAt(lastIndex) == PATH_SEPARATOR_CHAR)) {
            patternString = patternString.substring(0, lastIndex);
        }

        _MatchPattern = Pattern.compile(patternString);
        _ParamNames = new String[paramNames.size()];

        paramNames.toArray(_ParamNames);

    }

    public URI evaluate(final Map<String, Object> parameterMap) {

        return evaluate(parameterMap, false);
    }

    public URI evaluate(final Map<String, Object> parameterMap, final boolean allowNulls) {

        final URI staticUri = getStaticUri();
        if (staticUri != null) {
            if (parameterMap == null || parameterMap.size() == 0) {
                return staticUri;
            }
            else {
                throw new UriTemplateException("The URI Template accepts no parameters", null, this);
            }

        }

        final SyntaxLoader syntaxLoader = getSyntaxLoader();

        String matchResult = _MatchPattern.toString();

        for (final String templateParamName : _ParamNames) {
            final Object templateParamValue = parameterMap.get(templateParamName);
            final String templateParamStringValue;
            if (templateParamValue == null) {
                if (allowNulls) {
                    templateParamStringValue = "null";
                }
                else {
                    return null;
                }
            }
            else {
                templateParamStringValue = syntaxLoader.formatSyntaxValue(templateParamValue);
            }

            matchResult = matchResult.replace("(?<" + templateParamName + ">\\S+)", templateParamStringValue);
        }

        final SyntaxHandler<URI> uriSyntaxHandler = syntaxLoader.getSyntaxHandler(URI.class);
        final URI uri = uriSyntaxHandler.parseSyntacticText(matchResult);

        // TODO:  This would be cleaner and more consistent, but it currently breaks UriTemplateTest (unit test) with mocking strategy that makes it impossible...?
        //final URI uri = syntaxLoader.parseSyntacticText(matchResult, URI.class);

        return uri;
    }

    public String[] getParameterNames() {

        return _ParamNames;
    }

    public SortedSet<Parameter> getParameters(final URI uri) {

        if (uri == null) {
            throw new UriTemplateException("Null URI", null, this);
        }


        String uriString = uri.toString();
        uriString = StringUtils.substringBefore(uriString, "?");

        final Matcher matcher = _MatchPattern.matcher(uriString);

        if (!matcher.matches()) {
            return null;
        }

        final SortedSet<Parameter> parameterSet = new TreeSet<>();
        for (final String paramName : _ParamNames) {
            final String paramValue = matcher.group(paramName);
            final Parameter parameter = new Parameter(paramName, paramValue);
            parameterSet.add(parameter);
        }

        return parameterSet;
    }

    public SyntaxLoader getSyntaxLoader() {

        return _SyntaxLoader;
    }

    public String getUriTemplateString() {

        return _UriTemplateString;
    }

    public boolean matches(final URI uri) {

        if (uri == null) {
            return false;
        }

        final URI staticUri = getStaticUri();
        if (staticUri != null && uri.equals(staticUri)) {
            return true;
        }

        final String uriString = uri.normalize().toString();
        final Matcher matcher = getMatchPattern().matcher(uriString);

        return matcher.matches();
    }

    @Override
    public String toString() {

        String retval = "";

        retval += _UriTemplateString;

        return retval;
    }

    private Pattern getMatchPattern() {

        return _MatchPattern;
    }

    private URI getStaticUri() {

        if (_StaticUri == null && _ParamNames.length == 0) {
            _StaticUri = URI.create(_UriTemplateString);
        }
        return _StaticUri;
    }
}

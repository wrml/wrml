/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wrml.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wrml.event.EventManager;
import org.wrml.event.EventSource;

public class UriTemplate implements EventSource<UriTemplateEventListener> {

    private static final Pattern PARAM_NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

    private final String[] _ParamNames;
    private final Pattern _MatchPattern;
    private final String _UriTemplateString;

    /**
     * Manages the implementation of {@link EventSource}
     */
    private final EventManager<UriTemplateEventListener> _EventManager;

    public UriTemplate(final String uriTemplateString) {
        _UriTemplateString = uriTemplateString;
        _EventManager = new EventManager<UriTemplateEventListener>(UriTemplateEventListener.class);

        final int length = uriTemplateString.length();
        final Matcher matcher = PARAM_NAMES_PATTERN.matcher(uriTemplateString);
        final StringBuilder pattern = new StringBuilder();
        final List<String> paramNames = new ArrayList<String>();
        int end = 0;

        while (matcher.find()) {
            final int start = matcher.start();
            if (start != end) {
                pattern.append(Pattern.quote(uriTemplateString.substring(start, end)));
            }
            pattern.append("(.*)");
            paramNames.add(matcher.group(1));
            end = matcher.end();
        }

        if (end != length) {
            pattern.append(Pattern.quote(uriTemplateString.substring(end, length)));
        }

        String patternString = pattern.toString();
        final int lastIndex = patternString.length() - 1;
        if ((lastIndex >= 0) && (patternString.charAt(lastIndex) == '/')) {
            patternString = patternString.substring(0, lastIndex);
        }

        _MatchPattern = Pattern.compile(patternString);
        _ParamNames = new String[paramNames.size()];
        paramNames.toArray(_ParamNames);

    }

    @Override
    public boolean addEventListener(UriTemplateEventListener eventListener) {
        return _EventManager.addEventListener(eventListener);
    }

    public String getUriTemplateString() {
        return _UriTemplateString;
    }

    public boolean matches(URI uri) {
        if (uri == null) {
            return false;
        }

        final String uriString = uri.normalize().toString();
        final Matcher matcher = getMatchPattern().matcher(uriString);

        return matcher.matches();
    }

    @Override
    public boolean removeEventListener(UriTemplateEventListener eventListener) {
        return _EventManager.removeEventListener(eventListener);
    }

    private Pattern getMatchPattern() {
        return _MatchPattern;
    }

}

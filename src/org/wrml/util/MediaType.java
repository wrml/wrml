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
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wrml.util.observable.ObservableMap;
import org.wrml.util.observable.Observables;

/**
 * A media type. Instances are immutable.
 * 
 * message://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7
 * 
 * media-type = type "/" subtype *( ";" parameter ) type = token subtype = token
 */
public class MediaType implements Comparable<MediaType> {

    public static final String TYPE_APPLICATION = "application";
    public static final String SUBTYPE_WRML = "wrml";
    public static final String MEDIA_TYPE_STRING_WRML = TYPE_APPLICATION + '/' + SUBTYPE_WRML;
    public static final String PARAMETER_NAME_SCHEMA = "schema";
    public static final String PARAMETER_NAME_FORMAT = "format";
    public static final String PARAMETER_NAME_EMBED = "embed";

    private static final MessageFormat WRML_MEDIA_TYPE_MESSAGE_FORMAT;

    public static final String MEDIA_TYPE_REGEX_STRING = "^" + "([a-zA-Z0-9!#$%^&\\*_\\-\\+{}\\|'.`~]+)/"
            + "([a-zA-Z0-9!#$%^&\\*_\\-\\+{}\\|'.`~]+)(;\\s+"
            + "([a-zA-Z0-9!#$%^&\\*_\\-\\+{}\\|'.`~]+)=\"([^\"]*)\")*";

    public static final Pattern MEDIA_TYPE_REGEX_PATTERN = Pattern.compile(MEDIA_TYPE_REGEX_STRING);

    static {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(MediaType.PARAMETER_NAME_SCHEMA, "{0}");
        final String formatString = createMediaTypeString(MediaType.TYPE_APPLICATION, MediaType.SUBTYPE_WRML,
                parameters);
        WRML_MEDIA_TYPE_MESSAGE_FORMAT = new MessageFormat(formatString);
    }

    public static Comparator<MediaType> ALPHA_ORDER = new Comparator<MediaType>() {

        @Override
        public int compare(final MediaType mediaType1, final MediaType mediaType2) {
            if (mediaType1 == mediaType2) {
                return 0;
            }

            final String mediaTypeString1 = mediaType1.toString();
            final String mediaTypeString2 = mediaType2.toString();

            return Compare.twoInsensitiveStrings(mediaTypeString1, mediaTypeString2);
        }

    };

    public static MediaType create(String mediaTypeString) {
        final Matcher matcher = MEDIA_TYPE_REGEX_PATTERN.matcher(mediaTypeString);

        // TODO: NOTE: WARNING: I don't think the current regex works when there is more than one media type parameter
        if (!matcher.matches()) {
            //System.out.println("MediaTypeToStringTransformer.bToA(" + aValue + ") returning: null");
            return null;
        }

        final String type = matcher.group(1);
        final String subtype = matcher.group(2);

        //        System.out.println("matcher.group(0) - " + matcher.group(0));
        //        System.out.println("matcher.group(1) - " + matcher.group(1));
        //        System.out.println("matcher.group(2) - " + matcher.group(2));
        //        System.out.println("matcher.group(3) - " + matcher.group(3));
        //        System.out.println("matcher.group(4) - " + matcher.group(4));
        //        System.out.println("matcher.group(5) - " + matcher.group(5));

        final String schemaIdString = matcher.group(5);

        SortedMap<String, String> parameters = null;
        if (schemaIdString != null) {
            parameters = new TreeMap<String, String>();
            parameters.put(MediaType.PARAMETER_NAME_SCHEMA, schemaIdString);
        }

        final MediaType mediaType = new MediaType(type, subtype, (parameters != null) ? parameters : null);

        //System.out.println("MediaTypeToStringTransformer.bToA(" + aValue + ") returning: " + mediaType);

        return mediaType;
    }

    public static String createMediaTypeString(final String type, final String subtype,
            final Map<String, String> parameters) {

        final StringBuilder sb = new StringBuilder();
        sb.append(type.trim()).append('/').append(subtype.trim());
        appendParameters(sb, parameters);
        return sb.toString();
    }

    public static String createWrmlMediaTypeString(final String schemaIdString, final Map<String, String> parameters) {

        String mediaTypeString = WRML_MEDIA_TYPE_MESSAGE_FORMAT.format(new Object[] { schemaIdString });
        if (parameters != null) {
            final StringBuilder sb = new StringBuilder(mediaTypeString);
            appendParameters(sb, parameters);
            mediaTypeString = sb.toString();
        }
        return mediaTypeString;
    }

    private static void appendParameters(final StringBuilder sb, final Map<String, String> parameters) {
        if ((parameters == null) || (parameters.size() == 0)) {
            return;
        }

        for (final String parameterName : parameters.keySet()) {
            sb.append("; ").append(parameterName.trim()).append('=').append('\"')
                    .append(parameters.get(parameterName).trim()).append('\"');
        }
    }

    private final String _Type;
    private final String _Subtype;
    private final ObservableMap<String, String> _Parameters;
    private String _String;
    private final boolean _IsWrml;

    MediaType(String type, String subtype) {
        this(type, subtype, null);
    }

    MediaType(String type, String subtype, Map<String, String> parameters) {
        _Type = type.toLowerCase();
        _Subtype = subtype.toLowerCase();

        final SortedMap<String, String> inner = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

        if (parameters != null) {
            inner.putAll(parameters);
        }

        _Parameters = Observables.observableMap(inner);

        _IsWrml = (TYPE_APPLICATION.equals(_Type) && SUBTYPE_WRML.equals(_Subtype));
    }

    @Override
    public final int compareTo(final MediaType other) {
        return ALPHA_ORDER.compare(this, other);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final MediaType other = (MediaType) obj;

        if (_Type == null) {
            if (other._Type != null) {
                return false;
            }
        }
        else if (!_Type.equals(other._Type)) {
            return false;
        }

        if (_Subtype == null) {
            if (other._Subtype != null) {
                return false;
            }
        }
        else if (!_Subtype.equals(other._Subtype)) {
            return false;
        }

        if (_Parameters == null) {
            if (other._Parameters != null) {
                return false;
            }
        }
        else if (!_Parameters.equals(other._Parameters)) {
            return false;
        }

        return true;
    }

    public final List<URI> getEmbeddedLinkRelationIds() {
        // TODO: Get the embed parameter value from the media type
        return null;
    }

    public String getFormatIdString() {
        if (!isWrml()) {
            return null;
        }

        final Map<String, String> parameters = getParameters();
        if (parameters == null) {
            return null;
        }

        return parameters.get(PARAMETER_NAME_FORMAT);
    }

    public ObservableMap<String, String> getParameters() {
        return _Parameters;
    }

    public String getSchemaIdString() {
        if (!isWrml()) {
            return null;
        }

        final Map<String, String> parameters = getParameters();
        if (parameters == null) {
            return null;
        }

        return parameters.get(PARAMETER_NAME_SCHEMA);
    }

    public String getSubtype() {
        return _Subtype;
    }

    public String getType() {
        return _Type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((_Type == null) ? 0 : _Type.hashCode());
        result = (prime * result) + ((_Subtype == null) ? 0 : _Subtype.hashCode());
        result = (prime * result) + ((_Parameters == null) ? 0 : _Parameters.hashCode());
        return result;
    }

    public boolean isWrml() {
        return _IsWrml;
    }

    @Override
    public String toString() {
        if (_String == null) {
            _String = createMediaTypeString(getType(), getSubtype(), getParameters());
        }
        return _String;
    }

}

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
package org.wrml.model.rest;

import java.util.HashMap;
import java.util.Map;

// Generated from a Web Resource Schema
// TODO: link to Web Resource Schema, document
public enum CommonHeader {
    ACCEPT("Accept", HeaderCategory.REQUEST),
    ACCEPT_CHARSET("Accept-Charset", HeaderCategory.REQUEST),
    ACCEPT_ENCODING("Accept-Encoding", HeaderCategory.REQUEST),
    ACCEPT_LANGUAGE("Accept-Language", HeaderCategory.REQUEST),
    ACCEPT_RANGES("Accept-Ranges", HeaderCategory.RESPONSE),
    AGE("Age", HeaderCategory.RESPONSE),
    ALLOW("Allow", HeaderCategory.ENTITY),
    AUTHORIZATION("Authorization", HeaderCategory.REQUEST),
    CACHE_CONTROL("Cache-Control", HeaderCategory.GENERAL),
    CONNECTION("Connection", HeaderCategory.GENERAL),
    CONTENT_ENCODING("Content-Encoding", HeaderCategory.ENTITY),
    CONTENT_LANGUAGE("Content-Language", HeaderCategory.ENTITY),
    CONTENT_LENGTH("Content-Length", HeaderCategory.ENTITY),
    CONTENT_LOCATION("Content-Location", HeaderCategory.ENTITY),
    CONTENT_MD5("Content-MD5", HeaderCategory.ENTITY),
    CONTENT_RANGE("Content-Range", HeaderCategory.ENTITY),
    CONTENT_TYPE("Content-Type", HeaderCategory.ENTITY),
    DATE("Date", HeaderCategory.GENERAL),
    ETAG("ETag", HeaderCategory.RESPONSE),
    EXPECT("Expect", HeaderCategory.REQUEST),
    EXPIRES("Expires", HeaderCategory.ENTITY),
    FROM("From", HeaderCategory.REQUEST),
    HOST("Host", HeaderCategory.REQUEST),
    IF_MATCH("If-Match", HeaderCategory.REQUEST),
    IF_MODIFIED_SINCE("If-Modified-Since", HeaderCategory.REQUEST),
    IF_NONE_MATCH("If-None-Match", HeaderCategory.REQUEST),
    IF_RANGE("If-Range", HeaderCategory.REQUEST),
    IF_UNMODIFIED_SINCE("If-Unmodified-Since", HeaderCategory.REQUEST),
    LAST_MODIFIED("Last-Modified", HeaderCategory.ENTITY),
    LOCATION("Location", HeaderCategory.RESPONSE),
    MAX_FORWARDS("Max-Forwards", HeaderCategory.REQUEST),
    PRAGMA("Pragma", HeaderCategory.GENERAL),
    PROXY_AUTHENTICATE("Proxy-Authenticate", HeaderCategory.RESPONSE),
    PROXY_AUTHORIZATION("Proxy-Authorization", HeaderCategory.REQUEST),
    RANGE("Range", HeaderCategory.REQUEST),
    REFERER("Referer", HeaderCategory.REQUEST),
    RETRY_AFTER("Retry-After", HeaderCategory.RESPONSE),
    SERVER("Server", HeaderCategory.RESPONSE),
    TE("TE", HeaderCategory.REQUEST),
    TRAILER("Trailer", HeaderCategory.GENERAL),
    TRANSFER_ENCODING("Transfer-Encoding", HeaderCategory.GENERAL),
    UPGRADE("Upgrade", HeaderCategory.GENERAL),
    USER_AGENT("User-Agent", HeaderCategory.REQUEST),
    VARY("Vary", HeaderCategory.RESPONSE),
    VIA("Via", HeaderCategory.GENERAL),
    WARNING("Warning", HeaderCategory.GENERAL),
    WWW_AUTHENTICATE("WWW-Authenticate", HeaderCategory.RESPONSE);

    // TODO: Want a generic utility to help statically cache a map of Enum .toString() values to the actual Enum values.

    private static final Map<String, CommonHeader> STRING_MAP;

    static {
        final CommonHeader[] values = CommonHeader.values();
        STRING_MAP = new HashMap<String, CommonHeader>(values.length);
        for (final CommonHeader e : values) {
            CommonHeader.STRING_MAP.put(e.toString(), e);
        }
    }

    public static final CommonHeader fromString(final String string) {

        if (!CommonHeader.STRING_MAP.containsKey(string)) {
            return null;
        }

        return CommonHeader.STRING_MAP.get(string);
    }

    private final String _Name;

    private final HeaderCategory _Category;

    private CommonHeader(final String name, final HeaderCategory category) {

        _Name = name;
        _Category = category;
    }

    public HeaderCategory getCategory() {

        return _Category;
    }

    public String getName() {

        return _Name;
    }

    @Override
    public String toString() {

        return getName();
    }

}

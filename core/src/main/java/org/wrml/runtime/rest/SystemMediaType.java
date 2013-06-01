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

/**
 * <p>
 * The WRML media type interface specification.
 * </p>
 * 
 * <p>
 * <b>Syntax:</b>
 * </p>
 * 
 * <p>
 * 
 * <pre>
 * media-type = type "/" subtype *( ";" parameter ) type = token subtype = token
 * </pre>
 * 
 * </p>
 * 
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7">HTTP/1.1 Media Types</a>
 */
public interface SystemMediaType
{

    /**
     * application (as in application/wrml)
     */
    public static final String TYPE_APPLICATION = "application";

    /**
     * wrml (as in application/wrml)
     */
    public static final String SUBTYPE_WRML = "wrml";

    /**
     * application/wrml
     */
    public static final String MEDIA_TYPE_STRING_WRML = SystemMediaType.TYPE_APPLICATION + '/'
            + SystemMediaType.SUBTYPE_WRML;

    /**
     * Media type parameter to convey the content's semantic "data structure".
     */
    public static final String PARAMETER_NAME_SCHEMA = "schema";

    /**
     * Media type parameter to identify the content serialization syntax.
     */
    public static final String PARAMETER_NAME_FORMAT = "format";

    /**
     * Media type parameter to request "server-side" link traversal.
     */
    public static final String PARAMETER_NAME_EMBED = "embed";

    /**
     * Media type parameter to filter (omit) named slots from a model.
     */
    public static final String PARAMETER_NAME_EXCLUDE = "exclude";

    /**
     * Media type parameter to specify (by name) the slots containing the desired model state data.
     */
    public static final String PARAMETER_NAME_INCLUDE = "include";
}

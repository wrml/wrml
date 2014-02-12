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
package org.wrml.runtime.syntax;

import org.apache.commons.lang3.StringUtils;
import org.wrml.runtime.rest.MediaType;

/**
 * TODO: Need to tie this into the code that was added for Media Type parsing.
 */
public class MediaTypeSyntaxHandler extends SyntaxHandler<MediaType> {

    @Override
    public String formatSyntaxValue(final MediaType mediaType) {

        if (mediaType == null) {
            return null;
        }

        final String aValue = mediaType.toString();

        return aValue;

    }

    @Override
    public MediaType parseSyntacticText(final String mediaTypeString) {

        if (mediaTypeString == null) {
            return null;
        }

        // TODO: Parse with a regex or something that deals properly extracts the media type parameters
        final String[] parts = StringUtils.split(mediaTypeString, '/');
        final String type = parts[0].trim();
        final String[] params = StringUtils.split(parts[1], ';');
        final String subType = params[0].trim();
        final MediaType mediaType = new MediaType(type, subType);

        return mediaType;
    }


}

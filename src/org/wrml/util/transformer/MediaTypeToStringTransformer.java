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

package org.wrml.util.transformer;

import org.wrml.util.MediaType;

public class MediaTypeToStringTransformer implements ToStringTransformer<MediaType> {

    @Override
    public String aToB(MediaType mediaType) {

        if (mediaType == null) {
            System.out.println("MediaTypeToStringTransformer.aToB(" + mediaType + ") returning: null");
            return null;
        }

        final String aValue = mediaType.toString();

        System.out.println("MediaTypeToStringTransformer.aToB(" + mediaType + ") returning: " + aValue);
        return aValue;

    }

    @Override
    public MediaType bToA(String mediaTypeString) {

        if (mediaTypeString == null) {
            System.out.println("MediaTypeToStringTransformer.bToA(" + mediaTypeString + ") returning: null");
            return null;
        }

        final MediaType mediaType = MediaType.create(mediaTypeString);

        System.out.println("MediaTypeToStringTransformer.bToA(" + mediaTypeString + ") returning: " + mediaType);
        return mediaType;
    }

}

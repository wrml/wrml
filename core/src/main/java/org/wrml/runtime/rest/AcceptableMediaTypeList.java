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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.rest.CommonHeader;
import org.wrml.runtime.rest.MediaType.MediaTypeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AcceptableMediaTypeList extends ArrayList<MediaType> {

    public static final Pattern ACCEPT = Pattern.compile(CommonHeader.ACCEPT.getName() + ":( *)(.*)");

    public static final Pattern ACCEPT_MAJOR = Pattern.compile("( *)([^,]+)");

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(AcceptableMediaTypeList.class);

    private static final QSorter SORTER = new QSorter();

    public AcceptableMediaTypeList(String acceptHeaderValueString) {

        if (acceptHeaderValueString == null) {
            return;
        }

        Matcher accept = ACCEPT.matcher(acceptHeaderValueString);

        // Remove front if present
        if (accept.find()) {
            acceptHeaderValueString = accept.group(2);
        }

        accept = ACCEPT_MAJOR.matcher(acceptHeaderValueString);

        while (accept.find()) {
            final String group = accept.group(2);
            try {
                final MediaType type = new MediaType(group);
                add(type);
            }
            catch (final MediaTypeException ex) {
                LOG.debug("Unable to extract MediaType from string " + group, ex);
            }
        }

        Collections.sort(this, SORTER);
    }

    /**
     * Sorts based on the q parameter. A MediaType lacking this parameter has a value of 1 (highest).
     */
    public static class QSorter implements Comparator<MediaType> {

        public static final String Q_NAME = "q";

        @Override
        public int compare(final MediaType obj1, final MediaType obj2) {

            Double q1 = 1.0;
            if (obj1.getParameter(Q_NAME) != null) {
                q1 = Double.valueOf(obj1.getParameter(Q_NAME));
            }

            Double q2 = 1.0;
            if (obj2.getParameter(Q_NAME) != null) {
                q2 = Double.valueOf(obj2.getParameter(Q_NAME));
            }

            return -q1.compareTo(q2);
        }
    }
}

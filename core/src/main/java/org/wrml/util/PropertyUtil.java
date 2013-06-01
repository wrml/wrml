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
package org.wrml.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Property Utilities for CommandLine, System, and default properties.
 */
public class PropertyUtil {

    /**
     * @param systemPropertyName
     *            - the name specified using {@code -Dfoo=bar} style.
     * @return the property value or <code>null</code>.
     */
    public static final String getSystemProperty(final String systemPropertyName) {

        String result = null;

        if (StringUtils.isNotEmpty(systemPropertyName)) {
            result = System.getProperty(systemPropertyName);
        }

        return result;
    }

    /**
     * @param systemPropertyName
     *            - the name specified using {@code -Dfoo=bar} style.
     * @param defaultIfEmpty
     *            - the value to default to if empty/null.
     * @return the property value, defaulting to {@code defaultIfEmpty}.
     */
    public static final String getSystemProperty(final String systemPropertyName, final String defaultIfEmpty) {
        String result = getSystemProperty(systemPropertyName);
        if (StringUtils.isEmpty(result)) {
            result = defaultIfEmpty;
        }
        return result;
    }

}
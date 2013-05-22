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
package org.wrml.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.wrml.runtime.rest.ApiNavigator;

public abstract class PrefixTreeBase<T> implements PrefixTree<T>
{
    public static final String PATH_SEPARATOR = ApiNavigator.PATH_SEPARATOR;
    
    public String getPathSeparator()
    {
        return PATH_SEPARATOR;
    }
    
    List<String> segmentPath(final String path)
    {
        String tPath = path.trim();
        if (path.endsWith(getPathSeparator()))
        {
            tPath = tPath.substring(0, path.length() - 1);
        }
        if (path.startsWith(getPathSeparator()))
        {
            tPath = tPath.substring(1);
        }

        final List<String> segments = new LinkedList<String>(
                Arrays.asList(tPath.split(getPathSeparator())));
        return segments;
    }
}

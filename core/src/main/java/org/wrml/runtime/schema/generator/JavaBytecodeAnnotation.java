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
package org.wrml.runtime.schema.generator;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class JavaBytecodeAnnotation {

    private final String _InternalName;

    private final String _Descriptor;

    private final Map<String, Object> _Attributes;


    public JavaBytecodeAnnotation(final String internalName) {

        _InternalName = internalName;
        _Descriptor = 'L' + _InternalName + ';';
        _Attributes = new TreeMap<String, Object>();

    }

    public Set<String> getAttributeNames() {

        return _Attributes.keySet();
    }

    public Object getAttributeValue(final String name) {

        return _Attributes.get(name);
    }

    public String getInternalName() {

        return _InternalName;
    }

    public void setAttributeValue(final String name, final Object value) {

        _Attributes.put(name, value);
    }

    public String getDescriptor() {

        return _Descriptor;
    }

}

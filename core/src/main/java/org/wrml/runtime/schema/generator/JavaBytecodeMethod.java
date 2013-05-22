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
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
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

import java.util.ArrayList;
import java.util.List;

public class JavaBytecodeMethod
{

    private String _Name;

    private String _Descriptor;

    private String _Signature;

    private final List<String> _Exceptions;

    private final List<JavaBytecodeAnnotation> _Annotations;

    JavaBytecodeMethod()
    {
        _Annotations = new ArrayList<JavaBytecodeAnnotation>();
        _Exceptions = new ArrayList<String>();
    }

    public List<JavaBytecodeAnnotation> getAnnotations()
    {
        return _Annotations;
    }

    public String getDescriptor()
    {
        return _Descriptor;
    }

    public List<String> getExceptions()
    {
        return _Exceptions;
    }

    public String getName()
    {
        return _Name;
    }

    public String getSignature()
    {
        return _Signature;
    }

    public void setDescriptor(final String descriptor)
    {
        _Descriptor = descriptor;
    }

    public void setName(final String name)
    {
        _Name = name;
    }

    public void setSignature(final String signature)
    {
        _Signature = signature;
    }
}

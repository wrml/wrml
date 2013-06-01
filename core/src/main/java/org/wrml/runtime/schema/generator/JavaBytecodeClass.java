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

import java.util.ArrayList;
import java.util.List;

public class JavaBytecodeClass
{

    private String _InternalName;

    private String _Signature;

    private String _SuperName;

    private final List<String> _Interfaces;

    private final List<JavaBytecodeAnnotation> _Annotations;

    private final List<JavaBytecodeMethod> _Methods;

    private byte[] _Bytecode;

    JavaBytecodeClass()
    {
        this(null);
    }

    JavaBytecodeClass(final String internalName)
    {
        _InternalName = internalName;
        _Annotations = new ArrayList<JavaBytecodeAnnotation>();
        _Interfaces = new ArrayList<String>();
        _Methods = new ArrayList<JavaBytecodeMethod>();
    }

    public List<JavaBytecodeAnnotation> getAnnotations()
    {
        return _Annotations;
    }

    public byte[] getBytecode()
    {
        return _Bytecode;
    }

    public List<String> getInterfaces()
    {
        return _Interfaces;
    }

    public String getInternalName()
    {
        return _InternalName;
    }

    public List<JavaBytecodeMethod> getMethods()
    {
        return _Methods;
    }

    public String getSignature()
    {
        return _Signature;
    }

    public String getSuperName()
    {
        return _SuperName;
    }

    public void setBytecode(final byte[] bytecode)
    {
        _Bytecode = bytecode;
    }

    public void setInternalName(final String internalName)
    {
        _InternalName = internalName;
    }

    public void setSignature(final String signature)
    {
        _Signature = signature;
    }

    public void setSuperName(final String superName)
    {
        _SuperName = superName;
    }

}

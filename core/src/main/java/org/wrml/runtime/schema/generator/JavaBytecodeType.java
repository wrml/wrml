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

import org.wrml.model.Model;
import org.wrml.model.rest.Link;

import java.util.Date;
import java.util.List;
import java.util.SortedMap;

public class JavaBytecodeType
{

    public static final JavaBytecodeType BooleanBytecodeType = new JavaBytecodeType(Boolean.class);

    public static final JavaBytecodeType BooleanPrimitiveBytecodeType = new JavaBytecodeType(Boolean.TYPE);

    public static final JavaBytecodeType IntegerBytecodeType = new JavaBytecodeType(Integer.class);

    public static final JavaBytecodeType IntegerPrimitiveBytecodeType = new JavaBytecodeType(Integer.TYPE);

    public static final JavaBytecodeType DoubleBytecodeType = new JavaBytecodeType(Double.class);

    public static final JavaBytecodeType DoublePrimitiveBytecodeType = new JavaBytecodeType(Double.TYPE);

    public static final JavaBytecodeType LongBytecodeType = new JavaBytecodeType(Long.class);

    public static final JavaBytecodeType LongPrimitiveBytecodeType = new JavaBytecodeType(Long.TYPE);

    public static final JavaBytecodeType VoidPrimitiveBytecodeType = new JavaBytecodeType(Void.TYPE);

    public static final JavaBytecodeType ObjectBytecodeType = new JavaBytecodeType(Object.class);

    public static final JavaBytecodeType StringBytecodeType = new JavaBytecodeType(String.class);

    public static final JavaBytecodeType DateBytecodeType = new JavaBytecodeType(Date.class);

    public static final JavaBytecodeType ListBytecodeType = new JavaBytecodeType(List.class);

    public static final JavaBytecodeType EnumBytecodeType = new JavaBytecodeType(Enum.class);

    public static final JavaBytecodeType ModelBytecodeType = new JavaBytecodeType(Model.class);

    public static final JavaBytecodeType LinkBytecodeType = new JavaBytecodeType(Link.class);

    private String _String;

    private String _Token;

    private String _GenericSignature;

    private SortedMap<String, JavaBytecodeType> _Parameters;

    public JavaBytecodeType(final Class<?> clazz)
    {

        if (clazz.isPrimitive())
        {

            if (clazz.equals(Boolean.TYPE))
            {
                setToken("Z");
            }
            else if (clazz.equals(Double.TYPE))
            {
                setToken("D");
            }
            else if (clazz.equals(Integer.TYPE))
            {
                setToken("I");
            }
            else if (clazz.equals(Long.TYPE))
            {
                setToken("J");
            }
            else if (clazz.equals(Void.TYPE))
            {
                setToken("V");
            }
            else
            {
                throw new IllegalArgumentException("The type: " + clazz + " is not a supported primitive type.");
            }

        }
        else
        {
            setString(SchemaGenerator.externalTypeNameToInternalTypeName(clazz.getCanonicalName()));
        }
    }

    public JavaBytecodeType(final String string)
    {

        setString(string);
    }

    public String getDescriptor()
    {

        final String string = getString();
        if (string != null && !string.isEmpty())
        {
            return 'L' + string + ';';
        }
        else
        {
            return getToken();
        }
    }

    public String getGenericSignature()
    {

        if (_GenericSignature == null)
        {

            if (_Parameters == null || _Parameters.size() == 0)
            {
                // No parameters
                _GenericSignature = null;
            }
            else
            {
                // A parameterized type has a signature value

                final StringBuilder sb = new StringBuilder(_Token);
                sb.append(_String).append('<');
                for (final String parameterName : _Parameters.keySet())
                {
                    final JavaBytecodeType parameterType = _Parameters.get(parameterName);
                    if (parameterType != null)
                    {
                        if (parameterType.getParameters() == null)
                        {
                            sb.append(parameterType.getToken());
                            final String parameterTypeString = parameterType.getString();
                            if (parameterTypeString != null)
                            {
                                sb.append(parameterTypeString);
                                sb.append(';');
                            }
                        }
                        else
                        {
                            sb.append(parameterType.getGenericSignature());
                        }
                    }
                    else
                    {
                        sb.append('T').append(parameterName);
                    }
                }
                sb.append('>').append(';');

                _GenericSignature = sb.toString();
            }

        }

        return _GenericSignature;
    }

    public SortedMap<String, JavaBytecodeType> getParameters()
    {

        return _Parameters;
    }

    public void setParameters(final SortedMap<String, JavaBytecodeType> parameters)
    {

        _Parameters = parameters;
        _GenericSignature = null;
    }

    public String getString()
    {

        return _String;
    }

    public void setString(final String string)
    {

        _String = string;
        if (_String != null)
        {
            _Token = "L";
        }

        _GenericSignature = null;
    }

    public String getToken()
    {

        return _Token;
    }

    public void setToken(final String token)
    {

        _Token = token;
        _GenericSignature = null;
    }

    @Override
    public String toString()
    {

        final String token = getToken();
        final String signature = getGenericSignature();
        if (signature != null)
        {
            return signature;
        }
        if ("L".equals(token))
        {
            return getString();
        }
        else
        {
            return token;
        }
    }
}

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
package org.wrml.model.schema;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.wrml.model.Abstract;
import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Link;
import org.wrml.runtime.CompositeKey;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The WRML value types.
 */
public enum ValueType
{

    /**
     * In Java, maps to: java.lang.String or T, where T is determined by a
     * configuration-based mapping of Text syntax constraint to native
     * (text-based) type.
     */
    Text,

    /**
     * In Java, maps to: java.lang.Object
     */
    Native,

    /**
     * In Java, maps to Model or a subclass T determined by schema constraint.
     */
    Model,

    /**
     * In Java, maps to boolean or Boolean
     */
    Boolean,

    /**
     * In Java, maps to methods.
     */
    Link,

    /**
     * In Java, maps to: List<T>, where "primitive" type is determined
     * by type parameter constraint's type.
     * <p/>
     * Note: If the Type parameter constraint's Type is "Model", then T is
     * either org.wrml.Model or a subclass T determined by an added schema
     * constraint.
     */
    List,

    /**
     * In Java, maps to: ObservableMap<K,V>, where the K and V "param" types are
     * determined by type constraints.
     */
    // Map,

    /**
     * In Java, maps to: Enum&lt;T&gt;, where T is determined by choice menu
     * constraint's choice menu.
     */
    SingleSelect,

    /**
     * In Java, maps to: either int or java.lang.Integer, dependent on Field's
     * isRequired flag's value.
     */
    Integer,

    /**
     * In Java, maps to java.util.Date
     */
    Date,

    /**
     * In Java, maps to either long or java.lang.Long, dependent on Field's
     * isRequired flag's value.
     */
    Long,

    /**
     * In Java, maps to: either double or java.lang.Double, dependent on Field's
     * isRequired flag's value.
     */
    Double;

    public static final Class<?> JAVA_TYPE_ABSTRACT = Abstract.class;

    public static final Class<?> JAVA_TYPE_COMPOSITE_KEY = CompositeKey.class;

    public static final Class<?> JAVA_TYPE_LINK = Link.class;

    public static final Class<?> JAVA_TYPE_LIST_INTERFACE = List.class;

    public static final Class<?> JAVA_TYPE_MODEL = Model.class;

    public static final Class<?> JAVA_TYPE_DOCUMENT = Document.class;

    private final static Map<Class<?>, ValueType> TYPE_MAP = new HashMap<Class<?>, ValueType>();

    static
    {
        ValueType.TYPE_MAP.put(java.lang.Boolean.class, ValueType.Boolean);
        ValueType.TYPE_MAP.put(java.lang.Boolean.TYPE, ValueType.Boolean);
        ValueType.TYPE_MAP.put(java.util.Date.class, ValueType.Date);
        ValueType.TYPE_MAP.put(java.lang.Double.class, ValueType.Double);
        ValueType.TYPE_MAP.put(java.lang.Double.TYPE, ValueType.Double);
        ValueType.TYPE_MAP.put(java.lang.Integer.class, ValueType.Integer);
        ValueType.TYPE_MAP.put(java.lang.Integer.TYPE, ValueType.Integer);
        ValueType.TYPE_MAP.put(ValueType.JAVA_TYPE_LINK, ValueType.Link);
        ValueType.TYPE_MAP.put(ValueType.JAVA_TYPE_LIST_INTERFACE, ValueType.List);
        ValueType.TYPE_MAP.put(java.lang.Long.class, ValueType.Long);
        ValueType.TYPE_MAP.put(java.lang.Long.TYPE, ValueType.Long);
        ValueType.TYPE_MAP.put(ValueType.JAVA_TYPE_MODEL, ValueType.Model);
        ValueType.TYPE_MAP.put(java.lang.Object.class, ValueType.Native);
        ValueType.TYPE_MAP.put(java.lang.String.class, ValueType.Text);
    }

    private Object _DefaultValue;

    public final static Type getListElementType(final Type listType)
    {
        /*
         * Map<TypeVariable<?>, java.lang.reflect.Type> typeArgs = TypeUtils.getTypeArguments(listType,
         * ValueType.JAVA_TYPE_LIST_INTERFACE);
         *
         * final Iterator<java.lang.reflect.Type> parameterTypes = typeArgs.values().iterator();
         * if (parameterTypes.hasNext())
         * {
         * return parameterTypes.next();
         * }
         */

        if (listType instanceof ParameterizedType)
        {
            final ParameterizedType parameterizedType = (ParameterizedType) listType;
            final Type[] actualTypeArgs = parameterizedType.getActualTypeArguments();
            if (actualTypeArgs == null || actualTypeArgs.length == 0)
            {
                return null;
            }

            return actualTypeArgs[0];
        }
        else
        {
            return null;
        }
    }

    public final static ValueType getStaticallyMappedType(final Class<?> rawJavaType)
    {

        if (ValueType.isStaticallyMappedType(rawJavaType))
        {
            return ValueType.TYPE_MAP.get(rawJavaType);
        }

        return null;
    }

    public final static ValueType getValueType(final Type type)
    {

        final Class<?> rawType = ValueType.getRawType(type);
        final ValueType valueType;

        if (ValueType.isStaticallyMappedType(rawType))
        {
            valueType = ValueType.getStaticallyMappedType(rawType);
        }
        else if (TypeUtils.isAssignable(rawType, List.class))
        {
            valueType = ValueType.List;
        }
        else if (TypeUtils.isAssignable(rawType, Enum.class))
        {
            valueType = ValueType.SingleSelect;
        }
        else if (ValueType.isSchemaInterface(rawType))
        {
            valueType = ValueType.Model;
        }
        else
        {
            valueType = ValueType.Native;
        }

        return valueType;
    }

    public final static ValueType getValueType(final Value value)
    {

        final ValueType valueType;
        if (value instanceof TextValue)
        {
            valueType = ValueType.Text;
        }
        else if (value instanceof ListValue)
        {
            valueType = ValueType.List;
        }
        else if (value instanceof LinkValue)
        {
            valueType = ValueType.Link;
        }
        else if (value instanceof ModelValue)
        {
            valueType = ValueType.Model;
        }
        else if (value instanceof IntegerValue)
        {
            valueType = ValueType.Integer;
        }
        else if (value instanceof BooleanValue)
        {
            valueType = ValueType.Boolean;
        }
        else if (value instanceof DoubleValue)
        {
            valueType = ValueType.Double;
        }
        else if (value instanceof LongValue)
        {
            valueType = ValueType.Long;
        }
        else if (value instanceof DateValue)
        {
            valueType = ValueType.Date;
        }
        else if (value instanceof SingleSelectValue)
        {
            valueType = ValueType.SingleSelect;
        }
        else
        {
            valueType = ValueType.Native;
        }

        return valueType;
    }

    public static boolean isLinkType(final Type valueType)
    {

        return TypeUtils.isAssignable(valueType, ValueType.JAVA_TYPE_LINK);
    }

    public static boolean isListType(final Type valueType)
    {

        return TypeUtils.isAssignable(valueType, ValueType.JAVA_TYPE_LIST_INTERFACE);
    }

    public final static boolean isModel(final Object o)
    {

        return ValueType.isModelType(o.getClass());
    }

    public final static boolean isModelType(final Type type)
    {

        final Class<?> rawType = ValueType.getRawType(type);
        return ValueType.isSchemaInterface(rawType);
    }

    public final static boolean isSchemaInterface(final Class<?> schemaInterface)
    {

        return (ValueType.JAVA_TYPE_MODEL.isAssignableFrom(schemaInterface));
    }

    public final static boolean isStaticallyMappedType(final Class<?> rawJavaType)
    {

        return ValueType.TYPE_MAP.containsKey(rawJavaType);
    }

    public final static Class<?> getRawType(final Type type)
    {

        final Class<?> rawType;

        if (type instanceof Class<?>)
        {
            rawType = (Class<?>) type;
        }
        else if (type instanceof ParameterizedType)
        {
            rawType = (Class<?>) ((ParameterizedType) type).getRawType();
        }
        else
        {
            // Should not happen.
            throw new IllegalArgumentException("The raw type: " + type
                    + " is invalid. The heap value must be a class/interface or a parameterized class/interface.");
        }

        return rawType;
    }

    public final Object getDefaultValue()
    {

        if (_DefaultValue == null)
        {

            switch (this)
            {
                case Boolean:
                {
                    _DefaultValue = java.lang.Boolean.FALSE;
                    break;
                }
                case Double:
                {
                    _DefaultValue = 0.0d;
                    break;
                }
                case Integer:
                {
                    _DefaultValue = 0;
                    break;
                }
                case Long:
                {
                    _DefaultValue = 0L;
                    break;
                }
                default:
                {
                    _DefaultValue = null;
                    break;
                }

            } // End of switch
        }

        return _DefaultValue;
    }

}

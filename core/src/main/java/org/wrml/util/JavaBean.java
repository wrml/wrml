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
package org.wrml.util;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;

/**
 * A runtime-optimized compilation of Java Bean metadata related to a given interface/class.
 */
public class JavaBean
{

    /**
     * "is" is the standard *read* access method name prefix for Booleans.
     */
    public static final String IS = "is";

    /**
     * "get" is the standard *read* access method name prefix non-Booleans.
     */
    public static final String GET = "get";

    /**
     * "set" is the standard *write* access method name prefix for all property types.
     */
    public static final String SET = "set";

    private final Class<?> _IntrospectedClass;

    private final Class<?> _StopClass;

    private final SortedMap<String, Property> _Properties;

    private final SortedMap<String, SortedSet<JavaMethod>> _OtherMethods;

    @SuppressWarnings("unchecked")
    public <T extends Annotation> JavaBean(final Class<?> forClass, final Class<?> stopClass, final Class<?>... otherMethodAnnotations)
    {

        _IntrospectedClass = forClass;
        _StopClass = (stopClass != null) ? stopClass : Object.class;

        _Properties = new TreeMap<String, Property>();
        _OtherMethods = new TreeMap<String, SortedSet<JavaMethod>>();

        /*
         * Initialize the methods, a mapping of method name to set of method metadata (one per differing signature of the same method name).
         */

        // Note that this array contains all public methods (including inherited ones).
        final Method[] methods = _IntrospectedClass.getMethods();

        if (ArrayUtils.isEmpty(methods))
        {
            return;
        }

        for (final Method method : methods)
        {

            if (!Modifier.isPublic(method.getModifiers()) || method.getDeclaringClass().isAssignableFrom(_StopClass))
            {
                continue;
            }

            final String methodName = method.getName();

            String propertyName = null;
            boolean isRead = false;

            boolean isOtherMethod = false;
            if (otherMethodAnnotations != null)
            {
                for (final Class<?> annotationClass : otherMethodAnnotations)
                {
                    isOtherMethod = (method.getAnnotation((Class<T>) annotationClass) != null);
                    if (isOtherMethod)
                    {
                        break;
                    }
                }
            }

            if (!isOtherMethod)
            {

                if (methodName.startsWith(JavaBean.GET) && (method.getParameterTypes().length == 0))
                {
                    propertyName = methodName.substring(JavaBean.GET.length());
                    isRead = true;
                }
                else if (methodName.startsWith(JavaBean.IS) && (method.getParameterTypes().length == 0))
                {
                    propertyName = methodName.substring(JavaBean.IS.length());
                    isRead = true;
                }
                else if (methodName.startsWith(JavaBean.SET) && (method.getParameterTypes().length == 1))
                {
                    propertyName = methodName.substring(JavaBean.SET.length());
                    isRead = false;
                }
            }

            if (propertyName == null)
            {
                // The method is not part of a JavaBean property's definition

                // Add "other" list of JavaMethods
                SortedSet<JavaMethod> namedMethods;

                if (!_OtherMethods.containsKey(methodName))
                {
                    namedMethods = new TreeSet<JavaMethod>();
                    _OtherMethods.put(methodName, namedMethods);
                }

                namedMethods = _OtherMethods.get(methodName);
                namedMethods.add(new JavaMethod(method));

                continue;
            }

            // Apply the introspector's standardized adjustment to arrive at the
            // camel-cased property name
            propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);

            Property property = null;
            if (!_Properties.containsKey(propertyName))
            {
                property = new Property(propertyName);
                _Properties.put(propertyName, property);
            }

            property = _Properties.get(propertyName);

            if (isRead)
            {
                final JavaMethod getter = new JavaMethod(method);
                property.setGetter(getter);
            }
            else
            {
                final JavaMethod setter = new JavaMethod(method);
                property.setSetter(setter);
            }
        }
    }

    public Class<?> getIntrospectedClass()
    {

        return _IntrospectedClass;
    }

    public SortedMap<String, SortedSet<JavaMethod>> getOtherMethods()
    {

        return _OtherMethods;
    }

    public SortedMap<String, Property> getProperties()
    {

        return _Properties;
    }

    public Class<?> getStopClass()
    {

        return _StopClass;
    }

    private static final String FORMAT_TO_STRING = "%s [introspectedClass = %s]";

    @Override
    public String toString()
    {
        return String.format(FORMAT_TO_STRING, getClass().getName(), _IntrospectedClass);
    }

    public class Property implements Comparable<Property>
    {

        private final String _Name;

        private JavaMethod _Getter;

        private JavaMethod _Setter;

        Property(final String name)
        {

            _Name = name;
        }

        @Override
        public int compareTo(final Property other)
        {
            return ComparisonChain.start()//
                    .compare(_Name, other._Name) //
                    .compare(getDeclaringClass().getCanonicalName(), other.getDeclaringClass().getCanonicalName()) //
                    .result();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof Property)
            {
                final Property other = (Property) obj;
                return java.util.Objects.equals(this, other) //
                        && java.util.Objects.equals(getClass(), other.getClass()) //
                        && java.util.Objects.equals(_Getter, other._Getter);
            }
            else
            {
                return java.util.Objects.equals(this, obj) //
                        && java.util.Objects.equals(getClass(), obj.getClass());
            }
        }

        public Class<?> getDeclaringClass()
        {

            return getGetter().getMethod().getDeclaringClass();
        }

        public JavaMethod getGetter()
        {

            return _Getter;
        }

        public void setGetter(final JavaMethod getter)
        {

            _Getter = getter;
        }

        public String getName()
        {

            return _Name;
        }

        public JavaMethod getSetter()
        {

            return _Setter;
        }

        public void setSetter(final JavaMethod setter)
        {

            _Setter = setter;
        }

        public java.lang.reflect.Type getType()
        {
            if (_Getter != null && _Getter.getSignature() != null)
                return _Getter.getSignature().getReturnType();
            return null;
        }

        @Override
        public int hashCode()
        {
            return com.google.common.base.Objects.hashCode(_Name, _Getter);
        }

        public boolean isReadOnly()
        {

            return (_Setter == null);
        }

        public <T extends Annotation> T getAnnotation(final Class<T> annotationClass)
        {
            if (_Getter == null && _Setter == null)
            {
                throw new RuntimeException("getter or setter required");
            }
            T annotation = null;
            if (_Getter != null && _Getter.getMethod() != null)
            {
                annotation = _Getter.getMethod().getAnnotation(annotationClass);
            }
            if (annotation == null)
            {
                if (_Setter != null && _Setter.getMethod() != null)
                {
                    annotation = _Setter.getMethod().getAnnotation(annotationClass);
                }
            }
            return annotation;
        }

    }

}

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

package org.wrml.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.wrml.util.Compare;

public class JavaBean {

    private final Class<?> _IntrospectedClass;
    private final Class<?> _StopClass;

    private final Map<String, Property> _Properties;
    private final Map<String, Set<JavaMethod>> _OtherMethods;

    public JavaBean(Class<?> forClass, Class<?> stopClass) {
        _IntrospectedClass = forClass;
        _StopClass = stopClass;

        _Properties = new TreeMap<String, Property>();
        _OtherMethods = new TreeMap<String, Set<JavaMethod>>();

        /*
         * Initialize the methods, a mapping of method name to set of
         * method metadata (one per differing signature of the same method
         * name).
         */

        final Method[] methods = _IntrospectedClass.getMethods();

        if ((methods == null) || (methods.length == 0)) {
            return;
        }

        for (final Method method : methods) {

            if (!Modifier.isPublic(method.getModifiers())
                    || ((_StopClass != null) && method.getDeclaringClass().isAssignableFrom(_StopClass))) {
                continue;
            }

            final String methodName = method.getName();
            String propertyName = null;

            boolean isRead = false;
            if (methodName.startsWith(TypeSystem.GET) && (method.getParameterTypes().length == 0)) {
                propertyName = methodName.substring(TypeSystem.GET.length());
                isRead = true;
            }
            else if (methodName.startsWith(TypeSystem.IS) && (method.getParameterTypes().length == 0)) {
                propertyName = methodName.substring(TypeSystem.IS.length());
                isRead = true;
            }
            else if (methodName.startsWith(TypeSystem.SET) && (method.getParameterTypes().length == 1)) {
                propertyName = methodName.substring(TypeSystem.SET.length());
                isRead = false;
            }

            if (propertyName == null) {
                // The method is not part of a JavaBean field's definition

                // Add "other" list of JavaMethods
                Set<JavaMethod> namedMethods;

                if (!_OtherMethods.containsKey(methodName)) {
                    namedMethods = new TreeSet<JavaMethod>();
                    _OtherMethods.put(methodName, namedMethods);
                }

                namedMethods = _OtherMethods.get(methodName);
                namedMethods.add(new JavaMethod(method));

                continue;
            }

            // Apply the introspector's standardized adjustment to arrive at the camel-cased field name
            propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);

            Property property = null;
            if (!_Properties.containsKey(propertyName)) {
                property = new Property(propertyName);
                _Properties.put(propertyName, property);
            }

            property = _Properties.get(propertyName);

            if (isRead) {
                final JavaMethod getter = new JavaMethod(method);
                property.setGetter(getter);
            }
            else {
                final JavaMethod setter = new JavaMethod(method);
                property.setSetter(setter);
            }
        }
    }

    public Class<?> getIntrospectedClass() {
        return _IntrospectedClass;
    }

    public Map<String, Set<JavaMethod>> getOtherMethods() {
        return _OtherMethods;
    }

    public Map<String, Property> getProperties() {
        return _Properties;
    }

    public Class<?> getStopClass() {
        return _StopClass;
    }

    @Override
    public String toString() {
        return getClass().getName() + " [introspectedClass = " + _IntrospectedClass + "]";
    }

    public class Property implements Comparable<Property> {

        private final String _Name;
        private JavaMethod _Getter;
        private JavaMethod _Setter;

        Property(final String name) {
            _Name = name;
        }

        @Override
        public int compareTo(Property other) {
            final int propertyNameComparisonResult = Compare.twoStrings(getName(), other.getName());
            if (propertyNameComparisonResult == 0) {
                // The property names are the same, so compare their declaring class names.
                return Compare.twoComparables(getDeclaringClass().getCanonicalName(), other.getDeclaringClass()
                        .getCanonicalName());
            }
            else {
                return propertyNameComparisonResult;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Property other = (Property) obj;
            if (_Getter == null) {
                if (other._Getter != null) {
                    return false;
                }
            }
            else if (!_Getter.equals(other._Getter)) {
                return false;
            }
            return true;
        }

        public Class<?> getDeclaringClass() {
            return getGetter().getMethod().getDeclaringClass();
        }

        public JavaMethod getGetter() {
            return _Getter;
        }

        public String getName() {
            return _Name;
        }

        public JavaMethod getSetter() {
            return _Setter;
        }

        public java.lang.reflect.Type getType() {
            return getGetter().getSignature().getReturnType();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((_Getter == null) ? 0 : _Getter.hashCode());
            return result;
        }

        public void setGetter(JavaMethod getter) {
            _Getter = getter;
        }

        public void setSetter(JavaMethod setter) {
            _Setter = setter;
        }

    }

}

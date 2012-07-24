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
import java.lang.reflect.Type;

import org.wrml.util.Compare;
import org.wrml.util.Composition;

public class JavaMethod implements Comparable<JavaMethod> {

    private final Method _Method;
    private final Signature _Signature;

    public JavaMethod(final Method method) {
        _Method = method;
        _Signature = new Signature(_Method);
    }

    @Override
    public int compareTo(JavaMethod other) {
        return Compare.twoComparables(this.getSignature(), other.getSignature());
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
        final JavaMethod other = (JavaMethod) obj;
        if (_Signature == null) {
            if (other._Signature != null) {
                return false;
            }
        }
        else if (!_Signature.equals(other._Signature)) {
            return false;
        }
        return true;
    }

    public Method getMethod() {
        return _Method;
    }

    public int getParameterCount() {
        return getSignature().getParameterTypes().length;
    }

    public Signature getSignature() {
        return _Signature;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((_Signature == null) ? 0 : _Signature.hashCode());
        return result;
    }

    public static class Signature extends Composition implements Comparable<Signature> {

        private final String _Name;
        private final Type[] _ParameterTypes;
        private String _ToString;
        private final Type _ReturnType;

        public Signature(Method method) {
            this(method.getName(), method.getGenericParameterTypes(), method.getGenericReturnType());
        }

        public Signature(String name, Type[] parameterTypes, Type returnType) {

            // A method's return type is not considered for uniqueness in a signature's composition
            super(name, parameterTypes);

            _Name = name;
            _ParameterTypes = parameterTypes;
            _ReturnType = returnType;

        }

        @Override
        public int compareTo(Signature other) {
            return Compare.twoStrings(this.toString(), String.valueOf(other));
        }

        public String getName() {
            return _Name;
        }

        public Type[] getParameterTypes() {
            return _ParameterTypes;
        }

        public java.lang.reflect.Type getReturnType() {
            return _ReturnType;
        }

        @Override
        public String toString() {

            if (_ToString == null) {
                final StringBuilder signature = new StringBuilder();
                final String name = getName();
                signature.append(name);
                signature.append('(');
                final Type[] parameterTypes = getParameterTypes();
                if (parameterTypes != null) {
                    for (int i = 0; i < parameterTypes.length; i++) {
                        final Type parameterType = parameterTypes[i];
                        signature.append(parameterType);
                        if (i < (parameterTypes.length - 1)) {
                            signature.append(", ");
                        }
                    }
                }

                signature.append(')');

                // That was kind of a lot of work. Let's not do that again.
                _ToString = signature.toString();
            }

            return _ToString;
        }
    }

}

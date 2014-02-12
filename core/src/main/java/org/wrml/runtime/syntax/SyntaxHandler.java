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
package org.wrml.runtime.syntax;

import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

public abstract class SyntaxHandler<S> {

    public final static String TYPE_VARIABLE_NAME = "S";

    public static Class<?> getSyntaxType(final Class<?> syntaxHandlerClass) {

        final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(syntaxHandlerClass,
                SyntaxHandler.class);

        for (final TypeVariable<?> typeVar : typeArguments.keySet()) {

            final String typeVarName = typeVar.getName();
            if (SyntaxHandler.TYPE_VARIABLE_NAME.equals(typeVarName)) {
                return (Class<?>) typeArguments.get(typeVar);
            }
            else {
                throw new RuntimeException("Unexpected type variable name  \"" + typeVarName
                        + "\" in SyntaxHandler class (" + syntaxHandlerClass + ")");
            }

        }

        return null;

    }

    private Class<?> _SyntaxType;

    public abstract String formatSyntaxValue(final S syntaxValue) throws SyntaxHandlerException;

    public Class<?> getSyntaxType() {

        if (_SyntaxType == null) {
            _SyntaxType = SyntaxHandler.getSyntaxType(getClass());
        }
        return _SyntaxType;
    }

    public abstract S parseSyntacticText(final String syntacticText) throws SyntaxHandlerException;
}

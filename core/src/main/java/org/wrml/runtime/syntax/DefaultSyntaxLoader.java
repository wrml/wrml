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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.wrml.model.schema.Syntax;
import org.wrml.runtime.Context;
import org.wrml.runtime.ContextConfiguration;
import org.wrml.runtime.Keys;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultSyntaxLoader implements SyntaxLoader
{

    private final Map<URI, Syntax> _SystemSyntaxes;

    private final BiMap<URI, Class<?>> _SyntaxUriToJavaClassBiMap;

    private final ConcurrentHashMap<Class<?>, SyntaxHandler<?>> _SyntaxHandlers;

    private Context _Context;

    public DefaultSyntaxLoader()
    {

        _SystemSyntaxes = new HashMap<>();
        _SyntaxHandlers = new ConcurrentHashMap<>();
        _SyntaxUriToJavaClassBiMap = HashBiMap.create();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public final String formatSyntaxValue(final Object syntaxValue)
    {

        if (syntaxValue instanceof String)
        {
            return (String) syntaxValue;
        }

        if (syntaxValue == null)
        {
            return null;
        }

        final SyntaxHandler syntaxHandler = getSyntaxHandler(syntaxValue.getClass());

        if (syntaxHandler != null)
        {
            return syntaxHandler.formatSyntaxValue(syntaxValue);
        }

        return String.valueOf(syntaxValue);
    }

    @Override
    public Context getContext()
    {

        return _Context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> SyntaxHandler<T> getSyntaxHandler(final Class<T> syntaxJavaClass)
    {

        return (SyntaxHandler<T>) _SyntaxHandlers.get(syntaxJavaClass);
    }

    @Override
    public final Class<?> getSyntaxJavaClass(final URI syntaxUri)
    {

        if (!_SyntaxUriToJavaClassBiMap.containsKey(syntaxUri))
        {
            return null;
        }

        return _SyntaxUriToJavaClassBiMap.get(syntaxUri);
    }

    @Override
    public final URI getSyntaxUri(final Class<?> syntaxClass)
    {

        final BiMap<Class<?>, URI> javaClassToSyntaxUriMap = _SyntaxUriToJavaClassBiMap.inverse();
        if (!javaClassToSyntaxUriMap.containsKey(syntaxClass))
        {
            return null;
        }

        return javaClassToSyntaxUriMap.get(syntaxClass);
    }

    @Override
    public void loadInitialState()
    {

        loadConfiguredSyntaxes();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final <T> T parseSyntacticText(final String text, final java.lang.reflect.Type targetType)
    {

        if (text == null)
        {
            return null;
        }

        if (targetType == null || targetType.equals(String.class))
        {
            return (T) text;
        }

        if (targetType.equals(Integer.TYPE) || targetType.equals(Integer.class))
        {
            return (T) new Integer(text);
        }

        if (targetType.equals(Boolean.TYPE) || targetType.equals(Boolean.class))
        {
            return (T) (text.equals("true") ? Boolean.TRUE : Boolean.FALSE);
        }

        if (targetType.equals(Long.TYPE) || targetType.equals(Long.class))
        {
            return (T) new Long(text);
        }

        if (targetType.equals(Double.TYPE) || targetType.equals(Double.class))
        {
            return (T) new Double(text);
        }

        if (TypeUtils.isAssignable(targetType, Enum.class))
        {
            return (T) Enum.valueOf((Class<Enum>) targetType, text);
        }

        if (targetType instanceof Class<?>)
        {

            final SyntaxHandler<?> syntaxHandler = getSyntaxHandler((Class<?>) targetType);

            if (syntaxHandler != null)
            {
                return (T) syntaxHandler.parseSyntacticText(text);
            }
        }

        throw new SyntaxRegistryException("Failed to transform text: \"" + text + "\" value to target type: "
                + targetType, null, this);

    }

    @Override
    public void init(final Context context)
    {

        _Context = context;
        loadSystemSyntaxes();

    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> T[] parseSyntacticTextArray(final String[] textArray, final T[] destArray)
    {

        final Type arrayComponentType = TypeUtils.getArrayComponentType(destArray.getClass());
        for (int i = 0; i < textArray.length; i++)
        {
            destArray[i] = (T) parseSyntacticText(textArray[i], arrayComponentType);
        }
        return destArray;
    }

    @Override
    public final void loadConfiguredSyntax(final SyntaxConfiguration syntaxConfiguration)
    {

        if (syntaxConfiguration == null)
        {
            return;
        }

        final URI syntaxUri = syntaxConfiguration.getSyntaxUri();
        if (syntaxUri == null)
        {
            throw new SyntaxRegistryException("The Syntax id (URI) cannot be null", null, this);
        }

        final String syntaxHandlerClassName = syntaxConfiguration.getHandler();
        if (syntaxHandlerClassName == null)
        {
            throw new SyntaxRegistryException("The SyntaxHandler class name cannot be null", null, this);
        }

        Class<?> syntaxHandlerClass;
        try
        {
            syntaxHandlerClass = Class.forName(syntaxHandlerClassName);
        }
        catch (final ClassNotFoundException e)
        {
            throw new SyntaxRegistryException("Failed to load SyntaxHandler class (" + syntaxHandlerClassName + ")", e,
                    this);
        }

        SyntaxHandler<?> syntaxHandler = null;
        try
        {
            syntaxHandler = (SyntaxHandler<?>) syntaxHandlerClass.newInstance();
        }
        catch (final Exception e)
        {
            throw new SyntaxRegistryException("Failed to create new instance of SyntaxHandler class ("
                    + syntaxHandlerClass + ")", e, this);
        }


        loadSyntax(syntaxUri, syntaxHandler);

    }

    @Override
    public final void loadSyntax(final URI syntaxUri, final SyntaxHandler<?> syntaxHandler)
    {

        if (_SyntaxUriToJavaClassBiMap.containsKey(syntaxUri))
        {
            throw new SyntaxRegistryException("The syntax  \"" + syntaxUri + "\" is already installed (using: "
                    + _SyntaxUriToJavaClassBiMap.get(syntaxUri) + ")", null, this);

        }

        final Class<?> syntaxJavaClass = syntaxHandler.getSyntaxType();
        if (_SyntaxHandlers.containsKey(syntaxJavaClass))
        {
            throw new SyntaxRegistryException("The syntax  \"" + syntaxJavaClass.getCanonicalName()
                    + "\" is already installed (using: " + _SyntaxHandlers.get(syntaxJavaClass) + ")", null, this);

        }

        loadSyntaxInternal(syntaxUri, syntaxHandler);

    }

    @Override
    public Syntax getLoadedSyntax(final Keys keys)
    {

        final URI uri = keys.getValue(getContext().getSchemaLoader().getDocumentSchemaUri());
        if (uri == null)
        {
            return null;
        }

        if (_SystemSyntaxes.containsKey(uri))
        {
            return _SystemSyntaxes.get(uri);
        }

        return null;
    }

    private final void loadSyntaxInternal(final URI syntaxUri, final SyntaxHandler<?> syntaxHandler)
    {

        final Class<?> syntaxJavaClass = syntaxHandler.getSyntaxType();
        _SyntaxUriToJavaClassBiMap.put(syntaxUri, syntaxJavaClass);
        _SyntaxHandlers.put(syntaxJavaClass, syntaxHandler);
    }

    private final void loadSystemSyntaxes()
    {

        for (final SystemSyntax systemSyntax : SystemSyntax.values())
        {
            final SyntaxHandler<?> syntaxHandler = systemSyntax.getSyntaxHandler();

            switch (systemSyntax)
            {

                case Boolean:
                    _SyntaxHandlers.put(Boolean.TYPE, syntaxHandler);
                    break;

                case Double:
                    _SyntaxHandlers.put(Double.TYPE, syntaxHandler);
                    break;

                case Integer:
                    _SyntaxHandlers.put(Integer.TYPE, syntaxHandler);
                    break;

                case Long:
                    _SyntaxHandlers.put(Long.TYPE, syntaxHandler);
                    break;

                default:
                    break;

            }


            final URI syntaxUri = systemSyntax.getSyntaxUri();
            loadSyntaxInternal(syntaxUri, syntaxHandler);
        }

        final Context context = getContext();

        // Loop again to build the system Syntax models now that the
        for (final SystemSyntax systemSyntax : SystemSyntax.values())
        {
            final Syntax syntax = context.newModel(Syntax.class);

            final URI syntaxUri = systemSyntax.getSyntaxUri();
            syntax.setUniqueName(systemSyntax.getUniqueName());
            syntax.setUri(syntaxUri);

            _SystemSyntaxes.put(syntaxUri, syntax);
        }

    }

    protected void loadConfiguredSyntaxes()
    {

        final Context context = getContext();
        final ContextConfiguration contextConfig = context.getConfig();
        final SyntaxLoaderConfiguration config = contextConfig.getSyntaxLoader();
        if (config == null)
        {
            return;
        }

        final SyntaxConfiguration[] syntaxConfigs = config.getSyntaxes();
        if ((syntaxConfigs == null) || (syntaxConfigs.length == 0))
        {
            return;
        }

        for (final SyntaxConfiguration syntaxConfiguration : syntaxConfigs)
        {
            loadConfiguredSyntax(syntaxConfiguration);
        }

    }
}

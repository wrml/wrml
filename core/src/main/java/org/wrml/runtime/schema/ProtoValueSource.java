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
package org.wrml.runtime.schema;

import org.apache.commons.lang3.StringUtils;
import org.wrml.model.Model;
import org.wrml.model.schema.ValueSourceType;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.syntax.SyntaxLoader;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A runtime implementation of the {@link ValueSourceType} concept. This class enables a value to be "pulled" from a variety of sources.
 *
 * @see LinkSlotBinding
 * @see CollectionSlotCriterion
 * @see CollectionPropertyProtoSlot
 * @see LinkProtoSlot
 */
public final class ProtoValueSource
{

    private final Prototype _ReferencePrototype;

    private final String _ReferenceSlot;

    private final ProtoSlot _ReferenceProtoSlot;

    private final Prototype _ReferrerPrototype;

    private final String _ValueSource;

    private final ValueSourceType _ValueSourceType;

    private final Object _ConstantValue;


    ProtoValueSource(final Prototype referencePrototype, final String referenceSlot, final Prototype referrerPrototype, final String valueSource, final ValueSourceType valueSourceType)
    {

        _ReferencePrototype = referencePrototype;
        _ReferenceSlot = referenceSlot;
        _ReferrerPrototype = referrerPrototype;
        _ValueSource = valueSource;
        _ValueSourceType = valueSourceType;

        if (_ReferrerPrototype != null && _ReferenceSlot != null)
        {
            _ReferenceProtoSlot = _ReferencePrototype.getProtoSlot(_ReferenceSlot);
            if (_ValueSourceType == ValueSourceType.Constant)
            {
                _ConstantValue = coerceStringValue(_ValueSource);
            }
            else
            {
                _ConstantValue = null;
            }

        }
        else
        {
            _ReferenceProtoSlot = null;
            if (_ValueSourceType == ValueSourceType.Constant)
            {
                _ConstantValue = _ValueSource;
            }
            else
            {
                _ConstantValue = null;
            }

        }

    }

    /**
     * The {@link Prototype} associated with the referenced model.
     *
     * @return The {@link Prototype} associated with the referenced model.
     */
    public Prototype getReferencePrototype()
    {

        return _ReferencePrototype;
    }

    /**
     * The slot within the referenced {@link org.wrml.model.schema.Schema}.
     *
     * @return The slot within the referenced {@link org.wrml.model.schema.Schema}.
     */
    public String getReferenceSlot()
    {

        return _ReferenceSlot;
    }

    /**
     * The {@link ProtoSlot} associated with the reference slot.
     *
     * @return The {@link ProtoSlot} associated with the reference slot.
     * @see #getReferencePrototype()
     * @see #getReferenceSlot()
     */
    public ProtoSlot getReferenceProtoSlot()
    {

        return _ReferenceProtoSlot;
    }

    /**
     * The {@link Prototype} associated with the referrer model.
     *
     * @return The {@link Prototype} associated with the referrer model.
     */
    public Prototype getReferrerPrototype()
    {

        return _ReferrerPrototype;
    }

    /**
     * The source type for the binding value.
     *
     * @return The source type for the binding value.
     */
    public ValueSourceType getValueSourceType()
    {

        return _ValueSourceType;
    }

    /**
     * The {@link String} representation of the source of the value that will be used to "fill in" the reference slot.
     *
     * @return The {@link String} representation of the source of the value that will be used to "fill in" the reference slot.
     */
    public String getValueSource()
    {

        return _ValueSource;
    }

    /**
     * The constant value associated with this {@link ProtoValueSource}.
     *
     * @param <T> The generic return type associated with the referenced slot.
     * @return The constant value associated with this {@link ProtoValueSource}.
     * @see ValueSourceType#Constant
     */
    public <T> T getConstantValue()
    {

        return (T) _ConstantValue;
    }

    /**
     * Get the value from the source given the specified {@link Model} referrer.
     *
     * @param referrer The {@link Model} that is linking or searching for another; using the returned value to help form the bond.
     * @return The value associated with this {@link ProtoValueSource} based upon the given {@link Model} referrer.
     */
    public <T> T getValue(final Model referrer)
    {

        switch (_ValueSourceType)
        {
            case ReferrerSlot:


                if (!_ValueSource.contains("."))
                {
                    return (T) referrer.getSlotValue(_ValueSource);
                }
                else
                {

                    // Handle "." (dot notation)
                    final String[] propertyNames = StringUtils.split(_ValueSource, '.');
                    Object propertyValue = referrer;

                    for (final String propertyName : propertyNames)
                    {

                        if (propertyValue == null)
                        {
                            return null;
                        }

                        if (propertyValue instanceof Model)
                        {
                            propertyValue = ((Model) propertyValue).getSlotValue(propertyName);
                        }
                        else
                        {
                            final String getterMethodName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
                            final Class<?> propertyValueClass = propertyValue.getClass();
                            try
                            {
                                final Method getterMethod = propertyValueClass.getMethod(getterMethodName);
                                propertyValue = getterMethod.invoke(propertyValue);
                            }
                            catch (Exception t)
                            {
                                return null;
                            }
                        }
                    }

                    return (T) propertyValue;
                }


            case QueryParameter:

                final Dimensions referrerDimensions = referrer.getDimensions();

                final Map<String, String> parameters = referrerDimensions.getQueryParameters();

                if (parameters != null && parameters.containsKey(_ValueSource))
                {
                    final String parameterValue = parameters.get(_ValueSource);
                    final Object value = coerceStringValue(parameterValue);
                    return (T) value;
                }

                return null;

            case Constant:
            default:
                return getConstantValue();
        }

    }

    /**
     * Convert the specified string value into a value that is compatible with the reference slot's type.
     *
     * @param stringValue The {@link String} value to coerce into a compatible value.
     * @param <T>         The generic return type that enables the caller to omit the cast operator.
     * @return The converted value of the specified string value.
     */
    private <T> T coerceStringValue(final String stringValue)
    {

        if (stringValue == null || _ReferenceProtoSlot == null)
        {
            return (T) stringValue;
        }

        final Context context = _ReferenceProtoSlot.getContext();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
        final Type referenceSlotType = _ReferenceProtoSlot.getHeapValueType();

        if (ValueType.isListType(referenceSlotType))
        {
            // [a, b, c]

            String listString = stringValue.trim();
            listString = StringUtils.stripStart(listString, "[");
            listString = StringUtils.stripEnd(listString, "]");
            if (listString.isEmpty())
            {
                return (T) Collections.EMPTY_LIST;
            }

            final Type elementType = ValueType.getListElementType(referenceSlotType);

            final String[] listElementsStringArray = StringUtils.split(listString, ",");
            final List<Object> listValue = new ArrayList<>(listElementsStringArray.length);
            for (final String elementString : listElementsStringArray)
            {
                final Object element = syntaxLoader.parseSyntacticText(elementString.trim(), elementType);
                listValue.add(element);
            }

            return (T) listValue;
        }
        else
        {

            final Object value = syntaxLoader.parseSyntacticText(stringValue, referenceSlotType);
            return (T) value;
        }
    }

}

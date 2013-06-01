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

import org.apache.commons.lang3.reflect.TypeUtils;
import org.wrml.model.Model;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.util.JavaBean.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * A {@link ProtoSlot} that represents a "property" slot, which is a name/value pair that isn't associated with a hyperlink (which are managed by the runtime with
 * {@link LinkProtoSlot}s).
 * </p>
 * <p>
 * A {@link PropertyProtoSlot} is used by the runtime to hold the detailed knowledge about a particular {@link org.wrml.model.schema.Slot}. Unlike the {@link LinkProtoSlot}, this
 * type of {@link ProtoSlot} is to describe a slot that has<i>property</i> and is similar (in concept) to the {@link java.beans.PropertyDescriptor}.
 * </p>
 * 
 * @see Property
 * @see Prototype
 * @see ProtoSlot
 * @see LinkProtoSlot
 * @see org.wrml.model.schema.Schema
 * @see org.wrml.model.schema.Slot
 */
public class PropertyProtoSlot extends ProtoSlot
{

    private final Property _Property;

    private java.lang.reflect.Type _ListElementType;

    private URI _ListElementSchemaUri;

    private URI _ModelSchemaUri;

    private Object _DefaultValue;

    private Object _DivisibleByValue;

    private Object _MaximumValue;

    private Object _MinimumValue;

    private Integer _MinimumLength;

    private Integer _MaximumLength;

    private Integer _MinimumSize;

    private Integer _MaximumSize;

    private boolean _IsExclusiveMaximum;

    private boolean _IsExclusiveMinimum;

    private Set<Object> _DisallowedValues;

    private boolean _Searchable;

    PropertyProtoSlot(final Prototype prototype, final String slotName, final Property property)
    {

        super(prototype, slotName);

        if (property == null)
        {
            throw new NullPointerException("Prototype (" + prototype + ") Slot (" + slotName + ") property cannot be null.");
        }

        _Property = property;

        final Type heapValueType = getHeapValueType();

        final SyntaxLoader syntaxLoader = getContext().getSyntaxLoader();

        final DefaultValue defaultValue = getAnnotation(DefaultValue.class);
        if (defaultValue != null)
        {
            final String defaultValueString = defaultValue.value();

            try
            {
                _DefaultValue = syntaxLoader.parseSyntacticText(defaultValueString, heapValueType);
            }
            catch (final Exception e)
            {

                throw new PrototypeException(prototype + " slot named \"" + slotName + "\" default value annotation's value could not be converted from text value \""
                        + defaultValueString + "\" to a Java " + heapValueType + ". Detail message: " + e.getMessage(), e, prototype, slotName);
            }
        }

        if (Boolean.TYPE.equals(heapValueType))
        {
            if (_DefaultValue == null)
            {
                _DefaultValue = Boolean.FALSE;
            }
        }
        else if (TypeUtils.isAssignable(heapValueType, Enum.class))
        {

            if (_DefaultValue == null)
            {
                // Enum's default to their first constant
                // Single selects default to the first choice
                @SuppressWarnings("unchecked")
                final Class<Enum<?>> enumValueType = (Class<Enum<?>>) heapValueType;
                if (enumValueType != null)
                {
                    final Enum<?>[] enumChoices = enumValueType.getEnumConstants();
                    if (enumChoices != null && enumChoices.length > 0)
                    {
                        _DefaultValue = enumChoices[0];
                    }
                }
            }

        }
        else if (TypeUtils.isAssignable(heapValueType, Number.class) || Integer.TYPE.equals(heapValueType) || Long.TYPE.equals(heapValueType) || Double.TYPE.equals(heapValueType))
        {

            if (_DefaultValue == null && Integer.TYPE.equals(heapValueType) || Long.TYPE.equals(heapValueType) || Double.TYPE.equals(heapValueType))
            {
                _DefaultValue = getValueType().getDefaultValue();
            }

            // isolate()
            {
                final MinimumValue minimumValue = getAnnotation(MinimumValue.class);
                if (minimumValue != null)
                {
                    final String minimumValueString = minimumValue.value();

                    try
                    {
                        _MinimumValue = syntaxLoader.parseSyntacticText(minimumValueString, heapValueType);
                    }
                    catch (final Exception e)
                    {

                        throw new PrototypeException(prototype + " slot named \"" + slotName + "\" minimum value annotation's value could not be converted from text value \""
                                + minimumValueString + "\" to a Java " + heapValueType + ". Detail message: " + e.getMessage(), e, prototype, slotName);
                    }

                    _IsExclusiveMinimum = minimumValue.exclusive();

                }
            }

            // isolate()
            {
                final MaximumValue maximumValue = getAnnotation(MaximumValue.class);
                if (maximumValue != null)
                {
                    final String maximumValueString = maximumValue.value();

                    try
                    {
                        _MaximumValue = syntaxLoader.parseSyntacticText(maximumValueString, heapValueType);
                    }
                    catch (final Exception e)
                    {

                        throw new PrototypeException(prototype + " slot named \"" + slotName + "\" maximum value annotation's value could not be converted from text value \""
                                + maximumValueString + "\" to a Java " + heapValueType + ". Detail message: " + e.getMessage(), e, prototype, slotName);
                    }

                    _IsExclusiveMaximum = maximumValue.exclusive();
                }
            }

            // isolate()
            {
                final DivisibleByValue divisibleByValue = getAnnotation(DivisibleByValue.class);
                if (divisibleByValue != null &&
                // The "divisible by" constraint does not apply to doubles
                        !Double.TYPE.equals(heapValueType) && !TypeUtils.isAssignable(heapValueType, Double.class))
                {
                    final String divisibleByValueString = divisibleByValue.value();

                    try
                    {
                        _DivisibleByValue = syntaxLoader.parseSyntacticText(divisibleByValueString, heapValueType);
                    }
                    catch (final Exception e)
                    {

                        throw new PrototypeException(prototype + " slot named \"" + slotName + "\" divisibleBy value annotation's value could not be converted from text value \""
                                + divisibleByValueString + "\" to a Java " + heapValueType + ". Detail message: " + e.getMessage(), e, prototype, slotName);
                    }

                    if (_DivisibleByValue.equals(0))
                    {
                        throw new PrototypeException(prototype + " slot named \"" + slotName + "\" divisibleBy value annotation's value could not be converted from text value \""
                                + divisibleByValueString + "\" to a Java " + heapValueType + ". Detail message: " + "zero value", null, prototype, slotName);

                    }

                }
            }

            // isolate()
            {
                final DisallowedValues disallowedValues = getAnnotation(DisallowedValues.class);
                if (disallowedValues != null)
                {
                    final String[] disallowedValuesArray = disallowedValues.value();
                    if (disallowedValuesArray != null)
                    {
                        _DisallowedValues = new LinkedHashSet<>(disallowedValuesArray.length);
                        for (final String disallowedValueString : disallowedValuesArray)
                        {

                            final Object disallowedValue = syntaxLoader.parseSyntacticText(disallowedValueString, heapValueType);

                            _DisallowedValues.add(disallowedValue);
                        }
                    }
                }
            }

        }
        else if (String.class.equals(heapValueType))
        {

            final MinimumLength minimumLength = getAnnotation(MinimumLength.class);
            if (minimumLength != null)
            {
                _MinimumLength = minimumLength.value();
            }

            final MaximumLength maximumLength = getAnnotation(MaximumLength.class);
            if (maximumLength != null)
            {
                _MaximumLength = maximumLength.value();
            }

            final DisallowedValues disallowedValues = getAnnotation(DisallowedValues.class);
            if (disallowedValues != null)
            {
                final String[] disallowedValuesArray = disallowedValues.value();
                if (disallowedValuesArray != null)
                {
                    _DisallowedValues = new LinkedHashSet<>(disallowedValuesArray.length);
                    _DisallowedValues.addAll(Arrays.asList(disallowedValuesArray));
                }
            }

        }
        else if (TypeUtils.isAssignable(heapValueType, Collection.class))
        {

            final MinimumSize minimumSize = getAnnotation(MinimumSize.class);
            if (minimumSize != null)
            {
                _MinimumSize = minimumSize.value();
            }

            final MaximumSize maximumSize = getAnnotation(MaximumSize.class);
            if (maximumSize != null)
            {
                _MaximumSize = maximumSize.value();
            }

        }

        final Searchable searchable = getAnnotation(Searchable.class);
        if (searchable != null)
        {
            _Searchable = true;
        }

    }

    @Override
    public URI getDeclaringSchemaUri()
    {

        final Class<?> declaringSchemaInterface = getProperty().getDeclaringClass();
        final URI declaringSchemaUri = getSchemaLoader().getTypeUri(declaringSchemaInterface);
        return declaringSchemaUri;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefaultValue()
    {

        return (T) _DefaultValue;
    }

    public Set<?> getDisallowedValues()
    {

        return _DisallowedValues;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDivisibleByValue()
    {

        return (T) _DivisibleByValue;
    }

    @Override
    public Type getHeapValueType()
    {

        return getProperty().getType();
    }

    public URI getListElementSchemaUri()
    {

        if (_ListElementSchemaUri == null)
        {

            final java.lang.reflect.Type listElementType = getListElementType();

            // Compare the List's element type to the Model type
            if (TypeUtils.isAssignable(listElementType, ValueType.JAVA_TYPE_MODEL))
            {
                final SchemaLoader schemaLoader = getSchemaLoader();
                _ListElementSchemaUri = schemaLoader.getTypeUri(listElementType);
            }
        }

        return _ListElementSchemaUri;
    }

    public java.lang.reflect.Type getListElementType()
    {

        if (_ListElementType == null)
        {

            if (getValueType() != ValueType.List)
            {
                throw new PrototypeException("Prototype (" + getPrototype() + ") Slot (" + getName() + ") is not a List.", null, getPrototype(), getName());
            }

            _ListElementType = ValueType.getListElementType(getHeapValueType());
        }

        return _ListElementType;
    }

    public Integer getMaximumLength()
    {

        return _MaximumLength;
    }

    public Integer getMaximumSize()
    {

        return _MaximumSize;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMaximumValue()
    {

        return (T) _MaximumValue;
    }

    public Integer getMinimumLength()
    {

        return _MinimumLength;
    }

    public Integer getMinimumSize()
    {

        return _MinimumSize;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMinimumValue()
    {

        return (T) _MinimumValue;
    }

    public URI getModelSchemaUri()
    {

        if (_ModelSchemaUri == null)
        {

            if (getValueType() != ValueType.Model)
            {
                throw new PrototypeException("Prototype (" + getPrototype() + ") Slot (" + getName() + ") is not a model type.", null, getPrototype(), getName());
            }

            final SchemaLoader schemaLoader = getSchemaLoader();
            _ModelSchemaUri = schemaLoader.getTypeUri(getHeapValueType());
        }

        return _ModelSchemaUri;
    }

    public Property getProperty()
    {

        return _Property;
    }

    public boolean isExclusiveMaximum()
    {

        return _IsExclusiveMaximum;
    }

    public boolean isExclusiveMinimum()
    {

        return _IsExclusiveMinimum;
    }

    public boolean isSearchable()
    {

        return _Searchable;
    }

    public void validateNewValue(final Model model, final Object newValue) throws PrototypeException
    {

        final String name = getName();

        if (_DisallowedValues != null && _DisallowedValues.contains(newValue))
        {
            throw new PrototypeException("The " + name + " value: " + newValue + " is disallowed.", null, getPrototype(), name);
        }

        final ValueType valueType = getValueType();

        switch (valueType)
        {
            case Boolean:
            {
                break;
            }

            case Date:
            {
                break;
            }

            case Double:
            {
                if (newValue == null)
                {
                    break;
                }

                if (_MaximumValue != null)
                {
                    final boolean lessThanMax;
                    if (isExclusiveMaximum())
                    {
                        lessThanMax = (double) newValue < (double) _MaximumValue;
                    }
                    else
                    {
                        lessThanMax = (double) newValue <= (double) _MaximumValue;
                    }

                    if (!lessThanMax)
                    {
                        throw new PrototypeException("The " + name + " value: " + newValue + " is greater than the maximum allowed value: " + _MaximumValue, null, getPrototype(),
                                name);
                    }
                }

                if (_MinimumValue != null)
                {
                    final boolean greaterThanMin;
                    if (isExclusiveMinimum())
                    {
                        greaterThanMin = (double) newValue > (double) _MinimumValue;
                    }
                    else
                    {
                        greaterThanMin = (double) newValue >= (double) _MinimumValue;
                    }

                    if (!greaterThanMin)
                    {
                        throw new PrototypeException("The " + name + " value: " + newValue + " is less than the minimum allowed value: " + _MinimumValue, null, getPrototype(),
                                name);
                    }
                }

                if (_DivisibleByValue != null)
                {

                    final double remainder = Math.IEEEremainder((double) newValue, (double) _DivisibleByValue);
                    final boolean divisbleBy = (remainder == 0.0);

                    if (!divisbleBy)
                    {
                        throw new PrototypeException("The " + name + " value: " + newValue + " is not divisible by: " + _DivisibleByValue, null, getPrototype(), name);
                    }
                }

                break;
            }

            case Integer:
            {
                if (newValue == null)
                {
                    break;
                }

                if (_MaximumValue != null)
                {
                    final boolean lessThanMax;
                    if (isExclusiveMaximum())
                    {
                        lessThanMax = (int) newValue < (int) _MaximumValue;
                    }
                    else
                    {
                        lessThanMax = (int) newValue <= (int) _MaximumValue;
                    }

                    if (!lessThanMax)
                    {
                        throw new PrototypeException("The " + name + " value: " + newValue + " is greater than the maximum allowed value: " + _MaximumValue, null, getPrototype(),
                                name);
                    }
                }

                if (_MinimumValue != null)
                {
                    final boolean greaterThanMin;
                    if (isExclusiveMinimum())
                    {
                        greaterThanMin = (int) newValue > (int) _MinimumValue;
                    }
                    else
                    {
                        greaterThanMin = (int) newValue >= (int) _MinimumValue;
                    }

                    if (!greaterThanMin)
                    {
                        throw new PrototypeException("The " + name + " value: " + newValue + " is less than the minimum allowed value: " + _MinimumValue, null, getPrototype(),
                                name);
                    }
                }

                if (_DivisibleByValue != null)
                {
                    final boolean divisbleBy = ((int) newValue % (int) _DivisibleByValue) == 0;
                    if (!divisbleBy)
                    {
                        throw new PrototypeException("The " + name + " value: " + newValue + " is not divisible by: " + _DivisibleByValue, null, getPrototype(), name);
                    }
                }

                break;
            }

            case Link:
            {
                break;
            }
            case List:
            {
                break;
            }
            case Long:
            {
                if (newValue == null)
                {
                    break;
                }

                if (_MaximumValue != null)
                {
                    final boolean lessThanMax;
                    if (isExclusiveMaximum())
                    {
                        lessThanMax = (long) newValue < (long) _MaximumValue;
                    }
                    else
                    {
                        lessThanMax = (long) newValue <= (long) _MaximumValue;
                    }

                    if (!lessThanMax)
                    {
                        throw new PrototypeException("The " + name + " value: " + newValue + " is greater than the maximum allowed value: " + _MaximumValue, null, getPrototype(),
                                name);
                    }
                }

                if (_MinimumValue != null)
                {
                    final boolean greaterThanMin;
                    if (isExclusiveMinimum())
                    {
                        greaterThanMin = (long) newValue > (long) _MinimumValue;
                    }
                    else
                    {
                        greaterThanMin = (long) newValue >= (long) _MinimumValue;
                    }

                    if (!greaterThanMin)
                    {
                        throw new PrototypeException("The " + name + " value: " + newValue + " is less than the minimum allowed value: " + _MinimumValue, null, getPrototype(),
                                name);
                    }
                }

                if (_DivisibleByValue != null)
                {
                    final boolean divisbleBy = ((long) newValue % (long) _DivisibleByValue) == 0L;
                    if (!divisbleBy)
                    {
                        throw new PrototypeException("The " + name + " value: " + newValue + " is not divisible by: " + _DivisibleByValue, null, getPrototype(), name);
                    }
                }

                break;

            }
            case Model:
            {
                break;
            }
            case Native:
            {
                break;
            }
            case SingleSelect:
            {
                break;
            }
            case Text:
            {
                if (newValue == null)
                {
                    break;
                }

                if (_MaximumLength != null)
                {
                    final boolean lessThanMax = ((String) newValue).length() < _MaximumLength;

                    if (!lessThanMax)
                    {
                        throw new PrototypeException("The " + name + " value: " + newValue + " is longer than the maximum allowed length: " + _MaximumLength, null, getPrototype(),
                                name);
                    }
                }

                if (_MinimumLength != null)
                {
                    final boolean greaterThanMin = ((String) newValue).length() > _MinimumLength;

                    if (!greaterThanMin)
                    {
                        throw new PrototypeException("The " + name + " value: " + newValue + " is shorter than the minimum allowed length: " + _MinimumLength, null,
                                getPrototype(), name);
                    }
                }

                break;
            }
            default:
            {
                break;
            }
        }

    }

    @Override
    protected <T extends Annotation> T getAnnotationInternal(final Class<T> annotationClass)
    {

        return getProperty().getAnnotation(annotationClass);
    }

}

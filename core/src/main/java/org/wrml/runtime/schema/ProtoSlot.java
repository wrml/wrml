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

import org.wrml.model.schema.Slot;
import org.wrml.model.schema.ValueType;
import org.wrml.runtime.Context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;
/**
 * <p>
 * A ProtoSlot (prototypical slot) is a {@link Prototype}'s runtime-optimized rendition of a schema {@link Slot}.
 * </p>
 *
 * @see Prototype
 * @see Slot
 */
public abstract class ProtoSlot implements Comparable<ProtoSlot>
{

    public static Comparator<ProtoSlot> ALPHA_ORDER = new Comparator<ProtoSlot>()
    {

        @Override
        public int compare(final ProtoSlot protoSlot1, final ProtoSlot protoSlot2)
        {
            return ComparisonChain.start()//
                    .compare(protoSlot1.getClass().getName(), protoSlot2.getClass().getName())//
                    // .compare(protoSlot1.getPrototype(), protoSlot2.getPrototype())// not Comparable
                    .compare(protoSlot1.getName(), protoSlot2.getName())//
                    .compare(protoSlot1.getValueType(), protoSlot2.getValueType())//
                    // .compare(protoSlot1.getHeapValueType(), protoSlot2.getHeapValueType())// not Comparable
                    .compare(protoSlot1.getSchemaUri(), protoSlot2.getSchemaUri())//
                    .compare(protoSlot1.getDeclaringSchemaUri(), protoSlot2.getDeclaringSchemaUri())//
                    .compare(protoSlot1.toString(), protoSlot2.toString())// fallback
                    .result();
        }

    };

    private final Prototype _Prototype;

    private final String _Name;

    private SortedSet<String> _Aliases;

    private String _Description;

    private String _Title;

    private ValueType _ValueType;

    private String _RealName;

    private boolean _Alias;

    /**
     * Creates a new ProtoSlot (only within the {@link Prototype}).
     *
     * @param prototype The slot's owning {@link Prototype}.
     * @param slotName  The slot's name.
     */
    ProtoSlot(final Prototype prototype, final String slotName)
    {

        if (prototype == null || slotName == null)
        {
            throw new PrototypeException("Neither the prototype nor the name may be null.", null, prototype, slotName);
        }

        _Prototype = prototype;
        _Name = slotName;
        _RealName = _Name;
    }

    @Override
    public final int compareTo(final ProtoSlot other)
    {

        return ProtoSlot.ALPHA_ORDER.compare(this, other);
    }

    @Override
    public final boolean equals(final Object obj)
    {
        // TODO: Replace with guava's Objects.equal() OR apache EqualsBuilder?
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ProtoSlot other = (ProtoSlot) obj;
        if (_Name == null)
        {
            if (other._Name != null)
            {
                return false;
            }
        }
        else if (!_Name.equals(other._Name))
        {
            return false;
        }
        if (_Prototype == null)
        {
            if (other._Prototype != null)
            {
                return false;
            }
        }
        else if (!_Prototype.equals(other._Prototype))
        {
            return false;
        }
        return true;
    }

    /**
     * The set of aliases, or alternative names, for this slot.
     *
     * @return The aliases, or alternative names, for this slot or <code>null</code> if this slot has no aliases.
     */
    public SortedSet<String> getAliases()
    {

        if (_Aliases == null)
        {

            final Aliases aliases = getAnnotation(Aliases.class);
            if (aliases != null)
            {
                final String[] aliasArray = aliases.value();

                if ((aliasArray != null) && (aliasArray.length > 0))
                {
                    _Aliases = new TreeSet<String>(Arrays.asList(aliasArray));
                }

                if (_Aliases != null && _Aliases.contains(getName()))
                {
                    // Not allowed to contain own name (aliases cannot have aliases).
                    _Aliases = null;
                }
            }
            else
            {
                _Aliases = null;
            }
        }

        return _Aliases;
    }

    /**
     * The {@link Context} for this {@link ProtoSlot}.
     *
     * @return The {@link Context} for this {@link ProtoSlot}.
     */
    public final Context getContext()
    {

        return getSchemaLoader().getContext();
    }

    /**
     * The {@link URI} of the base {@link org.wrml.model.schema.Schema} that actually declared this slot.
     *
     * @return The {@link URI} of the base {@link org.wrml.model.schema.Schema} that actually declared this slot.
     * @see #getSchemaUri()
     * @see org.wrml.model.schema.Schema#getBaseSchemaUris()
     */
    public abstract URI getDeclaringSchemaUri();

    /**
     * The description of the slot.
     *
     * @return The description of the slot or <code>null</code> if this slot has not be described.
     * @see Description
     */
    public final String getDescription()
    {
        // NOTE: This method relies on a subclass hook (thus the lazy init design).
        // TODO: Refactor to address lazy init design?
        if (_Description == null)
        {
            final Description description = getAnnotation(Description.class);
            _Description = (description != null) ? description.value() : null;
        }
        return _Description;
    }

    /**
     * The runtime {@link Type} associated with this slot.
     *
     * @return The runtime {@link Type} associated with this slot.
     * @see ValueType#getValueType(java.lang.reflect.Type)
     * @see org.wrml.model.schema.Value
     */
    public abstract Type getHeapValueType();

    /**
     * The name associated with the slot.
     *
     * @return The slot's name.
     */
    public final String getName()
    {

        return _Name;
    }

    /**
     * The {@link Prototype} that owns this slot.
     *
     * @return The {@link Prototype} that owns this slot.
     */
    public final Prototype getPrototype()
    {

        return _Prototype;
    }

    /**
     * The <i>real</i> name of this slot, which will only differ from the slot's {@link #getName()} value if this slot is an "alias" slot ({@link #isAlias()}).
     *
     * @return The <i>real</i> name of this slot, which will only differ from the slot's {@link #getName()} value if this slot is an "alias" slot ({@link #isAlias()}).
     * @see #getName()
     * @see #isAlias()
     */
    public String getRealName()
    {

        return _RealName;
    }

    /**
     * Framework internal method used to support alias slots.
     *
     * @param realName The real name of this alias slot.
     * @see #isAlias()
     * @see #getRealName()
     */
    void setRealName(final String realName)
    {

        _RealName = realName;
    }

    /**
     * The {@link URI} of the {@link org.wrml.model.schema.Schema} that contains this slot, which it may have inherited from a base schema.
     *
     * @return The {@link URI} of the {@link org.wrml.model.schema.Schema} that contains this slot, which it may have inherited from a base schema.
     * @see #getDeclaringSchemaUri()
     */
    public final URI getSchemaUri()
    {

        return getPrototype().getSchemaUri();
    }

    /**
     * The {@link SchemaLoader} responsible for this slot's {@link Prototype}.
     *
     * @return The {@link SchemaLoader} responsible for this slot's {@link Prototype}.
     */
    public final SchemaLoader getSchemaLoader()
    {

        return getPrototype().getSchemaLoader();
    }

    /**
     * The title (display name) of this slot.
     *
     * @return The title (display name) of this slot or <code>null</code> if this slot was not given a title.
     * @see Title
     */
    public final String getTitle()
    {

        if (_Title == null)
        {
            final Title title = getAnnotation(Title.class);
            _Title = (title != null) ? title.value() : getName();
        }
        return _Title;
    }

    /**
     * The {@link ValueType} associated with this slot.
     *
     * @return The {@link ValueType} associated with this slot.
     * @see #getHeapValueType()
     * @see ValueType#getValueType(java.lang.reflect.Type)
     */
    public final ValueType getValueType()
    {

        // NOTE: This method relies on a subclass hook (thus the lazy init design).
        // TODO: Refactor to address lazy init design?

        if (_ValueType == null)
        {
            final Type heapValueType = getHeapValueType();
            try
            {
                _ValueType = getSchemaLoader().getValueType(heapValueType);
            }
            catch (final Exception e)
            {

                throw new PrototypeException("Prototype: " + getPrototype()
                        + " encountered an error while attempting to determine the value type of the slot named \""
                        + getName() + "\". Detail message: " + e.getMessage(), e, getPrototype(), getName());
            }
        }

        return _ValueType;
    }

    @Override
    public final int hashCode()
    {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((_Name == null) ? 0 : _Name.hashCode());
        result = prime * result + ((_Prototype == null) ? 0 : _Prototype.hashCode());
        return result;
    }

    /**
     * The <i>alias</i> flag is <code>true</code> if this slot is actually an alias for another slot. Alias slots provide an alternative name for a slot but utilize the same value storage.
     *
     * @return The <i>alias</i> flag is <code>true</code> if this slot is actually an alias for another slot.
     * @see #getName()
     * @see #getRealName()
     */
    public boolean isAlias()
    {

        return _Alias;
    }

    /**
     * Framework internal method used to support alias slots.
     *
     * @param alias The value of the alias flag.
     * @see #isAlias()
     * @see #setRealName(String)
     */
    void setAlias(final boolean alias)
    {

        _Alias = alias;
    }

    @Override
    public final String toString()
    {

        return getClass().getName() + " [prototype = " + getPrototype() + ", name = " + getName() + ", valueType = "
                + getValueType() + ", heapValueType = " + getHeapValueType() + ", schemaUri = " + getSchemaUri()
                + ", declaringSchemaUri = " + getDeclaringSchemaUri() + "]";
    }

    /**
     * Given the specified type, returns the {@link Annotation} instance associated with this slot (if one exists).
     *
     * @param annotationClass The {@link Annotation} type.
     * @param <T>             The generic {@link Annotation} type token that enables safe usage without the caller needing to cast the result.
     * @return The {@link Annotation} instance.
     */
    protected final <T extends Annotation> T getAnnotation(final Class<T> annotationClass)
    {

        if (isAlias())
        {
            return getPrototype().getProtoSlot(getRealName()).getAnnotation(annotationClass);
        }

        return getAnnotationInternal(annotationClass);
    }

    /**
     * Hook method for subclasses to implement in support of {@link #getAnnotation(Class)}.
     *
     * @param annotationClass The {@link Annotation} type.
     * @param <T>             The generic {@link Annotation} type token that enables safe usage without the caller needing to cast the result.
     * @return The {@link Annotation} instance.
     * @see #getAnnotation(Class)
     */
    protected abstract <T extends Annotation> T getAnnotationInternal(final Class<T> annotationClass);

}

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

import org.wrml.model.rest.Method;
import org.wrml.model.schema.LinkValue;
import org.wrml.model.schema.Schema;
import org.wrml.model.schema.ValueSourceType;
import org.wrml.model.schema.ValueType;
import org.wrml.util.JavaMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * A "pre-compiled" link descriptor, generated from a {@link Schema} slot's {@link LinkValue}.
 * </p>
 * <p>
 * A {@link LinkProtoSlot} is used by the runtime to hold the detailed knowledge about a particular {@link org.wrml.model.schema.Slot}
 * with a {@link LinkValue}. This type of {@link ProtoSlot} is to describe a slot that has <i>hyperlink method</i> and is
 * similar (in concept) to the {@link java.beans.MethodDescriptor}.
 * </p>
 *
 * @see Prototype
 * @see ProtoSlot
 * @see org.wrml.model.rest.Link
 * @see LinkValue
 * @see org.wrml.model.schema.Schema
 * @see org.wrml.model.schema.Slot
 */
public final class LinkProtoSlot extends ProtoSlot
{

    private final JavaMethod _LinkMethod;

    private final boolean _Embedded;

    private final URI _ResponseSchemaUri;

    private final URI _RequestSchemaUri;

    private final URI _LinkRelationUri;

    private final Method _Method;

    private Map<String, ProtoValueSource> _LinkSlotBindings;

    LinkProtoSlot(final Prototype prototype, final String slotName, final JavaMethod linkJavaMethod)
    {

        super(prototype, slotName);
        _LinkMethod = linkJavaMethod;

        final LinkSlot linkSlot = getAnnotationInternal(LinkSlot.class);

        final String relationUriString = linkSlot.linkRelationUri();
        _LinkRelationUri = URI.create(relationUriString);
        _Embedded = linkSlot.embedded();
        _Method = linkSlot.method();


        if (_Embedded && _Method != Method.Get)
        {
            throw new PrototypeException("Embedded links are not compatible with the interaction method: "
                    + _Method, null, getPrototype(), slotName);
        }

        if (getAnnotationInternal(LinkSlotBinding.class) != null)
        {
            throw new PrototypeException("The " + LinkSlotBinding.class + " annotation is not allowed here. Alternatively, one or more of these annotations may be included in the " + LinkSlot.class + " annotation.", null, prototype);
        }


        final SchemaLoader schemaLoader = getSchemaLoader();
        final JavaMethod.Signature linkMethodSignature = _LinkMethod.getSignature();
        final Type returnType = linkMethodSignature.getReturnType();

        if (returnType != null && !Void.TYPE.equals(returnType))
        {
            _ResponseSchemaUri = schemaLoader.getTypeUri(returnType);
        }
        else
        {
            _ResponseSchemaUri = null;
        }

        Type modelParameterType = null;
        final Type[] parameters = linkMethodSignature.getParameterTypes();
        if (parameters != null && parameters.length == 1)
        {
            final Type parameterType = parameters[0];
            if (ValueType.isModelType(parameterType))
            {
                modelParameterType = parameterType;
            }
        }

        if (modelParameterType != null)
        {
            _RequestSchemaUri = schemaLoader.getTypeUri(modelParameterType);
        }
        else
        {
            _RequestSchemaUri = null;
        }


    }

    @Override
    public URI getDeclaringSchemaUri()
    {

        final Class<?> declaringSchemaInterface = getJavaMethod().getMethod().getDeclaringClass();
        final URI declaringSchemaUri = getSchemaLoader().getTypeUri(declaringSchemaInterface);
        return declaringSchemaUri;
    }

    @Override
    public Type getHeapValueType()
    {

        return ValueType.JAVA_TYPE_LINK;
    }

    /**
     * The {@link JavaMethod} associated with this link.
     *
     * @return The {@link JavaMethod} associated with this link.
     */
    public JavaMethod getJavaMethod()
    {

        return _LinkMethod;
    }

    /**
     * A mapping of referenced slot name (slot name within this link's referenced/response schema) to the source of the value for the referenced slot name.
     *
     * @return A mapping of referenced slot name (slot name within this link's referenced/response schema) to the source of the value for the referenced slot name.
     */
    public Map<String, ProtoValueSource> getLinkSlotBindings()
    {

        // This is lazily initialized to avoid infinite cycles since this method calls schemaLoader.getPrototype(...)
        if (_LinkSlotBindings == null)
        {
            // Initialize the optional link slot bindings
            _LinkSlotBindings = new LinkedHashMap<>();

            final Prototype prototype = getPrototype();
            final LinkSlot linkSlot = getAnnotationInternal(LinkSlot.class);

            final LinkSlotBinding[] linkSlotBindings = linkSlot.bindings();
            if (linkSlotBindings != null && linkSlotBindings.length > 0)
            {
                final Prototype referencePrototype;


                final Type referenceType = _LinkMethod.getMethod().getReturnType();
                if (referenceType != null && referenceType != Void.TYPE)
                {

                    final SchemaLoader schemaLoader = prototype.getSchemaLoader();
                    final URI referenceSchemaUri = schemaLoader.getTypeUri(referenceType);
                    if (referenceSchemaUri.equals(prototype.getSchemaUri()))
                    {
                        referencePrototype = prototype;
                    }
                    else
                    {
                        referencePrototype = schemaLoader.getPrototype(referenceSchemaUri);
                    }
                }
                else
                {
                    referencePrototype = null;
                }


                for (final LinkSlotBinding linkSlotBinding : linkSlotBindings)
                {
                    final String referenceSlot = linkSlotBinding.referenceSlot();
                    final String valueSource = linkSlotBinding.valueSource();
                    final ValueSourceType valueSourceType = linkSlotBinding.valueSourceType();

                    final ProtoValueSource protoValueSource = new ProtoValueSource(referencePrototype, referenceSlot, prototype, valueSource, valueSourceType);
                    _LinkSlotBindings.put(referenceSlot, protoValueSource);
                }
            }
        }


        return _LinkSlotBindings;
    }

    /**
     * A flag that is set to <code>true</code> if this link's referenced/response document is embedded within the link by default.
     *
     * @return The value of the <code>embedded</code> flag, which is set to <code>true</code> if this link's referenced/response document is embedded within the link by default.
     */
    public boolean isEmbedded()
    {

        return _Embedded;
    }

    public URI getLinkRelationUri()
    {

        return _LinkRelationUri;
    }

    public URI getRequestSchemaUri()
    {

        return _RequestSchemaUri;
    }

    public URI getResponseSchemaUri()
    {

        return _ResponseSchemaUri;
    }

    public Method getMethod()
    {

        return _Method;
    }

    @Override
    protected <T extends Annotation> T getAnnotationInternal(final Class<T> annotationClass)
    {

        return getJavaMethod().getMethod().getAnnotation(annotationClass);
    }


}

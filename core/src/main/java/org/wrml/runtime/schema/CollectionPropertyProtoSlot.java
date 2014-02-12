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

import org.wrml.model.schema.ValueType;
import org.wrml.util.JavaBean;

import java.lang.reflect.Type;
import java.net.URI;

/**
 * <p>
 * The runtime representation of {@link CollectionSlot}, which is a {@link org.wrml.model.schema.ValueType#List} ({@link java.util.List}) slot
 * with elements that derive from {@link org.wrml.model.Model} and are <i>dynamically</i> added to the slot based upon
 * the results of a <i>search</i> that is performed upon request of the slot's containing {@link org.wrml.model.Model},
 * which is known as the "referrer".
 * </p>
 *
 * @see CollectionSlot
 * @see CollectionSlotCriterion
 * @see org.wrml.runtime.service.Service#search(org.wrml.runtime.search.SearchCriteria)
 */
public final class CollectionPropertyProtoSlot extends PropertyProtoSlot {

    private final URI _LinkRelationUri;

    private final Integer _Limit;

    private ProtoSearchCriteria _ProtoSearchCriteria;

    CollectionPropertyProtoSlot(final Prototype prototype, final String slotName, final JavaBean.Property property) {

        super(prototype, slotName, property);


        final CollectionSlot collectionSlot = getCollectionSlot();
        if (collectionSlot == null) {
            throw new IllegalArgumentException("The prototype: " + prototype.getUniqueName() + " Collection slot: " + slotName + " is missing the " + CollectionSlot.class + " annotation.");
        }

        if (getValueType() != ValueType.List) {
            throw new IllegalArgumentException("The prototype: " + prototype.getUniqueName() + " Collection slot: " + slotName + " is not a " + ValueType.List + " slot.");
        }

        final Type listElementType = getListElementType();
        final ValueType listElementValueType = ValueType.getValueType(listElementType);
        if (listElementValueType != ValueType.Model) {
            throw new IllegalArgumentException("The prototype: " + prototype.getUniqueName() + " Collection slot: " + slotName + " is not a " + ValueType.List + " of " + ValueType.Model + " slot.");
        }

        final String linkRelationUriString = collectionSlot.linkRelationUri();
        if (linkRelationUriString != null && !linkRelationUriString.isEmpty()) {
            _LinkRelationUri = URI.create(linkRelationUriString);
        }
        else {
            _LinkRelationUri = null;
        }

        _Limit = collectionSlot.limit();


        if (getAnnotationInternal(LinkSlotBinding.class) != null) {
            throw new PrototypeException("The " + CollectionSlotCriterion.class + " annotation is not allowed here. Alternatively, one or more of these annotations may be included in the " + CollectionSlot.class + " annotation.", null, prototype);
        }


    }

    /**
     * <p>
     * The optional "search result" limit, which may be used to restrict the number of elements that will fill in this slot.
     * </p>
     * <p>
     * The default value is <code>null</code>, which indicates no limit.
     * </p>
     *
     * @return The optional "search result" limit, which may be used to restrict the number of elements that will fill in this slot.
     */
    public Integer getLimit() {

        return _Limit;
    }

    /**
     * <p>
     * The optional {@link org.wrml.model.rest.LinkRelation} {@link URI} that identifies the relationship between the
     * referrer {@link org.wrml.model.rest.Document}, which contains this collection slot, and the {@link org.wrml.model.rest.Document}s that
     * are contained within this collection slot, which are known as this collection's "referenced" documents.
     * </p>
     * <p>
     * This value is considered optional since it is not necessarily the case that this collection slot's owner
     * (the referrer) or its contained (referenced) elements are {@link org.wrml.model.rest.Document} instances.
     * It is possible for a non-{@link org.wrml.model.rest.Document} model to have one or more collection slots and
     * it is also possible for a collection slot to contain non-{@link org.wrml.model.rest.Document} elements. In such cases
     * there is no need for a {@link org.wrml.model.rest.LinkRelation} {@link URI} since there are no links href's or uri
     * values for the framework's runtime to help manage.
     * </p>
     *
     * @return The optional {@link org.wrml.model.rest.LinkRelation} {@link URI} that identifies the relationship between referrer and
     * the referenced elements within this slot.
     */
    public URI getLinkRelationUri() {

        return _LinkRelationUri;
    }

    /**
     * The runtime's prototype for the {@link org.wrml.runtime.search.SearchCriteria} associated with this {@link CollectionPropertyProtoSlot}.
     *
     * @return The runtime's prototype for the {@link org.wrml.runtime.search.SearchCriteria} associated with this {@link CollectionPropertyProtoSlot}.
     */
    public ProtoSearchCriteria getProtoSearchCriteria() {

        if (_ProtoSearchCriteria == null) {

            final Prototype referrerPrototype = getPrototype();
            final SchemaLoader schemaLoader = referrerPrototype.getSchemaLoader();
            final Type referenceType = getListElementType();
            final URI referenceSchemaUri = schemaLoader.getTypeUri(referenceType);

            final Prototype referencePrototype;
            if (referenceSchemaUri.equals(referrerPrototype.getSchemaUri())) {
                referencePrototype = referrerPrototype;
            }
            else {
                referencePrototype = schemaLoader.getPrototype(referenceSchemaUri);
            }


            _ProtoSearchCriteria = new ProtoSearchCriteria(this, referencePrototype, referrerPrototype);
        }

        return _ProtoSearchCriteria;
    }

    /**
     * The {@link CollectionSlot} annotation associated with this {@link CollectionPropertyProtoSlot}.
     *
     * @return The {@link CollectionSlot} annotation associated with this {@link CollectionPropertyProtoSlot}.
     */
    public CollectionSlot getCollectionSlot() {

        return getAnnotationInternal(CollectionSlot.class);
    }


}

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

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.rest.AggregateDocument;
import org.wrml.model.rest.Document;
import org.wrml.model.schema.Schema;
import org.wrml.util.JavaBean;
import org.wrml.util.JavaBean.Property;
import org.wrml.util.JavaMethod;
import org.wrml.util.UniqueName;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * <p>
 * A runtime representation/descriptor of a {@link Schema}.
 * <p>
 * {@link Prototype} is designed to optimize reflection.
 * <p>
 * The implementation uses {@link Map}s to store the results of a {@link Prototype}'s construction-time reflection of its associated schema Java class. In other words, WRML's
 * runtime reflects upon each schema (Java interface) only once and quickly recalls the structure's details using a {@link Prototype}.
 */
public class Prototype
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Prototype.class);

    /**
     * The uri of every schema from which this prototype inherits are belong to us.
     */
    private final Set<URI> _AllBaseSchemaUris;

    /**
     * The names of the slots that this prototype's schema declared along with the names of any slot names gained from its base schemas.
     */
    private final SortedSet<String> _AllSlotNames;

    private final Set<URI> _BaseSchemaUris;

    private final String _Description;

    private final boolean _ReadOnly;

    /**
     * The reflection generated metadata associated with the Java class representation of the schema.
     */
    private final JavaBean _SchemaBean;

    /**
     * The uri of the schema that is represented by this {@link Prototype} at runtime.
     */
    private final URI _SchemaUri;

    /**
     * The parent/owning SchemaLoader for this Prototype instance.
     */
    private final SchemaLoader _SchemaLoader;

    private final URI _ThumbnailLocation;

    private final String _Title;

    private final UniqueName _UniqueName;

    /**
     * Map of slot name to prototype slot metadata.
     */
    private final SortedMap<String, ProtoSlot> _ProtoSlots;

    /**
     * A mapping of link slot name ({@link String}) to link relation uri ( {@link URI}).
     */
    private final SortedMap<String, URI> _LinkRelationUris;

    /**
     * A mapping of link relation uri ( {@link URI}) to link slot.
     */
    private final SortedMap<URI, LinkProtoSlot> _LinkProtoSlots;

    private final SortedMap<String, CollectionPropertyProtoSlot> _CollectionPropertyProtoSlots;

    private boolean _IsAbstract;

    private Set<Prototype> _BasePrototypes;

    private Set<Prototype> _AllBasePrototypes;

    private Set<String> _AllKeySlotNames;

    private Set<String> _ComparableSlotNames;

    /**
     * The names of the slots that this prototype's schema declared as it's keyslots (to determine uniqueness of models conforming to this schema).
     * <p/>
     * For simple keys, this is a list of a single slot's name. For composite/compound keys, this list contains two or more slot names that, when combined together, hold the
     * Schema's unique key value.
     */
    private SortedSet<String> _KeySlotNames;

    /**
     * The Java type of this Prototype's (Schema's) key slot. Note that this is a composition type if more than one key slot name is declared by the Schema.
     */
    private Type _KeyType;

    private SortedMap<String, String> _SlotAliases;

    private SortedSet<String> _SearchableSlots;

    private String _TitleSlotName;

    private SortedSet<String> _Tags;

    private long _Version;

    private boolean _IsDocument;

    private boolean _IsAggregate;

    private boolean _ContainsEmbeddedLink;

    /**
     * Creates a new Prototype to represent the identified schema.
     * 
     * @param schemaLoader
     *            The schema loader for this prototype's schema.
     * @param schemaUri
     *            The schema identifier.
     * @throws PrototypeException
     *             Thrown if there are problems with the initial prototyping of the schema.
     */
    Prototype(final SchemaLoader schemaLoader, final URI schemaUri) throws PrototypeException
    {

        LOGGER.debug("Creating Prototype for schema ID: {}", new Object[] {schemaUri});

        _SchemaLoader = schemaLoader;
        if (_SchemaLoader == null)
        {

            throw new PrototypeException("The SchemaLoader parameter value cannot be *null*.", null, this);
        }

        _SchemaUri = schemaUri;
        if (_SchemaUri == null)
        {
            throw new PrototypeException("The undefined (aka *null*) schema can not be prototyped.", null, this);
        }

        if (schemaUri.equals(schemaLoader.getResourceTemplateSchemaUri()))
        {
            LOGGER.debug("Creating Prototype for ResourceTemplate");
        }

        _UniqueName = new UniqueName(_SchemaUri);

        //
        // Use the SchemaLoader and the schema uri to get the schema's Java Class
        // representation.
        //
        final Class<?> schemaInterface = getSchemaInterface();

        if (ValueType.JAVA_TYPE_ABSTRACT.equals(schemaInterface))
        {
            _IsAbstract = true;
        }
        else if (Document.class.equals(schemaInterface))
        {
            _IsDocument = true;
        }

        //
        // Introspect the associated class, extracting metadata from the parent
        // schema's Java interfaces (up to but not including the Model
        // interface).
        //

        _SchemaBean = new JavaBean(schemaInterface, ValueType.JAVA_TYPE_MODEL, LinkSlot.class);
        _AllBaseSchemaUris = new LinkedHashSet<>();
        _BaseSchemaUris = new LinkedHashSet<>();
        _AllSlotNames = new TreeSet<>();
        _ProtoSlots = new TreeMap<>();
        _CollectionPropertyProtoSlots = new TreeMap<>();
        _LinkRelationUris = new TreeMap<>();

        _LinkProtoSlots = new TreeMap<>();
        _SlotAliases = new TreeMap<>();
        _SearchableSlots = new TreeSet<>();

        // initBaseSchemas(...)
        {

            //
            // Use Java reflection to get all implemented interfaces and then turn
            // them into schema ids. With reflection we get de-duplication and
            // recursive traversal for free.
            //

            final List<Class<?>> allBaseInterfaces = ClassUtils.getAllInterfaces(schemaInterface);
            // Loop backwards to achieve desired key mapping precedence/overriding
            for (final Class<?> baseInterface : allBaseInterfaces)
            {

                if (ValueType.isSchemaInterface(baseInterface) && (baseInterface != ValueType.JAVA_TYPE_MODEL))
                {

                    final URI baseSchemaUri = _SchemaLoader.getTypeUri(baseInterface);
                    _AllBaseSchemaUris.add(baseSchemaUri);

                    if (Document.class.equals(baseInterface))
                    {
                        _IsDocument = true;
                    }

                    if (AggregateDocument.class.equals(baseInterface))
                    {
                        _IsAggregate = true;
                    }

                }

            }

            // Store the immediate base schemas as well

            final Class<?>[] baseInterfaces = schemaInterface.getInterfaces();
            if (baseInterfaces != null)
            {

                for (final Class<?> baseInterface : baseInterfaces)
                {
                    if (ValueType.isSchemaInterface(baseInterface) && (baseInterface != ValueType.JAVA_TYPE_MODEL))
                    {
                        final URI baseSchemaUri = _SchemaLoader.getTypeUri(baseInterface);
                        _BaseSchemaUris.add(baseSchemaUri);
                    }

                    if (ValueType.JAVA_TYPE_ABSTRACT.equals(baseInterface))
                    {
                        _IsAbstract = true;
                    }
                }

            }

        } // End of base schema init

        // initKeys(...)
        {
            final WRML wrml = schemaInterface.getAnnotation(WRML.class);
            if (wrml != null)
            {
                final String[] keySlotNameArray = wrml.keySlotNames();

                if ((keySlotNameArray != null) && (keySlotNameArray.length > 0))
                {

                    _KeySlotNames = new TreeSet<>(Arrays.asList(keySlotNameArray));

                    if (_KeySlotNames.size() == 1)
                    {
                        final String keySlotName = _KeySlotNames.first();
                        final Property property = _SchemaBean.getProperties().get(keySlotName);
                        if (property != null)
                        {
                            _KeyType = property.getType();
                        }
                        else
                        {
                            throw new PrototypeException("The named key slot, \"" + keySlotName + "\", is not defined for Schema: " + schemaUri + ".", null, this);
                        }
                    }
                    else
                    {

                        // Schemas with Keys that use more than one slot value to
                        // determine uniqueness use the CompositeKey type (at
                        // runtime) as their key object.
                        //
                        _KeyType = ValueType.JAVA_TYPE_COMPOSITE_KEY;
                    }

                }

                final String[] comparableSlotNameArray = wrml.comparableSlotNames();

                if ((comparableSlotNameArray != null) && (comparableSlotNameArray.length > 0))
                {

                    _ComparableSlotNames = new LinkedHashSet<String>(Arrays.asList(comparableSlotNameArray));
                }

                final String titleSlotName = wrml.titleSlotName();
                if (StringUtils.isNotBlank(titleSlotName)) {
                    _TitleSlotName = titleSlotName;
                }

            }

        } // End of the key initialization

        // initMiscAnnotations(...)
        {

            final Description schemaDescription = schemaInterface.getAnnotation(Description.class);
            if (schemaDescription != null)
            {
                _Description = schemaDescription.value();
            }
            else
            {
                _Description = null;
            }

            final Title schemaTitle = schemaInterface.getAnnotation(Title.class);
            if (schemaTitle != null)
            {
                _Title = schemaTitle.value();
            }
            else
            {
                _Title = schemaInterface.getSimpleName();
            }

            final ThumbnailImage thumbnailImage = schemaInterface.getAnnotation(ThumbnailImage.class);
            if (thumbnailImage != null)
            {
                _ThumbnailLocation = URI.create(thumbnailImage.value());
            }
            else
            {
                _ThumbnailLocation = null;
            }

            _ReadOnly = (schemaInterface.getAnnotation(ReadOnly.class) != null) ? true : false;

            final Version schemaVersion = schemaInterface.getAnnotation(Version.class);
            if (schemaVersion != null)
            {
                _Version = schemaVersion.value();
            }
            else
            {
                // TODO: Look for the "static final long serialVersionUID" ?
                _Version = 1L;
            }

            final Tags tags = schemaInterface.getAnnotation(Tags.class);
            if (tags != null)
            {
                final String[] tagArray = tags.value();

                if ((tagArray != null) && (tagArray.length > 0))
                {

                    _Tags = new TreeSet<String>(Arrays.asList(tagArray));
                }
            }

        } // End of annotation-based initialization

        // initPropertySlots(...)
        {
            final Map<String, Property> properties = _SchemaBean.getProperties();

            for (final String slotName : properties.keySet())
            {
                final Property property = properties.get(slotName);

                final PropertyProtoSlot propertyProtoSlot;

                final CollectionSlot collectionSlot = property.getAnnotation(CollectionSlot.class);
                if (collectionSlot != null)
                {
                    propertyProtoSlot = new CollectionPropertyProtoSlot(this, slotName, property);
                }
                else
                {
                    propertyProtoSlot = new PropertyProtoSlot(this, slotName, property);
                }

                addProtoSlot(propertyProtoSlot);
            }
        }

        // initLinkSlots(...)
        {

            //
            // Map the the schema bean's "other" (non-Property) methods.
            //

            final SortedMap<String, SortedSet<JavaMethod>> otherMethods = _SchemaBean.getOtherMethods();
            final Set<String> otherMethodNames = otherMethods.keySet();

            for (final String methodName : otherMethodNames)
            {

                final SortedSet<JavaMethod> methodSet = otherMethods.get(methodName);
                if (methodSet.size() != 1)
                {
                    throw new PrototypeException("The link method: " + methodName + " cannot be overloaded.", this);
                }

                final JavaMethod javaMethod = methodSet.first();
                final Method method = javaMethod.getMethod();

                final LinkSlot linkSlot = method.getAnnotation(LinkSlot.class);
                if (linkSlot == null)
                {
                    throw new PrototypeException("The method: " + javaMethod + " is not a link method", null, this);
                }

                final String relationUriString = linkSlot.linkRelationUri();
                final URI linkRelationUri = URI.create(relationUriString);

                if (_LinkProtoSlots.containsKey(linkRelationUri))
                {
                    throw new PrototypeException("A schema cannot use the same link relation for more than one method. Duplicate link relation: " + linkRelationUri
                            + " found in link method: " + javaMethod, this);
                }

                final org.wrml.model.rest.Method relMethod = linkSlot.method();

                String slotName = methodName;
                if (relMethod == org.wrml.model.rest.Method.Get && slotName.startsWith(JavaBean.GET))
                {
                    slotName = slotName.substring(3);
                    slotName = Character.toLowerCase(slotName.charAt(0)) + slotName.substring(1);
                }
                _LinkRelationUris.put(slotName, linkRelationUri);

                if (_ProtoSlots.containsKey(slotName))
                {
                    throw new PrototypeException("A schema cannot use the same name for more than one slot. Duplicate slot name: " + slotName + " found in link method: "
                            + javaMethod, this);
                }

                final LinkProtoSlot linkProtoSlot = new LinkProtoSlot(this, slotName, javaMethod);

                if ((linkProtoSlot.isEmbedded() || isAggregate()) && (relMethod == org.wrml.model.rest.Method.Get))
                {
                    _ContainsEmbeddedLink = true;
                }

                _LinkProtoSlots.put(linkRelationUri, linkProtoSlot);

                addProtoSlot(linkProtoSlot);

            }

        } // End of link slot init

        if (!_SlotAliases.isEmpty())
        {
            for (final String alias : _SlotAliases.keySet())
            {
                final ProtoSlot protoSlot = _ProtoSlots.get(alias);
                protoSlot.setAlias(true);
                final String realName = _SlotAliases.get(alias);
                protoSlot.setRealName(realName);

            }
        }

    }

    public boolean containsEmbeddedLink()
    {

        return _ContainsEmbeddedLink;
    }

    public Set<String> getSlotAliases()
    {

        return _SlotAliases.keySet();
    }

    public Set<Prototype> getAllBasePrototypes()
    {

        if (_AllBasePrototypes == null)
        {
            final SchemaLoader schemaLoader = getSchemaLoader();
            _AllBasePrototypes = new LinkedHashSet<Prototype>();
            final Set<URI> allBaseSchemaUris = getAllBaseSchemaUris();
            if (allBaseSchemaUris != null && !allBaseSchemaUris.isEmpty())
            {
                for (final URI baseSchemaUri : allBaseSchemaUris)
                {
                    final Prototype basePrototype = schemaLoader.getPrototype(baseSchemaUri);
                    _AllBasePrototypes.add(basePrototype);
                }
            }
        }

        return _AllBasePrototypes;
    }

    public Set<URI> getAllBaseSchemaUris()
    {

        return _AllBaseSchemaUris;
    }

    public Set<String> getAllKeySlotNames()
    {

        if (_AllKeySlotNames == null)
        {
            _AllKeySlotNames = new LinkedHashSet<String>();
            final SortedSet<String> declaredKeySlotNames = getDeclaredKeySlotNames();
            if (declaredKeySlotNames != null)
            {
                _AllKeySlotNames.addAll(declaredKeySlotNames);
            }

            final Set<Prototype> allYourBase = getAllBasePrototypes();
            for (final Prototype base : allYourBase)
            {
                final SortedSet<String> baseDeclaredKeySlotNames = base.getDeclaredKeySlotNames();
                if (baseDeclaredKeySlotNames != null)
                {
                    _AllKeySlotNames.addAll(baseDeclaredKeySlotNames);
                }
            }
        }

        return _AllKeySlotNames;
    }

    public Set<URI> getAllRelatedSchemaUris()
    {

        final LinkedHashSet<URI> allRelatedSchemaUris = new LinkedHashSet<>(getAllBaseSchemaUris());

        // TODO: Add schemas in model slots and link signatures (as request or response entity).

        return allRelatedSchemaUris;
    }

    public SortedSet<String> getAllSlotNames()
    {

        return _AllSlotNames;
    }

    public Set<String> getComparableSlotNames()
    {

        return _ComparableSlotNames;
    }

    public Set<Prototype> getDeclaredBasePrototypes()
    {

        if (_BasePrototypes == null)
        {
            final SchemaLoader schemaLoader = getSchemaLoader();
            _BasePrototypes = new LinkedHashSet<Prototype>();
            final Set<URI> baseSchemaUris = getDeclaredBaseSchemaUris();
            if (baseSchemaUris != null && !baseSchemaUris.isEmpty())
            {
                for (final URI baseSchemaUri : baseSchemaUris)
                {
                    final Prototype basePrototype = schemaLoader.getPrototype(baseSchemaUri);
                    _BasePrototypes.add(basePrototype);
                }
            }
        }

        return _BasePrototypes;
    }

    public Set<URI> getDeclaredBaseSchemaUris()
    {

        return _BaseSchemaUris;
    }

    /**
     * A {@link SortedSet} of the names of the key {@link org.wrml.model.schema.Slot}s declared by this {@link Prototype}'s {@link Schema}.
     */
    public SortedSet<String> getDeclaredKeySlotNames()
    {

        return _KeySlotNames;
    }

    public String getDescription()
    {

        return _Description;
    }


    public java.lang.reflect.Type getKeyType()
    {

        return _KeyType;
    }

    public SortedMap<URI, LinkProtoSlot> getLinkProtoSlots()
    {

        return _LinkProtoSlots;
    }

    public Map<String, CollectionPropertyProtoSlot> getCollectionPropertyProtoSlots()
    {

        return _CollectionPropertyProtoSlots;
    }

    public URI getLinkRelationUri(final String linkSlotName)
    {

        return _LinkRelationUris.get(linkSlotName);
    }

    public SortedMap<String, URI> getLinkRelationUris()
    {

        return _LinkRelationUris;
    }

    /**
     * A {@link SortedSet} of the names of the {@link Searchable} {@link org.wrml.model.schema.Slot}s declared by this {@link Prototype}'s {@link Schema}.
     */
    public SortedSet<String> getSearchableSlots()
    {

        return _SearchableSlots;
    }

    public <T extends ProtoSlot> T getProtoSlot(final String slotName)
    {

        return getProtoSlot(slotName, true);
    }

    @SuppressWarnings("unchecked")
    public <T extends ProtoSlot> T getProtoSlot(final String slotName, final boolean strictMode)
    {

        if (!_ProtoSlots.containsKey(slotName))
        {
            if (strictMode)
            {
                final String error = "A (WRML) slot named \"" + slotName + "\" was not found within this prototype's schema interface (" + _SchemaBean.getIntrospectedClass() + ")";
                LOGGER.error(error);
                throw new PrototypeException(error, null, this);
            }
            else
            {
                return null;
            }
        }

        return (T) _ProtoSlots.get(slotName);
    }

    public String getRealSlotName(final String possibleAlias)
    {

        if (_SlotAliases.containsKey(possibleAlias))
        {
            return _SlotAliases.get(possibleAlias);
        }

        return null;
    }

    public JavaBean getSchemaBean()
    {

        return _SchemaBean;
    }

    public SchemaLoader getSchemaLoader()
    {

        return _SchemaLoader;
    }

    public URI getSchemaUri()
    {

        return _SchemaUri;
    }

    public SortedSet<String> getTags()
    {

        return _Tags;
    }

    public URI getThumbnailLocation()
    {

        return _ThumbnailLocation;
    }

    public String getTitle()
    {

        return _Title;
    }

    public String getTitleSlotName()
    {
        return _TitleSlotName;
    }

    public UniqueName getUniqueName()
    {

        return _UniqueName;
    }

    public Long getVersion()
    {

        return _Version;
    }

    public boolean isAbstract()
    {

        return _IsAbstract;
    }

    public boolean isAggregate()
    {

        return _IsAggregate;
    }

    public boolean isAssignableFrom(final URI schemaUri)
    {

        try
        {
            return getSchemaInterface().isAssignableFrom(getSchemaLoader().getSchemaInterface(schemaUri));
        }
        catch (final PrototypeException | ClassNotFoundException e)
        {
            return false;
        }
    }

    public boolean isDocument()
    {

        return _IsDocument;
    }

    public boolean isKeySlot(final String slotName)
    {

        return getAllKeySlotNames().contains(slotName);
    }

    public boolean isReadOnly()
    {

        return _ReadOnly;
    }

    @Override
    public String toString()
    {

        return "Prototype [schemaUri = " + _SchemaUri + ", version = " + _Version + ", description = " + _Description + "]";
    }

    private void addProtoSlot(final ProtoSlot protoSlot)
    {

        final String slotName = protoSlot.getName();
        _ProtoSlots.put(slotName, protoSlot);
        _AllSlotNames.add(slotName);

        final SortedSet<String> aliases = protoSlot.getAliases();
        if (aliases != null && aliases.size() > 0)
        {
            for (final String alias : aliases)
            {
                _SlotAliases.put(alias, slotName);
            }
        }

        if (protoSlot instanceof CollectionPropertyProtoSlot)
        {

            final CollectionPropertyProtoSlot collectionPropertyProtoSlot = (CollectionPropertyProtoSlot) protoSlot;

            _CollectionPropertyProtoSlots.put(slotName, collectionPropertyProtoSlot);
        }
        else if (protoSlot instanceof PropertyProtoSlot)
        {
            final PropertyProtoSlot propertyProtoSlot = (PropertyProtoSlot) protoSlot;

            final boolean isSearchable = propertyProtoSlot.isSearchable();

            if (isSearchable)
            {
                _SearchableSlots.add(propertyProtoSlot.getName());
            }
        }

    }

    private Class<?> getSchemaInterface() throws PrototypeException
    {

        Class<?> schemaInterface = null;
        try
        {
            schemaInterface = _SchemaLoader.getSchemaInterface(_SchemaUri);
            if (schemaInterface == null)
            {
                throw new PrototypeException("SchemaLoader returned a null Schema for: " + _SchemaUri, this);
            }
        }
        catch (final Exception t)
        {
            throw new PrototypeException("Interface not found for schema: " + String.valueOf(_SchemaUri), t, this);
        }

        return schemaInterface;
    }

}

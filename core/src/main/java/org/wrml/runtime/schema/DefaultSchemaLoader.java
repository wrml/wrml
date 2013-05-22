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
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.Virtual;
import org.wrml.model.format.Format;
import org.wrml.model.rest.*;
import org.wrml.model.schema.*;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.DimensionsBuilder;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.application.schema.json.JsonSchema;
import org.wrml.runtime.format.application.schema.json.JsonSchemaLoader;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.runtime.rest.Resource;
import org.wrml.runtime.rest.SystemApi;
import org.wrml.runtime.schema.generator.JavaBytecodeClass;
import org.wrml.runtime.schema.generator.SchemaGenerator;
import org.wrml.runtime.syntax.SyntaxHandler;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.util.UniqueName;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * The WRML runtime's default implementation of the SchemaLoader.
 * </p>
 */
public class DefaultSchemaLoader extends ClassLoader implements SchemaLoader
{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSchemaLoader.class);

    private static final String[] JSON_SCHEMA_FILE_EXTENSIONS = new String[]{"json"};

    private static final Class<?>[] JAVA_TYPE_WRML_BASES = new Class<?>[]{ValueType.JAVA_TYPE_DOCUMENT, ValueType.JAVA_TYPE_MODEL, ValueType.JAVA_TYPE_ABSTRACT};

    private final Dimensions _ApiDimensions;

    private final URI _AggregateDocumentSchemaUri;

    private final URI _VirtualSchemaUri;

    private final URI _ApiSchemaUri;

    private final BiMap<String, URI> _NativeTypeNameToUriBiMap;

    private final URI _DocumentSchemaUri;

    private final URI _EmbeddedSchemaUri;

    private final URI _LinkRelationSchemaUri;

    private final URI _LinkSchemaUri;

    private final URI _SchemaSchemaUri;

    private final URI _SchemaNamespaceSchemaUri;

    private final URI _SyntaxSchemaUri;

    private final ConcurrentHashMap<URI, Prototype> _Prototypes;

    private final URI _ResourceTemplateSchemaUri;

    private final Dimensions _SchemaDimensions;

    private final Dimensions _SchemaNamespaceDimensions;

    private final ConcurrentHashMap<URI, Class<?>> _SchemaInterfaces;

    private final Dimensions _LinkRelationDimensions;

    private final Dimensions _FormatDimensions;

    private final URI _FormatSchemaUri;

    private final ConcurrentHashMap<URI, Schema> _NativeSchemas;

    private final ConcurrentHashMap<URI, Schema> _Schemas;

    private final HashSet<URI> _SystemSchemaUris;

    private final URI _ChoicesSchemaUri;

    private final Dimensions _ChoicesDimensions;

    private final ConcurrentHashMap<URI, Choices> _NativeChoices;

    private final ConcurrentHashMap<URI, Choices> _Choices;

    private final ConcurrentHashMap<URI, Class<?>> _ChoicesEnumClasses;

    private Context _Context;

    private transient SchemaGenerator _SchemaGenerator;

    private transient JsonSchemaLoader _JsonSchemaLoader;


    public DefaultSchemaLoader()
    {

        this(DefaultSchemaLoader.class.getClassLoader());
    }

    public DefaultSchemaLoader(final ClassLoader parent)
    {

        super(parent);

        _NativeTypeNameToUriBiMap = HashBiMap.create();
        _SystemSchemaUris = new LinkedHashSet<>();
        _Schemas = new ConcurrentHashMap<>();
        _NativeSchemas = new ConcurrentHashMap<>();
        _SchemaInterfaces = new ConcurrentHashMap<>();
        _Prototypes = new ConcurrentHashMap<>();
        _Choices = new ConcurrentHashMap<>();
        _NativeChoices = new ConcurrentHashMap<>();
        _ChoicesEnumClasses = new ConcurrentHashMap<>();

        // System loaded schemas
        _ApiSchemaUri = getTypeUri(Api.class);
        _SystemSchemaUris.add(_ApiSchemaUri);
        _ApiDimensions = new DimensionsBuilder(_ApiSchemaUri).toDimensions();

        _SchemaSchemaUri = getTypeUri(Schema.class);
        _SystemSchemaUris.add(_SchemaSchemaUri);
        _SchemaDimensions = new DimensionsBuilder(_SchemaSchemaUri).toDimensions();

        _SchemaNamespaceSchemaUri = getTypeUri(SchemaNamespace.class);
        _SystemSchemaUris.add(_SchemaNamespaceSchemaUri);
        _SchemaNamespaceDimensions = new DimensionsBuilder(_SchemaNamespaceSchemaUri).toDimensions();

        _SyntaxSchemaUri = getTypeUri(Syntax.class);
        _SystemSchemaUris.add(_SyntaxSchemaUri);

        _ChoicesSchemaUri = getTypeUri(Choices.class);
        _SystemSchemaUris.add(_ChoicesSchemaUri);
        _ChoicesDimensions = new DimensionsBuilder(_ChoicesSchemaUri).toDimensions();

        _LinkRelationSchemaUri = getTypeUri(LinkRelation.class);
        _SystemSchemaUris.add(_LinkRelationSchemaUri);
        _LinkRelationDimensions = new DimensionsBuilder(_LinkRelationSchemaUri).toDimensions();

        _FormatSchemaUri = getTypeUri(Format.class);
        _SystemSchemaUris.add(_FormatSchemaUri);
        _FormatDimensions = new DimensionsBuilder(_FormatSchemaUri).toDimensions();

        // Other system-referenced schemas
        _DocumentSchemaUri = getTypeUri(Document.class);
        _AggregateDocumentSchemaUri = getTypeUri(AggregateDocument.class);
        _EmbeddedSchemaUri = getTypeUri(Embedded.class);
        _LinkSchemaUri = getTypeUri(Link.class);
        _ResourceTemplateSchemaUri = getTypeUri(ResourceTemplate.class);
        _VirtualSchemaUri = getTypeUri(Virtual.class);

    }

    @Override
    public URI getAggregateDocumentSchemaUri()
    {

        return _AggregateDocumentSchemaUri;
    }

    @Override
    public final Dimensions getApiDimensions()
    {

        return _ApiDimensions;
    }

    @Override
    public final URI getApiSchemaUri()
    {

        return _ApiSchemaUri;
    }

    @Override
    public Dimensions getChoicesDimensions()
    {

        return _ChoicesDimensions;
    }

    @Override
    public SortedSet<UniqueName> getChoicesNames(final UniqueName namespace)
    {

        // TODO: Refactor this method to be less of a copy paste of  getAllSubschemaNames
        if (namespace == null)
        {
            throw new IllegalArgumentException("The namespace cannot be null.");
        }

        final String namespaceString = namespace.toString();

        final SortedSet<UniqueName> typeNames = new TreeSet<>();
        final String javaPackageName = namespaceString.replace(UniqueName.NAME_SEPARATOR_CHAR, '.');

        // TODO: Cache or reuse the Reflections if possible (to avoid lag)
        final Reflections reflections = new Reflections(javaPackageName, this);

        final Set subTypes = reflections.getSubTypesOf(Enum.class);

        if (subTypes != null && !subTypes.isEmpty())
        {
            for (final Object subType : subTypes)
            {
                final Class<?> subClass = (Class<?>) subType;
                final UniqueName uniqueName = new UniqueName(subClass.getName().replace('.', UniqueName.NAME_SEPARATOR_CHAR));
                typeNames.add(uniqueName);
            }
        }

        final Set<URI> uriSet = _Choices.keySet();
        for (final URI uri : uriSet)
        {
            final UniqueName uniqueName = getTypeUniqueName(uri);
            final String uniqueNameString = uniqueName.toString();
            if (uniqueNameString.startsWith(namespaceString))
            {
                typeNames.add(uniqueName);
            }
        }


        return typeNames;
    }

    @Override
    public URI getChoicesSchemaUri()
    {

        return _ChoicesSchemaUri;
    }

    @Override
    public final SchemaLoaderConfiguration getConfig()
    {

        return getContext().getConfig().getSchemaLoader();
    }

    @Override
    public final Context getContext()
    {

        return _Context;
    }

    @Override
    public final URI getDocumentSchemaUri()
    {

        return _DocumentSchemaUri;
    }

    @Override
    public final URI getEmbeddedSchemaUri()
    {

        return _EmbeddedSchemaUri;
    }

    @Override
    public Dimensions getFormatDimensions()
    {

        return _FormatDimensions;
    }

    @Override
    public URI getFormatSchemaUri()
    {

        return _FormatSchemaUri;
    }

    @Override
    public JsonSchemaLoader getJsonSchemaLoader()
    {

        return _JsonSchemaLoader;
    }

    @Override
    public final Dimensions getLinkRelationDimensions()
    {

        return _LinkRelationDimensions;
    }

    @Override
    public final URI getLinkRelationSchemaUri()
    {

        return _LinkRelationSchemaUri;
    }

    @Override
    public final URI getLinkSchemaUri()
    {

        return _LinkSchemaUri;
    }

    @Override
    public Choices getLoadedChoices(final Keys keys)
    {

        final URI uri = (URI) keys.getValue(getDocumentSchemaUri());

        if (uri == null)
        {
            return null;
        }

        if (_NativeChoices.containsKey(uri))
        {
            return _NativeChoices.get(uri);
        }


        if (_Choices.containsKey(uri))
        {
            return _Choices.get(uri);
        }

        return null;

    }

    @Override
    public Schema getLoadedSchema(final Keys keys)
    {

        final URI schemaUri = keys.getValue(getDocumentSchemaUri());

        if (schemaUri == null)
        {
            return null;
        }

        if (_NativeSchemas.containsKey(schemaUri))
        {
            return _NativeSchemas.get(schemaUri);
        }

        if (_Schemas.containsKey(schemaUri))
        {
            return _Schemas.get(schemaUri);
        }

        return null;
    }

    @Override
    public final SortedSet<URI> getLoadedSchemaUris()
    {

        final SortedSet<URI> allLoadedSchemaUris = new TreeSet<>(_NativeSchemas.keySet());
        allLoadedSchemaUris.addAll(_Schemas.keySet());
        return allLoadedSchemaUris;
    }

    @Override
    public Choices getNativeChoices(final Keys keys)
    {

        URI uri = keys.getValue(getDocumentSchemaUri());

        if (uri == null)
        {
            final UniqueName uniqueName = keys.getValue(getChoicesSchemaUri());
            if (uniqueName != null)
            {
                final String enumInternalClassName = uniqueName.toString();
                final String enumExternalClassName = SchemaGenerator.internalTypeNameToExternalTypeName(enumInternalClassName);
                uri = getTypeUri(enumExternalClassName, true, true);
            }
        }

        if (uri == null)
        {
            return null;
        }

        if (_NativeChoices.containsKey(uri))
        {
            return _NativeChoices.get(uri);
        }

        final Class<?> nativeChoicesEnumClass = getNativeChoicesEnumClass(uri);
        if (nativeChoicesEnumClass == null)
        {
            return null;
        }


        final Choices nativeChoices = getSchemaGenerator().generateChoices(nativeChoicesEnumClass);
        _NativeChoices.put(uri, nativeChoices);
        return nativeChoices;

    }

    @Override
    public final Schema getNativeSchema(final Keys keys)
    {

        URI schemaUri = keys.getValue(getDocumentSchemaUri());

        if (schemaUri == null)
        {
            final UniqueName schemaUniqueName = keys.getValue(getSchemaSchemaUri());
            if (schemaUniqueName != null)
            {
                final String internalClassName = schemaUniqueName.toString();
                final String schemaInterfaceName = SchemaGenerator.internalTypeNameToExternalTypeName(internalClassName);
                schemaUri = getTypeUri(schemaInterfaceName);
            }
        }

        if (schemaUri == null)
        {
            return null;
        }

        if (_NativeSchemas.containsKey(schemaUri))
        {
            return _NativeSchemas.get(schemaUri);
        }

        final Class<?> nativeSchemaInterface = getNativeSchemaInterface(schemaUri);
        if (nativeSchemaInterface == null)
        {
            return null;
        }

        final Prototype prototype = getPrototype(schemaUri);
        final Schema nativeSchema = getSchemaGenerator().generateSchema(prototype);
        _NativeSchemas.put(schemaUri, nativeSchema);
        return nativeSchema;
    }

    @Override
    public final String getNativeTypeName(final URI typeUri)
    {

        final BiMap<URI, String> uriToNativeTypeNameBiMapView = _NativeTypeNameToUriBiMap.inverse();
        if (!uriToNativeTypeNameBiMapView.containsKey(typeUri))
        {

            final UniqueName uniqueName = getTypeUniqueName(typeUri);
            if (uniqueName == null)
            {
                throw new SchemaLoaderException("The type's uniqueName could not be determined from: " + typeUri,
                        null, this);
            }

            String localName = uniqueName.getLocalName();
            if (localName != null)
            {
                int indexOfLastDot = localName.lastIndexOf(".");

                if (indexOfLastDot > 0)
                {
                    localName = localName.substring(0, indexOfLastDot);
                }
                else if (indexOfLastDot == 0)
                {
                    localName = localName.substring(1);
                }
            }

            String namespace = uniqueName.getNamespace();
            if (namespace != null)
            {
                namespace = StringUtils.replaceChars(namespace, ".", "_");
            }

            String internalTypeName;
            if (namespace != null && localName != null)
            {
                String suffix = localName.trim();
                if (!suffix.isEmpty())
                {
                    suffix = UniqueName.NAME_SEPARATOR + suffix;
                }

                internalTypeName = namespace + suffix;
            }
            else if (namespace == null && localName == null)
            {
                internalTypeName = "unnamed";
            }
            else if (namespace == null && localName != null)
            {
                internalTypeName = localName;
            }
            else
            {
                internalTypeName = namespace;
            }


            internalTypeName = StringUtils.replaceChars(internalTypeName, ".", "_");
            final String schemaInterfaceName = SchemaGenerator.internalTypeNameToExternalTypeName(internalTypeName);

            _NativeTypeNameToUriBiMap.put(schemaInterfaceName, typeUri);
        }

        return uriToNativeTypeNameBiMapView.get(typeUri);

    }

    @Override
    public final Prototype getPrototype(final URI schemaUri)
    {

        if (schemaUri == null)
        {
            throw new SchemaLoaderException("The schema URI cannot be null", null, this);
        }

        if (!_Prototypes.containsKey(schemaUri))
        {
            final Prototype prototype = new Prototype(this, schemaUri);
            _Prototypes.put(schemaUri, prototype);

        }
        return _Prototypes.get(schemaUri);
    }

    @Override
    public final SortedSet<URI> getPrototypedSchemaUris()
    {

        return new TreeSet<>(_Prototypes.keySet());
    }

    @Override
    public final URI getResourceTemplateSchemaUri()
    {

        return _ResourceTemplateSchemaUri;
    }

    @Override
    public final Dimensions getSchemaDimensions()
    {

        return _SchemaDimensions;
    }

    @Override
    public final Class<?> getSchemaInterface(final URI schemaUri) throws ClassNotFoundException
    {

        if (!_SchemaInterfaces.containsKey(schemaUri))
        {

            Class<?> schemaInterface = getNativeSchemaInterface(schemaUri);
            if (schemaInterface != null)
            {
                return schemaInterface;
            }

            // See if the schema interface is loadable by the parent. This is done primarily for
            // bootstrapping WRML; allowing us to treat "standard" Java interfaces as if they were WRML schema-based.

            final ClassLoader parentClassLoader = getParent();

            final String schemaInterfaceName = getNativeTypeName(schemaUri);

            try
            {
                schemaInterface = parentClassLoader.loadClass(schemaInterfaceName);
            }
            catch (final ClassNotFoundException e)
            {
                // Swallow this exception and keep trying to load the schema ourselves
            }

            if (schemaInterface == null)
            {
                // Try to load the schema ourselves...
                schemaInterface = loadClass(schemaInterfaceName, true);
            }

            _SchemaInterfaces.put(schemaUri, schemaInterface);

        }

        return _SchemaInterfaces.get(schemaUri);

    }

    @Override
    public final byte[] getSchemaInterfaceBytecode(final Schema schema)
    {

        final SchemaGenerator generator = getSchemaGenerator();
        JavaBytecodeClass javaBytecodeClass = generator.generateSchemaInterface(schema);

        if (javaBytecodeClass != null)

        {
            final byte[] bytecode = javaBytecodeClass.getBytecode();
            return bytecode;
        }

        return null;
    }

    @Override
    public SortedSet<UniqueName> getSchemaNames(final UniqueName namespace)
    {

        final SortedSet<UniqueName> allSubschemaNames = getAllSubschemaNames(namespace);

        if (allSubschemaNames == null || allSubschemaNames.isEmpty())
        {
            return allSubschemaNames;
        }

        final String namespaceString = (namespace != null) ? namespace.toString() : "";
        final SortedSet<UniqueName> schemaNames = new TreeSet<>();
        for (final UniqueName subschemaName : allSubschemaNames)
        {
            final String subschemaNameString = subschemaName.toString();
            final String remainderNameString = StringUtils.removeStart(subschemaNameString, namespaceString + UniqueName.NAME_SEPARATOR);
            if (!remainderNameString.contains(UniqueName.NAME_SEPARATOR))
            {
                schemaNames.add(subschemaName);
            }
        }

        return schemaNames;
    }

    @Override
    public SortedSet<UniqueName> getSchemaSubnamespaces(final UniqueName namespace)
    {

        final SortedSet<UniqueName> allSubschemaNames = getAllSubschemaNames(namespace);

        if (allSubschemaNames == null || allSubschemaNames.isEmpty())
        {
            return allSubschemaNames;
        }

        String namespaceString = (namespace != null) ? namespace.toString() : "";
        if (!namespaceString.endsWith(UniqueName.NAME_SEPARATOR))
        {
            namespaceString = namespaceString + UniqueName.NAME_SEPARATOR;
        }
        final SortedSet<UniqueName> subnamespaces = new TreeSet<>();
        for (final UniqueName subschemaName : allSubschemaNames)
        {
            final String subschemaNameString = subschemaName.toString();
            final String remainderNameString = StringUtils.removeStart(subschemaNameString, namespaceString);

            final int endIndex = remainderNameString.indexOf(UniqueName.NAME_SEPARATOR);
            if (endIndex > 0)
            {
                final String localName = remainderNameString.substring(0, endIndex);
                subnamespaces.add(new UniqueName(namespace, localName));
            }

        }

        return subnamespaces;
    }

    @Override
    public Dimensions getSchemaNamespaceDimensions()
    {

        return _SchemaNamespaceDimensions;
    }

    @Override
    public URI getSchemaNamespaceSchemaUri()
    {

        return _SchemaNamespaceSchemaUri;
    }

    @Override
    public final URI getSchemaSchemaUri()
    {

        return _SchemaSchemaUri;
    }

    @Override
    public final URI getSyntaxSchemaUri()
    {

        return _SyntaxSchemaUri;
    }


    @Override
    public final URI getTypeUri(final String typeName)
    {

        return getTypeUri(typeName, false, false);
    }

    private final URI getTypeUri(final String typeName, final boolean isChoices, final boolean cache)
    {

        if (_NativeTypeNameToUriBiMap.containsKey(typeName))
        {
            return _NativeTypeNameToUriBiMap.get(typeName);
        }


        final URI baseSystemUri = (isChoices) ? SystemApi.Choices.getUri() : SystemApi.Schema.getUri();

        final String path = "/" + SchemaGenerator.externalTypeNameToInternalTypeName(typeName);

        final URI uri = baseSystemUri.resolve(path);

        if (cache)
        {
            _NativeTypeNameToUriBiMap.put(typeName, uri);
        }

        return uri;
    }

    @Override
    public final URI getTypeUri(final Type type)
    {

        if (type instanceof Class<?>)
        {
            final Class<?> clazz = (Class<?>) type;
            return getTypeUri(clazz.getCanonicalName(), clazz.isEnum(), true);
        }
        else
        {
            // Get this schema id associated with the *slotted* schema. A slotted schema has one or more "open"
            // {@link Slot}s. Other (programming) languages refer to this type system concept as "parameterized",
            // "generic", or "templated" types.
            /*
             * // A slotted schema
             * final Context context = getContext();
             * final ApiLoader apiLoader = context.getApiLoader();
             * final ParameterizedType parameterizedType = (ParameterizedType) type;
             * final Class<?> slottedSchemaInterface = (Class<?>) parameterizedType.getRawType();
             * final URI slottedSchemaUri = getTypeUri(slottedSchemaInterface);
             *
             * final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(parameterizedType);
             * for (final TypeVariable<?> typeVar : typeArguments.keySet())
             * {
             * final Type slotSchemaType = typeArguments.get(typeVar);
             * final URI slotSchemaUri = getTypeUri(slotSchemaType);
             * final URI urlEncodedSlotSchemaUri = RestUtils.encodeUri(slotSchemaUri);
             * final String queryParam = typeVar.getName() + "=" + urlEncodedSlotSchemaUri;
             */

            throw new SchemaLoaderException(
                    "Slotted schemas are not supported in this version; cannot associate a schema URI with type: "
                            + type, null, this);
        }
    }

    @Override
    public UniqueName getTypeUniqueName(final URI uri)
    {

        final JsonSchema jsonSchema = _JsonSchemaLoader.getLoadedJsonSchema(uri);
        if (jsonSchema != null)
        {
            return JsonSchema.createJsonSchemaUniqueName(uri);
        }


        final Context context = getContext();
        final ApiLoader apiLoader = context.getApiLoader();


        URI keyedSchemaUri = getSchemaSchemaUri();
        Keys keys = apiLoader.buildDocumentKeys(uri, keyedSchemaUri);
        UniqueName uniqueName = null;
        if (keys != null)
        {
            uniqueName = keys.getValue(keyedSchemaUri);
        }

        if (uniqueName == null)
        {
            keyedSchemaUri = getChoicesSchemaUri();
            keys = apiLoader.buildDocumentKeys(uri, keyedSchemaUri);
            if (keys != null)
            {
                uniqueName = keys.getValue(keyedSchemaUri);
            }
        }

        return uniqueName;
    }

    @Override
    public final ValueType getValueType(final Type type)
    {

        final SyntaxLoader syntaxLoader = getContext().getSyntaxLoader();

        ValueType valueType = ValueType.getValueType(type);
        if (valueType == ValueType.Native)
        {
            final Class<?> rawType = ValueType.getRawType(type);
            final SyntaxHandler<?> syntaxHandler = syntaxLoader.getSyntaxHandler(rawType);

            if (syntaxHandler != null)
            {
                valueType = ValueType.Text;
            }
        }

        return valueType;
    }

    @Override
    public URI getVirtualSchemaUri()
    {

        return _VirtualSchemaUri;
    }

    @Override
    public void init(final Context context)
    {

        if (context == null)
        {
            throw new SchemaLoaderException("The WRML context cannot be null.", null, this);
        }

        _Context = context;

        _SchemaGenerator = new SchemaGenerator(this);

        _JsonSchemaLoader = new JsonSchemaLoader();
        _JsonSchemaLoader.init(_Context);

    }

    @Override
    public final boolean isAbstractSchema(final Class<?> schemaInterface)
    {

        final Class<?>[] declaredInterfaces = schemaInterface.getInterfaces();
        if (declaredInterfaces != null)
        {
            for (final Class<?> declaredInterface : declaredInterfaces)
            {
                if (ValueType.JAVA_TYPE_ABSTRACT.equals(declaredInterface))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public final boolean isPrototyped(final URI schemaUri)
    {

        return _Prototypes.containsKey(schemaUri);
    }

    @Override
    public final boolean isSubschema(final Type base, final Type sub)
    {

        // Neither may be null
        if (base == null || sub == null)
        {
            return false;
        }

        if (base.equals(sub))
        {
            return false;
        }

        // Both must be models
        if (!TypeUtils.isAssignable(base, Model.class) || !TypeUtils.isAssignable(sub, Model.class))
        {
            return false;
        }

        return TypeUtils.isAssignable(sub, base);
    }

    @Override
    public boolean isSystemSchema(final URI schemaUri)
    {

        return _SystemSchemaUris.contains(schemaUri);
    }

    @Override
    public void loadInitialState()
    {

        _JsonSchemaLoader.loadInitialState();
        loadJsonSchemas();
    }

    @Override
    public final Schema load(final JsonSchema jsonSchema, final URI... baseSchemaIds)
    {

        final URI schemaUri = jsonSchema.getId();
        if (_Schemas.containsKey(schemaUri))
        {
            return _Schemas.get(schemaUri);
        }

        final SchemaGenerator generator = getSchemaGenerator();
        final Schema schema = generator.generateSchema(jsonSchema, baseSchemaIds);

        return load(schema);
    }

    @Override
    public final Schema load(final Schema schema)
    {

        final URI schemaUri = schema.getUri();
        if (schemaUri == null)
        {
            throw new IllegalArgumentException("The Schema URI cannot be null.");
        }

        if (isPrototyped(schemaUri))
        {
            throw new SchemaLoaderException(
                    "The Schema has been prototyped from a Java interface, which cannot be reloaded without reloading the Context.",
                    null, this);
        }

        if (StringUtils.isEmpty(schema.getTitle()))
        {

            final String title = schema.getUniqueName().getLocalName();
            schema.setTitle(title);
        }

        final List<Slot> slots = schema.getSlots();
        for (final Slot slot : slots)
        {

            if (StringUtils.isEmpty(slot.getTitle()))
            {

                slot.setTitle(slot.getName());
            }
        }

        _Schemas.put(schemaUri, schema);
        return schema;
    }

    @Override
    public final Schema load(final URI schemaUri)
    {

        if (schemaUri == null)
        {
            throw new IllegalArgumentException("The Schema URI cannot be null.");
        }

        final Context context = getContext();
        final ApiLoader apiLoader = context.getApiLoader();
        final Keys keys = apiLoader.buildDocumentKeys(schemaUri, getSchemaSchemaUri());
        return context.getModel(keys, getSchemaDimensions());
    }

    @Override
    public Choices loadChoices(final Choices choices)
    {

        final URI choicesUri = choices.getUri();
        if (choicesUri == null)
        {
            throw new IllegalArgumentException("The Choices URI cannot be null.");
        }

        // TODO: Is this needed?
        // Force mapping of Choices URI to enum name
        getNativeTypeName(choicesUri);

        _Choices.put(choicesUri, choices);
        return choices;

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Class findClass(final String typeName) throws ClassNotFoundException
    {

        final Context context = getContext();
        final ApiLoader apiLoader = context.getApiLoader();

        URI uri = getTypeUri(typeName, false, false);
        URI schemaUri = getTypeSchemaUri(uri);
        Keys keys = apiLoader.buildDocumentKeys(uri, schemaUri);
        Dimensions dimensions = new DimensionsBuilder(schemaUri).toDimensions();
        Model model = context.getModel(keys, dimensions);

        if (model == null)
        {
            uri = getTypeUri(typeName, true, false);
            schemaUri = getTypeSchemaUri(uri);
            keys = apiLoader.buildDocumentKeys(uri, schemaUri);
            dimensions = new DimensionsBuilder(schemaUri).toDimensions();
            model = context.getModel(keys, dimensions);
        }

        final SchemaGenerator generator = getSchemaGenerator();

        JavaBytecodeClass javaBytecodeClass = null;

        if (model instanceof Schema)
        {
            javaBytecodeClass = generator.generateSchemaInterface((Schema) model);
        }
        else if (model instanceof Choices)
        {
            javaBytecodeClass = generator.generateChoicesEnum((Choices) model);
        }

        if (javaBytecodeClass != null)

        {
            final byte[] bytecode = javaBytecodeClass.getBytecode();

            final File schemaClassRootDirectory = getSchemaClassRootDirectory();
            if (schemaClassRootDirectory != null)
            {

                // TODO: Can this be simplified? (just trying to construct a
                // file path here and this code looks crazy...)
                final String[] splitResult = StringUtils.split(javaBytecodeClass.getInternalName());
                final int lastElementIndex = splitResult.length - 1;
                final String[] relativePath = ArrayUtils.subarray(splitResult, 0, lastElementIndex);
                final String classFileName = splitResult[lastElementIndex] + ".class";
                final File classFileDir = FileUtils.getFile(schemaClassRootDirectory, relativePath);
                final File classFileOnDisk = FileUtils.getFile(classFileDir, classFileName);

                try
                {
                    FileUtils.writeByteArrayToFile(classFileOnDisk, bytecode);
                }
                catch (final IOException e)
                {

                    throw new SchemaLoaderException("Failed to write class file (" + classFileOnDisk + ") for Model ("
                            + model + ")", e, this);

                }
            }

            return defineClass(typeName, bytecode, 0, bytecode.length);
        }

        return super.findClass(typeName);

    }

    private SortedSet<UniqueName> getAllSubschemaNames(final UniqueName namespace)
    {

        final String namespaceString = (namespace != null) ? namespace.toString() : UniqueName.NAME_SEPARATOR;

        final String javaPackageName = (!namespaceString.equals(UniqueName.NAME_SEPARATOR)) ? namespaceString.replace(UniqueName.NAME_SEPARATOR_CHAR, '.') : null;

        // TODO: Cache or reuse the Reflections if possible (to avoid lag)
        final Reflections reflections = new Reflections(javaPackageName, this);

        final SortedSet<UniqueName> typeNames = new TreeSet<>();

        for (Class<?> superType : JAVA_TYPE_WRML_BASES)
        {
            final Set subTypes = reflections.getSubTypesOf(superType);
            if (subTypes != null && !subTypes.isEmpty())
            {
                for (final Object subType : subTypes)
                {
                    final Class<?> subClass = (Class<?>) subType;
                    if (subClass.isInterface())
                    {
                        final UniqueName uniqueName = new UniqueName(subClass.getName().replace('.', UniqueName.NAME_SEPARATOR_CHAR));
                        typeNames.add(uniqueName);
                    }
                }
            }
        }


        // TODO: Implement this more intelligently, leveraging the Classloader API if possible

        /*

        http://reflections.googlecode.com/svn/trunk/reflections/javadoc/apidocs/org/reflections/util/ClasspathHelper.html#forPackage(java.lang.String, java.lang.ClassLoader...)

        public static Set<URL> forPackage(String name, ClassLoader... classLoaders)

            returns urls with resources of package starting with given name, using ClassLoader.getResources(String)
            that is, forPackage("org.reflections") effectively returns urls from classpath with packages starting with org.reflections

            if optional ClassLoaders are not specified, then both contextClassLoader() and staticClassLoader() are used for ClassLoader.getResources(String)
        */

        /*
        final Set<URI> uriSet = _Schemas.keySet();
        for (final URI uri : uriSet)
        {
            final UniqueName uniqueName = getTypeUniqueName(uri);
            final String uniqueNameString = uniqueName.toString();
            if (namespaceString.equals(UniqueName.NAME_SEPARATOR) || uniqueNameString.startsWith(namespaceString))
            {
                typeNames.add(uniqueName);
            }
        }
          */

        return typeNames;

    }

    private final File getSchemaClassRootDirectory()
    {

        final SchemaLoaderConfiguration config = getConfig();
        if (config != null)
        {
            /*
             * Note that this directory value may be null, in which case the class
             * files will not be written to disk but will be loaded into the
             * runtime's memory only.
             */

            return config.getSchemaClassRootDirectory();
        }
        return null;

    }

    private final SchemaGenerator getSchemaGenerator()
    {

        return _SchemaGenerator;
    }

    private Class<?> getNativeChoicesEnumClass(final URI uri)
    {

        if (!_ChoicesEnumClasses.containsKey(uri))
        {

            final ClassLoader parentClassLoader = getParent();
            if (parentClassLoader == null)
            {
                return null;

            }

            final String choicesEnumName = getNativeTypeName(uri);

            try
            {
                final Class<?> choicesEnumClass = parentClassLoader.loadClass(choicesEnumName);
                if (choicesEnumClass != null && choicesEnumClass.isEnum())
                {
                    _ChoicesEnumClasses.put(uri, choicesEnumClass);
                }
            }
            catch (final ClassNotFoundException e)
            {
                return null;
            }

        }

        return _ChoicesEnumClasses.get(uri);

    }

    private Class<?> getNativeSchemaInterface(final URI schemaUri)
    {

        if (!_SchemaInterfaces.containsKey(schemaUri))
        {

            final ClassLoader parentClassLoader = getParent();
            if (parentClassLoader == null)
            {
                return null;
            }

            final String schemaInterfaceName = getNativeTypeName(schemaUri);

            Class<?> schemaInterface = null;

            // See if the schema interface is loadable by the parent. This is done primarily for
            // bootstrapping WRML; allowing us to treat "standard" Java interfaces as if they were WRML schema-based.

            try
            {
                schemaInterface = parentClassLoader.loadClass(schemaInterfaceName);
                _SchemaInterfaces.put(schemaUri, schemaInterface);
            }
            catch (final ClassNotFoundException e)
            {
                return null;
            }

        }

        return _SchemaInterfaces.get(schemaUri);
    }

    private URI getTypeSchemaUri(final URI uri)
    {

        final Context context = getContext();
        final ApiLoader apiLoader = context.getApiLoader();
        final ApiNavigator apiNavigator = apiLoader.getParentApiNavigator(uri);
        if (apiNavigator == null)
        {
            return getSchemaSchemaUri();
        }

        final Resource resource = apiNavigator.getResource(uri);
        if (resource == null)
        {
            return getSchemaSchemaUri();
        }

        return resource.getDefaultSchemaUri();
    }


    private void loadJsonSchemas()
    {

        final SchemaLoaderConfiguration config = _Context.getConfig().getSchemaLoader();
        if (config == null)
        {
            return;
        }

        final Map<URI, JsonSchema> loadedJsonSchemas = new LinkedHashMap<>();
        final URI[] jsonSchemaIds = config.getJsonSchemaIds();
        if (jsonSchemaIds != null)
        {
            for (final URI jsonSchemaUri : jsonSchemaIds)
            {
                try
                {
                    final JsonSchema jsonSchema = _JsonSchemaLoader.load(jsonSchemaUri);
                    if (jsonSchema != null)
                    {
                        loadedJsonSchemas.put(jsonSchema.getId(), jsonSchema);
                    }
                }
                catch (final IOException e)
                {
                    LOG.error(e.getMessage(), e);
                }
            }
        }

        final File[] jsonSchemaDirectories = config.getJsonSchemaDirectories();
        if (jsonSchemaDirectories != null)
        {
            for (final File jsonSchemaDirectory : jsonSchemaDirectories)
            {
                final Collection<File> jsonSchemaFiles = FileUtils.listFiles(jsonSchemaDirectory,
                        JSON_SCHEMA_FILE_EXTENSIONS, true);

                for (final File jsonSchemaFile : jsonSchemaFiles)
                {
                    try
                    {
                        final JsonSchema jsonSchema = _JsonSchemaLoader.load(jsonSchemaFile);
                        if (jsonSchema != null)
                        {
                            loadedJsonSchemas.put(jsonSchema.getId(), jsonSchema);
                        }
                    }
                    catch (final IOException e)
                    {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }

        final File[] jsonSchemaFiles = config.getJsonSchemaFiles();
        if (jsonSchemaFiles != null)
        {
            for (final File jsonSchemaFile : jsonSchemaFiles)
            {
                try
                {
                    final JsonSchema jsonSchema = _JsonSchemaLoader.load(jsonSchemaFile);
                    if (jsonSchema != null)
                    {
                        loadedJsonSchemas.put(jsonSchema.getId(), jsonSchema);
                    }
                }
                catch (final IOException e)
                {
                    LOG.error(e.getMessage(), e);
                }
            }
        }

        for (final JsonSchema loadedSchema : loadedJsonSchemas.values())
        {
            load(loadedSchema, getDocumentSchemaUri());
        }
    }

}

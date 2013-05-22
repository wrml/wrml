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
 * Copyright 2012 Mark Masse (OSS project WRML.org)
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
package org.wrml.runtime.format.application.schema.json;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.StringUtils;
import org.wrml.runtime.format.application.schema.json.JsonSchema.Definitions.JsonType;
import org.wrml.runtime.format.application.schema.json.JsonSchema.Definitions.PropertyType;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.util.UniqueName;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * http://tools.ietf.org/html/draft-zyp-json-schema-03
 */
public class JsonSchema
{

    private final JsonSchemaLoader _JsonSchemaLoader;

    private final ObjectNode _RootNode;

    private final URI _SchemaUri;

    private final ConcurrentMap<String, Property> _Properties;

    private final CopyOnWriteArrayList<Link> _Links;

    private final CopyOnWriteArraySet<URI> _ExtendsSet;
    
    private final CopyOnWriteArrayList<Property> _Required;
    
    private final Map<String, List<Property>> _Dependencies;

    JsonSchema(final JsonSchemaLoader loader, final ObjectNode rootNode)
    {
        _JsonSchemaLoader = loader;
        _RootNode = rootNode;

        final SyntaxLoader syntaxLoader = _JsonSchemaLoader.getContext().getSyntaxLoader();
        _SchemaUri = Definitions.PropertyType.$Schema.getValue(_RootNode, syntaxLoader);

        _ExtendsSet = new CopyOnWriteArraySet<>();

        _Properties = new ConcurrentHashMap<>();
        _Dependencies = new ConcurrentHashMap<>();
        _Required = new CopyOnWriteArrayList<>();
        _Links = new CopyOnWriteArrayList<>();
        
        // TODO not pass the args...
        parseExtensions(rootNode, syntaxLoader);
        parseProperties();
        parseLinks();
        parseRequireds();
        parseDependencies();
    }
    
    private void parseExtensions(final ObjectNode rootNode, final SyntaxLoader syntaxLoader)
    {
        // v3 uses the extends keyword
        if (rootNode.has(PropertyType.Extends.getName()))
        {
            final JsonNode extendsJsonNode = rootNode.get(PropertyType.Extends.getName());

            if (extendsJsonNode instanceof ArrayNode)
            {
                final ArrayNode extendsArrayNode = (ArrayNode) extendsJsonNode;
                final Iterator<JsonNode> elements = extendsArrayNode.elements();
                while (elements.hasNext())
                {
                    final JsonNode baseSchemaUriNode = elements.next();
                    final String baseSchemaUriString = baseSchemaUriNode.asText();
                    if (baseSchemaUriString != null && !baseSchemaUriString.isEmpty())
                    {
                        final URI baseSchemaUri = syntaxLoader.parseSyntacticText(baseSchemaUriString, URI.class);
                        if (baseSchemaUri != null)
                        {
                            _ExtendsSet.add(baseSchemaUri);
                        }
                    }
                }
            }
            else if (extendsJsonNode instanceof TextNode)
            {
                final String baseSchemaUriString = extendsJsonNode.asText();
                if (baseSchemaUriString != null && !baseSchemaUriString.isEmpty())
                {
                    final URI baseSchemaUri = syntaxLoader.parseSyntacticText(baseSchemaUriString, URI.class);
                    if (baseSchemaUri != null)
                    {
                        _ExtendsSet.add(baseSchemaUri);
                    }
                }

            }
        }
        
        // v4 uses the allOf keyword
        if (rootNode.has(PropertyType.AllOf.getName()))
        {
            final JsonNode allOfJsonNode = rootNode.get(PropertyType.AllOf.getName());
            
            // This element type MUST be an array
            if (allOfJsonNode instanceof ArrayNode)
            {
                final ArrayNode allOfArrayNode = (ArrayNode) allOfJsonNode;
                final Iterator<JsonNode> elements = allOfArrayNode.elements();
                while (elements.hasNext())
                {
                    final JsonNode schemaNode = elements.next();
                    final JsonNode baseSchemaUriNode = schemaNode.get(PropertyType.$Ref.getName());
                    if (baseSchemaUriNode != null)
                    {
                        final URI baseSchemaUri = syntaxLoader.parseSyntacticText(baseSchemaUriNode.asText(), URI.class);
                        if (baseSchemaUri != null)
                        {
                            _ExtendsSet.add(baseSchemaUri);
                        }
                    }
                }
            }
        }
    }
    
    private void parseProperties()
    {
        final ObjectNode propertiesNode = (ObjectNode) Definitions.PropertyType.Properties.getValueNode(_RootNode);

        if (propertiesNode != null)
        {

            final Iterator<String> propertyNames = propertiesNode.fieldNames();
            while (propertyNames.hasNext())
            {
                final String name = propertyNames.next();

                final ObjectNode propertyNode = (ObjectNode) propertiesNode.get(name);

                Property property;
                try
                {
                    property = new Property(this, name, propertyNode);
                }
                catch (final IOException e)
                {
                    continue;
                }

                // Add to the required set if noted
                Object value = property.getValue(PropertyType.Required);
                if (value != null && (Boolean)value)
                {
                    _Required.add(property);
                }
                
                _Properties.put(name, property);

            }
        }
    }
    
    private void parseLinks()
    {
        final ArrayNode linksNode = Definitions.PropertyType.Links.getValueNode(_RootNode);

        if (linksNode != null)
        {

            for (final JsonNode linkNode : linksNode)
            {
                final Link link = new Link(this, (ObjectNode) linkNode);
                _Links.add(link);
            }

        }
    }
    
    private void parseRequireds()
    {
        final JsonNode requiredPreNode = Definitions.PropertyType.Required.getValueNode(_RootNode);
        
        if (requiredPreNode != null && requiredPreNode instanceof ArrayNode)
        {
            final ArrayNode requiredNode = (ArrayNode)requiredPreNode;
            for (final JsonNode requiredSubNode : requiredNode)
            {
                final String propertyName = requiredSubNode.asText();
                final Property property = _Properties.get(propertyName);
                _Required.add(property);
                if (property != null)
                {
                    ObjectNode node = property.getPropertyNode();
                    node.put(PropertyType.Required.getName(), true);
                }
            }
        }
    }
    
    private void parseDependencies()
    {
        final ObjectNode dependenciesNode = Definitions.PropertyType.Dependencies.getValueNode(_RootNode);
        
        if (dependenciesNode != null)
        {
            Iterator<Entry<String, JsonNode>> iter2 = dependenciesNode.fields();
            
            while (iter2.hasNext())
            {
                Entry<String, JsonNode> current = iter2.next();
                String dependent = current.getKey();
                JsonNode dependencies = current.getValue();
                
                List<Property> propertyDependencies = new ArrayList<Property>();
                
                if (dependencies instanceof ArrayNode)
                {
                    ArrayNode dependenciesArray = (ArrayNode)dependencies;
                    Iterator<JsonNode> iter3 = dependenciesArray.iterator();
                    
                    while (iter3.hasNext())
                    {
                        JsonNode dependency = iter3.next();
                        String depText = dependency.asText();
                        Property prop = _Properties.get(depText);
                        if(prop != null)
                        {
                            propertyDependencies.add(prop);
                        }
                    }
                }
                
                // Only add if our list has values that map.
                if (!propertyDependencies.isEmpty())
                {
                    _Dependencies.put(dependent, propertyDependencies);
                }
            }
        }
    }

    public static UniqueName createJsonSchemaUniqueName(final URI schemaUri)
    {

        String uniqueNameString = schemaUri.getPath();
        uniqueNameString = StringUtils.stripStart(uniqueNameString, "/");
        uniqueNameString = StringUtils.stripEnd(uniqueNameString, "#");
        if (uniqueNameString.endsWith(".json"))
        {
            uniqueNameString = uniqueNameString.substring(0, uniqueNameString.length() - ".json".length());
        }

        if (!uniqueNameString.contains("/"))
        {
            uniqueNameString = "schemas/" + uniqueNameString;
        }

        final UniqueName literalUniqueName = new UniqueName(uniqueNameString);
        return literalUniqueName;
    }

    public String getDescription()
    {

        final SyntaxLoader syntaxLoader = _JsonSchemaLoader.getContext().getSyntaxLoader();
        return Definitions.PropertyType.Description.getValue(getRootNode(), syntaxLoader);
    }

    public Set<URI> getExtendedSchemaUris()
    {

        return _ExtendsSet;
    }

    public URI getId()
    {

        final SyntaxLoader syntaxLoader = _JsonSchemaLoader.getContext().getSyntaxLoader();
        return Definitions.PropertyType.Id.getValue(_RootNode, syntaxLoader);
    }

    public List<Link> getLinks()
    {

        return _Links;
    }
    
    public List<Property> getRequired()
    {
        return _Required;
    }
    
    public Map<String, List<Property>> getDependencies()
    {
        return _Dependencies;
    }

    public JsonSchemaLoader getLoader()
    {

        return _JsonSchemaLoader;
    }

    public ConcurrentMap<String, Property> getProperties()
    {

        return _Properties;
    }

    public ObjectNode getRootNode()
    {

        return _RootNode;
    }

    public URI getSchemaUri()
    {

        return _SchemaUri;
    }

    public JsonNode getTypeNode()
    {

        return Definitions.PropertyType.Type.getValueNode(getRootNode());
    }

    @Override
    public String toString()
    {

        return String.valueOf(getRootNode());
    }

    /*
     * date-time This SHOULD be a date in ISO 8601 format of YYYY-MM-
     * DDThh:mm:ssZ in UTC time. This is the recommended form of date/
     * timestamp.
     *
     * date This SHOULD be a date in the format of YYYY-MM-DD. It is
     * recommended that you use the "date-time" format instead of "date"
     * unless you need to transfer only the date part.
     *
     * time This SHOULD be a time in the format of hh:mm:ss. It is
     * recommended that you use the "date-time" format instead of "time"
     * unless you need to transfer only the time part.
     *
     * utc-millisec This SHOULD be the difference, measured in
     * milliseconds, between the specified time and midnight, 00:00 of
     * January 1, 1970 UTC. The value SHOULD be a number (integer or
     * float).
     *
     * regex A regular expression, following the regular expression
     * specification from ECMA 262/Perl 5.
     *
     * color This is a CSS color (like "#FF0000" or "red"), based on CSS
     * 2.1 [W3C.CR-CSS21-20070719].
     *
     * style This is a CSS style definition (like "color: red; background-
     * color:#FFF"), based on CSS 2.1 [W3C.CR-CSS21-20070719].
     *
     * phone This SHOULD be a phone number (format MAY follow E.123).
     *
     * uri This value SHOULD be a URI..
     *
     * email This SHOULD be an email address.
     *
     * ip-address This SHOULD be an ip version 4 address.
     *
     * ipv6 This SHOULD be an ip version 6 address.
     *
     * host-name This SHOULD be a host-name.
     */
    public enum JsonStringFormat
    {
        DateTime("date-time", Date.class),
        Date("date", Date.class),
        Time("time", Date.class),
        Uri("uri", URI.class);

        private final static Map<String, JsonStringFormat> KEYWORD_MAP = new HashMap<>();

        private final static Map<Class<?>, JsonStringFormat> JAVA_TYPE_MAP = new HashMap<>();

        static
        {
            for (final JsonStringFormat jsonStringFormat : JsonStringFormat.values())
            {
                KEYWORD_MAP.put(jsonStringFormat.getKeyword(), jsonStringFormat);
                JAVA_TYPE_MAP.put(jsonStringFormat.getJavaType(), jsonStringFormat);
            }
        }

        private final String _Keyword;

        private final Class<?> _JavaType;

        JsonStringFormat(final String keyword, final Class<?> javaType)
        {

            _Keyword = keyword;
            _JavaType = javaType;
        }

        public static JsonStringFormat forJavaType(final Class<?> javaType)
        {

            if (!JAVA_TYPE_MAP.containsKey(javaType))
            {
                return null;
            }
            return JAVA_TYPE_MAP.get(javaType);
        }

        public static JsonStringFormat forKeyword(final String keyword)
        {

            if (!KEYWORD_MAP.containsKey(keyword))
            {
                return null;
            }
            return KEYWORD_MAP.get(keyword);
        }

        public Class<?> getJavaType()
        {

            return _JavaType;
        }

        public String getKeyword()
        {

            return _Keyword;
        }

    }

    public static interface Definitions
    {

        /**
         * http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.1
         */
        public static enum JsonType
        {

            /**
             * Value MAY be of any type including null.
             */
            Any("any"),

            /**
             * Value MUST be an array.
             */
            Array("array"),

            /**
             * Value MUST be a boolean.
             */
            Boolean("boolean"),

            /**
             * Value MUST be an integer, no floating point numbers are allowed. This is a subset of the number type.
             */
            Integer("integer"),

            /**
             * Value MUST be null. Note this is mainly for purpose of being able use union types to define
             * nullability. If this type is not included in a union, null values are not allowed (the primitives
             * listed above do not allow nulls on their own).
             */
            Null("null"),

            /**
             * Value MUST be a number, floating point numbers are allowed.
             */
            Number("number"),

            /**
             * Value MUST be an object.
             */
            Object("object"),

            /**
             * Value MUST be a string.
             */
            String("string");

            private final static Map<String, JsonType> KEYWORD_MAP = new HashMap<>();

            static
            {
                for (final JsonType jsonType : JsonType.values())
                {
                    KEYWORD_MAP.put(jsonType.getKeyword(), jsonType);
                }
            }

            private final String _Keyword;

            JsonType(final String keyword)
            {

                _Keyword = keyword;
            }

            public static JsonType forKeyword(final String keyword)
            {

                if (!KEYWORD_MAP.containsKey(keyword))
                {
                    return null;
                }
                return KEYWORD_MAP.get(keyword);
            }

            public String getKeyword()
            {

                return _Keyword;
            }

        }

        /**
         * http://tools.ietf.org/html/draft-zyp-json-schema-03
         */
        public static enum PropertyType
        {

            AdditionalItems("additionalItems", JsonType.Any),
            AdditionalProperties("additionalProperties", JsonType.Any),
            AllOf("allOf", JsonType.Object),
            AnyOf("anyOf", JsonType.Object),
            Default("default", JsonType.Any),
            Definitions("definitions", JsonType.Object),
            Dependencies("dependencies", JsonType.Object),
            Description("description", JsonType.String),
            Disallow("disallow", JsonType.Any),
            DivisibleBy("divisibleBy", JsonType.Number),
            Enctype("enctype", JsonType.String),
            Enum("enum", JsonType.Array),
            ExclusiveMaximum("exclusiveMaximum", JsonType.Boolean),
            ExclusiveMinimum("exclusiveMinimum", JsonType.Boolean),
            Extends("extends", JsonType.Any),
            Format("format", JsonType.String),
            Href("href", URI.class),
            Id("id", URI.class),
            Items("items", JsonType.Any),
            Links("links", JsonType.Array),
            Maximum("maximum", JsonType.Number),
            MaxItems("maxItems", JsonType.Integer),
            MaxLength("maxLength", JsonType.Integer),
            MaxProperties("maxProperties", JsonType.Integer),
            Method("method", JsonType.String),
            Minimum("minimum", JsonType.Number),
            MinItems("minItems", JsonType.Integer),
            MinLength("minLength", JsonType.Integer),
            MinProperties("minProperties", JsonType.Integer),
            MultipleOf("multipleOf", JsonType.Number),
            Pattern("pattern", JsonType.String),
            PatternProperties("patternProperties", JsonType.Object),
            Properties("properties", JsonType.Object),
            $Ref("$ref", URI.class),
            Rel("rel", JsonType.String),
            // TODO This field is now deprecated in the properties
            Required("required", JsonType.Boolean),
            $Schema("$schema", URI.class),
            Schema("schema", JsonType.Object),
            TargetSchema("targetSchema", JsonType.Object),
            Title("title", JsonType.String),
            Type("type", JsonType.Any),
            UniqueItems("uniqueItems", JsonType.Boolean);

            private final static Map<String, PropertyType> NAME_MAP = new HashMap<>();

            static
            {
                for (final PropertyType propertyType : PropertyType.values())
                {
                    final String name = propertyType.getName();
                    if (NAME_MAP.containsKey(name))
                    {
                        throw new RuntimeException("Duplicate mappings detected.");
                    }

                    NAME_MAP.put(name, propertyType);
                }
            }

            private final String _Name;

            private final JsonType _JsonType;

            private final Object _DefaultValue;

            private final Class<?> _Format;

            private PropertyType(final String textPropertyName, final Class<?> format)
            {

                this(textPropertyName, JsonType.String, format, null);
            }

            private PropertyType(final String propertyName, final JsonType jsonType)
            {

                this(propertyName, jsonType, null, null);
            }

            private PropertyType(final String propertyName, final JsonType jsonType, final Class<?> format,
                                 final Object defaultValue)
            {

                _Name = propertyName;
                _JsonType = jsonType;
                _Format = format;
                _DefaultValue = defaultValue;
            }

            private PropertyType(final String propertyName, final JsonType jsonType, final Object defaultValue)
            {

                this(propertyName, jsonType, null, defaultValue);
            }

            public static PropertyType forName(final String name)
            {

                if (!NAME_MAP.containsKey(name))
                {
                    return null;
                }
                return NAME_MAP.get(name);
            }

            public Object getDefaultValue()
            {

                return _DefaultValue;
            }

            public Class<?> getFormat()
            {

                return _Format;
            }

            public JsonType getJsonType()
            {

                return _JsonType;
            }

            public String getName()
            {

                return _Name;
            }

            @SuppressWarnings("unchecked")
            public <T> T getValue(final JsonNode jsonNode, final SyntaxLoader syntaxLoader)
            {

                final JsonNode valueNode = getValueNode(jsonNode);
                if (valueNode == null)
                {
                    return null;
                }

                final T value;

                switch (_JsonType)
                {

                    case Any:
                    {

                        value = (T) valueNode.asText();
                        break;
                    }
                    case Array:
                    {
                        value = null;
                        break;
                    }
                    case Boolean:
                    {

                        if (valueNode.isBoolean())
                        {
                            value = (T) ((valueNode.asBoolean()) ? Boolean.TRUE : Boolean.FALSE);
                        }
                        else
                        {
                            value = null;
                        }

                        break;
                    }
                    case Integer:
                    {

                        if (valueNode.isIntegralNumber())
                        {
                            value = (T) new Integer(valueNode.asInt());
                        }
                        else
                        {
                            value = null;
                        }

                        break;
                    }
                    case Null:
                    {
                        value = null;
                        break;
                    }
                    case Number:
                    {
                        if (valueNode.isFloatingPointNumber())
                        {
                            value = (T) new Double(valueNode.asDouble());
                        }
                        else if (valueNode.isIntegralNumber())
                        {
                            value = (T) new Integer(valueNode.asInt());
                        }
                        else
                        {
                            value = null;
                        }

                        break;
                    }
                    case Object:
                    {
                        value = (T) valueNode;
                        break;
                    }
                    case String:
                    {

                        if (valueNode.isTextual())
                        {
                            final String textValue = valueNode.textValue();

                            if (_Format != null && textValue != null && !(textValue.trim()).isEmpty())
                            {
                                value = syntaxLoader.parseSyntacticText(textValue, _Format);
                            }
                            else
                            {
                                value = (T) textValue;
                            }
                        }
                        else
                        {
                            value = null;
                        }

                        break;
                    }
                    default:
                    {
                        value = null;
                        break;
                    }
                }

                return value;
            }

            @SuppressWarnings("unchecked")
            public <T extends JsonNode> T getValueNode(final JsonNode jsonNode)
            {

                return (T) jsonNode.get(_Name);
            }

            public void setValue(final ObjectNode jsonNode, final Object value, final SyntaxLoader syntaxLoader)
            {

                if (value == null)
                {
                    return;
                }

                final String name = getName();
                switch (_JsonType)
                {
                    case Any:
                    {
                        if (value instanceof String)
                        {
                            jsonNode.put(name, (String) value);
                        }
                        else if (value instanceof Integer)
                        {
                            jsonNode.put(name, (Integer) value);
                        }
                        else if (value instanceof Long)
                        {
                            jsonNode.put(name, (Long) value);
                        }
                        else if (value instanceof Double)
                        {
                            jsonNode.put(name, (Double) value);
                        }
                        else if (value instanceof Boolean)
                        {
                            jsonNode.put(name, (Boolean) value);
                        }
                        else
                        {
                            final String formattedValue = syntaxLoader.formatSyntaxValue(value);
                            if (formattedValue != null)
                            {
                                jsonNode.put(name, formattedValue);
                            }
                        }
                        break;
                    }
                    case Array:
                    {
                        break;
                    }
                    case Boolean:
                    {
                        jsonNode.put(name, (Boolean) value);
                        break;
                    }
                    case Integer:
                    {
                        jsonNode.put(name, (Integer) value);
                        break;
                    }
                    case Null:
                    {
                        break;
                    }
                    case Number:
                    {
                        if (value instanceof Integer)
                        {
                            jsonNode.put(name, (Integer) value);
                        }
                        else if (value instanceof Long)
                        {
                            jsonNode.put(name, (Long) value);
                        }
                        else if (value instanceof Double)
                        {
                            jsonNode.put(name, (Double) value);
                        }

                        break;
                    }
                    case Object:
                    {
                        jsonNode.putObject(name);
                        break;

                    }
                    case String:
                    {
                        jsonNode.put(name, syntaxLoader.formatSyntaxValue(value));
                        break;
                    }
                    default:
                    {
                        break;
                    }

                }

            }
        }

    }

    public static final class Link
    {

        private final JsonSchema _JsonSchema;

        private final ObjectNode _LinkNode;

        private final String _Rel;

        private final URI _RelId;

        private final URI _SchemaId;

        private final URI _TargetSchemaId;

        public Link(final JsonSchema jsonSchema, final ObjectNode linkNode)
        {

            if (jsonSchema == null || linkNode == null)
            {
                throw new NullPointerException();
            }
            _JsonSchema = jsonSchema;
            _LinkNode = linkNode;


            final SyntaxLoader syntaxLoader = getJsonSchema().getLoader().getContext().getSyntaxLoader();
            _Rel = PropertyType.Rel.getValue(getLinkNode(), syntaxLoader);

            URI linkRelationUri = null;
            try
            {
                linkRelationUri = new URI(_Rel);
            }
            catch (final URISyntaxException e)
            {

            }

            _RelId = linkRelationUri;

            // isolate()
            {
                final ObjectNode targetSchemaNode = PropertyType.TargetSchema.getValueNode(getLinkNode());
                if (targetSchemaNode == null)
                {
                    _TargetSchemaId = null;
                }
                else
                {
                    _TargetSchemaId = PropertyType.$Ref.getValue(targetSchemaNode, syntaxLoader);
                }
            }

            // isolate()
            {

                final ObjectNode schemaNode = PropertyType.Schema.getValueNode(getLinkNode());
                if (schemaNode == null)
                {
                    _SchemaId = null;
                }
                else
                {
                    _SchemaId = PropertyType.$Ref.getValue(schemaNode, syntaxLoader);
                }
            }

        }

        public JsonSchema getJsonSchema()
        {

            return _JsonSchema;
        }

        public ObjectNode getLinkNode()
        {

            return _LinkNode;
        }

        public String getRel()
        {

            return _Rel;
        }

        public URI getRelId()
        {

            return _RelId;
        }

        public URI getSchemaId()
        {

            return _SchemaId;
        }

        public URI getTargetSchemaId()
        {

            return _TargetSchemaId;
        }

    }

    public static final class Property
    {

        private final JsonSchema _JsonSchema;

        private final String _Name;

        private final ObjectNode _PropertyNode;

        private final JsonType _JsonType;

        public Property(final JsonSchema jsonSchema, final String name, final ObjectNode propertyNode)
                throws IOException
        {

            if (jsonSchema == null || name == null || propertyNode == null)
            {
                throw new NullPointerException();
            }

            _JsonSchema = jsonSchema;
            _Name = name;

            final boolean isReference = isReference(propertyNode);
            if (isReference)
            {
                final JsonSchema referencedJsonSchema = resolveReference(propertyNode);
                _PropertyNode = referencedJsonSchema.getRootNode();
            }
            else
            {
                _PropertyNode = propertyNode;
            }

            final JsonNode typeNode = PropertyType.Type.getValueNode(_PropertyNode);

            if (typeNode != null)
            {
                if (typeNode.isTextual())
                {
                    final String typeName = typeNode.textValue();
                    _JsonType = Definitions.JsonType.forKeyword(typeName);
                }
                else if (typeNode instanceof ArrayNode)
                {
                    _JsonType = JsonType.String;
                }
                else
                {
                    _JsonType = null;
                }
            }
            else
            {
                _JsonType = null;
            }

        }

        public JsonSchema getJsonSchema()
        {

            return _JsonSchema;
        }

        public JsonType getJsonType()
        {

            return _JsonType;
        }

        public String getName()
        {

            return _Name;
        }

        public ObjectNode getPropertyNode()
        {

            return _PropertyNode;
        }
        
        void setRequired(final Boolean required)
        {
            
        }

        public PropertyType getPropertyType()
        {

            return PropertyType.forName(getName());
        }

        public <T> T getValue(final PropertyType propertyType)
        {

            final SyntaxLoader syntaxLoader = getJsonSchema().getLoader().getContext().getSyntaxLoader();
            return propertyType.getValue(getPropertyNode(), syntaxLoader);
        }

        public JsonNode getValueNode(final PropertyType propertyType)
        {

            return propertyType.getValueNode(getPropertyNode());
        }

        private boolean isReference(final JsonNode node)
        {

            final JsonNode refNode = PropertyType.$Ref.getValueNode(node);
            if (refNode == null)
            {
                return false;
            }
            final JsonSchemaLoader jsonSchemaLoader = getJsonSchema().getLoader();
            final SyntaxLoader syntaxLoader = jsonSchemaLoader.getContext().getSyntaxLoader();
            final URI refUri = PropertyType.$Ref.getValue(node, syntaxLoader);
            return (refUri != null);

        }

        private JsonSchema resolveReference(final JsonNode node) throws IOException
        {

            final JsonNode refNode = PropertyType.$Ref.getValueNode(node);
            if (refNode == null)
            {
                return null;
            }

            final JsonSchema jsonSchema = getJsonSchema();
            final JsonSchemaLoader jsonSchemaLoader = jsonSchema.getLoader();
            final SyntaxLoader syntaxLoader = jsonSchemaLoader.getContext().getSyntaxLoader();
            final URI refUri = PropertyType.$Ref.getValue(node, syntaxLoader);
            if (refUri == null)
            {
                return null;
            }

            final URI resolvedUri;
            if (refUri.isAbsolute())
            {
                resolvedUri = refUri;
            }
            else
            {
                resolvedUri = jsonSchema.getId().resolve(refUri);
            }

            final JsonSchema referencedSchema = jsonSchemaLoader.load(resolvedUri);

            return referencedSchema;
        }

    }

}

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
package org.wrml.runtime.schema.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.MaybeReadOnly;
import org.wrml.model.MaybeRequired;
import org.wrml.model.Model;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.rest.Method;
import org.wrml.model.schema.*;
import org.wrml.runtime.Context;
import org.wrml.runtime.format.application.schema.json.JsonSchema;
import org.wrml.runtime.format.application.schema.json.JsonSchema.Definitions.JsonType;
import org.wrml.runtime.format.application.schema.json.JsonSchema.Definitions.PropertyType;
import org.wrml.runtime.format.application.schema.json.JsonSchema.JsonStringFormat;
import org.wrml.runtime.format.application.schema.json.JsonSchema.Property;
import org.wrml.runtime.format.application.schema.json.JsonSchemaLoader;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.schema.*;
import org.wrml.runtime.syntax.SyntaxHandler;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.util.JavaBean;
import org.wrml.util.UniqueName;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Implements the transformation of Schema to Java Class (.class bytecode).
 * </p>
 */
public class SchemaGenerator implements Opcodes
{


    private static final Logger LOG = LoggerFactory.getLogger(SchemaGenerator.class);

    private static final String ANNONYMOUS_INNER_SCHEMA_PREFIX = "$";

    private static final String ANNOTATION_INTERNAL_NAME_ALIASES = SchemaGenerator.classToInternalTypeName(Aliases.class);

    private static final String ANNOTATION_INTERNAL_NAME_COLLECTION_SLOT = SchemaGenerator.classToInternalTypeName(CollectionSlot.class);

    private static final String ANNOTATION_INTERNAL_NAME_COLLECTION_SLOT_CRITERION = SchemaGenerator.classToInternalTypeName(CollectionSlotCriterion.class);

    private static final String ANNOTATION_INTERNAL_NAME_DEFAULT_VALUE = SchemaGenerator.classToInternalTypeName(DefaultValue.class);

    private static final String ANNOTATION_INTERNAL_NAME_DESCRIPTION = SchemaGenerator.classToInternalTypeName(Description.class);

    private static final String ANNOTATION_INTERNAL_NAME_DISALLOWED_VALUES = SchemaGenerator.classToInternalTypeName(DisallowedValues.class);

    private static final String ANNOTATION_INTERNAL_NAME_DIVISIBLE_BY_VALUE = SchemaGenerator.classToInternalTypeName(DivisibleByValue.class);

    private static final String ANNOTATION_INTERNAL_NAME_LINK_SLOT = SchemaGenerator.classToInternalTypeName(LinkSlot.class);

    private static final String ANNOTATION_INTERNAL_NAME_LINK_SLOT_BINDING = SchemaGenerator.classToInternalTypeName(LinkSlotBinding.class);

    private static final String ANNOTATION_INTERNAL_NAME_MAXIMUM_LENGTH = SchemaGenerator.classToInternalTypeName(MaximumLength.class);

    private static final String ANNOTATION_INTERNAL_NAME_MAXIMUM_SIZE = SchemaGenerator.classToInternalTypeName(MaximumSize.class);

    private static final String ANNOTATION_INTERNAL_NAME_MAXIMUM_VALUE = SchemaGenerator.classToInternalTypeName(MaximumValue.class);

    private static final String ANNOTATION_INTERNAL_NAME_MINIMUM_LENGTH = SchemaGenerator.classToInternalTypeName(MinimumLength.class);

    private static final String ANNOTATION_INTERNAL_NAME_MINIMUM_SIZE = SchemaGenerator.classToInternalTypeName(MinimumSize.class);

    private static final String ANNOTATION_INTERNAL_NAME_MINIMUM_VALUE = SchemaGenerator.classToInternalTypeName(MinimumValue.class);

    private static final String ANNOTATION_INTERNAL_NAME_MULTILINE = SchemaGenerator.classToInternalTypeName(Multiline.class);

    private static final String ANNOTATION_INTERNAL_NAME_NON_NULL = SchemaGenerator.classToInternalTypeName(NonNull.class);

    private static final String ANNOTATION_INTERNAL_NAME_READ_ONLY = SchemaGenerator.classToInternalTypeName(ReadOnly.class);

    private static final String ANNOTATION_INTERNAL_NAME_TAGS = SchemaGenerator.classToInternalTypeName(Tags.class);

    private static final String ANNOTATION_INTERNAL_NAME_THUMBNAIL_IMAGE = SchemaGenerator.classToInternalTypeName(ThumbnailImage.class);

    private static final String ANNOTATION_INTERNAL_NAME_TITLE = SchemaGenerator.classToInternalTypeName(Title.class);

    private static final String ANNOTATION_INTERNAL_NAME_VERSION = SchemaGenerator.classToInternalTypeName(Version.class);

    private static final String ANNOTATION_INTERNAL_NAME_WRML = SchemaGenerator.classToInternalTypeName(WRML.class);

    private static final String CLOSE_PARENTHESIS = ")";

    private static final String EMPTY_PARENTHESES = SchemaGenerator.OPEN_PARENTHESIS + SchemaGenerator.CLOSE_PARENTHESIS;

    private static final String CLOSED_METHOD_DESCRIPTOR = "()V";

    private static final String INIT_METHOD_NAME = "<init>";

    private static final String CLINIT_METHOD_NAME = "<clinit>";

    private static final String VALUES_METHOD_NAME = "values";

    private static final String VALUE_OF_METHOD_NAME = "valueOf";

    private static final String $VALUES_CONSTANT_NAME = "$VALUES";

    private static final String CLONE_METHOD_NAME = "clone";

    /**
     * The access modifiers for Java interfaces.
     */
    private static final int INTERFACE_ACCESS = Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE;

    /**
     * The access modifiers for Java interface methods.
     */
    private static final int INTERFACE_METHOD_ACCESS = Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT;

    private static final String OBJECT_INTERNAL_NAME = "java/lang/Object";

    private static final String CLASS_INTERNAL_NAME = "java/lang/Class";

    private static final String STRING_INTERNAL_NAME = "java/lang/String";

    private static final String ENUM_INTERNAL_NAME = "java/lang/Enum";

    private static final String INIT_METHOD_DESCRIPTOR = "(L" + STRING_INTERNAL_NAME + ";I)V";

    private static final String STRING_PARAM_METHOD_DESCRIPTOR_PREFIX = "(L" + STRING_INTERNAL_NAME + ";)";

    private static final String RETURN_OBJECT_METHOD_DESCRIPTOR = "()L" + OBJECT_INTERNAL_NAME + ";";

    private static final String VALUE_OF_METHOD_DESCRIPTOR = "(L" + CLASS_INTERNAL_NAME + ";L" + STRING_INTERNAL_NAME + ";)L" + ENUM_INTERNAL_NAME + ";";

    /**
     * JVM class file API version
     */
    private static final int JVM_VERSION = Opcodes.V1_5;

    private static final String MODEL_INTERFACE_INTERNAL_NAME = SchemaGenerator.classToInternalTypeName(Model.class);

    private static final String OPEN_PARENTHESIS = "(";


    private final JavaBean _ModelJavaBean;

    private final SchemaLoader _SchemaLoader;

    private final ConcurrentHashMap<URI, Integer> _InnerSchemaCountMap;

    /**
     * Create a new SchemaGenerator for the specified SchemaLoader.
     *
     * @param schemaLoader The SchemaLoader (a ClassLoader) instance that will use this SchemaGenerator to load Schema Java interfaces from Schema models and other sources.
     */
    public SchemaGenerator(final SchemaLoader schemaLoader)
    {

        _SchemaLoader = schemaLoader;
        if (_SchemaLoader == null)
        {
            throw new NullPointerException("The SchemaLoader is null");
        }

        _ModelJavaBean = new JavaBean(ValueType.JAVA_TYPE_MODEL, null);

        _InnerSchemaCountMap = new ConcurrentHashMap<URI, Integer>();

    }

    public static final String classToInternalTypeName(final Class<?> clazz)
    {

        return SchemaGenerator.externalTypeNameToInternalTypeName(clazz.getCanonicalName());
    }

    public static final String externalTypeNameToInternalTypeName(final String externalTypeName)
    {

        return externalTypeName.replace('.', '/');
    }

    public static final String internalTypeNameToExternalTypeName(final String internalTypeName)
    {

        return internalTypeName.replace('/', '.');
    }

    public Choices generateChoices(final Class<?> nativeChoicesEnumClass)
    {

        if (nativeChoicesEnumClass == null || !nativeChoicesEnumClass.isEnum())
        {
            return null;
        }

        final SchemaLoader schemaLoader = getSchemaLoader();
        final Choices choices = getContext().newModel(schemaLoader.getChoicesSchemaUri());

        final URI choicesUri = schemaLoader.getTypeUri(nativeChoicesEnumClass);
        choices.setUri(choicesUri);

        final UniqueName choicesUniqueName = schemaLoader.getTypeUniqueName(choicesUri);
        choices.setUniqueName(choicesUniqueName);

        final Object[] enumConstants = nativeChoicesEnumClass.getEnumConstants();
        if (enumConstants != null && enumConstants.length > 0)
        {
            final LinkedHashSet choiceSet = new LinkedHashSet(enumConstants.length);

            for (final Object enumConstant : enumConstants)
            {
                final String choice = String.valueOf(enumConstant);
                choiceSet.add(enumConstant);
            }

            choices.getList().addAll(choiceSet);
        }


        return choices;
    }

    /**
     * Yay Bytecode!
     */
    public JavaBytecodeClass generateChoicesEnum(final Choices choices)
    {


        if (choices == null)
        {
            return null;
        }

        final List<String> choicesList = choices.getList();

        final URI choicesUri = choices.getUri();
        final String enumName = uriToInternalTypeName(choicesUri);
        final String enumTypeName = "L" + enumName + ";";
        final String enumArrayTypeName = "[" + enumTypeName;


        final JavaBytecodeClass enumByteCodeClass = new JavaBytecodeClass(enumName);
        final JavaBytecodeAnnotation wrmlAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_WRML);
        wrmlAnnotation.setAttributeValue(AnnotationParameterName.uniqueName.name(), choices.getUniqueName().getFullName());
        enumByteCodeClass.getAnnotations().add(wrmlAnnotation);

        final ClassWriter classWriter = new ClassWriter(0);

        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;

        classWriter.visit(51, ACC_PUBLIC + ACC_FINAL + ACC_SUPER + ACC_ENUM, enumName, "L" + ENUM_INTERNAL_NAME + "<" + enumTypeName + ">;", ENUM_INTERNAL_NAME, null);

        {
            for (final String choice : choicesList)
            {
                fieldVisitor = classWriter.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC + ACC_ENUM, choice, enumTypeName, null, null);
                fieldVisitor.visitEnd();
            }
        }

        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC + ACC_SYNTHETIC, $VALUES_CONSTANT_NAME, enumArrayTypeName, null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, VALUES_METHOD_NAME, OPEN_PARENTHESIS + CLOSE_PARENTHESIS + enumArrayTypeName, null, null);
            methodVisitor.visitCode();
            methodVisitor.visitFieldInsn(GETSTATIC, enumName, $VALUES_CONSTANT_NAME, enumArrayTypeName);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, enumArrayTypeName, CLONE_METHOD_NAME, RETURN_OBJECT_METHOD_DESCRIPTOR);
            methodVisitor.visitTypeInsn(CHECKCAST, enumArrayTypeName);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(1, 0);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, VALUE_OF_METHOD_NAME, STRING_PARAM_METHOD_DESCRIPTOR_PREFIX + enumTypeName, null, null);
            methodVisitor.visitCode();
            methodVisitor.visitLdcInsn(org.objectweb.asm.Type.getType(enumTypeName));
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESTATIC, ENUM_INTERNAL_NAME, VALUE_OF_METHOD_NAME, VALUE_OF_METHOD_DESCRIPTOR);
            methodVisitor.visitTypeInsn(CHECKCAST, enumName);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(2, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PRIVATE, INIT_METHOD_NAME, INIT_METHOD_DESCRIPTOR, CLOSED_METHOD_DESCRIPTOR, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, ENUM_INTERNAL_NAME, INIT_METHOD_NAME, INIT_METHOD_DESCRIPTOR);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(3, 3);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_STATIC, CLINIT_METHOD_NAME, CLOSED_METHOD_DESCRIPTOR, null, null);
            methodVisitor.visitCode();

            int constantIndex = 0;
            for (final String choice : choicesList)
            {
                methodVisitor.visitTypeInsn(NEW, enumName);
                methodVisitor.visitInsn(DUP);
                methodVisitor.visitLdcInsn(choice);

                visitConstantIncrement(methodVisitor, constantIndex);
                constantIndex++;

                methodVisitor.visitMethodInsn(INVOKESPECIAL, enumName, INIT_METHOD_NAME, INIT_METHOD_DESCRIPTOR);
                methodVisitor.visitFieldInsn(PUTSTATIC, enumName, choice, enumTypeName);
            }

            methodVisitor.visitIntInsn(BIPUSH, constantIndex);
            methodVisitor.visitTypeInsn(ANEWARRAY, enumName);


            constantIndex = 0;
            for (final String choice : choicesList)
            {
                methodVisitor.visitInsn(DUP);

                visitConstantIncrement(methodVisitor, constantIndex);
                constantIndex++;

                methodVisitor.visitFieldInsn(GETSTATIC, enumName, choice, enumTypeName);
                methodVisitor.visitInsn(AASTORE);
            }

            methodVisitor.visitFieldInsn(PUTSTATIC, enumName, $VALUES_CONSTANT_NAME, enumArrayTypeName);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(4, 0);
            methodVisitor.visitEnd();

        }
        classWriter.visitEnd();

        final byte[] bytecode = classWriter.toByteArray();

        enumByteCodeClass.setBytecode(bytecode);

        return enumByteCodeClass;
    }

    private void visitConstantIncrement(final MethodVisitor methodVisitor, final int constantIndex)
    {

        switch (constantIndex)
        {

            case 0:
                methodVisitor.visitInsn(ICONST_0);
                break;

            case 1:
                methodVisitor.visitInsn(ICONST_1);
                break;

            case 2:
                methodVisitor.visitInsn(ICONST_2);
                break;

            case 3:
                methodVisitor.visitInsn(ICONST_3);
                break;

            case 4:
                methodVisitor.visitInsn(ICONST_4);
                break;

            case 5:
                methodVisitor.visitInsn(ICONST_5);
                break;

            default:
                methodVisitor.visitIntInsn(BIPUSH, constantIndex);
                break;
        }

    }

    public Schema generateSchema(final JsonSchema jsonSchema, final URI... baseSchemaUris)
            throws SchemaGeneratorException
    {

        final URI schemaUri = jsonSchema.getId();
        final Context context = getContext();

        final Schema schema = context.newModel(getSchemaLoader().getSchemaDimensions());
        schema.setUri(schemaUri);

        final UniqueName literalUniqueName = JsonSchema.createJsonSchemaUniqueName(schemaUri);
        final String namespace = literalUniqueName.getNamespace();
        final String localName = StringUtils.capitalize(literalUniqueName.getLocalName());
        final UniqueName uniqueName = new UniqueName(namespace, localName);
        schema.setUniqueName(uniqueName);
        final String schemaDescription = jsonSchema.getDescription();
        if (schemaDescription != null)
        {
            schema.setDescription(schemaDescription);
        }

        final String schemaTitle = uniqueName.getLocalName();
        if (schemaTitle != null)
        {
            schema.setTitle(schemaTitle);
        }

        final Set<URI> baseSchemaUriSet = new HashSet<>();
        if (baseSchemaUris != null)
        {
            baseSchemaUriSet.addAll(Arrays.asList(baseSchemaUris));
        }

        baseSchemaUriSet.addAll(jsonSchema.getExtendedSchemaUris());
        schema.getBaseSchemaUris().addAll(baseSchemaUriSet);

        final List<Slot> slots = schema.getSlots();
        final Map<String, JsonSchema.Property> properties = jsonSchema.getProperties();

        for (final String name : properties.keySet())
        {

            final JsonSchema.Property property = properties.get(name);

            final Slot slot = context.newModel(Slot.class);
            slot.setName(name);

            final String slotDescription = property.getValue(PropertyType.Description);
            if (slotDescription != null)
            {
                slot.setDescription(slotDescription);
            }
            final String slotTitle = property.getValue(PropertyType.Title);
            if (slotTitle != null)
            {
                slot.setTitle(slotTitle);
            }

            final Value value = generateValue(jsonSchema, property);

            if (value == null)
            {
                throw new SchemaGeneratorException("Failed to generate a Value for slot: " + name, null, this);
            }

            slot.setValue(value);

            slots.add(slot);

        }

        final List<JsonSchema.Link> links = jsonSchema.getLinks();
        for (final JsonSchema.Link jsonSchemaLink : links)
        {
            final Slot linkSlot = context.newModel(Slot.class);
            final URI linkRelationUri = jsonSchemaLink.getRelId();
            if (linkRelationUri == null)
            {
                continue;
            }

            final LinkRelation linkRelation = getContext().getApiLoader().loadLinkRelation(linkRelationUri);
            if (linkRelation == null)
            {
                continue;
            }
            final String linkSlotName = linkRelation.getUniqueName().getLocalName();
            if (linkSlotName == null)
            {
                continue;
            }

            linkSlot.setName(linkSlotName);

            final LinkValue linkValue = context.newModel(LinkValue.class);
            linkSlot.setValue(linkValue);
            linkValue.setLinkRelationUri(linkRelationUri);

            final URI targetSchemaUri = jsonSchemaLink.getTargetSchemaId();
            if (targetSchemaUri != null)
            {
                linkValue.setResponseSchemaUri(targetSchemaUri);
            }

            final URI paramSchemaUri = jsonSchemaLink.getSchemaId();
            linkValue.setRequestSchemaUri(paramSchemaUri);

            slots.add(linkSlot);
        }

        final ObjectNode rootNode = jsonSchema.getRootNode();
        if (rootNode.has(Schema.SLOT_NAME_KEY_SLOT_NAMES))
        {
            final JsonNode keySlotNamesJsonNode = rootNode.get(Schema.SLOT_NAME_KEY_SLOT_NAMES);

            if (keySlotNamesJsonNode instanceof ArrayNode)
            {
                final ArrayNode keySlotNamesArrayNode = (ArrayNode) keySlotNamesJsonNode;
                final Iterator<JsonNode> elements = keySlotNamesArrayNode.elements();
                while (elements.hasNext())
                {
                    final JsonNode keySlotNameJsonNode = elements.next();
                    final String keySlotName = keySlotNameJsonNode.asText();
                    schema.getKeySlotNames().add(keySlotName);
                }
            }
        }

        LOG.debug("Generated WRML Schema from JSON Schema (" + schema.getUri() + "):\n" + schema);

        return schema;

    }

    @SuppressWarnings("unchecked")
    public Schema generateSchema(final Prototype prototype)
    {

        final URI schemaUri = prototype.getSchemaUri();
        final Context context = getContext();

        final Schema schema = context.newModel(getSchemaLoader().getSchemaDimensions());
        schema.setUniqueName(prototype.getUniqueName());
        schema.setDescription(prototype.getDescription());

        final boolean isReadOnly = prototype.isReadOnly();
        if (isReadOnly)
        {
            schema.setReadOnly(true);
        }

        schema.setTitle(prototype.getTitle());
        schema.setThumbnailLocation(prototype.getThumbnailLocation());
        schema.setVersion(prototype.getVersion());
        schema.setTitleSlotName(prototype.getTitleSlotName());

        schema.setUri(schemaUri);

        final Set<URI> prototypeBaseSchemaUris = prototype.getDeclaredBaseSchemaUris();
        if (prototypeBaseSchemaUris != null)
        {
            schema.getBaseSchemaUris().addAll(prototypeBaseSchemaUris);
        }

        final SortedSet<String> prototypeKeySlotNames = prototype.getDeclaredKeySlotNames();
        if (prototypeKeySlotNames != null)
        {
            schema.getKeySlotNames().addAll(prototypeKeySlotNames);
        }

        final Set<String> prototypeComparableSlotNames = prototype.getComparableSlotNames();
        if (prototypeComparableSlotNames != null)
        {
            schema.getComparableSlotNames().addAll(prototypeComparableSlotNames);
        }

        final SortedSet<String> prototypeTags = prototype.getTags();
        if (prototypeTags != null)
        {
            schema.getTags().addAll(prototypeTags);
        }

        final List<Slot> slots = schema.getSlots();
        final SortedSet<String> prototypeAllSlotNames = prototype.getAllSlotNames();
        if (prototypeAllSlotNames != null)
        {

            for (final String name : prototypeAllSlotNames)
            {
                final ProtoSlot protoSlot = prototype.getProtoSlot(name);
                final URI slotDeclaredSchemaUri = protoSlot.getDeclaringSchemaUri();
                if (!schemaUri.equals(slotDeclaredSchemaUri))
                {
                    continue;
                }

                final Slot slot = context.newModel(Slot.class);
                slot.setName(name);

                final SortedSet<String> aliases = protoSlot.getAliases();
                if (aliases != null)
                {
                    slot.getAliases().addAll(aliases);
                }

                slot.setDescription(protoSlot.getDescription());
                slot.setTitle(protoSlot.getTitle());

                final Type slotHeapValueType = protoSlot.getHeapValueType();
                final Value value = generateValue(slotHeapValueType, protoSlot);

                if (value == null)
                {
                    throw new SchemaGeneratorException("Failed to generate a Value for slot: " + protoSlot, null, this);
                }

                if (protoSlot instanceof PropertyProtoSlot)
                {
                    final PropertyProtoSlot propertyProtoSlot = (PropertyProtoSlot) protoSlot;
                    final Object propertyDefaultValue = propertyProtoSlot.getDefaultValue();
                    final ValueType propertyValueType = propertyProtoSlot.getValueType();
                    final Object valueTypeDefaultValue = propertyValueType.getDefaultValue();
                    if (propertyDefaultValue != null && !propertyDefaultValue.equals(valueTypeDefaultValue))
                    {
                        value.setSlotValue(Value.SLOT_NAME_DEFAULT, propertyDefaultValue);
                    }

                    @SuppressWarnings("rawtypes")
                    final Set disallowedValues = propertyProtoSlot.getDisallowedValues();
                    if (disallowedValues != null && !disallowedValues.isEmpty())
                    {
                        propertyProtoSlot.getDisallowedValues().addAll(disallowedValues);
                    }

                    final Object divisibleByValue = propertyProtoSlot.getDivisibleByValue();
                    if (divisibleByValue != null)
                    {
                        value.setSlotValue(NumericValue.SLOT_NAME_DIVISIBLE_BY, divisibleByValue);
                    }

                    final Integer maximumLength = propertyProtoSlot.getMaximumLength();
                    if (maximumLength != null)
                    {
                        value.setSlotValue(TextValue.SLOT_NAME_MAXIMUM_LENGTH, maximumLength);
                    }

                    final Integer maximumSize = propertyProtoSlot.getMaximumSize();
                    if (maximumSize != null)
                    {
                        value.setSlotValue(ListValue.SLOT_NAME_MAXIMUM_SIZE, maximumSize);
                    }

                    final Object maximumValue = propertyProtoSlot.getMaximumValue();
                    if (maximumValue != null)
                    {
                        value.setSlotValue(NumericValue.SLOT_NAME_MAXIMUM, maximumValue);
                    }

                    final Integer minimumLength = propertyProtoSlot.getMinimumLength();
                    if (minimumLength != null)
                    {
                        value.setSlotValue(TextValue.SLOT_NAME_MINIMUM_LENGTH, minimumLength);
                    }

                    final Integer minimumSize = propertyProtoSlot.getMinimumSize();
                    if (minimumSize != null)
                    {
                        value.setSlotValue(ListValue.SLOT_NAME_MINIMUM_SIZE, minimumSize);
                    }

                    final Object minimumValue = propertyProtoSlot.getMinimumValue();
                    if (minimumValue != null)
                    {
                        value.setSlotValue(NumericValue.SLOT_NAME_MINIMUM, minimumValue);
                    }

                    final boolean isMultiline = propertyProtoSlot.isMultiline();
                    if (isMultiline)
                    {
                        value.setSlotValue(TextValue.SLOT_NAME_MULTILINE, isMultiline);
                    }

                    if (slotHeapValueType instanceof Class<?>
                            && Set.class.isAssignableFrom((Class<?>) slotHeapValueType))
                    {
                        value.setSlotValue(ListValue.SLOT_NAME_ELEMENT_UNIQUENESS_CONSTRAINED, Boolean.TRUE);
                    }

                }


                slot.setValue(value);

                slots.add(slot);

            }
        }

        return schema;
    }

    /**
     * Generate a {@link JavaBytecodeClass} from the specified {@link Schema}.
     * <p/>
     * Like a *big* regex (regular expression), we can compile all of the
     * WRML schema metadata (as if it is a single *big* String input) into a
     * loaded Java class so that the rest of the WRML *runtime* can use
     * (Prototype-optimized) reflection to access WRML's type system.
     *
     * @param schema The Schema to represent as a Java class.
     * @return The Java class representation of the specified schema.
     */
    public JavaBytecodeClass generateSchemaInterface(final Schema schema)
    {

        /*
         * Create the simple POJO that will return the transformation
         * information. By the end of this method, this will be full of Java
         * bytecode-oriented information gleaned from this method's schema
         * parameter.
         */
        final JavaBytecodeClass javaBytecodeClass = new JavaBytecodeClass();
        final JavaBytecodeAnnotation wrmlAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_WRML);
        wrmlAnnotation.setAttributeValue(AnnotationParameterName.uniqueName.name(), schema.getUniqueName().getFullName());
        javaBytecodeClass.getAnnotations().add(wrmlAnnotation);

        final SortedSet<String> keySlotNameSet = new TreeSet<>();

        /*
         * If the schema declares any key slots, note them with a
         * class-level annotation.
         */
        final List<String> keySlotNames = schema.getKeySlotNames();
        if (keySlotNames != null && keySlotNames.size() > 0)
        {
            keySlotNameSet.addAll(keySlotNames);
        }

        if (!keySlotNameSet.isEmpty())
        {
            final String[] keySlotNamesArray = new String[keySlotNameSet.size()];
            wrmlAnnotation.setAttributeValue(AnnotationParameterName.keySlotNames.name(), keySlotNameSet.toArray(keySlotNamesArray));
        }

        /*
         * If the schema declares any comparable slots, note them with a
         * class-level annotation.
         */
        final List<String> comparableSlotNames = schema.getComparableSlotNames();
        if (comparableSlotNames != null && comparableSlotNames.size() > 0)
        {
            final String[] comparableSlotNamesArray = new String[comparableSlotNames.size()];
            wrmlAnnotation.setAttributeValue(AnnotationParameterName.comparableSlotNames.name(), comparableSlotNames.toArray(comparableSlotNamesArray));
        }

        wrmlAnnotation.setAttributeValue(AnnotationParameterName.titleSlotName.name(), schema.getTitleSlotName());


        /*
         * In Java, all interfaces extend java.lang.Object, so this can
         * remain constant for Schema too.
         */
        javaBytecodeClass.setSuperName(SchemaGenerator.OBJECT_INTERNAL_NAME);

        /*
         * Go from schema id (URI) to internal Java class name. This is a
         * simple matter of stripping the leading forward slash from the
         * URI's path. Internally (in the bytecode), Java's class names use
         * forward slash (/) instead of full stop dots (.).
         */
        final URI schemaUri = schema.getUri();
        final String interfaceInternalName = uriToInternalTypeName(schemaUri);
        // if (schema.getUniqueName() == null)
        // {
        // schema.setUniqueName(new UniqueName(schemaUri.getPath()));
        // }

        javaBytecodeClass.setInternalName(interfaceInternalName);

        /*
         * Add the class-level Description annotation to capture the
         * schema's description.
         */
        final String schemaDescription = schema.getDescription();
        if (schemaDescription != null && !schemaDescription.trim().isEmpty())
        {
            final JavaBytecodeAnnotation schemaDescriptionAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_DESCRIPTION);
            schemaDescriptionAnnotation.setAttributeValue(AnnotationParameterName.value.name(), schemaDescription);
            javaBytecodeClass.getAnnotations().add(schemaDescriptionAnnotation);
        }

        String schemaTitle = schema.getTitle();
        if (schemaTitle == null || schemaTitle.trim().isEmpty())
        {
            schemaTitle = schema.getUniqueName().getLocalName();
        }

        final JavaBytecodeAnnotation schemaTitleAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_TITLE);
        schemaTitleAnnotation.setAttributeValue(AnnotationParameterName.value.name(), schemaTitle);
        javaBytecodeClass.getAnnotations().add(schemaTitleAnnotation);

        final URI schemaThumbnailImageLocation = schema.getThumbnailLocation();
        if (schemaThumbnailImageLocation != null)
        {
            final JavaBytecodeAnnotation schemaThumbnailImageAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_THUMBNAIL_IMAGE);
            schemaThumbnailImageAnnotation.setAttributeValue(AnnotationParameterName.value.name(),
                    schemaThumbnailImageLocation.toString());
            javaBytecodeClass.getAnnotations().add(schemaThumbnailImageAnnotation);
        }


        boolean isAggregate = false;

        /*
         * Turn the schema's base schema list into our Java class's base
         * (aka extended) interfaces.
         */
        final List<URI> baseSchemaUris = schema.getBaseSchemaUris();
        for (final URI baseSchemaUri : baseSchemaUris)
        {
            final String baseSchemaInternalName = uriToInternalTypeName(baseSchemaUri);
            javaBytecodeClass.getInterfaces().add(baseSchemaInternalName);


            if (!isAggregate && getSchemaLoader().getAggregateDocumentSchemaUri().equals(baseSchemaUri))
            {
                isAggregate = true;

                final List<Slot> slots = schema.getSlots();
                for (final Slot slot : slots)
                {
                    final Value value = slot.getValue();
                    if (!(value instanceof LinkValue || value instanceof ModelValue || value instanceof MultiSelectValue))
                    {
                        keySlotNameSet.add(slot.getName());
                    }
                }
            }
        }

        // Add the Model base interface
        javaBytecodeClass.getInterfaces().add(SchemaGenerator.MODEL_INTERFACE_INTERNAL_NAME);


        /*
         * Add the class-level Tags annotation to capture the schema's tags.
         */
        final List<String> schemaTags = schema.getTags();
        if (schemaTags != null && schemaTags.size() > 0)
        {
            final JavaBytecodeAnnotation tagsAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_TAGS);
            final String[] tagsArray = new String[schemaTags.size()];
            tagsAnnotation.setAttributeValue(AnnotationParameterName.value.name(), schemaTags.toArray(tagsArray));
            javaBytecodeClass.getAnnotations().add(tagsAnnotation);
        }

        final Long schemaVersion = schema.getVersion();
        if (schemaVersion != null)
        {
            final JavaBytecodeAnnotation versionAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_VERSION);
            versionAnnotation.setAttributeValue(AnnotationParameterName.value.name(), schemaVersion);
            javaBytecodeClass.getAnnotations().add(versionAnnotation);
        }

        final Boolean maybeReadOnly = schema.isReadOnly();
        if (maybeReadOnly != null && maybeReadOnly)
        {
            final JavaBytecodeAnnotation readOnlyAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_READ_ONLY);
            javaBytecodeClass.getAnnotations().add(readOnlyAnnotation);
        }

        /*
         * Generate the interface method signatures.
         */
        generateSchemaInterfaceMethods(schema, javaBytecodeClass, isAggregate);

        // TODO: "Open slots" with signatures. Track the open slots via
        // the JavaBytecode types.

        //
        // TODO: The signature will need to be changed for generics:
        // Example:
        //
        // Java: public interface Test<T extends List<?>> extends List<T>
        // Class File: public abstract interface org.wrml.schema.Test
        // extends java.util.List
        // Signature:
        // <T::Ljava/util/List<*>;>Ljava/lang/Object;Ljava/util/List<TT;>;
        //

        javaBytecodeClass.setSignature(null);

        generateSchemaInterfaceBytecode(javaBytecodeClass);
        return javaBytecodeClass;
    }

    public Context getContext()
    {

        return getSchemaLoader().getContext();
    }

    public SchemaLoader getSchemaLoader()
    {

        return _SchemaLoader;
    }

    private void addAnnotations(final JavaBytecodeMethod method, final JavaBytecodeAnnotation... annotations)
    {

        if (annotations != null && annotations.length > 0)
        {
            final List<JavaBytecodeAnnotation> methodAnnotations = method.getAnnotations();
            for (final JavaBytecodeAnnotation annotation : annotations)
            {
                if (annotation != null)
                {
                    methodAnnotations.add(annotation);
                }
            }
        }
    }

    private String generateArgs(final String... args)
    {

        if (args == null || args.length == 0)
        {
            return SchemaGenerator.EMPTY_PARENTHESES;
        }
        else
        {
            StringBuilder argsStringBuilder = null;

            for (final String arg : args)
            {
                if (arg == null)
                {
                    continue;
                }

                if (argsStringBuilder == null)
                {
                    argsStringBuilder = new StringBuilder();
                    argsStringBuilder.append(SchemaGenerator.OPEN_PARENTHESIS);
                }

                argsStringBuilder.append(arg);

            }

            if (argsStringBuilder != null)
            {
                argsStringBuilder.append(SchemaGenerator.CLOSE_PARENTHESIS);
                return argsStringBuilder.toString();
            }

        }

        return null;
    }

    private String generateArgsDescriptor(final JavaBytecodeType... argumentTypes)
    {

        if (argumentTypes == null || argumentTypes.length == 0)
        {
            return generateArgs((String[]) null);
        }

        final List<String> argList = new ArrayList<String>();
        for (int i = 0; i < argumentTypes.length; i++)
        {
            final JavaBytecodeType argType = argumentTypes[i];
            if (argType != null)
            {
                argList.add(argType.getDescriptor());
            }
        }

        if (argList.size() > 0)
        {
            String[] args = new String[argList.size()];
            args = argList.toArray(args);
            return generateArgs(args);
        }

        return generateArgs((String[]) null);
    }

    private String generateArgsSignature(final JavaBytecodeType... argumentTypes)
    {

        if (argumentTypes == null || argumentTypes.length == 0)
        {
            return generateArgs((String[]) null);
        }

        final List<String> argList = new ArrayList<String>();
        for (int i = 0; i < argumentTypes.length; i++)
        {
            final JavaBytecodeType argType = argumentTypes[i];
            if (argType != null)
            {
                argList.add(argType.getGenericSignature());
            }
        }

        if (argList.size() > 0)
        {
            String[] args = new String[argList.size()];
            args = argList.toArray(args);
            return generateArgs(args);
        }

        return null;
    }

    private void generateBeanAccessorMethods(final JavaBytecodeClass javaBytecodeClass, final Schema schema, final boolean isAggregate, final Slot slot)
    {

        final String slotName = slot.getName();
        final Value slotValue = slot.getValue();
        final JavaBytecodeType type = getSlotType(javaBytecodeClass, schema, isAggregate, slot);

        /*
         * Following JavaBean property conventions, the suffix (end) of the
         * property accessor's method name will be the WRML schema slot's
         * name as mixed upper case.
         */
        final String methodNameSuffix = StringUtils.capitalize(StringUtils.deleteWhitespace(slotName));
        final String readMethodNamePrefix = (slotValue instanceof BooleanValue) ? JavaBean.IS : JavaBean.GET;
        final String readMethodName = readMethodNamePrefix + methodNameSuffix;

        JavaBytecodeAnnotation aliasAnnotation = null;
        JavaBytecodeAnnotation descriptionAnnotation = null;
        JavaBytecodeAnnotation titleAnnotation = null;
        JavaBytecodeAnnotation defaultValueAnnotation = null;

        final List<String> aliases = slot.getAliases();
        if (aliases != null && aliases.size() > 0)
        {
            aliasAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_ALIASES);
            final String[] aliasesArray = new String[aliases.size()];
            aliasAnnotation.setAttributeValue(AnnotationParameterName.value.name(), aliases.toArray(aliasesArray));
        }

        final String descriptionString = slot.getDescription();
        if (descriptionString != null && !descriptionString.isEmpty())
        {
            descriptionAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_DESCRIPTION);
            descriptionAnnotation.setAttributeValue(AnnotationParameterName.value.name(), descriptionString);
        }

        final String titleString = slot.getTitle();
        if (titleString != null && !titleString.isEmpty())
        {
            titleAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_TITLE);
            titleAnnotation.setAttributeValue(AnnotationParameterName.value.name(), titleString);
        }

        final String defaultValueString = getDefaultValueString(slotValue);
        if (defaultValueString != null && !defaultValueString.isEmpty())
        {
            defaultValueAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_DEFAULT_VALUE);
            defaultValueAnnotation.setAttributeValue(AnnotationParameterName.value.name(), defaultValueString);
        }

        JavaBytecodeAnnotation disallowedValuesAnnotation = null;
        JavaBytecodeAnnotation divisibleByAnnotation = null;
        JavaBytecodeAnnotation maximumLengthAnnotation = null;
        JavaBytecodeAnnotation maximumSizeAnnotation = null;
        JavaBytecodeAnnotation maximumValueAnnotation = null;
        JavaBytecodeAnnotation minimumLengthAnnotation = null;
        JavaBytecodeAnnotation minimumSizeAnnotation = null;
        JavaBytecodeAnnotation minimumValueAnnotation = null;
        JavaBytecodeAnnotation multilineAnnotation = null;

        JavaBytecodeAnnotation collectionSlotAnnotation = null;

        if (slotValue.containsSlotValue(Value.SLOT_NAME_DISALLOWED_VALUES))
        {

            final List<?> disallowedValues = (List<?>) slotValue.getSlotValue(Value.SLOT_NAME_DISALLOWED_VALUES);
            if (disallowedValues != null && disallowedValues.size() > 0)
            {
                disallowedValuesAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_DISALLOWED_VALUES);

                final String[] values = new String[disallowedValues.size()];

                for (int i = 0; i < values.length; i++)
                {
                    values[i] = String.valueOf(disallowedValues.get(i));
                }

                disallowedValuesAnnotation.setAttributeValue(AnnotationParameterName.value.name(), values);

            }

        }

        if (slotValue.containsSlotValue(TextValue.SLOT_NAME_MULTILINE) && slotValue instanceof TextValue)
        {
            final boolean isMultiline = (boolean) slotValue.getSlotValue(TextValue.SLOT_NAME_MULTILINE);
            if (isMultiline)
            {
                multilineAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_MULTILINE);
            }
        }

        if (slotValue.containsSlotValue(NumericValue.SLOT_NAME_DIVISIBLE_BY) && (slotValue instanceof IntegerValue || slotValue instanceof LongValue))
        {
            divisibleByAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_DIVISIBLE_BY_VALUE);
            divisibleByAnnotation.setAttributeValue(AnnotationParameterName.value.name(),
                    String.valueOf(slotValue.getSlotValue(NumericValue.SLOT_NAME_DIVISIBLE_BY)));

        }

        // Maximum values

        if (slotValue.containsSlotValue(TextValue.SLOT_NAME_MAXIMUM_LENGTH) && slotValue instanceof TextValue)
        {
            maximumLengthAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_MAXIMUM_LENGTH);
            maximumLengthAnnotation.setAttributeValue(AnnotationParameterName.value.name(),
                    (int) slotValue.getSlotValue(TextValue.SLOT_NAME_MAXIMUM_LENGTH));

        }
        else if (slotValue.containsSlotValue(ListValue.SLOT_NAME_MAXIMUM_SIZE) && slotValue instanceof ListValue)
        {
            maximumSizeAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_MAXIMUM_SIZE);
            maximumSizeAnnotation.setAttributeValue(AnnotationParameterName.value.name(),
                    (int) slotValue.getSlotValue(ListValue.SLOT_NAME_MAXIMUM_SIZE));

        }
        else if (slotValue.containsSlotValue(NumericValue.SLOT_NAME_MAXIMUM) && slotValue instanceof NumericValue)
        {
            maximumValueAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_MAXIMUM_VALUE);
            maximumValueAnnotation.setAttributeValue(AnnotationParameterName.value.name(),
                    String.valueOf(slotValue.getSlotValue(NumericValue.SLOT_NAME_MAXIMUM)));

            maximumValueAnnotation.setAttributeValue(AnnotationParameterName.exclusive.name(),
                    ((NumericValue) slotValue).isExclusiveMaximum());

        }

        // Minimum values

        if (slotValue.containsSlotValue(TextValue.SLOT_NAME_MINIMUM_LENGTH) && slotValue instanceof TextValue)
        {
            minimumLengthAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_MINIMUM_LENGTH);
            minimumLengthAnnotation.setAttributeValue(AnnotationParameterName.value.name(),
                    (int) slotValue.getSlotValue(TextValue.SLOT_NAME_MINIMUM_LENGTH));

        }
        else if (slotValue.containsSlotValue(ListValue.SLOT_NAME_MINIMUM_SIZE) && slotValue instanceof ListValue)
        {
            minimumSizeAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_MINIMUM_SIZE);
            minimumSizeAnnotation.setAttributeValue(AnnotationParameterName.value.name(),
                    (int) slotValue.getSlotValue(ListValue.SLOT_NAME_MINIMUM_SIZE));

        }
        else if (slotValue.containsSlotValue(NumericValue.SLOT_NAME_MINIMUM) && slotValue instanceof NumericValue)
        {
            minimumValueAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_MINIMUM_VALUE);
            minimumValueAnnotation.setAttributeValue(AnnotationParameterName.value.name(),
                    String.valueOf(slotValue.getSlotValue(NumericValue.SLOT_NAME_MINIMUM)));

            minimumValueAnnotation.setAttributeValue(AnnotationParameterName.exclusive.name(),
                    ((NumericValue) slotValue).isExclusiveMinimum());

        }


        if (slotValue instanceof CollectionValue)
        {

            final CollectionValue collectionValue = (CollectionValue) slotValue;
            final Slot elementSlot = collectionValue.getElementSlot();

            if (elementSlot == null)
            {
                throw new SchemaGeneratorException("The collection value: " + collectionValue + " does not define an element slot. Collection values must have an element type of " + ModelValue.class, null, this);
            }

            final Value elementValue = collectionValue.getElementSlot().getValue();
            if (!(elementValue instanceof ModelValue))
            {
                throw new SchemaGeneratorException("The collection value: " + collectionValue + " does not define a model-based element slot. Collection values must have an element type of " + ModelValue.class, null, this);
            }

            collectionSlotAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_COLLECTION_SLOT);

            final URI linkRelationUri = collectionValue.getLinkRelationUri();
            collectionSlotAnnotation.setAttributeValue(AnnotationParameterName.linkRelationUri.name(), linkRelationUri.toString());

            final Integer limit = collectionValue.getLimit();
            if (limit != null)
            {
                if (limit < 1)
                {
                    throw new SchemaGeneratorException("The collection value: " + collectionValue + " defines an invalid limit: " + limit, null, this);
                }

                collectionSlotAnnotation.setAttributeValue(AnnotationParameterName.limit.name(), limit.toString());
            }

            final List<CollectionValueSearchCriterion> andedCriterionList = collectionValue.getAnd();
            if (andedCriterionList.size() > 0)
            {
                final JavaBytecodeAnnotation[] andCriterionArray = generateCollectionSlotCriterionArray(andedCriterionList);
                collectionSlotAnnotation.setAttributeValue(AnnotationParameterName.and.name(), andCriterionArray);
            }

            final List<CollectionValueSearchCriterion> oredCriterionList = collectionValue.getOr();
            if (oredCriterionList.size() > 0)
            {
                final JavaBytecodeAnnotation[] orCriterionArray = generateCollectionSlotCriterionArray(oredCriterionList);
                collectionSlotAnnotation.setAttributeValue(AnnotationParameterName.or.name(), orCriterionArray);
            }


        }


        //
        // Create an object to describe the *read* access method (aka
        // "getter" method) that needs
        // to be generated.
        //
        final JavaBytecodeMethod readMethod = generateMethod(readMethodName, type, aliasAnnotation,
                descriptionAnnotation, titleAnnotation, defaultValueAnnotation, disallowedValuesAnnotation,
                divisibleByAnnotation, maximumValueAnnotation, minimumValueAnnotation, maximumLengthAnnotation,
                minimumLengthAnnotation, maximumSizeAnnotation, minimumSizeAnnotation, multilineAnnotation, collectionSlotAnnotation);

        javaBytecodeClass.getMethods().add(readMethod);

        if (aliases != null && aliases.size() > 0)
        {
            for (final String alias : aliases)
            {
                if (alias == null)
                {
                    continue;
                }

                final String aliasMethodNameSuffix = StringUtils.capitalize(StringUtils.deleteWhitespace(alias));
                final String aliasReadMethodName = readMethodNamePrefix + aliasMethodNameSuffix;

                final JavaBytecodeMethod aliasReadMethod = generateMethod(aliasReadMethodName, type);

                javaBytecodeClass.getMethods().add(aliasReadMethod);

            }
        }

        if (slotValue instanceof MaybeReadOnly)
        {

            final Boolean maybeReadOnly = ((MaybeReadOnly) slotValue).isReadOnly();

            if (maybeReadOnly == null || !maybeReadOnly)
            {

                final String writeMethodName = JavaBean.SET + methodNameSuffix;

                JavaBytecodeAnnotation nonNullAnnotation = null;  // ;-)
                if (slotValue instanceof MaybeRequired)
                {
                    final Boolean maybeRequired = ((MaybeRequired) slotValue).isRequired();
                    final boolean required = (maybeRequired != null && maybeRequired);
                    if (required && !(slotValue instanceof NumericValue) && !(slotValue instanceof BooleanValue))
                    {
                        nonNullAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_NON_NULL);
                    }
                }

                //
                // Create an object to describe the *write* access method
                // (aka "setter" method) that needs
                // to be generated.
                //
                final JavaBytecodeMethod writeMethod = generateMethod(writeMethodName, type, type, nonNullAnnotation);
                javaBytecodeClass.getMethods().add(writeMethod);

                if (aliases != null && aliases.size() > 0)
                {
                    for (final String alias : aliases)
                    {
                        if (alias == null)
                        {
                            continue;
                        }

                        final String aliasMethodNameSuffix = StringUtils
                                .capitalize(StringUtils.deleteWhitespace(alias));
                        final String aliasWriteMethodName = JavaBean.SET + aliasMethodNameSuffix;
                        final JavaBytecodeMethod aliasWriteMethod = generateMethod(aliasWriteMethodName, type, type);
                        javaBytecodeClass.getMethods().add(aliasWriteMethod);
                    }
                }
            }
        }

    }

    private JavaBytecodeAnnotation[] generateCollectionSlotCriterionArray(final List<CollectionValueSearchCriterion> criterionList)
    {

        final JavaBytecodeAnnotation[] criterionArray = new JavaBytecodeAnnotation[criterionList.size()];
        for (int i = 0; i < criterionArray.length; i++)
        {

            final CollectionValueSearchCriterion criterion = criterionList.get(i);
            final JavaBytecodeAnnotation collectionSlotCriterionAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_COLLECTION_SLOT_CRITERION);
            criterionArray[i] = collectionSlotCriterionAnnotation;


            final String referenceSlot = criterion.getReferenceSlot();
            collectionSlotCriterionAnnotation.setAttributeValue(AnnotationParameterName.referenceSlot.name(), referenceSlot);

            final ComparisonOperator operator = criterion.getOperator();
            collectionSlotCriterionAnnotation.setAttributeValue(AnnotationParameterName.operator.name(), operator);


            final String linkValueSource = criterion.getValueSource();
            final ValueSourceType linkValueSourceType = criterion.getValueSourceType();

            if (operator != ComparisonOperator.exists && operator != ComparisonOperator.notExists && linkValueSource != null)
            {
                collectionSlotCriterionAnnotation.setAttributeValue(AnnotationParameterName.valueSource.name(), linkValueSource);
                collectionSlotCriterionAnnotation.setAttributeValue(AnnotationParameterName.valueSourceType.name(), linkValueSourceType);
            }

            final String regex = criterion.getRegex();
            if (operator == ComparisonOperator.regex && regex != null)
            {
                collectionSlotCriterionAnnotation.setAttributeValue(AnnotationParameterName.regex.name(), regex);
            }


        }

        return criterionArray;
    }

    private List<CollectionValueSearchCriterion> generateCollectionValueSearchCriterionList(final List<ProtoSearchCriterion> protoSearchCriterionList)
    {

        final int listSize = (protoSearchCriterionList != null) ? protoSearchCriterionList.size() : 0;
        final List<CollectionValueSearchCriterion> criterionList = new ArrayList<>(listSize);

        final Context context = getContext();
        if (protoSearchCriterionList != null)
        {
            for (final ProtoSearchCriterion protoSearchCriterion : protoSearchCriterionList)
            {

                final CollectionValueSearchCriterion searchCriterion = context.newModel(CollectionValueSearchCriterion.class);


                final ComparisonOperator operator = protoSearchCriterion.getComparisonOperator();
                searchCriterion.setOperator(operator);

                final ProtoValueSource protoValueSource = protoSearchCriterion.getProtoValueSource();
                searchCriterion.setReferenceSlot(protoValueSource.getReferenceSlot());

                if (operator != ComparisonOperator.exists && operator != ComparisonOperator.notExists)
                {
                    searchCriterion.setValueSourceType(protoValueSource.getValueSourceType());
                    searchCriterion.setValueSource(protoValueSource.getValueSource());

                }

                if (operator == ComparisonOperator.regex)
                {
                    searchCriterion.setRegex(protoSearchCriterion.getRegex());
                }

                criterionList.add(searchCriterion);
            }
        }

        return criterionList;
    }

    /**
     * Generates the method associated with a Link slot value ({@link LinkValue}).
     *
     * @param javaBytecodeClass The {@link JavaBytecodeClass} representing the Schema interface being generated.
     * @param isAggregate       <code>true</code> if the {@link Schema} that owns the link {@link Slot} is an {@link org.wrml.model.rest.AggregateDocument}.
     * @param slot              The slot holding the LinkValue to use as a template for a set of generated Java methods.
     */
    private void generateLinkMethod(final JavaBytecodeClass javaBytecodeClass, final boolean isAggregate, final Slot slot)
    {


        JavaBytecodeAnnotation aliasAnnotation = null;

        final List<String> aliases = slot.getAliases();
        if (aliases != null && aliases.size() > 0)
        {
            aliasAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_ALIASES);
            final String[] aliasesArray = new String[aliases.size()];
            aliasAnnotation.setAttributeValue(AnnotationParameterName.value.name(), aliases.toArray(aliasesArray));
        }

        final JavaBytecodeAnnotation linkSlotAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_LINK_SLOT);

        final LinkValue linkValue = (LinkValue) slot.getValue();

        final URI linkRelationUri = linkValue.getLinkRelationUri();
        if (linkRelationUri == null)
        {
            throw new SchemaGeneratorException("The link must specify a URI value in its \"linkRelationUri\" slot.", null, this);
        }

        final String linkRelationUriString = linkRelationUri.toASCIIString();
        if (linkRelationUriString == null || linkRelationUriString.isEmpty())
        {
            throw new SchemaGeneratorException("The link must specify a URI value in its \"linkRelationUri\" slot.", null, this);
        }

        linkSlotAnnotation.setAttributeValue(AnnotationParameterName.linkRelationUri.name(), linkRelationUriString);

        final ApiLoader apiLoader = getContext().getApiLoader();
        final LinkRelation linkRelation = apiLoader.loadLinkRelation(linkRelationUri);
        if (linkRelation == null)
        {
            throw new SchemaGeneratorException("Could not get the LinkRelation with URI: " + linkRelationUri, null, this);
        }


        final String slotName = slot.getName();

        final String linkSlotName = StringUtils.deleteWhitespace(slotName);
        String linkMethodName = linkSlotName;

        final Method method = linkRelation.getMethod();
        linkSlotAnnotation.setAttributeValue(AnnotationParameterName.method.name(), method);


        final URI linkValueResponseSchemaUri = linkValue.getResponseSchemaUri();
        final URI linkRelationResponseSchemaUri = linkRelation.getResponseSchemaUri();
        final URI methodReturnSchemaUri = (linkValueResponseSchemaUri != null) ? linkValueResponseSchemaUri : linkRelationResponseSchemaUri;
        final JavaBytecodeType methodReturnType = javaBytecodeTypeForSchemaUri(methodReturnSchemaUri);

        final URI linkValueRequestSchemaUri = linkValue.getRequestSchemaUri();
        final URI linkRelationRequestSchemaUri = linkRelation.getRequestSchemaUri();
        final URI methodParameterSchemaUri = (linkValueRequestSchemaUri != null) ? linkValueRequestSchemaUri : linkRelationRequestSchemaUri;

        final JavaBytecodeMethod linkMethod;
        if (methodParameterSchemaUri != null)
        {
            // Link method accepts a parameter
            final JavaBytecodeType methodParameterType = javaBytecodeTypeForSchemaUri(methodParameterSchemaUri);
            linkMethod = generateMethod(linkMethodName, methodReturnType, methodParameterType, aliasAnnotation, linkSlotAnnotation);
        }
        else
        {
            // A zero argument link method
            if (method == Method.Get)
            {
                // Links with GET semantics are named with a pattern: get{LinkSlotName}()
                linkMethodName = JavaBean.GET + StringUtils.capitalize(linkMethodName);
            }

            linkMethod = generateMethod(linkMethodName, methodReturnType, aliasAnnotation, linkSlotAnnotation);
        }

        boolean embedded = (linkValue.isEmbedded() || isAggregate) && (method == Method.Get);
        linkSlotAnnotation.setAttributeValue(AnnotationParameterName.embedded.name(), embedded);

        List<LinkValueBinding> linkValueBindings = linkValue.getBindings();
        if (!linkValueBindings.isEmpty())
        {

            final JavaBytecodeAnnotation[] bindingsArray = new JavaBytecodeAnnotation[linkValueBindings.size()];
            for (int i = 0; i < bindingsArray.length; i++)
            {

                final LinkValueBinding linkValueBinding = linkValueBindings.get(i);
                final String referenceSlot = linkValueBinding.getReferenceSlot();
                final String linkValueSource = linkValueBinding.getValueSource();
                final ValueSourceType linkValueSourceType = linkValueBinding.getValueSourceType();

                final JavaBytecodeAnnotation linkSlotBindingAnnotation = new JavaBytecodeAnnotation(SchemaGenerator.ANNOTATION_INTERNAL_NAME_LINK_SLOT_BINDING);
                linkSlotBindingAnnotation.setAttributeValue(AnnotationParameterName.referenceSlot.name(), referenceSlot);
                linkSlotBindingAnnotation.setAttributeValue(AnnotationParameterName.valueSource.name(), linkValueSource);
                linkSlotBindingAnnotation.setAttributeValue(AnnotationParameterName.valueSourceType.name(), linkValueSourceType);

                bindingsArray[i] = linkSlotBindingAnnotation;
            }

            linkSlotAnnotation.setAttributeValue(AnnotationParameterName.bindings.name(), bindingsArray);

        }


        javaBytecodeClass.getMethods().add(linkMethod);

    }

    private List<LinkValueBinding> generateLinkValueBindingList(final LinkProtoSlot linkProtoSlot)
    {

        final Context context = getContext();
        final Map<String, ProtoValueSource> linkSlotBindings = linkProtoSlot.getLinkSlotBindings();
        final List<LinkValueBinding> linkValueBindingList = new ArrayList<>(linkSlotBindings.size());
        final Collection<ProtoValueSource> protoValueSources = linkSlotBindings.values();
        for (final ProtoValueSource protoValueSource : protoValueSources)
        {
            final LinkValueBinding linkValueBinding = context.newModel(LinkValueBinding.class);
            linkValueBindingList.add(linkValueBinding);

            linkValueBinding.setReferenceSlot(protoValueSource.getReferenceSlot());
            linkValueBinding.setValueSourceType(protoValueSource.getValueSourceType());
            linkValueBinding.setValueSource(protoValueSource.getValueSource());
        }

        return linkValueBindingList;

    }

    private JavaBytecodeMethod generateMethod(final String name, final JavaBytecodeType returnType,
                                              final JavaBytecodeAnnotation... annotations)
    {

        return generateMethod(name, returnType, (JavaBytecodeType) null, annotations);
    }

    private JavaBytecodeMethod generateMethod(final String name, final JavaBytecodeType returnType,
                                              final JavaBytecodeType argumentType, final JavaBytecodeAnnotation... annotations)
    {

        final JavaBytecodeMethod method = new JavaBytecodeMethod();
        method.setName(StringUtils.deleteWhitespace(name));
        method.setDescriptor(generateMethodDescriptor(returnType, argumentType));
        method.setSignature(generateMethodSignature(returnType, argumentType));
        addAnnotations(method, annotations);
        return method;
    }

    private String generateMethodDescriptor(final JavaBytecodeType returnType, final JavaBytecodeType argumentType)
    {

        final String returnTypeDescriptor = generateReturnTypeDescriptor(returnType);
        final String argsDescriptor = generateArgsDescriptor(argumentType);
        final String methodDescriptor = argsDescriptor + returnTypeDescriptor;
        return methodDescriptor;
    }

    private String generateMethodDescriptor(final JavaBytecodeType returnType, final JavaBytecodeType[] argumentTypes)
    {

        final String returnTypeDescriptor = generateReturnTypeDescriptor(returnType);
        final String argsDescriptor = generateArgsDescriptor(argumentTypes);
        final String methodDescriptor = argsDescriptor + returnTypeDescriptor;
        return methodDescriptor;
    }

    private String generateMethodSignature(final JavaBytecodeType returnType, final JavaBytecodeType argumentType)
    {

        final String returnTypeSignature = generateReturnTypeSignature(returnType);
        final String argsSignature = generateArgsSignature(argumentType);
        final String methodSignature = generateMethodSignature(returnTypeSignature, argsSignature);
        return methodSignature;

    }

    private String generateMethodSignature(final JavaBytecodeType returnType, final JavaBytecodeType[] argumentTypes)
    {

        final String returnTypeSignature = generateReturnTypeSignature(returnType);
        final String argsSignature = generateArgsSignature(argumentTypes);
        final String methodSignature = generateMethodSignature(returnTypeSignature, argsSignature);
        return methodSignature;
    }

    private String generateMethodSignature(final String returnTypeSignature, final String argsSignature)
    {

        if (returnTypeSignature == null && argsSignature == null)
        {
            return null;
        }

        if (returnTypeSignature == null && argsSignature != null)
        {
            return argsSignature + JavaBytecodeType.VoidPrimitiveBytecodeType.getDescriptor();
        }
        else if (returnTypeSignature != null && argsSignature == null)
        {
            return SchemaGenerator.EMPTY_PARENTHESES + returnTypeSignature;
        }
        else
        {
            return argsSignature + returnTypeSignature;
        }

    }

    private String generateReturnTypeDescriptor(final JavaBytecodeType returnType)
    {

        String returnTypeDescriptor = null;
        if (returnType != null)
        {
            returnTypeDescriptor = returnType.getDescriptor();
        }

        if (returnTypeDescriptor == null)
        {
            returnTypeDescriptor = JavaBytecodeType.VoidPrimitiveBytecodeType.getDescriptor();
        }

        return returnTypeDescriptor;
    }

    private String generateReturnTypeSignature(final JavaBytecodeType returnType)
    {

        final String returnTypeSignature;
        if (returnType == null)
        {
            returnTypeSignature = null;
        }
        else
        {
            returnTypeSignature = returnType.getGenericSignature();
        }
        return returnTypeSignature;
    }

    /**
     * Handles the actual JVM bytecode generation.
     *
     * @param classFile The Java class file information used to drive the bytecode
     *                  generation.
     */
    private void generateSchemaInterfaceBytecode(final JavaBytecodeClass classFile)
    {

        final ClassWriter classVisitor = new ClassWriter(0);

        final List<String> interfaces = classFile.getInterfaces();
        String[] interfaceNames = new String[interfaces.size()];
        interfaceNames = (interfaceNames.length > 0) ? interfaces.toArray(interfaceNames) : null;

        // Generate the interface declaration
        classVisitor.visit(SchemaGenerator.JVM_VERSION, SchemaGenerator.INTERFACE_ACCESS, classFile.getInternalName(),
                classFile.getSignature(), classFile.getSuperName(), interfaceNames);

        for (final JavaBytecodeAnnotation annotation : classFile.getAnnotations())
        {
            visitAnnotation(annotation, classVisitor, null, null, null);
        }

        final List<JavaBytecodeMethod> methods = classFile.getMethods();

        for (final JavaBytecodeMethod method : methods)
        {

            final List<String> exceptions = method.getExceptions();
            String[] exceptionNames = new String[exceptions.size()];
            exceptionNames = (exceptionNames.length > 0) ? exceptions.toArray(exceptionNames) : null;

            final MethodVisitor methodVisitor = classVisitor.visitMethod(SchemaGenerator.INTERFACE_METHOD_ACCESS,
                    method.getName(), method.getDescriptor(), method.getSignature(), exceptionNames);

            // If we weren't simply generating Java interfaces, things would
            // get more _advanced_ right here.

            for (final JavaBytecodeAnnotation annotation : method.getAnnotations())
            {
                visitAnnotation(annotation, null, methodVisitor, null, null);
            }

            methodVisitor.visitEnd();
        }

        // Finish the code generation
        classVisitor.visitEnd();

        final byte[] bytecode = classVisitor.toByteArray();
        classFile.setBytecode(bytecode);
    }

    /*
     * For each WRML Slot defined by the schema, generate a JavaBean
     * property in the class file.
     *
     * A JavaBean property is not formalized by the language but rather
     * exists via the pattern of a "getter" method and, for non-read only
     * (writable) properties, also a "setter" method. For more information
     * about the JavaBean property pattern, see:
     *
     * <a href="http://docs.oracle.com/javase/tutorial/javabeans/writing/properties.html">
     */
    private void generateSchemaInterfaceMethods(final Schema schema, final JavaBytecodeClass javaBytecodeClass, final boolean isAggregate)
    {

        final List<Slot> slots = schema.getSlots();
        for (final Slot slot : slots)
        {

            /*
             * WRML schema slots must be named using mixed lower case (aka
             * camel case) with no whitespace allowed.
             */
            final String slotName = slot.getName();

            if (_ModelJavaBean.getProperties().containsKey(slotName))
            {

                // TODO: Filter other things like Java reserved words and
                // symbols

                throw new SchemaGeneratorException("Sorry but \"" + slotName
                        + "\" is reserved, it is not legal within a Schema.", null, this);
            }

            if (slot != null && slot.getValue() instanceof LinkValue)
            {
                generateLinkMethod(javaBytecodeClass, isAggregate, slot);
            }
            else
            {
                generateBeanAccessorMethods(javaBytecodeClass, schema, isAggregate, slot);
            }
        }
    }

    private Value generateValue(final JsonSchema jsonSchema, final JsonSchema.Property property)
            throws SchemaGeneratorException
    {

        if (property == null)
        {
            throw new SchemaGeneratorException("Failed to generate a Value", null, this);
        }

        final URI schemaUri = jsonSchema.getId();
        if (schemaUri == null)
        {
            throw new SchemaGeneratorException("Failed to generate a Value", null, this);
        }

        final JsonType jsonType = property.getJsonType();
        if (jsonType == null)
        {
            throw new SchemaGeneratorException("Failed to generate a Value", null, this);
        }

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final ValueType valueType = getValueType(jsonType);
        Value value = null;

        switch (valueType)
        {

            case Boolean:
            {
                final BooleanValue booleanValue = context.newModel(BooleanValue.class);

                final JsonNode defaultNode = property.getValueNode(PropertyType.Default);
                if (defaultNode != null)
                {
                    booleanValue.setDefault(defaultNode.asBoolean());
                }

                value = booleanValue;
                break;
            }
            case Date:
            {
                break;
            }
            case Double:
            {
                final DoubleValue doubleValue = context.newModel(DoubleValue.class);

                final JsonNode defaultNode = property.getValueNode(PropertyType.Default);
                if (defaultNode != null)
                {
                    doubleValue.setDefault(defaultNode.asDouble());
                }

                final JsonNode maximumNode = property.getValueNode(PropertyType.Maximum);
                if (maximumNode != null)
                {
                    doubleValue.setMaximum(maximumNode.asDouble());
                }

                final JsonNode minimumNode = property.getValueNode(PropertyType.Minimum);
                if (minimumNode != null)
                {
                    doubleValue.setMinimum(minimumNode.asDouble());
                }

                value = doubleValue;
                break;
            }
            case Integer:
            {
                final IntegerValue integerValue = context.newModel(IntegerValue.class);

                final JsonNode defaultNode = property.getValueNode(PropertyType.Default);
                if (defaultNode != null)
                {
                    integerValue.setDefault(defaultNode.asInt());
                }

                final JsonNode divisibleByNode = property.getValueNode(PropertyType.DivisibleBy);
                if (divisibleByNode != null)
                {
                    integerValue.setDivisibleBy(divisibleByNode.asInt());
                }

                final JsonNode multipleOfNode = property.getValueNode(PropertyType.MultipleOf);
                if (multipleOfNode != null)
                {
                    integerValue.setDivisibleBy(multipleOfNode.asInt());
                }

                final JsonNode maximumNode = property.getValueNode(PropertyType.Maximum);
                if (maximumNode != null)
                {
                    integerValue.setMaximum(maximumNode.asInt());
                }

                final JsonNode minimumNode = property.getValueNode(PropertyType.Minimum);
                if (minimumNode != null)
                {
                    integerValue.setMinimum(minimumNode.asInt());
                }

                value = integerValue;
                break;
            }
            case Link:
                // NOTE: Falling through the Link switch case on purpose to treat same as Model
                // TODO: Revisit this design.
            case Model:
            {

                final ModelValue modelValue = context.newModel(ModelValue.class);
                final JsonSchemaLoader jsonSchemaLoader = schemaLoader.getJsonSchemaLoader();
                final JsonSchema modelJsonSchema;

                final URI baseSchemaUri;
                final JsonNode schemaIdNode = property.getValueNode(PropertyType.Id);
                if (schemaIdNode != null)
                {
                    baseSchemaUri = schemaLoader.getDocumentSchemaUri();

                    final URI modelSchemaUri = property.getValue(PropertyType.Id);
                    if (modelSchemaUri != schemaUri)
                    {
                        try
                        {
                            modelJsonSchema = jsonSchemaLoader.load(modelSchemaUri);
                        }
                        catch (final IOException e)
                        {
                            throw new SchemaGeneratorException(e.getMessage(), e, this);
                        }
                    }
                    else
                    {
                        modelJsonSchema = jsonSchema;
                    }
                }
                else
                {

                    baseSchemaUri = schemaLoader.getEmbeddedSchemaUri();

                    String modelSchemaUriString = schemaUri.toString();

                    if (modelSchemaUriString.endsWith(".json"))
                    {
                        modelSchemaUriString = modelSchemaUriString.substring(0,
                                modelSchemaUriString.length() - ".json".length());
                    }

                    int innerClassNumber = 0;

                    if (_InnerSchemaCountMap.containsKey(schemaUri))
                    {
                        innerClassNumber = _InnerSchemaCountMap.get(schemaUri);
                    }

                    innerClassNumber++;

                    _InnerSchemaCountMap.put(schemaUri, innerClassNumber);

                    final String annonymousInnerSchemaName = ANNONYMOUS_INNER_SCHEMA_PREFIX
                            + String.valueOf(innerClassNumber);

                    modelSchemaUriString = modelSchemaUriString.concat(annonymousInnerSchemaName);

                    final ObjectNode propertyNode = property.getPropertyNode();
                    propertyNode.put(PropertyType.Id.getName(), modelSchemaUriString);

                    final URI modelSchemaUri = URI.create(modelSchemaUriString);

                    modelJsonSchema = jsonSchemaLoader.load(propertyNode, modelSchemaUri);
                }

                if (modelJsonSchema != jsonSchema)
                {

                    schemaLoader.load(modelJsonSchema, baseSchemaUri);
                }

                modelValue.setModelSchemaUri(modelJsonSchema.getId());

                value = modelValue;
                break;
            }
            case List:
            {
                final ListValue listValue = context.newModel(ListValue.class);

                final JsonNode uniqueItemsNode = property.getValueNode(PropertyType.UniqueItems);
                if (uniqueItemsNode != null)
                {
                    listValue.setElementUniquenessConstrained(uniqueItemsNode.asBoolean());
                }

                final JsonNode maxItemsNode = property.getValueNode(PropertyType.MaxItems);
                if (maxItemsNode != null)
                {
                    listValue.setMaximumSize(maxItemsNode.asInt());
                }

                final JsonNode minItemsNode = property.getValueNode(PropertyType.MinItems);
                if (minItemsNode != null)
                {
                    listValue.setMinimumSize(minItemsNode.asInt());
                }

                final JsonNode itemsNode = property.getValueNode(PropertyType.Items);
                ObjectNode itemNode = null;
                if (itemsNode instanceof ObjectNode)
                {
                    itemNode = (ObjectNode) itemsNode;

                }
                else if (itemsNode instanceof ArrayNode)
                {

                    final ArrayNode itemsArrayNode = (ArrayNode) itemsNode;
                    if (itemsArrayNode.has(0))
                    {
                        itemNode = (ObjectNode) itemsArrayNode.get(0);
                    }
                }

                if (itemNode != null)
                {
                    Property itemsProperty;
                    try
                    {
                        itemsProperty = new Property(jsonSchema, PropertyType.Items.getName(), itemNode);
                        final Value elementValue = generateValue(jsonSchema, itemsProperty);
                        final Slot elementSlot = context.newModel(Slot.class);
                        elementSlot.setName("E");
                        elementSlot.setValue(elementValue);
                        listValue.setElementSlot(elementSlot);
                    }
                    catch (final IOException e)
                    {
                        throw new SchemaGeneratorException(e.getMessage(), e, this);
                    }
                }

                value = listValue;
                break;
            }
            case Long:
            {
                final LongValue longValue = context.newModel(LongValue.class);

                final JsonNode defaultNode = property.getValueNode(PropertyType.Default);
                if (defaultNode != null)
                {
                    longValue.setDefault(defaultNode.asLong());
                }

                final JsonNode divisibleByNode = property.getValueNode(PropertyType.DivisibleBy);
                if (divisibleByNode != null)
                {
                    longValue.setDivisibleBy(divisibleByNode.asLong());
                }

                final JsonNode multipleOfNode = property.getValueNode(PropertyType.MultipleOf);
                if (multipleOfNode != null)
                {
                    longValue.setDivisibleBy(multipleOfNode.asLong());
                }

                final JsonNode maximumNode = property.getValueNode(PropertyType.Maximum);
                if (maximumNode != null)
                {
                    longValue.setMaximum(maximumNode.asLong());
                }

                final JsonNode minimumNode = property.getValueNode(PropertyType.Minimum);
                if (minimumNode != null)
                {
                    longValue.setMinimum(minimumNode.asLong());
                }

                value = longValue;
                break;
            }
            case Native:
            {
                break;
            }
            case SingleSelect:
            {
                final SingleSelectValue singleSelectValue = context.newModel(SingleSelectValue.class);
                value = singleSelectValue;
                break;
            }
            case Text:
            {
                final TextValue textValue = context.newModel(TextValue.class);

                final JsonNode defaultNode = property.getValueNode(PropertyType.Default);
                if (defaultNode != null)
                {
                    textValue.setDefault(defaultNode.asText());
                }

                final JsonNode maxLengthNode = property.getValueNode(PropertyType.MaxLength);
                if (maxLengthNode != null)
                {
                    textValue.setMaximumLength(maxLengthNode.asInt());
                }

                final JsonNode minLengthNode = property.getValueNode(PropertyType.MinLength);
                if (minLengthNode != null)
                {
                    textValue.setMinimumLength(minLengthNode.asInt());
                }

                final JsonNode formatNode = property.getValueNode(PropertyType.Format);
                if (formatNode != null)
                {

                    final String formatKeyword = formatNode.asText();
                    final JsonStringFormat jsonStringFormat = JsonStringFormat.forKeyword(formatKeyword);
                    if (jsonStringFormat != null)
                    {
                        final Class<?> formatJavaType = jsonStringFormat.getJavaType();
                        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
                        final URI syntaxUri = syntaxLoader.getSyntaxUri(formatJavaType);
                        textValue.setSyntaxUri(syntaxUri);
                    }
                }

                textValue.getDisallowedValues();

                value = textValue;

                break;
            }
            default:
            {
                break;
            }
        }

        if (value instanceof MaybeRequired)
        {
            final JsonNode requiredNode = property.getValueNode(PropertyType.Required);
            if (requiredNode != null)
            {
                ((MaybeRequired) value).setRequired(requiredNode.asBoolean());
            }
        }

        if (value instanceof NumericValue)
        {
            final NumericValue numericValue = (NumericValue) value;

            final JsonNode exclusiveMaximumNode = property.getValueNode(PropertyType.ExclusiveMaximum);
            if (exclusiveMaximumNode != null)
            {
                numericValue.setExclusiveMaximum(exclusiveMaximumNode.asBoolean());
            }

            final JsonNode exclusiveMinimumNode = property.getValueNode(PropertyType.ExclusiveMinimum);
            if (exclusiveMinimumNode != null)
            {
                numericValue.setExclusiveMinimum(exclusiveMinimumNode.asBoolean());
            }
        }

        return value;

    }

    private Value generateValue(final Type heapValueType, final ProtoSlot protoSlot)
    {

        final Context context = getContext();

        if (heapValueType == null)
        {
            return null;
        }

        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final ValueType valueType = schemaLoader.getValueType(heapValueType);
        final Class<?> heapValueClass = (heapValueType instanceof Class<?>) ? (Class<?>) heapValueType : null;

        final boolean requiresNonNullValue = (heapValueClass != null && heapValueClass.isPrimitive());
        Value value = null;

        switch (valueType)
        {

            case Boolean:
                final BooleanValue booleanValue = context.newModel(BooleanValue.class);
                value = booleanValue;
                break;

            case Date:
                final DateValue dateValue = context.newModel(DateValue.class);
                value = dateValue;

                break;

            case Double:
                final DoubleValue doubleValue = context.newModel(DoubleValue.class);
                value = doubleValue;
                break;

            case Integer:
                final IntegerValue integerValue = context.newModel(IntegerValue.class);
                value = integerValue;
                break;

            case Link:
                final LinkValue linkValue = context.newModel(LinkValue.class);

                if (protoSlot instanceof LinkProtoSlot)
                {
                    final LinkProtoSlot linkProtoSlot = (LinkProtoSlot) protoSlot;

                    linkValue.setLinkRelationUri(linkProtoSlot.getLinkRelationUri());
                    linkValue.setEmbedded(linkProtoSlot.isEmbedded());
                    linkValue.setRequestSchemaUri(linkProtoSlot.getRequestSchemaUri());
                    linkValue.setResponseSchemaUri(linkProtoSlot.getResponseSchemaUri());

                    final List<LinkValueBinding> bindings = generateLinkValueBindingList(linkProtoSlot);
                    linkValue.getBindings().addAll(bindings);
                }

                value = linkValue;
                break;

            case Model:

                final ModelValue modelValue = context.newModel(ModelValue.class);
                final URI modelSchemaUri = schemaLoader.getTypeUri(heapValueClass);
                modelValue.setModelSchemaUri(modelSchemaUri);

                value = modelValue;
                break;

            case List:

                final ListValue listValue;

                if (protoSlot instanceof CollectionPropertyProtoSlot)
                {
                    final CollectionPropertyProtoSlot collectionPropertyProtoSlot = (CollectionPropertyProtoSlot) protoSlot;
                    final CollectionValue collectionValue = context.newModel(CollectionValue.class);
                    listValue = collectionValue;

                    final Integer limit = collectionPropertyProtoSlot.getLimit();
                    if (limit != null && limit > 0)
                    {
                        collectionValue.setLimit(limit);
                    }

                    final URI linkRelationUri = collectionPropertyProtoSlot.getLinkRelationUri();
                    if (linkRelationUri != null)
                    {
                        collectionValue.setLinkRelationUri(linkRelationUri);
                    }

                    final ProtoSearchCriteria protoSearchCriteria = collectionPropertyProtoSlot.getProtoSearchCriteria();

                    final List<CollectionValueSearchCriterion> and = generateCollectionValueSearchCriterionList(protoSearchCriteria.getAnd());
                    collectionValue.getAnd().addAll(and);

                    final List<CollectionValueSearchCriterion> or = generateCollectionValueSearchCriterionList(protoSearchCriteria.getOr());
                    collectionValue.getOr().addAll(or);
                }
                else
                {
                    listValue = context.newModel(ListValue.class);
                }


                final Slot elementSlot = context.newModel(Slot.class);
                // TODO get this from the java class
                elementSlot.setName("E");

                final Type elementValueHeapType = ValueType.getListElementType(heapValueType);
                final Value elementValue = generateValue(elementValueHeapType, null);
                elementSlot.setValue(elementValue);
                listValue.setElementSlot(elementSlot);

                value = listValue;
                break;

            case Long:
                final LongValue longValue = context.newModel(LongValue.class);
                value = longValue;
                break;

            case Native:
                break;

            case SingleSelect:
                final SingleSelectValue singleSelectValue = context.newModel(SingleSelectValue.class);
                singleSelectValue.setChoicesUri(schemaLoader.getTypeUri(heapValueType));
                value = singleSelectValue;
                break;

            case Text:
                final TextValue textValue = context.newModel(TextValue.class);

                if (!String.class.equals(heapValueType) && heapValueClass != null)
                {
                    final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
                    final URI syntaxUri = syntaxLoader.getSyntaxUri(heapValueClass);
                    textValue.setSyntaxUri(syntaxUri);
                }

                value = textValue;

                break;

            default:
                break;

        }

        if (value instanceof MaybeRequired && requiresNonNullValue)
        {
            ((MaybeRequired) value).setRequired(requiresNonNullValue);
        }


        return value;
    }

    private String getDefaultValueString(final Value slotValue)
    {

        final Context context = getContext();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();

        String defaultValueString = null;

        if (slotValue instanceof TextValue)
        {
            defaultValueString = ((TextValue) slotValue).getDefault();
        }
        else if (slotValue instanceof IntegerValue)
        {
            final Integer defaultValue = ((IntegerValue) slotValue).getDefault();
            if (defaultValue != null)
            {
                defaultValueString = String.valueOf(defaultValue.intValue());
            }
        }
        else if (slotValue instanceof SingleSelectValue)
        {
            defaultValueString = ((SingleSelectValue) slotValue).getDefault();
        }
        else if (slotValue instanceof MultiSelectValue)
        {
            final List<String> defaultValue = ((MultiSelectValue) slotValue).getDefault();
            // TODO: Revisit this format
            defaultValueString = String.valueOf(defaultValue);
        }
        else if (slotValue instanceof DateValue)
        {
            final Date defaultValue = ((DateValue) slotValue).getDefault();
            if (defaultValue != null)
            {
                final SyntaxHandler<Date> syntaxHandler = syntaxLoader.getSyntaxHandler(Date.class);
                defaultValueString = syntaxHandler.formatSyntaxValue(defaultValue);
            }
        }
        else if (slotValue instanceof BooleanValue)
        {
            final Boolean defaultValue = ((BooleanValue) slotValue).getDefault();
            if (defaultValue != null)
            {
                defaultValueString = String.valueOf(defaultValue.booleanValue());
            }
        }
        else if (slotValue instanceof DoubleValue)
        {
            final Double defaultValue = ((DoubleValue) slotValue).getDefault();
            if (defaultValue != null)
            {
                defaultValueString = String.valueOf(defaultValue.doubleValue());
            }
        }
        else if (slotValue instanceof LongValue)
        {
            final Long defaultValue = ((LongValue) slotValue).getDefault();
            if (defaultValue != null)
            {
                defaultValueString = String.valueOf(defaultValue.longValue());
            }
        }

        return defaultValueString;

    }

    private JavaBytecodeType getSlotType(final JavaBytecodeClass javaBytecodeClass, final Schema schema, final boolean isAggregate, final Slot slot)
    {

        final Context context = getContext();
        final Value slotValue = slot.getValue();

        boolean required = false;
        if (slotValue instanceof MaybeRequired)
        {
            final Boolean maybeRequired = ((MaybeRequired) slotValue).isRequired();
            required = (maybeRequired != null && maybeRequired);
        }

        final JavaBytecodeType type;

        if (slotValue instanceof TextValue)
        {

            final URI syntaxUri = ((TextValue) slotValue).getSyntaxUri();
            if (syntaxUri != null)
            {
                final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
                final Class<?> syntaxJavaClass = syntaxLoader.getSyntaxJavaClass(syntaxUri);

                if (syntaxJavaClass != null)
                {
                    type = new JavaBytecodeType(syntaxJavaClass);
                }
                else
                {
                    throw new SchemaGeneratorException("Unsupported syntax, " + syntaxUri, null, this);
                }
            }
            else
            {
                type = JavaBytecodeType.StringBytecodeType;
            }
        }
        else if (slotValue instanceof LinkValue)
        {
            type = JavaBytecodeType.LinkBytecodeType;
        }
        else if (slotValue instanceof ModelValue)
        {

            final URI modelSchemaUri = ((ModelValue) slotValue).getModelSchemaUri();
            if (modelSchemaUri != null)
            {
                type = javaBytecodeTypeForSchemaUri(modelSchemaUri);
            }
            else
            {
                type = JavaBytecodeType.ModelBytecodeType;
            }
        }
        else if (slotValue instanceof ListValue)
        {

            final ListValue listValue = (ListValue) slotValue;
            final Slot elementSlot = listValue.getElementSlot();
            if (elementSlot != null)
            {
                type = new JavaBytecodeType(JavaBytecodeType.ListBytecodeType.getString());
                final JavaBytecodeType elementType = getSlotType(javaBytecodeClass, schema, isAggregate, elementSlot);
                final SortedMap<String, JavaBytecodeType> parameters = new TreeMap<String, JavaBytecodeType>();
                parameters.put(elementSlot.getName(), elementType);
                type.setParameters(parameters);
            }
            else
            {
                type = JavaBytecodeType.ListBytecodeType;
            }
        }
        else if (slotValue instanceof BooleanValue)
        {

            if (required)
            {
                // No nulls allowed, convert from a "Boolean" to a
                // "boolean"
                type = JavaBytecodeType.BooleanPrimitiveBytecodeType;
            }
            else
            {
                type = JavaBytecodeType.BooleanBytecodeType;
            }

        }
        else if (slotValue instanceof IntegerValue)
        {

            if (required)
            {
                // No nulls allowed, convert from a "Integer" to a "int"
                type = JavaBytecodeType.IntegerPrimitiveBytecodeType;
            }
            else
            {
                type = JavaBytecodeType.IntegerBytecodeType;
            }
        }
        else if (slotValue instanceof LongValue)
        {

            if (required)
            {
                // No nulls allowed, convert from a "Long" to a "long"
                type = JavaBytecodeType.LongPrimitiveBytecodeType;
            }
            else
            {
                type = JavaBytecodeType.LongBytecodeType;
            }

        }
        else if (slotValue instanceof DoubleValue)
        {

            if (required)
            {
                // No nulls allowed, convert from a "Double" to a "double"
                type = JavaBytecodeType.DoublePrimitiveBytecodeType;
            }
            else
            {
                type = JavaBytecodeType.DoubleBytecodeType;
            }

        }
        else if (slotValue instanceof DateValue)
        {
            type = JavaBytecodeType.DateBytecodeType;
        }
        else if (slotValue instanceof NativeValue)
        {
            // TODO: Finish support for "extended" value types.
            type = JavaBytecodeType.ObjectBytecodeType;
        }
        else if (slotValue instanceof SingleSelectValue)
        {

            final URI choicesUri = ((SingleSelectValue) slotValue).getChoicesUri();
            if (choicesUri != null)
            {
                type = javaBytecodeTypeForChoicesUri(choicesUri);
            }
            else
            {
                type = JavaBytecodeType.EnumBytecodeType;
            }

        }
        else
        {
            throw new SchemaGeneratorException("Unhandled value, " + slotValue + ", found in: "
                    + schema.getUniqueName().getLocalName() + "." + slot.getName(), null, this);
        }

        return type;
    }

    private ValueType getValueType(final JsonType jsonType)
    {

        switch (jsonType)
        {
            case Any:
                return ValueType.Native;

            case Array:
                return ValueType.List;

            case Boolean:
                return ValueType.Boolean;

            case Integer:
                return ValueType.Integer;

            case Null:
                return ValueType.Native;

            case Number:
                return ValueType.Double;

            case Object:
                return ValueType.Model;

            case String:
                return ValueType.Text;

            default:
                return ValueType.Native;

        }

    }

    private JavaBytecodeType javaBytecodeTypeForChoicesUri(final URI choicesUri)
    {

        if (choicesUri == null)
        {
            return null;
        }


        return new JavaBytecodeType(uriToInternalTypeName(choicesUri));

    }

    private JavaBytecodeType javaBytecodeTypeForSchemaUri(final URI schemaUri)
    {

        if (schemaUri == null)
        {
            return null;
        }

        return new JavaBytecodeType(uriToInternalTypeName(schemaUri));

    }

    private String uriToInternalTypeName(final URI uri)
    {

        final SchemaLoader schemaLoader = getSchemaLoader();
        final String externalClassName = schemaLoader.getNativeTypeName(uri);
        final String internalClassName = SchemaGenerator.externalTypeNameToInternalTypeName(externalClassName);
        return internalClassName;
    }

    private void visitAnnotation(final JavaBytecodeAnnotation annotation, final ClassVisitor classVisitor, final MethodVisitor methodVisitor, final AnnotationVisitor parentAnnotationVisitor, final String parentAnnotationValueName)
    {

        final String descriptor = annotation.getDescriptor();

        final AnnotationVisitor annotationVisitor;
        if (parentAnnotationVisitor != null)
        {
            annotationVisitor = parentAnnotationVisitor.visitAnnotation(parentAnnotationValueName, descriptor);
        }
        else if (methodVisitor != null)
        {
            annotationVisitor = methodVisitor.visitAnnotation(descriptor, true);
        }
        else
        {
            annotationVisitor = classVisitor.visitAnnotation(descriptor, true);
        }

        for (final String name : annotation.getAttributeNames())
        {
            final Object value = annotation.getAttributeValue(name);
            if (name != null && value != null)
            {
                if (value instanceof Object[])
                {
                    final AnnotationVisitor arrayValueVisitor = annotationVisitor.visitArray(name);
                    final Object[] array = (Object[]) value;
                    for (final Object element : array)
                    {
                        if (element instanceof Enum)
                        {
                            final String enumDescriptor = new JavaBytecodeType(element.getClass()).getDescriptor();
                            final String enumValueString = ((Enum) element).name();
                            arrayValueVisitor.visitEnum(null, enumDescriptor, enumValueString);
                        }
                        else if (element instanceof JavaBytecodeAnnotation)
                        {
                            visitAnnotation(((JavaBytecodeAnnotation) element), classVisitor, methodVisitor, arrayValueVisitor, name);
                        }
                        else
                        {
                            arrayValueVisitor.visit(null, element);
                        }
                    }
                    arrayValueVisitor.visitEnd();
                }
                else if (value instanceof Enum)
                {
                    final String enumDescriptor = new JavaBytecodeType(value.getClass()).getDescriptor();
                    final String enumValueString = ((Enum) value).name();
                    annotationVisitor.visitEnum(name, enumDescriptor, enumValueString);
                }
                else if (value instanceof JavaBytecodeAnnotation)
                {
                    visitAnnotation(((JavaBytecodeAnnotation) value), classVisitor, methodVisitor, annotationVisitor, name);
                }
                else
                {
                    annotationVisitor.visit(name, value);
                }
            }
        }

        annotationVisitor.visitEnd();
    }


    private static enum AnnotationParameterName
    {
        and,
        bindings,
        comparableSlotNames,
        embedded,
        exclusive,
        keySlotNames,
        limit,
        linkRelationUri,
        method,
        operator,
        or,
        referenceSlot,
        regex,
        titleSlotName,
        uniqueName,
        value,
        valueSource,
        valueSourceType;
    }

}

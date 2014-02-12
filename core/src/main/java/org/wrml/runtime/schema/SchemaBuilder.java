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
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.schema.*;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.KeysBuilder;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.util.UniqueName;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

/**
 * A helper utility to build Schema models from everyday inputs (e.g. <code>Map<String, Object></code>).
 */
public final class SchemaBuilder {

    private static final String JAVA_KEYWORDS[] = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
            "default", "do", "double", "else", "extends", "false", "final", "finally", "float", "for", "goto", "if",
            "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"
    };

    private static final SortedSet<String> RESERVED_WORD_SET = new TreeSet<>();

    static {
        for (final String javaKeyword : JAVA_KEYWORDS) {
            RESERVED_WORD_SET.add(javaKeyword);
        }
    }

    private final Context _Context;

    private final Schema _Schema;

    private final Map<String, Slot> _Slots;

    public SchemaBuilder(final Context context) {

        this(context, (Map<String, Object>) null);
    }

    public SchemaBuilder(final Context context, final Object... slots) {

        this(context, (Map<String, Object>) null);
        slots(slots);
    }

    public SchemaBuilder(final Context context, final Map<String, Object> startFromSlotMap) {

        this(context, startFromSlotMap, null);
    }

    public SchemaBuilder(final Context context, final Map<String, Object> startFromSlotMap, final UniqueName name) {

        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null.");
        }

        _Context = context;
        _Schema = _Context.newModel(Schema.class);

        if (name != null) {
            _Schema.setUniqueName(name);
        }

        _Slots = new TreeMap<>();

        if (startFromSlotMap != null) {
            slots(startFromSlotMap);
        }
    }

    public static String ensureValidJavaIdentifier(final String identifier) {

        if (identifier == null || RESERVED_WORD_SET.contains(identifier)) {
            return null;
        }

        String validJavaIdentifier = identifier;

        int index = validJavaIdentifier.indexOf('-');
        if (index > 0) {
            validJavaIdentifier = validJavaIdentifier.replace('-', '_');
        }

        if (!Character.isJavaIdentifierStart(validJavaIdentifier.charAt(0))) {
            validJavaIdentifier = "_" + validJavaIdentifier;
        }

        final int validJavaIdentifierLength = validJavaIdentifier.length();
        String replaceCharacters = "";
        for (int i = 1; i < validJavaIdentifierLength; i++) {
            char c = validJavaIdentifier.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                replaceCharacters += c;
            }
        }

        if (!replaceCharacters.isEmpty()) {
            validJavaIdentifier = StringUtils.replaceChars(validJavaIdentifier, replaceCharacters, "$");
        }

        return validJavaIdentifier;
    }

    public SchemaBuilder extend(final URI baseSchemaUri, final URI... baseSchemaUris) {

        _Schema.getBaseSchemaUris().add(baseSchemaUri);

        if (baseSchemaUris != null) {
            _Schema.getBaseSchemaUris().addAll(Arrays.asList(baseSchemaUris));
        }

        return this;
    }

    public SchemaBuilder extend(final Schema baseSchema, final Schema... baseSchemas) {

        extend(baseSchema.getUri());

        if (baseSchemas != null) {
            final LinkedHashSet<URI> baseSchemaUris = new LinkedHashSet<>(baseSchemas.length);
            for (final Schema schema : baseSchemas) {
                baseSchemaUris.add(schema.getUri());
            }

            _Schema.getBaseSchemaUris().addAll(baseSchemaUris);
        }

        return this;
    }

    public SchemaBuilder extend(final Class<?> baseSchemaInterface, final Class<?>... baseSchemaInterfaces) {

        final SchemaLoader schemaLoader = _Context.getSchemaLoader();
        extend(schemaLoader.getTypeUri(baseSchemaInterface));

        if (baseSchemaInterfaces != null) {
            final LinkedHashSet<URI> baseSchemaUris = new LinkedHashSet<>(baseSchemaInterfaces.length);
            for (final Class<?> schemaInterface : baseSchemaInterfaces) {
                baseSchemaUris.add(schemaLoader.getTypeUri(schemaInterface));
            }

            _Schema.getBaseSchemaUris().addAll(baseSchemaUris);
        }

        return this;
    }

    public SchemaBuilder extend(final Collection<URI> baseSchemaUris) {

        _Schema.getBaseSchemaUris().addAll(baseSchemaUris);
        return this;
    }

    public SchemaBuilder slots(final Map<String, Object> slotMap) {

        final Set<String> slotNames = slotMap.keySet();
        for (final String slotName : slotNames) {
            slot(slotName, slotMap.get(slotName));
        }

        return this;
    }

    public SchemaBuilder slots(final Object... slots) {

        if (slots == null) {
            throw new IllegalArgumentException("The slots cannot be null.");
        }

        if ((slots.length % 2) != 0) {
            throw new IllegalArgumentException("The slots must be in name/value pair order. Slots: " + Arrays.deepToString(slots));
        }

        for (int i = 0; i < slots.length; i++) {

            if (!(slots[i] instanceof String)) {
                throw new IllegalArgumentException("The slot's _definition_ array must contain a (String) name and (Object) value. Slots: " + Arrays.deepToString(slots));
            }

            final String slotName = (String) slots[i];

            i = i + 1;

            final Object slotValue = slots[i];

            slot(slotName, slotValue);
        }

        return this;
    }

    public SchemaBuilder slot(final String slotName, final Object slotValue) {

        if (slotName == null) {
            throw new IllegalArgumentException("Slot name cannot be null.");
        }

        final String validName = ensureValidJavaIdentifier(slotName);

        if (validName == null) {
            throw new IllegalArgumentException("Invalid slot name: " + slotName);
        }

        final ProtoSlot protoSlot = _Schema.getPrototype().getProtoSlot(validName, false);
        if (protoSlot != null) {
            _Schema.setSlotValue(validName, slotValue);
            return this;
        }

        final Slot slot = _Context.newModel(Slot.class);
        slot.setName(validName);

        final Value value = createValue(slotValue);
        if (value == null) {
            throw new IllegalArgumentException("Invalid slot value: " + slotValue);
        }

        slot.setValue(value);

        _Slots.put(validName, slot);
        _Schema.getSlots().add(slot);

        return this;
    }

    public SchemaBuilder link(final LinkRelation linkRelation) {

        return link(linkRelation.getUri(), linkRelation.getUniqueName().getLocalName());
    }

    public SchemaBuilder link(final URI linkRelationUri) {

        final SchemaLoader schemaLoader = _Context.getSchemaLoader();
        final Keys keys = new KeysBuilder().addKey(schemaLoader.getDocumentSchemaUri(), linkRelationUri).toKeys();
        final Dimensions dimensions = schemaLoader.getLinkRelationDimensions();
        final LinkRelation linkRelation = _Context.getModel(keys, dimensions);
        return link(linkRelation);
    }

    public SchemaBuilder link(final URI linkRelationUri, final String linkSlotName) {

        return link(linkRelationUri, linkSlotName, null);
    }

    public SchemaBuilder link(final URI linkRelationUri, final String linkSlotName, final URI responseSchemaUri) {

        return link(linkRelationUri, linkSlotName, responseSchemaUri, false);
    }

    public SchemaBuilder link(final URI linkRelationUri, final String linkSlotName, final URI responseSchemaUri, final boolean embedded) {

        if (linkRelationUri == null) {
            throw new IllegalArgumentException("Link Relation URI cannot be null.");
        }

        if (linkSlotName == null) {
            throw new IllegalArgumentException("Link slot name cannot be null.");
        }


        final String validName = ensureValidJavaIdentifier(linkSlotName);

        if (validName == null) {
            throw new IllegalArgumentException("Invalid link slot name: " + linkSlotName);
        }


        final Slot slot = _Context.newModel(Slot.class);
        slot.setName(validName);

        final LinkValue linkValue = _Context.newModel(LinkValue.class);
        linkValue.setLinkRelationUri(linkRelationUri);
        if (responseSchemaUri != null) {
            linkValue.setResponseSchemaUri(responseSchemaUri);
        }

        linkValue.setEmbedded(embedded);


        slot.setValue(linkValue);

        _Slots.put(validName, slot);
        _Schema.getSlots().add(slot);
        return this;

    }

    public SchemaBuilder key(final String keySlotName) {

        return keys(keySlotName, (String[]) null);
    }

    public SchemaBuilder keys(final String keySlotName, final String... keySlotNames) {

        if (keySlotName == null) {
            throw new IllegalArgumentException("Key slot name cannot be null.");
        }

        _Schema.getKeySlotNames().add(keySlotName);

        if (keySlotNames != null) {
            for (final String slotName : keySlotNames) {
                if (slotName == null) {
                    throw new IllegalArgumentException("Key slot name cannot be null.");
                }

                _Schema.getKeySlotNames().add(slotName);
            }
        }

        return this;
    }

    public SchemaBuilder keys(final Collection<String> keySlotNames) {

        _Schema.getKeySlotNames().addAll(keySlotNames);
        return this;
    }

    public Schema toSchema() {

        return _Schema;
    }

    public Context getContext() {

        return _Context;
    }

    public Slot getSlot(final String slotName) {

        if (!_Slots.containsKey(slotName)) {
            return null;
        }

        return _Slots.get(slotName);
    }

    public Class<?> load() throws ClassNotFoundException {

        final SchemaLoader schemaLoader = _Context.getSchemaLoader();
        final Schema schema = toSchema();
        schemaLoader.load(schema);
        return schemaLoader.getSchemaInterface(schema.getUri());
    }

    private Value createValue(final ValueType valueType) {

        return createValue(valueType, null);
    }

    private Value createValue(final Object value) {

        final Value slotValue;
        if (value instanceof ValueType) {
            slotValue = createValue((ValueType) value);
        }
        else if (value instanceof Class<?>) {
            slotValue = createValue((Class<?>) value);
        }
        else {
            slotValue = createValue(value.getClass());

            if (slotValue != null && slotValue.getPrototype().getProtoSlot(Value.SLOT_NAME_DEFAULT, false) != null) {
                slotValue.setSlotValue(Value.SLOT_NAME_DEFAULT, value);
            }
        }

        return slotValue;
    }

    private Value createValue(final Class<?> heapValueType) {

        final SchemaLoader schemaLoader = _Context.getSchemaLoader();
        final ValueType valueType = schemaLoader.getValueType(heapValueType);
        return createValue(valueType, heapValueType);
    }

    private Value createValue(final ValueType valueType, final Class<?> heapValueType) {

        final SchemaLoader schemaLoader = _Context.getSchemaLoader();
        final SyntaxLoader syntaxLoader = _Context.getSyntaxLoader();

        Value value = null;

        switch (valueType) {
            case Boolean:
                final BooleanValue booleanValue = _Context.newModel(BooleanValue.class);
                value = booleanValue;
                break;

            case Date:
                final DateValue dateValue = _Context.newModel(DateValue.class);
                value = dateValue;

                break;

            case Double:
                final DoubleValue doubleValue = _Context.newModel(DoubleValue.class);
                value = doubleValue;
                break;

            case Integer:
                final IntegerValue integerValue = _Context.newModel(IntegerValue.class);
                value = integerValue;
                break;

            case Model:

                final ModelValue modelValue = _Context.newModel(ModelValue.class);
                if (heapValueType != null) {
                    final URI modelSchemaUri = schemaLoader.getTypeUri(heapValueType);
                    modelValue.setModelSchemaUri(modelSchemaUri);
                }

                value = modelValue;
                break;

            case List:
                final ListValue listValue = _Context.newModel(ListValue.class);


                final Slot elementSlot = _Context.newModel(Slot.class);

                elementSlot.setName("E");

                final Type elementValueHeapType = ValueType.getListElementType(heapValueType);
                final Value elementValue = createValue(elementValueHeapType);
                elementSlot.setValue(elementValue);
                listValue.setElementSlot(elementSlot);

                value = listValue;
                break;

            case Long:
                final LongValue longValue = _Context.newModel(LongValue.class);
                value = longValue;
                break;

            case SingleSelect:
                final SingleSelectValue singleSelectValue = _Context.newModel(SingleSelectValue.class);
                value = singleSelectValue;
                break;

            case Text:
                final TextValue textValue = _Context.newModel(TextValue.class);

                if (heapValueType != null && !String.class.equals(heapValueType)) {
                    final URI syntaxUri = syntaxLoader.getSyntaxUri(heapValueType);
                    textValue.setSyntaxUri(syntaxUri);
                }

                value = textValue;

                break;

            default:
                break;

        }

        return value;

    }

}

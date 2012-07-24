/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wrml.runtime;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.reflect.TypeUtils;

import org.wrml.model.Model;
import org.wrml.model.schema.Constraint;
import org.wrml.model.schema.Key;
import org.wrml.model.schema.Schema;
import org.wrml.model.schema.Type;
import org.wrml.runtime.JavaBean.Property;
import org.wrml.util.Compare;
import org.wrml.util.Composition;
import org.wrml.util.observable.DelegatingObservableList;
import org.wrml.util.observable.ObservableList;
import org.wrml.util.observable.ObservableMap;
import org.wrml.util.observable.Observables;

/**
 * A runtime representation/descriptor of a {@link Schema}.
 */
public final class Prototype {

    public static final Class<?> LIST_TYPE = ObservableList.class;
    public static final Class<?> MAP_TYPE = ObservableMap.class;
    public static final Class<?> MODEL_TYPE = Model.class;
    public static final Class<?> COMPOSITION_TYPE = Composition.class;

    private final SchemaLoader _SchemaLoader;
    private final URI _SchemaId;
    private final JavaBean _SchemaBean;
    private final URI[] _AllBaseSchemaIds;

    private final ObservableMap<String, Prototype.Field> _PrototypeFields;

    private Schema _Schema;
    private java.lang.reflect.Type _KeyType;

    /**
     * Creates a new Prototype to represent the identified schema.
     * 
     * @param schemaLoader
     *            The schema loader for this prototype's schema.
     * 
     * @param schemaId
     *            The schema identifier.
     * 
     * @throws PrototypeException
     *             Thrown if there are problems with the initial prototyping of
     *             the schema.
     */
    Prototype(final SchemaLoader schemaLoader, final URI schemaId) throws PrototypeException {

        _SchemaLoader = schemaLoader;
        if (_SchemaLoader == null) {
            throw new PrototypeException("The SchemaLoader parameter value cannot be *null*.", null, this);
        }

        _SchemaId = schemaId;
        if (_SchemaId == null) {
            throw new PrototypeException("The undefined (aka *null*) schema can not be prototyped.", null, this);
        }

        /*
         * Use the SchemaLoader and the schema id to get the schema's Java Class
         * representation (aka its alternate).
         */
        final Class<?> schemaInterface = getSchemaInterface();

        _SchemaBean = new JavaBean(schemaInterface, Model.class);

        /*
         * Use Java reflection to get all implemented interfaces and then
         * turn them into schema ids. With reflection we get de-duplication and
         * recursive traversal for free.
         */

        final List<URI> allBaseSchemaIds = new ArrayList<URI>();
        final java.lang.reflect.Type[] genericInterfaces = schemaInterface.getGenericInterfaces();
        if (genericInterfaces != null) {
            for (final java.lang.reflect.Type genericInterface : genericInterfaces) {
                if (TypeUtils.isAssignable(genericInterface, MODEL_TYPE)) {
                    Class<?> baseSchemaInterface = null;
                    if (genericInterface instanceof ParameterizedType) {
                        final ParameterizedType parameterizedBaseSchemaInterface = (ParameterizedType) genericInterface;
                        baseSchemaInterface = (Class<?>) parameterizedBaseSchemaInterface.getRawType();
                    }
                    else if (genericInterface instanceof Class<?>) {
                        baseSchemaInterface = (Class<?>) genericInterface;
                    }
                    final String baseSchemaInterfaceName = baseSchemaInterface.getCanonicalName();
                    final URI baseSchemaId = _SchemaLoader.getSchemaId(baseSchemaInterfaceName);
                    allBaseSchemaIds.add(baseSchemaId);
                }
            }
        }

        final URI[] allBaseSchemaIdArray = new URI[allBaseSchemaIds.size()];
        _AllBaseSchemaIds = allBaseSchemaIds.toArray(allBaseSchemaIdArray);
        _PrototypeFields = Observables.observableMap(new TreeMap<String, Prototype.Field>());

    }

    public URI[] getAllBaseSchemaIds() {
        return _AllBaseSchemaIds;
    }

    public Set<String> getAllFieldNames() {
        return _SchemaBean.getProperties().keySet();
    }

    public java.lang.reflect.Type getKeyType() throws PrototypeException {

        if (_KeyType == null) {

            // TODO: Implement this using an inner Enum or Attributes associated with the schema's Java interface
            final Schema schema = getSchema();

            final Key key = schema.getKey();
            final ObservableList<String> keyFieldNames = key.getKeyFieldNames();

            // Get the field type for a singular list, use a composite for any multi-key values
            final JavaBean schemaBean = getSchemaBean();
            final Map<String, Property> properties = schemaBean.getProperties();

            if (keyFieldNames.size() == 1) {
                final String keyFieldName = keyFieldNames.get(0);
                final Property property = properties.get(keyFieldName);
                _KeyType = property.getType();
            }
            else {
                _KeyType = COMPOSITION_TYPE;
            }
        }

        return _KeyType;
    }

    // Needed for sharding and instance folding

    public Prototype.Field getPrototypeField(final String fieldName) {

        if (!_PrototypeFields.containsKey(fieldName)) {

            final Map<String, Property> properties = _SchemaBean.getProperties();
            if (!properties.containsKey(fieldName)) {
                throw new PrototypeException("A (WRML) field named \"" + fieldName
                        + "\" was not found within this prototype's schema interface ("
                        + _SchemaBean.getIntrospectedClass() + ")", null, this);
            }

            final Property property = properties.get(fieldName);
            final Prototype.Field protoField = new Prototype.Field(property);
            _PrototypeFields.put(fieldName, protoField);
        }

        return _PrototypeFields.get(fieldName);
    }

    public Schema getSchema() throws PrototypeException {

        if (_Schema == null) {
            final SchemaLoader schemaLoader = getSchemaLoader();
            final URI schemaId = getSchemaId();
            if (schemaId == null) {
                return null;
            }

            try {
                _Schema = schemaLoader.getSchema(schemaId);
            }
            catch (final SchemaLoaderException e) {
                throw new PrototypeException(e.getMessage(), e, this);
            }
        }

        return _Schema;
    }

    public JavaBean getSchemaBean() {
        return _SchemaBean;
    }

    public URI getSchemaId() {
        return _SchemaId;
    }

    public SchemaLoader getSchemaLoader() {
        return _SchemaLoader;
    }

    @Override
    public String toString() {
        return getClass().getName() + " [schemaId = " + _SchemaId + "]";
    }

    private Class<?> getSchemaInterface() throws PrototypeException {

        Class<?> schemaInterface = null;
        try {
            schemaInterface = _SchemaLoader.getSchemaInterface(_SchemaId);
            if (schemaInterface == null) {
                throw new PrototypeException("SchemaLoader returned a null Schema for: " + _SchemaId, null, this);
            }
        }
        catch (final Throwable t) {
            throw new PrototypeException("Interface not found for schema: " + String.valueOf(_SchemaId), t, this);
        }

        return schemaInterface;
    }

    /**
     * Prototype's field representation.
     */
    public class Field implements Comparable<Field> {

        /**
         * The WRML schema concept of Field (core part of the schematic
         * metadata).
         */
        private org.wrml.model.schema.Field _SchemaField;

        /** The Java Bean concept of Property */
        private final Property _Property;

        /** The assembled list of constraints */
        private final ObservableList<Constraint> _Constraints;

        /** The default value for models with this field. */
        private Object _DefaultValue;

        private Boolean _ModelType;

        private Boolean _ListType;
        private Boolean _MapType;
        private Boolean _ListOfModels;
        private Boolean _MapWithModelKeys;
        private Boolean _MapWithModelValues;

        private java.lang.reflect.Type _ListElementType;
        private java.lang.reflect.Type _MapKeyType;
        private java.lang.reflect.Type _MapValueType;

        private URI _ListElementSchemaId;
        private URI _MapKeySchemaId;
        private URI _MapValueSchemaId;
        private URI _ModelSchemaId;

        Field(final Property property) {

            if (property == null) {
                throw new NullPointerException("The Property parameter's value is expected to be non-null.");
            }

            _Property = property;
            _Constraints = new DelegatingObservableList<Constraint>(new ArrayList<Constraint>());
        }

        @Override
        public int compareTo(Field other) {
            return Compare.twoComparables(getProperty(), other.getProperty());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Field other = (Field) obj;
            if (!getPrototype().equals(other.getPrototype())) {
                return false;
            }
            if (_Property == null) {
                if (other._Property != null) {
                    return false;
                }
            }
            else if (!_Property.equals(other._Property)) {
                return false;
            }
            return true;
        }

        public ObservableList<Constraint> getConstraints() {
            // TODO: Lazy assemble this
            return _Constraints;
        }

        public URI getDeclaringSchemaId() {
            final Class<?> declaringClass = getProperty().getDeclaringClass();
            return getSchemaLoader().getSchemaId(declaringClass.getCanonicalName());
        }

        public Object getDefaultValue() {
            if (_DefaultValue == null) {
                final Property property = getProperty();
                final java.lang.reflect.Type type = property.getType();
                if (type.equals(Boolean.TYPE)) {
                    _DefaultValue = Boolean.FALSE;
                }
                else if (type.equals(Integer.TYPE)) {
                    _DefaultValue = 0;
                }
                else if (type.equals(Float.TYPE)) {
                    _DefaultValue = 0F;
                }
                else if (type.equals(Long.TYPE)) {
                    _DefaultValue = 0L;
                }
            }

            return _DefaultValue;
        }

        public String getDescription() {
            return getSchemaField().getDescription();
        }

        public URI getListElementSchemaId() {
            if (isListOfModels()) {
                return _ListElementSchemaId;
            }
            else {
                throw new PrototypeException("Field (" + getName() + ") is not a List of models.", null, getPrototype());
            }
        }

        public java.lang.reflect.Type getListElementType() {
            if (_ListElementType != null) {
                return _ListElementType;
            }

            if (!isListType()) {
                throw new PrototypeException("Field (" + getName() + ") is not a List.", null, getPrototype());
            }

            final java.lang.reflect.Type propertyType = getProperty().getType();
            final Map<TypeVariable<?>, java.lang.reflect.Type> typeArgs = TypeUtils.getTypeArguments(propertyType,
                    LIST_TYPE);

            // Get the first and only generic parameter type (the List's element type)
            final Iterator<java.lang.reflect.Type> parameterTypes = typeArgs.values().iterator();
            _ListElementType = parameterTypes.next();
            return _ListElementType;
        }

        public URI getMapKeySchemaId() {
            if (isMapWithModelKeys()) {
                return _MapKeySchemaId;
            }
            else {
                throw new PrototypeException("Field (" + getName() + ") is not a Map with model keys.", null,
                        getPrototype());
            }
        }

        public java.lang.reflect.Type getMapKeyType() {
            if (isMapType()) {
                if (_MapKeyType == null) {
                    initMapTypeInformation();
                }
                return _MapKeyType;
            }
            else {
                throw new PrototypeException("Field (" + getName() + ") is not a Map.", null, getPrototype());
            }
        }

        public URI getMapValueSchemaId() {
            if (isMapWithModelValues()) {
                return _MapValueSchemaId;
            }
            else {
                throw new PrototypeException("Field (" + getName() + ") is not a Map with model values.", null,
                        getPrototype());
            }
        }

        public java.lang.reflect.Type getMapValueType() {
            if (isMapType()) {
                if (_MapValueType == null) {
                    initMapTypeInformation();
                }
                return _MapValueType;
            }
            else {
                throw new PrototypeException("Field (" + getName() + ") is not a Map.", null, getPrototype());
            }
        }

        public URI getModelSchemaId() {
            if (isModelType()) {
                return _ModelSchemaId;
            }
            else {
                throw new PrototypeException("Field (" + getName() + ") is not a model type.", null, getPrototype());
            }
        }

        public String getName() {
            return getProperty().getName();
        }

        public Property getProperty() {
            return _Property;
        }

        public Prototype getPrototype() {
            return Prototype.this;
        }

        public org.wrml.model.schema.Field getSchemaField() {
            // TODO: Currently this is always null.
            return _SchemaField;
        }

        public URI getSchemaId() {
            return getPrototype().getSchemaId();
        }

        public Type getType() {
            return getSchemaField().getType();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + getPrototype().hashCode();
            result = (prime * result) + ((_Property == null) ? 0 : _Property.hashCode());
            return result;
        }

        public boolean isListOfModels() {

            if (_ListOfModels == null) {

                if (!isListType()) {
                    // Nope, it is not a List of Models since it isn't even a List field.
                    _ListOfModels = Boolean.FALSE;
                }
                else {

                    final java.lang.reflect.Type listElementType = getListElementType();

                    // Compare the List's element type to the Model type
                    if (TypeUtils.isAssignable(listElementType, MODEL_TYPE)) {

                        _ListOfModels = Boolean.TRUE;

                        // Set the element's schema id while we are already here.
                        final Class<?> listElementSchemaInterface = (Class<?>) listElementType;
                        final SchemaLoader schemaLoader = getSchemaLoader();
                        _ListElementSchemaId = schemaLoader.getSchemaId(listElementSchemaInterface.getCanonicalName());
                    }
                    else {
                        _ListOfModels = Boolean.FALSE;
                    }
                }
            }

            return _ListOfModels.booleanValue();
        }

        public boolean isListType() {

            if (_ListType == null) {

                final java.lang.reflect.Type propertyType = getProperty().getType();

                if (TypeUtils.isAssignable(propertyType, LIST_TYPE)) {
                    _ListType = Boolean.TRUE;
                }
                else {
                    _ListType = Boolean.FALSE;
                }
            }

            return _ListType.booleanValue();

        }

        public boolean isMapType() {

            if (_MapType == null) {

                final java.lang.reflect.Type propertyType = getProperty().getType();

                if (TypeUtils.isAssignable(propertyType, MAP_TYPE)) {
                    _MapType = Boolean.TRUE;
                }
                else {
                    _MapType = Boolean.FALSE;
                }
            }

            return _MapType.booleanValue();
        }

        public boolean isMapWithModelKeys() {

            if (_MapWithModelKeys == null) {
                initMapTypeInformation();
            }

            return _MapWithModelKeys.booleanValue();
        }

        public boolean isMapWithModelValues() {
            if (_MapWithModelValues == null) {
                initMapTypeInformation();
            }

            return _MapWithModelValues.booleanValue();
        }

        public boolean isModelType() {

            if (_ModelType == null) {

                final java.lang.reflect.Type propertyType = getProperty().getType();

                if (TypeUtils.isAssignable(propertyType, MODEL_TYPE)) {

                    _ModelType = Boolean.TRUE;

                    // Set the model field's schema id while we are already here.
                    final SchemaLoader schemaLoader = getSchemaLoader();
                    final Class<?> modelSchemaInterface = (Class<?>) propertyType;

                    _ModelSchemaId = schemaLoader.getSchemaId(modelSchemaInterface.getCanonicalName());
                }
                else {
                    _ModelType = Boolean.FALSE;
                }
            }

            return _ModelType.booleanValue();
        }

        @Override
        public String toString() {
            return getClass().getCanonicalName() + " : { \"property\" : { " + _Property + "}, \"schemaField\" : {"
                    + _SchemaField + "} }";
        }

        public void validateWrite(final Model model, final Object newValue) {
            // TODO: If any of the field's constranints have associated "Validators" load them (via code-on-demand), sandbox them, and run them.
        }

        private void initMapTypeInformation() {

            if (!isMapType()) {
                // Not a Map
                _MapKeyType = null;
                _MapValueType = null;
                _MapWithModelKeys = Boolean.FALSE;
                _MapWithModelValues = Boolean.FALSE;
                _MapKeySchemaId = null;
                _MapValueSchemaId = null;
                return;
            }

            final Property property = getProperty();
            final java.lang.reflect.Type propertyType = property.getType();

            final Map<TypeVariable<?>, java.lang.reflect.Type> typeArgs = TypeUtils.getTypeArguments(propertyType,
                    MAP_TYPE);

            final SchemaLoader schemaLoader = getSchemaLoader();

            // Get the maps's generic parameter types
            for (final TypeVariable<?> typeVar : typeArgs.keySet()) {
                if ("K".equals(typeVar.getName())) {

                    _MapKeyType = typeArgs.get(typeVar);

                    // Compare the Map's key type to the Model type
                    if (TypeUtils.isAssignable(_MapKeyType, MODEL_TYPE)) {
                        _MapWithModelKeys = Boolean.TRUE;

                        final Class<?> keySchemaInterface = (Class<?>) _MapKeyType;
                        _MapKeySchemaId = schemaLoader.getSchemaId(keySchemaInterface.getCanonicalName());
                    }
                    else {
                        _MapWithModelKeys = Boolean.FALSE;
                        _MapKeySchemaId = null;
                    }
                }
                else if ("V".equals(typeVar.getName())) {

                    _MapValueType = typeArgs.get(typeVar);

                    // Compare the Map's value type to the Model type
                    if (TypeUtils.isAssignable(_MapValueType, MODEL_TYPE)) {
                        _MapWithModelValues = Boolean.TRUE;

                        final Class<?> valueSchemaInterface = (Class<?>) _MapValueType;
                        _MapValueSchemaId = schemaLoader.getSchemaId(valueSchemaInterface.getCanonicalName());
                    }
                    else {
                        _MapWithModelValues = Boolean.FALSE;
                        _MapValueSchemaId = null;
                    }
                }
                else {
                    throw new PrototypeException("Map type variables are non-standard.", null, getPrototype());
                }
            }
        }
    }
}

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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.wrml.event.EventManager;
import org.wrml.event.EventSource;
import org.wrml.model.Model;
import org.wrml.model.schema.Constraint;
import org.wrml.model.schema.Field;
import org.wrml.model.schema.Schema;
import org.wrml.model.schema.Type;
import org.wrml.model.schema.constraint.SchemaConstraint;
import org.wrml.model.schema.constraint.text.SyntaxConstraint;
import org.wrml.util.observable.ObservableMap;
import org.wrml.util.observable.Observables;

/**
 * The SchemaLoader is a ClassLoader that is specialized to load WRML
 * Schema-based Java classes.
 */
public final class SchemaLoader extends ClassLoader implements EventSource<SchemaLoaderEventListener> {

    private final Context _Context;

    private final EventManager<SchemaLoaderEventListener> _EventManager;

    private final ObservableMap<String, URI> _SchemaIds;
    private final JavaBean _ModelJavaBean;
    private final ObservableMap<URI, Prototype> _Prototypes;
    private final ObservableMap<URI, Class<?>> _SchemaInterfaces;

    private final JavaBytecodeGenerator _JavaBytecodeGenerator;
    private final File _SchemaClassRootDirectory;

    private final URI _MetaschemaId;

    private final Dimensions _DefaultSchemaModelDimensions;
    private Dimensions _SchemaModelDimensions;

    private final Dimensions _DefaultConstraintModelDimensions;

    private Dimensions _ConstraintModelDimensions;

    SchemaLoader(Context context) {
        _Context = context;

        /*
         * Note that this directory value may be null, in which case the class
         * files will not be written to disk but will be loaded into the
         * runtime's memory only.
         */
        _SchemaClassRootDirectory = _Context.getEngine().getConfig().getSchemaClassRootDirectory();

        _EventManager = new EventManager<SchemaLoaderEventListener>(SchemaLoaderEventListener.class);

        _JavaBytecodeGenerator = new JavaBytecodeGenerator();

        _ModelJavaBean = new JavaBean(Model.class, null);

        _SchemaIds = Observables.observableMap(new TreeMap<String, URI>());
        _Prototypes = Observables.observableMap(new TreeMap<URI, Prototype>());
        _SchemaInterfaces = Observables.observableMap(new TreeMap<URI, Class<?>>());

        _MetaschemaId = getSchemaId(Schema.class.getName());

        _DefaultSchemaModelDimensions = new Dimensions(context);
        _DefaultSchemaModelDimensions.setRequestedSchemaId(_MetaschemaId);
        _SchemaModelDimensions = _DefaultSchemaModelDimensions;

        _DefaultConstraintModelDimensions = new Dimensions(context);
        _DefaultConstraintModelDimensions.setRequestedSchemaId(getSchemaId(Constraint.class.getName()));
        _ConstraintModelDimensions = _DefaultConstraintModelDimensions;

    }

    @Override
    public boolean addEventListener(SchemaLoaderEventListener eventListener) {
        return _EventManager.addEventListener(eventListener);
    }

    public Dimensions getConstraintModelDimensions() {
        return _ConstraintModelDimensions;
    }

    public Context getContext() {
        return _Context;
    }

    public Dimensions getDefaultConstraintModelDimensions() {
        return _DefaultConstraintModelDimensions;
    }

    public Dimensions getDefaultSchemaModelDimensions() {
        return _DefaultSchemaModelDimensions;
    }

    public JavaBytecodeGenerator getJavaBytecodeGenerator() {
        return _JavaBytecodeGenerator;
    }

    public URI getMetaschemaId() {
        return _MetaschemaId;
    }

    public Prototype getPrototype(URI schemaId) {

        if (!_Prototypes.containsKey(schemaId)) {
            final Prototype prototype = new Prototype(this, schemaId);
            _Prototypes.put(schemaId, prototype);

            if (_EventManager.isEventHearable()) {
                final SchemaLoaderEvent event = new SchemaLoaderEvent(this, prototype);
                _EventManager.fireEvent(SchemaLoaderEventListener.EventType.prototypeCreated, event);
            }
        }
        return _Prototypes.get(schemaId);
    }

    public Schema getSchema(URI schemaId) throws SchemaLoaderException {
        return getSchema(schemaId, getSchemaModelDimensions());
    }

    public Schema getSchema(URI schemaId, Dimensions dimensions) throws SchemaLoaderException {
        final Context context = getContext();
        return context.get(schemaId, dimensions);
    }

    public File getSchemaClassRootDirectory() {
        return _SchemaClassRootDirectory;
    }

    public URI getSchemaId(String schemaInterfaceName) {
        if (!_SchemaIds.containsKey(schemaInterfaceName)) {
            final URI schemaId = getSchemaApiDocrootId().resolve(schemaInterfaceName.replace('.', '/'));
            _SchemaIds.put(schemaInterfaceName, schemaId);
        }
        return _SchemaIds.get(schemaInterfaceName);
    }

    public Class<?> getSchemaInterface(URI schemaId) throws ClassNotFoundException {

        if (!_SchemaInterfaces.containsKey(schemaId)) {

            final String schemaInterfaceName = getSchemaApiDocrootId().relativize(schemaId).toString()
                    .replace('/', '.');

            Class<?> schemaInterface = null;

            try {
                /*
                 * First see if the schema interface is loadable by Java's
                 * system class loader. This is done primarily for bootstrapping
                 * WRML; allowing us to treat "standard" Java interfaces as if
                 * they were WRML schema-based.
                 */
                schemaInterface = getSystemClassLoader().loadClass(schemaInterfaceName);
            }
            catch (final ClassNotFoundException e) {
                // Swallow this exception and try to load the schema ourselves 
            }

            if (schemaInterface == null) {
                schemaInterface = loadClass(schemaInterfaceName, true);
            }

            _SchemaInterfaces.put(schemaId, schemaInterface);

            if (_EventManager.isEventHearable()) {
                final SchemaLoaderEvent event = new SchemaLoaderEvent(this, schemaInterface);
                _EventManager.fireEvent(SchemaLoaderEventListener.EventType.schemaInterfaceLoaded, event);
            }
        }

        return _SchemaInterfaces.get(schemaId);

    }

    public Dimensions getSchemaModelDimensions() {
        return _SchemaModelDimensions;
    }

    @Override
    public boolean removeEventListener(SchemaLoaderEventListener eventListener) {
        return _EventManager.removeEventListener(eventListener);
    }

    public void setConstraintModelDimensions(Dimensions constraintModelDimensions) {
        _ConstraintModelDimensions = constraintModelDimensions;
    }

    public void setSchemaModelDimensions(Dimensions schemaModelDimensions) {
        _SchemaModelDimensions = schemaModelDimensions;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected Class findClass(String name) throws ClassNotFoundException {

        final URI schemaId = getSchemaApiDocrootId().resolve(StringUtils.replace(name, ".", "/"));

        Schema schema;

        try {
            schema = getSchema(schemaId);
        }
        catch (final SchemaLoaderException sle) {
            throw new ClassNotFoundException(sle.getMessage(), sle);
        }

        if (schema != null) {

            //
            // The Schema POJO holds the WRML metadata about a data type; use it to 
            // generate a Java interface representation (bytecode).
            //

            final JavaBytecodeGenerator generator = getJavaBytecodeGenerator();
            final JavaBytecodeClass javaBytecodeClass = generator.generateSchemaInterface(schema);

            final byte[] bytecode = javaBytecodeClass.getBytecode();

            final File schemaClassRootDirectory = getSchemaClassRootDirectory();
            if (schemaClassRootDirectory != null) {

                // TODO: Can this be simplified? (just trying to construct a file path here...)
                final String[] splitResult = StringUtils.split(javaBytecodeClass.getInternalName());
                final int lastElementIndex = splitResult.length - 1;
                final String[] relativePath = ArrayUtils.subarray(splitResult, 0, lastElementIndex);
                final String classFileName = splitResult[lastElementIndex] + ".class";
                final File classFileDir = FileUtils.getFile(schemaClassRootDirectory, relativePath);
                final File classFileOnDisk = FileUtils.getFile(classFileDir, classFileName);

                try {
                    FileUtils.writeByteArrayToFile(classFileOnDisk, bytecode);
                }
                catch (final IOException e) {
                    throw new SchemaLoaderException("Failed to write class file (" + classFileOnDisk + ") for Schema ("
                            + schema + ")", e, this);
                }
            }

            return defineClass(name, bytecode, 0, bytecode.length);
        }

        return super.findClass(name);
    }

    /*
     * TODO: Future, this will need to be parameterized (by schema interface
     * name?) to allow for multiple/distributed Schema APIs to serve different
     * types
     */
    private URI getSchemaApiDocrootId() {

        final Context context = getContext();
        final Engine engine = context.getEngine();
        final EngineConfiguration config = engine.getConfig();

        final URI schemaApiDocrootId = config.getSchemaApiDocrootId();

        return schemaApiDocrootId;
    }

    /**
     * Internal class that holds a Java class file's data.
     */
    class JavaBytecodeClass {

        private String _InternalName;
        private String _Signature;
        private String _SuperName;
        private final List<String> _Interfaces;
        private final List<JavaBytecodeMethod> _Methods;

        private byte[] _Bytecode;

        JavaBytecodeClass() {
            _Interfaces = new ArrayList<String>();
            _Methods = new ArrayList<JavaBytecodeMethod>();
        }

        public byte[] getBytecode() {
            return _Bytecode;
        }

        public List<String> getInterfaces() {
            return _Interfaces;
        }

        public String getInternalName() {
            return _InternalName;
        }

        public List<JavaBytecodeMethod> getMethods() {
            return _Methods;
        }

        public String getSignature() {
            return _Signature;
        }

        public String getSuperName() {
            return _SuperName;
        }

        public void setBytecode(byte[] bytecode) {
            _Bytecode = bytecode;
        }

        public void setInternalName(String internalName) {
            _InternalName = internalName;
        }

        public void setSignature(String signature) {
            _Signature = signature;
        }

        public void setSuperName(String superName) {
            _SuperName = superName;
        }

    }

    /**
     * Internal class that generates Java bytecode (from a WRML schema).
     */
    class JavaBytecodeGenerator {

        /** JVM class file API version */
        private static final int JVM_VERSION = Opcodes.V1_5;

        /** The access modifiers for Java interfaces. */
        private static final int INTERFACE_ACCESS = Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE;

        /**
         * From a class file structure perspective, interfaces always extend
         * Object.
         */
        private static final String INTERFACE_SUPER_NAME = "java/lang/Object";

        /** The access modifiers for Java interface methods. */
        private static final int INTERFACE_METHOD_ACCESS = Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT;

        JavaBytecodeGenerator() {

        }

        /**
         * Generate a {@link JavaBytecodeClass} from the specified
         * {@link Schema}.
         * 
         * @param schema
         *            The Schema to represent as a Java class.
         * 
         * @return The Java class representation of the specified schema.
         */
        public JavaBytecodeClass generateSchemaInterface(final Schema schema) {

            final JavaBytecodeClass javaBytecodeClass = new JavaBytecodeClass();
            javaBytecodeClass.setSuperName(INTERFACE_SUPER_NAME);

            final URI schemaId = schema.getId();
            final String interfaceInternalName = schemaIdToClassName(schemaId);
            javaBytecodeClass.setInternalName(interfaceInternalName);

            final List<URI> baseSchemaIds = schema.getBaseSchemaIds();
            for (final URI baseSchemaId : baseSchemaIds) {
                final String baseSchemaInternalName = schemaIdToClassName(baseSchemaId);
                javaBytecodeClass.getInterfaces().add(baseSchemaInternalName);
            }

            final Context context = getContext();
            final Dimensions constraintDimensions = getConstraintModelDimensions();

            final List<Field> fields = schema.getFields();
            for (final Field field : fields) {

                final String fieldName = field.getName();
                if (_ModelJavaBean.getProperties().containsKey(fieldName)) {

                    // TODO: Filter other things like Java reserved words and symbols

                    throw new SchemaLoaderException("Sorry but \"" + fieldName
                            + "\" is reserved, it is not legal within a Schema.", null, SchemaLoader.this);
                }

                final String methodNameSuffix = StringUtils.capitalize(StringUtils.deleteWhitespace(fieldName));

                final Type fieldType = field.getType();

                final List<URI> constraintIds = field.getConstraintIds();
                List<Constraint> constraints = null;

                if (constraintIds != null) {
                    constraints = context.getMultiple(constraintIds, constraintDimensions);
                }

                // 
                // Determine the default Java type equivalence for the WRML type.
                //

                String javaTypeString = null;
                String javaTypeToken = "L";

                switch (fieldType) {

                default:
                case Text:

                    javaTypeString = "java/lang/String";

                    if (constraints != null) {
                        for (final Constraint constraint : constraints) {
                            if (constraint instanceof SyntaxConstraint) {
                                final SyntaxConstraint syntaxConstraint = (SyntaxConstraint) constraint;
                                final URI syntaxContstraintId = syntaxConstraint.getId();
                                final Class<?> syntaxJavaClass = context.getSyntaxJavaClass(syntaxContstraintId);
                                if (syntaxJavaClass != null) {
                                    javaTypeString = StringUtils.replace(syntaxJavaClass.getCanonicalName(), ".", "/");
                                }

                                break;
                            }
                        }
                    }

                    break;

                /*
                 * TODO: Should WRML support the "Native" type?
                 * 
                 * case Native:
                 * 
                 * javaTypeString = "java/lang/Object";
                 * 
                 * for (final Constraint constraint : constraints) {
                 * 
                 * final String nativeType = constraint.getNativeType();
                 * if (nativeType != null) {
                 * javaTypeString = nativeType;
                 * }
                 * 
                 * }
                 * 
                 * break;
                 */

                case Model:
                    javaTypeString = "org/wrml/model/Model";

                    if (constraints != null) {
                        for (final Constraint constraint : constraints) {
                            if (constraint instanceof SchemaConstraint) {
                                final SchemaConstraint schemaConstraint = (SchemaConstraint) constraint;
                                final URI constrainedSchemaId = schemaConstraint.getConstrainedSchemaId();
                                if (constrainedSchemaId != null) {
                                    javaTypeString = schemaIdToClassName(constrainedSchemaId);
                                }

                                break;
                            }
                        }
                    }

                    break;

                case Boolean:
                    javaTypeString = "java/lang/Boolean";

                    if (field.isValueRequired()) {
                        // No nulls allowed, convert from a "Boolean" to a "boolean"
                        javaTypeString = "";
                        javaTypeToken = "Z";
                    }

                    break;

                case List:

                    javaTypeString = "java/util/List";

                    //   // Signature: ()Ljava/util/List<Ljava/net/URI;>;

                    break;

                case Map:

                    javaTypeString = "java/util/Map";
                    break;

                case Choice:

                    javaTypeString = "java/lang/Enum";

                    // TODO: ChoiceMenus should code generate as enum and the specific enum type should should used here 
                    // TODO: Note that code generation of ChoiceMenus as enum will require some more advance bytecode generation, but it looks pretty straightforward with ASM.
                    break;

                case Integer:

                    javaTypeString = "java/lang/Integer";

                    if (field.isValueRequired()) {
                        // No nulls allowed, convert from a "Integer" to a "int"
                        javaTypeString = "";
                        javaTypeToken = "I";
                    }

                    break;

                case Date:

                    javaTypeString = "java/util/Date";
                    break;

                case Long:

                    javaTypeString = "java/lang/Long";

                    if (field.isValueRequired()) {
                        // No nulls allowed, convert from a "Long" to a "long"
                        javaTypeString = "";
                        javaTypeToken = "J";
                    }

                    break;

                case Float:

                    javaTypeString = "java/lang/Float";

                    if (field.isValueRequired()) {
                        // No nulls allowed, convert from a "Float" to a "float"
                        javaTypeString = "";
                        javaTypeToken = "F";
                    }

                    break;

                }

                if (!javaTypeString.isEmpty()) {
                    javaTypeString += ";";
                }

                //
                // Create an object to describe the *read* access method (aka "getter" method) that needs 
                // to be generated.
                //

                final String readMethodNamePrefix = (fieldType == Type.Boolean) ? TypeSystem.IS : TypeSystem.GET;
                final String readMethodName = readMethodNamePrefix + methodNameSuffix;

                final JavaBytecodeMethod readMethod = new JavaBytecodeMethod();
                readMethod.setName(readMethodName);

                // TODO: This will need to be done for generics
                readMethod.setSignature(null);

                final String readMethodDescriptor = "()" + javaTypeToken + javaTypeString;
                readMethod.setDescriptor(readMethodDescriptor);

                javaBytecodeClass.getMethods().add(readMethod);

                if (!field.isReadOnly()) {
                    //            
                    // Create an object to describe the *write* access method (aka "setter" method) that needs 
                    // to be generated.
                    //

                    final String writeMethodName = TypeSystem.SET + methodNameSuffix;

                    final JavaBytecodeMethod writeMethod = new JavaBytecodeMethod();
                    writeMethod.setName(writeMethodName);

                    // TODO: This will need to be done for generics
                    writeMethod.setSignature(null);

                    // TODO: Do we want the setters to return the previous value (like Map.put) or void ("V")?
                    //final String writeMethodDescriptor = "(" + javaTypeToken + javaTypeString + ")" + javaTypeToken + javaTypeString;
                    final String writeMethodDescriptor = "(" + javaTypeToken + javaTypeString + ")" + "V";
                    writeMethod.setDescriptor(writeMethodDescriptor);

                    javaBytecodeClass.getMethods().add(writeMethod);
                }

            }

            //
            // TODO: The signature will need to be changed for generics:
            // Example:
            //
            // Java: public interface Test<T extends List<?>> extends List<T> 
            // Class File: public abstract interface org.wrml.schema.Test extends java.util.List
            // Signature: <T::Ljava/util/List<*>;>Ljava/lang/Object;Ljava/util/List<TT;>;
            //

            javaBytecodeClass.setSignature(null);

            generateSchemaInterfaceBytecode(javaBytecodeClass);
            return javaBytecodeClass;
        }

        /**
         * Handles the actual JVM bytecode generation.
         * 
         * @param classFile
         *            The Java class file information used to drive the bytecode
         *            generation.
         */
        private void generateSchemaInterfaceBytecode(final JavaBytecodeClass classFile) {

            final ClassWriter schemaInterfaceWriter = new ClassWriter(0);

            final List<String> interfaces = classFile.getInterfaces();
            String[] interfaceNames = new String[interfaces.size()];
            interfaceNames = (interfaceNames.length > 0) ? interfaces.toArray(interfaceNames) : null;

            // Generate the interface declaration
            schemaInterfaceWriter.visit(JVM_VERSION, INTERFACE_ACCESS, classFile.getInternalName(),
                    classFile.getSignature(), classFile.getSuperName(), interfaceNames);

            final List<JavaBytecodeMethod> methods = classFile.getMethods();

            for (final JavaBytecodeMethod method : methods) {

                final List<String> exceptions = method.getExceptions();
                String[] exceptionNames = new String[exceptions.size()];
                exceptionNames = (exceptionNames.length > 0) ? exceptions.toArray(exceptionNames) : null;

                final MethodVisitor methodVisitor = schemaInterfaceWriter.visitMethod(INTERFACE_METHOD_ACCESS,
                        method.getName(), method.getDescriptor(), method.getSignature(), exceptionNames);

                // If we weren't simply generating Java interfaces, things would get more _advanced_ right here.

                methodVisitor.visitEnd();
            }

            // Finish the code generation
            schemaInterfaceWriter.visitEnd();

            final byte[] bytecode = schemaInterfaceWriter.toByteArray();
            classFile.setBytecode(bytecode);
        }

        private String schemaIdToClassName(URI schemaId) {
            // "Convert" the schema id (URI) to a Java interface name.
            return StringUtils.removeStart(schemaId.getPath(), "/");
        }
    }

    /**
     * Internal class that holds a Java class file's method data.
     */
    class JavaBytecodeMethod {

        private String _Name;
        private String _Descriptor;
        private String _Signature;
        private final List<String> _Exceptions;

        JavaBytecodeMethod() {
            _Exceptions = new ArrayList<String>();
        }

        public String getDescriptor() {
            return _Descriptor;
        }

        public List<String> getExceptions() {
            return _Exceptions;
        }

        public String getName() {
            return _Name;
        }

        public String getSignature() {
            return _Signature;
        }

        public void setDescriptor(String descriptor) {
            _Descriptor = descriptor;
        }

        public void setName(String name) {
            _Name = name;
        }

        public void setSignature(String signature) {
            _Signature = signature;
        }
    }
}

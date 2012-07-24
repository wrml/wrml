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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.wrml.util.UriTemplate;

/**
 * An "implementation" of the WRML engine configuration data structure.
 */
public final class EngineConfiguration {

    public final static String DEFAULT_FILE_NAME = "wrml.json";

    public final static EngineConfiguration load() throws IOException {
        return load(DEFAULT_FILE_NAME);
    }

    public final static EngineConfiguration load(File fileOrDirectory) throws IOException {

        if (fileOrDirectory == null) {
            // Look in the local directory for the configuration file with the default name
            fileOrDirectory = FileUtils.getFile(new File("."), DEFAULT_FILE_NAME);
            if (!fileOrDirectory.exists()) {
                fileOrDirectory = FileUtils.getFile(FileUtils.getUserDirectory(), DEFAULT_FILE_NAME);
            }
        }
        else if (fileOrDirectory.isDirectory()) {
            // Named a directory (assume default file name)
            fileOrDirectory = FileUtils.getFile(fileOrDirectory, DEFAULT_FILE_NAME);
        }
        else if (!fileOrDirectory.exists()) {
            throw new FileNotFoundException("The WRML engine configuration file named \"" + fileOrDirectory
                    + "\" does not exist. Bummer!");
        }

        final InputStream in = FileUtils.openInputStream(fileOrDirectory);
        final EngineConfiguration config = load(in);
        IOUtils.closeQuietly(in);
        return config;
    }

    public final static EngineConfiguration load(File directory, String fileName) throws IOException {
        return load(FileUtils.getFile(directory, fileName));
    }

    public final static EngineConfiguration load(InputStream in) throws IOException {
        return new ObjectMapper().readValue(in, EngineConfiguration.class);
    }

    public final static EngineConfiguration load(String fileName) throws IOException {
        return load(FileUtils.getFile(fileName));
    }

    public final static EngineConfiguration load(URI configLocation) throws IOException {
        return load(configLocation.toURL());
    }

    public final static EngineConfiguration load(URL configLocation) throws IOException {
        // http://www.unitconversion.org/time/seconds-to-milliseconds-conversion.html

        final int connectionTimeoutMilliseconds = 10000;
        final int readTimeoutMilliseconds = 10000;
        final File tempFile = FileUtils.getFile(FileUtils.getTempDirectory(), DEFAULT_FILE_NAME);
        FileUtils.copyURLToFile(configLocation, tempFile, connectionTimeoutMilliseconds, readTimeoutMilliseconds);
        return load(tempFile);
    }

    // TODO: Implement "store" equivalents for Config writing

    private URI _SchemaApiDocrootId;
    private File _SchemaJsonRootDirectory;
    private File _SchemaClassRootDirectory;
    private boolean _StrictlyStatic;
    private URI _DefaultFormatId;
    private ServiceMapping[] _ServiceMappings;
    private SyntaxConstraintHandler[] _SyntaxConstraintHandlers;

    public URI getDefaultFormatId() {
        return _DefaultFormatId;
    }

    public URI getSchemaApiDocrootId() {
        return _SchemaApiDocrootId;
    }

    public File getSchemaClassRootDirectory() {
        return _SchemaClassRootDirectory;
    }

    public File getSchemaJsonRootDirectory() {
        return _SchemaJsonRootDirectory;
    }

    public ServiceMapping[] getServiceMappings() {
        return _ServiceMappings;
    }

    public SyntaxConstraintHandler[] getSyntaxConstraintHandlers() {
        return _SyntaxConstraintHandlers;
    }

    public boolean isStrictlyStatic() {
        return _StrictlyStatic;
    }

    public void setDefaultFormatId(URI defaultFormatId) {
        _DefaultFormatId = defaultFormatId;
    }

    public void setSchemaApiDocrootId(URI schemaApiDocrootId) {
        _SchemaApiDocrootId = schemaApiDocrootId;
        if ((_SchemaApiDocrootId != null) && !_SchemaApiDocrootId.getPath().endsWith("/")) {
            _SchemaApiDocrootId = _SchemaApiDocrootId.resolve("/");
        }
    }

    public void setSchemaClassRootDirectory(File schemaClassRootDirectory) {
        _SchemaClassRootDirectory = schemaClassRootDirectory;
    }

    public void setSchemaJsonRootDirectory(File schemaJsonRootDirectory) {
        _SchemaJsonRootDirectory = schemaJsonRootDirectory;
    }

    public void setServiceMappings(ServiceMapping[] serviceMappings) {
        _ServiceMappings = serviceMappings;
    }

    public void setStrictlyStatic(boolean strictlyStatic) {
        _StrictlyStatic = strictlyStatic;
    }

    public void setSyntaxConstraintHandlers(SyntaxConstraintHandler[] syntaxConstraintHandlers) {
        _SyntaxConstraintHandlers = syntaxConstraintHandlers;
    }

    public static class ServiceMapping {

        private String _ServiceClassName;
        private URI[] _SchemaIds;
        private UriTemplate[] _SchemaIdTemplates;

        public ServiceMapping() {

        }

        public URI[] getSchemaIds() {
            return _SchemaIds;
        }

        public UriTemplate[] getSchemaIdTemplates() {
            return _SchemaIdTemplates;
        }

        public String getServiceClassName() {
            return _ServiceClassName;
        }

        public void setSchemaIds(URI[] schemaIds) {
            _SchemaIds = schemaIds;
        }

        public void setSchemaIdTemplates(UriTemplate[] schemaIdTemplates) {
            _SchemaIdTemplates = schemaIdTemplates;
        }

        public void setServiceClassName(String serviceClassName) {
            _ServiceClassName = serviceClassName;
        }
    }

    public static class SyntaxConstraintHandler {

        private URI _SyntaxConstraintId;
        private String _ToStringTransformerClassName;

        public URI getSyntaxConstraintId() {
            return _SyntaxConstraintId;
        }

        public String getToStringTransformerClassName() {
            return _ToStringTransformerClassName;
        }

        public void setSyntaxConstraintId(URI syntaxConstraintId) {
            _SyntaxConstraintId = syntaxConstraintId;
        }

        public void setToStringTransformerClassName(String toStringTransformerClassName) {
            _ToStringTransformerClassName = toStringTransformerClassName;
        }
    }
}

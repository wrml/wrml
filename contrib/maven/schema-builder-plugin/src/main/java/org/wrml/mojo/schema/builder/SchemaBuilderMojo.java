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
package org.wrml.mojo.schema.builder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.DefaultEngine;
import org.wrml.runtime.Engine;
import org.wrml.runtime.EngineConfiguration;
import org.wrml.runtime.format.application.schema.json.JsonSchema;
import org.wrml.runtime.format.application.schema.json.JsonSchemaLoader;
import org.wrml.runtime.rest.SystemApi;
import org.wrml.runtime.schema.SchemaLoader;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

/**
 * Goal which touches a timestamp file.
 *
 * @goal model
 * @phase process-resources
 */
public class SchemaBuilderMojo extends AbstractMojo {

    public static final String MANIFEST_NAME = "manifest.txt";

    /**
     * Project's source directory as specified in the POM.
     *
     * @parameter expression="${project.build.sourceDirectory}"
     * @readonly
     * @required
     */
    private File _ResourcesDirectory;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File _OutputDirectory;

    private Engine _Engine;

    private URI _RootJsonSchemaUri;

    @SuppressWarnings("unused")
    private URI _RootWrmlModelUri;

    private enum SourceType {
        JSON_SCHEMA,
        WRML_MODEL
    }


    public void execute() throws MojoExecutionException {
        /*
         * Recursive decent into the _ResourcesDirectory looking for files with
		 * the extension .mdl These are JSON files that describe a model. Read
		 * each .mdl file and generate a class in the target directory that
		 * implements the model.
		 */
        File mainDir = _ResourcesDirectory.getParentFile();
        if (!mainDir.isDirectory()) {
            throw new MojoExecutionException(String.format(
                    "%s is not a directory.", _ResourcesDirectory.getPath()));
        }


        File rootWrmlModelDir = new File(mainDir, "wrml-model");
        File rootJsonSchemaDir = new File(mainDir, "json-schema");

        getLog().info("Expected JsonSchema Location: " + rootJsonSchemaDir.toString());
        getLog().info("Expected Wrml Model location: " + rootWrmlModelDir.toString());
        Path rootWrmlModelPath = rootWrmlModelDir.toPath();
        Path rootJsonSchemaPath = rootJsonSchemaDir.toPath();

        _RootWrmlModelUri = rootWrmlModelPath.toUri();
        _RootJsonSchemaUri = rootJsonSchemaPath.toUri();

        try {
            _Engine = createEngine();
        }
        catch (IOException e) {
            throw new MojoExecutionException("Error creating wrml engine.", e);
        }

        try {
            if (rootJsonSchemaDir.isDirectory()) {
                getLog().info("Processing: " + rootJsonSchemaDir.toString());
                File manifest = new File(rootJsonSchemaDir + File.separator + MANIFEST_NAME);
                if (manifest.exists() && manifest.isFile()) {
                    getLog().info("Using file Manifest...");
                    decendManifest(manifest);
                }
                else {
                    getLog().info("No Manifest at " + manifest.getAbsolutePath() + ", decending per directories...");
                    decend(rootJsonSchemaDir, SourceType.JSON_SCHEMA);
                }
            }
            if (rootWrmlModelDir.isDirectory()) {
                getLog().info("Processing: " + rootWrmlModelDir.toString());
                decend(rootWrmlModelDir, SourceType.WRML_MODEL);
            }
        }
        catch (Exception e) {
            throw new MojoExecutionException("Error generating models.", e);
        }
    }

    // Only do this for JsonSchemas....
    private void decendManifest(File manifestFile) throws IOException, ClassNotFoundException {

        List<String> files = IOUtils.readLines(new FileInputStream(manifestFile));
        String relPath = manifestFile.getParentFile().getAbsolutePath();
        for (String file : files) {
            File rawSchema = new File(relPath + File.separator + file);
            generateJsonSchema(rawSchema);
        }
    }

    private void decend(File parentDir, SourceType sourceType) throws IOException, ClassNotFoundException {

        if (sourceType == SourceType.JSON_SCHEMA) {
            File models[] = parentDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {

                    return name.toLowerCase().endsWith(".json");
                }
            });
            for (File model : models) {
                generateJsonSchema(model);
            }
        }
        else if (sourceType == SourceType.WRML_MODEL) {
            /* TODO: Uncomment the contents of this block when the
             *  generateWrmlModel method is fixed to work correctly.
			 *  
			File models[] = parentDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".mdl");
				}
			});

			for (File model : models) {
				generateWrmlModel(model);
			}
			*/
        }

        File subDirectories[] = parentDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {

                return pathname.isDirectory();
            }
        });
        for (File subDirectory : subDirectories) {
            decend(subDirectory, sourceType);
        }
    }

    private URI createModelUri(File sourceFile, URI rootUri) {

        URI relativeUri = rootUri.relativize(sourceFile.toPath().toUri());
        String modelUriString = SystemApi.Schema.getUri().toString() + "/" + relativeUri.toString();
        modelUriString = modelUriString.substring(0, modelUriString.lastIndexOf("."));
        getLog().info("Model URI created from " + relativeUri + ", and root " + rootUri + ", is " + modelUriString);
        return URI.create(modelUriString);
    }

    private void generateJsonSchema(File sourceFile) throws IOException, ClassNotFoundException {

        getLog().info("Working: " + sourceFile.getAbsolutePath());
        SchemaLoader loader = _Engine.getContext().getSchemaLoader();
        InputStream in = FileUtils.openInputStream(sourceFile);
        JsonSchemaLoader jsonSchemaLoader = loader.getJsonSchemaLoader();
        JsonSchema jsonSchema = jsonSchemaLoader.load(in, createModelUri(sourceFile, _RootJsonSchemaUri));
        Schema schema = loader.load(jsonSchema);
        Class<?> classz = loader.getSchemaInterface(schema.getUri());
        getLog().info("Created: " + classz.getName());
    }

	/* TODO: Uncomment and fix this so that it works 
	private void generateWrmlModel(File sourceFile) throws IOException, ClassNotFoundException {
		SchemaLoader loader = _Engine.getContext().getSchemaLoader();
		Schema schema = loader.load(sourceFile.toURI());
		Class<?> classz = loader.getSchemaInterface(schema.getUri());
		getLog().info("Created: " + classz.getName());
	}
	*/

    private EngineConfiguration createEngineConfig() throws IOException {

        EngineConfiguration config = EngineConfiguration.load(this.getClass(), "wrml.json");
        config.getContext().getSchemaLoader().setSchemaClassRootDirectory(_OutputDirectory);
        getLog().info("config.context.schemaLoader.schemaClassRootDirectory = "
                + config.getContext().getSchemaLoader().getSchemaClassRootDirectory().toString());
        return config;
    }

    public final Engine createEngine() throws IOException {

        final Engine engine = new DefaultEngine();
        engine.init(createEngineConfig());
        return engine;
    }
}
 
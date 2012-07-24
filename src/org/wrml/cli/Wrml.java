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

package org.wrml.cli;

import java.net.URI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.ArrayUtils;

import org.wrml.cli.gui.Gui;
import org.wrml.model.Model;
import org.wrml.model.ModelEvent;
import org.wrml.model.ModelEventListener;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Engine;
import org.wrml.runtime.EngineConfiguration;

/**
 * This class represents the execution entry point for the Java implementation
 * of WRML. This is Java WRML's command line interface (CLI).
 */
public final class Wrml {

    private final static Options OPTIONS = new Options();

    static {

        final OptionDescriptor[] optionDescriptors = OptionDescriptor.values();

        for (final OptionDescriptor optionDescriptor : optionDescriptors) {
            OPTIONS.addOption(optionDescriptor.toOption());
        }

    }

    public static void main(String[] args) throws Exception {

        // Create the command line parser
        final CommandLineParser parser = new GnuParser();

        // Parse the command line arguments
        final CommandLine commandLine = parser.parse(OPTIONS, args);

        if ((args == null) || (args.length == 0) || commandLine.hasOption(OptionDescriptor.help.getName())
                || commandLine.hasOption(OptionDescriptor.usage.getName())) {

            // Automatically generate the help statement
            final HelpFormatter formatter = new HelpFormatter();

            formatter.printHelp("wrml <engine configuration file>", OPTIONS);
            return;
        }

        // Load the engine config
        final String configFileName = commandLine.getArgs()[0];
        final EngineConfiguration config = EngineConfiguration.load(configFileName);

        // Create the engine
        final Engine engine = new Engine(config);

        //SimpleTest.run(engine);

        // OR?
        startGui(engine);
    }

    private static void startGui(Engine engine) {
        new Gui(engine);
    }

    private static class SimpleTest {

        private static void run(Engine engine) {
            // Get the engine's default context and schema loader
            final Context context = engine.getDefaultContext();

            final Dimensions dimensions = new Dimensions(context);
            dimensions.setRequestedSchemaId(URI.create("http://api.schema.wrml.org/example/Foo"));
            final Model fooModel = context.get(URI.create("http://www.wrml.org/example/foo"), dimensions);
            System.out.println("Foo methods: " + ArrayUtils.toString(fooModel.getClass().getMethods()));

            fooModel.addEventListener(new ModelEventListener() {

                @Override
                public void onFieldValueSet(ModelEvent event) {
                    System.out.println("onFieldValueSet(" + event + ")");
                }

                @Override
                public void onFieldValueUnset(ModelEvent event) {
                    System.out.println("onFieldValueUnset(" + event + ")");
                }
            });

            System.out.println("Foo message: " + fooModel.getFieldValue("message"));
            fooModel.setFieldValue("message", "Greetings Program!");
            System.out.println("Foo message: " + fooModel.getFieldValue("message"));
        }
    }

}

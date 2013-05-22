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
package org.wrml.runtime.format.application.json;

import java.io.IOException;
import java.io.OutputStream;

import org.wrml.runtime.format.ModelPrinter;
import org.wrml.runtime.format.ModelPrinterException;
import org.wrml.runtime.format.ModelPrinterFactory;
import org.wrml.runtime.format.ModelWriteOptions;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class JsonModelPrinterFactory implements ModelPrinterFactory
{

    @Override
    public ModelPrinter createModelPrinter(final OutputStream out, final ModelWriteOptions writeOptions)
            throws IOException, ModelPrinterException
    {
        final JsonFactory jsonFactory = new JsonFactory();
        final JsonGenerator jsonGenerator;

        try
        {
            jsonGenerator = jsonFactory.createGenerator(out);
        }
        catch (final IOException e)
        {
            throw new ModelPrinterException(
                    "An serious I/O related problem has occurred while attempting to print a Model.", e, null);
        }

        final JsonModelPrinter printer = new JsonModelPrinter(jsonGenerator, writeOptions);
        return printer;
    }

}

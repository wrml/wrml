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
package org.wrml.runtime.format;

import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.DefaultConfiguration;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

/**
 * The <i>pluggable</i> {@link Formatter} implementation.
 *
 * @see ModelReader
 * @see ModelWriter
 */
public class PluggableFormatter extends AbstractFormatter
{

    public static final String PARSER_FACTORY_SETTING_NAME = "parserFactory";

    public static final String PRINTER_FACTORY_SETTING_NAME = "printerFactory";

    private ModelReader _ModelReader;

    private ModelWriter _ModelWriter;

    public PluggableFormatter()
    {

    }

    @Override
    protected void initFromConfiguration(final FormatterConfiguration config)
    {

        final Map<String, String> settings = config.getSettings();
        if (settings == null || settings.isEmpty())
        {
            throw new IllegalArgumentException("The Format settings cannot be null.");
        }

        final Context context = getContext();
        final URI formatUri = getFormatUri();

        final ModelParserFactory parserFactory;
        final String modelParserFactoryClassName = settings.get(PARSER_FACTORY_SETTING_NAME);
        if (modelParserFactoryClassName != null)
        {
            parserFactory = DefaultConfiguration.newInstance(modelParserFactoryClassName);

            if (parserFactory != null)
            {
                _ModelReader = new ModelReader(context, formatUri, parserFactory);
            }
        }

        final ModelPrinterFactory printerFactory;
        final String modelPrinterFactoryClassName = settings.get(PRINTER_FACTORY_SETTING_NAME);
        if (modelPrinterFactoryClassName != null)
        {
            printerFactory = DefaultConfiguration.newInstance(modelPrinterFactoryClassName);
            if (printerFactory != null)
            {
                _ModelWriter = new ModelWriter(context, formatUri, printerFactory);
            }
        }

        if (_ModelReader == null && _ModelWriter == null)
        {
            throw new IllegalArgumentException("The Format must configure both/either a " + PARSER_FACTORY_SETTING_NAME +
                    " and/or a " + PRINTER_FACTORY_SETTING_NAME + ".");
        }

    }

    @Override
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys,
                                         final Dimensions rootModelDimensions) throws ModelReadingException
    {

        if (_ModelReader == null)
        {
            throw new UnsupportedOperationException("The \"readModel\" operation is not supported by the \"" + getFormatUri() + "\" format.");
        }

        return _ModelReader.readModel(in, rootModelKeys, rootModelDimensions);
    }

    @Override
    public void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException
    {

        if (_ModelWriter == null)
        {
            throw new UnsupportedOperationException("The \"writeModel\" operation is not supported by the \"" + getFormatUri() + "\" format.");
        }

        _ModelWriter.writeModel(out, model, writeOptions);
    }

}

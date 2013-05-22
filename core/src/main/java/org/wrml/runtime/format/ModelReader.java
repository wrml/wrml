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

package org.wrml.runtime.format;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;

/**
 * Reads a {@link Model}, relying on the {@link ParserModelGraph} for the
 * semantic analysis.
 * 
 * @see PluggableFormatter
 */
class ModelReader
{

    private static final Logger LOG = LoggerFactory.getLogger(ModelReader.class);

    /**
     * The context to operate within.
     */
    private final Context _Context;

    private final URI _FormatUri;

    private final ModelParserFactory _ModelParserFactory;

    /**
     * Creates a new model reader.
     * 
     * @param context
     *            The context to operate within.
     */
    public ModelReader(final Context context, final URI formatUri, final ModelParserFactory modelParserFactory)
    {

        _Context = context;

        _FormatUri = formatUri;

        _ModelParserFactory = modelParserFactory;
    }

    public Context getContext()
    {
        return _Context;
    }

    public URI getFormatUri()
    {
        return _FormatUri;
    }

    public ModelParserFactory getModelParserFactory()
    {
        return _ModelParserFactory;
    }

    /**
     * Read a model of unknown schema using the default format from the
     * specified stream with the indicated options applied.
     * 
     * @param in
     *            The input stream containing formatted bytes that represent the
     *            model graph to be read.
     * 
     * @param rootModelKeys
     * 
     * @param rootModelDimensions
     *            The dimensions to apply to the graph's root model.
     * 
     * @return The initialized model representing the root of the input stream's
     *         model graph.
     * 
     * @throws ModelReaderException
     *             Thrown if any problems are encountered.
     */
    @SuppressWarnings("unchecked")
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys,
            final Dimensions rootModelDimensions) throws ModelReaderException
    {
        if (rootModelDimensions == null)
        {
            throw new ModelReaderException("The root model Dimensions cannot be null.", null, this);
        }

        if (rootModelDimensions.getSchemaUri() == null)
        {
            throw new ModelReaderException("The root model Schema URI cannot be null.", null, this);
        }

        LOG.debug("Reading a model with dimensions:\n{}", rootModelDimensions);

        final Context context = getContext();

        final ModelParserFactory parserFactory = getModelParserFactory();
        final ModelParser parser;
        try
        {
            parser = parserFactory.createModelParser(in);
        }
        catch (final IOException e)
        {
            throw new ModelReaderException(e.getMessage(), e, this);
        }
        catch (final ModelParserException e)
        {
            throw new ModelReaderException(e.getMessage(), e, this);
        }

        final ParserModelGraph parserModelGraph = new ParserModelGraph(context, rootModelKeys, rootModelDimensions);

        Model model = null;
        ModelToken token;

        try
        {
            while ((token = parser.parseNextToken()) != null)
            {
                switch (token)
                {
                case MODEL_START:
                {
                    LOG.debug("ModelReader: Starting model");
                    parserModelGraph.startModel();
                    break;
                }
                case MODEL_END:
                {
                    LOG.debug("ModelReader: Finishing model");
                    model = parserModelGraph.endModel();
                    break;
                }
                case SLOT_NAME:
                {
                    final String slotName = parser.parseSlotName();
                    LOG.debug("ModelReader: Reading model slot: {}", slotName);
                    parserModelGraph.startSlot(slotName);
                    break;
                }
                case LIST_START:
                {
                    LOG.debug("ModelReader: Starting list");
                    parserModelGraph.startList();
                    break;
                }
                case LIST_END:
                {
                    LOG.debug("ModelReader: Finishing list");
                    parserModelGraph.endList();
                    break;
                }
                case VALUE_TEXT:
                {
                    final String stringValue = parser.parseTextValue();
                    LOG.debug("ModelReader: Read text: {}", stringValue);
                    parserModelGraph.endValue(stringValue);
                    break;
                }
                case VALUE_NULL:
                {
                    LOG.debug("ModelReader: Read NULL value");
                    parserModelGraph.endValue(null);
                    break;
                }
                case VALUE_TRUE:
                {
                    LOG.debug("ModelReader: Read TRUE");
                    parserModelGraph.endValue(Boolean.TRUE);
                    break;
                }
                case VALUE_FALSE:
                {
                    LOG.debug("ModelReader: Read FALSE");
                    parserModelGraph.endValue(Boolean.FALSE);
                    break;
                }
                case VALUE_INTEGER:
                {
                    final Integer integerValue = parser.parseIntegerValue();
                    LOG.debug("ModelReader: Read number: {}", integerValue);
                    parserModelGraph.endValue(integerValue);
                    break;
                }
                case VALUE_DOUBLE:
                {
                    final Double doubleValue = parser.parseDoubleValue();
                    LOG.debug("ModelReader: Read number: {}", doubleValue);
                    parserModelGraph.endValue(doubleValue);
                    break;
                }
                default:
                {
                    break;
                }
                }
            }
        }
        catch (final IOException e)
        {
            LOG.error(e.getMessage(), e);
            throw new ModelReaderException("Encountered an I/O related problem while attempting to read a model: "
                    + e.getMessage(), e, this);
        }
        catch (final Exception e)
        {

            LOG.error(e.getMessage(), e);
            throw new ModelReaderException("Encountered an issue while attempting to read a model: " + e.getMessage(),
                    e, this);

        }

        return (M) model;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + " [Format = " + _FormatUri + "]";
    }

}

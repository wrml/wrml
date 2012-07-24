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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.wrml.event.EventManager;
import org.wrml.event.EventSource;
import org.wrml.model.Model;

/**
 * Reads a {@link Model}, relying on the {@link ModelGraph} for the semantic
 * analysis.
 */
public final class ModelReader implements EventSource<ModelReaderEventListener> {

    /** The context to operate within. */
    private final Context _Context;

    /** Manages the implementation of {@link EventSource} */
    private final EventManager<ModelReaderEventListener> _EventManager;

    /**
     * Creates a new model reader.
     * 
     * @param context
     *            The context to operate within.
     */
    public ModelReader(Context context) {
        _Context = context;
        _EventManager = new EventManager<ModelReaderEventListener>(ModelReaderEventListener.class);
    }

    @Override
    public boolean addEventListener(ModelReaderEventListener eventListener) {
        return _EventManager.addEventListener(eventListener);
    }

    public Context getContext() {
        return _Context;
    }

    /**
     * Read a model of unknown schema using the default format from the
     * specified stream.
     * 
     * @param in
     *            The input stream containing formatted bytes that represent the
     *            model graph to be read.
     * 
     * @return The initialized model representing the root of the input stream's
     *         model graph.
     * 
     * @throws ModelReaderException
     *             Thrown if any problems are encountered.
     */
    public <M extends Model> M readModel(InputStream in) throws ModelReaderException {
        return readModel(in, null);
    }

    /**
     * Read a model of unknown schema using the default format from the
     * specified stream with the indicated options applied.
     * 
     * @param in
     *            The input stream containing formatted bytes that represent the
     *            model graph to be read.
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
    public <M extends Model> M readModel(InputStream in, Dimensions rootModelDimensions) throws ModelReaderException {

        /*
         * The id of the Format to apply to the input stream's syntax (or
         * null for default format).
         */
        @SuppressWarnings("unused")
        // TODO: Use this to implement format (aka on-wire serialization) flexibility with code on demand.
        final URI formatId = rootModelDimensions.getRequestedFormatId();

        /*
         * The schema of the model graph's root (or null if unknown).
         * NOTE: It would be nice if Java made it possible to "discover"
         * the schema id associated with "M" (the return type).
         */

        final Context context = getContext();

        final ModelGraph modelGraph = new ModelGraph(context, rootModelDimensions);

        // TODO: Refactor this to consider the formatId param and not be tied to JSON

        final JsonFactory jsonFactory = new JsonFactory();
        JsonParser jsonParser;
        try {
            jsonParser = jsonFactory.createJsonParser(in);
        }
        catch (final JsonParseException e) {
            throw new ModelReaderException(
                    "An serious JSON related problem has occurred while attempting to read a Model.", e, this);
        }
        catch (final IOException e) {
            throw new ModelReaderException(
                    "An serious I/O related problem has occurred while attempting to read a Model.", e, this);
        }

        Model model = null;
        JsonToken token;

        try {
            while ((token = jsonParser.nextToken()) != null) {

                switch (token) {

                case START_OBJECT:
                    modelGraph.startModel();
                    break;

                case END_OBJECT:
                    model = modelGraph.endModel();
                    break;

                case FIELD_NAME:
                    final String fieldName = jsonParser.getCurrentName();
                    modelGraph.startField(fieldName);
                    break;

                case START_ARRAY:
                    modelGraph.startList();
                    break;

                case END_ARRAY:
                    modelGraph.endList();
                    break;

                case VALUE_STRING:
                    final String stringValue = jsonParser.getText();
                    modelGraph.endValue(stringValue);
                    break;

                case VALUE_NULL:
                    modelGraph.endValue(null);
                    break;

                case VALUE_TRUE:
                    modelGraph.endValue(Boolean.TRUE);
                    break;

                case VALUE_FALSE:
                    modelGraph.endValue(Boolean.FALSE);
                    break;

                case VALUE_NUMBER_INT:
                    final Integer integerValue = jsonParser.getIntValue();
                    modelGraph.endValue(integerValue);
                    break;

                case VALUE_NUMBER_FLOAT:
                    final Float floatValue = jsonParser.getFloatValue();
                    modelGraph.endValue(floatValue);
                    break;

                case VALUE_EMBEDDED_OBJECT:
                case NOT_AVAILABLE:
                default:
                    break;

                }
            }
        }
        catch (final JsonParseException e) {
            throw new ModelReaderException(
                    "An serious JSON related problem has occurred while attempting to read a Model.", e, this);
        }
        catch (final IOException e) {
            throw new ModelReaderException(
                    "An serious I/O related problem has occurred while attempting to read a Model.", e, this);
        }

        if (_EventManager.isEventHearable()) {
            final ModelReaderEvent event = new ModelReaderEvent(this, model);
            _EventManager.fireEvent(ModelReaderEventListener.EventType.modelRead, event);
        }

        return (M) model;
    }

    @Override
    public boolean removeEventListener(ModelReaderEventListener eventListener) {
        return _EventManager.removeEventListener(eventListener);
    }

}

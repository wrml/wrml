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
import java.util.HashMap;
import java.util.Map;

import org.wrml.runtime.format.ModelParser;
import org.wrml.runtime.format.ModelParserException;
import org.wrml.runtime.format.ModelToken;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonModelParser implements ModelParser
{

    private final static Map<JsonToken, ModelToken> TOKEN_MAP = new HashMap<JsonToken, ModelToken>();

    static
    {
        JsonModelParser.TOKEN_MAP.put(JsonToken.START_OBJECT, ModelToken.MODEL_START);
        JsonModelParser.TOKEN_MAP.put(JsonToken.END_OBJECT, ModelToken.MODEL_END);
        JsonModelParser.TOKEN_MAP.put(JsonToken.FIELD_NAME, ModelToken.SLOT_NAME);
        JsonModelParser.TOKEN_MAP.put(JsonToken.START_ARRAY, ModelToken.LIST_START);
        JsonModelParser.TOKEN_MAP.put(JsonToken.END_ARRAY, ModelToken.LIST_END);
        JsonModelParser.TOKEN_MAP.put(JsonToken.VALUE_STRING, ModelToken.VALUE_TEXT);
        JsonModelParser.TOKEN_MAP.put(JsonToken.VALUE_NULL, ModelToken.VALUE_NULL);
        JsonModelParser.TOKEN_MAP.put(JsonToken.VALUE_TRUE, ModelToken.VALUE_TRUE);
        JsonModelParser.TOKEN_MAP.put(JsonToken.VALUE_FALSE, ModelToken.VALUE_FALSE);
        JsonModelParser.TOKEN_MAP.put(JsonToken.VALUE_NUMBER_INT, ModelToken.VALUE_INTEGER);
        JsonModelParser.TOKEN_MAP.put(JsonToken.VALUE_NUMBER_FLOAT, ModelToken.VALUE_DOUBLE);
    }

    private final JsonParser _JsonParser;

    public JsonModelParser(final JsonParser jsonParser)
    {
        _JsonParser = jsonParser;
    }

    public JsonParser getJsonParser()
    {
        return _JsonParser;
    }

    @Override
    public Double parseDoubleValue() throws IOException, ModelParserException
    {
        return getJsonParser().getDoubleValue();
    }

    @Override
    public Integer parseIntegerValue() throws IOException, ModelParserException
    {
        return getJsonParser().getIntValue();
    }

    @Override
    public ModelToken parseNextToken() throws IOException, ModelParserException
    {
        final JsonToken jsonToken = getJsonParser().nextToken();
        return JsonModelParser.TOKEN_MAP.get(jsonToken);
    }

    @Override
    public String parseSlotName() throws IOException, ModelParserException
    {
        return getJsonParser().getCurrentName();
    }

    @Override
    public String parseTextValue() throws IOException, ModelParserException
    {
        return getJsonParser().getText();
    }

}

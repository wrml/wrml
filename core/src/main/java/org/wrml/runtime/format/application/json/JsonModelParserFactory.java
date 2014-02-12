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
package org.wrml.runtime.format.application.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import org.wrml.runtime.format.ModelParser;
import org.wrml.runtime.format.ModelParserException;
import org.wrml.runtime.format.ModelParserFactory;

import java.io.IOException;
import java.io.InputStream;

public class JsonModelParserFactory implements ModelParserFactory {

    @Override
    public ModelParser createModelParser(final InputStream in) throws IOException, ModelParserException {

        final JsonFactory jsonFactory = new JsonFactory();
        final JsonParser jsonParser;

        try {
            jsonParser = jsonFactory.createParser(in);
        }
        catch (final JsonParseException e) {
            throw new ModelParserException(
                    "An serious JSON related problem has occurred while attempting to parse a Model.", e, null);
        }
        catch (final IOException e) {
            throw new ModelParserException(
                    "An serious I/O related problem has occurred while attempting to parse a Model.", e, null);
        }

        final JsonModelParser parser = new JsonModelParser(jsonParser);
        return parser;
    }

}

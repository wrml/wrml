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

import java.io.IOException;
import java.util.List;

import org.wrml.model.Model;

import org.wrml.runtime.format.ModelPrinter;
import org.wrml.runtime.format.ModelPrinterException;
import org.wrml.runtime.format.ModelWriteOptions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class JsonModelPrinter implements ModelPrinter
{

    private final JsonGenerator _JsonGenerator;

    private final ModelWriteOptions _WriteOptions;

    public JsonModelPrinter(final JsonGenerator jsonGenerator, final ModelWriteOptions writeOptions)
    {
        _JsonGenerator = jsonGenerator;
        _WriteOptions = writeOptions;

        if (_WriteOptions.isPrettyPrint())
        {
            final DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
            prettyPrinter.indentObjectsWith(new DefaultPrettyPrinter.Lf2SpacesIndenter());
            prettyPrinter.indentArraysWith(new DefaultPrettyPrinter.Lf2SpacesIndenter());
            _JsonGenerator.setPrettyPrinter(prettyPrinter);

            // _JsonGenerator.useDefaultPrettyPrinter();
        }
    }

    @Override
    public void close() throws IOException, ModelPrinterException
    {
        _JsonGenerator.close();
    }

    public JsonGenerator getJsonGenerator()
    {
        return _JsonGenerator;
    }

    @Override
    public ModelWriteOptions getWriteOptions()
    {
        return _WriteOptions;
    }

    @Override
    public void printBooleanValue(final boolean booleanValue) throws IOException, ModelPrinterException
    {
        _JsonGenerator.writeBoolean(booleanValue);
    }

    @Override
    public void printDoubleValue(final double value) throws IOException, ModelPrinterException
    {
        _JsonGenerator.writeNumber(value);
    }

    @Override
    public void printIntegerValue(final int value) throws IOException, ModelPrinterException
    {
        _JsonGenerator.writeNumber(value);
    }

    @Override
    public void printListEnd(final List<?> list) throws IOException, ModelPrinterException
    {
        _JsonGenerator.writeEndArray();
    }

    @Override
    public void printListStart(final List<?> list) throws IOException, ModelPrinterException
    {
        _JsonGenerator.writeStartArray();
    }

    @Override
    public void printLongValue(final long value) throws IOException, ModelPrinterException
    {
        _JsonGenerator.writeNumber(value);
    }

    @Override
    public void printModelEnd(final Model model) throws IOException, ModelPrinterException
    {
        _JsonGenerator.writeEndObject();
    }

    @Override
    public void printModelStart(final Model model) throws IOException, ModelPrinterException
    {
        _JsonGenerator.writeStartObject();
    }

    @Override
    public void printNullValue() throws IOException, ModelPrinterException
    {
        _JsonGenerator.writeNull();
    }

    @Override
    public void printSlotName(final String slotName) throws IOException, ModelPrinterException
    {
        _JsonGenerator.writeFieldName(slotName);
    }

    @Override
    public void printTextValue(final String textValue) throws IOException, ModelPrinterException
    {
        _JsonGenerator.writeString(textValue);
    }

}

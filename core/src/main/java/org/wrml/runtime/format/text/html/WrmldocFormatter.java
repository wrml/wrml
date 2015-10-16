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
package org.wrml.runtime.format.text.html;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.wrml.model.Model;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.*;
import org.wrml.runtime.format.application.vnd.wrml.complete.api.CompleteApiBuilder;
import org.wrml.runtime.format.application.vnd.wrml.complete.schema.CompleteSchemaBuilder;
import org.wrml.runtime.format.application.vnd.wrml.wrmldoc.WrmldocDataBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * The wrmldoc formatter is spiritually akin to the <code>javadoc</code> tool in that it generates HTML from models and metadata.
 *
 * @see SystemFormat#html
 * @see <a href="http://www.wrml.org/wrmldoc/archive/common">First Introduced</a>
 * @see <a href="http://www.wrml.org/java/api/wrml-core-1.0">Javadoc Doclet Prototype</a>
 */
public class WrmldocFormatter extends AbstractFormatter {

    public static final String DOCROOT_SETTING_NAME = "docroot";

    public static final String MINIFY_SETTING_NAME = "minify";

    public static final String SHELL_PAGE_TEMPLATE_RESOURCE = "index.html";

    private Map<String, MessageFormat> _Templates;

    private WrmldocDataBuilder _WrmldocDataBuilder;

    private boolean _IsSourceCodeMinified;

    @SuppressWarnings("unchecked")
    @Override
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException, UnsupportedOperationException {

        throw new UnsupportedOperationException("The \"readModel\" operation is not supported by the \"" + getFormatUri() + "\" format.");
    }

    @Override
    public void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException, UnsupportedOperationException {

        final String dotMin = (_IsSourceCodeMinified) ? ".min" : "";
        final ObjectMapper objectMapper = new ObjectMapper();

        try {

            final ObjectNode wrmldocData = _WrmldocDataBuilder.buildWrmldocData(objectMapper, model);

            final String documentTitle = wrmldocData.get(WrmldocDataBuilder.PropertyName.documentTitle.name()).asText();
            final String documentIcon = wrmldocData.get(WrmldocDataBuilder.PropertyName.documentIcon.name()).asText();
            final String schemaUriString = wrmldocData.get(WrmldocDataBuilder.PropertyName.schemaUri.name()).asText();

            final JsonNode modelNode = wrmldocData.get(WrmldocDataBuilder.PropertyName.model.name());
            final String modelValue = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(modelNode);

            final JsonNode schemaNode = wrmldocData.get(WrmldocDataBuilder.PropertyName.schema.name());
            final String schemaValue = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaNode);

            final JsonNode apiNode = wrmldocData.get(WrmldocDataBuilder.PropertyName.api.name());
            final String apiValue = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(apiNode);

            final JsonNode relationNode = wrmldocData.get(WrmldocDataBuilder.PropertyName.relation.name());
            final String relationValue = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(relationNode);

            final MessageFormat pageTemplate = getTemplate(SHELL_PAGE_TEMPLATE_RESOURCE);
            final String renderedPage = renderPage(pageTemplate, _WrmldocDataBuilder.getDocroot(), dotMin, documentTitle, documentIcon, schemaUriString, modelValue, schemaValue, apiValue, relationValue);

            IOUtils.write(renderedPage, out);

        }
        catch (IOException e) {
            throw new ModelWritingException(e.getMessage(), e, this);
        }

    }

    @Override
    protected void initFromConfiguration(final FormatterConfiguration config) {

        final Map<String, String> settings = config.getSettings();
        if (settings == null) {
            throw new NullPointerException("The settings cannot be null.");
        }

        _Templates = new HashMap<>();

        try {
            getTemplate(SHELL_PAGE_TEMPLATE_RESOURCE);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("The shell page template could not be read from: " + SHELL_PAGE_TEMPLATE_RESOURCE);
        }

        String docroot = null;
        if (settings.containsKey(DOCROOT_SETTING_NAME)) {
            docroot = settings.get(DOCROOT_SETTING_NAME);
        }

        final CompleteSchemaBuilder completeSchemaBuilder = new CompleteSchemaBuilder();
        final CompleteApiBuilder completeApiBuilder = new CompleteApiBuilder(completeSchemaBuilder);
        _WrmldocDataBuilder = new WrmldocDataBuilder(completeApiBuilder, docroot);

        _IsSourceCodeMinified = false;
        if (settings.containsKey(MINIFY_SETTING_NAME)) {
            _IsSourceCodeMinified = Boolean.valueOf(settings.get(MINIFY_SETTING_NAME));
        }
    }


    protected MessageFormat getTemplate(String templateName) throws IOException {

        if (!_Templates.containsKey(templateName)) {
            final InputStream templateStream = getClass().getResourceAsStream(templateName);
            final String templateSource = IOUtils.toString(templateStream);

            _Templates.put(templateName, new MessageFormat(templateSource));
        }

        return _Templates.get(templateName);

    }


    protected String renderPage(final MessageFormat template, Object... params) throws IOException {

        final String renderedPage = template.format(params);
        return renderedPage;
    }



}

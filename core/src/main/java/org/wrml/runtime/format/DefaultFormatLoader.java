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

import org.wrml.model.format.Format;
import org.wrml.runtime.Context;
import org.wrml.runtime.DefaultConfiguration;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.application.json.JsonModelParserFactory;
import org.wrml.runtime.format.application.json.JsonModelPrinterFactory;
import org.wrml.runtime.format.application.schema.json.JsonSchemaFormatter;
import org.wrml.runtime.format.application.vnd.wrml.ascii.api.ApiAsciiFormatter;
import org.wrml.runtime.format.application.vnd.wrml.design.schema.SchemaDesignFormatter;
import org.wrml.runtime.format.application.xml.XmlFormatter;
import org.wrml.runtime.format.text.html.WrmldocFormatter;
import org.wrml.runtime.format.text.java.JavaFormatter;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.MediaType;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.util.UniqueName;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFormatLoader implements FormatLoader {

    private final ConcurrentHashMap<URI, Format> _Formats;

    private final ConcurrentHashMap<URI, Formatter> _Formatters;

    private final ConcurrentHashMap<MediaType, Format> _MediaTypeMapping;

    private Context _Context;

    private URI _DefaultFormatUri;

    public DefaultFormatLoader() {

        _Formats = new ConcurrentHashMap<>();
        _Formatters = new ConcurrentHashMap<>();
        _MediaTypeMapping = new ConcurrentHashMap<>();
        _DefaultFormatUri = SystemFormat.json.getFormatUri();
    }

    @Override
    public FormatLoaderConfiguration getConfig() {

        return getContext().getConfig().getFormatLoader();
    }

    @Override
    public Context getContext() {

        return _Context;
    }

    @Override
    public URI getDefaultFormatUri() {

        return _DefaultFormatUri;
    }

    @Override
    public void setDefaultFormatUri(final URI formatUri) {

        if (formatUri != null) {
            _DefaultFormatUri = formatUri;
        }
    }

    @Override
    public Format getDefaultFormat() {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final ApiLoader apiLoader = context.getApiLoader();
        final URI defaultFormatUri = getDefaultFormatUri();
        final Keys formatKeys = apiLoader.buildDocumentKeys(defaultFormatUri, schemaLoader.getFormatSchemaUri());
        return getLoadedFormat(formatKeys);
    }

    @Override
    public Formatter getFormatter(final URI formatUri) {

        if (!_Formatters.containsKey(formatUri)) {
            return null;
        }
        return _Formatters.get(formatUri);
    }

    @Override
    public Format getLoadedFormat(final Keys keys) {

        final SchemaLoader schemaLoader = getContext().getSchemaLoader();
        final URI uri = keys.getValue(schemaLoader.getDocumentSchemaUri());
        if (uri == null) {
            return null;
        }

        if (_Formats.containsKey(uri)) {
            return _Formats.get(uri);
        }
        return null;

    }

    @Override
    public Format getLoadedFormat(final MediaType mediaType) {

        if (mediaType == null) {
            return null;
        }

        if (_MediaTypeMapping.containsKey(mediaType)) {
            return _MediaTypeMapping.get(mediaType);
        }

        return null;
    }

    @Override
    public Set<Format> getLoadedFormats() {

        return new LinkedHashSet<>(_Formats.values());
    }

    @Override
    public SortedSet<URI> getLoadedFormatUris() {

        return new TreeSet<>(_Formats.keySet());
    }

    @Override
    public void init(final Context context) {

        if (context == null) {
            throw new FormatLoaderException("The WRML context cannot be null.", null, this);
        }

        _Context = context;

        loadSystemFormats();
    }

    @Override
    public void loadInitialState() {


        loadConfiguredFormats();

        final FormatLoaderConfiguration config = getConfig();
        if (config == null) {
            return;
        }

        final URI defaultFormatUri = config.getDefaultFormatUri();
        if (defaultFormatUri != null) {
            setDefaultFormatUri(defaultFormatUri);
        }

    }

    @Override
    public void loadConfiguredFormat(final FormatterConfiguration formatterConfiguration) throws FormatLoaderException {

        final Context context = getContext();
        final URI formatUri = formatterConfiguration.getFormatUri();
        if (formatUri == null) {
            throw new FormatLoaderException("The Format URI cannot be null.", null, this);
        }

        final String formatterClassName = formatterConfiguration.getFormatter();
        if (formatterClassName != null) {

            loadFormat(formatUri);

            final Formatter formatter = DefaultConfiguration.newInstance(formatterClassName);
            formatter.init(context, formatterConfiguration);
            loadFormatter(formatter);
        }
        else {
            throw new FormatLoaderException("The Formatter name cannot be null.", null, this);
        }

    }

    @Override
    public void loadFormat(final Format format) throws FormatLoaderException {

        final URI formatUri = format.getUri();
        if (formatUri == null) {
            throw new FormatLoaderException("The Format's URI cannot be null.", null, this);
        }

        _Formats.put(formatUri, format);

        final MediaType mediaType = format.getMediaType();
        if (mediaType == null) {
            throw new FormatLoaderException("The Format's media type cannot be null.", null, this);
        }

        _MediaTypeMapping.put(mediaType, format);

    }

    @Override
    public Format loadFormat(final URI formatUri) throws FormatLoaderException {

        if (formatUri == null) {
            return null;
        }

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final ApiLoader apiLoader = context.getApiLoader();
        final Keys keys = apiLoader.buildDocumentKeys(formatUri, schemaLoader.getFormatSchemaUri());
        final Dimensions dimensions = schemaLoader.getFormatDimensions();

        final Format format = context.getModel(keys, dimensions);
        if (format == null) {
            throw new FormatLoaderException("The Format associated with Keys:\n" + keys + "\n... and Dimensions:\n"
                    + dimensions + " could not be loaded.", null, this);
        }

        loadFormat(format);

        return format;
    }

    @Override
    public void loadFormatter(final Formatter formatter) {

        final URI formatUri = formatter.getFormatUri();
        if (formatUri == null) {
            throw new FormatLoaderException("The Formatter's URI cannot be null.", null, this);
        }

        _Formatters.put(formatUri, formatter);
    }

    protected Formatter createSystemFormatter(final SystemFormat systemFormat) throws FormatLoaderException {

        final Context context = getContext();
        final URI formatUri = systemFormat.getFormatUri();

        final FormatterConfiguration formatterConfiguration = new FormatterConfiguration();
        formatterConfiguration.setFormatUri(formatUri);

        final Map<String, String> settings = new HashMap<>();
        formatterConfiguration.setSettings(settings);

        final Formatter formatter;
        switch (systemFormat) {

            case json: {
                settings.put(PluggableFormatter.PARSER_FACTORY_SETTING_NAME, JsonModelParserFactory.class.getName());
                settings.put(PluggableFormatter.PRINTER_FACTORY_SETTING_NAME, JsonModelPrinterFactory.class.getName());
                formatter = new PluggableFormatter();
                break;
            }
            case xml: {
                formatter = new XmlFormatter();
                break;
            }
            case html: {
                formatter = new WrmldocFormatter();
                break;
            }
            case json_schema: {
                formatter = new JsonSchemaFormatter();
                break;
            }
            case java: {
                formatter = new JavaFormatter();
                break;
            }
            case vnd_wrml_design_schema: {
                formatter = new SchemaDesignFormatter();
                break;
            }
            case vnd_wrml_ascii_api: {
                formatter = new ApiAsciiFormatter();
                break;
            }
            default: {
                formatter = null;
                break;
            }
        }

        if (formatter != null) {
            formatterConfiguration.setFormatter(formatter.getClass().getName());
            formatter.init(context, formatterConfiguration);
        }

        return formatter;

    }

    protected void loadConfiguredFormats() throws FormatLoaderException {

        final FormatLoaderConfiguration config = getConfig();
        if (config == null) {
            return;
        }

        final FormatterConfiguration[] formatterConfigurations = config.getFormatters();
        if ((formatterConfigurations == null) || (formatterConfigurations.length == 0)) {
            return;
        }

        for (final FormatterConfiguration formatterConfig : formatterConfigurations) {
            loadConfiguredFormat(formatterConfig);
        }
    }

    private void loadSystemFormats() throws FormatLoaderException {

        final Context context = getContext();

        for (final SystemFormat systemFormat : SystemFormat.values()) {

            final URI formatUri = systemFormat.getFormatUri();

            final UniqueName formatUniqueName = systemFormat.getUniqueName();
            final String mediaTypeString = formatUniqueName.toString();

            final Format format = context.newModel(Format.class);

            format.setUniqueName(formatUniqueName);
            format.setDescription(systemFormat.getDescription());
            format.setTitle(mediaTypeString);
            format.setHomePageUri(systemFormat.getHomePageUri());
            format.setRfcPageUri(systemFormat.getRfcPageUri());
            format.setMediaType(systemFormat.getMediaType());
            format.setFileExtension(systemFormat.getFileExtension());

            format.setUri(formatUri);

            loadFormat(format);

            final Formatter formatter = createSystemFormatter(systemFormat);
            loadFormatter(formatter);

        }

    }

}

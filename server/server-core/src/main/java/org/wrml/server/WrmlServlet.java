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
package org.wrml.server;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.format.Format;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.CommonHeader;
import org.wrml.model.rest.Method;
import org.wrml.model.rest.status.*;
import org.wrml.runtime.*;
import org.wrml.runtime.format.FormatLoader;
import org.wrml.runtime.format.ModelReadingException;
import org.wrml.runtime.format.ModelWriterException;
import org.wrml.runtime.format.SystemFormat;
import org.wrml.runtime.rest.*;
import org.wrml.runtime.rest.MediaType.MediaTypeException;
import org.wrml.runtime.schema.PropertyProtoSlot;
import org.wrml.runtime.schema.Prototype;
import org.wrml.util.PropertyUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * The WRML engine's HTTP server adapter.
 */
public class WrmlServlet extends HttpServlet {

    /**
     * TODO: Javadoc this.
     */
    public static final String HOST_HEADER_NAME = HttpHeaders.HOST;

    /**
     * TODO: Javadoc this.
     */
    public static final String WRML_HOST_HEADER_NAME = "WRML-HOST";

    /**
     * TODO: Javadoc this.
     * <p/>
     */
    public static final String WRML_SCHEME_HEADER_NAME = "WRML-Scheme";

    public static final String ACCEPT_PARAMETER_NAME = "accept";

    public static final String HOST_PARAMETER_NAME = "host";

    public static final String WRML_CONFIGURATION_FILE_PATH_INIT_PARAM_NAME = "wrml-config-file-path";

    public static final String WRML_CONFIGURATION_RESOURCE_PATH_INIT_PARAM_NAME = "wrml-config-resource-path";

    /**
     * TODO: Manage these routes in a separate class?
     */
    public static final String WRML_METADATA_ROOT_PATH = "/_wrml";

    public static final String WRML_METADATA_PING_PATH = WRML_METADATA_ROOT_PATH + "/ping";

    public static final String WRML_METADATA_API_PATH = WRML_METADATA_ROOT_PATH + "/api";

    public static final String WRML_METADATA_API_SWAGGER_PATH = WRML_METADATA_API_PATH + "/swagger";

    public static final String WRML_METADATA_API_LOAD_PATH = WRML_METADATA_API_PATH + "/load";


    public static final MediaType APPLICATION_JSON_MEDIA_TYPE = new MediaType("application", "json");

    private static final Logger LOGGER = LoggerFactory.getLogger(WrmlServlet.class);

    private static final long serialVersionUID = 1L;


    private Engine _Engine;

    private StatusReport _PingStatusReport;

    /**
     * Creates a new instance of the {@link WrmlServlet}.
     *
     * @see #init(javax.servlet.ServletConfig)
     */
    public WrmlServlet() {

    }

    @Override
    public void init(final ServletConfig servletConfig) throws ServletException {

        LOGGER.debug("Servlet Name {}", servletConfig.getServletName());

        if (LOGGER.isDebugEnabled()) {

            LOGGER.debug("Parameters names passed [");
            List<String> paramList = new ArrayList<>();
            Enumeration<String> params = servletConfig.getInitParameterNames();
            while (params.hasMoreElements()) {
                String paramName = params.nextElement();
                paramList.add(String.format("%s=%s", paramName, servletConfig.getInitParameter(paramName)));
            }
            LOGGER.debug("Parameters names passed={}", Arrays.toString(paramList.toArray()));
        }

        super.init(servletConfig);

        final String configFileLocation = PropertyUtil.getSystemProperty(
                EngineConfiguration.WRML_CONFIGURATION_FILE_PATH_PROPERTY_NAME,
                servletConfig.getInitParameter(WRML_CONFIGURATION_FILE_PATH_INIT_PARAM_NAME));


        String configResourceLocation = null;
        if (configFileLocation == null) {
            configResourceLocation = servletConfig.getInitParameter(WRML_CONFIGURATION_RESOURCE_PATH_INIT_PARAM_NAME);
        }

        try {

            final EngineConfiguration engineConfig;

            if (configFileLocation != null) {
                LOGGER.info("Determined configuration file location: {}", configFileLocation);
                engineConfig = EngineConfiguration.load(configFileLocation);
            }
            else if (configResourceLocation != null) {
                LOGGER.info("Determined configuration resource location: {}", configResourceLocation);
                engineConfig = EngineConfiguration.load(getClass(), configResourceLocation);
            }
            else {
                throw new ServletException("The WRML engine configuration is null. Unable to initialize servlet.");
            }

            final Engine engine = new DefaultEngine();
            engine.init(engineConfig);
            setEngine(engine);
            LOGGER.debug("Initialized WRML with: {}", engineConfig);

            final Context context = getContext();
            _PingStatusReport = context.newModel(StatusReport.class);
            _PingStatusReport.setTitle("Ping Success");
            _PingStatusReport.setStatus(Status.OK);

        }
        catch (IOException ex) {
            throw new ServletException("Unable to initialize servlet.", ex);
        }

        LOGGER.info("WRML SERVLET INITIALIZED --------------------------------------------------");
    }

    /**
     * The WRML {@link Engine} that is wrapped by this {@link WrmlServlet}.
     *
     * @return The WRML {@link Engine} that is wrapped by this {@link WrmlServlet}.
     */
    public Engine getEngine() {

        return _Engine;
    }

    /**
     * Sets the {@link WrmlServlet}'s {@link Engine} <i>programatically</i>.
     *
     * @param engine The WRML {@link Engine} to be used by this {@link WrmlServlet}.
     */
    public void setEngine(final Engine engine) {

        _Engine = engine;
    }

    /**
     * The current {@link Context} associated with the {@link Engine} that is wrapped by this {@link WrmlServlet}.
     *
     * @return The current {@link Context} associated with the {@link Engine} that is wrapped by this {@link WrmlServlet}.
     */
    public Context getContext() {

        final Context context = getEngine().getContext();
        return context;

    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final Context context = getContext();
        final ApiLoader apiLoader = context.getApiLoader();

        // Determine the HTTP interaction method.
        final Method method = Method.fromProtocolGivenName(request.getMethod().toUpperCase());

        final List<MediaType> acceptableMediaTypes;


        LOGGER.debug("Parameter Values: {}", request.getParameterMap());
        final String acceptParameterStringValue = request.getParameter(ACCEPT_PARAMETER_NAME);
        LOGGER.debug(ACCEPT_PARAMETER_NAME + " Parameter Value: {}", acceptParameterStringValue);
        if (!StringUtils.isEmpty(acceptParameterStringValue)) {
            acceptableMediaTypes = new AcceptableMediaTypeList(acceptParameterStringValue);
        }
        else {
            final String acceptHeaderStringValue = request.getHeader(HttpHeaders.ACCEPT);
            acceptableMediaTypes = new AcceptableMediaTypeList(acceptHeaderStringValue);
        }

        LOGGER.debug("Acceptable Media Types: {}", acceptableMediaTypes);

        try {
            // Determine the identity of the request's resource "endpoint".
            final URI requestUri = getRequestUri(request);

            final ApiNavigator apiNavigator = apiLoader.getParentApiNavigator(requestUri);
            final Api api = (apiNavigator != null) ? apiNavigator.getApi() : null;
            final String path = requestUri.getPath();

            // TODO: This is ugly here. Move this handling to an "admin" construct.
            if (method == Method.Get && path.startsWith(WRML_METADATA_ROOT_PATH)) {

                final Model responseModel;
                MediaType responseEntityMediaType = null;
                URI responseEntityFormatUri = null;

                switch (path) {

                    case WRML_METADATA_PING_PATH:
                        responseModel = _PingStatusReport;
                        responseEntityMediaType = APPLICATION_JSON_MEDIA_TYPE;
                        break;

                    case WRML_METADATA_API_PATH:
                        responseModel = api;
                        responseEntityMediaType = getMostAcceptableMediaType(responseModel.getSchemaUri(), acceptableMediaTypes);
                        break;

                    case WRML_METADATA_API_SWAGGER_PATH:
                        responseModel = api;
                        responseEntityMediaType = APPLICATION_JSON_MEDIA_TYPE;
                        responseEntityFormatUri = SystemFormat.vnd_wrml_swagger_api.getFormatUri();
                        break;

                    case WRML_METADATA_API_LOAD_PATH:
                        responseModel = loadApi(requestUri);
                        responseEntityMediaType = getMostAcceptableMediaType(responseModel.getSchemaUri(), acceptableMediaTypes);
                        break;

                    default:
                        responseModel = null;
                        break;

                }

                if (responseModel != null) {

                    try {
                        writeModelAsResponseEntity(method, response, responseModel, responseEntityMediaType, responseEntityFormatUri);
                    } catch (final ModelWriterException | MediaTypeException e) {
                        throw new ServletException("Failed to write model to HTTP response output stream (URI = " + requestUri + ", Model = [" + api + "]).", e);
                    }

                    return;
                }

            }

            if (apiNavigator == null) {
                final ApiNotFoundErrorReport notFoundErrorReport = createNotFoundErrorReport(ApiNotFoundErrorReport.class, requestUri, null);
                throw new WrmlServletException(notFoundErrorReport);
            }

            LOGGER.debug("Request is associated with REST API: {}.", api.getTitle());

            final Resource endpointResource = apiNavigator.getResource(requestUri);
            if (endpointResource == null) {
                final ResourceNotFoundErrorReport notFoundErrorReport = createNotFoundErrorReport(ResourceNotFoundErrorReport.class, requestUri, api);
                throw new WrmlServletException(notFoundErrorReport);
            }

            if (!endpointResource.getReferenceMethods().contains(method)) {

                final String methodProtocolGivenName = method.getProtocolGivenName();
                MethodNotAllowedErrorReport methodNotAllowedErrorReport = context.newModel(MethodNotAllowedErrorReport.class);
                methodNotAllowedErrorReport.setTitle("METHOD NOT ALLOWED");
                methodNotAllowedErrorReport.setStatus(Status.METHOD_NOT_ALLOWED);
                methodNotAllowedErrorReport.setRequestUri(requestUri);
                methodNotAllowedErrorReport.setMethodName(methodProtocolGivenName);
                methodNotAllowedErrorReport.setDescription("This resource does not allow references using " + methodProtocolGivenName);

                throw new WrmlServletException(methodNotAllowedErrorReport);
            }

            // Build the Model query objects; the Keys (URI and other identities) and Dimensions ("header" metadata).
            final Dimensions dimensions = buildDimensions(request, method, requestUri, api, acceptableMediaTypes);
            final Keys keys = apiLoader.buildDocumentKeys(requestUri, dimensions.getSchemaUri());

            LOGGER.debug("Request Keys: {}.", keys);
            LOGGER.debug("Request Dimensions: {}.", dimensions);

            // Read the request entity (with PUT or POST) as a model that will be passed as a parameter.
            final Model parameterModel = readModelFromRequestEntity(request, method, requestUri);

            if (parameterModel != null) {
                LOGGER.debug("Request method [" + method.getProtocolGivenName() + "] passed parameter Model\n: " + parameterModel);
            }

            // Delegate to the WRML runtime to service the request from here.
            final Model responseModel = context.request(method, keys, dimensions, parameterModel);

            if (responseModel == null) {
                LOGGER.debug("Request method [" + method.getProtocolGivenName() + "] URI: " + requestUri + " returned a null Model.");


                switch (method) {
                    case Get: {
                        final DocumentNotFoundErrorReport notFoundErrorReport = createNotFoundErrorReport(DocumentNotFoundErrorReport.class, requestUri, api);
                        final URI defaultSchemaUri = dimensions.getSchemaUri();
                        final Prototype defaultSchemaPrototype = context.getSchemaLoader().getPrototype(defaultSchemaUri);
                        notFoundErrorReport.setDefaultSchemaUri(defaultSchemaUri);
                        notFoundErrorReport.setDefaultSchemaTitle(defaultSchemaPrototype.getTitle());

                        if (keys.getCount() > 1) {

                            final Set<Parameter> surrogateKeyComponents = endpointResource.getSurrogateKeyComponents(requestUri, defaultSchemaPrototype);
                            if (surrogateKeyComponents != null && !surrogateKeyComponents.isEmpty()) {
                                for (final Parameter surrogateKeyComponent : surrogateKeyComponents) {
                                    final SurrogateKeyValue surrogateKeyValue = context.newModel(SurrogateKeyValue.class);
                                    final String keySlotName = surrogateKeyComponent.getName();
                                    surrogateKeyValue.setName(keySlotName);
                                    surrogateKeyValue.setTextValue(surrogateKeyComponent.getValue());

                                    final PropertyProtoSlot propertyProtoSlot = defaultSchemaPrototype.getProtoSlot(keySlotName);
                                    surrogateKeyValue.setDeclaringSchemaUri(propertyProtoSlot.getDeclaringSchemaUri());
                                    surrogateKeyValue.setValueType(propertyProtoSlot.getValueType());

                                    notFoundErrorReport.getSurrogateKeyValues().add(surrogateKeyValue);
                                }
                            }
                        }

                        throw new WrmlServletException(notFoundErrorReport);
                        //break;
                    }
                    case Save: {
                        throw new ServletException("A 400. The save operation didn't return a response representation for the requested URI: " + requestUri);
                        //break;
                    }

                    default:

                        writeVoid(response);
                        break;
                }

            }
            else {
                LOGGER.debug("Request method [" + method.getProtocolGivenName() + "] returning response Model: \n" + responseModel);

                try {
                    MediaType responseEntityMediaType = getMostAcceptableMediaType(responseModel.getSchemaUri(), acceptableMediaTypes);
                    writeModelAsResponseEntity(method, response, responseModel, responseEntityMediaType, null);
                }
                catch (final ModelWriterException | MediaTypeException e) {
                    throw new ServletException("Failed to write model to HTTP response output stream (URI = " + requestUri + ", Model = [" + responseModel + "]).", e);
                }
            }


        }
        catch (final WrmlServletException wse) {
            try {
                ErrorReport errorReport = wse.getErrorReport();
                MediaType responseEntityMediaType = getMostAcceptableMediaType(errorReport.getSchemaUri(), acceptableMediaTypes);
                writeModelAsResponseEntity(method, response, wse.getErrorReport(), responseEntityMediaType, null);
            }
            catch (Exception e) {
                throw new IOException("Failed to write error report to HTTP response output stream.", e);
            }
        }
        catch (final Exception e) {
            // Bad Request
            LOGGER.error("Returning error.", e);


            // TODO, map a response in the function call?
            writeException(e, response, !method.isEntityAllowedInResponseMessage());
        }
    }


    /**
     *
     *
     * @param notFoundErrorType
     * @param requestUri
     * @param api
     * @param <T>
     * @return
     */
    private <T extends NotFoundErrorReport> T createNotFoundErrorReport(final Class<T> notFoundErrorType, final URI requestUri, final Api api) {

        final Context context = getContext();
        final NotFoundErrorReport notFoundErrorReport = context.newModel(notFoundErrorType);
        notFoundErrorReport.setStatus(Status.NOT_FOUND);
        notFoundErrorReport.setRequestUri(requestUri);

        final String title;
        final String description;

        if (notFoundErrorReport instanceof ApiNotFoundErrorReport) {
            title = "API Not Found";
            description = "No parent WRML REST API was found for requested URI: " + requestUri;
        }
        else if (notFoundErrorReport instanceof ResourceNotFoundErrorReport) {
            title = "Resource Not Found";
            description = "The WRML REST API (" + api.getTitle() + ") does not define a resource that matches the requested URI: " + requestUri;
            ((ResourceNotFoundErrorReport) notFoundErrorReport).setParentApiUri(api.getUri());
        }
        else if (notFoundErrorReport instanceof DocumentNotFoundErrorReport) {
            title = "Document Not Found";
            description = "The WRML REST API (" + api.getTitle() + ") does not have a document that matches the requested URI: " + requestUri;
            ((DocumentNotFoundErrorReport) notFoundErrorReport).setParentApiUri(api.getUri());
        }
        else {
            title = null;
            description = null;
        }

        notFoundErrorReport.setTitle(title);
        notFoundErrorReport.setDescription(description);
        return (T) notFoundErrorReport;
    }

    /**
     * Get the requested resource's id from the the {@link HttpServletRequest}.
     *
     * @param request The {@link HttpServletRequest} that holds the {@link URI}.
     * @return The requested resource's id from the the {@link HttpServletRequest}.
     * @throws URISyntaxException Thrown if there is a syntax problem when constructing the {@link URI}.
     */
    URI getRequestUri(final HttpServletRequest request) throws URISyntaxException {
        // Due to the quirky nature of a servlet container, we're after the entire path.  
        // This seems to work with servlet 3.0 and Tomcat 7.X
        String path = request.getServletPath();
        String extra = request.getPathInfo();
        if (path != null && extra != null) {
            path += request.getPathInfo();
        }
        else if (path == null) {
            path = extra;
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String scheme = StringUtils.defaultIfEmpty(request.getHeader(WRML_SCHEME_HEADER_NAME), request.getScheme());
        String host = StringUtils.defaultIfEmpty(request.getHeader(WRML_HOST_HEADER_NAME), request.getHeader(HOST_HEADER_NAME));

        final String hostParameterValue = request.getParameter(HOST_PARAMETER_NAME);
        if (!StringUtils.isEmpty(hostParameterValue)) {
            host = hostParameterValue;
        }

        String portString = "80";
        int portSeparatorIndex = host.indexOf(':');
        if (portSeparatorIndex > 0 && portSeparatorIndex < host.length() - 1) {
            portString = host.substring(portSeparatorIndex + 1);
            host = host.substring(0, portSeparatorIndex);
        }

        int port = 80;
        try {
            port = Integer.parseInt(portString);
        }
        catch (NumberFormatException e) {
            LOGGER.debug("Cannot parse request URI port string value as int: {}", portString);
        }

        if (port == 80) {
            port = -1;
        }

        final URI requestUri = new URI(scheme, null, host, port, path, null, null);

        LOGGER.debug("Determined request URI: {}", requestUri);
        return requestUri;
    }

    /**
     *
     * @param method
     * @param requestUri
     * @param acceptableMediaTypes
     * @return
     * @throws ServletException
     */
    List<URI> getAcceptableResponseEntitySchemaUris(final Method method, final URI requestUri, final List<MediaType> acceptableMediaTypes) throws ServletException {

        final List<URI> acceptableSchemaUriList = new ArrayList<>();

        for (final MediaType mediaType : acceptableMediaTypes) {
            if (mediaType.getParameter(SystemMediaType.PARAMETER_NAME_SCHEMA) != null && mediaType.getFullType().equals(SystemMediaType.MEDIA_TYPE_STRING_WRML)) {

                final String acceptableSchemaUriString = mediaType.getParameter(SystemMediaType.PARAMETER_NAME_SCHEMA);
                final URI acceptableSchemaUri = URI.create(acceptableSchemaUriString);
                acceptableSchemaUriList.add(acceptableSchemaUri);
            }
        }

        if (acceptableSchemaUriList.isEmpty()) {
            final ApiLoader loader = getContext().getApiLoader();
            final ApiNavigator apiNavigator = loader.getParentApiNavigator(requestUri);
            final Resource endpointResource = apiNavigator.getResource(requestUri);
            final Set<URI> responseSchemaUris = endpointResource.getResponseSchemaUris(method);
            if (responseSchemaUris != null) {
                acceptableSchemaUriList.addAll(responseSchemaUris);
            }
        }

        return acceptableSchemaUriList;
    }

    /**
     * Build the WRML {@link Dimensions} object that, within the WRML runtime, will represent many of the same
     * "metadata" ideas that HTTP has delcared {@link org.wrml.model.rest.CommonHeader}s.
     *
     * @param request              The {@link HttpServletRequest} that holds the metadata that is needed for the {@link Dimensions}.
     * @param method               The requested interaction {@link Method}.
     * @param requestUri           The requested resource's id ({@link URI}).
     * @param api                  The target REST API ({@link Api}).
     * @param acceptableMediaTypes The client-specified acceptable {@link MediaType}s.
     * @return The requested {@link Dimensions} of the desired response entity {@link Model}.
     */
    Dimensions buildDimensions(final HttpServletRequest request, final Method method, final URI requestUri, final Api api, final List<MediaType> acceptableMediaTypes) throws ServletException {

        // Determine the best possible schema URI for the response.
        final List<URI> acceptableSchemaUriList = getAcceptableResponseEntitySchemaUris(method, requestUri, acceptableMediaTypes);

        final URI responseModelSchemaUri;
        if (!acceptableSchemaUriList.isEmpty()) {
            responseModelSchemaUri = acceptableSchemaUriList.get(0);
        }
        else {

            if (!acceptableMediaTypes.isEmpty()) {
                throw new ServletException("A 406. The WRML REST API (" + api.getTitle() + ") doesn't define any acceptable representations of the resource identified by: " + requestUri);
            }

            if (method == Method.Get) {
                throw new ServletException("A 403? The WRML REST API (" + api.getTitle() + ") doesn't define any representation of the resource identified by: " + requestUri);
            }

            // The interaction may not return anything, (e.g. DELETE)
            responseModelSchemaUri = null;
        }


        final DimensionsBuilder dimensionsBuilder = new DimensionsBuilder(responseModelSchemaUri);

        if (responseModelSchemaUri != null && !acceptableMediaTypes.isEmpty()) {

            // It would make sense for this to be the first (and only) media type that a WRML client would pass in the Accept header.
            final MediaType mediaType = acceptableMediaTypes.get(0);
            if (mediaType.getFullType().equals(SystemMediaType.MEDIA_TYPE_STRING_WRML)) {
                // These are communicated to a WRML server as parameters to the WRML media type that is passed in the HTTP Accept header.
                final String includedSlotNamesStringValue = mediaType.getParameter(SystemMediaType.PARAMETER_NAME_INCLUDE);
                final List<String> includedSlotNames = dimensionsBuilder.getIncludedSlotNames();
                includedSlotNames.addAll(parseMediaTypeParameterList(includedSlotNamesStringValue));

                final String excludedSlotNamesStringValue = mediaType.getParameter(SystemMediaType.PARAMETER_NAME_EXCLUDE);
                final List<String> excludedSlotNames = dimensionsBuilder.getExcludedSlotNames();
                excludedSlotNames.addAll(parseMediaTypeParameterList(excludedSlotNamesStringValue));

                final String embeddedLinkSlotNamesStringValue = mediaType.getParameter(SystemMediaType.PARAMETER_NAME_EMBED);
                final List<String> embeddedLinkSlotNames = dimensionsBuilder.getEmbeddedLinkSlotNames();
                embeddedLinkSlotNames.addAll(parseMediaTypeParameterList(embeddedLinkSlotNamesStringValue));
            }

        }

        final Locale locale = request.getLocale();
        dimensionsBuilder.setLocale(locale);

        final SortedMap<String, String> metadata = dimensionsBuilder.getMetadata();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();

            final Enumeration<String> headerValues = request.getHeaders(headerName);
            final StringBuilder headerValueStringBuilder = new StringBuilder();
            while (headerValues.hasMoreElements()) {
                final String partialHeaderValue = headerValues.nextElement();
                headerValueStringBuilder.append(partialHeaderValue);
                if (headerValues.hasMoreElements()) {
                    headerValueStringBuilder.append(", ");
                }
            }

            final String headerValue = headerValueStringBuilder.toString();
            metadata.put(headerName, headerValue);

            final CommonHeader commonHeader = CommonHeader.fromString(headerName);
            if (commonHeader == null) {
                continue;
            }

            switch (commonHeader) {
                case REFERER:

                    final URI referrerUri = URI.create(headerValue);
                    dimensionsBuilder.setReferrerUri(referrerUri);
                    break;

                default:
                    break;
            }
        }


        final SortedMap<String, String> queryParameters = dimensionsBuilder.getQueryParameters();
        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String parameterName = parameterNames.nextElement();
            final String[] parameterValues = request.getParameterValues(parameterName);
            final String parameterValue = StringUtils.join(parameterValues, ", ");
            queryParameters.put(parameterName, parameterValue);
        }


        final Context context = getContext();
        final ApiLoader apiLoader = context.getApiLoader();
        final Dimensions dimensions = apiLoader.buildDocumentDimensions(method, requestUri, dimensionsBuilder);
        return dimensions;
    }

    /**
     *
     * @param listString
     * @return
     */
    List<String> parseMediaTypeParameterList(final String listString) {

        if (StringUtils.isEmpty(listString)) {
            return Collections.EMPTY_LIST;
        }

        final String[] listElementArray = StringUtils.split(listString, ", ");
        return Arrays.asList(listElementArray);
    }


    /**
     *
     * @param request
     * @param requestMethod
     * @param uri
     * @return
     * @throws ServletException
     */
    Model readModelFromRequestEntity(final HttpServletRequest request, final Method requestMethod, final URI uri) throws ServletException {

        if (!requestMethod.isEntityAllowedInRequestMessage()) {
            LOGGER.debug("This type of request does not carry a payload [{}]", new Object[]{requestMethod});
            return null;
        }

        MediaType requestEntityMediaType = null;
        if (request.getContentType() != null) {
            try {
                requestEntityMediaType = new MediaType(request.getContentType());
            }
            catch (final MediaTypeException ex) {
                LOGGER.error("Unable to create request media type.", ex);
            }
        }

        final URI requestEntitySchemaUri = getRequestEntitySchemaUri(requestEntityMediaType, requestMethod, uri);
        final URI requestEntityFormatUri = getRequestFormatUri(requestEntityMediaType);

        if (requestEntitySchemaUri == null) {
            LOGGER.debug("The request schema URI is null, returning null");
            return null;
        }

        Model model = null;
        InputStream in;
        try {
            in = request.getInputStream();
        }
        catch (final Exception e) {
            throw new ServletException("Failed to read HTTP request content.");
        }
        try {
            model = getContext().readModel(in, uri, requestEntitySchemaUri, requestEntityFormatUri);
        }
        catch (final ModelReadingException e) {
            throw new ServletException("Failed to read model graph from HTTP response input stream (URI = " + request.getRequestURI() + ", Schema = [" + requestEntitySchemaUri + "]).",
                    e);
        }
        finally {
            try {
                // TODO: Is this the appropriate way to finish with the InputStream?
                in.close();
            }
            catch (final IOException e) {
                throw new ServletException("Failed to close model graph input stream.", e);
            }
        }

        return model;
    }

    /**
     * Get the non-null {@link URI} of the {@link Format} used to deserialize the request entity.
     *
     * @param requestEntityMediaType The {@link MediaType} that may identify the {@link Format} and/or {@link org.wrml.model.schema.Schema}.
     * @return The non-null {@link URI} of the {@link Format} used to deserialize the request entity.
     */
    URI getRequestFormatUri(final MediaType requestEntityMediaType) {

        URI requestFormatUri = null;

        if (requestEntityMediaType != null) {
            if (requestEntityMediaType.getFullType().equals(SystemMediaType.MEDIA_TYPE_STRING_WRML)) {
                requestFormatUri = URI.create(requestEntityMediaType.getParameter(SystemMediaType.PARAMETER_NAME_FORMAT));
            }
            else {
                requestFormatUri = getLoadedFormatUri(requestEntityMediaType);
            }
        }

        if (requestFormatUri == null) {
            requestFormatUri = getDefaultFormatUri();
        }

        return requestFormatUri;
    }

    /**
     * Get the {@link URI} of the {@link org.wrml.model.schema.Schema} used to describe the request entity.
     *
     * @param requestEntityMediaType The {@link MediaType} that may identify the {@link Format} and/or {@link org.wrml.model.schema.Schema}.
     * @return The {@link URI} of the {@link org.wrml.model.schema.Schema} used to describe the request entity.
     */
    URI getRequestEntitySchemaUri(final MediaType requestEntityMediaType, final Method method, final URI uri) {

        URI requestSchemaUri = null;

        if (requestEntityMediaType != null && requestEntityMediaType.getFullType().equals(SystemMediaType.MEDIA_TYPE_STRING_WRML)) {
            requestSchemaUri = URI.create(requestEntityMediaType.getParameter(SystemMediaType.PARAMETER_NAME_SCHEMA));
        }

        if (requestSchemaUri == null) {
            LOGGER.debug("Falling back to resource default for uri: {}", uri);
            requestSchemaUri = getContext().getApiLoader().getDefaultResponseSchemaUri(method, uri);
        }

        return requestSchemaUri;
    }

    /**
     *
     * @param e
     * @param response
     * @param noBody
     * @throws IOException
     */
    private void writeException(final Exception e, final HttpServletResponse response, final boolean noBody) throws IOException {

        LOGGER.error("An exception was thrown during request processing.", e);

        response.setContentType(ContentType.TEXT_PLAIN.toString());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        if (!noBody) {
            // NullPointerExceptions don't have messages.
            if (null != e.getMessage()) {
                response.setContentLength(e.getMessage().length());

                final OutputStream responseOut = response.getOutputStream();

                if (responseOut != null) {
                    IOUtils.write((e.getMessage()), responseOut);
                    responseOut.flush();
                    responseOut.close();
                }
            }
        }

        response.flushBuffer();
    }

    /**
     *
     * @param requestMethod
     * @param response
     * @param responseModel
     * @param responseEntityMediaType
     * @param responseFormatUri
     * @throws MediaTypeException
     * @throws ServletException
     * @throws IOException
     */
    void writeModelAsResponseEntity(final Method requestMethod, final HttpServletResponse response, final Model responseModel, MediaType responseEntityMediaType, URI responseFormatUri) throws MediaTypeException, ServletException, IOException {

        // Set the content type
        //MediaType responseEntityMediaType = getMostAcceptableMediaType(responseModel.getSchemaUri(), acceptableMediaTypes);
        if (responseEntityMediaType == null) {
            responseEntityMediaType = getDefaultMediaType();
        }
        final String contentTypeHeaderValue = responseEntityMediaType.toContentType();
        response.setContentType(contentTypeHeaderValue);

        LOGGER.debug("Responding with Content-Type: " + contentTypeHeaderValue);

        // Set the locale
        final Dimensions responseDimensions = responseModel.getDimensions();
        final Locale responseLocale = responseDimensions.getLocale();
        if (responseLocale != null) {
            response.setLocale(responseLocale);
        }

        // Set the status
        response.setStatus(HttpServletResponse.SC_OK);

        final boolean noBody = !requestMethod.isEntityAllowedInResponseMessage();
        if (noBody) {
            response.setContentLength(0);
        }
        else {
            final Context context = getContext();

            final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

            // Set the format for output
            URI formatUri = responseFormatUri;
            if (formatUri == null) {
                if (responseEntityMediaType.getFullType().equals(SystemMediaType.MEDIA_TYPE_STRING_WRML)) {
                    final String format = responseEntityMediaType.getParameter(SystemMediaType.PARAMETER_NAME_FORMAT);
                    if (format != null) {
                        formatUri = URI.create(format);
                    }
                } else {
                    formatUri = getLoadedFormatUri(responseEntityMediaType);
                }
            }

            context.writeModel(byteOut, responseModel, formatUri);

            final byte[] modelBytes = byteOut.toByteArray();
            final int contentLength = modelBytes.length;
            response.setContentLength(contentLength);

            final OutputStream responseOut = response.getOutputStream();
            IOUtils.write(modelBytes, responseOut);
            // Make sure it's on the wire
            responseOut.flush();
            // Close our stream
            responseOut.close();
        }

        response.flushBuffer();
        // TODO: response.setBufferSize(?); - Is this needed?
        // TODO: response.setCharacterEncoding(?); - Is this needed?
        // TODO: Set other headers as needed.
    }

    /**
     * Computes the most acceptable {@link MediaType} from the specified {@link List}, with consideration of response model's schema.
     *
     * @param responseSchemaUri    The {@link java.net.URI} of the {@link org.wrml.model.schema.Schema} that describes the form of the response model.
     * @param acceptableMediaTypes The {@link java.util.List} of {@link org.wrml.runtime.rest.MediaType}s that the requestor is willing to accept.
     * @return The most acceptable {@link MediaType} from the specified {@link List}, or <code>null</code> if none of the acceptable types are supported.
     */
    MediaType getMostAcceptableMediaType(final URI responseSchemaUri, final List<MediaType> acceptableMediaTypes) throws MediaTypeException {

        for (final MediaType mediaType : acceptableMediaTypes) {
            // Skip wild card types.
            if (mediaType.getType().equals(MediaType.WILDCARD) || mediaType.getSubType().equals(MediaType.WILDCARD)) {
                continue;
            }

            if (mediaType.getFullType().equals(SystemMediaType.MEDIA_TYPE_STRING_WRML)) {
                // This is an application/wrml media type
                MediaType wrmlMediaType = null;

                final String responseSchemaUriString = responseSchemaUri.toString();

                final String wrmlMediaTypeSchemaUriString = mediaType.getParameter(SystemMediaType.PARAMETER_NAME_SCHEMA);
                if (wrmlMediaTypeSchemaUriString == null) {
                    wrmlMediaType = new MediaType(mediaType.getFullType());
                    wrmlMediaType.setParameter(SystemMediaType.PARAMETER_NAME_SCHEMA, responseSchemaUriString);
                }
                else if (!wrmlMediaTypeSchemaUriString.equals(responseSchemaUriString)) {
                    // TODO: Consider using Prototype.isAssignableFrom to allow for polymorphism here (instead of exact schema ID match).
                    // Should this return null instead to indicate that the WRML-specific response schema is not here?
                    continue;
                }

                final String wrmlMediaTypeFormatUriString = mediaType.getParameter(SystemMediaType.PARAMETER_NAME_FORMAT);
                if (wrmlMediaTypeFormatUriString == null) {

                    if (wrmlMediaType == null) {
                        wrmlMediaType = new MediaType(mediaType.getFullType());

                        // We didn't need to adjust the schema but we do need to set the format param so we a new MediaType with both params (schema & format).
                        wrmlMediaType.setParameter(SystemMediaType.PARAMETER_NAME_SCHEMA, mediaType.getParameter(SystemMediaType.PARAMETER_NAME_SCHEMA));
                    }

                    wrmlMediaType.setParameter(SystemMediaType.PARAMETER_NAME_FORMAT, getDefaultFormatUri().toString());
                }
                else {

                    final URI formatUri = URI.create(wrmlMediaTypeFormatUriString);
                    final Context context = getContext();
                    final FormatLoader formatLoader = context.getFormatLoader();

                    if (formatLoader.getFormatter(formatUri) == null) {
                        // Should this return null instead to indicate that the WRML-specific response format is not here?
                        continue;
                    }
                }

                return (wrmlMediaType != null) ? wrmlMediaType : mediaType;
            }
            else {
                final URI formatUri = getLoadedFormatUri(mediaType);
                if (formatUri != null) {
                    return mediaType;
                }
            }
        }

        // No exact match
        return null;
    }

    /**
     *
     * @param response
     * @throws IOException
     */
    void writeNotFound(final HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentLength(0);
        response.flushBuffer();
    }

    /**
     *
     * @param response
     * @throws IOException
     */
    void writeVoid(final HttpServletResponse response) throws IOException {

        // TODO

        /*
         * Writing no entity body in the response may be perfectly fine...depending on the intent of the request (method, desired schema, etc), which is expressed in the
         * intendedResponseDimensions.
         */

        // TODO
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentLength(0);
        response.flushBuffer();
    }

    /**
     * Get the {@link URI} of an already loaded {@link Format} that is identified by the specified {@link MediaType}.
     *
     * @param mediaType The {@link MediaType} that identifies the {@link Format}.
     * @return The {@link URI} of an already loaded {@link Format} or <code>null</code> if it could not be found.
     */
    URI getLoadedFormatUri(final MediaType mediaType) {

        final Context context = getContext();
        final FormatLoader formatLoader = context.getFormatLoader();
        final Format format = formatLoader.getLoadedFormat(mediaType);
        if (format == null) {
            return null;
        }

        return format.getUri();
    }

    /**
     * The default {@link Format} {@link URI} associated with the runtime.
     *
     * @return The default {@link Format} {@link URI} associated with the runtime.
     */
    URI getDefaultFormatUri() {

        final Context context = getContext();
        final FormatLoader formatLoader = context.getFormatLoader();
        return formatLoader.getDefaultFormatUri();
    }

    /**
     * The default {@link Format} {@link MediaType} associated with the runtime.
     *
     * @return The default {@link Format} {@link MediaType} associated with the runtime.
     */
    MediaType getDefaultMediaType() {

        final Context context = getContext();
        final FormatLoader formatLoader = context.getFormatLoader();
        return formatLoader.getDefaultFormat().getMediaType();
    }

    /**
     * Load the API specified by the request URI
     *
     * @param requestUri identifies the URI of the API to load
     * @return the loaded {@Link Api}.
     */
    private Api loadApi(URI requestUri) throws WrmlServletException {

        final Context context = getContext();

        final URI apiUri = URI.create("http://" + requestUri.getHost());

        for (final SystemApi systemApi : SystemApi.values()) {
            if (systemApi.getUri().equals(apiUri)) {

                final ErrorReport errorReport = context.newModel(ErrorReport.class);
                errorReport.setStatus(Status.BAD_REQUEST);
                errorReport.setRequestUri(requestUri);
                errorReport.setDescription("System APIs cannot be reloaded.");
                errorReport.setTitle("API Load Failed");

                throw new WrmlServletException(errorReport);
            }
        }

        final ApiNavigator apiNavigator;

        try {
            apiNavigator = context.getApiLoader().loadApi(apiUri);
        }
        catch (ApiLoaderException e) {

            final ErrorReport errorReport = context.newModel(ErrorReport.class);
            errorReport.setStatus(Status.INTERNAL_SERVER_ERROR);
            errorReport.setRequestUri(requestUri);
            errorReport.setDescription(e.getLocalizedMessage());
            errorReport.setTitle("API Load Failed");

            throw new WrmlServletException(errorReport);
        }

        final Api api = apiNavigator.getApi();
        return api;
    }

}

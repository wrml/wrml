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

package org.wrml.server;

import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.wrml.model.Model;
import org.wrml.model.format.Format;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Method;
import org.wrml.runtime.*;
import org.wrml.runtime.format.FormatLoader;
import org.wrml.runtime.format.Formatter;
import org.wrml.runtime.format.SystemFormat;
import org.wrml.runtime.rest.*;
import org.wrml.runtime.rest.MediaType.MediaTypeException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class WrmlServletTest
{

    private static final String BAD_HOST_1 = "moose::/squirrel:/";

    private static final String BAD_HOST_2 = "http://localhost:8080/";

    private static final String LOCALHOST = "localhost";

    private static final String CAPRICA_API_DOMAIN = "api.caprica.wrml.org";

    private static final String PORT_1 = "8080";

    private static final String PORT_2 = "80";

    private static final String DEFAULT_CONTENT_TYPE = SystemFormat.json.getUniqueName().getFullName();

    private static final URI DOCROOT_ENDPOINT = URI.create("http://" + CAPRICA_API_DOMAIN);

    private static final URI DOCROOT_SLASH_ENDPOINT = DOCROOT_ENDPOINT.resolve("/");

    private static final URI CAPRICA_SIX_SPOOF_1_ENDPOINT = URI.create("http://localhost/capricas/6");

    private static final URI CAPRICA_SIX_SPOOF_2_ENDPOINT = URI.create("http://localhost:8080/capricas/6");

    private static final URI CAPRICA_SIX_ENDPOINT = DOCROOT_ENDPOINT.resolve("/capricas/6");

    private static final URI CAPRICA_SCHEMA_URI = SystemApi.Schema.getUri().resolve("/org/wrml/example/model/Caprica");

    private static final String JSON_MEDIA_TYPE = SystemFormat.json.getUniqueName().getFullName();

    private Engine _Engine;

    private WrmlServlet _Servlet;

    @Before
    public void init() throws MediaTypeException
    {

        _Servlet = new WrmlServlet();
        _Engine = mock(Engine.class);

        Context context = mock(Context.class);
        when(_Engine.getContext()).thenReturn(context);

        FormatLoader formatLoader = mock(FormatLoader.class);
        when(context.getFormatLoader()).thenReturn(formatLoader);

        final URI defaultFormatUri = SystemFormat.json.getFormatUri();
        final MediaType defaultFormatMediaType = SystemFormat.json.getMediaType();

        Format defaultFormat = mock(Format.class);
        when(formatLoader.getDefaultFormat()).thenReturn(defaultFormat);
        when(formatLoader.getDefaultFormatUri()).thenReturn(defaultFormatUri);
        when(formatLoader.getLoadedFormat((MediaType) null)).thenReturn(defaultFormat);
        when(formatLoader.getLoadedFormat(defaultFormatMediaType)).thenReturn(defaultFormat);

        when(defaultFormat.getMediaType()).thenReturn(defaultFormatMediaType);
        when(defaultFormat.getUri()).thenReturn(defaultFormatUri);

        ApiLoader loader = mock(ApiLoader.class);
        when(context.getApiLoader()).thenReturn(loader);

    }

    private void initMockWrmlRequest(final MockHttpServletRequest request, final Method method, final URI requestUri, final URI responseSchemaUri)
    {

        _Servlet.setEngine(_Engine);
        final Context context = _Engine.getContext();

        final URI documentSchemaUri = SystemApi.Schema.getUri().resolve(Document.class.getName().replace('.', '/'));
        final Keys keys = new KeysBuilder().addKey(documentSchemaUri, requestUri).toKeys();

        final Model responseModel = mock(Model.class);
        when(responseModel.getSchemaUri()).thenReturn(responseSchemaUri);

        ApiLoader loader = context.getApiLoader();
        when(loader.getDefaultResponseSchemaUri(method, requestUri)).thenReturn(responseSchemaUri);
        when(loader.buildDocumentKeys(requestUri, responseSchemaUri)).thenReturn(keys);
        when(loader.buildDocumentDimensions(any(Method.class), any(URI.class), any(DimensionsBuilder.class))).thenAnswer(new Answer<Dimensions>()
        {
            @Override
            public Dimensions answer(final InvocationOnMock invocation) throws Throwable
            {

                DimensionsBuilder dimensionsBuilder = (DimensionsBuilder) invocation.getArguments()[2];

                final Dimensions dimensions = dimensionsBuilder.toDimensions();

                when(responseModel.getDimensions()).thenReturn(dimensions);

                return dimensions;
            }
        });

        initMockApiNavigator(method, requestUri, responseSchemaUri);

        when(context.request(any(Method.class), any(Keys.class), (Dimensions) anyObject(), (Model) anyObject())).thenReturn(responseModel);
    }

    private void initMockApiNavigator(final Method method, final URI requestUri, final URI responseSchemaUri)
    {

        final Context context = _Engine.getContext();
        ApiLoader loader = context.getApiLoader();
        final ApiNavigator apiNavigator = mock(ApiNavigator.class);
        when(loader.getParentApiNavigator(requestUri)).thenReturn(apiNavigator);

        final Api api = mock(Api.class);
        when(api.getTitle()).thenReturn("Test Mock API");
        when(apiNavigator.getApi()).thenReturn(api);

        final Resource endpointResource = mock(Resource.class);
        when(apiNavigator.getResource(requestUri)).thenReturn(endpointResource);

        final Set<URI> responseSchemaUris = new LinkedHashSet<>();
        responseSchemaUris.add(responseSchemaUri);
        when(endpointResource.getResponseSchemaUris(method)).thenReturn(responseSchemaUris);

    }

    private void initMockHttpRequest(MockHttpServletRequest req, URI uri)
    {

        req.setRequestURI(uri.toString());
        req.setPathInfo(uri.getPath());
        req.setRemotePort(uri.getPort());
        req.setRemoteHost(uri.getHost());
        req.setScheme(uri.getScheme());
        req.setContextPath("/");
    }

    @After
    public void destruct()
    {

        _Engine = null;
        _Servlet = null;
    }

    @Test
    public void create()
    {

        assertTrue(_Servlet != null);
    }

    @Test
    public void createAndConfigure() throws MediaTypeException
    {

        _Servlet.setEngine(_Engine);
    }

    @Test(expected = ServletException.class)
    public void createAndInitNoParam() throws ServletException
    {

        ServletConfig servletConfig = mock(ServletConfig.class);
        Enumeration<String> eStrings = new TestEmptyEnum();
        when(servletConfig.getInitParameterNames()).thenReturn(eStrings);

        _Servlet.init(servletConfig);
    }

    // Checks the logic in a given for loop for debugging
    @Test(expected = ServletException.class)
    public void createAndInitTwoParam() throws ServletException
    {

        ServletConfig servletConfig = mock(ServletConfig.class);
        Enumeration<String> eStrings = new TestTwoEnum();
        when(servletConfig.getInitParameterNames()).thenReturn(eStrings);
        _Servlet.init(servletConfig);
    }

    @Test(expected = ServletException.class)
    public void createAndInitBadLocation() throws ServletException
    {

        ServletConfig servletConfig = mock(ServletConfig.class);
        Enumeration<String> eStrings = new TestEmptyEnum();
        when(servletConfig.getInitParameterNames()).thenReturn(eStrings);
        String configLocation = "abcdefg";
        when(servletConfig.getInitParameter(WrmlServlet.WRML_CONFIGURATION_FILE_PATH_INIT_PARAM_NAME)).thenReturn(configLocation);
        _Servlet.init(servletConfig);
    }

    @Test(expected = ServletException.class)
    public void createAndInitMalformedFile() throws ServletException
    {

        ServletConfig servletConfig = mock(ServletConfig.class);
        Enumeration<String> eStrings = new TestEmptyEnum();
        when(servletConfig.getInitParameterNames()).thenReturn(eStrings);
        String configLocation = "wrmlbad.json";
        when(servletConfig.getInitParameter(WrmlServlet.WRML_CONFIGURATION_FILE_PATH_INIT_PARAM_NAME)).thenReturn(configLocation);
        _Servlet.init(servletConfig);
    }

    @Test
    public void createAndInit() throws ServletException
    {

        ServletConfig servletConfig = mock(ServletConfig.class);
        Enumeration<String> eStrings = new TestEmptyEnum();
        when(servletConfig.getInitParameterNames()).thenReturn(eStrings);
        String configLocation = "wrml.json";
        when(servletConfig.getInitParameter(WrmlServlet.WRML_CONFIGURATION_RESOURCE_PATH_INIT_PARAM_NAME)).thenReturn(configLocation);
        _Servlet.init(servletConfig);
    }

    // TODO: Make this a different exception type.
    @Test(expected = IllegalArgumentException.class)
    public void requestBadMethod() throws ServletException, IOException
    {

        _Servlet.setEngine(_Engine);

        MockHttpServletRequest request = new MockHttpServletRequest();
        this.initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod("BAD");

        MockHttpServletResponse response = new MockHttpServletResponse();
        _Servlet.service(request, response);
    }

    /**
     * This tests that the system returns an error when no 1) Accept Header is listed, and 2) No default is provided by
     * the engine
     */
    @Test
    public void requestNoAcceptHeaderNoDefault() throws ServletException, IOException
    {

        _Servlet.setEngine(_Engine);

        initMockApiNavigator(Method.Get, CAPRICA_SIX_ENDPOINT, CAPRICA_SCHEMA_URI);

        final Context context = _Engine.getContext();
        final ApiNavigator apiNavigator = context.getApiLoader().getParentApiNavigator(CAPRICA_SIX_ENDPOINT);
        final Resource endpointResource = apiNavigator.getResource(CAPRICA_SIX_ENDPOINT);
        when(endpointResource.getResponseSchemaUris(Method.Get)).thenReturn(Collections.EMPTY_SET);

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        // request.addHeader("Accept", null);

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream out = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(out);


        _Servlet.service(request, response);


        verify(response, times(1)).setContentType(ContentType.TEXT_PLAIN.toString());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        //verify(response, times(0)).setContentLength(anyInt());
        verify(response, times(1)).flushBuffer();
    }

    @Test
    public void requestNoAcceptHeaderNotFound() throws ServletException, IOException
    {


        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        // request.addHeader("Accept", null);

        HttpServletResponse response = mock(HttpServletResponse.class);

        initMockWrmlRequest(request, Method.Get, CAPRICA_SIX_ENDPOINT, CAPRICA_SCHEMA_URI);
        final Context context = _Engine.getContext();
        when(context.request(any(Method.class), any(Keys.class), any(Dimensions.class), any(Model.class))).thenReturn(null);

        _Servlet.service(request, response);

        // Verify not found
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response, times(1)).setContentLength(0);
        verify(response, times(1)).flushBuffer();
    }

    // This tests checking the engine for a default schema to service this request.
    @Test
    public void requestNoAcceptHeaderFound() throws ServletException, IOException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream out = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(out);

        initMockWrmlRequest(request, Method.Get, CAPRICA_SIX_ENDPOINT, CAPRICA_SCHEMA_URI);

        _Servlet.service(request, response);

        // Verify Model Write
        verify(response, times(1)).setContentType(DEFAULT_CONTENT_TYPE);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(response, times(1)).setContentLength(anyInt());
        verify(response, times(1)).flushBuffer();
    }

    @Test
    public void requestSingleAcceptHeader() throws ServletException, IOException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        request.addHeader(HttpHeaders.ACCEPT, JSON_MEDIA_TYPE);

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream out = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(out);

        initMockWrmlRequest(request, Method.Get, CAPRICA_SIX_ENDPOINT, null);

        _Servlet.service(request, response);

        // Verify Model Write
        verify(response, times(1)).setContentType(DEFAULT_CONTENT_TYPE);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(response, times(1)).setContentLength(anyInt());
        verify(response, times(1)).flushBuffer();
    }

    @Test
    public void requestApiLoaderException() throws ServletException, IOException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        //request.addHeader(HttpHeaders.ACCEPT, JSON_MEDIA_TYPE);

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream out = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(out);


        initMockWrmlRequest(request, Method.Get, CAPRICA_SIX_ENDPOINT, CAPRICA_SCHEMA_URI);

        Throwable mockThrowable = mock(ApiLoaderException.class);
        String mockThrowableMessage = "This is an error message.";
        when(mockThrowable.getMessage()).thenReturn(mockThrowableMessage);

        final Context context = _Engine.getContext();
        when(context.request(any(Method.class), any(Keys.class), any(Dimensions.class), any(Model.class))).thenThrow(mockThrowable);

        _Servlet.service(request, response);

        // Verify Model Write
        verify(response, times(1)).setContentType(ContentType.TEXT_PLAIN.toString());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response, times(1)).setContentLength(anyInt());
        verify(response, times(1)).flushBuffer();
    }

    @Test
    public void requestWithBadHostHeader() throws ServletException, IOException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        //request.addHeader(HttpHeaders.ACCEPT, JSON_MEDIA_TYPE);
        request.addHeader(WrmlServlet.WRML_HOST_HEADER_NAME, BAD_HOST_1);

        MockHttpServletResponse response = new MockHttpServletResponse();

        initMockWrmlRequest(request, Method.Get, CAPRICA_SIX_ENDPOINT, CAPRICA_SCHEMA_URI);

        _Servlet.service(request, response);

        // Verify Model Write
        Assert.assertTrue(response.getContentType().contains("text/plain"));
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        Assert.assertTrue(response.getContentLength() > 0);
        Assert.assertTrue(response.getContentAsString().contains("moose::/squirrel:"));
    }

    @Test
    public void requestWithBadHostHeader2() throws ServletException, IOException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        //request.addHeader(HttpHeaders.ACCEPT, JSON_MEDIA_TYPE);
        request.addHeader(WrmlServlet.WRML_HOST_HEADER_NAME, BAD_HOST_2);

        initMockWrmlRequest(request, Method.Get, CAPRICA_SIX_ENDPOINT, CAPRICA_SCHEMA_URI);

        MockHttpServletResponse response = new MockHttpServletResponse();

        _Servlet.service(request, response);

        // Verify Model Write
        Assert.assertTrue(response.getContentType().contains("text/plain"));
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void requestWithHostHeader() throws ServletException, IOException
    {


        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_SPOOF_1_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        request.addHeader(WrmlServlet.WRML_HOST_HEADER_NAME, LOCALHOST);

        MockHttpServletResponse response = new MockHttpServletResponse();


        initMockWrmlRequest(request, Method.Get, CAPRICA_SIX_SPOOF_1_ENDPOINT, CAPRICA_SCHEMA_URI);

        _Servlet.service(request, response);

        // Verify Model Write
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals(DEFAULT_CONTENT_TYPE, response.getContentType());
        Assert.assertEquals(response.getContentAsByteArray().length, response.getContentLength());
    }

    @Test
    public void requestWithHostAndPortHeaders() throws ServletException, IOException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        request.addHeader(WrmlServlet.WRML_HOST_HEADER_NAME, LOCALHOST);
        request.addHeader(WrmlServlet.WRML_PORT_HEADER_NAME, PORT_1);

        MockHttpServletResponse response = new MockHttpServletResponse();

        initMockWrmlRequest(request, Method.Get, CAPRICA_SIX_SPOOF_2_ENDPOINT, CAPRICA_SCHEMA_URI);

        _Servlet.service(request, response);

        // Verify Model Write
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals(DEFAULT_CONTENT_TYPE, response.getContentType());
        Assert.assertEquals(response.getContentAsByteArray().length, response.getContentLength());
    }

    @Test
    public void testGetRequestUriBadHost() throws IOException, URISyntaxException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        request.addHeader(WrmlServlet.WRML_HOST_HEADER_NAME, BAD_HOST_1);

        try
        {
            _Servlet.getRequestUri(request);
        }
        catch (URISyntaxException use)
        {
            Assert.assertTrue(use.getMessage().contains(BAD_HOST_1));
            return;
        }
        Assert.assertTrue(false);
    }

    @Test
    public void testGetResourceIdBadHost2() throws IOException, URISyntaxException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        //request.addHeader(HttpHeaders.ACCEPT, JSON_MEDIA_TYPE);
        request.addHeader(WrmlServlet.WRML_HOST_HEADER_NAME, BAD_HOST_2);

        try
        {
            URI resourceUri = _Servlet.getRequestUri(request);
        }
        catch (URISyntaxException use)
        {
            Assert.assertTrue(use.getMessage().contains(BAD_HOST_2));
            return;
        }
        Assert.assertTrue(false);
    }

    @Test
    public void testGetResourceIdHostNoChange() throws IOException, URISyntaxException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_SPOOF_1_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        request.addHeader(WrmlServlet.WRML_HOST_HEADER_NAME, LOCALHOST);

        URI resourceUri = _Servlet.getRequestUri(request);
        Assert.assertEquals(resourceUri, CAPRICA_SIX_SPOOF_1_ENDPOINT);
    }

    @Test
    public void testGetResourceIdHost() throws IOException, URISyntaxException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_SPOOF_1_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        request.addHeader(WrmlServlet.WRML_HOST_HEADER_NAME, CAPRICA_API_DOMAIN);

        URI resourceUri = _Servlet.getRequestUri(request);
        Assert.assertEquals(resourceUri, CAPRICA_SIX_ENDPOINT);
    }

    @Test
    public void testGetResourceIdPort() throws IOException, URISyntaxException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_SPOOF_1_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        request.addHeader(WrmlServlet.WRML_PORT_HEADER_NAME, PORT_1);

        URI resourceUri = _Servlet.getRequestUri(request);
        Assert.assertEquals(resourceUri, CAPRICA_SIX_SPOOF_2_ENDPOINT);
    }

    @Test
    public void testGetResourceIdPortNoChange() throws IOException, URISyntaxException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_SPOOF_1_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        request.addHeader(WrmlServlet.WRML_PORT_HEADER_NAME, PORT_2);

        URI resourceUri = _Servlet.getRequestUri(request);
        Assert.assertEquals(resourceUri, CAPRICA_SIX_SPOOF_1_ENDPOINT);
    }

    @Test
    public void testGetResourceIdHostAndPort() throws IOException, URISyntaxException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();

        initMockHttpRequest(request, CAPRICA_SIX_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());
        request.addHeader(WrmlServlet.WRML_HOST_HEADER_NAME, LOCALHOST);
        request.addHeader(WrmlServlet.WRML_PORT_HEADER_NAME, PORT_1);

        URI resourceUri = _Servlet.getRequestUri(request);
        Assert.assertEquals(resourceUri, CAPRICA_SIX_SPOOF_2_ENDPOINT);
    }

    /*
     * This test is to verify that, given a wildcard accept, we use the default.
     */
    @Test
    public void requestWithWildCardAccept() throws ServletException, IOException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, CAPRICA_SIX_SPOOF_2_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());

        MockHttpServletResponse response = new MockHttpServletResponse();

        initMockWrmlRequest(request, Method.Get, CAPRICA_SIX_SPOOF_2_ENDPOINT, CAPRICA_SCHEMA_URI);

        _Servlet.service(request, response);

        // Verify Model Write
        Assert.assertEquals(DEFAULT_CONTENT_TYPE, response.getContentType());
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    // This tests checking the engine for a default schema to service this request.
    @Test
    public void requestRootWithSlash() throws ServletException, IOException
    {


        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, DOCROOT_SLASH_ENDPOINT);
        request.setMethod(Method.Get.getProtocolGivenName());

        MockHttpServletResponse response = new MockHttpServletResponse();

        initMockWrmlRequest(request, Method.Get, DOCROOT_ENDPOINT, CAPRICA_SCHEMA_URI);

        _Servlet.service(request, response);

        // Verify Model Write
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals(DEFAULT_CONTENT_TYPE, response.getContentType());
    }

    // --------------------------------- Method.Save tests (Input Model reading).

    @Test
    public void requestPostNoData() throws ServletException, IOException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();

        initMockHttpRequest(request, DOCROOT_ENDPOINT);
        request.setMethod(Method.Save.getProtocolGivenName());
        request.setContentType(DEFAULT_CONTENT_TYPE);
        request.setContent(new byte[]{});

        MockHttpServletResponse response = new MockHttpServletResponse();

        initMockWrmlRequest(request, Method.Save, DOCROOT_ENDPOINT, CAPRICA_SCHEMA_URI);


        _Servlet.service(request, response);

        // Verify Model Write
        Assert.assertEquals(DEFAULT_CONTENT_TYPE, response.getContentType());
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals(response.getContentAsByteArray().length, response.getContentLength());
    }

    @Test
    public void requestWithoutEntityButWithContentType() throws ServletException, IOException
    {

        MockHttpServletRequest request = new MockHttpServletRequest();
        initMockHttpRequest(request, DOCROOT_ENDPOINT);
        request.setMethod(Method.Save.getProtocolGivenName());
        request.setContentType(DEFAULT_CONTENT_TYPE);
        request.setContent(new byte[]{});

        MockHttpServletResponse response = new MockHttpServletResponse();

        initMockWrmlRequest(request, Method.Save, DOCROOT_ENDPOINT, CAPRICA_SCHEMA_URI);

        _Servlet.service(request, response);

        // Verify Model Write
        Assert.assertEquals(DEFAULT_CONTENT_TYPE, response.getContentType());
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals(response.getContentAsByteArray().length, response.getContentLength());
    }

    public void readModelTest() throws Exception
    {

    }

    @Test
    public void findOptimalTypeTest1() throws Exception
    {

        _Servlet.setEngine(_Engine);

        final Context context = _Engine.getContext();
        final FormatLoader formatLoader = context.getFormatLoader();

        List<MediaType> accepts = new ArrayList<MediaType>();

        final String schemaUriString = "http://foo.bar.baz/uberschema";
        final String formatUriString = "http://foo.bar.baz/uberformat";
        final String bogusUriString = "http://bingo.razor.com/fliberdijibbet";

        final URI schemaUri = URI.create(schemaUriString);
        final URI formatUri = URI.create(formatUriString);
        final URI bogusUri = URI.create(bogusUriString);

        accepts.add(new MediaType("application/wrml; schema=\"" + schemaUriString + "\"; format=\"" + formatUriString + "\""));

        when(formatLoader.getFormatter(formatUri)).thenReturn(mock(Formatter.class));

        MediaType optimal = _Servlet.getMostAcceptableMediaType(schemaUri, accepts);
        Assert.assertTrue(accepts.get(0) == optimal);

        optimal = _Servlet.getMostAcceptableMediaType(bogusUri, accepts);
        Assert.assertNull(optimal);
    }

    @Test
    public void getRequestFormatUriTest() throws Exception
    {

        _Servlet.setEngine(_Engine);

        URI actual = _Servlet.getRequestFormatUri(new MediaType("application/wrml; schema=\"http://foo.bar.baz/uberschema\"; format=\"http://foo.bar.baz/uberformat\""));
        Assert.assertEquals(new URI("http://foo.bar.baz/uberformat"), actual);

        final URI jsonFormatUri = SystemFormat.json.getFormatUri();
        actual = _Servlet.getRequestFormatUri(new MediaType("application/json"));
        Assert.assertEquals(jsonFormatUri, actual);

        actual = _Servlet.getRequestFormatUri(null);
        Assert.assertEquals(jsonFormatUri, actual);
    }

    // Test support classes
    public class TestEmptyEnum implements Enumeration<String>
    {

        @Override
        public boolean hasMoreElements()
        {

            return false;
        }

        @Override
        public String nextElement()
        {

            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public class TestTwoEnum implements Enumeration<String>
    {

        private int MAX = 2;

        private int index = 0;

        @Override
        public boolean hasMoreElements()
        {

            return index < MAX;
        }

        @Override
        public String nextElement()
        {

            if (hasMoreElements())
            {
                return "" + ++index;
            }
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}

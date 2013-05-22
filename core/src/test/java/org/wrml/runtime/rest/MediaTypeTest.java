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
package org.wrml.runtime.rest;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.runtime.rest.MediaType.MediaTypeException;

public class MediaTypeTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaTypeTest.class);

    @Before
    public void init()
    {

    }

    @After
    public void destruct()
    {

    }

    @Test
    public void testSimpleContentType1() throws MediaTypeException
    {
        String contentType = "Content-Type: text/plain";
        MediaType type = new MediaType(contentType);

        LOGGER.debug("Parsed media type is : " + type);

        assertTrue(type.getType().equals("text"));
        assertTrue(type.getSubType().equals("plain"));
        assertTrue(type.getParameters().isEmpty());
    }

    @Test
    public void testSimpleContentType2() throws MediaTypeException
    {
        String contentType = "text/plain";
        MediaType type = new MediaType(contentType);

        LOGGER.debug("Parsed media type is : " + type);

        assertTrue(type.getType().equals("text"));
        assertTrue(type.getSubType().equals("plain"));
        assertTrue(type.getParameters().isEmpty());
    }

    @Test
    public void testSimpleContentType3() throws MediaTypeException
    {
        String contentType = "Content-Type: text/plain;";
        MediaType type = new MediaType(contentType);

        LOGGER.debug("Parsed media type is : " + type);

        assertTrue(type.getType().equals("text"));
        assertTrue(type.getSubType().equals("plain"));
        assertTrue(type.getParameters().isEmpty());
    }

    @Test
    public void testSimpleContentType4() throws MediaTypeException
    {
        String contentType = "Content-Type: text/plain; charset=us-ascii";
        MediaType type = new MediaType(contentType);

        LOGGER.debug("Parsed media type is : " + type);

        assertTrue(type.getType().equals("text"));
        assertTrue(type.getSubType().equals("plain"));
        assertTrue(!type.getParameters().isEmpty());
        assertTrue(type.getParameters().size() == 1);
        assertTrue(type.getParameter("charset").equals("us-ascii"));
    }

    @Test
    public void testContentType1() throws MediaTypeException
    {
        String contentType = "Content-Type: text/plain; charset=us-ascii;ben=test; space=\"aliens\"";
        MediaType type = new MediaType(contentType);

        LOGGER.debug("Parsed media type is : " + type);

        assertTrue(type.getType().equals("text"));
        assertTrue(type.getSubType().equals("plain"));
        assertTrue(!type.getParameters().isEmpty());
        assertTrue("Expected size of 3 but got " + type.getParameters().size(), type.getParameters().size() == 3);
        assertTrue(type.getParameter("charset").equals("us-ascii"));
        assertTrue(type.getParameter("ben").equals("test"));
        assertTrue("Expected a value of aliens but got " + type.getParameter("space"), type.getParameter("space").equals("aliens"));
    }

    @Test
    public void testContentType2() throws MediaTypeException
    {
        String contentType = "text/plain; charset=us-ascii;ben=test; space=\"aliens\"";
        MediaType type = new MediaType(contentType);

        LOGGER.debug("Parsed media type is : " + type);

        assertTrue(type.getType().equals("text"));
        assertTrue(type.getSubType().equals("plain"));
        assertTrue(!type.getParameters().isEmpty());
        assertTrue("Expected size of 3 but got " + type.getParameters().size(), type.getParameters().size() == 3);
        assertTrue(type.getParameter("charset").equals("us-ascii"));
        assertTrue(type.getParameter("ben").equals("test"));
        assertTrue("Expected a value of aliens but got " + type.getParameter("space"), type.getParameter("space").equals("aliens"));
    }

    @Test
    public void testContentType3() throws MediaTypeException
    {
        String contentType = "Content-Type: text/plain; charset=us-ascii;ben=test; space=\"ali;ens\"";
        MediaType type = new MediaType(contentType);

        LOGGER.debug("Parsed media type is : " + type);

        assertTrue(type.getType().equals("text"));
        assertTrue(type.getSubType().equals("plain"));
        assertTrue(!type.getParameters().isEmpty());
        assertTrue("Expected size of 3 but got " + type.getParameters().size(), type.getParameters().size() == 3);
        assertTrue(type.getParameter("charset").equals("us-ascii"));
        assertTrue(type.getParameter("ben").equals("test"));
        assertTrue("Expected a value of aliens but got " + type.getParameter("space"), type.getParameter("space").equals("ali;ens"));
    }

    @Test
    public void wrmlContentTypeTest1() throws MediaTypeException
    {
        String contentType = "Content-Type: application/wrml; schema=http://schema.api.wrml.org/org/wrml/example/model/Caprica;format=\"http://format.api.wrml.org/org/wrml/format/application/json\"";
        MediaType type = new MediaType(contentType);

        LOGGER.debug("Parsed media type is : " + type);

        assertTrue(type.getType().equals("application"));
        assertTrue(type.getSubType().equals("wrml"));
        assertTrue(type.getParameters().size() == 2);
        assertTrue(type.getParameter("schema").equals("http://schema.api.wrml.org/org/wrml/example/model/Caprica"));
        assertTrue(type.getParameter("format").equals("http://format.api.wrml.org/org/wrml/format/application/json"));
    }
}

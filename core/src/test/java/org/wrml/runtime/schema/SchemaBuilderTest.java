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
package org.wrml.runtime.schema;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wrml.model.UniquelyNamed;
import org.wrml.model.rest.Document;
import org.wrml.runtime.Context;
import org.wrml.runtime.Engine;
import org.wrml.runtime.EngineTest;
import org.wrml.runtime.rest.SystemApi;
import org.wrml.util.UniqueName;

import java.net.URI;
import java.util.Date;

public class SchemaBuilderTest {
    private Context _Context;

    @Before
    public void setUp() throws Exception {

        final Engine engine = EngineTest.createTestEngine();
        _Context = engine.getContext();

    }

    @After
    public void tearDown() throws Exception {

        _Context = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorParamContextNullFailure() {

        new SchemaBuilder(null);
    }

    @Test
    public void contextNotNull() {

        final SchemaBuilder schemaBuilder = new SchemaBuilder(_Context);
        Assert.assertNotNull(schemaBuilder.getContext());
    }

    @Test
    public void schemaNotNull() {

        final SchemaBuilder schemaBuilder = new SchemaBuilder(_Context);
        Assert.assertNotNull(schemaBuilder.toSchema());

    }

    @Test
    public void loadSchema001() throws ClassNotFoundException {

        final UniqueName uniqueName = UniqueName.createTemporaryUniqueName();
        final URI uri = SystemApi.Schema.getUri().resolve("/" + uniqueName.toString());

        final SchemaBuilder schemaBuilder = new SchemaBuilder(_Context, UniquelyNamed.SLOT_NAME_UNIQUE_NAME, uniqueName, Document.SLOT_NAME_URI, uri, "testInt1", 7, "testFlag",
                true, "testTime", new Date(), "testText", "Hello World", "testLong", 8L);

        Assert.assertNotNull(schemaBuilder.load());

    }

}

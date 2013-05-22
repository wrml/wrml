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

package org.wrml.werminal.dialog;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import org.mockito.runners.MockitoJUnitRunner;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.util.JavaBean;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.terminal.TerminalAppPanel;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link OpenModelDialog}.
 * <p/>
 * Only select high-value JUnit tests were added for coverage. The highest-profiled hotspot is
 * {@link OpenModelDialog#updateKeysPanel}.
 * <p/>
 * Some dependencies could not be (Power)Mocked due to java/inheritance limitations, complexities.
 *
 * @author JJ Zabkar
 */
@RunWith(MockitoJUnitRunner.class)
// @RunWith(PowerMockRunner.class)
// @PrepareForTest({ WerminalWindow.class }) // no worky (stackmap err)
// @PrepareForTest({ TerminalApp.class }) // no worky (stackmap err)
// @PrepareForTest({ JavaBean.class })
@Ignore
public class OpenModelDialogTest
{

    private final URI mockSchemaUri = URI.create("mockSchemaUri");

    private final Object mockSchemaUriOld = URI.create("mockSchemaUriOld");

    /**
     * class under test
     */
    OpenModelDialog _OpenModelDialog;

    @Mock
    private WerminalAction mockConfirmAction;

    @Mock
    private TerminalAppPanel mockKeysPanel;

    @Mock
    private Set<String> mockKeySlotNames;

    @Mock
    private Map<URI, WerminalTextBox> mockKeyInputs;

    @Mock
    private WerminalTextBox mockWerminalTextBox;

    @Mock
    private SchemaLoader mockSchemaLoader;

    // @Mock
    private Prototype mockPrototype;

    @Before
    public void setUp() throws Exception
    {

        MockUtil mockUtil = new MockUtil();

        _OpenModelDialog = new OpenModelDialog();
        _OpenModelDialog._SchemaUriTextBox = mockWerminalTextBox;
        _OpenModelDialog._KeysPanel = mockKeysPanel;
        _OpenModelDialog._ConfirmAction = mockConfirmAction;
        _OpenModelDialog._KeySlotNames = mockKeySlotNames;
        _OpenModelDialog._KeyInputs = mockKeyInputs;
        _OpenModelDialog._SchemaUri = mockSchemaUri;

        //_OpenModelDialog._SchemaLoader = mock(SchemaLoader.class, Mockito.RETURNS_DEEP_STUBS);
        //_OpenModelDialog._ApiLoader = mock(ApiLoader.class);
        mockPrototype = mock(Prototype.class, Mockito.RETURNS_DEEP_STUBS);

        assertNotNull(mockWerminalTextBox);
        assertTrue(mockUtil.isMock(mockWerminalTextBox));

        when(mockWerminalTextBox.setValue(any(Object.class))).thenReturn(mockSchemaUriOld);
        when(mockSchemaLoader.getPrototype(any(URI.class))).thenReturn(mockPrototype);

    }

    /**
     * fails on NPE due to unmockable inherited context
     */
    @Test(expected = NullPointerException.class)
    public void testConstructor4ArgNull()
    {

        _OpenModelDialog = new OpenModelDialog(null, null, null, null);
        fail("expected NPE");
    }

    @Test
    @Ignore
    public void testConstructor() throws Exception
    {

        Werminal mockWerminal = mock(Werminal.class, Mockito.RETURNS_DEEP_STUBS);
        // deep stub: http://docs.mockito.googlecode.com/hg/org/mockito/Mockito.html#RETURNS_DEEP_STUBS
        when(mockWerminal.getContext().getSchemaLoader().getApiSchemaUri()).thenReturn(mockSchemaUri);
        _OpenModelDialog = new OpenModelDialog(mockWerminal, null, null, null);
    }

    @Test
    public void testDefaultConstructor()
    {

        assertNotNull(_OpenModelDialog);
    }

    @Test
    public void testSetKeys()
    {

        _OpenModelDialog.setKeys(null);
    }

    @Test
    public void testSetSchemaUriNull()
    {

        _OpenModelDialog.setSchemaUri(null);
    }

    /**
     * Fails on NPE related to {@link JavaBean}. Worth troubleshooting deep mocking?
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @Ignore
    public void testSetSchemaUri() throws ClassNotFoundException
    {

        JavaBean mockJavaBean = mock(JavaBean.class);
        Class mockSchemaInterface = Schema.class;
        when(mockSchemaLoader.getSchemaInterface(mockSchemaUri)).thenReturn(mockSchemaInterface);
        when(mockPrototype.getSchemaBean()).thenReturn(mockJavaBean);

        _OpenModelDialog.setSchemaUri(mockSchemaUri);
    }
}

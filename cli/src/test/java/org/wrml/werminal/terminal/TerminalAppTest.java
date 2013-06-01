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
package org.wrml.werminal.terminal;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.GUIScreen.Position;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.dialog.DialogResult;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalSize;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wrml.runtime.Context;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Test for {@link TerminalApp}.
 * <p/>
 * Initial goal is to hit most-used methods based on profiling results.
 *
 * @author JJ Zabkar
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MessageBox.class, TerminalFacade.class, DialogResult.class, Position.class})
public class TerminalAppTest
{

    /**
     * class under test
     */
    private TerminalApp _TerminalApp;

    @Mock
    private Context mockContext;

    @Mock
    private String mockErrorMessage;

    @Mock
    private String mockTitle;

    @Mock
    private String mockMessage;

    @Mock
    private DialogResult mockDialogResult;

    @Mock
    private Throwable mockThrowable;

    @Mock
    private Window mockWindow;

    @Mock
    private Position mockPosition;

    @Mock
    private GUIScreen mockGUIScreen;

    @Mock
    private Screen mockScreen;

    @Mock
    private TerminalSize mockTerminalSize;

    @Before
    public void setUp() throws Exception
    {

        mockStatic(MessageBox.class);
        mockStatic(TerminalFacade.class);

        when(MessageBox.showMessageBox(any(GUIScreen.class), any(String.class), any(String.class))).thenReturn(
                mockDialogResult);
        when(TerminalFacade.createGUIScreen(any(Terminal.class))).thenReturn(mockGUIScreen);
        when(TerminalFacade.createScreen(any(Terminal.class))).thenReturn(mockScreen);
        when(mockGUIScreen.getScreen()).thenReturn(mockScreen);
        when(mockScreen.getTerminalSize()).thenReturn(mockTerminalSize);
        when(mockTerminalSize.getColumns()).thenReturn(100);

        _TerminalApp = new TerminalApp("Test", mockContext);
    }

    @After
    public void tearDown() throws Exception
    {

        _TerminalApp = null;
    }

    @Test
    public void testShowErrorString()
    {

        _TerminalApp.showError(mockErrorMessage);
    }

    @Test
    public void testShowErrorStringThrowable()
    {

        _TerminalApp.showError(mockErrorMessage, mockThrowable);
    }

    @Test
    public void testShowMessageBox()
    {

        _TerminalApp.showMessageBox(mockTitle, mockMessage);
    }

    @Test
    public void testShowWindowWindow()
    {

        _TerminalApp.showWindow(mockWindow);
    }

    @Test
    public void testShowWindowWindowPosition()
    {

        _TerminalApp.showWindow(mockWindow, mockPosition);
    }

}

/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wrml.cli.gui;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Theme;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.CheckBoxList;
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.dialog.DialogResult;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.layout.SizePolicy;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.TerminalSize;
import com.googlecode.lanterna.terminal.text.UnixTerminal;

import org.wrml.cli.gui.base.BasePanel;
import org.wrml.cli.gui.base.ButtonPanel;
import org.wrml.cli.gui.base.Menu;
import org.wrml.cli.gui.base.MenuBarWindow;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Engine;

/**
 * WRML's command line interface's graphical user interface (aka the
 * "WRML CLI GUI").
 */
public final class Gui {

    private final URI _WrmlDotOrg;

    private final Engine _Engine;
    private final GUIScreen _Screen;

    private final Theme _DarkTheme;
    private final Theme _LightTheme;

    private final SplashWindow _SplashWindow;
    private final MenuBarWindow _MainMenuBarWindow;
    private final NewModelDialog _NewModelDialog;
    private final OpenModelDialog _OpenModelDialog;
    private final MenuBarWindow _ModelMenuBarWindow;

    private GuiAction _MainMenuNewModelAction;
    private GuiAction _NewModelAction;
    private GuiAction _NewModelConfirmationAction;

    private GuiAction _MainMenuOpenModelAction;
    private GuiAction _OpenModelAction;
    private GuiAction _OpenModelConfirmationAction;

    private GuiAction _SaveAction;

    private GuiAction _MainMenuWrmlOrgAction;
    private GuiAction _WrmlOrgAction;

    private GuiAction _CloseAction;
    private GuiAction _ExitAction;
    private GuiAction _CancelAction;

    private GuiAction _ShowModelMenuAction;

    private final Map<String, GuiAction> _UnimplementedActions;

    public Gui(final Engine engine) throws GuiException {
        _Engine = engine;

        /*
         * Using the UnixTerminal forces the use of the much cooler looking
         * Terminal shell (on Mac OS X at least).
         */
        final UnixTerminal terminal = new UnixTerminal(System.in, System.out, Charset.forName("UTF8"));
        _Screen = new GUIScreen(new Screen(terminal));

        /*
         * This construction of the GUIScreen leads to the less cool looking
         * (but still pretty cool) Swing Terminal emulator.
         */
        //_Screen = TerminalFacade.createGUIScreen();

        if (_Screen == null) {
            throw new GuiException("Couldn't allocate a terminal!", null, this);
        }

        _Screen.setShowMemoryUsage(true);

        try {
            _WrmlDotOrg = new java.net.URI("http://www.wrml.org");
        }
        catch (final URISyntaxException e) {
            throw new GuiException("Huh?", e, this);
        }

        _UnimplementedActions = new HashMap<String, GuiAction>();
        _DarkTheme = new DarkTheme();
        _LightTheme = new LightTheme();

        _SplashWindow = new SplashWindow();
        _MainMenuBarWindow = new MenuBarWindow("  WRML  ", new MainMenuBar(), new ButtonPanel(getExitAction()));
        _NewModelDialog = new NewModelDialog("  New Model  ", this, getNewModelConfirmationAction(), getCancelAction());
        _OpenModelDialog = new OpenModelDialog("  Open Model  ", this, getOpenModelConfirmationAction(),
                getCancelAction());
        _ModelMenuBarWindow = new MenuBarWindow("", new ModelMenuBar(), new ButtonPanel(getCloseAction()));

        final URI metaSchemaId = engine.getDefaultContext().getSchemaLoader().getMetaschemaId();
        _NewModelDialog.setSchemaId(metaSchemaId);
        _OpenModelDialog.setSchemaId(metaSchemaId);

        _Screen.getScreen().startScreen();

        showSplashWindow();

        Throwable error = null;
        String errorMessage = null;

        //while (true) {

        try {
            showMainMenuBarWindow();
        }
        catch (final Throwable t) {
            error = t;
            errorMessage = "An unexpected error has occurred.";
            showError(errorMessage, t);
            //continue;
        }

        //break;
        // }

        _Screen.getScreen().stopScreen();

        if ((error != null) || (errorMessage != null)) {
            System.err.println(error);
            error.printStackTrace();
        }
    }

    public void close() {

        _Screen.closeWindow();
        _Screen.getScreen().refresh();

    }

    public void copy() {

    }

    public void createModelWindow(Model model) {

        final ModelWindow modelWindow = new ModelWindow("  Model  ", this, new Component[] {
                new ButtonPanel(getShowModelMenuAction()), new ButtonPanel(getSaveAction()),
                new ButtonPanel(getCloseAction()) });

        modelWindow.setModel(model);

        showWindow(modelWindow);
    }

    public void cut() {

    }

    public void delete() {

    }

    public GuiAction getCancelAction() {
        if (_CancelAction == null) {
            _CancelAction = new CancelAction(this);
        }
        return _CancelAction;
    }

    public GuiAction getCloseAction() {
        if (_CloseAction == null) {
            _CloseAction = new ClosingGuiAction(this);
        }
        return _CloseAction;
    }

    public Theme getDarkTheme() {
        return _DarkTheme;
    }

    public Engine getEngine() {
        return _Engine;
    }

    public GuiAction getExitAction() {
        if (_ExitAction == null) {
            _ExitAction = new ExitAction(this);
        }
        return _ExitAction;
    }

    public Theme getLightTheme() {
        return _LightTheme;
    }

    public MenuBarWindow getMainMenuBarWindow() {
        return _MainMenuBarWindow;
    }

    public GuiAction getMainMenuNewModelAction() {
        if (_MainMenuNewModelAction == null) {
            _MainMenuNewModelAction = new MainMenuNewModelAction(this);
        }
        return _MainMenuNewModelAction;
    }

    public GuiAction getMainMenuOpenModelAction() {
        if (_MainMenuOpenModelAction == null) {
            _MainMenuOpenModelAction = new MainMenuOpenModelAction(this);
        }
        return _MainMenuOpenModelAction;
    }

    public GuiAction getMainMenuWrmlOrgAction() {
        if (_MainMenuWrmlOrgAction == null) {
            _MainMenuWrmlOrgAction = new MainMenuWrmlOrgAction(this);
        }
        return _MainMenuWrmlOrgAction;
    }

    public MenuBarWindow getModelMenuBarWindow() {
        return _ModelMenuBarWindow;
    }

    public GuiAction getNewModelAction() {
        if (_NewModelAction == null) {
            _NewModelAction = new NewModelAction(this);
        }
        return _NewModelAction;
    }

    public GuiAction getNewModelConfirmationAction() {
        if (_NewModelConfirmationAction == null) {
            _NewModelConfirmationAction = new NewModelConfirmationAction(this);
        }

        return _NewModelConfirmationAction;
    }

    public GuiAction getOpenModelAction() {
        if (_OpenModelAction == null) {
            _OpenModelAction = new OpenModelAction(this);
        }
        return _OpenModelAction;
    }

    public GuiAction getOpenModelConfirmationAction() {
        if (_OpenModelConfirmationAction == null) {
            _OpenModelConfirmationAction = new OpenModelConfirmationAction(this);
        }

        return _OpenModelConfirmationAction;
    }

    public GuiAction getSaveAction() {
        if (_SaveAction == null) {
            _SaveAction = new SaveAction(this);
        }
        return _SaveAction;
    }

    public GUIScreen getScreen() {
        return _Screen;
    }

    public GuiAction getShowModelMenuAction() {
        if (_ShowModelMenuAction == null) {
            _ShowModelMenuAction = new ShowModelMenuAction(this);
        }
        return _ShowModelMenuAction;
    }

    public GuiAction getSortAction(String title) {
        return new SortAction(title, this);
    }

    public SplashWindow getSplashWindow() {
        return _SplashWindow;
    }

    public GuiAction getUnimplementedAction(final String title) {
        if (!_UnimplementedActions.containsKey(title)) {
            final UnimplementedAction action = new UnimplementedAction(title, this);
            _UnimplementedActions.put(title, action);
        }

        return _UnimplementedActions.get(title);
    }

    public GuiAction getWrmlOrgAction() {
        if (_WrmlOrgAction == null) {
            _WrmlOrgAction = new WrmlOrgAction(this);
        }
        return _WrmlOrgAction;
    }

    public void newModel() {
        showWindow(_NewModelDialog);
    }

    public void newModelWindow(URI schemaId) {

        // Get the engine's default context and schema loader
        final Context context = _Engine.getDefaultContext();

        final Dimensions dimensions = new Dimensions(context);
        dimensions.setRequestedSchemaId(schemaId);

        // Say hello to Buckaroo Banzai and store our fields in an ephemeral zone (aka client/local/temp/non-SOR storage). 
        dimensions.setEphemeralDimension(8);

        final Model model = context.create(dimensions);
        createModelWindow(model);
    }

    public void openModel() {
        showWindow(_OpenModelDialog);
    }

    public void openModelWindow(URI schemaId, Object key, UUID heapId) {

        // Get the engine's default context and schema loader
        final Context context = _Engine.getDefaultContext();

        final Dimensions dimensions = new Dimensions(context);
        dimensions.setRequestedSchemaId(schemaId);

        // Say hello to Buckaroo Banzai and store our fields in an ephemeral zone (aka client/local/temp/non-SOR storage). 
        dimensions.setEphemeralDimension(8);

        final Model model = context.get(key, dimensions);
        createModelWindow(model);
    }

    public void paste() {

    }

    public void saveModel() {

    }

    public void showMainMenuBarWindow() {
        _Screen.setTheme(getLightTheme());
        showWindow(getMainMenuBarWindow());
    }

    public DialogResult showMessageBox(final String title, final String message) {
        return MessageBox.showMessageBox(_Screen, title, message);
    }

    public void showWindow(final Window window) {
        showWindow(window, GUIScreen.Position.CENTER);
    }

    public void showWindow(final Window window, final GUIScreen.Position position) {
        _Screen.showWindow(window, position);
    }

    public void wrmlOrg() {

        if (!java.awt.Desktop.isDesktopSupported()) {
            showSplashWindow();
            return;
        }

        final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            showSplashWindow();
            return;
        }

        try {
            desktop.browse(_WrmlDotOrg);
        }
        catch (final IOException e) {
            System.err.println(e.getMessage());
            showSplashWindow();
            return;
        }
    }

    void newModelConfirmation() {
        final URI schemaId = _NewModelDialog.getSchemaId();
        newModelWindow(schemaId);
        final CheckBoxList schemaIdHistoryCheckBoxList = _NewModelDialog.getSchemaIdHistoryCheckBoxList();

        schemaIdHistoryCheckBoxList.addItem(schemaId);
        //_OpenModelDialog.getHistoryCheckBoxList().addItem(schemaId);
    }

    void openModelConfirmation() {
        openModelWindow(_OpenModelDialog.getSchemaId(), _OpenModelDialog.getKey(), _OpenModelDialog.getHeapId());
    }

    void showError(String errorMessage, Throwable t) {
        showMessageBox("  Error  ", errorMessage + " (" + t + ")");
    }

    void showSplashWindow() {
        _Screen.setTheme(getDarkTheme());
        showWindow(getSplashWindow());
    }

    void showUnimplementedMessage(String title) {
        showMessageBox("Not Implemented", "The action \"" + title + "\" has not been implmented.");
    }

    private class CancelAction extends ClosingGuiAction {

        public CancelAction(final Gui gui) {
            super("Cancel", gui);
        }

    }

    private class ExitAction extends ClosingGuiAction {

        public ExitAction(final Gui gui) {
            super("Exit", gui);
        }

    }

    private class MainMenuBar extends BasePanel {

        public MainMenuBar() {
            super("", new Border.Invisible(), Orientation.HORISONTAL, true, false);

            setBetweenComponentsPadding(0);

            final Menu modelMenu = new Menu("  Model  ");
            modelMenu.setPreferredSize(new TerminalSize(30, 8));
            addComponent(modelMenu, SizePolicy.CONSTANT);

            final ButtonPanel modelNewMenuItem = new ButtonPanel(getMainMenuNewModelAction());
            modelMenu.addComponent(modelNewMenuItem);
            final ButtonPanel modelOpenMenuItem = new ButtonPanel(getMainMenuOpenModelAction());
            modelMenu.addComponent(modelOpenMenuItem);

            final Menu helpMenu = new Menu("  Help  ");
            helpMenu.setPreferredSize(new TerminalSize(30, 8));
            addComponent(helpMenu);

            final ButtonPanel wrmlOrgMenuItem = new ButtonPanel(getMainMenuWrmlOrgAction());
            helpMenu.addComponent(wrmlOrgMenuItem, SizePolicy.CONSTANT);

        }
    }

    private class MainMenuNewModelAction extends GuiAction {

        public MainMenuNewModelAction(final Gui gui) {
            super("New...", gui);
        }

        @Override
        public void doAction() {
            newModel();
        }
    }

    private class MainMenuOpenModelAction extends GuiAction {

        public MainMenuOpenModelAction(final Gui gui) {
            super("Open...", gui);
        }

        @Override
        public void doAction() {
            openModel();
        }
    }

    private class MainMenuWrmlOrgAction extends GuiAction {

        public MainMenuWrmlOrgAction(final Gui gui) {
            super("WRML.org", gui);
        }

        @Override
        public void doAction() {
            wrmlOrg();
        }
    }

    private class ModelMenuBar extends BasePanel {

        public ModelMenuBar() {
            super("", new Border.Invisible(), Orientation.HORISONTAL, true, false);

            setBetweenComponentsPadding(0);

            final Menu modelMenu = new Menu("  Model  ");

            addComponent(modelMenu, SizePolicy.CONSTANT);

            final ButtonPanel modelNewMenuItem = new ButtonPanel(getNewModelAction());
            modelMenu.addComponent(modelNewMenuItem);
            final ButtonPanel modelOpenMenuItem = new ButtonPanel(getOpenModelAction());
            modelMenu.addComponent(modelOpenMenuItem);
            final ButtonPanel modelSaveMenuItem = new ButtonPanel(getUnimplementedAction("Save"));
            modelMenu.addComponent(modelSaveMenuItem);
            final ButtonPanel modelSaveAsMenuItem = new ButtonPanel(getUnimplementedAction("Save As..."));
            modelMenu.addComponent(modelSaveAsMenuItem);
            final ButtonPanel modelDeleteMenuItem = new ButtonPanel(getUnimplementedAction("Delete"));
            modelMenu.addComponent(modelDeleteMenuItem);

            final Menu editMenu = new Menu("  Selection  ");
            addComponent(editMenu, SizePolicy.CONSTANT);

            final ButtonPanel cutwMenuItem = new ButtonPanel(getUnimplementedAction("Cut"));
            editMenu.addComponent(cutwMenuItem);
            final ButtonPanel copyMenuItem = new ButtonPanel(getUnimplementedAction("Copy"));
            editMenu.addComponent(copyMenuItem);
            final ButtonPanel pasteMenuItem = new ButtonPanel(getUnimplementedAction("Paste"));
            editMenu.addComponent(pasteMenuItem);
            final ButtonPanel deleteMenuItem = new ButtonPanel(getUnimplementedAction("Delete"));
            editMenu.addComponent(deleteMenuItem);

            final Menu helpMenu = new Menu("  Help  ");
            addComponent(helpMenu);

            final ButtonPanel wrmlOrgMenuItem = new ButtonPanel(getWrmlOrgAction());
            helpMenu.addComponent(wrmlOrgMenuItem, SizePolicy.CONSTANT);

            addComponent(new EmptySpace(0, 4));
        }
    }

    private class NewModelAction extends ClosingGuiAction {

        public NewModelAction(final Gui gui) {
            super(gui, gui.getMainMenuNewModelAction());
        }

    }

    private class NewModelConfirmationAction extends ClosingGuiAction {

        public NewModelConfirmationAction(final Gui gui) {
            super("OK", gui);
        }

        @Override
        protected boolean doIt() {
            newModelConfirmation();
            return true;
        }
    }

    private class OpenModelAction extends ClosingGuiAction {

        public OpenModelAction(final Gui gui) {
            super(gui, gui.getMainMenuOpenModelAction());
        }

    }

    private class OpenModelConfirmationAction extends ClosingGuiAction {

        public OpenModelConfirmationAction(final Gui gui) {
            super("OK", gui);
        }

        @Override
        protected boolean doIt() {
            openModelConfirmation();
            return true;
        }
    }

    private class SaveAction extends GuiAction {

        public SaveAction(final Gui gui) {
            super("Save", gui);
        }

        @Override
        public void doAction() {
            saveModel();
        }

    }

    private class ShowModelMenuAction extends GuiAction {

        public ShowModelMenuAction(final Gui gui) {
            super("Menu...", gui);
        }

        @Override
        public void doAction() {
            showWindow(_ModelMenuBarWindow);
        }
    }

    private class SortAction extends GuiAction {

        public SortAction(String title, final Gui gui) {
            super(title, gui);
        }

        @Override
        public void doAction() {
            // TODO: Implement sorting
        }
    }

    private class UnimplementedAction extends GuiAction {

        public UnimplementedAction(final String title, final Gui gui) {
            super(title, gui);
        }

        @Override
        public void doAction() {
            showUnimplementedMessage(getTitle());
        }
    }

    private class WrmlOrgAction extends ClosingGuiAction {

        public WrmlOrgAction(final Gui gui) {
            super("WRML.org", gui, gui.getMainMenuWrmlOrgAction());
        }

    }

}

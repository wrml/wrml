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
package org.wrml.werminal;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.GUIScreen.Position;
import com.googlecode.lanterna.gui.Theme;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Filed;
import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Method;
import org.wrml.runtime.*;
import org.wrml.runtime.format.application.schema.json.JsonSchemaLoader;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.SystemApi;
import org.wrml.runtime.schema.*;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.util.PropertyUtil;
import org.wrml.util.UniqueName;
import org.wrml.werminal.action.*;
import org.wrml.werminal.component.*;
import org.wrml.werminal.dialog.*;
import org.wrml.werminal.model.EntryModel;
import org.wrml.werminal.model.SlotValueHistoryListModel;
import org.wrml.werminal.model.WerminalModel;
import org.wrml.werminal.terminal.TerminalApp;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppMenuWindow;
import org.wrml.werminal.terminal.TerminalType;
import org.wrml.werminal.theme.DefaultSplashTheme;
import org.wrml.werminal.theme.DefaultWindowTheme;
import org.wrml.werminal.theme.LightTheme;
import org.wrml.werminal.util.History;
import org.wrml.werminal.window.ModelWindow;
import org.wrml.werminal.window.SplashWindow;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

import static org.wrml.runtime.EngineConfiguration.WRML_CONFIGURATION_FILE_PATH_PROPERTY_NAME;


/**
 * <p>
 * <p/>
 * {@code Werminal} is a terminal (command line) application for WRML model browsing and editing. See
 * {@link OptionDescriptor} for a complete set of Wrml options.
 * </p>
 * see {@link #main(String[])}.
 * </p>
 *
 * @see <a href="http://code.google.com/p/lanterna/">Lanterna, easy console text GUI library for Java</a>
 * @see Engine
 * @see EngineConfiguration
 * @see org.wrml.runtime.service.file.FileSystemService
 * @see org.wrml.runtime.service.rest.RestService
 * @see Model
 * @see Keys
 * @see org.wrml.model.rest.Api
 * @see org.wrml.model.rest.Document
 * @see org.wrml.model.schema.Schema
 */
public class Werminal extends TerminalApp
{

    public static final Logger LOG = LoggerFactory.getLogger(Werminal.class);

    private static final String WERMINAL_MODEL_FILE_NAME = Werminal.class.getSimpleName() + ".json";

    private final WerminalModel _WerminalModel;

    private final Theme _DefaultSplashTheme;

    private final Theme _DefaultMenuTheme;

    private final Theme _LightTheme;

    private final SplashWindow _SplashWindow;

    private final TerminalAppMenuWindow _MainMenuWindow;

    private final NewModelDialog _NewModelDialog;

    private final OpenModelDialog _OpenModelDialog;

    private final LoadApiDialog _LoadApiDialog;

    private final PrintDialog _PrintDialog;

    private final TerminalAppMenuWindow _ModelMenuWindow;

    private final TerminalAppMenuWindow _ListMenuWindow;

    private final WerminalAction _NewAction;

    private final WerminalAction _OpenAction;

    private final WerminalAction _NewConfirmationAction;

    private final WerminalAction _OpenConfirmationAction;

    private final WerminalAction _SetOriginAction;

    private final WerminalAction _WrmlOrgAction;

    private final WerminalAction _HelpGuideAction;

    private final WerminalAction _ExitAction;

    private final WerminalAction _SaveAction;

    private final WerminalAction _PrintPreviewAction;

    private final WerminalAction _PrintAction;

    private final WerminalAction _PrintConfirmationAction;

    private final WerminalAction _LoadAction;

    private final WerminalAction _CancelAction;

    private final WerminalAction _DeleteAction;

    private final WerminalAction _ShowModelMenuAction;

    private final WerminalAction _ShowListMenuAction;

    private final CloseBeforeAction _CloseAction;

    private final CloseBeforeAction _ClosingWrmlOrgAction;

    private final Map<String, WerminalAction> _UnimplementedActions;

    private final History<URI> _SchemaUriHistory;

    private final SortedMap<URI, SortedMap<String, SortedSet<Object>>> _SlotValueHistories;

    private final SortedMap<URI, SlotValueHistoryListModel> _SlotValueHistoryModels;


    public Werminal(final WerminalModel werminalModel, final Context context, final TerminalType terminalType) throws Exception
    {

        super("Werminal", context, terminalType);

        _WerminalModel = werminalModel;

        final GUIScreen guiScreen = getGuiScreen();

        final List<URI> schemaUriHistoryList = _WerminalModel.getSchemaUriHistoryList();

        _SchemaUriHistory = new History<>(schemaUriHistoryList);

        _SlotValueHistories = new TreeMap<>();
        _SlotValueHistoryModels = new TreeMap<>();

        final List<SlotValueHistoryListModel> slotValueHistoryLists = _WerminalModel.getSlotValueHistoryLists();
        compileSlotValueHistoryLists(slotValueHistoryLists);

        _UnimplementedActions = new HashMap<String, WerminalAction>();
        _DefaultSplashTheme = new DefaultSplashTheme();
        _DefaultMenuTheme = new DefaultWindowTheme();
        _LightTheme = new LightTheme();

        _CancelAction = new CancelAction(this);
        _CloseAction = new CloseBeforeAction(this);
        _DeleteAction = new DeleteAction(this);
        _ExitAction = new ExitAction(this);
        _NewAction = new NewAction(this);
        _NewConfirmationAction = new NewConfirmationAction(this);
        _OpenAction = new OpenAction(this);
        _OpenConfirmationAction = new OpenConfirmationAction(this);
        _SetOriginAction = new SetOriginAction(this);

        _SaveAction = new SaveAction(this);

        _PrintPreviewAction = new PrintPreviewAction(this);
        _PrintAction = new PrintAction(this);
        _PrintConfirmationAction = new PrintConfirmationAction(this);

        _LoadAction = new LoadAction(this);

        _ShowModelMenuAction = new ShowModelMenuAction(this);
        _ShowListMenuAction = new ShowListMenuAction(this);
        _HelpGuideAction = new HelpGuideAction(this);
        _WrmlOrgAction = new WrmlOrgAction(this);

        _ClosingWrmlOrgAction = new CloseBeforeAction(this, _WrmlOrgAction);

        _SplashWindow = new SplashWindow(this);
        _MainMenuWindow = new TerminalAppMenuWindow(this, "WRML - Werminal", new MainMenu(this),
                new TerminalAppButtonPanel(_ExitAction));
        _NewModelDialog = new NewModelDialog(this, "New Model", _NewConfirmationAction, _CancelAction);
        _OpenModelDialog = new OpenModelDialog(this, "Open Model", _OpenConfirmationAction, _CancelAction);

        _ModelMenuWindow = new TerminalAppMenuWindow(this, "", new ModelMenu(this), new TerminalAppButtonPanel(
                _CloseAction));

        _ListMenuWindow = new TerminalAppMenuWindow(this, "", new ListMenu(this), new TerminalAppButtonPanel(
                _CloseAction));

        _PrintDialog = new PrintDialog(this, "Print Model");
        _LoadApiDialog = new LoadApiDialog(this, "Load REST API Metadata");

        final URI metaSchemaUri = getContext().getSchemaLoader().getSchemaSchemaUri();
        _NewModelDialog.setSchemaUri(metaSchemaUri);

        for (final URI historySchemaUri : schemaUriHistoryList)
        {
            addToSchemaUriHistory(historySchemaUri);
        }

        final JsonSchemaLoader jsonSchemaLoader = context.getSchemaLoader().getJsonSchemaLoader();
        final SortedSet<URI> loadedJsonSchemaUris = jsonSchemaLoader.getLoadedJsonSchemaUris();
        for (final URI loadedJsonSchemaUri : loadedJsonSchemaUris)
        {
            addToSchemaUriHistory(loadedJsonSchemaUri);
        }

        final Screen screen = guiScreen.getScreen();
        screen.startScreen();

        final Terminal terminal = screen.getTerminal();
        if (terminal instanceof SwingTerminal)
        {
            final JFrame frame = ((SwingTerminal) terminal).getJFrame();
            frame.setTitle(getAppTitle());

            // TODO: Do something fun with the JFrame. =)
        }

        showSplashWindow();

        String errorMessage = null;

        while (true)
        {
            try
            {
                showMainMenuBarWindow();
                break;
            }
            catch (final Exception e)
            {
                errorMessage = "An unexpected error has occurred.";
                showError(errorMessage, e);
                LOG.error(errorMessage + " (" + e.getMessage() + ")", e);
            }
        }

        screen.stopScreen();

        schemaUriHistoryList.clear();
        schemaUriHistoryList.addAll(_SchemaUriHistory.getElementSet());

        getContext().saveModel(_WerminalModel);
    }

    /**
     * Entry point to run the Werminal console app. To run Werminal, two parameters are required:
     * <ul>
     * <li><code>-{@link OptionDescriptor#unix unix}</code> or <code>-{@link OptionDescriptor#swing swing}</code></li>
     * <li><code>-{@link OptionDescriptor#config config} <i>path/to/wrml.json</i></code></li>
     * </ul>
     * <h3>Example</h3>
     * <code>org.wrmlx.app.werminal.Werminal -{@link OptionDescriptor#swing swing} -{@link OptionDescriptor#config config} <i>path/to/wrml.json</i></code>
     * <p/>
     * Alternatively, the config file path can be specified with a variable: "
     * <code>-D{@link org.wrml.runtime.EngineConfiguration#WRML_CONFIGURATION_FILE_PATH_PROPERTY_NAME wrmlConfiguration}=<i>path/to/wrml.json</i></code>
     * <p/>
     *
     * @param args - standard command line arguments; see {@link OptionDescriptor}.
     */
    public static void main(String[] args) throws Exception
    {

        final CommandLineParser parser = new GnuParser();
        final CommandLine commandLine = parser.parse(OptionDescriptor.OPTIONS, args);
        TerminalType terminalType = TerminalType.Swing;
        if (commandLine.hasOption(OptionDescriptor.unix.getName()))
        {
            terminalType = TerminalType.Unix;
        }

        // Check the system property
        String configurationFilePath = PropertyUtil.getSystemProperty(WRML_CONFIGURATION_FILE_PATH_PROPERTY_NAME);
        if (configurationFilePath == null)
        {
            configurationFilePath = commandLine.getOptionValue(OptionDescriptor.config.getName());
        }


        final EngineConfiguration engineConfiguration = EngineConfiguration.load(configurationFilePath);
        final Engine engine = new DefaultEngine();
        engine.init(engineConfiguration);

        final Context context = engine.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final WerminalModel werminalModel;
        final File appFile = FileUtils.getFile(WERMINAL_MODEL_FILE_NAME).getCanonicalFile();
        if (appFile.exists() && appFile.isFile() && appFile.canRead())
        {
            final Keys keys = new KeysBuilder(schemaLoader.getTypeUri(Filed.class), appFile).toKeys();
            final Dimensions dimensions = new DimensionsBuilder(schemaLoader.getTypeUri(WerminalModel.class))
                    .toDimensions();
            werminalModel = context.getModel(keys, dimensions);
        }
        else
        {
            werminalModel = context.newModel(WerminalModel.class);
        }
        werminalModel.setTitle(Werminal.class.getSimpleName());
        werminalModel.setFile(appFile);
        new Werminal(werminalModel, context, terminalType);
    }

    public boolean addSlotValueToHistory(final URI historyListSchemaUri, final String slotName, final Object slotValue)
    {

        final Context context = getContext();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
        final String stringValue = syntaxLoader.formatSyntaxValue(slotValue);

        final SlotValueHistoryListModel slotValueHistoryList;
        if (!_SlotValueHistoryModels.containsKey(historyListSchemaUri))
        {

            slotValueHistoryList = context.newModel(SlotValueHistoryListModel.class);
            slotValueHistoryList.setHistoryListSchemaUri(historyListSchemaUri);
            _SlotValueHistoryModels.put(historyListSchemaUri, slotValueHistoryList);

            final List<SlotValueHistoryListModel> slotValueHistoryLists = _WerminalModel.getSlotValueHistoryLists();
            slotValueHistoryLists.add(slotValueHistoryList);
        }
        else
        {
            slotValueHistoryList = _SlotValueHistoryModels.get(historyListSchemaUri);
        }

        final SortedSet<Object> slotValueHistory = getSlotValueHistory(historyListSchemaUri, slotName);
        if (!slotValueHistory.add(slotValue))
        {
            return false;
        }

        final List<EntryModel> slotValueEntries = slotValueHistoryList.getSlotValueEntries();

        final EntryModel entry = context.newModel(EntryModel.class);
        entry.setName(slotName);
        entry.setValue(stringValue);

        slotValueEntries.add(entry);

        return true;
    }

    public void addToSchemaUriHistory(final URI schemaUri)
    {

        if (schemaUri == null)
        {
            return;
        }

        final History<URI> schemaUriHistory = getSchemaUriHistory();
        schemaUriHistory.add(schemaUri);

        final NewModelDialog newModelDialog = getNewModelDialog();
        final HistoryCheckListBox newModelDialogSchemaUriHistoryCheckBoxList = newModelDialog
                .getSchemaUriHistoryCheckBoxList();

        final OpenModelDialog openModelDialog = getOpenModelDialog();
        final HistoryCheckListBox openModelDialogSchemaUriHistoryCheckBoxList = openModelDialog
                .getSchemaUriHistoryCheckBoxList();

        newModelDialogSchemaUriHistoryCheckBoxList.addItem(schemaUri);
        openModelDialogSchemaUriHistoryCheckBoxList.addItem(schemaUri);

        final SchemaLoader schemaLoader = getContext().getSchemaLoader();
        if (schemaLoader.isPrototyped(schemaUri))
        {
            final Prototype prototype;
            try
            {
                prototype = schemaLoader.getPrototype(schemaUri);
            }
            catch (final Exception e)
            {
                LOG.warn("Unable to add schema to history: " + schemaUri + ".  Reason: " + e.getMessage());
                return;
            }

            // Capture all related schema URIs in the history list

            final Set<URI> relatedSchemaUris = prototype.getAllRelatedSchemaUris();
            schemaUriHistory.addAll(relatedSchemaUris);

            newModelDialogSchemaUriHistoryCheckBoxList.addItems(relatedSchemaUris);
            openModelDialogSchemaUriHistoryCheckBoxList.addItems(relatedSchemaUris);

        }

    }

    public URI createSchemaUri(final String simpleSchemaName)
    {

        final String systemUserName = System.getProperty("user.name");

        final String appUserName = (systemUserName != null && !systemUserName.isEmpty()) ? systemUserName : "user "
                + getClass().getSimpleName() + " wrml";
        String userNamespace = StringUtils.replace(appUserName, " ", "/");
        userNamespace = StringUtils.reverseDelimited(userNamespace, '/');

        // TODO: Make the userNamespace an option or setting or preference (in the Werminal tool options dialog)

        userNamespace = userNamespace.toLowerCase();

        final UniqueName newSchemaUniqueName = new UniqueName("org/" + userNamespace, simpleSchemaName);
        final URI schemaUri = URI.create(SystemApi.Schema.getUri().toString() + "/" + newSchemaUniqueName.toString());

        return schemaUri;
    }

    public WerminalAction getCancelAction()
    {

        return _CancelAction;
    }

    public CloseBeforeAction getCloseAction()
    {

        return _CloseAction;
    }

    public CloseBeforeAction getClosingWrmlOrgAction()
    {

        return _ClosingWrmlOrgAction;
    }

    public Theme getDefaultMenuTheme()
    {

        return _DefaultMenuTheme;
    }

    public Theme getDefaultSplashTheme()
    {

        return _DefaultSplashTheme;
    }

    public WerminalAction getDeleteAction()
    {

        return _DeleteAction;
    }

    public WerminalAction getExitAction()
    {

        return _ExitAction;
    }

    public WerminalAction getHelpGuideAction()
    {

        return _HelpGuideAction;
    }

    public Theme getLightTheme()
    {

        return _LightTheme;
    }

    public TerminalAppMenuWindow getListMenuBarWindow()
    {

        return _ListMenuWindow;
    }

    public WerminalAction getLoadAction()
    {

        return _LoadAction;
    }

    public TerminalAppMenuWindow getMainMenuBarWindow()
    {

        return _MainMenuWindow;
    }

    public TerminalAppMenuWindow getModelMenuBarWindow()
    {

        return _ModelMenuWindow;
    }

    public WerminalAction getNewAction()
    {

        return _NewAction;
    }

    public WerminalAction getNewConfirmationAction()
    {

        return _NewConfirmationAction;
    }

    public NewModelDialog getNewModelDialog()
    {

        return _NewModelDialog;
    }

    public WerminalAction getOpenAction()
    {

        return _OpenAction;
    }

    public WerminalAction getOpenConfirmationAction()
    {

        return _OpenConfirmationAction;
    }

    public OpenModelDialog getOpenModelDialog()
    {

        return _OpenModelDialog;
    }

    public WerminalAction getPrintPreviewAction()
    {

        return _PrintPreviewAction;
    }

    public WerminalAction getSetOriginAction()
    {

        return _SetOriginAction;
    }

    public WerminalAction getPrintAction()
    {

        return _PrintAction;
    }

    public WerminalAction getPrintConfirmationAction()
    {

        return _PrintConfirmationAction;
    }

    public WerminalAction getSaveAction()
    {

        return _SaveAction;
    }

    public History<URI> getSchemaUriHistory()
    {

        return _SchemaUriHistory;
    }

    public WerminalAction getShowListMenuAction()
    {

        return _ShowListMenuAction;
    }

    public WerminalAction getShowModelMenuAction()
    {

        return _ShowModelMenuAction;
    }

    public SortedSet<Object> getSlotValueHistory(final URI schemaUri, final String slotName)
    {

        if (schemaUri == null || slotName == null)
        {
            return null;
        }

        if (!_SlotValueHistories.containsKey(schemaUri))
        {
            _SlotValueHistories.put(schemaUri, new TreeMap<String, SortedSet<Object>>());
        }

        final Map<String, SortedSet<Object>> schemaHistory = _SlotValueHistories.get(schemaUri);

        if (!schemaHistory.containsKey(slotName))
        {
            schemaHistory.put(slotName, new TreeSet<Object>());
        }

        return schemaHistory.get(slotName);
    }

    public SplashWindow getSplashWindow()
    {

        return _SplashWindow;
    }

    public String getTypeTitle(final Type type)
    {

        final Context context = getContext();
        final ValueType valueType = context.getSchemaLoader().getValueType(type);
        final String typeTitle;
        if (valueType == ValueType.Text && !String.class.equals(type))
        {
            typeTitle = valueType.name() + "/" + ((Class<?>) type).getSimpleName();
        }
        else
        {
            typeTitle = valueType.name();
        }

        return typeTitle;
    }

    public WerminalAction getUnimplementedAction(final String title)
    {

        if (!_UnimplementedActions.containsKey(title))
        {
            final UnimplementedAction action = new UnimplementedAction(this, title);
            _UnimplementedActions.put(title, action);
        }

        return _UnimplementedActions.get(title);
    }

    public Map<String, WerminalAction> getUnimplementedActions()
    {

        return _UnimplementedActions;
    }

    public WerminalAction getWrmlOrgAction()
    {

        return _WrmlOrgAction;
    }

    public String listToString(final List<?> listValue, final Type listType)
    {

        final int itemCount = listValue.size();
        final String itemString = (itemCount == 1) ? "1 element" : itemCount + " elements";
        return listTypeToString(listType) + " " + "[ " + itemString + " ]";

    }

    public String listTypeToString(final Type listType)
    {

        final Type listElementType = ValueType.getListElementType(listType);
        final Class<?> listElementClass = (Class<?>) listElementType;
        final String stringValue = "List<" + listElementClass.getCanonicalName() + ">";
        return stringValue;
    }

    public void newModelWindow(final URI schemaUri)
    {

        LOG.debug("NewModelWindow created with schemaUri [{}]", schemaUri);

        // Get the engine's default context and schema loader
        final Context context = getContext();

        final Dimensions dimensions = new DimensionsBuilder(schemaUri).toDimensions();

        final Model model = context.newModel(dimensions);

        openModelWindow(model);
    }

    public void openListDialog(final FormField listFormField)
    {

        final WerminalTextBox listFormFieldTextBox = listFormField.getFieldValueTextBox();
        final Type listType = listFormFieldTextBox.getHeapValueType();
        final List<?> listValue = (List<?>) listFormFieldTextBox.getValue();
        final String listValueString = listTypeToString(listType);

        final ListValueDialogConfirmationAction listValueDialogConfirmationAction = new ListValueDialogConfirmationAction(
                this, listFormFieldTextBox);

        final AddAction addAction = new AddAction(this);

        final ListValueDialog listValueDialog = new ListValueDialog(this, listFormField.getFieldName() + " - "
                + listValueString, new Component[]{new TerminalAppButtonPanel(addAction),
                new TerminalAppButtonPanel(_ShowListMenuAction)}, listValueDialogConfirmationAction, _CancelAction);

        listValueDialogConfirmationAction.setListValueDialog(listValueDialog);
        addAction.setListValueDialog(listValueDialog);

        listValueDialog.setList(listValue, ValueType.getListElementType(listType));

        showWindow(listValueDialog, Position.FULL_SCREEN);
    }

    @SuppressWarnings("unchecked")
    public <M extends Model> M openModel(final URI schemaUri, final Keys keys, final UUID heapId)
    {

        if (schemaUri == null || (keys == null && heapId == null))
        {
            return null;
        }

        // Get the engine's default context and schema loader
        final Context context = getContext();

        final Dimensions dimensions;
        final URI uri = context.getKeyValue(keys, Document.class);
        if (uri != null)
        {
            final ApiLoader apiLoader = context.getApiLoader();
            dimensions = apiLoader.buildDocumentDimensions(Method.Get, uri, new DimensionsBuilder(schemaUri));
        }
        else
        {
            dimensions = new DimensionsBuilder(schemaUri).toDimensions();
        }

        Model model = null;
        String errorMessage = null;
        try
        {
            if (heapId != null)
            {
                // model = context.newModel(dimensions);
                // TODO: support this
                showError("Opening by heapId is temporarily disabled.");
                model = null;
            }
            else
            {
                model = context.getModel(keys, dimensions);
            }
        }
        catch (final Exception e)
        {
            errorMessage = "An error occured while trying to open the model using schemaUri: " + schemaUri + ", Keys: "
                    + keys + "\nError: " + e.toString() + " - message: " + e.getMessage() + " - stack trace:\n"
                    + ExceptionUtils.getStackTrace(e);
        }

        if (model == null)
        {
            if (errorMessage == null)
            {
                errorMessage = "Model could not be opened: (schemaUri: " + schemaUri + ", id: " + keys + ", heapId: "
                        + heapId + ")";
            }

            showError(errorMessage);
            return null;
        }

        return (M) model;
    }

    public Window openModelWindow(final Model model)
    {

        final URI schemaUri = model.getSchemaUri();
        addKeySlotValuesToHistory(schemaUri, model.getKeys());

        final String title = ModelWindow.getModelWindowTitle(model);

        final ModelWindow modelWindow = new ModelWindow(this, title, new Component[]{
                new TerminalAppButtonPanel(_CloseAction),
                new TerminalAppButtonPanel(_PrintPreviewAction),
                new TerminalAppButtonPanel(_SetOriginAction),
                new TerminalAppButtonPanel(_SaveAction)
        });

        modelWindow.setModel(model);

        // showWindow(modelWindow, Position.OVERLAPPING);
        showWindow(modelWindow, Position.FULL_SCREEN);

        return modelWindow;
    }

    public Window openModelWindow(final URI schemaUri, final Keys keys, final UUID heapId)
    {

        if (keys == null || keys.getCount() == 0)
        {
            showError("\nPlease enter one (or more) key value(s).");
            return null;
        }

        final Model model = openModel(schemaUri, keys, heapId);
        if (model != null)
        {

            return openModelWindow(model);
        }

        return null;
    }

    public void showMainMenuBarWindow()
    {
        // TODO: Make themes pluggable in a different way
        final GUIScreen guiScreen = getGuiScreen();
        guiScreen.setTheme(getDefaultMenuTheme());

        showWindow(getMainMenuBarWindow());
    }

    public void showSplashWindow()
    {

        final Random easterEggDice = new Random();
        final int diceRoll = easterEggDice.nextInt(100) + 1;
        final Theme theme;
        switch (diceRoll)
        {
            case 77:
            {
                // Rolled double 7s on 2 d10!
                theme = getLightTheme();
                break;
            }

            default:
            {
                theme = getDefaultSplashTheme();
                break;
            }
        }

        final GUIScreen guiScreen = getGuiScreen();
        guiScreen.setTheme(theme);

        showWindow(getSplashWindow());
    }

    private void addKeySlotValuesToHistory(final URI historyListSchemaUri, final Keys keys)
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        if (keys == null || keys.getCount() == 0)
        {
            return;
        }

        for (final URI keyedSchemaUri : keys.getKeyedSchemaUris())
        {
            final Object keyValue = keys.getValue(keyedSchemaUri);

            final Prototype keyDeclaredPrototype = schemaLoader.getPrototype(keyedSchemaUri);
            if (keyDeclaredPrototype == null)
            {
                continue;
            }
            final SortedSet<String> keySlotNames = keyDeclaredPrototype.getDeclaredKeySlotNames();

            if (keySlotNames == null || keySlotNames.isEmpty())
            {
                continue;
            }

            if (keySlotNames.size() == 1)
            {
                final String keySlotName = keySlotNames.first();

                addSlotValueToHistory(historyListSchemaUri, keySlotName, keyValue);
            }
            else
            {
                final CompositeKey compositeKey = (CompositeKey) keyValue;
                if (compositeKey == null)
                {
                    continue;
                }

                final Map<String, Object> compositeKeySlots = compositeKey.getKeySlots();
                for (final String compositeKeySlotName : compositeKeySlots.keySet())
                {
                    final Object compositeKeySlotValue = compositeKeySlots.get(compositeKeySlotName);
                    if (compositeKeySlotValue != null)
                    {
                        addSlotValueToHistory(historyListSchemaUri, compositeKeySlotName, compositeKeySlotValue);
                    }
                }
            }
        }
    }

    private void compileSlotValueHistoryLists(final List<SlotValueHistoryListModel> slotValueHistoryLists)
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();

        for (final SlotValueHistoryListModel slotValueHistoryList : slotValueHistoryLists)
        {
            final URI historyListSchemaUri = slotValueHistoryList.getHistoryListSchemaUri();

            _SlotValueHistoryModels.put(historyListSchemaUri, slotValueHistoryList);

            final Prototype prototype;
            try
            {
                prototype = schemaLoader.getPrototype(historyListSchemaUri);
            }
            catch (PrototypeException e)
            {
                LOG.debug("Failed to load prototype for: " + historyListSchemaUri, e);
                continue;
            }

            if (prototype == null)
            {
                LOG.debug("Failed to load prototype for: " + historyListSchemaUri);
                continue;
            }

            final List<EntryModel> slotValueEntries = slotValueHistoryList.getSlotValueEntries();

            for (final EntryModel entry : slotValueEntries)
            {
                final String slotName = entry.getName();
                final String slotValueString = entry.getValue();

                final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
                final Object slotValue = syntaxLoader.parseSyntacticText(slotValueString, protoSlot.getHeapValueType());

                final SortedSet<Object> slotValueHistory = getSlotValueHistory(historyListSchemaUri, slotName);
                slotValueHistory.add(slotValue);
            }
        }

    }

    public LoadApiDialog getLoadApiDialog()
    {

        return _LoadApiDialog;
    }

    public PrintDialog getPrintDialog()
    {

        return _PrintDialog;
    }

}




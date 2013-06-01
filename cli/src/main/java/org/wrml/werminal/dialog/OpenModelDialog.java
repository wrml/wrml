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
package org.wrml.werminal.dialog;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.Component.Alignment;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel.Orientation;
import com.googlecode.lanterna.gui.layout.LinearLayout;
import com.googlecode.lanterna.gui.listener.ComponentAdapter;
import com.googlecode.lanterna.terminal.TerminalSize;
import org.wrml.model.rest.Document;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.CompositeKey;
import org.wrml.runtime.Context;
import org.wrml.runtime.Keys;
import org.wrml.runtime.KeysBuilder;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.SystemApi;
import org.wrml.runtime.schema.ProtoSlot;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.component.HistoryCheckListBox;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.terminal.*;
import org.wrml.werminal.window.WerminalWindow;

import java.net.URI;
import java.util.*;

public class OpenModelDialog extends WerminalWindow
{

    private static final String DEFAULT_KEYS_PANEL_LABEL = " Keys: ";

    private static final String ENTER_KEYS_BUTTON_LABEL = "Enter Keys...";

    private static final String SHOW_HISTORY_BUTTON_LABEL = "History...";

    WerminalTextBox _HeapIdTextBox;

    WerminalTextBox _SchemaUriTextBox;

    TerminalAppPanel _KeysPanel;

    WerminalAction _ConfirmAction;

    Set<String> _KeySlotNames;

    Map<URI, WerminalTextBox> _KeyInputs;

    HistoryCheckListBox _SchemaUriHistoryCheckBoxList;

    Map<URI, Map<String, WerminalTextBox>> _CompositeKeyInputs;

    URI _SchemaUri;


    /**
     * for testing -- no need to mock {@code werminal.context}
     */
    OpenModelDialog()
    {

        super(null, null);
    }

    public OpenModelDialog(final Werminal werminal, final String title, final WerminalAction confirmAction,
                           final WerminalAction dismissAction)
    {

        super(werminal, title);
        final URI schemaUri = werminal.getContext().getSchemaLoader().getApiSchemaUri();
        init(werminal, confirmAction, dismissAction, schemaUri);
    }

    /**
     * @param werminal
     * @param confirmAction
     * @param dismissAction
     */
    private void init(final Werminal werminal, final WerminalAction confirmAction, final WerminalAction dismissAction,
                      final URI schemaUri)
    {

        _ConfirmAction = confirmAction;

        _KeyInputs = new LinkedHashMap<>();
        _CompositeKeyInputs = new LinkedHashMap<>();
        _KeySlotNames = new LinkedHashSet<>();

        setBorder(new Border.Standard());

        // setBetweenComponentsPadding(0);

        addEmptySpace();

        //
        // Schema ID
        //

        final WerminalAction enterKeysAction = new WerminalAction(getWerminal(), ENTER_KEYS_BUTTON_LABEL)
        {

            @Override
            public void doAction()
            {

                _SchemaUri = getSchemaUri();
                updateKeysPanel(_SchemaUri, null);
            }
        };

        final TerminalAppButton enterKeysButton = new TerminalAppButton(enterKeysAction);

        _SchemaUriTextBox = new WerminalTextBox(werminal, 60, URI.class, enterKeysAction)
        {

            @Override
            protected void setText(final String stringValue, final boolean clearValue)
            {

                super.setText(stringValue, clearValue);
                enterKeysAction.doAction();
            }

        };

        final TerminalAppTextBoxPanel schemaUriTextBoxPanel = new TerminalAppTextBoxPanel(werminal, " Schema (URI): ",
                _SchemaUriTextBox);

        schemaUriTextBoxPanel.addComponent(enterKeysButton);

        _SchemaUriTextBox.addComponentListener(new ComponentAdapter()
        {

            @Override
            public void onComponentInvalidated(final Component component)
            {

                final boolean canEnterKeys;
                final String schemaUriText = _SchemaUriTextBox.getText();
                if (schemaUriText == null || schemaUriText.trim().isEmpty())
                {
                    canEnterKeys = false;
                }
                else
                {
                    final URI schemaUri = getSchemaUri();
                    canEnterKeys = schemaUri != null && schemaUri.equals(_SchemaUri);
                }

                // enterKeysButton.setVisible(canEnterKeys);

                if (!canEnterKeys)
                {
                    updateKeysPanel(null, null);
                }
            }
        });

        addComponent(schemaUriTextBoxPanel);

        addEmptySpace();

        final TerminalAppPanel historyPanel = new TerminalAppPanel(werminal, " History: ", new Border.Standard(),
                Orientation.VERTICAL);
        _SchemaUriHistoryCheckBoxList = new HistoryCheckListBox(_SchemaUriTextBox, null, confirmAction);
        _SchemaUriHistoryCheckBoxList.setPreferredSize(new TerminalSize(70, 10));
        historyPanel.addComponent(_SchemaUriHistoryCheckBoxList);
        addComponent(historyPanel);

        addEmptySpace();
        //
        // Keys
        //
        // addComponent(new Label("Keys: ", 70, Alignment.START), SizePolicy.CONSTANT);
        _KeysPanel = new TerminalAppPanel(werminal, DEFAULT_KEYS_PANEL_LABEL, new Border.Bevel(true),
                Orientation.VERTICAL);
        addComponent(_KeysPanel, LinearLayout.GROWS_VERTICALLY);

        // Heap ID

        _HeapIdTextBox = new WerminalTextBox(werminal, 60, UUID.class, confirmAction);

        addEmptySpace();

        //
        // Dialog buttons
        //
        final TerminalAppToolBar footerToolBar = new TerminalAppToolBar(werminal, new Component[]{
                new TerminalAppButtonPanel(confirmAction), new TerminalAppButtonPanel(dismissAction)});
        addComponent(footerToolBar);

        setSchemaUri(schemaUri);

        final Context context = werminal.getContext();
    }

    public UUID getHeapId()
    {

        return (UUID) _HeapIdTextBox.getValue();
    }

    public Keys getKeys()
    {

        final URI schemaUri = getSchemaUri();
        if (schemaUri == null)
        {
            return null;
        }

        final Context context = getApp().getContext();
        final ApiLoader apiLoader = context.getApiLoader();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final URI documentSchemaUri = schemaLoader.getDocumentSchemaUri();

        final KeysBuilder keysBuilder = new KeysBuilder();

        final Set<URI> keySchemaUris = _KeyInputs.keySet();
        for (final URI keySchemaUri : keySchemaUris)
        {

            final WerminalTextBox keyTextBox = _KeyInputs.get(keySchemaUri);

            final Object keyValue;
            try
            {
                keyValue = keyTextBox.getValue();
            }
            catch (final Exception e)
            {
                continue;
            }

            if (keyValue != null)
            {
                keysBuilder.addKey(keySchemaUri, keyValue);
            }
        }

        if (_KeyInputs.containsKey(documentSchemaUri))
        {
            // Build and add the document keys (if uri value != null)
            final WerminalTextBox uriTextBox = _KeyInputs.get(documentSchemaUri);

            if (uriTextBox != null)
            {
                final URI uri = uriTextBox.getValue();
                if (uri != null)
                {
                    Keys surrogateKeys = apiLoader.buildDocumentKeys(uri, schemaUri);
                    keysBuilder.addAll(surrogateKeys);
                }
            }
        }

        if (!_CompositeKeyInputs.isEmpty())
        {

            final Set<URI> compositeKeySchemaUris = _CompositeKeyInputs.keySet();

            outer:
            for (final URI compositeKeySchemaUri : compositeKeySchemaUris)
            {

                final SortedMap<String, Object> compositeKeySlots = new TreeMap<String, Object>();
                final Map<String, WerminalTextBox> compositeKeyTextBoxes = _CompositeKeyInputs
                        .get(compositeKeySchemaUri);
                for (final String compositeKeySlotName : compositeKeyTextBoxes.keySet())
                {

                    final WerminalTextBox compositeKeyTextBox = compositeKeyTextBoxes.get(compositeKeySlotName);

                    final Object keyComponentValue;
                    try
                    {
                        keyComponentValue = compositeKeyTextBox.getValue();
                    }
                    catch (final Exception e)
                    {
                        continue outer;
                    }

                    if (keyComponentValue == null)
                    {
                        continue outer;
                    }

                    compositeKeySlots.put(compositeKeySlotName, keyComponentValue);

                }

                if (!compositeKeySlots.isEmpty())
                {
                    keysBuilder.addKey(compositeKeySchemaUri, new CompositeKey(compositeKeySlots));
                }

            }
        }

        return keysBuilder.toKeys();
    }

    public void setKeys(final Keys keys)
    {

        updateKeysPanel(getSchemaUri(), keys);
    }

    public URI getSchemaUri()
    {

        try
        {
            return (URI) _SchemaUriTextBox.getValue();
        }
        catch (final Exception e)
        {
            return null;
        }
    }

    public HistoryCheckListBox getSchemaUriHistoryCheckBoxList()
    {

        return _SchemaUriHistoryCheckBoxList;
    }

    public URI setSchemaUri(final URI schemaUri)
    {

        _SchemaUri = schemaUri;
        final URI oldSchemaUri = (URI) _SchemaUriTextBox.setValue(schemaUri, false);
        updateKeysPanel(schemaUri, null);
        return oldSchemaUri;
    }

    private boolean addKeyInput(final URI schemaUri, final Prototype keyDeclaredPrototype, final Keys keys)
    {

        final Werminal werminal = getWerminal();
        final Context context = werminal.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final URI keyDeclaredSchemaUri = keyDeclaredPrototype.getSchemaUri();

        if (_KeyInputs.containsKey(keyDeclaredSchemaUri))
        {
            return false;
        }

        final SortedSet<String> keySlotNames = keyDeclaredPrototype.getDeclaredKeySlotNames();
        if (keySlotNames == null || keySlotNames.isEmpty())
        {
            // The schema declare's *zero* key slots
            return false;
        }

        if (keySlotNames.size() == 1)
        {
            // The schema declare's *only one* key slot

            final String keySlotName = keySlotNames.first();

            if (_KeySlotNames.contains(keySlotName))
            {
                return false;
            }

            final Class<?> keySlotType = (Class<?>) keyDeclaredPrototype.getKeyType();
            final TerminalAppTextBoxPanel keyTextBoxPanel = createKeyInput(schemaUri, keyDeclaredPrototype,
                    keySlotName, keySlotType, 60);
            final WerminalTextBox keyTextBox = (WerminalTextBox) keyTextBoxPanel.getTextBox();
            _KeysPanel.addComponent(keyTextBoxPanel);
            _KeysPanel.addEmptySpace();

            _KeyInputs.put(keyDeclaredSchemaUri, keyTextBox);
            _KeySlotNames.add(keySlotName);

            final URI documentSchemaUri = schemaLoader.getDocumentSchemaUri();
            final boolean isDocumentPrototype = keyDeclaredSchemaUri.equals(documentSchemaUri);
            if (isDocumentPrototype)
            {

                keyTextBox.addComponentListener(new ComponentAdapter()
                {

                    @Override
                    public void onComponentInvalidated(final Component component)
                    {

                        final URI uri;
                        try
                        {
                            uri = (URI) keyTextBox.getValue();
                            updateKeysPanelDocumentSurrogateKeyInputs(uri);
                        }
                        catch (final Exception ex)
                        {
                            return;
                        }

                    }

                });

            }

            return true;
        }
        else
        {

            // The schema declare's *more than one* key slot

            final SortedMap<String, WerminalTextBox> compositeKeyTextBoxes = new TreeMap<>();
            for (final String keySlotName : keySlotNames)
            {

                if (_KeySlotNames.contains(keySlotName))
                {
                    continue;
                }

                final ProtoSlot protoSlot = keyDeclaredPrototype.getProtoSlot(keySlotName);
                if (protoSlot == null)
                {
                    continue;
                }

                final Class<?> keyComponentType = (Class<?>) protoSlot.getHeapValueType();
                final TerminalAppTextBoxPanel keyTextBoxPanel = createKeyInput(schemaUri, keyDeclaredPrototype,
                        keySlotName, keyComponentType, 60);
                final WerminalTextBox keyTextBox = (WerminalTextBox) keyTextBoxPanel.getTextBox();
                compositeKeyTextBoxes.put(keySlotName, keyTextBox);

                _KeysPanel.addComponent(keyTextBoxPanel);
                _KeysPanel.addEmptySpace();

                _KeySlotNames.add(keySlotName);
            }

            if (compositeKeyTextBoxes.isEmpty())
            {
                return false;
            }

            _CompositeKeyInputs.put(keyDeclaredSchemaUri, compositeKeyTextBoxes);
            return true;
        }

    }

    private void addKeyMessageLabel()
    {

        addMessageLabel("Enter a Schema.id and *click* the \"" + ENTER_KEYS_BUTTON_LABEL + "\" button.",
                Alignment.LEFT_CENTER);

    }

    private void addMessageLabel(final String message, final Alignment alignment)
    {

        final Label label = new Label(message);
        label.setAlignment(alignment);
        _KeysPanel.addEmptySpace();
        _KeysPanel.addComponent(label, LinearLayout.MAXIMIZES_HORIZONTALLY);
        _KeysPanel.addEmptySpace();
    }

    private TerminalAppTextBoxPanel createKeyInput(final URI schemaUri, final Prototype keyDeclaredPrototype,
                                                   final String keyInputName, final Class<?> keyInputType, final int keyInputWidth)
    {

        final Werminal werminal = getWerminal();
        final WerminalTextBox keyTextBox = new WerminalTextBox(getWerminal(), keyInputWidth, keyInputType,
                _ConfirmAction);

        final Class<?> schemaInterface = keyDeclaredPrototype.getSchemaBean().getIntrospectedClass();
        final String schemaTitle = schemaInterface.getSimpleName();
        final String keyInputTypeTitle = keyInputType.getSimpleName();
        final String panelTitle = " " + schemaTitle + "." + keyInputName + " (" + keyInputTypeTitle + "): ";
        final TerminalAppTextBoxPanel keyTextBoxPanel = new TerminalAppTextBoxPanel(werminal, panelTitle, keyTextBox);

        final WerminalAction showHistoryAction = new WerminalAction(getWerminal(), SHOW_HISTORY_BUTTON_LABEL)
        {

            @Override
            public void doAction()
            {

                final Prototype prototype = werminal.getContext().getSchemaLoader().getPrototype(schemaUri);
                final SortedSet<Object> keyHistory = werminal.getSlotValueHistory(schemaUri, keyInputName);
                if (keyHistory != null && !keyHistory.isEmpty())
                {
                    final String popupTitle = "Key History - " + prototype.getTitle() + " - " + panelTitle;
                    final HistoryPopup historyPopup = new HistoryPopup(werminal, popupTitle, keyTextBox);

                    final HistoryCheckListBox keyHistoryCheckListBox = historyPopup.getHistoryCheckListBox();

                    keyHistoryCheckListBox.addItems(keyHistory);
                    werminal.showWindow(historyPopup);
                }
                else
                {
                    werminal.showMessageBox("Empty Key History",
                            "\nUnfortunately, there are no saved history values associated with the \"" + schemaTitle
                                    + "." + keyInputName + "\" key slot.");
                }

                setFocus(keyTextBox);
            }

        };

        final TerminalAppButton showHistoryButton = new TerminalAppButton(showHistoryAction);
        keyTextBoxPanel.addComponent(showHistoryButton);
        return keyTextBoxPanel;
    }

    private void updateKeysPanel(final URI schemaUri, final Keys keys)
    {

        _KeysPanel.removeAllComponents();
        _KeyInputs.clear();
        _KeySlotNames.clear();

        if (schemaUri == null)
        {
            _KeysPanel.setTitle(DEFAULT_KEYS_PANEL_LABEL);
            addKeyMessageLabel();

            return;
        }

        final Context context = getWerminal().getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final Prototype prototype;
        final Class<?> schemaInterface;
        try
        {
            prototype = schemaLoader.getPrototype(schemaUri);
            schemaInterface = schemaLoader.getSchemaInterface(schemaUri);
        }
        catch (final Exception e)
        {

            addKeyMessageLabel();

            return;
        }

        final String title = String.format(" %1s%2s", prototype.getSchemaBean().getIntrospectedClass().getSimpleName(),
                DEFAULT_KEYS_PANEL_LABEL);
        _KeysPanel.setTitle(title);
        _KeysPanel.addEmptySpace();

        URI uri = null;

        // Put Document's key first
        if (Document.class.isAssignableFrom(schemaInterface))
        {
            final URI documentSchemaUri = schemaLoader.getDocumentSchemaUri();
            final Prototype documentPrototype = schemaLoader.getPrototype(documentSchemaUri);
            addKeyInput(schemaUri, documentPrototype, keys);

            if (Schema.class.equals(schemaInterface))
            {
                final WerminalTextBox textBox = _KeyInputs.get(documentSchemaUri);
                textBox.setText(SystemApi.Schema.getUri().toString() + "/com/example");
                uri = (URI) textBox.getValue();

            }
        }

        // Add all of the other key inputs; skipping Document's since we already made it first.
        if (!Document.class.equals(schemaInterface))
        {

            addKeyInput(schemaUri, prototype, keys);

            final Set<Prototype> basePrototypes = prototype.getDeclaredBasePrototypes();
            for (final Prototype basePrototype : basePrototypes)
            {
                addKeyInput(schemaUri, basePrototype, keys);
            }

        }

        /*
         * TODO: Need to support opening by heap id?
         * 
         * if (!_KeyInputs.isEmpty()) { // Added some keys.
         * 
         * // or... heap id addMessageLabel("or", Alignment.CENTER); }
         * 
         * // Add the heap id for UUID-based look-ups (for...debugging?) final TerminalAppTextBoxPanel
         * heapIdTextBoxPanel = new TerminalAppTextBoxPanel(werminal, " Model.heapId [UUID]: ", _HeapIdTextBox);
         * _KeysPanel.addComponent(heapIdTextBoxPanel);
         */

        if (uri != null)
        {
            updateKeysPanelDocumentSurrogateKeyInputs(uri);
        }
    }

    private void updateKeysPanelDocumentSurrogateKeyInputs(final URI uri)
    {

        if (uri == null)
        {
            return;
        }

        final Context context = getWerminal().getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final ApiLoader apiLoader = context.getApiLoader();

        final URI documentSchemaUri = schemaLoader.getDocumentSchemaUri();
        final Keys allDocumentKeys = apiLoader.buildDocumentKeys(uri, getSchemaUri());
        if (allDocumentKeys != null && allDocumentKeys.getCount() > 1)
        {
            final Set<URI> keyedSchemaUris = allDocumentKeys.getKeyedSchemaUris();
            for (final URI keyedSchemaUri : keyedSchemaUris)
            {
                if (!keyedSchemaUri.equals(documentSchemaUri))
                {
                    if (_KeyInputs.containsKey(keyedSchemaUri))
                    {
                        final WerminalTextBox surrogateKeyTextBox = _KeyInputs.get(keyedSchemaUri);
                        final Object surrogateKeyValue = allDocumentKeys.getValue(keyedSchemaUri);
                        surrogateKeyTextBox.setValue(surrogateKeyValue);
                    }
                }
            }
        }

    }

}

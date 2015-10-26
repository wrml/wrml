#                                                                             
#  WRML - Web Resource Modeling Language                                      
#   __     __   ______   __    __   __                                        
#  /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \                                       
#  \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____                                  
#   \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\                                 
#    \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/                                 
#                                                                             
# http://www.wrml.org                                                         
#                                                                             
# Copyright 2011 - 2013 Mark Masse (OSS project WRML.org)                     
#                                                                             
# Licensed under the Apache License, Version 2.0 (the "License");             
# you may not use this file except in compliance with the License.            
# You may obtain a copy of the License at                                     
#                                                                             
# http://www.apache.org/licenses/LICENSE-2.0                                  
#                                                                             
# Unless required by applicable law or agreed to in writing, software         
# distributed under the License is distributed on an "AS IS" BASIS,           
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
# See the License for the specific language governing permissions and         
# limitations under the License.                                              
#             

# CoffeeScript


@Wrmldoc.module "HeaderApp.Show", (Show, App, Backbone, Marionette, $, _) ->
  class Show.Header extends App.Views.ItemView

    self = @

    template: "header/show/header"

    events:
      'click #newDocumentMenuItem' : 'showNewDocumentDialog'
      'click #openDocumentMenuItem' : 'showOpenDocumentDialog'
      'click #documentDialogActionButton' : 'handleDocumentDialogAction'
      'keypress #documentDialogUriInput' : 'handleDocumentDialogUriInputKeypress'

    onRender: ->
      self = @
      App.headerView = self

    showNewDocumentDialog: ->
      console.log("New Document")

      jstreeDataRoot = @model.attributes["jstree"]
      jstreeData = jstreeDataRoot.openDocumentDialog

      self.showDocumentDialog({
        action: "new",
        title: "New Document",
        actionButtonLabel: "New",
        bottomMessageHtml: "Note that the new Document <strong>WILL NOT</strong> be saved automatically."
      }, jstreeData)

    showOpenDocumentDialog: ->
      console.log("Open Document")

      jstreeDataRoot = @model.attributes["jstree"]
      jstreeData = jstreeDataRoot.openDocumentDialog

      self.showDocumentDialog({
        action: "open",
        title: "Open Document",
        actionButtonLabel: "Open",
        bottomMessage: ""
      }, jstreeData)

    showDocumentDialog: (dialogData, jstreeData) ->
      documentDialog = $("#documentDialog")

      for key, value of dialogData
        documentDialog.data(key, value)

      jstreeDiv = $("#documentDialogJstree")

      if $.isFunction(jstreeDiv.jstree)
        jstreeDiv.jstree("destroy")

      jstreeDiv.unbind()
      jstreeDiv.empty()

      jstreeDiv.bind("changed.jstree", self.handleTreeSelection)
      jstreeDiv.jstree(jstreeData)

      documentDialogTitleLabel = $("#documentDialogTitleLabel")
      documentDialogTitleLabel.text(dialogData.title)

      documentDialogActionButton = $("#documentDialogActionButton")
      documentDialogActionButton.text(dialogData.actionButtonLabel)

      bottomMessagePanel = $("#documentDialogBottomMessagePanel")
      bottomMessagePanel.hide()
      bottomMessageHtml = dialogData.bottomMessageHtml
      if bottomMessageHtml?.length
        bottomMessageDiv = $("#documentDialogBottomMessage")
        bottomMessageDiv.html(bottomMessageHtml)
        bottomMessagePanel.show()

      documentDialog.modal("show")


    handleDocumentDialogAction: (e) ->
      uri = $('#documentDialogUriInput').val()
      App.openDocument(uri)
      $('#documentDialog').modal('hide')

    handleDocumentDialogUriInputKeypress: (e) ->
      if e.which is 13
        $("#documentDialogActionButton").click();

    handleTreeSelection: (e, data) ->

      jstree = $('#documentDialogJstree').jstree(true)

      apiInput = $("#documentDialogApiInput")
      resourceInput = $("#documentDialogResourceInput")
      schemaInput = $("#documentDialogSchemaInput")
      uriInput = $("#documentDialogUriInput")
      keysPanel = $("#documentDialogKeysPanel")

      selectedNode = data.node
      selectedNodeData = selectedNode.data
      selectedNodeType = selectedNodeData.type

      keysPanel.find("input[id^=key-]").unbind();

      keysPanel.empty()
      messageElement = $("<div class='alert'>Select a Schema to input Document key values.</div>")
      keysPanel.append(messageElement);

      uri = ""

      if (selectedNodeType is "api")
        apiData = selectedNodeData.api
        apiInput.val(apiData.title)
        uri = apiData.uri
        resourceInput.val("")
        schemaInput.val("")

      else if (selectedNodeType is "resource")
        resourceData = selectedNodeData.resource
        resourceInput.val(resourceData.title)

        apiNode = self.getParentNode(jstree, selectedNode.parent, "api")
        apiData = apiNode.data.api
        apiInput.val(apiData.title)

        uri = apiData.uri + resourceData.path

        schemaInput.val("")

      else if (selectedNodeType is "schema")
        schemaData = selectedNodeData.schema
        schemaInput.val(schemaData.title)

        resourceNode = self.getParentNode(jstree, selectedNode.parent, "resource")
        resourceData = resourceNode.data.resource
        resourceInput.val(resourceData.title)

        apiNode = self.getParentNode(jstree, resourceNode.parent, "api")
        apiData = apiNode.data.api
        apiInput.val(apiData.title)

        uri = apiData.uri + resourceData.path

        keys = schemaData.keys

        #keysPanel.toggleClass("wrml-divided-panel", not $.isEmptyObject(keys))

        if not $.isEmptyObject(keys)
          keysPanel.empty()

        for key in keys

          keyInputId = "key-" + key.name

          keyLabel = $("<label class='wrml-modal-form-field-label' for='" + keyInputId + "'><img class='wrml-form-field-label-icon' src='" + self.model.attributes["docroot"] + "img/type/" + key.type + ".png' /> " + key.name + "</label>")
          keysPanel.append(keyLabel);

          # TODO: Use different form input based on key type
          keyInput = $("<input class='wrml-modal-input' type='text' id='" + keyInputId + "' value=''>")
          keysPanel.append(keyInput)
          keyInput.data("name", key.name)
          keyInput.on("keyup", self.handleKeyInputKeyup)

      uriInput.data("template", uri)
      self.setUriValue(uri)

    handleKeyInputKeyup: (e) ->
      if e.which is 13
        $("#documentDialogActionButton").click();

      keysPanel = $("#documentDialogKeysPanel")
      keyInputs = keysPanel.find("input[id^=key-]")
      uriInput = $("#documentDialogUriInput")
      uriTemplate = uriInput.data("template")
      uri = uriTemplate

      keyInputs.each ->
        keyInput = $(this)
        keyValue = keyInput.val();
        keyName = keyInput.data("name")
        uriTemplateParameter = "{" + keyName + "}"
        uri = uri.replace(uriTemplateParameter, keyValue)

      self.setUriValue(uri)

    getParentNode: (jstree, nodeId, type) ->
      node = jstree.get_node(nodeId)
      nodeData = node.data
      nodeType = nodeData.type
      if (nodeType is type)
        return node
      else
        return @getParentNode(jstree, node.parent, type)

    setUriValue: (uri) ->
      uri = App.rewriteUri(uri)
      uriInput = $("#documentDialogUriInput")
      uriInput.val(uri)

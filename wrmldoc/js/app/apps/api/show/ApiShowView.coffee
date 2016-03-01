#                                                                             
#  WRML - Web Resource Schemaing Language                                      
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

@Wrmldoc.module "ApiApp.Show", (Show, App, Backbone, Marionette, $, _) ->
  class Show.Api extends App.Views.Layout

    self = @

    template: "api/show/api_show"

    regions:
      resourcesSection: "#apiResourcesSection"
      apiBrowser: "#apiBrowser"

    events:
      'click .wrml-document-opener' : 'handleDocumentOpen'
      'click .wrml-document-new-dialog' : 'showNewDocumentDialog'
      'click .wrml-document-open-dialog' : 'showOpenDocumentDialog'
      'keyup .wrml-model-property-input' : 'handleModelPropertyInputKeyup'
      'click #mainToolbarSaveButton' : 'handleSave'
      'click #mainToolbarLoadButton' : 'handleLoad'
      'click #mainToolbarSwaggerButton' : 'handleSwagger'
      'click .api-resource-add-child-resource-menu-item' : 'handleAddChildResource'
      'click .api-resource-edit-resource-menu-item' : 'handleEditResource'
      'click .api-resource-remove-resource-menu-item' : 'handleRemoveResource'
      'click #resourceDialogActionButton' : 'handleResourceDialogAction'
      'click #resourceDialogDefaultSchemaSelectButton' : 'handleResourceDialogSchemaSelect'
      'click #resourceDialogDefaultSchemaSelectMenuItem' : 'handleResourceDialogSchemaSelect'
      'keypress #resourceDialogPathSegmentInput' : 'handleResourceDialogPathSegmentInputKeypress'


    onRender: ->
      console.log("Show.Api::onRender")

      self = @

      console.log(@model)

      @viewDocument = $.extend(true, {}, @model.attributes.model)
      @viewApi = $.extend(true, {}, @model.attributes.api)
      resourceArray = @viewApi.allResources
      console.log(resourceArray)
      @resourceCollection = new App.Entities.Collection(resourceArray)
      console.log(@resourceCollection)

      @resourcesSection.show(new ResourceCollectionView({
          collection: @resourceCollection
          wrmldocDocroot: @model.attributes.docroot
        }
      ))

    getViewDocument: ->
      return @viewDocument

    handleDocumentOpen: (e) ->
      App.handleDocumentOpen(e)

    showNewDocumentDialog: ->
      App.headerView.showNewDocumentDialog()

    showOpenDocumentDialog: ->
      App.headerView.showOpenDocumentDialog()


    handleModelPropertyInputKeyup: (e) ->
      console.log("Show.Api::handleModelPropertyInputKeyup")
      console.log(e)
      propertyInput = $(e.currentTarget)
      propertyInputData = propertyInput.data();
      propertyName = propertyInputData.wrmlModelPropertyName
      console.log(propertyName)

      propertyValue = propertyInput.val()
      @viewDocument[propertyName] = propertyValue

    handleSave: (e) ->
      console.log("handleSave")
      console.log(e)
      App.saveDocument()

    handleLoad: (e) ->
      console.log("handleLoad")
      console.log(e)

      apiUri = @viewDocument.uri
      apiLoadUri = apiUri + "/_wrml/api/load"
      apiLoadUri = App.rewriteUri(apiLoadUri)

      apiUri = App.rewriteUri(apiUri)

      $.ajax({
        type: "POST",
        url: apiLoadUri,
        dataType: "json",
        success: (data, textStatus, jqXHR) ->
          console.log(textStatus)
          console.log(data)
          console.log(jqXHR.responseText)
          App.openDocument(apiUri)

        error: (jqXHR, textStatus, errorThrown) ->
          console.error(textStatus)
          console.error(errorThrown)
          console.error(jqXHR.responseText)
      })


    handleSwagger: (e) ->
      console.log("handleSwagger")
      console.log(e)

      apiUri = @viewDocument.uri

      swaggerJsonUri = apiUri + "/_wrml/api/swagger"
      swaggerJsonUri = App.rewriteUri(swaggerJsonUri)

      swaggerUri = apiUri + "/_wrml/wrmldoc/swagger"
      queryParams = {}
      queryParams.url = swaggerJsonUri
      swaggerUri = App.rewriteUri(swaggerUri, queryParams)

      App.openDocument(swaggerUri, "_blank")


    handleAddChildResource: (e) ->
      console.log("handleAddChildResource")
      #console.log(e)
      #console.log(@resourceCollection)

      pathSegment = ""

      console.log(e.target)

      eventTarget = $(e.target)
      console.log(eventTarget)

      parentResourceId = eventTarget.data("resource-id")
      console.log("parentResourceId: " + parentResourceId)

      parentResourceElement = $('.api-resource[data-resource-id="' + parentResourceId + '"]');
      console.log(parentResourceElement)

      parentPath = parentResourceElement.data("full-path")
      console.log("parentPath: " + parentPath)

      resourceId = App.generateUUID()
      console.log("resourceId: " + resourceId)

      resource = {
        fullPath: parentPath + pathSegment
        id: resourceId
        parentPath: parentPath
        pathSegment: pathSegment
      }

      self.showResourceDialog({
        action: "new"
        title: "New Resource"
        actionButtonLabel: "New"
        resource: resource
      })


    showResourceDialog: (dialogData) ->
      resourceDialog = $("#resourceDialog")

      for key, value of dialogData
        resourceDialog.data(key, value)

      resourceDialogTitleLabel = $("#resourceDialogTitleLabel")
      resourceDialogTitleLabel.text(dialogData.title)

      resourceDialogActionButton = $("#resourceDialogActionButton")
      resourceDialogActionButton.text(dialogData.actionButtonLabel)

      parentPathLabel = $("#resourceDialogParentPathLabel")

      parentPath = dialogData.resource.parentPath
      if (parentPath.startsWith('/'))
        parentPath = parentPath.substring(1)

      if (not not parentPath)
        parentPath = parentPath + '/'

      parentPathLabel.text(parentPath)

      pathSegmentInput = $("#resourceDialogPathSegmentInput")
      pathSegmentInput.val(dialogData.resource.pathSegment)

      resourceDialog.on('shown', ->
        pathSegmentInput.focus()
      )

      resourceDialog.modal("show")

    handleResourceDialogAction: (e) ->
      resourceDialog = $("#resourceDialog")
      resource = resourceDialog.data("resource")
      console.log(resource)
      pathSegmentInput = $("#resourceDialogPathSegmentInput")
      console.log(pathSegmentInput)
      resource.pathSegment = pathSegmentInput.val()
      parentPath = resource.parentPath
      if (!parentPath.endsWith('/'))
        parentPath = parentPath + '/'

      resource.fullPath = parentPath + resource.pathSegment

      resourceDialog.modal('hide')

      resourceModel = new App.Entities.Model(resource)
      console.log(resourceModel)

      # TODO: Insert the model in sort order
      @resourceCollection.add(resourceModel)
      console.log(@resourceCollection)

      resourceElement = $('.api-resource[data-resource-id="' + resource.id + '"]');
      console.log(resourceElement)

      if (resourceElement)
        App.scrollToView(resourceElement)
        App.flashElement(resourceElement)

    handleResourceDialogPathSegmentInputKeypress: (e) ->
      if e.which is 13
        $("#resourceDialogActionButton").click();

    handleResourceDialogSchemaSelect: (e) ->
      resourceDialog = $("#resourceDialog")
      resource = resourceDialog.data("resource")
      console.log("handleResourceDialogSchemaSelect")

      @apiBrowser.show(new App.ApiBrowserApp.Show.ApiBrowser({
          #collection: @resourceCollection
          #wrmldocDocroot: @model.attributes.docroot
        }
      ))

    handleEditResource: (e) ->
      console.log("handleEditResource")

    handleRemoveResource: (e) ->
      console.log("handleRemoveResource")

    #
    # View for a single Resource
    #
    class ResourceView extends App.Views.ItemView

      template: "api/show/resource_show"

      initialize: (options) ->
        @wrmldocDocroot = options.wrmldocDocroot
        #console.log("ResourceView::initialize")
        #console.log(options)
        #console.log(@wrmldocDocroot)

      onRender: ->
        #console.log("Rendering ResourceView")
        #console.log(@)
        #console.log(@model)
        #console.log(@wrmldocDocroot)

      templateHelpers: ->
        return {
          wrmldocDocroot: @wrmldocDocroot
        }


    #
    # View for a collection of Resources
    #
    class ResourceCollectionView extends App.Views.CollectionView

      itemView: ResourceView

      itemViewOptions: (model, index) ->
        return {
          wrmldocDocroot: @wrmldocDocroot
        }

      initialize: (options) ->
        @wrmldocDocroot = options.wrmldocDocroot
        #console.log("ResourceCollectionView::initialize")
        #console.log(options)
        #console.log(@wrmldocDocroot)

      onRender: ->
        #console.log("Rendering ResourceCollectionView")
        #console.log(@collection)

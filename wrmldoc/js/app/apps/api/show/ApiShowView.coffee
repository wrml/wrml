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
  class Show.Api extends App.Views.ItemView
    template: "api/show/api_show"

    events:
      'keyup .wrml-model-property-input' : 'handleModelPropertyInputKeyup'
      'click #main-toolbar-save-button' : 'handleMainToolbarSave'
      'click #main-toolbar-load-button' : 'handleMainToolbarLoad'
      'click #main-toolbar-swagger-button' : 'handleMainToolbarSwagger'

    onRender: ->
      @self = @
      @viewDocument = $.extend(true, {}, @model.attributes.model)

    getViewDocument: ->
      return @viewDocument

    handleModelPropertyInputKeyup: (e) ->
      console.log("handleModelPropertyInputKeyup")
      console.log(e)
      propertyInput = $(e.currentTarget)
      propertyInputData = propertyInput.data();
      propertyName = propertyInputData.wrmlModelPropertyName
      console.log(propertyName)

      propertyValue = propertyInput.val()
      @viewDocument[propertyName] = propertyValue

    handleMainToolbarSave: (e) ->
      console.log("handleMainToolbarSave")
      console.log(e)
      App.saveDocument()

    handleMainToolbarLoad: (e) ->
      console.log("handleMainToolbarLoad")
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


    handleMainToolbarSwagger: (e) ->
      console.log("handleMainToolbarSwagger")
      console.log(e)

      apiUri = @viewDocument.uri

      #http://192.168.99.100:8888/_wrml/wrmldoc/swagger/?host=goonies01.api.wrml.org&url=http://192.168.99.100:8888/_wrml/api/swagger/?host=goonies01.api.wrml.org

      swaggerJsonUri = apiUri + "/_wrml/api/swagger"
      swaggerJsonUri = App.rewriteUri(swaggerJsonUri)

      swaggerUri = apiUri + "/_wrml/wrmldoc/swagger"
      queryParams = {}
      queryParams.url = swaggerJsonUri
      swaggerUri = App.rewriteUri(swaggerUri, queryParams)

      App.openDocument(swaggerUri, "_blank")

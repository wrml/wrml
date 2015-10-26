###                                                                             
  WRML - Web Resource Modeling Language                                      
   __     __   ______   __    __   __                                        
  /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \                                       
  \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____                                  
   \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\                                 
    \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/                                 
                                                                             
 http://www.wrml.org                                                         
                                                                             
 Copyright 2011 - 2013 Mark Masse (OSS project WRML.org)                     
                                                                             
 Licensed under the Apache License, Version 2.0 (the "License");             
 you may not use this file except in compliance with the License.            
 You may obtain a copy of the License at                                     
                                                                             
 http://www.apache.org/licenses/LICENSE-2.0                                  
                                                                             
 Unless required by applicable law or agreed to in writing, software         
 distributed under the License is distributed on an "AS IS" BASIS,           
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 See the License for the specific language governing permissions and         
 limitations under the License.                                              
###

# CoffeeScript

@Wrmldoc = do (Backbone, Marionette) ->
  App = new Marionette.Application

  #App.rootRoute = "/model"

  # App Layout
  App.addRegions
    headerRegion: "#header-region"
    mainRegion: "#main-region"
    footerRegion: "#footer-region"


  # App Init
  App.addInitializer ->
    App.module("HeaderApp").start(App.dataModel)

    schemaUri = App.dataModel.get "schemaUri"

    module = App.getModuleForSchema(schemaUri)
    module.start(App.dataModel)

    App.module("FooterApp").start(App.dataModel)


  #
  # Event Handlers (on)
  #

  App.on "initialize:before", (wrmlData) ->
    App.dataModel = App.createDataModel(wrmlData)

  App.on "initialize:after", ->
    @startHistory()
    @navigate(@rootRoute, trigger: true) unless @getCurrentRoute()

  App.getEmbeddedDataModel = ->
    App.dataModel

  App.openDocument = (uri) ->

    $("<a>").attr("href", uri).attr("target", "_blank")[0].click();

    #settings = {
    #  headers: {
    #    Accept: "application/vnd.wrml.wrmldoc+json"
    #  }
    #  dataType: "json"
    #  success: (wrmlData, textStatus) ->
    #    console.log(wrmlData);
    #    dataModel = App.createDataModel(wrmlData)
    #    schemaUri = dataModel.attributes["schemaUri"]
    #    console.log("Response Schema: " + schemaUri)
    #    module = App.getModuleForSchema(schemaUri)
    #    console.log(module)
    #    new module.Show.Controller(dataModel)
    #}
    #$.ajax(uri, settings)

  App.rewriteUri = (uri) ->

    rewrittenUri = uri

    windowLocation = window.location

    if windowLocation.search.indexOf("host=") > -1

      uriAnchor = document.createElement('a')
      uriAnchor.href = uri

      rewrittenUri = rewrittenUri.replace(uriAnchor.host, windowLocation.host)
      rewrittenUri += "?host=" + uriAnchor.host

    return rewrittenUri;

  App.createDataModel = (wrmlData) ->
    new App.Entities.Model wrmlData


  App.newDocument = (dataModel) ->
    #alert "New Document!"
    #module = App.getModuleForSchema(schemaUri)
    module = App.module("ModelApp")
    module.showView(dataModel)


  App.getModuleForSchema = (schemaUri) ->
    module = null

    if schemaUri is "http://schema.api.wrml.org/org/wrml/model/schema/Schema"
      module = App.module("SchemaApp")

    else if schemaUri is "http://schema.api.wrml.org/org/wrml/model/rest/Api"
      module = App.module("ApiApp")

    else if schemaUri is "http://schema.api.wrml.org/org/wrml/model/rest/LinkRelation"
      module = App.module("RelationApp")

    else if schemaUri is "http://schema.api.wrml.org/org/wrml/model/rest/status/ApiNotFoundErrorReport"
      module = App.module("ApiNotFoundApp")

    else if schemaUri is "http://schema.api.wrml.org/org/wrml/model/rest/status/ResourceNotFoundErrorReport"
      module = App.module("ResourceNotFoundApp")

    else if schemaUri is "http://schema.api.wrml.org/org/wrml/model/rest/status/DocumentNotFoundErrorReport"
      module = App.module("DocumentNotFoundApp")

    else
      module = App.module("ModelApp")

  #
  # GET (reqres)
  #

  App.reqres.setHandler "default:region", ->
    App.mainRegion

  App.reqres.setHandler "wrml:data", ->
    App.getEmbeddedDataModel()

  #
  # POST (commands)
  #

  App.commands.setHandler "register:instance", (instance, id) ->
    App.register instance, id #if App.environment is "development"

  App.commands.setHandler "unregister:instance", (instance, id) ->
    App.unregister instance, id #if App.environment is "development"


  #
  # Return Wrmldoc
  #

  # For debugging or whatever; provide a handle to the app in the Console.
  window.wrmldoc = App

  console.log(App)

  App
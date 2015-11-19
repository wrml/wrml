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

    App.currentModule = App.getModuleForSchema(schemaUri)
    App.currentModule.start(App.dataModel)

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

  App.openDocument = (uri, target) ->

    aElement = $("<a>").attr("href", uri)
    if (target)
      aElement = aElement.attr("target", "_blank")

    aElement[0].click();

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

  App.rewriteUri = (uri, queryParams) ->

    rewrittenUri = uri

    windowLocation = window.location

    queryString = ""
    if windowLocation.search.indexOf("host=") > -1

      uriAnchor = document.createElement('a')
      uriAnchor.href = uri

      rewrittenUri = rewrittenUri.replace(uriAnchor.host, windowLocation.host)
      queryString = "?host=" + uriAnchor.host

    if queryParams and not $.isEmptyObject(queryParams)
      if queryString.length is 0
        queryString = "?"
      else
        queryString += "&"

      for name, value of queryParams
        queryString += name
        if value?.length > 0
          queryString += "=" + value

        queryString += "&"

      queryString = queryString.slice(0, -1)


    if queryString.length > 0
      rewrittenUri += queryString

    return rewrittenUri


  App.getApiUri = (documentUri) ->
    uriAnchor = document.createElement('a')
    uriAnchor.href = documentUri
    uriAnchor.pathname = ""
    apiUri = uriAnchor.href
    return apiUri

  App.createDataModel = (wrmlData) ->
    new App.Entities.Model wrmlData

  App.saveDocument = ->
    App.currentModule.saveDocument()

  App.saveViewDocument = (viewDocument) ->
    console.log("App.saveViewDocument")
    console.log(viewDocument)

    url = App.rewriteUri(viewDocument.uri)

    $.ajax({
      type: "PUT",
      url: url,
      dataType: "json",
      contentType: "application/json",
      data: JSON.stringify(viewDocument),
      success: (data, textStatus, jqXHR) ->
        console.log(textStatus)
        console.log(data)
        console.log(jqXHR.responseText)
        App.openDocument(url)

      error: (jqXHR, textStatus, errorThrown) ->
        console.error(textStatus)
        console.error(errorThrown)
        console.error(jqXHR.responseText)
    })

  #App.newDocument = (dataModel) ->
  #  #alert "New Document!"
  #  #module = App.getModuleForSchema(schemaUri)
  #  module = App.module("ModelApp")
  #  module.showView(dataModel)


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
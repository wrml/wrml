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
    App.module("HeaderApp").start(App.wrmlData)

    schemaUri = App.wrmlData.get "schemaUri"

    #App.module("RegistrationApp").start(App.wrmlData)

    if schemaUri is "http://schema.api.wrml.org/org/wrml/model/schema/Schema"
      App.module("SchemaApp").start(App.wrmlData)

    else if schemaUri is "http://schema.api.wrml.org/org/wrml/model/rest/Api"
      App.module("ApiApp").start(App.wrmlData)

    else if schemaUri is "http://schema.api.wrml.org/org/wrml/model/rest/LinkRelation"
      App.module("RelationApp").start(App.wrmlData)

    else if schemaUri is "http://schema.api.wrml.org/org/wrml/model/rest/status/ApiNotFoundErrorReport"
      App.module("ApiNotFoundApp").start(App.wrmlData)

    else if schemaUri is "http://schema.api.wrml.org/org/wrml/model/rest/status/ResourceNotFoundErrorReport"
      App.module("ResourceNotFoundApp").start(App.wrmlData)

    else if schemaUri is "http://schema.api.wrml.org/org/wrml/model/rest/status/DocumentNotFoundErrorReport"
      App.module("DocumentNotFoundApp").start(App.wrmlData)

    else
      App.module("ModelApp").start(App.wrmlData)

    App.module("FooterApp").start(App.wrmlData)


  #
  # Event Handlers (on)
  #

  App.on "initialize:before", (wrmlData) ->
    App.wrmlData = new App.Entities.Model wrmlData


  App.on "initialize:after", ->
    @startHistory()
    @navigate(@rootRoute, trigger: true) unless @getCurrentRoute()

  App.getWrmlData = ->
    App.wrmlData

  #
  # GET (reqres)
  #

  App.reqres.setHandler "default:region", ->
    App.mainRegion

  App.reqres.setHandler "wrml:data", ->
    #App.wrmlData
    App.getWrmlData()

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

  App
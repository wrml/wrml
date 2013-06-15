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

@Wrmldoc.module "Components.Form", (Form, App, Backbone, Marionette, $, _) ->
	
	class Form.Controller extends App.Controllers.Base
		
		initialize: (options = {}) ->
			@contentView = wrmlData.view
			
			@formLayout = @getFormLayout wrmlData.config
			
			@listenTo @formLayout, "show", @formContentRegion
			@listenTo @formLayout, "form:submit", @formSubmit
			@listenTo @formLayout, "form:cancel", @formCancel
		
		formCancel: ->
			@contentView.triggerMethod "form:cancel"
		
		formSubmit: ->
			data = Backbone.Syphon.serialize @formLayout
			if @contentView.triggerMethod("form:submit", data) isnt false
				model = @contentView.model
				collection = @contentView.collection
				@processFormSubmit data, model, collection
		
		processFormSubmit: (data, model, collection) ->
			model.save data,
				collection: collection
		
		onClose: ->
			console.log "onClose", @
		
		formContentRegion: ->
			@region = @formLayout.formContentRegion
			@show @contentView
		
		getFormLayout: (options = {}) ->
			config = @getDefaultConfig _.result(@contentView, "form")
			_.extend config, options
			
			buttons = @getButtons config.buttons

			new Form.FormWrapper
				config: config
				model: @contentView.model
				buttons: buttons
		
		getDefaultConfig: (config = {}) ->
			_.defaults config,
				footer: true
				focusFirstInput: true
				errors: true
				syncing: true
		
		getButtons: (buttons = {}) ->
			App.request("form:button:entities", buttons, @contentView.model) unless buttons is false
	
	App.reqres.setHandler "form:wrapper", (contentView, options = {}) ->
		throw new Error "No model found inside of form's contentView" unless contentView.model
		formController = new Form.Controller
			view: contentView
			config: options
		formController.formLayout
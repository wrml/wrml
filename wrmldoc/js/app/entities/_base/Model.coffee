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

@Wrmldoc.module "Entities", (Entities, App, Backbone, Marionette, $, _) ->
	
	class Entities.Model extends Backbone.Model
		
		destroy: (options = {}) ->
			_.defaults options,
				wait: true
			
			@set _destroy: true
			super options
		
		isDestroyed: ->
			@get "_destroy"
		
		save: (data, options = {}) ->
			isNew = @isNew()
			
			_.defaults options,
				wait: true
				success: 	_.bind(@saveSuccess, @, isNew, options.collection)
				error:		_.bind(@saveError, @)
		
			@unset "_errors"
			super data, options
		
		saveSuccess: (isNew, collection) =>
			if isNew ## model is being created
				collection.add @ if collection
				collection.trigger "model:created", @ if collection
				@trigger "created", @
			else ## model is being updated
				collection ?= @collection ## if model has collection property defined, use that if no collection option exists
				collection.trigger "model:updated", @ if collection
				@trigger "updated", @
		
		saveError: (model, xhr, options) =>
			## set errors directly on the model unless status returned was 500 or 404
			@set _errors: $.parseJSON(xhr.responseText)?.errors unless xhr.status is 500 or xhr.status is 404
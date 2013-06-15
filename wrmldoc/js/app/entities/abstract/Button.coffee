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
	
	class Entities.Button extends Entities.Model
		defaults:
			buttonType: "button"
		
	class Entities.ButtonsCollection extends Entities.Collection
		model: Entities.Button
	
	API =
		getFormButtons: (buttons, model) ->
			buttons = @getDefaultButtons buttons, model
			
			array = []
			array.push { type: "cancel", className: "button small secondary radius", text: buttons.cancel } unless buttons.cancel is false
			array.push { type: "primary", className: "button small radius", text: buttons.primary, buttonType: "submit" } unless buttons.primary is false
				
			array.reverse() if buttons.placement is "left"
			
			buttonCollection = new Entities.ButtonsCollection array
			buttonCollection.placement = buttons.placement
			buttonCollection
		
		getDefaultButtons: (buttons, model) ->
			_.defaults buttons,
				primary: if model.isNew() then "Create" else "Update"
				cancel: "Cancel"
				placement: "right"
	
	App.reqres.setHandler "form:button:entities", (buttons = {}, model) ->
		API.getFormButtons buttons, model
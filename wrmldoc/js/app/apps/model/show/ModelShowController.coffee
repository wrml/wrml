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

@Wrmldoc.module "ModelApp.Show", (Show, App, Backbone, Marionette, $, _) ->
  class Show.Controller extends App.Controllers.Base

    initialize: (wrmlData) ->
      showView = @createShowView(wrmlData)
      @show showView

    createShowView: (wrmlData) ->
      #wrmlData = App.request "wrml:data"
      new Show.Model
        model: wrmlData
						
###

	class Show.Controller extends App.Controllers.Base
		
		initialize: ->
			model = App.request "model:entities"
			
			App.execute "when:fetched", model, =>
			
				@layout = @getLayoutView model
				
				# @listenTo @layout, "close", @close
			
				@listenTo @layout, "show", =>
					@titleRegion()
					@panelRegion()
					@modelRegion model
			
				@show @layout
		
		titleRegion: ->
			titleView = @getTitleView()
			@layout.titleRegion.show titleView
		
		panelRegion: ->
			panelView = @getPanelView()
			
			@listenTo panelView, "new:model:button:clicked", =>
				@newRegion()
			
			@layout.panelRegion.show panelView
		
		newRegion: ->
			App.execute "new:model:member", @layout.newRegion
		
		modelRegion: (model) ->
			modelView = @getModelView model
			
			@listenTo modelView, "childview:model:member:clicked", (child, args) ->
				App.vent.trigger "model:member:clicked", args.model
			
			@listenTo modelView, "childview:model:delete:clicked", (child, args) ->
				model = args.model
				if confirm "Are you sure you want to delete #{model.get("name")}?" then model.destroy() else false
			
			@layout.modelRegion.show modelView
		
		getModelView: (model) ->
			
			modelView = 
				new List.Model
					collection: model

			window.modelView = modelView

			modelView
		
		getPanelView: ->
			new List.Panel
		
		getTitleView: ->
			wrmlData = App.request "wrml:data"

			new List.Title
				model: new App.Entities.Model wrmlData
		
		getLayoutView: (model) ->
			new List.Layout
				collection: model


###

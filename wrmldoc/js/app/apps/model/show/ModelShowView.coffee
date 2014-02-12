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

  #class List.Layout extends App.Views.Layout
  #	template: "model/show/model_show"

  #	regions:
  #titleRegion: 	"#title-region"
  #panelRegion:	"#panel-region"
  #newRegion:		"#new-region"
  #		modelRegion:		"#model-region"

  #class List.Title extends App.Views.ItemView
  #	template: "model/list/model_title"

  #class List.Panel extends App.Views.ItemView
  #	template: "model/list/model_panel"

  #	triggers:
  #		"click #new-model" : "new:model:button:clicked"

  #class List.ModelMember extends App.Views.ItemView
  #	template: "model/list/model_list_item"
  #	tagName: "li"
  #	className: "model-member"

  #	triggers:
  #		"click .model-delete button" : "model:delete:clicked"
  #		"click" : "model:member:clicked"

  #class List.Empty extends App.Views.ItemView
  #	template: "model/list/model_empty"
  #	tagName: "li"
  class Show.Model extends App.Views.ItemView
    template: "model/show/model_show"

#class Show.Model extends App.Views.CompositeView
#	template: "model/list/model_list"
#	itemView: List.ModelMember
#	emptyView: List.Empty
#	itemViewContainer: "ul"
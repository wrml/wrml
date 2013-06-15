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
	
	App.commands.setHandler "when:fetched", (entities, callback) ->
		xhrs = _.chain([entities]).flatten().pluck("_fetch").value()
	
		# if _.isArray(entities)
		# 	xhrs.push(entity._fetch) for entity in entities
		# else
		# 	xhrs.push(entities._fetch)
		
		$.when(xhrs...).done ->
			callback()
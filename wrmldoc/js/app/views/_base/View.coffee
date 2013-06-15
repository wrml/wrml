###############################################################################
#                                                                             #
#  WRML - Web Resource Modeling Language                                      #
#   __     __   ______   __    __   __                                        #
#  /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \                                       #
#  \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____                                  #
#   \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\                                 #
#    \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/                                 #
#                                                                             #
# http://www.wrml.org                                                         #
#                                                                             #
# Copyright 2011 - 2013 Mark Masse (OSS project WRML.org)                     #
#                                                                             #
# Licensed under the Apache License, Version 2.0 (the "License");             #
# you may not use this file except in compliance with the License.            #
# You may obtain a copy of the License at                                     #
#                                                                             #
# http://www.apache.org/licenses/LICENSE-2.0                                  #
#                                                                             #
# Unless required by applicable law or agreed to in writing, software         #
# distributed under the License is distributed on an "AS IS" BASIS,           #
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    #
# See the License for the specific language governing permissions and         #
# limitations under the License.                                              #
#                                                                             #
###############################################################################

# CoffeeScript

@Wrmldoc.module "Views", (Views, App, Backbone, Marionette, $, _) ->
	
	_remove = Marionette.View::remove
	
	_.extend Marionette.View::,
	
		addOpacityWrapper: (init = true) ->
			@$el.toggleWrapper
				className: "opacity"
			, init
	
		setInstancePropertiesFor: (args...) ->
			for key, val of _.pick(@options, args...)
				@[key] = val
	
		remove: (args...) ->
			console.log "removing", @
			if @model?.isDestroyed?()
				
				wrapper = @$el.toggleWrapper
					className: "opacity"
					backgroundColor: "red"
				
				wrapper.fadeOut 400, ->
					$(@).remove()
				
				@$el.fadeOut 400, =>
					_remove.apply @, args
			else
				_remove.apply @, args
	
		templateHelpers: ->
			
			linkTo: (name, url, options = {}) ->
				_.defaults options,
					external: false
				
				url = "#" + url unless options.external
				
				"<a href='#{url}'>#{@escape(name)}</a>"
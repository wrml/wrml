#! /bin/bash

##########################################################################################
#                                                                                        #
#  WRML - Web Resource Modeling Language                                                 #
#   __     __   ______   __    __   __                                                   #
#  /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \                                                  #
#  \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____                                             #
#   \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\                                            # 
#    \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/                                            #
#                                                                                        #
# http://www.wrml.org                                                                    #
#                                                                                        #
# Copyright 2013 Mark Masse (OSS project WRML.org)                                       #
#                                                                                        #
# Licensed under the Apache License, Version 2.0 (the "License");                        #
# you may not use this file except in compliance with the License.                       #
# You may obtain a copy of the License at                                                #  
#                                                                                        #
# http://www.apache.org/licenses/LICENSE-2.0                                             #
#                                                                                        #
# Unless required by applicable law or agreed to in writing, software                    #
# distributed under the License is distributed on an "AS IS" BASIS,                      #
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.               #  
# See the License for the specific language governing permissions and                    #
# limitations under the License.                                                         #  
#                                                                                        #
##########################################################################################


java -DwrmlConfiguration=/Users/mark/Documents/WRML/config/dev-wrml.json -classpath "/Users/mark/projects/wrml/wrml/cli/target/wrml-cli.jar" org.wrml.werminal.Werminal -unix
#java -DwrmlConfiguration=/Users/mark/Documents/WRML/config/dev-wrml.json -classpath "/Users/mark/projects/wrml/wrml/cli/target/wrml-cli.jar:/Users/mark/projects/wrml/wrml/core/src/test/resources" org.wrml.werminal.Werminal -unix


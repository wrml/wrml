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

##################################################  
# Environment Variables
##################################################

WRML_SRC_PATH=/Users/mark/projects/wrml/wrml
TOMCAT_PATH=/Library/Tomcat

##################################################  
# Script Variables
##################################################

ARCHIVE_NAME=ROOT

ARCHIVE_EXTENSION=war

ARCHIVE_FILE_NAME=$ARCHIVE_NAME.$ARCHIVE_EXTENSION

SERVER_SOURCE_ARCHIVE=$WRML_SRC_PATH/server/server-war/target/$ARCHIVE_FILE_NAME

SERVER_DEST_DIR=$TOMCAT_PATH/webapps/$ARCHIVE_NAME

SERVER_DEST_ARCHIVE=$SERVER_DEST_DIR.$ARCHIVE_EXTENSION

##################################################  
# Build Server
##################################################

mvn -f $WRML_SRC_PATH/pom.xml clean install 

##################################################  
# Stop Server
##################################################

$TOMCAT_PATH/bin/shutdown.sh

##################################################  
# Clean Server archive and exploded dir
##################################################

rm $SERVER_DEST_ARCHIVE

rm -rf $SERVER_DEST_DIR

##################################################  
# Deploy Server archive file
##################################################

cp $SERVER_SOURCE_ARCHIVE $SERVER_DEST_ARCHIVE

##################################################  
# Start Server
##################################################

$TOMCAT_PATH/bin/startup.sh

##################################################  
# Tail Server
##################################################

tail -f $TOMCAT_PATH/logs/catalina.out
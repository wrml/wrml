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
# Copyright 2011 - 2013 Mark Masse (OSS project WRML.org)                                #
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
# Dependencies 
################################################## 

######################## 
# CoffeeScript 
# http://coffeescript.org
#
# Add "coffee" command to your path
# npm install -g coffee-script
######################## 

######################## 
# ECO Templates (Embedded CoffeeScript)
# https://github.com/sstephenson/eco
#
# Add "eco" command to your path
# On MacOS, the NODE_PATH env var is needed for eco command line to work. 
# NODE_PATH=/usr/local/lib/node:/usr/local/lib/node_modules
#######################

#######################
# Closure Compiler
# https://developers.google.com/closure/compiler
# 
# Download Closure Compiler: http://closure-compiler.googlecode.com/files/compiler-latest.zip
# Unzip and configure the CLOSURE_JAR_PATH variable below
#######################

#######################
# YUI Compressor
# https://github.com/yui/yuicompressor
#
# Download YUI Compressor: https://github.com/yui/yuicompressor/downloads
# Decompress and configure the YUI_COMPRESSOR_JAR_PATH variable below
#######################

#######################
# Sass
# http://sass-lang.com/
#
# gem install sass
# Download Sass: http://sass-lang.com/download.html
#######################

##################################################  
# Script Variables
##################################################

APP_NAME=wrmldoc

JS_PATH=js

JS_LIB_PATH=${JS_PATH}/lib
JS_APP_PATH=${JS_PATH}/app

COMPILED_LIB_JS=${JS_PATH}/lib.js
COMPILED_LIB_MIN_JS=${JS_PATH}/lib.min.js

COMPILED_APP_JS=${JS_PATH}/${APP_NAME}.js
COMPILED_APP_MIN_JS=${JS_PATH}/${APP_NAME}.min.js

CSS_PATH=css

CSS_LIB_PATH=${CSS_PATH}/lib
CSS_APP_PATH=${CSS_PATH}/app

COMPILED_LIB_CSS=${CSS_PATH}/lib.css

COMPILED_APP_SCSS=${CSS_PATH}/${APP_NAME}.scss
COMPILED_APP_CSS=${CSS_PATH}/${APP_NAME}.tmp.css

COMPILED_FINAL_CSS=${CSS_PATH}/${APP_NAME}.css
COMPILED_FINAL_MIN_CSS=${CSS_PATH}/${APP_NAME}.min.css

##################################################  
# Environment Variables
##################################################

APP_DEPLOY_PATH=/Library/Tomcat/webapps/ROOT/_wrml/${APP_NAME}

TOOLS_PATH=/Users/mark/tools

CLOSURE_JAR_PATH=${TOOLS_PATH}/closure/compiler.jar

YUI_COMPRESSOR_JAR_PATH=${TOOLS_PATH}/yuicompressor-2.4.7/build/yuicompressor-2.4.7.jar

##################################################  
# Clean previous run
##################################################

# Remove the previous compilation
rm ${COMPILED_LIB_JS}
rm ${COMPILED_LIB_MIN_JS}

rm ${COMPILED_APP_JS}
rm ${COMPILED_APP_MIN_JS}

rm ${COMPILED_LIB_CSS} 
rm ${COMPILED_APP_SCSS} 
rm ${COMPILED_APP_CSS}
rm ${COMPILED_FINAL_CSS}
rm ${COMPILED_FINAL_MIN_CSS}


##################################################  
# Compile the lib.js file
##################################################

# Concat all of the lib/ .js files

#######################
# jQuery
#######################
cat ${JS_LIB_PATH}/jquery.js > ${COMPILED_LIB_JS}

#######################
# Marionette
#######################

# TODO: Replace this with xargs and a file that lists the files in order
cat ${JS_LIB_PATH}/underscore.js >> ${COMPILED_LIB_JS}
cat ${JS_LIB_PATH}/json2.js >> ${COMPILED_LIB_JS}
cat ${JS_LIB_PATH}/backbone.js >> ${COMPILED_LIB_JS}
cat ${JS_LIB_PATH}/marionette.js >> ${COMPILED_LIB_JS}

cat ${JS_LIB_PATH}/syphon.js >> ${COMPILED_LIB_JS}

#######################
# Bootstrap
#######################
cat ${JS_LIB_PATH}/bootstrap.js >> ${COMPILED_LIB_JS}

echo "Compiled ${COMPILED_LIB_JS}"

##################################################  
# Minify the lib.js with YUI Compressor
##################################################

java -jar ${YUI_COMPRESSOR_JAR_PATH} ${COMPILED_LIB_JS} -o ${COMPILED_LIB_MIN_JS}

##################################################  
# Compile App's CoffeeScript
##################################################

# Compile the .coffee files to .js
coffee -b -c ${JS_APP_PATH}

echo "Compiled all CoffeeScript files found here: ${JS_APP_PATH}"

##################################################  
# Compile App's ECO templates
##################################################

# Compile the .eco files to .js
eco ${JS_APP_PATH}

echo "Compiled all ECO templates found here: ${JS_APP_PATH}"

##################################################  
# Compile the App's .js file
##################################################

# TODO: Replace this with xargs and a file that lists the files/paths in order
cat $(find ${JS_APP_PATH}/config -name *.js) > ${COMPILED_APP_JS}
cat ${JS_APP_PATH}/*.js >> ${COMPILED_APP_JS}
cat $(find ${JS_APP_PATH}/views -name *.js) >> ${COMPILED_APP_JS}
cat $(find ${JS_APP_PATH}/entities -name *.js) >> ${COMPILED_APP_JS}
cat $(find ${JS_APP_PATH}/controllers -name *.js) >> ${COMPILED_APP_JS}
cat $(find ${JS_APP_PATH}/components -name *.js) >> ${COMPILED_APP_JS}
cat $(find ${JS_APP_PATH}/apps -name *.js) >> ${COMPILED_APP_JS}

echo "Compiled ${COMPILED_APP_JS}"

##################################################  
# Clean up App's temporary .js files
##################################################

# Remove all of the compiled CoffeeScript and ECO JS files
find ${JS_APP_PATH} -name *.js -exec rm -f {} \;

##################################################  
# Minify the App's .js with Closure Compiler
##################################################

java -jar ${CLOSURE_JAR_PATH} --js ${COMPILED_APP_JS} --js_output_file ${COMPILED_APP_MIN_JS}

echo "Minified ${COMPILED_APP_JS} to ${COMPILED_APP_MIN_JS} with ${CLOSURE_JAR_PATH}"

##################################################  
# Compile the App .scss
##################################################

# TODO: Replace this with xargs and a file that lists the paths in order

cat ${CSS_APP_PATH}/*.scss > ${COMPILED_APP_SCSS}
cat $(find ${CSS_APP_PATH}/components -name *.scss) >> ${COMPILED_APP_SCSS}
cat $(find ${CSS_APP_PATH}/apps -name *.scss) >> ${COMPILED_APP_SCSS}

echo "Compiled ${COMPILED_APP_SCSS}"

##################################################  
# Compile the App .css
##################################################

cat $(find ${CSS_LIB_PATH} -name *.css) > ${COMPILED_LIB_CSS}

echo "Compiled ${COMPILED_LIB_CSS} from ${CSS_LIB_PATH} .css files"

cat ${COMPILED_LIB_CSS} > ${COMPILED_FINAL_CSS}

echo "Appended ${COMPILED_LIB_CSS} file to ${COMPILED_FINAL_CSS}"

sass -scss --update ${COMPILED_APP_SCSS}:${COMPILED_APP_CSS}

echo "Compiled ${COMPILED_APP_SCSS} to ${COMPILED_APP_CSS} with sass"

cat ${COMPILED_APP_CSS} >> ${COMPILED_FINAL_CSS}

echo "Appended ${COMPILED_APP_CSS} file to ${COMPILED_FINAL_CSS}"


##################################################  
# Minify the App .css with YUI Compressor
##################################################

java -jar ${YUI_COMPRESSOR_JAR_PATH} ${COMPILED_FINAL_CSS} -o ${COMPILED_FINAL_MIN_CSS}

echo "Minified ${COMPILED_FINAL_CSS} to ${COMPILED_FINAL_MIN_CSS} with ${YUI_COMPRESSOR_JAR_PATH}"

##################################################  
# Deploy
##################################################

cp ${COMPILED_LIB_JS} ${APP_DEPLOY_PATH}/${COMPILED_LIB_JS}
echo "Deployed ${COMPILED_LIB_JS} to ${APP_DEPLOY_PATH}/${COMPILED_LIB_JS}"

cp ${COMPILED_LIB_MIN_JS} ${APP_DEPLOY_PATH}/${COMPILED_LIB_MIN_JS}
echo "Deployed ${COMPILED_LIB_MIN_JS} to ${APP_DEPLOY_PATH}/${COMPILED_LIB_MIN_JS}"

cp ${COMPILED_APP_JS} ${APP_DEPLOY_PATH}/${COMPILED_APP_JS}
echo "Deployed ${COMPILED_APP_JS} to ${APP_DEPLOY_PATH}/${COMPILED_APP_JS}"

cp ${COMPILED_APP_MIN_JS} ${APP_DEPLOY_PATH}/${COMPILED_APP_MIN_JS}
echo "Deployed ${COMPILED_APP_MIN_JS} to ${APP_DEPLOY_PATH}/${COMPILED_APP_MIN_JS}"

cp ${COMPILED_FINAL_CSS} ${APP_DEPLOY_PATH}/${COMPILED_FINAL_CSS}
echo "Deployed ${COMPILED_FINAL_CSS} to ${APP_DEPLOY_PATH}/${COMPILED_FINAL_CSS}"

cp ${COMPILED_FINAL_MIN_CSS} ${APP_DEPLOY_PATH}/${COMPILED_FINAL_MIN_CSS}
echo "Deployed ${COMPILED_FINAL_MIN_CSS} to ${APP_DEPLOY_PATH}/${COMPILED_FINAL_MIN_CSS}"






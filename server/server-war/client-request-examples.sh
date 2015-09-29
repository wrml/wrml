#!/bin/bash

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
# Copyright 2015 Mark Masse (OSS project WRML.org)                                       #
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

##########################################################################################
# Tests WRML client-server interaction on localhost.                                     #
##########################################################################################

echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://schema.api.wrml.org/org/wrml/model/schema/Schema"
echo "#"
echo "# The Schema model is interesting because it is a Model which defines the structure"
echo "# of all models. You will notice that it has the standard model slots and it also "
echo "# schematically defines these same slots for all models."
echo "#"
echo " "
curl http://localhost:8080/org/wrml/model/schema/Schema?wrml-api=http://schema.api.wrml.org

echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://schema.api.wrml.org/org/wrml/model/Model"
echo "#"
echo "#"
echo " "
curl http://localhost:8080/org/wrml/model/Model?wrml-api=http://schema.api.wrml.org

echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://schema.api.wrml.org/_wrml/api"
echo "#"
echo "#"
echo " "
curl http://localhost:8080/_wrml/api?wrml-api=http://schema.api.wrml.org

echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://schema.api.wrml.org/_wrml/api/swagger"
echo "#"
echo "#"
echo " "
curl http://localhost:8080/_wrml/api/swagger?wrml-api=http://schema.api.wrml.org


echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://schema.api.wrml.org/org/wrml/model/format/Format"
echo "#"
echo "#"
echo " "
curl http://localhost:8080/org/wrml/model/format/Format?wrml-api=http://schema.api.wrml.org

echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://format.api.wrml.org/application/json"
echo "#"
echo "#"
echo " "
curl http://localhost:8080/application/json?wrml-api=http://format.api.wrml.org

echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://schema.api.wrml.org/org/wrml/model/rest/LinkRelation"
echo "#"
echo "#"
echo " "
curl http://localhost:8080/org/wrml/model/rest/LinkRelation?wrml-api=http://schema.api.wrml.org

echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://relation.api.wrml.org/org/wrml/relation/self"
echo "#"
echo "#"
echo " "
curl http://localhost:8080/org/wrml/relation/self?wrml-api=http://relation.api.wrml.org



echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://schema.api.wrml.org/org/wrml/model/schema/Syntax"
echo "#"
echo "#"
echo " "
curl http://localhost:8080/org/wrml/model/schema/Syntax?wrml-api=http://schema.api.wrml.org

echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://syntax.api.wrml.org/org/wrml/syntax/URI"
echo "#"
echo "#"
echo " "
curl http://localhost:8080/org/wrml/syntax/URI?wrml-api=http://syntax.api.wrml.org

echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://schema.api.wrml.org/org/wrml/model/schema/Choices"
echo "#"
echo "#"
echo " "
curl http://localhost:8080/org/wrml/model/schema/Choices?wrml-api=http://schema.api.wrml.org

echo " "
echo " "
echo "#"
echo "#"
echo "# Requesting http://choices.api.wrml.org/org/wrml/model/schema/ValueType"
echo "#"
echo "#"
echo " "
curl http://localhost:8080/org/wrml/model/schema/ValueType?wrml-api=http://choices.api.wrml.org



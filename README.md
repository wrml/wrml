<p align="center">
  <img src="doc/wrml.png"/>
</p>

# About
WRML, the Web Resource Modeling Language, is a domain-specific modeling language that's oriented toward the design of REST APIs. It is a formalization of common REST API design and implementation patterns found in modern application servers. 

WRML is an open source software project (http://www.wrml.org) focused on providing REST API standards, frameworks, and tools to support the development of web-oriented, client-server applications.

The initial implementation of the WRML runtime is Java-based, with the WrmlServlet providing the REST API engine that frees the service developer to focus on application logic.

# Key Benefits

* Loads and initializes REST API models (API design metadata) to be routed and invoked

* Routes requests to a configured “back-end” **Service** implementation class based upon the target API endpoint’s response document’s **Schema**

* Generates hyperlinks in responses based upon the designs of the API and the response document’s Schema (HATEOAS!)

* Represents response documents using any configured Format (e.g. JSON)

* To reduce the number of requests per screen, supports embedding linked document(s) within the requested document

* To reduce the byte size of responses, supports omission of unused properties from the requested document

* Exposes API and Schema metadata to automate generation of code and docs for clients and intermediaries


# Getting Started

*Getting started* with WRML means something a little bit different to each role involved in the creation of REST APIs.

The remainder of this README is intended for developers or other folks wanting to download, build, install, and run the WRML server and/or client tools.

## Before you install WRML

From a console/terminal window, verify that *Java 7* is installed:

	$> java -version

Which should display something like this (with a "1.7.0" or higher version number):

	java version "1.7.0_25"

If not, it can be downloaded from: http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html

Also verify that *Maven 3* is installed:

	$>  mvn -version

Which should display something like this:

    Apache Maven 3.3.3 (7994120775791599e205a5524ec3e0dfe41d4a06; 2015-04-22T04:57:37-07:00)

If not, it can be downloaded from: http://maven.apache.org/download.cgi

## Download WRML

	$> git clone git@github.com:wrml/wrml.git

## Install WRML

Change to the root directory of the WRML project.

	$> cd wrml

From the root directory run maven's install command.
	
	$> mvn install

Please note that the first time you run this command, it may download several of WRML's dependencies and install them into your system's local maven repository.


## Configure WRML

Edit the WRML configuration file at **wrml/config/filesystem-wrml.json** to match your local environment. The default contents of the configuration file are shown below:
    
	{
	    "context": {
	
	        "schemaLoader": {
	
	            "schemaClassRootDirectory": "/etc/wrml/schemas"
	        },
	
	        "serviceLoader": {
	            "services": [
	                {
	                    "name": "File",
	                    "implementation": "org.wrml.runtime.service.file.FileSystemService",
	                    "settings": {
	                        "rootDirectory": "/etc/wrml/models"
	                    }
	                }
	            ],
	
	            "serviceMapping": {
	
	                "*": "File"
	            }
	        }
	    }
	}

## File Service Root Directory

This setting specifies the root directory for WRML Models (stored as JSON files on disk).

	"rootDirectory" : "/etc/wrml/schemas"
	
You may edit this line to refer to a different directory or keep it as is to accept the default. In either case, ensure that this directory **exists** and it and its subdirectories are **readable & writable** by you.

<p align="center">
  <img src="doc/README/Permissions-01.png" width="50%"/>
</p>
	

## Schema Loader Schema Directory

This setting specifies the root directory for WRML Schemas (compiled as Java interfaces).

	"schemaClassRootDirectory" : "/etc/wrml/models"

You may edit this line to refer to a different directory or keep it as is to accept the default. In either case, ensure that this directory **exists** and it and its subdirectories are **readable & writable** by you.

# Werminal - WRML Terminal

Werminal is a terminal (command line) application for WRML model browsing and editing.

Werminal can be used to create new models of any type such as: Schemas, Teams, Players, Aliens, HomeScreens, Movies; whatever your application calls for.

Werminal also enables you to open, edit, and save data (of any data type).

<p align="center">
  <img src="doc/wormle.png" width="80%"/>
</p>


## Running Werminal

From the project root directory of the WRML project, change to the *cli* subdirectory.

    $> cd cli
    $> ./werminal

The **werminal** command runs Werminal with the following command: 

    java -DwrmlConfiguration=../config/filesystem-wrml.json -classpath "target/wrml-cli.jar" org.wrml.werminal.Werminal -unix

Werminal starts by dispaying the splash screen (shown below). From here you may press any key to start interacting with WRML. 

<p align="center">
  <img src="doc/README/Werminal-001.png" width="90%"/>
</p>

If you have problems starting Werminal, please confirm that the WRML configuration file is using the correct "slash" leaning direction for the folder/directory paths match the conventions of your OS. In WRML config files, the "/" forward slash should work cross-platform.

For more information about running Werminal, see <a href="./cli/README.md">the Werminal /cli project README.</a>

## wrmldoc

See the <a href="./wrmldoc/README.md">/wrmldoc project</a>.

# License
WRML is copyright (C) 2012-2015 Mark Masse <mark@wrml.org> (OSS project WRML.org). WRML is licensed under the Apache License, Version 2.0. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0


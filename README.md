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

* Generates hyperlinks in responses based upon the designs of the API and the response document’s Schema

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

For more information about running Werminal, see <a href="./cli/README.md">the Werminal /cli project README.</a>

If you have problems starting Werminal, please confirm that the WRML configuration file is using the correct "slash" leaning direction for the folder/directory paths match the conventions of your OS. In WRML config files, the "/" forward slash should work cross-platform.


## A Quick Tour of Werminal
This section will walk through a simple example of using Werminal to demonstrate WRML's modeling features. Specifically, in this tour we will:

1. Design a new schema
  * Create a new schema, named **Demo**
  * Save our new schema to disk, using the "File" service 
  * Load our new schema as a java class
2. Create model instances of new schema type
  * Create a new instance of **Demo**
  * Save the model instance to disk
  * Exit Werminal, then re-launch and re-open the saved demo instance 

For more about the Werminal app, please consult the "[Werminal Masters Handbook](https://github.com/wrml/wrml/blob/master/doc/WRML_WerminalMastersHandbook.pdf)"

### Designing a new Schema

As you might expect, a WRML schema is a structured data type. For the first part of the Werminal tour, you will create a new schema named **Demo**.

Following the splash screen (after pressing any key), Werminal displays its main menu (shown below).

<p align="center">
  <img src="doc/README/Werminal-002.png" width="90%"/>
</p>

The **New** button has input focus (indicated by bold red text) so you can press the enter key to activate it.

The **New Model** dialog will prompt you to enter the URI that identifies the schema of the model that you wish to create. To create a new schema, the URI is [http://schema.api.wrml.org/org/wrml/model/schema/Schema](http://schema.api.wrml.org/org/wrml/model/schema/Schema).

By default, this URI is already entered so you can simply press the tab key until the **OK** button has input focus and then press the enter key to activate it.

<p align="center">
  <img src="doc/README/Werminal-003.png" width="90%"/>
</p>

Now you should see Werminal's model editor window with the title **"Werminal - Model - Schema"**, which indicates that you are editing a model of type schema. 

The top portion of the model editor window displays a toolbar, which we will make use of in a moment. For now, we will start by editing a few of the **slots** of our new schema model. A WRML model slot is analagous to a property, field, or column in other modeling systems. 

<p align="center">
  <img src="doc/README/Werminal-004.png" width="90%"/>
</p>

The first slot to edit is named **uri**. Press the tab key until you see the uri slot's value gain input focus, which is indicated by red highlight and cursor location. 

With the uri slot focused, edit the value to **http://schema.api.wrml.org/Demo**.

<p align="center">
  <img src="doc/README/Werminal-005.png" width="90%"/>
</p>

Next, press the tab key (or down arrow key) to give focus to the **baseSchemaURis** slot. This slot allows schema models to declare that they extend other schemas. The value in this slot is a list of URIs that identify the *base* schemas for this schema. 

With the **baseSchemaURis** slot focused, press the enter key to open the list editor window.

<p align="center">
  <img src="doc/README/Werminal-006.png" width="90%"/>
</p>



<p align="center">
  <img src="doc/README/Werminal-007.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-008.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-009.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-010.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-011.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-012.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-013.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-014.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-015.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-016.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-024.png" width="90%"/>
</p>


### Creating a Model Instance

<p align="center">
  <img src="doc/README/Werminal-017.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-018.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-019.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-020.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-021.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-022.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-023.png" width="90%"/>
</p>

<p align="center">
  <img src="doc/README/Werminal-025.png" width="90%"/>
</p>




## wrmldoc

See the <a href="./wrmldoc/README.md">/wrmldoc project.</a>.



# License
WRML is copyright (C) 2012-2015 Mark Masse <mark@wrml.org> (OSS project WRML.org). WRML is licensed under the Apache License, Version 2.0. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0


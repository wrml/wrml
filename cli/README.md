<p align="center">
  <img src="../doc/wrml.png"/>
</p>

Werminal 
================================

Werminal is a terminal (command line) application for WRML model browsing and editing. 

Werminal can be used to create new models of _any_ type such as: Schemas, Teams, Players, Aliens, HomeScreens, Movies; whatever your application calls for.  

Werminal also enables you to open, edit, and save data (of any data type).  

Running Werminal
================================

Note that all the following examples are dependent on the `wrml-cli.jar` file.  This file can be built from maven in the `cli` project:


## Default ##

To run Werminal with the default options, execute the `wrml-cli.jar` using the `java -jar` command:

    java -jar cli/target/wrml-cli.jar

This assumes that you have a WRML configuration file named `wrml.json` in either the current working directory or your user directory.

## Specify <code>-config</code> command line argument ##

To run Werminal with a specified configuration file path using the `-config` program argument:

    java -jar cli/target/wrml-cli.jar org.wrml.werminal.Werminal -config my/path/to/wrml.json

or to specify the UNIX terminal mode:

    java -jar cli/target/wrml-cli.jar org.wrml.werminal.Werminal -config my/path/to/wrml.json -unix

## Specify <code>wrmlConfiguration</code> JVM argument ##

To run Werminal with a specific configuration file path using the `-DwrmlConfiguration` JVM argument:

    java -DwrmlConfiguration=my/path/to/wrml.json -jar cli/target/wrml-cli.jar

or to manually specify the CLI's main class:    

    java -DwrmlConfiguration=my/path/to/wrml.json -jar cli/target/wrml-cli.jar org.wrml.werminal.Werminal

or to specify the UNIX terminal mode:

    java -DwrmlConfiguration=my/path/to/wrml.json -jar cli/target/wrml-cli.jar org.wrml.werminal.Werminal -unix
  

## Via Eclipse .launch Configuration ##

1. In 'Run Configurations', choose 'New Java Application'.
1. On the 'Main' tab, enter:
   1. Project: "`cli`"
   1. Main class: "`org.wrml.werminal.Werminal`"
   1. Check "Stop in main"
1. [Optional] On the 'Arguments' tab, either the `-config` or `-DwrmlConfiguration` options described above.


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
  <img src="../doc/README/Werminal-002.png" width="90%"/>
</p>

The **New** button has input focus (indicated by bold red text) so you can press the enter key to activate it.

The **New Model** dialog will prompt you to enter the URI that identifies the schema of the model that you wish to create. To create a new schema, the URI is [http://schema.api.wrml.org/org/wrml/model/schema/Schema](http://schema.api.wrml.org/org/wrml/model/schema/Schema).

By default, this URI is already entered so you can simply press the tab key until the **OK** button has input focus and then press the enter key to activate it.

<p align="center">
  <img src="../doc/README/Werminal-003.png" width="90%"/>
</p>

Now you should see Werminal's model editor window with the title **"Werminal - Model - Schema"**, which indicates that you are editing a model of type schema. 

The top portion of the model editor window displays a toolbar, which we will make use of in a moment. For now, we will start by editing a few of the **slots** of our new schema model. A WRML model slot is analagous to a property, field, or column in other modeling systems. 

<p align="center">
  <img src="../doc/README/Werminal-004.png" width="90%"/>
</p>

The first slot to edit is named **uri**. Press the tab key until you see the uri slot's value gain input focus, which is indicated by red highlight and cursor location. 

With the uri slot focused, edit the value to **http://schema.api.wrml.org/Demo**.

<p align="center">
  <img src="../doc/README/Werminal-005.png" width="90%"/>
</p>

Next, press the tab key (or down arrow key) to give focus to the **baseSchemaURis** slot. This slot allows schema models to declare that they extend other schemas. The value in this slot is a list of URIs that identify the *base* schemas for this schema. 

With the **baseSchemaURis** slot focused, press the enter key to open the list editor window.

<p align="center">
  <img src="../doc/README/Werminal-006.png" width="90%"/>
</p>




<p align="center">
  <img src="../doc/README/Werminal-007.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-008.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-009.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-010.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-011.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-012.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-013.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-014.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-015.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-016.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-024.png" width="90%"/>
</p>


### Creating a Model Instance

<p align="center">
  <img src="../doc/README/Werminal-017.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-018.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-019.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-020.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-021.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-022.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-023.png" width="90%"/>
</p>

<p align="center">
  <img src="../doc/README/Werminal-025.png" width="90%"/>
</p>



References
===============

For more about the Werminal app, please consult the "Werminal Masters Handbook": https://github.com/wrml/wrml/blob/master/doc/WRML_WerminalMastersHandbook.pdf

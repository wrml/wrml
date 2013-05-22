<p align="center">
  <img src="http://www.wrml.org/images/site-logo-2.png"/>
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


References
===============

For more about the Werminal app, please consult the "Werminal Masters Handbook": http://www.wrml.org/werminal/WerminalMastersHandbook.pdf

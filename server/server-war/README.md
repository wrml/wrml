<p align="center">
  <img src="http://www.wrml.org/images/site-logo-2.png"/>
</p>

Server-WAR 
================================

The server-war module packages the <a href="../../core">WRML core</a> into a <a href="https://en.wikipedia.org/wiki/WAR_(Sun_file_format)">WAR file</a>.


Module Dependencies
================================
1. <a href="https://tomcat.apache.org/download-70.cgi">Tomcat 7.x</a>



Running Server-War
================================

## Default ##

By default, the WRML Servlet will look for wrmlConfiguration to be defined in the servlet's web.xml file:

    <servlet>
      <display-name>WrmlServlet</display-name>
      <servlet-name>WrmlServlet</servlet-name>
      <servlet-class>org.wrml.server.WrmlServlet</servlet-class>
      <init-param>
          <param-name>wrmlConfiguration</param-name>
          <param-value>/wrml.json</param-value>
      </init-param>
    </servlet>


## Specify <code>wrmlConfiguration</code> ##

[Optional] To run a web container (i.e., Tomcat) with a specified home path using the `wrmlHome` program variable, add the following to the web container program (startup) variables: 

    -DwrmlConfiguration=/path/to/wrml.json 

## Example ##

See <a href="./src/main/webapp/WEB-INF/web.xml">web.xml</a>.


## Old Instructions 
Instructions for use of the War:

1. Compile
2. Download Tomcat 7.X
3. Inside `TOMCAT_HOME`, create a directory called wrml

    `cd TOMCAT_HOME; mkdir wrml`

4. Copy the war into the directory

    `cp WRML_HOME/server/server-war/target/server-war-1.0-SNAPSHOT.war TOMCAT_HOME/wrml/`


5. Add a ROOT.xml to `TOMCAT_HOME/conf/Catalina/localhost/` with the following:

    `<Context docBase="TOMCAT_HOME/wrml/server-war-1.0-SNAPSHOT" path="" reloadable="true" />`


6. Startup Tomcat (see <a href="https://tomcat.apache.org/tomcat-7.0-doc/setup.html">apache.org</a> for help)

## Verification 
Try hitting a schema:

    GET http://localhost:8080/org/wrml/model/schema/Schema


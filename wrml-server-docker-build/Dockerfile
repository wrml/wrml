# Based on jetty: https://hub.docker.com/_/jetty/
FROM jetty:9

# Copy the WRML server war file to the spot that Jetty expects
COPY ROOT.war /var/lib/jetty/webapps/

# Make a few directories that WRML components require
RUN mkdir -p /etc/wrml/schemas
RUN chmod -R a+w /etc/wrml/schemas

RUN mkdir -p /etc/wrml/models
RUN chmod -R a+w /etc/wrml/models

# Copy the WRML configuration file to the image
COPY wrml.json /etc/wrml/

# Copy the Werminal CLI launch script
COPY werminal.sh /etc/wrml/

# Make the Werminal CLI launch script executable
RUN chmod +x /etc/wrml/werminal.sh

# Copy the Werminal CLI app
COPY wrml-cli.jar /etc/wrml/

# Set the WRML configuration file environment variable
ENV wrmlConfiguration /etc/wrml/wrml.json


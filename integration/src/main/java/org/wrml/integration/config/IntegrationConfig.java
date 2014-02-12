package org.wrml.integration.config;

/**
 * User: john
 * Date: 4/15/13
 * Time: 10:53 AM
 */

import java.io.IOException;
import java.util.Properties;

/**
 * An abstract base class for integration test configuration
 */
public abstract class IntegrationConfig {

    private Properties props;

    private static final String PROPERTIES_FILE = "/config.properties";

    public IntegrationConfig() throws IOException {

        props = new Properties();
        props.load(IntegrationConfig.class.getResourceAsStream(PROPERTIES_FILE));
    }

    public String getValue(String key) {

        return props.getProperty(key);
    }

}

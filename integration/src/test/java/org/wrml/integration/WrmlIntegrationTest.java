package org.wrml.integration;

import org.apache.catalina.LifecycleException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.wrml.integration.context.ContextStore;
import org.wrml.integration.tomcat.TomcatManager;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * User: john Date: 4/9/13 Time: 10:42 AM
 */
@ContextConfiguration(locations = "classpath:/applicationContext.xml", loader = AnnotationConfigContextLoader.class)
@RunWith(IntegrationTestRunner.class)
@ActiveProfiles({"embedded", "test"})
public class WrmlIntegrationTest implements ApplicationContextAware {

    //@Inject
    private ApplicationContext context;
    // private ApplicationContext context;

    @Autowired(required = true)
    private ContextStore contextStore;


    private static TomcatManager tomcatManager;

    private static Logger LOGGER = LoggerFactory.getLogger(WrmlIntegrationTest.class);

    public WrmlIntegrationTest() {
        //context = contextStore.getApplicationContext();
    }

    /**
     * Start up Tomcat before each test class
     *
     * @throws LifecycleException if there is a problem starting Tomcat
     * @throws IOException        if there is a problem getting the underlying config during the TomcatManager instantiation
     */
    @BeforeClass
    public static void startupTomcat() throws ServletException, LifecycleException, IOException {

        LOGGER.info("Starting Tomcat harness.");
        tomcatManager = new TomcatManager();
        tomcatManager.init();
        LOGGER.info("Tomcat harness started.");
    }

    /**
     * Shut down Tomcat after each test class
     *
     * @throws LifecycleException if there is a problem stopping Tomcat
     */
    @AfterClass
    public static void shutdownTomcat() throws LifecycleException {

        LOGGER.info("Shutting down Tomcat harness.");
        tomcatManager.shutdown();
        LOGGER.info("Tomcat harness shutdown complete.");
    }

    /**
     * Returns the port that Tomcat uses
     */
    public static int getPort() {

        return tomcatManager.getPort();
    }

    public ApplicationContext getContext() {

        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {

        this.context = context;
    }

    public ContextStore getContextStore() {

        return contextStore;
    }

    public void setContextStore(ContextStore contextStore) {

        this.contextStore = contextStore;
    }

}

package org.wrml.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: john Date: 4/16/13 Time: 11:18 AM
 */
public class TestTest extends WrmlRestIntegrationTest {

    private static Logger LOGGER = LoggerFactory.getLogger(TestTest.class);

    @IntegrationTest
    public void fooTest() {

        LOGGER.error("Testing");
    }

    @IntegrationTest
    public void barTest() {

        LOGGER.info("" + WrmlIntegrationTest.getPort());
    }

    @IntegrationTest
    //@Ignore
    public void bazTest() {
        // TODO: Fix NPE
        LOGGER.error(super.getContext().toString());
    }

    @IntegrationTest
    public void quuxTest() {

        LOGGER.error(getContextStore().toString());
    }

}

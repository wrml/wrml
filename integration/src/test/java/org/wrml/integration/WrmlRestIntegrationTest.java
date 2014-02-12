package org.wrml.integration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.filter.log.RequestLoggingFilter;
import com.jayway.restassured.filter.log.ResponseLoggingFilter;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * User: john
 * Date: 5/6/13
 * Time: 10:42 AM
 */
public abstract class WrmlRestIntegrationTest extends WrmlIntegrationTest {

    private static RequestSpecification basicRequest;

    @BeforeClass
    public static void beforeEachClass() throws IOException {

        basicRequest = new RequestSpecBuilder()
                .addFilter(new RequestLoggingFilter())
                .addFilter(ResponseLoggingFilter.responseLogger())
                .setPort(getPort())
                .build();
        RestAssured.port = getPort();
    }

    /**
     * Returns a basic request
     */
    public static RequestSpecification getRequest() {

        return basicRequest;
    }

}

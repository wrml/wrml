package org.wrml.integration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: john
 * Date: 4/9/13
 * Time: 9:47 AM
 */

/**
 * Annotation for marking integration tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationTest {
}

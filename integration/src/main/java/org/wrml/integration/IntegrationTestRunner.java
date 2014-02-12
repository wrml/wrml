package org.wrml.integration;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.List;

/**
 * User: john
 * Date: 4/9/13
 * Time: 9:49 AM
 */

/**
 * A test runner for integration tests
 */
public class IntegrationTestRunner extends BlockJUnit4ClassRunner {

    public IntegrationTestRunner(Class<?> klazz) throws InitializationError {

        super(klazz);
    }

    @Override
    public List<FrameworkMethod> computeTestMethods() {

        return getTestClass().getAnnotatedMethods(IntegrationTest.class);
    }

}

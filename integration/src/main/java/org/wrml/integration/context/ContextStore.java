package org.wrml.integration.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.inject.Inject;

/**
 * User: john
 * Date: 4/15/13
 * Time: 3:15 PM
 */
public class ContextStore implements ApplicationContextAware {

    private static final ContextStore INSTANCE = new ContextStore();

    private ApplicationContext ctx;

    private ContextStore() {

    }

    public static ContextStore getInstance() {
        return INSTANCE;
    }

    public ApplicationContext getApplicationContext() {
        return ctx;
    }

    @Inject
    public void setApplicationContext(ApplicationContext ctx) {
        this.ctx = ctx;
        System.err.println(ctx.toString());
    }

}

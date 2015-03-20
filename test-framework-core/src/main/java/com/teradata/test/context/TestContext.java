/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.context;

import static java.util.Arrays.asList;

public interface TestContext
{
    /**
     * Allows obtaining runtime dependency from within test body.
     * Common types of dependencies would be State instance comming out of RequirementFulfillers
     * and services for perfoming work on cluster (like QueryExecutor, HdfsClient, RemoteExcecutor)
     *
     * @param dependencyClass Class of dependency to be obtained
     */
    <T> T getDependency(Class<T> dependencyClass);

    <T> T getDependency(Class<T> dependencyClass, String dependencyName);

    void pushStates(Iterable<State> states);

    default void pushStates(State... states)
    {
        pushStates(asList(states));
    }

    void popStates();

    /**
     * Registers a callback that will be executed on {@link TestContext} close.
     */
    void registerCloseCallback(Runnable callback);

    void close();
}

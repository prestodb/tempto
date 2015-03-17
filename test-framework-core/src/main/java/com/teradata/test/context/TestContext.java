/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.context;

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

    <S extends State> void pushState(S state);

    <S extends State> void pushState(S state, String stateName);

    void popState();

    void close();
}

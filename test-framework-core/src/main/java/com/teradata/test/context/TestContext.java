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
    public <T> T getDependency(Class<T> dependencyClass);

    // todo add mutability methods for sake of DSLs changing context
}

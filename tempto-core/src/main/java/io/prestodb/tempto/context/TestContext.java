/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.prestodb.tempto.context;

import java.util.Optional;

import static java.util.Arrays.asList;

public interface TestContext
{
    /**
     * Allows obtaining runtime dependency from within test body.
     * Common types of dependencies would be State instance coming out of RequirementFulfillers
     * and services for performing work on cluster (like QueryExecutor, HdfsClient, RemoteExecutor)
     *
     * @param <T> template
     * @param dependencyClass Class of dependency to be obtained
     * @return Dependency
     */
    <T> T getDependency(Class<T> dependencyClass);

    <T> T getDependency(Class<T> dependencyClass, String dependencyName);

    /**
     * Same as {@link #getDependency(Class)} but will return Optional.empty() if no binding is present for
     * given class.
     *
     * @param <T> template
     * @param dependencyClass Class of dependency to be obtained
     * @return Dependency
     */
    <T> Optional<T> getOptionalDependency(Class<T> dependencyClass);

    /**
     * Same as {@link #getDependency(Class, String)} but will return Optional.empty() if no binding is present for
     * given class.
     *
     * @param <T> template
     * @param dependencyClass Class of dependency to be obtained
     * @param dependencyName Name of dependency
     * @return Dependency
     */
    <T> Optional<T> getOptionalDependency(Class<T> dependencyClass, String dependencyName);

    /**
     * Creates a new child {@link TestContext} with new {@link com.google.inject.Injector}
     * that contains new states.
     *
     * @param states requested states
     * @return TestContext
     */
    TestContext createChildContext(Iterable<State> states);

    default TestContext createChildContext(State... states)
    {
        return createChildContext(asList(states));
    }

    /**
     * Registers a callback that will be executed on {@link TestContext} close.
     *
     * @param callback callback that will be executed on {@link TestContext} close.
     */
    void registerCloseCallback(TestContextCloseCallback callback);

    /**
     * Closes this {@link TestContext} and all children {@link TestContext}s.
     */
    void close();
}

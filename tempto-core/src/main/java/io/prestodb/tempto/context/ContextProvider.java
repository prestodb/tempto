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

/**
 * Provider class used for generation and cleanup of dsl defined contexts.
 *
 * @param <T> context class
 */
public interface ContextProvider<T>
{
    /**
     * Method generating new context.
     *
     * @return generated context
     */
    T setup();

    /**
     * Method invoked after finishing {@link ContextRunnable#run}
     *
     * @param context dls defined context
     */
    void cleanup(T context);
}

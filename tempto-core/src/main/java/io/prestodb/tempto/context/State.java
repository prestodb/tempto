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

/**
 * Marker interface of state objects produced by fulfillers.
 */
public interface State
{
    /**
     * Return name for state. If non-empty optional is
     * returned State will be bound in TestContext with name annotation.
     *
     * @return Name
     */
    default Optional<String> getName()
    {
        return Optional.empty();
    }
}

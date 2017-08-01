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

package com.teradata.tempto.internal.hadoop.revisions;

import java.util.Optional;

/**
 * Store revision id for particular file system path
 */
public interface RevisionStorage
{

    /**
     * Get current data revision for given {@code path}
     * @param path Path to be examined
     * @return data revision
     */
    Optional<String> get(String path);

    /**
     * Store data revision for {@code path}
     *
     * @param path Path to be affected
     * @param revision Revision string to be associated with path
     */
    void put(String path, String revision);

    /**
     * Remove information about {@code path} data revision
     * If data revision hasn't been set or path does not exist method will just return as regular
     *
     * @param path Path to be affected
     */
    void remove(String path);
}

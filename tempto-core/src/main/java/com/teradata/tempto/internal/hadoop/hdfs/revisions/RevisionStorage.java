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

package com.teradata.tempto.internal.hadoop.hdfs.revisions;

import java.util.Optional;

/**
 * Store revision id for particular hdfs path
 */
public interface RevisionStorage
{

    /**
     * Get current data revision for given {@code hdfsPath}
     *
     * @param hdfsPath Path to be examined
     * @return data revision
     */
    Optional<String> get(String hdfsPath);

    /**
     * Store data revision for {@code hdfsPath}
     *
     * @param hdfsPath Path to be affected
     * @param revision Revision string to be associated with hdfsPath
     */
    void put(String hdfsPath, String revision);

    /**
     * Remove information about {@code hdfsPath} data revision
     * If data revision hasn't been set or hdfsPath does not exist method will just return as regular
     *
     * @param hdfsPath Path to be affected
     */
    void remove(String hdfsPath);
}

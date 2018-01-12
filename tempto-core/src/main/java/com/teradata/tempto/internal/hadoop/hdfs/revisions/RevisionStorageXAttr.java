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

import com.teradata.tempto.hadoop.hdfs.HdfsClient;

import java.util.Optional;

/**
 * Store table data revision as XAttr
 */
public class RevisionStorageXAttr
        implements RevisionStorage
{

    /**
     * XAttr name stored on HDFS for each data source file.
     */
    private static final String REVISON_XATTR_NAME = "user.test-data-revision";

    private final HdfsClient hdfsClient;

    RevisionStorageXAttr(HdfsClient hdfsClient)
    {
        this.hdfsClient = hdfsClient;
    }

    @Override
    public Optional<String> get(String hdfsPath)
    {
        return hdfsClient.getXAttr(hdfsPath, REVISON_XATTR_NAME);
    }

    @Override
    public void put(String hdfsPath, String revision)
    {
        hdfsClient.setXAttr(hdfsPath, REVISON_XATTR_NAME, revision);
    }

    @Override
    public void remove(String hdfsPath)
    {
        if (get(hdfsPath).isPresent()) {
            hdfsClient.removeXAttr(hdfsPath, REVISON_XATTR_NAME);
        }
    }
}

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

import com.teradata.tempto.hadoop.FileSystemClient;

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

    private final FileSystemClient fsClient;

    RevisionStorageXAttr(FileSystemClient hdfsClient)
    {
        this.fsClient = hdfsClient;
    }

    @Override
    public Optional<String> get(String path)
    {
        return fsClient.getXAttr(path, REVISON_XATTR_NAME);
    }

    @Override
    public void put(String path, String revision)
    {
        fsClient.setXAttr(path, REVISON_XATTR_NAME, revision);
    }

    @Override
    public void remove(String path)
    {
        if(get(path).isPresent()){
            fsClient.removeXAttr(path, REVISON_XATTR_NAME);
        }
    }
}

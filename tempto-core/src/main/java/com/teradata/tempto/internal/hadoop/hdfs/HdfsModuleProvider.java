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

package com.teradata.tempto.internal.hadoop.hdfs;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.teradata.tempto.configuration.Configuration;
import com.teradata.tempto.hadoop.hdfs.HdfsClient;
import com.teradata.tempto.initialization.AutoModuleProvider;
import com.teradata.tempto.initialization.SuiteModuleProvider;
import com.teradata.tempto.internal.hadoop.hdfs.revisions.RevisionStorage;
import com.teradata.tempto.internal.hadoop.hdfs.revisions.RevisionStorageProvider;

@AutoModuleProvider
public class HdfsModuleProvider
        implements SuiteModuleProvider
{
    @Override
    public Module getModule(Configuration configuration)
    {
        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind(HdfsClient.class).to(WebHDFSClient.class).in(Scopes.SINGLETON);
                bind(RevisionStorage.class).toProvider(RevisionStorageProvider.class).in(Scopes.SINGLETON);
                bind(HdfsDataSourceWriter.class).to(DefaultHdfsDataSourceWriter.class).in(Scopes.SINGLETON);
            }
        };
    }
}

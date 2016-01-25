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

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.teradata.tempto.configuration.Configuration;
import com.teradata.tempto.hadoop.hdfs.HdfsClient;
import com.teradata.tempto.initialization.AutoModuleProvider;
import com.teradata.tempto.initialization.SuiteModuleProvider;
import com.teradata.tempto.internal.hadoop.hdfs.revisions.RevisionStorage;
import com.teradata.tempto.internal.hadoop.hdfs.revisions.RevisionStorageProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Optional;

@AutoModuleProvider
public class HdfsModuleProvider
        implements SuiteModuleProvider
{
    private static final String AUTHENTICATION_SPNEGO = "SPNEGO";
    private static final int NUMBER_OF_HTTP_RETRIES = 3;

    @Override
    public Module getModule(Configuration configuration)
    {
        return new PrivateModule()
        {
            @Override
            protected void configure()
            {
                install(httpRequestsExecutorModule());

                bind(HdfsClient.class).to(WebHdfsClient.class).in(Scopes.SINGLETON);
                bind(RevisionStorage.class).toProvider(RevisionStorageProvider.class).in(Scopes.SINGLETON);
                bind(HdfsDataSourceWriter.class).to(DefaultHdfsDataSourceWriter.class).in(Scopes.SINGLETON);

                expose(HdfsClient.class);
                expose(RevisionStorage.class);
                expose(HdfsDataSourceWriter.class);
            }

            private Module httpRequestsExecutorModule()
            {
                if (spnegoAuthenticationRequired()) {
                    return new SpnegoHttpRequestsExecutor.Module();
                }
                else {
                    return new SimpleHttpRequestsExecutor.Module();
                }
            }

            private boolean spnegoAuthenticationRequired()
            {
                Optional<String> authentication = configuration.getString("hdfs.webhdfs.authentication");
                return authentication.isPresent() && authentication.get().equalsIgnoreCase(AUTHENTICATION_SPNEGO);
            }

            @Inject
            @Provides
            @Singleton
            CloseableHttpClient createHttpClient()
            {
                HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
                httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(NUMBER_OF_HTTP_RETRIES, true));
                return httpClientBuilder.build();
            }
        };
    }
}

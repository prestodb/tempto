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

package io.prestodb.tempto.internal.hadoop.hdfs;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.hadoop.hdfs.HdfsClient;
import io.prestodb.tempto.initialization.AutoModuleProvider;
import io.prestodb.tempto.initialization.SuiteModuleProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

import static io.prestodb.tempto.internal.hadoop.hdfs.WebHdfsClient.CONF_HDFS_WEBHDFS_HOST_KEY;

@AutoModuleProvider
public class HdfsModuleProvider
        implements SuiteModuleProvider
{
    private static final Logger logger = LoggerFactory.getLogger(HdfsModuleProvider.class);

    public static final String CONF_TESTS_HDFS_PATH_KEY = "tests.hdfs.path";

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
                Set<String> configurationKeys = configuration.listKeys();
                if (!configurationKeys.contains(CONF_HDFS_WEBHDFS_HOST_KEY)
                        || !configurationKeys.contains(CONF_TESTS_HDFS_PATH_KEY)) {
                    logger.debug("No HDFS support enabled as '{}' or '{}' is configured",
                            CONF_HDFS_WEBHDFS_HOST_KEY,
                            CONF_TESTS_HDFS_PATH_KEY);
                    return;
                }

                install(httpRequestsExecutorModule());

                bind(HdfsClient.class).to(WebHdfsClient.class).in(Scopes.SINGLETON);
                bind(HdfsDataSourceWriter.class).to(DefaultHdfsDataSourceWriter.class).in(Scopes.SINGLETON);

                expose(HdfsClient.class);
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

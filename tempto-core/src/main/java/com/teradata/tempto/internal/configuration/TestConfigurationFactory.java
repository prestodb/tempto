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
package com.teradata.tempto.internal.configuration;

import com.teradata.tempto.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import static com.teradata.tempto.internal.configuration.EmptyConfiguration.emptyConfiguration;

public class TestConfigurationFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestConfigurationFactory.class);

    public static final String TEST_CONFIGURATION_URI_KEY = "tempto.configuration";
    public static final String LOCAL_TEST_CONFIGURATION_URI_KEY = "tempto.configuration.local";

    public static final String CLASSPATH_PROTOCOL = "classpath:";

    private static final String DEFAULT_TEST_CONFIGURATION_URI = CLASSPATH_PROTOCOL + "/tempto-configuration.yaml";
    private static final String LOCAL_DEFAULT_TEST_CONFIGURATION_URI = CLASSPATH_PROTOCOL + "/tempto-configuration-local.yaml";

    // TODO @Inject, unstatic this class
    private static final ConfigurationVariableResolver CONFIGURATION_VARIABLE_RESOLVER = new ConfigurationVariableResolver();

    public static Configuration createTestConfiguration()
    {
        Configuration configuration = new HierarchicalConfiguration(readTestConfiguration(), readLocalConfiguration());
        return CONFIGURATION_VARIABLE_RESOLVER.resolve(configuration);
    }

    private static Configuration readTestConfiguration()
    {
        String testConfigurationUri = System.getProperty(TEST_CONFIGURATION_URI_KEY, DEFAULT_TEST_CONFIGURATION_URI);
        Optional<InputStream> testConfigurationStream = getConfigurationInputStream(testConfigurationUri);
        if (!testConfigurationStream.isPresent()) {
            throw new IllegalArgumentException("tempto configuration URI is incorrect: " + testConfigurationUri);
        }
        return parseConfiguration(testConfigurationStream.get());
    }

    private static Configuration readLocalConfiguration()
    {
        String localTestConfigurationUri = System.getProperty(LOCAL_TEST_CONFIGURATION_URI_KEY, LOCAL_DEFAULT_TEST_CONFIGURATION_URI);
        Optional<InputStream> localTestConfigurationStream = getConfigurationInputStream(localTestConfigurationUri);
        if (localTestConfigurationStream.isPresent()) {
            InputStream testConfigurationStream = localTestConfigurationStream.get();
            return parseConfiguration(testConfigurationStream);
        }
        else {
            return emptyConfiguration();
        }
    }

    private static Optional<InputStream> getConfigurationInputStream(String testConfigurationUri)
    {
        if (testConfigurationUri.startsWith(CLASSPATH_PROTOCOL)) {
            InputStream input = YamlConfiguration.class.getResourceAsStream(testConfigurationUri.substring(CLASSPATH_PROTOCOL.length()));
            if (input == null) {
                LOGGER.warn("Unable to find configuration: {}", testConfigurationUri);
                return Optional.empty();
            }
            return Optional.of(input);
        }
        else {
            try {
                return Optional.of(new URL(testConfigurationUri).openConnection().getInputStream());
            }
            catch (IOException e) {
                LOGGER.warn("Unable to find configuration: {}", testConfigurationUri);
                return Optional.empty();
            }
        }
    }

    private static YamlConfiguration parseConfiguration(InputStream testConfigurationStream)
    {
        try (InputStream configurationInputStream = testConfigurationStream) {
            return new YamlConfiguration(configurationInputStream);
        }
        catch (IOException e) {
            throw new RuntimeException("could not parse configuration file", e);
        }
    }
}

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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static com.teradata.tempto.internal.configuration.EmptyConfiguration.emptyConfiguration;

public class TestConfigurationFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestConfigurationFactory.class);

    public static final String TEST_CONFIGURATION_URI_KEY = "tempto.configuration";
    public static final String LOCAL_TEST_CONFIGURATION_URI_KEY = "tempto.configuration.local";

    public static final String DEFAULT_TEST_CONFIGURATION_LOCATION = "tempto-configuration.yaml";
    public static final String DEFAULT_LOCAL_TEST_CONFIGURATION_LOCATION = "tempto-configuration-local.yaml";

    private TestConfigurationFactory() {}

    public static Configuration createTestConfiguration()
    {
        Configuration configuration = new HierarchicalConfiguration(readTestConfiguration(), readLocalConfiguration());
        return new ConfigurationVariableResolver().resolve(configuration);
    }

    private static Configuration readTestConfiguration()
    {
        String testConfigurationUri = System.getProperty(TEST_CONFIGURATION_URI_KEY, DEFAULT_TEST_CONFIGURATION_LOCATION);
        Optional<InputStream> testConfigurationStream = getConfigurationInputStream(testConfigurationUri);
        if (!testConfigurationStream.isPresent()) {
            throw new IllegalArgumentException("Unable find to configuration: " + testConfigurationUri);
        }
        return parseConfiguration(testConfigurationStream.get());
    }

    private static Configuration readLocalConfiguration()
    {
        String configurationLocation = System.getProperty(LOCAL_TEST_CONFIGURATION_URI_KEY, DEFAULT_LOCAL_TEST_CONFIGURATION_LOCATION);
        Optional<InputStream> localTestConfigurationStream = getConfigurationInputStream(configurationLocation);
        if (localTestConfigurationStream.isPresent()) {
            InputStream testConfigurationStream = localTestConfigurationStream.get();
            return parseConfiguration(testConfigurationStream);
        }
        else {
            LOGGER.info("Default configuration is being used. Local configuration is absent: {}", configurationLocation);
            return emptyConfiguration();
        }
    }

    private static Optional<InputStream> getConfigurationInputStream(String location)
    {
        Optional<InputStream> inputStream = getFileInputStream(location);
        if (!inputStream.isPresent()) {
            inputStream = getResourceInputStream(location);
        }
        return inputStream;
    }

    private static Optional<InputStream> getFileInputStream(String path)
    {
        try {
            return Optional.of(new FileInputStream(path));
        }
        catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    private static Optional<InputStream> getResourceInputStream(String path)
    {
        if(!path.startsWith("/")) {
            path = "/" + path;
        }
        return Optional.ofNullable(TestConfigurationFactory.class.getResourceAsStream(path));
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

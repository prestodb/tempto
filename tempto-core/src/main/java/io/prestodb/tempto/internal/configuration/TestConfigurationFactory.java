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
package io.prestodb.tempto.internal.configuration;

import com.google.common.base.Splitter;
import io.prestodb.tempto.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static io.prestodb.tempto.internal.configuration.EmptyConfiguration.emptyConfiguration;

public class TestConfigurationFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestConfigurationFactory.class);

    public static final String TEST_CONFIGURATION_URIS_KEY = "tempto.configurations";
    public static final String DEFAULT_TEST_CONFIGURATION_LOCATION = "tempto-configuration.yaml";

    private static Configuration configuration = null;

    private TestConfigurationFactory() {}

    public static synchronized Configuration testConfiguration()
    {
        if (configuration == null) {
            configuration = createTestConfiguration();
        }
        return configuration;
    }

    // Exposed only for tests
    @Deprecated
    static Configuration createTestConfiguration()
    {
        return new ConfigurationVariableResolver().resolve(readTestConfiguration());
    }

    private static Configuration readTestConfiguration()
    {
        String testConfigurationUris = System.getProperty(TEST_CONFIGURATION_URIS_KEY, DEFAULT_TEST_CONFIGURATION_LOCATION);
        LOGGER.info("Reading configuriontion from {}", testConfigurationUris);
        Configuration configuration = emptyConfiguration();
        for (String testConfigurationUri : Splitter.on(",").split(testConfigurationUris)) {
            Optional<InputStream> testConfigurationStream = getConfigurationInputStream(testConfigurationUri);
            if (!testConfigurationStream.isPresent()) {
                throw new IllegalArgumentException("Unable find to configuration: " + testConfigurationUri);
            }
            Configuration parsedConfiguration = parseConfiguration(testConfigurationStream.get());
            configuration = new HierarchicalConfiguration(configuration, parsedConfiguration);
        }
        return configuration;
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
        if (!path.startsWith("/")) {
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

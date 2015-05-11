/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.configuration;

import com.teradata.test.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static com.teradata.test.internal.configuration.EmptyConfiguration.emptyConfiguration;

public class TestConfigurationFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestConfigurationFactory.class);

    public static final String TEST_CONFIGURATION_URI_KEY = "test.configuration";
    public static final String LOCAL_TEST_CONFIGURATION_URI_KEY = "test.configuration.local";

    public static final String CLASSPATH_PROTOCOL = "classpath:";

    private static final String DEFAULT_TEST_CONFIGURATION_URI = CLASSPATH_PROTOCOL + "/test-configuration.yaml";
    private static final String LOCAL_DEFAULT_TEST_CONFIGURATION_URI = CLASSPATH_PROTOCOL + "/test-configuration-local.yaml";

    public static Configuration createTestConfiguration()
    {
        return new HierarchicalConfiguration(readTestConfiguration(), readLocalConfiguration());
    }

    private static Configuration readTestConfiguration()
    {
        String testConfigurationUri = System.getProperty(TEST_CONFIGURATION_URI_KEY, DEFAULT_TEST_CONFIGURATION_URI);
        Optional<InputStream> testConfigurationStream = getConfigurationInputStream(testConfigurationUri);
        if (!testConfigurationStream.isPresent()) {
            throw new IllegalArgumentException("test configuration URI is incorrect: " + testConfigurationUri);
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

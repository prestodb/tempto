/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class TestConfigurationFactory
{
    private static final String TEST_CONFIGURATION_URI_KEY = "test-configuration";
    private static final String DEFAULT_TEST_CONFIGURATION_URI = "classpath:/test-configuration.yaml";
    private static final String CLASSPATH_PROTOCOL = "classpath:";

    public static YamlConfiguration createTestConfiguration()
    {
        try (InputStream configurationInputStream = getConfigurationInputStream()) {
            return new YamlConfiguration(configurationInputStream);
        }
        catch (IOException e) {
            throw new RuntimeException("could not parse configuration file", e);
        }
    }

    private static InputStream getConfigurationInputStream()
    {
        String testConfigurationUri = System.getProperty(TEST_CONFIGURATION_URI_KEY, DEFAULT_TEST_CONFIGURATION_URI);
        if (testConfigurationUri.startsWith(CLASSPATH_PROTOCOL)) {
            InputStream input = YamlConfiguration.class.getResourceAsStream(testConfigurationUri.substring(CLASSPATH_PROTOCOL.length()));
            if (input == null) {
                throw new IllegalArgumentException("test configuration URI is incorrect: " + testConfigurationUri);
            }
            return input;
        }
        else {
            try {
                return new URL(testConfigurationUri).openConnection().getInputStream();
            }
            catch (IOException e) {
                throw new IllegalArgumentException("test configuration URI is incorrect: " + testConfigurationUri);
            }
        }
    }
}

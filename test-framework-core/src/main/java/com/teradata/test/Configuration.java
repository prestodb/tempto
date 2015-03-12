/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test;

import java.util.Optional;

public interface Configuration
{
    Optional<String> getString(String key);

    String getStringMandatory(String key);

    String getStringMandatory(String key, String errorMessage);
}

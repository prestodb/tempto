/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class ConventionTestsUtils
{
    private static final String SQL_TESTS_PATH_PART = "sql-tests";

    private ConventionTestsUtils()
    {
    }

    public static Optional<Path> getConventionsTestsPath(String child)
    {
        URL productTestURI = ClassLoader.getSystemResource(SQL_TESTS_PATH_PART + "/" + child);
        if (productTestURI == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Paths.get(productTestURI.toURI()));
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}

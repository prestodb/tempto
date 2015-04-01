/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.recursion;

import com.teradata.test.internal.convention.ConventionBasedTest;
import com.teradata.test.internal.convention.ConventionBasedTestFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RecursionPathTestFactory
        implements ConventionBasedTestFactory.PathTestFactory
{

    @Override
    public boolean isSupportedPath(Path path)
    {
        return Files.isDirectory(path);
    }

    @Override
    public List<ConventionBasedTest> createTestsForPath(Path path, String testNamePrefix, ConventionBasedTestFactory factory)
    {
        String newPrefix = testNamePrefix + "." + path.getFileName();
        return factory.createTestsForChildrenOfPath(path, newPrefix);
    }

}

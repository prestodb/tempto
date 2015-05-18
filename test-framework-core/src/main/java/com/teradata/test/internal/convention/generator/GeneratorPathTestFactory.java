/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.generator;

import com.teradata.test.internal.convention.ConventionBasedTest;
import com.teradata.test.internal.convention.ConventionBasedTestFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.io.Files.createTempDir;
import static com.teradata.test.internal.convention.ProcessUtils.execute;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.getExtension;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.getFilenameWithoutExtension;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.makeExecutable;

public class GeneratorPathTestFactory
        implements ConventionBasedTestFactory.PathTestFactory
{

    private static final String GENERATOR_SCRIPT_EXTENSION = "generator";

    @Override
    public boolean isSupportedPath(Path path)
    {
        return GENERATOR_SCRIPT_EXTENSION.equals(getExtension(path));
    }

    @Override
    public List<ConventionBasedTest> createTestsForPath(Path path, String testNamePrefix, ConventionBasedTestFactory factory)
    {
        String newPrefix = testNamePrefix + "." + getFilenameWithoutExtension(path);
        File tmpTestsDirectory = createTempDir();
        tmpTestsDirectory.deleteOnExit();
        makeExecutable(path);
        execute(path.toFile().toString(), tmpTestsDirectory.toString());
        return factory.createTestsForChildrenOfPath(tmpTestsDirectory.toPath(), newPrefix);
    }
}

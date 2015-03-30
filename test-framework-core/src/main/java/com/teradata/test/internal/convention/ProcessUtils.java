/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.convention;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;

public final class ProcessUtils
{
    private static final int SUCCESS_EXIT_CODE = 0;

    public static void execute(String... cmdarray)
    {
        checkState(cmdarray.length > 0);

        try {
            Process process = Runtime.getRuntime().exec(cmdarray);
            process.waitFor();
            checkState(process.exitValue() == SUCCESS_EXIT_CODE, "%s exited with status code: %s", cmdarray[0], process.exitValue());
        }
        catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private ProcessUtils()
    {
    }
}

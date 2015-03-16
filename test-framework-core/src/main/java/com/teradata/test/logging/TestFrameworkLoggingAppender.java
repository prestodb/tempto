/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.logging;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkState;

/**
 * This class is a custom log appender that is responsible for two things:
 * <p>
 * <ol>
 * <li>Writing out messages to the console when they meet the minimum level</li>
 * <li>Writing out messages to a per-test file when they meet the minimum level</li>
 * </ol>
 * <p>
 * For now, the console and file levels are defined statically, as well as the output patterns.
 */
public class TestFrameworkLoggingAppender
        extends AppenderSkeleton
{
    private static final PatternLayout CONSOLE_OUTPUT_FORMAT = new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss}] %m%n");
    private static final Level MINIMUM_CONSOLE_LEVEL = Level.INFO;

    private static final PatternLayout FILE_OUTPUT_FORMAT = new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss}] [%p] %c{10}: %m%n");
    private static final Level MINIMUM_FILE_LEVEL = Level.TRACE;

    private LoadingCache<String, PrintWriter> printWriterCache = buildPrintWriterCache();

    private LoadingCache<String, PrintWriter> buildPrintWriterCache()
    {
        return CacheBuilder.newBuilder()
                .maximumSize(20)
                .removalListener((RemovalNotification<String, PrintWriter> rn) -> rn.getValue().close())
                .build(new CacheLoader<String, PrintWriter>()
                {
                    @Override
                    public PrintWriter load(String fileName)
                            throws Exception
                    {
                        return new PrintWriter(new FileOutputStream(fileName, true));
                    }
                });
    }

    @Override
    protected void append(LoggingEvent event)
    {
        checkState(!closed, "Cannot append to a closed TestFrameworkLoggingAppender");

        Level eventLevel = event.getLevel();

        ifAboveMinimumWriteToConsole(CONSOLE_OUTPUT_FORMAT.format(event), eventLevel);
        ifAboveMinimumWriteToFile(FILE_OUTPUT_FORMAT.format(event), eventLevel);

        String[] throwableStack = event.getThrowableStrRep();
        if (throwableStack != null) {
            for (String throwableElement : throwableStack) {
                if (CONSOLE_OUTPUT_FORMAT.ignoresThrowable()) {
                    ifAboveMinimumWriteToConsole(throwableElement, eventLevel);
                }
                if (FILE_OUTPUT_FORMAT.ignoresThrowable()) {
                    ifAboveMinimumWriteToFile(throwableElement, eventLevel);
                }
            }
        }
    }

    private void ifAboveMinimumWriteToConsole(String message, Level level)
    {
        if (level.isGreaterOrEqual(MINIMUM_CONSOLE_LEVEL)) {
            System.out.print(message);
        }
    }

    private void ifAboveMinimumWriteToFile(String message, Level level)
    {
        if (level.isGreaterOrEqual(MINIMUM_FILE_LEVEL)) {
            PrintWriter printWriter = getFilePrintWriterForCurrentTest();
            printWriter.print(message);
            printWriter.flush();
        }
    }

    private PrintWriter getFilePrintWriterForCurrentTest()
    {
        return getFilePrintWriter(getCurrentTestLogFileName());
    }

    private PrintWriter getFilePrintWriter(String fileName)
    {
        return printWriterCache.getUnchecked(fileName);
    }

    private static String getCurrentTestLogFileName()
    {
        // todo obtain proper one
        return "/tmp/test_framework_output_file.log";
    }

    @Override
    public void close()
    {
        closed = true;
        printWriterCache.invalidateAll();
        printWriterCache.cleanUp();
    }

    @Override
    public boolean requiresLayout()
    {
        return false;
    }
}



/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.logging;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.inject.ConfigurationException;
import com.teradata.test.internal.TestInfo;
import com.teradata.test.context.TestContext;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.Files.createParentDirs;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContextIfSet;
import static org.apache.commons.io.FileUtils.getTempDirectoryPath;

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
    private static final PatternLayout CONSOLE_OUTPUT_FORMAT = new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss}] [%X{test_id}] %m%n");
    private static final Level MINIMUM_CONSOLE_LEVEL = Level.INFO;

    private static final PatternLayout FILE_OUTPUT_FORMAT = new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss}] [%p] %c{10}: %m%n");
    private static final Level MINIMUM_FILE_LEVEL = Level.TRACE;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'_'HH-mm-ss");

    private LoadingCache<String, PrintWriter> printWriterCache = buildPrintWriterCache();

    private final String logsDirectory = selectLogsDirectory();
    private Date markerTime = new Date();

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
                        File file = new File(fileName);
                        createParentDirs(file);
                        return new PrintWriter(new FileOutputStream(file, true));
                    }
                });
    }

    private String selectLogsDirectory()
    {
        return getTempDirectoryPath() + "/testlogs/" + DATE_FORMAT.format(new Date());
    }

    @Override
    protected void append(LoggingEvent event)
    {
        checkState(!closed, "Cannot append to a closed TestFrameworkLoggingAppender");

        if (shouldWriteToConsole(event)) {
            writeToConsole(CONSOLE_OUTPUT_FORMAT.format(event));
            String[] throwableStack = event.getThrowableStrRep();
            if (throwableStack != null) {
                for (String throwableElement : throwableStack) {
                    if (CONSOLE_OUTPUT_FORMAT.ignoresThrowable()) {
                        writeToConsole(formatThrowableElement(throwableElement));
                    }
                }
            }
        }

        if (shouldWriteToFile(event)) {
            writeToFile(FILE_OUTPUT_FORMAT.format(event));
            String[] throwableStack = event.getThrowableStrRep();
            if (throwableStack != null) {
                for (String throwableElement : throwableStack) {
                    if (FILE_OUTPUT_FORMAT.ignoresThrowable()) {
                        writeToFile(formatThrowableElement(throwableElement));
                    }
                }
            }
        }
    }

    private String formatThrowableElement(String throwableElement) {return "   " + throwableElement + "\n";}

    private boolean shouldWriteToConsole(LoggingEvent loggingEvent)
    {
        return loggingEvent.getLevel().isGreaterOrEqual(MINIMUM_CONSOLE_LEVEL);
    }

    private void writeToConsole(String message)
    {
        System.out.print(message);
    }

    private boolean shouldWriteToFile(LoggingEvent loggingEvent)
    {
        return loggingEvent.getLevel().isGreaterOrEqual(MINIMUM_FILE_LEVEL);
    }

    private void writeToFile(String message)
    {
        Optional<PrintWriter> printWriter = getFilePrintWriterForCurrentTest();
        if (printWriter.isPresent()) {
            printWriter.get().print(message);
            printWriter.get().flush();
        }
    }

    private Optional<PrintWriter> getFilePrintWriterForCurrentTest()
    {
        Optional<String> currentTestLogFileName = getCurrentTestLogFileName();
        if (currentTestLogFileName.isPresent()) {
            return Optional.of(getFilePrintWriter(currentTestLogFileName.get()));
        }
        else {
            return Optional.empty();
        }
    }

    private PrintWriter getFilePrintWriter(String fileName)
    {
        return printWriterCache.getUnchecked(fileName);
    }

    private Optional<String> getCurrentTestLogFileName()
    {
        Optional<TestContext> testContext = testContextIfSet();
        try {
            String testName = "SUITE";
            if (testContext.isPresent()) {
                Optional<TestInfo> testInfo = testContext.get().getOptionalDependency(TestInfo.class);
                if (testInfo.isPresent()) {
                    testName = testInfo.get().getTestName();
                }
            }
            return Optional.of(logsDirectory + "/" + testName + "_" + DATE_FORMAT.format(markerTime));
        }
        catch (ConfigurationException e) {
            System.err.append("Could not load TestInfo");
            return Optional.empty();
        }
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

    /**
     * Returns logs directory for configured TestFrameworkLoggingAppender.
     */
    public static Optional<String> getSelectedLogsDirectory()
    {
        Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            Appender appender = (Appender) allAppenders.nextElement();
            if (appender instanceof TestFrameworkLoggingAppender) {
                return Optional.of(((TestFrameworkLoggingAppender) appender).logsDirectory);
            }
        }
        return Optional.empty();
    }
}



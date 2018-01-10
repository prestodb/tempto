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

package io.prestodb.tempto.logging;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.inject.ConfigurationException;
import io.prestodb.tempto.context.TestContext;
import io.prestodb.tempto.internal.listeners.TestMetadata;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
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
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.testContextIfSet;
import static org.apache.commons.io.FileUtils.getTempDirectoryPath;

/**
 * This class is a custom log appender that is responsible for two things:
 * <ol>
 * <li>Writing out messages to the console when they meet the minimum level</li>
 * <li>Writing out messages to a per-test file when they meet the minimum level</li>
 * </ol>
 * For now, the console and file levels are defined statically, as well as the output patterns.
 */
public class TestFrameworkLoggingAppender
        extends AppenderSkeleton
{
    private static final PatternLayout DEFAULT_FILE_OUTPUT_FORMAT = new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss}] [%p] %c{10}: %m%n");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'_'HH-mm-ss");

    private final LoadingCache<String, PrintWriter> printWriterCache = buildPrintWriterCache();
    private final String logsDirectory = selectLogsDirectory();

    public TestFrameworkLoggingAppender()
    {
        setLayout(DEFAULT_FILE_OUTPUT_FORMAT);
    }

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

    private String getRootLogsDirectory()
    {
        String userLogsDir = System.getProperty("io.prestodb.tempto.root.logs.dir");
        if (userLogsDir != null) {
            return userLogsDir;
        }
        else {
            return getTempDirectoryPath() + "/tempto_logs";
        }
    }

    private String selectLogsDirectory()
    {
        return getRootLogsDirectory() + "/" + DATE_FORMAT.format(new Date());
    }

    @Override
    protected void append(LoggingEvent event)
    {
        checkState(!closed, "Cannot append to a closed TestFrameworkLoggingAppender");

        writeToFile(getLayout().format(event));
        String[] throwableStack = event.getThrowableStrRep();
        if (throwableStack != null) {
            for (String throwableElement : throwableStack) {
                if (getLayout().ignoresThrowable()) {
                    writeToFile(formatThrowableElement(throwableElement));
                }
            }
        }
    }

    private String formatThrowableElement(String throwableElement) {return "   " + throwableElement + "\n";}

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
                Optional<TestMetadata> testMetadata = testContext.get().getOptionalDependency(TestMetadata.class);
                if (testMetadata.isPresent()) {
                    testName = testMetadata.get().testName;
                }
            }
            return Optional.of(logsDirectory + "/" + testName);
        }
        catch (ConfigurationException e) {
            System.err.append("Could not load TestMetadata from guice context");
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
        return true;
    }

    /**
     * @return logs directory for configured TestFrameworkLoggingAppender
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



/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.sql;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for generating expected result files for the product test framework.
 */
public class SqlResultGenerator {
  private static final String EXPECTED_SUFFIX = ".result";
  private static final String HELP = "help";

  private static final Logger LOGGER = LoggerFactory.getLogger(SqlResultGenerator.class);

  private File testFile;
  private Properties properties;
  private JdbcConnection connection;

  /**
   * The easiest way to generate expected results is to use
   * the python front-end (generate_results.py).
   *
   * @param args
   */
  public static void main(String[] args) {
    Options options = configureCommandLineParser();
    try {
      CommandLineParser parser = new BasicParser();
      CommandLine commandLine = parser.parse(options, args);
      if (commandLine.hasOption(HELP)) {
        usage(options);
        return;
      }
      String propertiesFileName = commandLine.getOptionValue("p");
      String testFileName = commandLine.getOptionValue("s");
      SqlResultGenerator resultGenerator = new SqlResultGenerator(testFileName, propertiesFileName);
      resultGenerator.generateExpectedResults();
    } catch (ParseException e) {
      usage(options);
    } catch (Exception e) {
      LOGGER.error("Caught exception in main", e);
    }
    System.exit(99);
  }

  /**
   * @param options as returned by configureCommandLineParser()
   */
  private static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    String header = "The easiest way to generate expected results is to use the python front-end (generate_results.py).";
    formatter.printHelp("SqlTestRunner", header, options, "", true);
  }

  /**
   * @param testFileName       SQL test file to be executed.
   * @param propertiesFileName Properties file containing information about how to connect to the database.
   *
   * @throws IOException            if either of the files is not readable.
   * @throws SQLException           if a database connection cannot be established.
   */
  public SqlResultGenerator(String testFileName, String propertiesFileName) throws IOException, SQLException {
    connection = new JdbcConnection(propertiesFileName);
    connection.connect();
    testFile = new File(testFileName);
    try (BufferedReader propertiesFileReader =
            new BufferedReader(new FileReader(new File(propertiesFileName)))) {
      properties = new Properties();
      properties.load(propertiesFileReader);
    }
  }

  private void generateExpectedResults() {
    LOGGER.info("Reading {}", testFile);
    String testFileName = testFile.toString();
    int dot = testFileName.lastIndexOf('.');
    testFileName = testFileName.substring(0,dot);
    File resultFile = new File(testFileName + EXPECTED_SUFFIX);
    if (resultFile.exists()) {
      LOGGER.warn("Result file {} exists.  Skipping...", resultFile.toString());
    } else {
      StringBuilder query = new StringBuilder();
      try (BufferedReader testFileReader = new BufferedReader(new FileReader(testFile))) {
        String line;
        while ((line = testFileReader.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty() || line.startsWith("--")) {
            continue;
          }
          if (query.length() > 0) {
            query.append(' ');
          }
          if (line.contains(";")) {
            int semicolon = line.lastIndexOf(';');
            query.append(line.substring(0, semicolon));
            LOGGER.info("Executing query: {}", query.toString());
            connection.executeQueryToFile(query.toString(), resultFile);
            // Test framework only allows one query per file, so we're done.
            break;
          }
          else {
            query.append(line);
          }
        }
      }
      catch (SQLException e) {
        LOGGER.error("Executing: {}", query, e);
      }
      catch (IOException e) {
        LOGGER.error("Reading: {}", testFile, e);
      }
    }
  }

  private static Options configureCommandLineParser() {
    Options options = new Options();

    Option propertiesOption = new Option("p", true, "Properties file");
    propertiesOption.setRequired(true);

    Option sqlFileOption = new Option("s", true, "SQL file");
    sqlFileOption.setRequired(true);

    Option helpOption = new Option(HELP, "Print this message");

    options.addOption(propertiesOption);
    options.addOption(sqlFileOption);
    options.addOption(helpOption);

    return options;
  }
}

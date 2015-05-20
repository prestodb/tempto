/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */


package com.teradata.test.sql;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A convenience class for executing SQL queries via JDBC.
 */
public class JdbcConnection {
  private static final Logger LOGGER = LoggerFactory.getLogger(JdbcConnection.class);
  public static final String RESULT = "result";

  private String jdbcUrl;
  private String user;
  private String password;

  private Connection conn = null;
  private Statement stmt = null;
  private Properties properties = null;

  protected static final String JDBC_URL = "jdbc_url";
  protected static final String JDBC_USER = "user";
  protected static final String JDBC_PASSWORD = "password";

  public JdbcConnection(String propertiesFileName) throws IOException {
    BufferedReader propertiesFileReader = new BufferedReader(new FileReader(new File(propertiesFileName)));

    try {
      properties = new Properties();
      properties.load(propertiesFileReader);
    }finally {
      propertiesFileReader.close();
    }

    String url = properties.getProperty(JDBC_URL);
    String user = properties.getProperty(JDBC_USER);
    String password = properties.getProperty(JDBC_PASSWORD);

    initialize(url, user, password);
  }

  protected void initialize(String jdbcUrl, String user, String password) {
    LOGGER.info("url {}", jdbcUrl);
    LOGGER.info("user {}", user);
    LOGGER.info("password {}", password);

    this.jdbcUrl = jdbcUrl;
    this.user = user;
    this.password = password;
  }

  public JdbcConnection(Properties properties) {
    String url = properties.getProperty(JDBC_URL);
    String user = properties.getProperty(JDBC_USER);
    String password = properties.getProperty(JDBC_PASSWORD);

    initialize(url, user, password);
  }

  /**
   * @param jdbcUrl    jdbc:hadapt://[host]:[port]/[database]
   * @param user       Database user name
   * @param password   Database password
   */
  public JdbcConnection(String jdbcUrl, String user, String password) {
    initialize(jdbcUrl, user, password);
  }

  public void connect() throws SQLException {
    LOGGER.info("Connecting to database...");
    conn = DriverManager.getConnection(jdbcUrl, user, password);
    stmt = conn.createStatement();
  }

  public void close() {
    LOGGER.info("Closing database connection.");
    try {
      stmt.close();
      stmt = null;
    } catch (SQLException e) {
      LOGGER.error("Can't close statement.", e);
    }
    try {
      conn.close();
      conn = null;
    } catch (SQLException e) {
      LOGGER.error("Can't close connection.", e);
    }
  }

  private String rowToString(ResultSet rs)  throws SQLException {
    ResultSetMetaData meta = rs.getMetaData();
    int colCount = meta.getColumnCount();
    StringBuilder line = new StringBuilder();
    for (int i = 1; i <= colCount; i++) {
      String column = rs.getString(i);
      if (column == null) {
        column = "null";
      } else {
        int type = meta.getColumnType(i);
        if (type == Types.CHAR || type == Types.VARCHAR) {
          column = column.replace("|", "\\|");
        }
      }
      line.append(column);
      line.append("|");
    }
    line.append("\n");
    return line.toString();
  }

  /**
   * @param sql  Query to be executed.
   * @param resultFile  If this file exists, it will be overwritten.
   *
   * @throws SQLException
   */
  public void executeQueryToFile(String sql, File resultFile) throws SQLException, IOException {
    if (!stmt.execute(sql)) {
      LOGGER.warn("No result set from query: {}", sql);
      return;
    }

    try (
            ResultSet rs = stmt.getResultSet();
            BufferedWriter resultFileWriter = new BufferedWriter(new FileWriter(resultFile));
    ) {
        if (rs == null) {
          LOGGER.warn("NULL result set from query: {}", sql);
        } else {
          resultFileWriter.write("-- delimiter: |; ignoreOrder: true;\n");
          while (rs.next()) {
            resultFileWriter.write(rowToString(rs));
          }
        }
    } catch (IOException e) {
      LOGGER.error("Error writing result file", e);
      throw e;
    }
  }
}

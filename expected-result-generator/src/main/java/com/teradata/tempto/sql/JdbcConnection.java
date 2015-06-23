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

package com.teradata.tempto.sql;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Integer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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
  private int timeout;

  private Connection conn = null;
  private Statement stmt = null;
  private Properties properties = null;

  protected static final String JDBC_URL = "jdbc_url";
  protected static final String JDBC_USER = "user";
  protected static final String JDBC_PASSWORD = "password";
  protected static final String JDBC_TIMEOUT = "timeout";

  public JdbcConnection(String propertiesFileName) throws IOException {
    BufferedReader propertiesFileReader = new BufferedReader(new FileReader(new File(propertiesFileName)));

    try {
      properties = new Properties();
      properties.load(propertiesFileReader);
    }finally {
      propertiesFileReader.close();
    }

    jdbcUrl = properties.getProperty(JDBC_URL);
    user = properties.getProperty(JDBC_USER);
    password = properties.getProperty(JDBC_PASSWORD);
    timeout = Integer.parseInt(properties.getProperty(JDBC_TIMEOUT, "600"));

    LOGGER.info("url {}", jdbcUrl);
    LOGGER.info("user {}", user);
    LOGGER.info("password {}", password);
    LOGGER.info("timeout {}", timeout);
  }

   public void connect() throws SQLException {
     LOGGER.info("Connecting to database...");
     conn = DriverManager.getConnection(jdbcUrl, user, password);
     stmt = conn.createStatement();
     stmt.setQueryTimeout(timeout);
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

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

package io.prestodb.tempto.internal.query;

import io.prestodb.tempto.assertions.QueryAssert.Row;

import javax.xml.bind.DatatypeConverter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static io.prestodb.tempto.assertions.QueryAssert.Row.row;
import static java.util.Objects.requireNonNull;

/**
 * This class transforms string values to Java types based on column types.
 */
public class QueryRowMapper
{
    public static final String NULL_STRING = "null";

    private final List<JDBCType> columnTypes;

    public QueryRowMapper(List<JDBCType> columnTypes)
    {
        this.columnTypes = columnTypes;
    }

    public Row mapToRow(List<String> values)
    {
        checkState(values.size() == columnTypes.size(), "Expected %s values in row: %s", columnTypes.size(), values);

        Object[] rowValues = new Object[values.size()];
        for (int i = 0; i < values.size(); ++i) {
            String value = values.get(i);
            JDBCType type = columnTypes.get(i);

            rowValues[i] = convertValue(value, type);
        }

        return row(rowValues);
    }

    private Object convertValue(String value, JDBCType expectedType)
    {
        requireNonNull(value, "value is null");

        if (NULL_STRING.equals(value)) {
            return null;
        }

        switch (expectedType) {
            case CHAR:
            case VARCHAR:
            case LONGVARCHAR:
            case LONGNVARCHAR:
                return value;
            case BINARY:
            case VARBINARY:
            case LONGVARBINARY:
                return convertBinary(value);
            case BOOLEAN:
                return convertBoolean(value);
            case BIT:
                return convertBit(value);
            case TINYINT:
            case SMALLINT:
            case INTEGER:
                return Integer.valueOf(value);
            case BIGINT:
                return Long.valueOf(value);
            case REAL:
            case FLOAT:
            case DOUBLE:
                return Double.valueOf(value);
            case DECIMAL:
            case NUMERIC:
                return new BigDecimal(value);
            case DATE:
                return Date.valueOf(value);
            case TIME:
            case TIME_WITH_TIMEZONE:
                return Time.valueOf(value);
            case TIMESTAMP:
            case TIMESTAMP_WITH_TIMEZONE:
                return Timestamp.valueOf(value);
            default:
                throw unsupportedConversionException(value, expectedType);
        }
    }

    private byte[] convertBinary(String value)
    {
        return DatatypeConverter.parseHexBinary(value);
    }

    private Boolean convertBoolean(String value)
    {
        if (value.equalsIgnoreCase(Boolean.TRUE.toString())) {
            return true;
        }
        else if (value.equalsIgnoreCase(Boolean.FALSE.toString())) {
            return false;
        }
        throw unsupportedConversionException(value, JDBCType.BOOLEAN);
    }

    private Boolean convertBit(String value)
    {
        if (value.equals("0")) {
            return false;
        }
        else if (value.equals("1")) {
            return true;
        }
        throw unsupportedConversionException(value, JDBCType.BIT);
    }

    private IllegalArgumentException unsupportedConversionException(String value, JDBCType type)
    {
        throw new IllegalArgumentException("Unsupported JDBC type conversion, type: " + type + ", value: " + value);
    }
}

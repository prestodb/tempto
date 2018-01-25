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

import com.google.common.base.Throwables;
import com.google.common.math.DoubleMath;
import com.google.common.primitives.UnsignedBytes;
import io.prestodb.tempto.configuration.Configuration;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * Comparator for query result values. It should be instantiated per column with given JDBCType.
 * This comparator should only be used for equality comparison. It tries to "non-strictly"
 * compare values by upcasting numerical values to long/double.
 */
public class QueryResultValueComparator
        implements Comparator<Object>
{
    public static final String FLOAT_TOLERANCE_CONFIGURATION_KEY = "tests.assert.float_tolerance";

    private final JDBCType type;
    private final Configuration configuration;

    private QueryResultValueComparator(JDBCType type, Configuration configuration)
    {
        this.type = requireNonNull(type, "type is null");
        this.configuration = requireNonNull(configuration, "configuration is null");
    }

    public static QueryResultValueComparator comparatorForType(JDBCType type, Configuration configuration)
    {
        return new QueryResultValueComparator(type, configuration);
    }

    @Override
    public int compare(Object actual, Object expected)
    {
        if (isNull(actual) && isNull(expected)) {
            return 0;
        }
        if (isNull(actual) != isNull(expected)) {
            return -1;
        }
        switch (type) {
            case CHAR:
            case VARCHAR:
            case LONGVARCHAR:
            case LONGNVARCHAR:
                return stringsEqual(actual, expected);
            case BINARY:
            case VARBINARY:
            case LONGVARBINARY:
                return binaryEqual(actual, expected);
            case BIT:
            case BOOLEAN:
                return booleanEqual(actual, expected);
            case TINYINT:
            case SMALLINT:
            case INTEGER:
                return integerEqual(actual, expected);
            case BIGINT:
                return longEqual(actual, expected);
            case REAL:
            case FLOAT:
                return floatingEqual(actual, expected);
            case DOUBLE:
                return floatingEqual(actual, expected);
            case DECIMAL:
            case NUMERIC:
                return bigDecimalEqual(actual, expected);
            case DATE:
                return dateEqual(actual, expected);
            case TIME:
            case TIME_WITH_TIMEZONE:
                return timeEqual(actual, expected);
            case TIMESTAMP:
            case TIMESTAMP_WITH_TIMEZONE:
                return timestampEqual(actual, expected);
            case ARRAY:
                return arrayEqual(actual, expected);
            default:
                throw new RuntimeException("Unsupported sql type " + type);
        }
    }

    private int arrayEqual(Object actual, Object expected)
    {
        if (!(actual instanceof Array && expected instanceof List)) {
            return -1;
        }
        Array actualArray = (Array) actual;
        QueryResultValueComparator elementComparator;
        elementComparator = comparatorForArrayElements(actualArray);
        List actualList = arrayAsList(actualArray);
        List expectedList = (List) expected;

        if (actualList.size() != expectedList.size()) {
            return -1;
        }

        for (int i = 0; i < actualList.size(); ++i) {
            Object actualValue = actualList.get(i);
            Object expectedValue = expectedList.get(i);
            int compareResult = elementComparator.compare(actualValue, expectedValue);
            if (compareResult != 0) {
                return compareResult;
            }
        }
        return 0;
    }

    private QueryResultValueComparator comparatorForArrayElements(Array actualArray)
    {
        QueryResultValueComparator elementComparator;
        try {
            elementComparator = comparatorForType(JDBCType.valueOf(actualArray.getBaseType()), configuration);
        }
        catch (SQLException e) {
            throw Throwables.propagate(e);
        }
        return elementComparator;
    }

    private List arrayAsList(Array array)
    {
        try {
            return Arrays.asList((Object[]) array.getArray());
        }
        catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    private int stringsEqual(Object actual, Object expected)
    {
        if (actual instanceof String && expected instanceof String) {
            return ((String) actual).compareTo((String) expected);
        }
        return -1;
    }

    private int binaryEqual(Object actual, Object expected)
    {
        if (actual instanceof byte[] && expected instanceof byte[]) {
            return UnsignedBytes.lexicographicalComparator().compare((byte[]) actual, (byte[]) expected);
        }
        return -1;
    }

    private int booleanEqual(Object actual, Object expected)
    {
        if (actual instanceof Boolean && expected instanceof Boolean) {
            return ((Boolean) actual).compareTo((Boolean) expected);
        }
        return -1;
    }

    private int longEqual(Object actual, Object expected)
    {
        if (!(isLongOrNarrower(actual) && isLongOrNarrower(expected))) {
            return -1;
        }
        Long actualLong = ((Number) actual).longValue();
        Long expectedLong = ((Number) expected).longValue();
        return actualLong.compareTo(expectedLong);
    }

    private int integerEqual(Object actual, Object expected)
    {
        if (!(isIntegerOrNarrower(actual) && isIntegerOrNarrower(expected))) {
            return -1;
        }
        Integer actualInteger = ((Number) actual).intValue();
        Integer expectedInteger = ((Number) expected).intValue();
        return actualInteger.compareTo(expectedInteger);
    }

    private int floatingEqual(Object actual, Object expected)
    {
        if (!(isFloatingPointValue(actual) && isFloatingPointValue(expected))) {
            return -1;
        }

        double expectedDouble = getDoubleValue(expected);
        double tolerance = 0;
        Optional<Double> configTolerance = configuration.getDouble(FLOAT_TOLERANCE_CONFIGURATION_KEY);
        if (configTolerance.isPresent()) {
            tolerance = Math.abs(configTolerance.get() * expectedDouble);
        }
        return DoubleMath.fuzzyCompare(getDoubleValue(actual), expectedDouble, tolerance);
    }

    private int bigDecimalEqual(Object actual, Object expected)
    {
        if (actual instanceof BigDecimal && expected instanceof BigDecimal) {
            return ((BigDecimal) actual).compareTo((BigDecimal) expected);
        }
        return -1;
    }

    private int dateEqual(Object actual, Object expected)
    {
        if (actual instanceof Date && expected instanceof Date) {
            return ((Date) actual).compareTo((Date) expected);
        }
        return -1;
    }

    private int timeEqual(Object actual, Object expected)
    {
        if (actual instanceof Time && expected instanceof Time) {
            return ((Time) actual).compareTo((Time) expected);
        }
        return -1;
    }

    private int timestampEqual(Object actual, Object expected)
    {
        if (actual instanceof Timestamp && expected instanceof Timestamp) {
            return ((Timestamp) actual).compareTo((Timestamp) expected);
        }
        return -1;
    }

    private static boolean isLongOrNarrower(Object value)
    {
        return (value instanceof Long || isIntegerOrNarrower(value));
    }

    private static boolean isIntegerOrNarrower(Object value)
    {
        return (value instanceof Integer || value instanceof Short || value instanceof Byte);
    }

    private static boolean isFloatingPointValue(Object value)
    {
        return (value instanceof Float || value instanceof Double);
    }

    private static double getDoubleValue(Object object)
    {
        return ((Number) object).doubleValue();
    }
}

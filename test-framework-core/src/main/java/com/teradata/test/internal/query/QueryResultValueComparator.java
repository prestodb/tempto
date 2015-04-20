/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.query;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.UnsignedBytes;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Comparator;

import static java.util.Objects.isNull;

/**
 * Comparator for query result values. It should be instantiated per column with given JDBCType.
 * This comparator should only be used for equality comparison. It tries to "non-strictly"
 * compare values by upcasting numerical values to long/double.
 */
public class QueryResultValueComparator
        implements Comparator<Object>
{

    private static final double DOUBLE_FUZZY_MATCH_THRESHOLD = 0.00000000001;
    private final JDBCType type;

    private QueryResultValueComparator(JDBCType type)
    {
        this.type = type;
    }

    public static QueryResultValueComparator comparatorForType(JDBCType type)
    {
        return new QueryResultValueComparator(type);
    }

    @Override
    public int compare(Object actual, Object expected)
    {
        if (isNull(actual) && isNull(expected)) {
            return 0;
        }
        if (isNull(actual) ^ isNull(expected)) {
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
            case BIGINT:
                return longEqual(actual, expected);
            case REAL:
            case FLOAT:
            case DOUBLE:
                return doubleEqual(actual, expected);
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
            default:
                throw new RuntimeException("Unsupported sql type " + type);
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
        if (isIntegerValue(actual) && isIntegerValue(expected)) {
            Long actualLong = Long.valueOf(actual.toString());
            Long expectedLong = Long.valueOf(expected.toString());
            return actualLong.compareTo(expectedLong);
        }
        return -1;
    }

    private int doubleEqual(Object actual, Object expected)
    {
        if (isFloatingPointValue(actual) && isFloatingPointValue(expected)) {
            Double actualDouble = Double.valueOf(actual.toString());
            Double expectedDouble = Double.valueOf(expected.toString());
            Double threshold = expectedDouble * DOUBLE_FUZZY_MATCH_THRESHOLD;
            return DoubleMath.fuzzyCompare(actualDouble, expectedDouble, threshold);
        }
        return -1;
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

    private boolean isIntegerValue(Object value)
    {
        return (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte);
    }

    private boolean isFloatingPointValue(Object value)
    {
        return (value instanceof Float || value instanceof Double);
    }
}

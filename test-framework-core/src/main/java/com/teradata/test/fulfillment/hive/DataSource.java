/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import com.google.common.io.ByteSource;

import java.util.Collection;

/**
 * Responsible for providing data.
 */
public interface DataSource
{
    /**
     * @return path suffix where data source data should be stored
     */
    String getPathSuffix();

    /**
     * @return list with table files data. For each byte source separate file will be created on HDFS
     */
    Collection<ByteSource> data();

    /**
     * Revision marker is used to determine if data should be regenerated. This
     * method should be fast.
     *
     * @return revision marker
     */
    String revisionMarker();
}

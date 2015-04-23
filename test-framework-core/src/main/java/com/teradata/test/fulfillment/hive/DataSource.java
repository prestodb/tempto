/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import com.teradata.test.hadoop.hdfs.HdfsClient.RepeatableContentProducer;

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
     * @return collection with table files {@link RepeatableContentProducer}.
     * For each {@link RepeatableContentProducer} separate file will be created on HDFS
     */
    Collection<RepeatableContentProducer> data();

    /**
     * Revision marker is used to determine if data should be regenerated. This
     * method should be fast.
     *
     * @return revision marker
     */
    String revisionMarker();
}

/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

/**
 * Responsible for providing data for hive table to HDFS.
 */
public interface HiveDataSource
{
    /**
     * @return HDFS path
     */
    public String ensureDataOnHdfs();
}

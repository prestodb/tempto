/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.hadoop.hdfs;

import com.teradata.test.fulfillment.hive.DataSource;

public interface HdfsDataSourceWriter
{
    void ensureDataOnHdfs(String dataPath, DataSource dataSource);
}

/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.fulfillment.hive;

import com.teradata.test.fulfillment.hive.DataSource;

public interface HiveDataSourceWriter
{
    void ensureDataOnHdfs(String dataPath, DataSource dataSource);
}

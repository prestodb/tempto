/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.initialization.modules;

import com.google.inject.AbstractModule;
import com.teradata.test.fulfillment.hive.DefaultHiveDataSourceWriter;
import com.teradata.test.fulfillment.hive.HiveDataSourceWriter;
import com.teradata.test.hadoop.hdfs.HdfsClient;
import com.teradata.test.hadoop.hdfs.WebHDFSClient;

public class HadoopModule
        extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(HdfsClient.class).to(WebHDFSClient.class);
        bind(HiveDataSourceWriter.class).to(DefaultHiveDataSourceWriter.class);
    }
}

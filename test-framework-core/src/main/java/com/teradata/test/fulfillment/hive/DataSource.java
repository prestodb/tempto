/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import java.io.InputStream;

/**
 * Responsible for providing data.
 */
public interface DataSource
{

    /**
     * @return path where data source data should be stored
     */
    String getPath();

    /**
     * @return input stream of data
     */
    InputStream data();

    /**
     * Revision marker is used to determine if data should be regenerated. This
     * method should be fast.
     *
     * @return revision marker
     */
    String revisionMarker();
}

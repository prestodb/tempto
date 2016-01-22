package com.teradata.tempto.internal.hadoop.hdfs;


public class Main {

    public static void main(String[] args) {
        WebHDFSClient hdfsClient = new WebHDFSClient("master-locluster", 50070, "SPNEGO", "andrii@HADOOP.TERADATA.COM", "123456");
        long length = hdfsClient.getLength("/user/hive/warehouse", "hive");
        System.out.println(length);
    }
}

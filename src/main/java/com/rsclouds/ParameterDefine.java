package com.rsclouds;

import org.apache.hadoop.hbase.util.Bytes;

import com.rsclouds.util.ConfigProperty;

public class ParameterDefine {

	public static final String HDFSHOST_PATH = "hdfs://smaster02.data.com:8020/gt-MD5/";
	public static final String REDIS_HOST = ConfigProperty.getInstance()
			.getStringValue("redis.host");
	public static final int REDIS_PORT = ConfigProperty.getInstance()
			.getIntValue("redis.port");

	public static final String RESOURCE_TABLENAME = ConfigProperty
			.getInstance().getStringValue("meta.table");
	public static final byte[] RESOURCE_FAMILY = Bytes.toBytes("img");
	public static final byte[] RESOURCE_LINKS = Bytes.toBytes("links");
	public static final byte[] RESOURCE_DATA = Bytes.toBytes("data");

	public static final String META_TABLENAME = ConfigProperty.getInstance()
			.getStringValue("resource.table");
	public static final byte[] META_FAMILY = Bytes.toBytes("atts");
	public static final byte[] META_URL = Bytes.toBytes("url");
	public static final byte[] META_DFS = Bytes.toBytes("dfs");
	public static final byte[] META_SIZE = Bytes.toBytes("size");
	public static final byte[] META_TIME = Bytes.toBytes("time");
	public static final byte[] ONE = Bytes.toBytes("1");
	public static final byte[] ZERO = Bytes.toBytes("0");

}

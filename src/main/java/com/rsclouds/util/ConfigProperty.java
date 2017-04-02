package com.rsclouds.util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigProperty {
	private Properties properties;
	private static ConfigProperty instance = null;
	private static volatile Object obj = new Object();

	/**
	 * singleton
	 * 
	 * @return
	 */
	public static ConfigProperty getInstance() {
		synchronized (obj) {
			if (instance == null) {
				instance = new ConfigProperty();
			}
		}
		return instance;
	}

	public ConfigProperty() {
		properties = new Properties();
		try {
			InputStream fis = this.getClass().getClassLoader().getResourceAsStream("gtdata_api.properties");
			properties.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 返回 int值
	 * 
	 * @param key
	 * @return
	 */
	public Integer getIntValue(String key) {
		String val = properties.getProperty(key, "0");
		return Integer.valueOf(val);
	}

	/**
	 * 返回字符串
	 * 
	 * @param key
	 * @return
	 */
	public String getStringValue(String key) {
		return properties.getProperty(key, "").trim();
	}

	public static void main(String[] args) {
		System.out.println("redis.host = ["
				+ ConfigProperty.getInstance().getStringValue("redis.host")
				+ "]");
	}

}

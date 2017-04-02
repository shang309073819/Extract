package com.rsclouds.util;

import redis.clients.jedis.Jedis;
import com.rsclouds.ParameterDefine;
public class RedisUtils {	
	public static String replaceLast(String string, String toReplace,
			String replacement) {
		int pos = string.lastIndexOf(toReplace);
		if (pos > -1) {
			return string.substring(0, pos)
					+ replacement
					+ string.substring(pos + toReplace.length(),
							string.length());
		} else {
			return string;
		}
	}
	
	public static void redisCheck(String outputPath){
		outputPath=outputPath.substring(0, outputPath.lastIndexOf("//"));
		outputPath=replaceLast(outputPath,"/","//");
		@SuppressWarnings("resource")
		Jedis jedis = new Jedis(ParameterDefine.REDIS_HOST,ParameterDefine.REDIS_PORT);
		if(jedis.exists(outputPath)){
			String value = jedis.get(outputPath);
			if(value.endsWith("1,")){
				value = replaceLast(value,"1,","0,");
				jedis.set(outputPath, value);
			}
		}
	}
}

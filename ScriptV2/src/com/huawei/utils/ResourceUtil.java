package com.huawei.utils;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ResourceUtil {
	
	private static final Logger log = Logger.getLogger(ResourceUtil.class);
	private static Properties commonConfigProps = null;
	
	public static String getCommonProperty(String key) throws Exception {
		try {
			if (commonConfigProps == null) {
				InputStream input = ResourceUtil.class.getResourceAsStream(Constants.CONFIGFILEPATH);
				commonConfigProps = new Properties();
				commonConfigProps.load(input);
				input.close();
			}
			return commonConfigProps.getProperty(key);
		} catch (Exception e) {
			log.info("Get Property Error: ", e);
			throw new Exception(e);
		}
	}
}
package com.daas.common;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfFactory {

	public static Config conf = ConfigFactory.load();

	/**
	 * @return the conf
	 */
	public static Config getConf() {
		return conf;
	}
	
	/**
	 * @return the conf
	 */
	public static Config getPrivateConf() {
		
		Config config = ConfigFactory.load("awsCreds.properties");		
		return config;
	}
	
	public static Config getShellConfigEC2() {
		
		Config config = ConfigFactory.load("cmd.sh");		
		return config;
	}
	
}

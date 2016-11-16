package com.daas.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daas.exception.InputInvalidException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class 
 * @author Vivek
 *
 */
public class DaasUtil {

	private static final Logger logger = LoggerFactory.getLogger(DaasUtil.class);

	private static Gson gson = new GsonBuilder().serializeNulls().create();

	private static final String EMAIL_PATTERN =
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
					+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";


	/**
	 * validation check for null path params, throws input invalid exception for null values
	 * @throws InputInvalidException
	 */
	public static void checkForNull(Object object) throws Exception {

		HashMap<String,Object> map =
				new ObjectMapper().readValue(gson.toJson(object), HashMap.class);

		String keys[] = map.keySet().toArray(new String[0]);
		for(int i = 0 ; i < keys.length;++i) {
			if(map.get(keys[i]) == null){
				logger.warn("Parameter, "+keys[i]+" is null. For model - "+ object.getClass().getCanonicalName());
				throw new InputInvalidException(" No value for " + keys[i]+". For model - "+ object.getClass().getCanonicalName());
			}
		}
	}

	
	/**
	 * validation check for null path params, throws input invalid exception for null values
	 * @param map
	 * @throws InputInvalidException
	 */
	public static void checkForNull(Map<String,Object> map) throws Exception
	{

		String keys[] = map.keySet().toArray(new String[0]);
		for(int i = 0 ; i < keys.length;++i)
		{
			if(map.get(keys[i]) == null){
				logger.warn("Query parameter, "+keys[i]+" is null.");
				throw new InputInvalidException(" No value for " + keys[i]);				
			}
		}
	}

	/**
	 * Validate hex with regular expression
	 *
	 * @param hex
	 *            hex for validation
	 * @return true valid hex, false invalid hex
	 */
	public static boolean validEmail(final String hex) {

		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(hex);
		return matcher.matches();
	}

	/**
	 * Check if URL is valid or not
	 * @param url
	 * 				URL to validate
	 * @return true if URL valid, false if not
	 */
	public static boolean validURL(String url){
		
		UrlValidator urlValidator = new UrlValidator();
	    if (urlValidator.isValid(url)) {
	    	return true;
	    } 
		return false;
	}

}

package com.example.camelkafkaconsumer.demo.constants;

public final class Constants {

	private Constants() {
		
	}
	
    /** key value separator used in kafka url builder **/
    public static final String KEY_VALUE_SEPARATOR = "=";
    /** query parameter starting point in kafka url builder **/
    public static final String URL_QUERY_PARAM = "?";
    /** query parameters separator used in kafka url builder **/
    public static final String URL_QUERY_PARAMS_DELIMITER = "&";
    
    public static final String EVENT_HEALTH_CHECK = "doHealthCheck";
    
}
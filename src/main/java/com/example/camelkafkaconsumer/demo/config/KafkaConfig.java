package com.example.camelkafkaconsumer.demo.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.example.camelkafkaconsumer.demo.constants.Constants;

/**
 * Class to pick the properties defined for Kafka from application.properties and create a camel endpoint to call kafka
 * 
 * @author Srikant
 *
 */
@Configuration("kafkaConfig")
@ConfigurationProperties("kafka")
public class KafkaConfig {

	private String topic;
	
	private String errorTopic;
	
	private final Map<String, String> camelKafkaOptions = new HashMap<>();
	
	private String kafkaURL = "";
	
	private String kafkaErrorTopicURL = "";
	
	/**
	 * Forms the kafka url based on the configuration
	 */
	@PostConstruct
	public void constructKafkaURL() {
		this.kafkaURL = getKafkaTopicURL(getTopic());
		this.kafkaErrorTopicURL = getKafkaTopicURL(getErrorTopic());
	}

	private String getKafkaTopicURL(String topicName) {
		StringBuilder urlBuilder = new StringBuilder("kafka:" + topicName);
		
        if (!getCamelKafkaOptions().isEmpty()) {
        	urlBuilder.append(Constants.URL_QUERY_PARAM);
            getCamelKafkaOptions().forEach(
        		(key, value) -> {
	                if (StringUtils.isNotBlank(value)) {
	                	appendConfig(urlBuilder, key, value);
	                }
	            }
            );
        }
        return stripEnd(urlBuilder.toString());
	}

	private void appendConfig(StringBuilder urlBuilder, String key, String value) {
		urlBuilder.append(key).append(Constants.KEY_VALUE_SEPARATOR).append(value).append(Constants.URL_QUERY_PARAMS_DELIMITER);
	}
	
	private String stripEnd(String url) {
		return StringUtils.stripEnd(StringUtils.stripEnd(url, Constants.URL_QUERY_PARAMS_DELIMITER), Constants.URL_QUERY_PARAM);
	}
	
	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Map<String, String> getCamelKafkaOptions() {
		return camelKafkaOptions;
	}

	public String getKafkaURL() {
		return kafkaURL;
	}

	public String getKafkaErrorTopicURL() {
		return kafkaErrorTopicURL;
	}

	public String getErrorTopic() {
		return errorTopic;
	}

	public void setErrorTopic(String errorTopic) {
		this.errorTopic = errorTopic;
	}
}
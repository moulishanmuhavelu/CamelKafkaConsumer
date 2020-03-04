package com.example.camelkafkaconsumer.demo.processor;

import java.time.Duration;

import javax.annotation.PostConstruct;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.camelkafkaconsumer.demo.exception.KafkaConsumerErrorHandler;
import com.example.camelkafkaconsumer.demo.exception.ServerUnavailableException;

@Service("kafkaProcessor")
public class KafkaProcessor implements Processor {
	
	@Value("${server.healthcheck.url}")
	private String serverHealthCheckURL;
	
	@Value("${server.process.url}")
	private String serverProcessURL;
	
	@Value("${server.request.timeout}") 
	private long serverRequestTimeout;
	
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

	protected RestTemplate restTemplate;
	
	@PostConstruct
	public void postConstruct() {
		initializeRestTemplate(restTemplateBuilder);
	}

	/**
	 * Creates the instance of RestTemplate 
	 * @param restTemplateBuilder object to instantiate RestTemplate object
	 */
	protected void initializeRestTemplate(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.requestFactory(
				HttpComponentsClientHttpRequestFactory::new
		).setReadTimeout(Duration.ofMillis(serverRequestTimeout))
		 .build();
		this.restTemplate.setErrorHandler(new KafkaConsumerErrorHandler());
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void process(Exchange exchange) throws Exception {
        if (exchange.getIn() != null) {
            Message message = exchange.getIn();
            Object data = message.getBody();

            logger.info("topicName={}, partitionId={}, messageKey={}, currentOffset={}, message={}", 
            		message.getHeader(KafkaConstants.TOPIC), 
            		message.getHeader(KafkaConstants.PARTITION), 
            		message.getHeader(KafkaConstants.KEY),
            		message.getHeader(KafkaConstants.OFFSET), data);            
            processEvent(data);            
        }
	}

	/**
	 * This method call the server to process the data, if the server is unavailable, throws a rest client exception.
	 * 
	 * @param The data to process the event
	 * @return The HTTP Status
	 * @throws ServerUnavailableException Exception when server is unavailable
	 */
	public HttpStatus processEvent(Object data) throws ServerUnavailableException {		
		HttpStatus httpStatus = null;
		try {
			ResponseEntity<String> response = this.restTemplate.postForEntity(serverProcessURL, (String)data, String.class);
			httpStatus = response.getStatusCode();
			if (httpStatus.isError()) {
				throw new ServerUnavailableException(response.getBody());
			}
		} catch (RestClientException rce) {
			throw new ServerUnavailableException("RestClientException during rest call", rce); 
		}
		return httpStatus;
	
	}

	/**
	 * Checks if Server is available
	 * @return true or false based on server availability
	 */
	public boolean checkIfServerAvailable() {
		try {
			HttpStatus httpStatus = doHealthCheck();
			return (httpStatus.is2xxSuccessful());
		} catch (ServerUnavailableException e) {
			logger.error("Exception during health check", e);
			return false;
		}
	}
	
	/**
	 * @return The http Status
	 * @throws ServerUnavailableException Exception when server is unavailable
	 */
	public HttpStatus doHealthCheck() throws ServerUnavailableException {
		try {
			ResponseEntity<String> response = this.restTemplate.getForEntity(serverHealthCheckURL, String.class);			
			return response.getStatusCode();
		} catch (RestClientException rce) {
			throw new ServerUnavailableException("RestClientException occurred:", rce);
		}
	}
}

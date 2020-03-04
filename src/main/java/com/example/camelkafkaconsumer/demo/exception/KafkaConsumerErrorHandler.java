package com.example.camelkafkaconsumer.demo.exception;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class KafkaConsumerErrorHandler implements ResponseErrorHandler {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		HttpStatus.Series series = response.getStatusCode().series();
		return (HttpStatus.Series.CLIENT_ERROR.equals(series) || HttpStatus.Series.SERVER_ERROR.equals(series));
	}

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		logger.error("Server returned an error:responseCode:{}::responseStatusText:{}", response.getStatusCode(), response.getStatusText());

	}

}

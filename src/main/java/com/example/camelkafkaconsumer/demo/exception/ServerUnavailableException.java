package com.example.camelkafkaconsumer.demo.exception;

public class ServerUnavailableException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public ServerUnavailableException(String message) {
		super(message);
	}
	
	public ServerUnavailableException(String message, Throwable throwable) {
		super(message, throwable);
	}

}




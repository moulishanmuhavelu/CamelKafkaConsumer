# Getting Started

## Introduction
The purpose of this project is to reproduce the issue related to kafka consumer count number reducing from a defined value to default value inline with the ThrottlingExceptionRoutePolicy when defined.
This project describes the behaviour of how a ThrottlingExceptionRoutePolicy pitches in when a server is unavailable while processing the messages using Camel Kafka.
The project runs initially with 15 consumer threads, but drops to 1 after the consumers are restarted after the server is available.

### Pre-Requisites
* [JDK 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven 3.6](https://maven.apache.org/)
* [Apache Kafka 1.0.1](https://kafka.apache.org/downloads) or higher
 

### Setup
* Configure below properties in application.properties

##### Mandatory

	kafka.topic
	kafka.camelKafkaOptions.brokers
	kafka.camelKafkaOptions.groupId
	
##### Optional
	
	kafka.camelKafkaOptions.sslTruststorePassword
	kafka.camelKafkaOptions.sslTruststoreLocation
	kafka.camelKafkaOptions.sslKeystorePassword
	kafka.camelKafkaOptions.sslKeystoreLocation
	kafka.camelKafkaOptions.sslKeyPassword
	kafka.camelKafkaOptions.sslEndpointAlgorithm
	kafka.camelKafkaOptions.securityProtocol


### Order of Execution
Ensure that the below 2 applications are running before starting SimpleCamelKafkaConsumer application.

	SimpleRestInterface
	SimpleCamelKafkaProducer

### Steps to Execute
Run the application using the below command

		mvn package

		mvn spring-boot:run


### Actual Behavior
The below steps are the observation after application is started
1. KafkaConsumer calls the /process endpoint to process the message
2. Server times out and throws exception
3. Circuit is opened
4. Stopping consumers on the topic
5. Health check is performed by calling /healthCheck endpoint
6. HealthCheck succeeds and Circuit is Closed
7. All the kafka consumers start subscribing and un-subscribing one by one, the last thread remains active 

### Expected Behavior
The consumer application should run with defined number of threads after server is available.

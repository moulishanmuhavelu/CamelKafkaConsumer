# Getting Started

## Introduction
This is a sample project to consume messages from Kafka and show how to handle failure scenario when the downstream system is down. This consumer application will stop consuming messages when the downstream system is down and keep checking the health of the downstream system regularly. Once the downstream system is up again, the application will start consuming messages from Kafka. 

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
Ensure that the MySampleRestService application is running before starting CamelKafkaConsumer application.

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
7. Kafka Consumer starts consuming messages again 


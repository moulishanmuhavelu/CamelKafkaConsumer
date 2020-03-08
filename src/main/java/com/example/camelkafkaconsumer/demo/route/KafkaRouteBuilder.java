package com.example.camelkafkaconsumer.demo.route;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.KafkaManualCommit;
import org.apache.camel.impl.ThrottlingExceptionRoutePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.camelkafkaconsumer.demo.config.KafkaConfig;
import com.example.camelkafkaconsumer.demo.exception.ServerUnavailableException;
import com.example.camelkafkaconsumer.demo.processor.HttpStatus;
import com.example.camelkafkaconsumer.demo.processor.KafkaProcessor;
import com.example.camelkafkaconsumer.demo.processor.ResponseEntity;
import com.example.camelkafkaconsumer.demo.processor.RestClientException;

@Component
public class KafkaRouteBuilder extends RouteBuilder {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private KafkaProcessor kafkaProcessor;
	
	@Autowired
	private KafkaConfig kafkaConfig;

	/**
	 * Defines a Kafka Route with a ThrottlingExceptionRoutePolicy
	 */
	@Override
	public void configure() throws Exception {
		logger.info("configure():start");

		if (kafkaConfig.getTopic() != null 
				&& kafkaConfig.getCamelKafkaOptions() != null
				&& !kafkaConfig.getCamelKafkaOptions().isEmpty()) {

			from(kafkaConfig.getKafkaURL())
				.routePolicy(getThrottlingExceptionRoutePolicy())
				.process("kafkaProcessor")
				.choice()
					.when(getManualCommitPredicate())
						.process(this::doManualCommit)
					.otherwise()
						.end();
		}
	}

	private Predicate getManualCommitPredicate() {
		return PredicateBuilder.constant("true".equalsIgnoreCase(kafkaConfig.getCamelKafkaOptions().get("allowManualCommit")));
	}
	
	/**
	 * Defines a throttling route policy, The circuit is opened after 60secs
	 * Performs a health check if the server is available.
	 * Circuit is closed if the health check succeeds, otherwise retry after 60secs.
	 * @return
	 */
	private ThrottlingExceptionRoutePolicy getThrottlingExceptionRoutePolicy() {
		List<Class<?>> throttledExceptions = new ArrayList<Class<?>>();		
		throttledExceptions.add(ServerUnavailableException.class);
		ThrottlingExceptionRoutePolicy routePolicy = new ThrottlingExceptionRoutePolicy(1, 30000, 60000, throttledExceptions);
		routePolicy.setHalfOpenHandler(() -> {
			logger.debug("event=healthCheckStarted");
			return kafkaProcessor.checkIfPulseAvailable();
		});
		return routePolicy;
	}
	
	/**
	 * Performs a manual commit by checking if last in offset in the batch
	 * 
	 * @param exchange
	 */
	private void doManualCommit(Exchange exchange) {
		Boolean lastRecordOfBatch = exchange.getIn().getHeader(KafkaConstants.LAST_RECORD_BEFORE_COMMIT, Boolean.class);
		if (lastRecordOfBatch != null && lastRecordOfBatch) {
			KafkaManualCommit kafkaManualCommit = exchange.getIn().getHeader(KafkaConstants.MANUAL_COMMIT,
					KafkaManualCommit.class);
			if (kafkaManualCommit != null) {
				logger.info("Triggering manual commit");
				kafkaManualCommit.commitSync();
			}
		}
	}
}
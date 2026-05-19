package de.ullmann.fooddelivery.deliverservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public KafkaConsumerConfig() {
        log.info("KafkaConsumerConfig loaded");
    }

    @Bean
    public ConsumerFactory<String, OrderReadyForDeliveryEvent> orderReadyConsumerFactory() {
        JacksonJsonDeserializer<OrderReadyForDeliveryEvent> jsonDeserializer =
                new JacksonJsonDeserializer<>(OrderReadyForDeliveryEvent.class);
        jsonDeserializer.setUseTypeHeaders(false);
        jsonDeserializer.addTrustedPackages("de.ullmann.fooddelivery.*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer));
    }

    @Bean("orderReadyFactory")
    public ConcurrentKafkaListenerContainerFactory<String, OrderReadyForDeliveryEvent> orderReadyFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderReadyForDeliveryEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderReadyConsumerFactory());
        return factory;
    }
}

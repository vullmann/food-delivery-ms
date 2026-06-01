package de.ullmann.fooddelivery.orderservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.DriverAssignedEvent;
import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RequiredArgsConstructor
@EnableKafka
@Profile("!aws")
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final JsonMapper jsonMapper;


    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> targetType) {
        JacksonJsonDeserializer<T> jsonDeserializer = new JacksonJsonDeserializer<>(targetType, jsonMapper);
        jsonDeserializer.setUseTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer));
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(Class<T> targetType) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(targetType));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedEvent> orderConfirmedFactory() {
        return factory(OrderConfirmedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderInPreparationEvent> orderInPreparationFactory() {
        return factory(OrderInPreparationEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderReadyForDeliveryEvent> orderReadyForDeliveryFactory() {
        return factory(OrderReadyForDeliveryEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DriverAssignedEvent> driverAssignedFactory() {
        return factory(DriverAssignedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderOnTheWayEvent> orderOnTheWayFactory() {
        return factory(OrderOnTheWayEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderDeliveredEvent> orderDeliveredFactory() {
        return factory(OrderDeliveredEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RestaurantOrderCancelledEvent> restaurantOrderCancelledFactory() {
        return factory(RestaurantOrderCancelledEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeliveryCancelledEvent> deliveryCancelledFactory() {
        return factory(DeliveryCancelledEvent.class);
    }
}

package de.ullmann.fooddelivery.notificationservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import de.ullmann.fooddelivery.common.event.CustomerCreatedEvent;
import de.ullmann.fooddelivery.common.event.CustomerProfileUpdatedEvent;
import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;

@EnableKafka
@Configuration
@Profile("!aws")
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(Class<T> targetType) {
        JacksonJsonDeserializer<T> deserializer = new JacksonJsonDeserializer<>(targetType);
        deserializer.setUseTypeHeaders(false);
        deserializer.addTrustedPackages("de.ullmann.fooddelivery.*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new ErrorHandlingDeserializer<>(deserializer));

        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CustomerCreatedEvent> customerCreatedFactory() {
        return factory(CustomerCreatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CustomerProfileUpdatedEvent> customerProfileUpdatedFactory() {
        return factory(CustomerProfileUpdatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> orderPlacedFactory() {
        return factory(OrderPlacedEvent.class);
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

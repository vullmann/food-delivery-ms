package de.ullmann.fooddelivery.notificationservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConsumerConfigTest {

    private KafkaConsumerConfig config;

    @BeforeEach
    void setUp() {
        config = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
    }

    @Test
    void customerCreatedFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.customerCreatedFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    void customerProfileUpdatedFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.customerProfileUpdatedFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    void orderPlacedFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.orderPlacedFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    void orderConfirmedFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.orderConfirmedFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    void orderInPreparationFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.orderInPreparationFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    void orderReadyForDeliveryFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.orderReadyForDeliveryFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    void orderOnTheWayFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.orderOnTheWayFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    void orderDeliveredFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.orderDeliveredFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    void restaurantOrderCancelledFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.restaurantOrderCancelledFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    void deliveryCancelledFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.deliveryCancelledFactory();
        assertThat(factory).isNotNull();
    }
}

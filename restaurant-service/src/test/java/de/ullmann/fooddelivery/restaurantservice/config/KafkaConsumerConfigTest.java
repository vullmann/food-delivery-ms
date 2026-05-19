package de.ullmann.fooddelivery.restaurantservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
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
    void orderPlacedConsumerFactory_shouldReturnFactory() {
        ConsumerFactory<?, ?> factory = config.orderPlacedConsumerFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    void orderPlacedFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.orderPlacedFactory();
        assertThat(factory).isNotNull();
    }
}

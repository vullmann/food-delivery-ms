package de.ullmann.fooddelivery.deliverservice.config;

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
    void orderReadyConsumerFactory_shouldReturnFactory() {
        ConsumerFactory<?, ?> factory = config.orderReadyConsumerFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    void orderReadyFactory_shouldReturnFactory() {
        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.orderReadyFactory();
        assertThat(factory).isNotNull();
    }
}

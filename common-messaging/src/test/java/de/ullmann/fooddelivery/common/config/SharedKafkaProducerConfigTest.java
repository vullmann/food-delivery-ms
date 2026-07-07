package de.ullmann.fooddelivery.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

class SharedKafkaProducerConfigTest {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private SharedKafkaProducerConfig config;

    @BeforeEach
    void setUp() throws Exception {
        config = new SharedKafkaProducerConfig();
        Field field = SharedKafkaProducerConfig.class.getDeclaredField("bootstrapServers");
        field.setAccessible(true);
        field.set(config, BOOTSTRAP_SERVERS);
    }

    @Test
    void producerFactory_shouldReturnDefaultKafkaProducerFactory() {
        ProducerFactory<String, Object> factory = config.producerFactory();

        assertThat(factory).isInstanceOf(DefaultKafkaProducerFactory.class);
    }

    @Test
    void producerFactory_shouldConfigureBootstrapServers() {
        DefaultKafkaProducerFactory<String, Object> factory =
                (DefaultKafkaProducerFactory<String, Object>) config.producerFactory();

        assertThat(factory.getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    }

    @Test
    void producerFactory_shouldUseStringSerializerForKey() {
        DefaultKafkaProducerFactory<String, Object> factory =
                (DefaultKafkaProducerFactory<String, Object>) config.producerFactory();

        assertThat(factory.getConfigurationProperties())
                .containsEntry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    }

    @Test
    void producerFactory_shouldUseJacksonJsonSerializerForValue() {
        DefaultKafkaProducerFactory<String, Object> factory =
                (DefaultKafkaProducerFactory<String, Object>) config.producerFactory();

        assertThat(factory.getConfigurationProperties())
                .containsEntry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
    }

    @Test
    void producerFactory_shouldEnableTypeInfoHeaders() {
        DefaultKafkaProducerFactory<String, Object> factory =
                (DefaultKafkaProducerFactory<String, Object>) config.producerFactory();

        assertThat(factory.getConfigurationProperties())
                .containsEntry(JacksonJsonSerializer.ADD_TYPE_INFO_HEADERS, true);
    }

    @Test
    void kafkaTemplate_shouldReturnNonNullTemplate() {
        KafkaTemplate<String, Object> template = config.kafkaTemplate();

        assertThat(template).isNotNull();
    }

    @Test
    void kafkaTemplate_shouldUseProducerFactoryWithSameConfig() {
        ProducerFactory<String, Object> expectedFactory = config.producerFactory();
        KafkaTemplate<String, Object> template = config.kafkaTemplate();

        Map<String, Object> templateProps =
                ((DefaultKafkaProducerFactory<String, Object>) template.getProducerFactory())
                        .getConfigurationProperties();
        Map<String, Object> factoryProps =
                ((DefaultKafkaProducerFactory<String, Object>) expectedFactory)
                        .getConfigurationProperties();
        assertThat(templateProps).isEqualTo(factoryProps);
    }

    @Test
    void outboxKafkaTemplate_shouldReturnNonNullTemplate() {
        KafkaTemplate<String, String> template = config.outboxKafkaTemplate();

        assertThat(template).isNotNull();
    }

    @Test
    void outboxKafkaTemplate_shouldConfigureBootstrapServers() {
        DefaultKafkaProducerFactory<String, String> factory =
                (DefaultKafkaProducerFactory<String, String>) config.outboxKafkaTemplate().getProducerFactory();

        assertThat(factory.getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    }

    @Test
    void outboxKafkaTemplate_shouldUseStringSerializerForKey() {
        DefaultKafkaProducerFactory<String, String> factory =
                (DefaultKafkaProducerFactory<String, String>) config.outboxKafkaTemplate().getProducerFactory();

        assertThat(factory.getConfigurationProperties())
                .containsEntry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    }

    @Test
    void outboxKafkaTemplate_shouldUseStringSerializerForValue() {
        DefaultKafkaProducerFactory<String, String> factory =
                (DefaultKafkaProducerFactory<String, String>) config.outboxKafkaTemplate().getProducerFactory();

        assertThat(factory.getConfigurationProperties())
                .containsEntry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    }
}

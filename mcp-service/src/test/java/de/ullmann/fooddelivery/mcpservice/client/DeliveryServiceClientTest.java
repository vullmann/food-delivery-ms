package de.ullmann.fooddelivery.mcpservice.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.ullmann.fooddelivery.mcpservice.dto.DeliveryResponse;

class DeliveryServiceClientTest {

    private DeliveryServiceClient client;
    private MockRestServiceServer server;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Initialize ObjectMapper and register the Java 8 Time module for LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 1. Grab a clean, standard builder shell
        RestClient.Builder builder = RestClient.builder();

        // 2. Bind the Mock Server directly to that builder shell
        server = MockRestServiceServer.bindTo(builder).build();

        // 3. Construct the real client directly, bypassing Spring's @Qualifier entirely!
        client = new DeliveryServiceClient(builder.build());
    }

    @Test
    void getDeliveryByOrderId_success_shouldReturnDelivery() throws Exception {
        DeliveryResponse expected = new DeliveryResponse("del-id", "ord-id", "PENDING", null,
                LocalDateTime.now(), LocalDateTime.now());

        String jsonPayload = objectMapper.writeValueAsString(expected);

        server.expect(requestTo("/deliveries?orderId=ord-id"))
                .andRespond(withSuccess(jsonPayload, MediaType.APPLICATION_JSON));

        Optional<DeliveryResponse> result = client.getDeliveryByOrderId("ord-id");

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("del-id");
        server.verify();
    }

    @Test
    void getDeliveryByOrderId_notFound_shouldReturnEmpty() {

        String orderId = "missing-ord-123";

        server.expect(requestTo("/deliveries?orderId=" + orderId))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<DeliveryResponse> result = client.getDeliveryByOrderId(orderId);

        assertThat(result).isEmpty();
        server.verify();
    }
}

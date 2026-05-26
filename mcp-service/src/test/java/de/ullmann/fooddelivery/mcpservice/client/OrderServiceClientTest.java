package de.ullmann.fooddelivery.mcpservice.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.ullmann.fooddelivery.mcpservice.dto.OrderResponse;

class OrderServiceClientTest {

    private OrderServiceClient client;
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
        client = new OrderServiceClient(builder.build());
    }

    // ------------------------------------------------------------------
    // Tests: getOrderById
    // ------------------------------------------------------------------

    @Test
    void getOrderById_success_shouldReturnOrder() throws Exception { // Added throws Exception
        OrderResponse expected = new OrderResponse("ord-id", "cust-id", "rest-id", "PLACED",
                BigDecimal.valueOf(25.00), List.of(), LocalDateTime.now());
        String jsonPayload = objectMapper.writeValueAsString(expected);

        server.expect(requestTo("/orders/ord-id"))
                .andRespond(withSuccess(jsonPayload, MediaType.APPLICATION_JSON));

        Optional<OrderResponse> result = client.getOrderById("ord-id");

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("ord-id");
        server.verify();
    }

    @Test
    void getOrderById_notFound_shouldReturnEmptyOptional() {
        server.expect(requestTo("/orders/invalid-id"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<OrderResponse> result = client.getOrderById("invalid-id");

        assertThat(result).isEmpty();
        server.verify();
    }

    // ------------------------------------------------------------------
    // Tests: getOrdersByCustomer (Completely Mockito-Free)
    // ------------------------------------------------------------------

    @Test
    void getOrdersByCustomer_success_shouldReturnList() throws Exception { // Added throws Exception
        List<OrderResponse> expected = List.of(
                new OrderResponse("ord-id", "cust-id", "rest-id", "PLACED",
                        BigDecimal.valueOf(25.00), List.of(), LocalDateTime.now()));
        String jsonPayload = objectMapper.writeValueAsString(expected);

        server.expect(requestTo("/orders/customer/cust-id"))
                .andRespond(withSuccess(jsonPayload, MediaType.APPLICATION_JSON));

        List<OrderResponse> result = client.getOrdersByCustomer("cust-id");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("ord-id");
        server.verify();
    }

    @Test
    void getOrdersByCustomer_onException_shouldReturnEmptyList() {
        server.expect(requestTo("/orders/customer/cust-id"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        List<OrderResponse> result = client.getOrdersByCustomer("cust-id");

        assertThat(result).isEmpty();
        server.verify();
    }

    @Test
    void getOrderById_networkError_shouldReturnEmpty() {
        server.expect(requestTo("/orders/ord-id"))
                .andRespond(withException(new IOException("Connection refused")));

        Optional<OrderResponse> result = client.getOrderById("ord-id");

        assertThat(result).isEmpty();
        server.verify();
    }

    @Test
    void getOrdersByCustomer_networkError_shouldReturnEmptyList() {
        server.expect(requestTo("/orders/customer/cust-id"))
                .andRespond(withException(new IOException("Connection refused")));

        List<OrderResponse> result = client.getOrdersByCustomer("cust-id");

        assertThat(result).isEmpty();
        server.verify();
    }

    @Test
    void getOrdersByCustomer_nullBody_shouldReturnEmptyList() {
        server.expect(requestTo("/orders/customer/cust-id"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        List<OrderResponse> result = client.getOrdersByCustomer("cust-id");

        assertThat(result).isEmpty();
        server.verify();
    }
}


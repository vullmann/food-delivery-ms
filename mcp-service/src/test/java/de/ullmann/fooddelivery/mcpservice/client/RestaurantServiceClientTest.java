package de.ullmann.fooddelivery.mcpservice.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.mcpservice.dto.RestaurantResponse;

class RestaurantServiceClientTest {

    private RestaurantServiceClient client;
    private MockRestServiceServer server;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RestaurantServiceClient(builder.build());
    }

    @Test
    void getRestaurantById_success_shouldReturnRestaurant() throws Exception {
        RestaurantResponse expected = new RestaurantResponse("rest-id", "Trattoria", "Italian", "ITALIAN", true, null);
        String jsonPayload = objectMapper.writeValueAsString(expected);

        server.expect(requestTo("/restaurants/rest-id"))
                .andRespond(withSuccess(jsonPayload, MediaType.APPLICATION_JSON));

        Optional<RestaurantResponse> result = client.getRestaurantById("rest-id");

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("rest-id");
        server.verify();
    }

    @Test
    void getRestaurantById_notFound_shouldReturnEmpty() {
        server.expect(requestTo("/restaurants/missing-id"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<RestaurantResponse> result = client.getRestaurantById("missing-id");

        assertThat(result).isEmpty();
        server.verify();
    }

    @Test
    void getRestaurantById_serverError_shouldReturnEmpty() {
        server.expect(requestTo("/restaurants/rest-id"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        Optional<RestaurantResponse> result = client.getRestaurantById("rest-id");

        assertThat(result).isEmpty();
        server.verify();
    }
}

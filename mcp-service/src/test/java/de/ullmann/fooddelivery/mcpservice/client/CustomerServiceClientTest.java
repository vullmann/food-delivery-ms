package de.ullmann.fooddelivery.mcpservice.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.ullmann.fooddelivery.mcpservice.dto.CustomerResponse;

class CustomerServiceClientTest {

    private CustomerServiceClient client;
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
        client = new CustomerServiceClient(builder.build());
    }


    @Test
    void getCustomerById_success_shouldReturnCustomer() throws Exception {
        // 1. Arrange: Erwartete Daten vorbereiten
        CustomerResponse expected = new CustomerResponse("cust-id", "John", "Doe", "john@doe.com", "+49123", null);
        String jsonResponse = objectMapper.writeValueAsString(expected);

        // 2. Expectation auf dem Mock-Server definieren (Simuliert die HTTP-Antwort)
        this.server.expect(requestTo("/customers/cust-id")) // Matcher für die URI
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // 3. Act: call method
        Optional<CustomerResponse> result = client.getCustomerById("cust-id");

        // 4. Assert: Ergebnisse prüfen
        assertThat(result).contains(expected);

        // Verifiziert, dass alle erwarteten HTTP-Aufrufe getätigt wurden
        this.server.verify();
    }

    @Test
    void getCustomerByEmail_success_shouldReturnCustomer() throws Exception {
        String customerEmail = "anEmail";

        // 1. Arrange: Erwartete Daten vorbereiten
        CustomerResponse expected = new CustomerResponse("cust-id", "John", "Doe", customerEmail, "+49123", null);
        String jsonResponse = objectMapper.writeValueAsString(expected);

        // 2. Expectation auf dem Mock-Server definieren (Simuliert die HTTP-Antwort)
        this.server.expect(requestTo("/customers?email=" + customerEmail))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // 3. Act: call method
        Optional<CustomerResponse> result = client.getCustomerByEmail(customerEmail);

        // 4. Assert: Ergebnisse prüfen
        assertThat(result).contains(expected);

        // Verifiziert, dass alle erwarteten HTTP-Aufrufe getätigt wurden
        this.server.verify();
    }

    @Test
    void getCustomerById_onException_shouldReturnEmpty() {

        String customerId = "0815";

        // 3. Stub the network exception
        server.expect(requestTo("/customers/" + customerId))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // 4. Execute & Assert
        Optional<CustomerResponse> result = client.getCustomerById(customerId);

        assertThat(result).isEmpty();
        server.verify();
    }


    @Test
    void getCustomerByEmail_onException_shouldReturnEmpty() {

        String customerEmail = "notExisting";

        // 3. Stub the network exception
        server.expect(requestTo("/customers?email=" + customerEmail))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // 4. Execute & Assert
        Optional<CustomerResponse> result = client.getCustomerByEmail(customerEmail);

        assertThat(result).isEmpty();
        server.verify();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCustomerById_networkError_shouldReturnEmpty() {
        RestClient.RequestHeadersUriSpec<?> uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec<?> headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        RestClient mockRestClient = mock(RestClient.class);

        when(mockRestClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) uriSpec);
        when(uriSpec.uri(anyString(), (Object) any())).thenReturn((RestClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CustomerResponse.class)).thenThrow(new ResourceAccessException("timeout"));

        Optional<CustomerResponse> result = new CustomerServiceClient(mockRestClient).getCustomerById("cust-id");

        assertThat(result).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCustomerByEmail_networkError_shouldReturnEmpty() {
        RestClient.RequestHeadersUriSpec<?> uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec<?> headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        RestClient mockRestClient = mock(RestClient.class);

        when(mockRestClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) uriSpec);
        when(uriSpec.uri(anyString(), (Object) any())).thenReturn((RestClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CustomerResponse.class)).thenThrow(new ResourceAccessException("timeout"));

        Optional<CustomerResponse> result = new CustomerServiceClient(mockRestClient).getCustomerByEmail("test@email.com");

        assertThat(result).isEmpty();
    }
}

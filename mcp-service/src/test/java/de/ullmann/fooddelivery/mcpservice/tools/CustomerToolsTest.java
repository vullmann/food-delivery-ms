package de.ullmann.fooddelivery.mcpservice.tools;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.mcpservice.client.CustomerServiceClient;
import de.ullmann.fooddelivery.mcpservice.dto.CustomerResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerToolsTest {

    @Mock
    private CustomerServiceClient customerServiceClient;

    @InjectMocks
    private CustomerTools customerTools;

    private static final String CUSTOMER_ID = "cust-id-123";
    private static final String EMAIL = "john@doe.com";

    @Test
    void getCustomer_whenFound_shouldReturnFormattedInfo() {
        CustomerResponse.Address address = new CustomerResponse.Address("Main St", "1", "Berlin", "10115", "Germany");
        CustomerResponse customer = new CustomerResponse(CUSTOMER_ID, "John", "Doe", EMAIL, "+49123", address);
        when(customerServiceClient.getCustomerById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        String result = customerTools.getCustomer(CUSTOMER_ID);

        assertThat(result).contains("John").contains("Doe").contains(EMAIL).contains("+49123");
    }

    @Test
    void getCustomer_whenNotFound_shouldReturnNotFoundMessage() {
        when(customerServiceClient.getCustomerById(CUSTOMER_ID)).thenReturn(Optional.empty());

        String result = customerTools.getCustomer(CUSTOMER_ID);

        assertThat(result).contains("No customer found").contains(CUSTOMER_ID);
    }

    @Test
    void getCustomer_whenAddressIsNull_shouldNotIncludeAddress() {
        CustomerResponse customer = new CustomerResponse(CUSTOMER_ID, "John", "Doe", EMAIL, "+49123", null);
        when(customerServiceClient.getCustomerById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        String result = customerTools.getCustomer(CUSTOMER_ID);

        assertThat(result).contains("John");
    }

    @Test
    void findCustomerByEmail_whenFound_shouldReturnFormattedInfo() {
        CustomerResponse customer = new CustomerResponse(CUSTOMER_ID, "John", "Doe", EMAIL, "+49123", null);
        when(customerServiceClient.getCustomerByEmail(EMAIL)).thenReturn(Optional.of(customer));

        String result = customerTools.findCustomerByEmail(EMAIL);

        assertThat(result).contains(CUSTOMER_ID).contains("John");
    }

    @Test
    void findCustomerByEmail_whenNotFound_shouldReturnNotFoundMessage() {
        when(customerServiceClient.getCustomerByEmail(EMAIL)).thenReturn(Optional.empty());

        String result = customerTools.findCustomerByEmail(EMAIL);

        assertThat(result).contains("No customer found").contains(EMAIL);
    }
}

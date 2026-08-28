package de.ullmann.fooddelivery.customerservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.customerservice.dto.AddressRequest;
import de.ullmann.fooddelivery.customerservice.dto.UpdateCustomerRequest;
import de.ullmann.fooddelivery.customerservice.entity.Customer;
import de.ullmann.fooddelivery.customerservice.exception.CustomerNotFoundException;
import de.ullmann.fooddelivery.customerservice.exception.GlobalExceptionHandler;
import de.ullmann.fooddelivery.customerservice.service.CustomerService;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "spring.jpa.open-in-view=false")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private CustomerService customerService;

    // --- GET /customers/{id} ---

    @Test
    void getCustomer_shouldReturnCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        Customer customer = Customer.create("John", "Doe", "john.doe@example.com",
                "+49123456789", berlinAddress());

        when(customerService.findCustomer(id)).thenReturn(customer);

        mockMvc.perform(get("/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void getCustomer_shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        when(customerService.findCustomer(id)).thenThrow(new CustomerNotFoundException(id));

        mockMvc.perform(get("/customers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Customer not found: " + id));
    }

    // --- GET /customers?email= ---

    @Test
    void findByEmail_shouldReturnCustomer() throws Exception {
        String email = "john.doe@example.com";
        Customer customer = Customer.create("John", "Doe", email,
                "+49123456789", berlinAddress());

        when(customerService.findCustomerByEmail(email)).thenReturn(customer);

        mockMvc.perform(get("/customers").param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void findByEmail_shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        String email = "nonexistent@example.com";

        when(customerService.findCustomerByEmail(email)).thenThrow(new CustomerNotFoundException(email));

        mockMvc.perform(get("/customers").param("email", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Customer not found: " + email));
    }

    // --- GET /customers (no params) ---

    @Test
    void findAllCustomers_shouldReturnListOfCustomers() throws Exception {
        Customer c1 = Customer.create("John", "Doe", "john.doe@example.com",
                "+49123456789", berlinAddress());
        Customer c2 = Customer.create("Jane", "Smith", "jane.smith@example.com",
                "+49987654321", munichAddress());

        when(customerService.findAllCustomers()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    // --- PUT /customers/{id} ---

    @Test
    void updateCustomer_shouldReturnUpdatedCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateCustomerRequest request = buildUpdateRequest("Jane", "Smith");
        Customer customer = Customer.create("Jane", "Smith", "john.doe@example.com",
                "+49987654321", munichAddress());

        when(customerService.updateCustomer(eq(id), any())).thenReturn(customer);

        mockMvc.perform(put("/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.phone").value("+49987654321"));
    }

    @Test
    void updateCustomer_shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        when(customerService.updateCustomer(eq(id), any())).thenThrow(new CustomerNotFoundException(id));

        mockMvc.perform(put("/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUpdateRequest("Jane", "Smith"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCustomer_shouldReturnBadRequestWhenFirstNameIsBlank() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUpdateRequest("", "Smith"))))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE /customers/{id} ---

    @Test
    void deleteCustomer_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(customerService).deleteCustomer(id);

        mockMvc.perform(delete("/customers/{id}", id))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer(id);
    }

    @Test
    void deleteCustomer_shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new CustomerNotFoundException(id)).when(customerService).deleteCustomer(id);

        mockMvc.perform(delete("/customers/{id}", id))
                .andExpect(status().isNotFound());
    }

    // --- Helpers ---

    private UpdateCustomerRequest buildUpdateRequest(String firstName, String lastName) {
        return new UpdateCustomerRequest(firstName, lastName, "+49987654321",
                new AddressRequest("New St", "456", "Munich", "80331", "Germany"));
    }

    private Address berlinAddress() {
        return Address.of("Main St", "123", "Berlin", "10115", "Germany");
    }

    private Address munichAddress() {
        return Address.of("New St", "456", "Munich", "80331", "Germany");
    }
}
